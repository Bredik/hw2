package org.example;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

/**
 * Объект груза
 */
@EqualsAndHashCode
@ToString
public class Block implements Comparable<Block> {

    public final String uuid = UUID.randomUUID().toString();
    private final String name;

    public Block(int i) {
        this.name = String.valueOf(i);
    }

    @Override
    public int compareTo(Block o) {
        return uuid.compareTo(o.uuid);
    }


}
