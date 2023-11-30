package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class TextValue extends Value {

    protected String value;

    public TextValue(Object value) {
        setValue(value);
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    //TODO: to allow empty values
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .map(Object::toString)
            .filter(not(String::isEmpty))
            .orElse(null);
    }

    @Override
    public Object value() {
        return value;
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
