package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
@Getter
public class Warehouse extends Thread {

    private final List<Block> storage = new ArrayList<>();
    private final Queue<Truck> trucks = new ArrayDeque<>();

    public Warehouse(String name) {
        super(name);
    }

    public Warehouse(String name, Collection<Block> initialStorage) {
        this(name);
        storage.addAll(initialStorage);
    }

    @Override
    public void run() {
        Truck truck;
        while (!currentThread().isInterrupted()) {
            truck = getNextArrivedTruck();
            if (truck == null) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    if (currentThread().isInterrupted()) {
                        break;
                    }
                }
                continue;
            }
            if (truck.getBlocks().isEmpty()) {
                loadTruck(truck);
            } else {
                unloadTruck(truck);
            }
        }
        log.info("Warehouse thread interrupted");
    }

    private void loadTruck(Truck truck) {
        Collection<Block> blocksToLoad = getFreeBlocks(truck.getCapacity());
        log.info("Грузим в грузовик {}, {} блоков, со склада {}", truck.getName(), blocksToLoad.size(), this.getName());
        try {
            sleep(10L * blocksToLoad.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while loading truck", e);
        }
        if (blocksToLoad.isEmpty()) {
            return;
        }
        truck.getBlocks().addAll(blocksToLoad);
        log.info("Грузовик {} загружен. Кол-во блоков {}", truck.getName(), blocksToLoad.size());
        synchronized (truck) {
            truck.notifyAll(); //отпускаем грузовик когда его загрузили
        }
    }

    private synchronized Collection<Block> getFreeBlocks(int maxItems) {
        //TODO необходимо реализовать потокобезопасную логику по получению свободных блоков
        //TODO 1 блок грузится в 1 грузовик, нельзя клонировать блоки во время загрузки
        List<Block> blocks = new ArrayList<>();
        for (int i = 0; i < maxItems; i++) {
            blocks.add(storage.remove(0));
        }
        return blocks;
    }

    private void returnBlocksToStorage(List<Block> returnedBlocks) {
        log.info("Грузим блоки на склад, выгрузили {} блоков", returnedBlocks.size());
        synchronized (storage) {
            storage.addAll(returnedBlocks);
        }
        //TODO реализовать потокобезопасную логику по возврату блоков на склад
    }

    private void unloadTruck(Truck truck) {
        log.info("Разгружаем грузовик {}, у него {} блоков", truck.getName(), truck.getBlocks().size());
        List<Block> arrivedBlocks = truck.getBlocks();
        try {
            sleep(10L * arrivedBlocks.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while unloading truck", e);
        }
        returnBlocksToStorage(arrivedBlocks);
        truck.getBlocks().clear();
        synchronized (truck) {
            truck.notifyAll(); //отпускаем грузовик когда его разгрузили
        }
        log.info("Разгрузка грузовика {} завершена, на складе {}, всего {} блоков", truck.getName(), this.getName(), storage.size());
    }

    private Truck getNextArrivedTruck() {
        return trucks.poll();
        //TODO необходимо реализовать логику по получению следующего прибывшего грузовика внутри потока склада
    }
    
    public void arrive(Truck truck) {
        log.info("Грузовик {}, прибыл на склад {}, на складе {} блоков", truck.getName(), this.getName(), this.storage.size());
        try {
            trucks.add(truck);
            synchronized (truck) {
                truck.wait(); //когда грузовик прибывает, просим его подождать
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //TODO необходимо реализовать логику по сообщению потоку склада о том, что грузовик приехал
        //TODO так же дождаться разгрузки блоков, при возврате из этого метода - грузовик покинет склад
    }
}
