package org.cosinus.streamer.api.value;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class Value implements Comparable<Value> {

    public Value() {
        injectContext(this);
    }

    public abstract boolean isNumeric();
}
