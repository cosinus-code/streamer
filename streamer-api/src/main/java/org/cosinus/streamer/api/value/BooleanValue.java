package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.ObjectUtils.compare;

public class BooleanValue extends Value {

    protected final Integer value;

    public BooleanValue(Integer value) {
        this.value = value;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof BooleanValue integerValue) {
            return Integer.compare(value, integerValue.value);
        }
        return compare(this, other);
    }
}
