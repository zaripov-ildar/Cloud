package ru.starstreet.cloud.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class BigFile extends AbstractMessage {
    private byte[] bytes;
    private long position;
    private int bytesToRead;
    private String departure;
    private String destination;
    private long size;

//    @Override
//    public String toString() {
//        String file = Path.of(destination).getFileName().toString();
//        return String.format("%s:\t%.2f\n",file, (float)position/size);
//    }
}
