package org.cosinus.streamer.api.value;

import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class LongValue extends Value {

    protected Long value;

    public LongValue(Long value) {
        this.value = value;
    }

    public LongValue(Object value) {
        setValue(value);
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .or(() -> ofNullable(value)
                .map(Object::toString)
                .filter(not(String::isEmpty))
                .map(Long::valueOf))
            .orElse(null);
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof LongValue longValue) {
            return Long.compare(value, longValue.value);
        }
        return compare(this, other);
    }
}
