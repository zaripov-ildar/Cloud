package ru.starstreet.cloud.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
public class PackedFileMessage extends AbstractMessage {
    private String path;
    private byte[] bytes;
}
