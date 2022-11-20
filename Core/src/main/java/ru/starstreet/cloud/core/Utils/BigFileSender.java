package ru.starstreet.cloud.core.Utils;

import ru.starstreet.cloud.core.Chunk;

@FunctionalInterface
public interface BigFileSender {
    void sendFile(Chunk file);
}
