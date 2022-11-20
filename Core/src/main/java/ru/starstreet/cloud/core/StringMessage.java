package ru.starstreet.cloud.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class StringMessage extends AbstractMessage {
    private Command cmd;
    private String argument;

}
