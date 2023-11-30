package org.cosinus.streamer.api.value;

import org.cosinus.swing.format.FormatHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class MemoryValue extends LongValue {

    @Autowired
    private FormatHandler formatHandler;

    public MemoryValue(Long value) {
        super(value);
    }

    public MemoryValue(Object value) {
        super(value);
    }

    @Override
    public String toString() {
        return ofNullable(value)
            .map(formatHandler::formatMemorySize)
            .orElse("");
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof MemoryValue memoryValue) {
            return Long.compare(value, memoryValue.value);
        }
        return compare(this, other);
    }
}
