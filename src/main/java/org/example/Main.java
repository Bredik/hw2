package org.example;

import lombok.extern.log4j.Log4j2;

import java.util.*;

import static java.util.Arrays.asList;

@Log4j2
public class Main {
    public static void main(String[] args) {
        System.out.println("гоу!");
        List<Block> initialBlocks = new ArrayList<>();
        for (int i = 0; i < 10; i++) { //todo было 10_000
            initialBlocks.add(new Block());
        }

        Warehouse fabric = new Warehouse("Fabric", initialBlocks);
        Warehouse retail = new Warehouse("Retail");

        List<Warehouse> route = asList(fabric, retail);

        route.forEach(Thread::start); //запускаем потоки складов

        List<Truck> trucks = new ArrayList<>();

        for (int i = 0; i < 1; i++) { //todo было 10
            trucks.add(new Truck("Грузовик " + i, 1000, route));
        }

        trucks.forEach(Thread::start); //запускаем потоки грузовиков

        trucks.forEach(truck -> {
            try {
                truck.join(); //ожидаем завершение работы всех грузовиков
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        });

        route.forEach(Thread::interrupt); //прерываем работу складов

        Collections.sort(initialBlocks);

        List<Block> deliveredBlocks = new ArrayList<>(retail.getStorage());

        Collections.sort(deliveredBlocks);

        //Сверяем, что все блоки были доставлены
        if (initialBlocks.equals(deliveredBlocks)) {
            log.info("Correct delivery");
        } else {
            log.error("Error in delivery");
        }
    }
}