package ru.starstreet.cloud.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;



@EqualsAndHashCode(callSuper=false)
@Data
@AllArgsConstructor
public class Message extends AbstractMessage {
    private String message;

}
