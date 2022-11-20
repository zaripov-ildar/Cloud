package ru.starstreet.cloud.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Chunk extends AbstractMessage {
    private byte[] bytes;
    private long position;
    private int bytesToRead;
    private String departure;
    private String destination;
    private long size;
}
