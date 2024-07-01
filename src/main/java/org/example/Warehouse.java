package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Log4j2
@Getter
public class Warehouse extends Thread {

    private final List<Block> storage = new ArrayList<>();

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
        log.info("Грузим в грузовик {}", truck.getName());
        Collection<Block> blocksToLoad = getFreeBlocks(truck.getCapacity());
        try {
            sleep(10L * blocksToLoad.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while loading truck", e);
        }
        truck.getBlocks().addAll(blocksToLoad);
        log.info("Грузовик загружен {}", truck.getName());
    }

    private Collection<Block> getFreeBlocks(int maxItems) {
        //TODO необходимо реализовать потокобезопасную логику по получению свободных блоков
        //TODO 1 блок грузится в 1 грузовик, нельзя клонировать блоки во время загрузки
        List<Block> blocks = new ArrayList<>();
        for (int i = 0; i < maxItems; i++) {
            blocks.add(new Block());
        }
        return blocks;
    }

    private void returnBlocksToStorage(List<Block> returnedBlocks) {
        //TODO реализовать потокобезопасную логику по возврату блоков на склад
    }

    private void unloadTruck(Truck truck) {
        log.info("Разгружаем грузовик {}", truck.getName());
        List<Block> arrivedBlocks = truck.getBlocks();
        try {
            sleep(100L * arrivedBlocks.size());
        } catch (InterruptedException e) {
            log.error("Interrupted while unloading truck", e);
        }
        returnBlocksToStorage(arrivedBlocks);
        truck.getBlocks().clear();
        log.info("Разгрузка завершена {}", truck.getName());
    }

    private Truck getNextArrivedTruck() {
        //TODO необходимо реализовать логику по получению следующего прибывшего грузовика внутри потока склада
        return null;
    }


    public void arrive(Truck truck) {
        //TODO необходимо реализовать логику по сообщению потоку склада о том, что грузовик приехал
        //TODO так же дождаться разгрузки блоков, при возврате из этого метода - грузовик покинет склад
    }
}
