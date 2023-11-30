package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class IntegerValue extends Value {

    protected Integer value;

    public IntegerValue(Integer value) {
        this.value = value;
    }

    public IntegerValue(Object value) {
        setValue(value);
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .or(() -> ofNullable(value)
                .map(Object::toString)
                .filter(not(String::isEmpty))
                .map(Integer::valueOf))
            .orElse(null);
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof IntegerValue integerValue) {
            return Integer.compare(value, integerValue.value);
        }
        return compare(this, other);
    }
}
