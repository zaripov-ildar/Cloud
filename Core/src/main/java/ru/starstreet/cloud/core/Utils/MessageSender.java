package ru.starstreet.cloud.core.Utils;

import ru.starstreet.cloud.core.AbstractMessage;

@FunctionalInterface
public interface MessageSender {
    void send(AbstractMessage message);
}
