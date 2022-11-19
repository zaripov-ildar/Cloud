package ru.starstreet.cloud.core.Utils;

import ru.starstreet.cloud.core.BigFile;

@FunctionalInterface
public interface BigFileSender {
    void sendFile(BigFile file);
}
