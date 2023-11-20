package org.cosinus.streamer.api.value;

import static java.util.Optional.ofNullable;

public class TextValue extends Value {

    protected String value;

    public TextValue(Object value) {
        this.value = ofNullable(value)
            .map(Object::toString)
            .orElse(null);
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public String toString() {
        return value;
    }
}
