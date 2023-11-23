package org.cosinus.streamer.api;

import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public interface Streamable {

    String getId();

    String getName();

    Streamer<?> getStreamer();

    public boolean isParent();

    boolean isHidden();

    boolean isLink();

    String getIconName();

    public Path getPath();

    default List<TranslatableName> detailNames() {
        return emptyList();
    }

    default Map<TranslatableName, Value> details() {
        return emptyMap();
    }
}
