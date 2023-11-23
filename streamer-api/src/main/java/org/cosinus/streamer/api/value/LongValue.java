package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.ObjectUtils.compare;

public class LongValue extends Value {

    protected final Long value;

    public LongValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof LongValue longValue) {
            return Long.compare(value, longValue.value);
        }
        return compare(this, other);
    }
}
