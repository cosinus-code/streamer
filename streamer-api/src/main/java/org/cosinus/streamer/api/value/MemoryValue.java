package org.cosinus.streamer.api.value;

import org.cosinus.swing.format.FormatHandler;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;

public class MemoryValue extends Value {

    @Autowired
    private FormatHandler formatHandler;

    protected Long value;

    public MemoryValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String toString() {
        return ofNullable(value)
            .map(formatHandler::formatMemorySize)
            .orElse("");
    }
}
