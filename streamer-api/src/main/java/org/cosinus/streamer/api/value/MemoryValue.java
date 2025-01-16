package org.cosinus.streamer.api.value;

import org.cosinus.swing.format.FormatHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class MemoryValue extends LongValue {

    private final boolean computing;

    @Autowired
    private FormatHandler formatHandler;

    public MemoryValue(Long value) {
        this(value, false);
    }

    public MemoryValue(Long value, boolean computing) {
        super(value);
        this.computing = computing;
    }

    @Override
    public String toString() {
        String memorySize = ofNullable(value)
            .map(formatHandler::formatMemorySize)
            .orElse("");
        return computing ? "...".concat(memorySize) : memorySize;
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof MemoryValue memoryValue) {
            return Long.compare(value, memoryValue.value);
        }
        return compare(this, other);
    }
}
