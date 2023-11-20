package org.cosinus.streamer.api.value;

public class IntegerValue extends Value {

    protected final Integer value;

    public IntegerValue(Integer value) {
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
}
