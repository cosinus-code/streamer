package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.compare;

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

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof TextValue textValue) {
            return compare(value, textValue.value);
        }
        return compare(this, other);
    }
}
