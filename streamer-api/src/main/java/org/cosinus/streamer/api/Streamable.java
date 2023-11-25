package org.cosinus.streamer.api;

import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.getExtension;

public interface Streamable {

    Path getPath();

    String getProtocol();

    default String getId() {
        return ofNullable(getPath())
            .map(Path::toString)
            .map(path -> ofNullable(getProtocol())
                .map(protocol -> protocol.concat(path))
                .orElse(path))
            .orElseGet(() -> ofNullable(getName())
                .map(name -> ofNullable(getProtocol())
                    .map(protocol -> protocol.concat(name))
                    .orElse(name))
                .orElse(""));
    }

    default String getName() {
        return ofNullable(getPath().getFileName())
            .map(Path::toString)
            .orElseGet(() -> getPath().toString());
    }

    default String getUrlPath() {
        String pathText = ofNullable(getPath())
            .map(Path::toString)
            .orElse("");
        return ofNullable(getProtocol())
            .map(protocol -> protocol.concat(pathText))
            .orElse(pathText);
    }

    default String getDescription() {
        return null;
    }

    default String getType() {
        return getExtension(getName());
    }

    default boolean isParent() {
        return false;
    }

    default long getSize() {
        return -1;
    }

    default long lastModified() {
        return 0;
    }

    default boolean isHidden() {
        return false;
    }

    default boolean isLink() {
        return false;
    }

    default String getIconName() {
        return null;
    }

    default boolean canRead() {
        return true;
    }

    default boolean canUpdate() {
        return true;
    }

    default List<TranslatableName> detailNames() {
        return emptyList();
    }

    default Map<TranslatableName, Value> details() {
        return emptyMap();
    }

    default void initDetails() {
    };
}
