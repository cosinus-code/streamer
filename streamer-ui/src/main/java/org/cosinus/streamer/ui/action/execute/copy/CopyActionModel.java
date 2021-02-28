/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.swing.action.execute.ActionModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.cosinus.streamer.ui.action.execute.copy.TransferType.TRANSFER_AUTO;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Encapsulates the model of the copy elements action
 */
public class CopyActionModel<S extends Streamer, T extends Streamer> extends ActionModel {

    private static final String COPY_ACTION_NAME = "act-copy";

    private static final String MOVE_ACTION_NAME = "act-move";

    private static final String PACK_ACTION_NAME = "act-pack";

    private StreamerFilter sourceFilter = streamer -> true;

    private List<Streamer> elementsToCopy;

    private S source;

    private T destination;

    private Path sourcePath;

    private Path targetPath;

    private boolean forceNewInsteadOverwrite;

    private TransferType transferType = TRANSFER_AUTO;

    private String packType;

    private boolean overwriteAllExistingTargets;

    private boolean overwriteAllExistingTargetsIfOlder;

    private boolean skipAllExistingTargets;

    private boolean overwriteCurrentTarget;

    private boolean skipCurrentTargets;

    private boolean appendSourceToCurrentTargets;

    private boolean resumeCopy;

    private boolean verifyCopy;

    private boolean pack;

    public CopyActionModel(String actionName) {
        super(UUID.randomUUID().toString(), actionName);
    }

    public static <S extends DirectoryStreamer, T extends DirectoryStreamer>
    CopyActionModel<S, T> copy(List<Streamer> elementsToCopy) {
        return new CopyActionModel<S, T>(COPY_ACTION_NAME)
                .setCopyElements(elementsToCopy);
    }

    public static <S extends DirectoryStreamer, T extends DirectoryStreamer>
    CopyActionModel<S, T> move(List<Streamer> elementsToCopy) {
        return new CopyActionModel<S, T>(MOVE_ACTION_NAME)
                .setCopyElements(elementsToCopy);
    }

    public static <S extends DirectoryStreamer, T extends DirectoryStreamer>
    CopyActionModel<S, T> pack(List<Streamer> elementsToCopy) {
        return new CopyActionModel<S, T>(PACK_ACTION_NAME)
                .setCopyElements(elementsToCopy)
                .pack();
    }

    public Path getTargetPath() {
        return targetPath;
    }

    public CopyActionModel<S, T> toTargetPath(Path targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public CopyActionModel<S, T> toTargetPath(String targetPath) {
        return toTargetPath(Paths.get(targetPath));
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public CopyActionModel<S, T> withTransferType(TransferType transferType) {
        this.transferType = transferType;
        return this;
    }

    public String getPackType() {
        return packType;
    }

    public CopyActionModel<S, T> withPackType(String packType) {
        this.packType = packType;
        return this;
    }

    public S getSource() {
        return source;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public CopyActionModel<S, T> fromSourcePath(Path sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public CopyActionModel<S, T> fromSource(S source) {
        this.source = source;
        return this;
    }

    public StreamerFilter getSourceFilter() {
        return sourceFilter;
    }

    public CopyActionModel<S, T> setSourceFilter(StreamerFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
        return this;
    }

    public CopyActionModel<S, T> setCopyElements(List<Streamer> elementsToCopy) {
        this.elementsToCopy = elementsToCopy;
        this.sourceFilter = this.elementsToCopy::contains;
        return this;
    }

    public CopyActionModel<S, T> from(S source) {
        this.source = source;
        this.sourcePath = source.getPath();
        return this;
    }

    public T getDestination() {
        return destination;
    }

    public CopyActionModel<S, T> toDestination(T destination) {
        this.destination = destination;
        return this;
    }

    public CopyActionModel<S, T> to(T destination) {
        this.destination = destination;
        this.targetPath = destination.getPath();
        return this;
    }

    public boolean isForceNewInsteadOverwrite() {
        return forceNewInsteadOverwrite;
    }

    public CopyActionModel<S, T> forceNewInsteadOverwrite() {
        this.forceNewInsteadOverwrite = true;
        return this;
    }

    public boolean shouldOverwriteAllExistingTargets() {
        return overwriteAllExistingTargets;
    }

    public CopyActionModel<S, T> overwriteAllExistingTargets() {
        this.overwriteAllExistingTargets = true;
        return this;
    }

    public boolean shouldOverwriteAllExistingTargetsIfOlder() {
        return overwriteAllExistingTargetsIfOlder;
    }

    public CopyActionModel<S, T> overwriteAllExistingTargetsIfOlder() {
        this.overwriteAllExistingTargetsIfOlder = true;
        return this;
    }

    public boolean shouldSkipAllExistingTargets() {
        return skipAllExistingTargets;
    }

    public CopyActionModel<S, T> skipAllExistingTargets() {
        this.skipAllExistingTargets = true;
        return this;
    }

    public boolean shouldOverwriteCurrentTarget() {
        return overwriteCurrentTarget;
    }

    public CopyActionModel<S, T> overwriteCurrentTarget() {
        this.overwriteCurrentTarget = true;
        return this;
    }

    public boolean shouldSkipCurrentTargets() {
        return skipCurrentTargets;
    }

    public CopyActionModel<S, T> skipCurrentTarget() {
        this.skipCurrentTargets = true;
        return this;
    }

    public boolean shouldAppendSourceToCurrentTarget() {
        return appendSourceToCurrentTargets;
    }

    public CopyActionModel<S, T> appendSourceToCurrentTarget() {
        this.appendSourceToCurrentTargets = true;
        return this;
    }

    public boolean shouldResumeCopy() {
        return resumeCopy;
    }

    public CopyActionModel<S, T> resumeCopy() {
        this.resumeCopy = true;
        return this;
    }

    public boolean shouldVerifyCopy() {
        return verifyCopy;
    }

    public CopyActionModel<S, T> verifyCopy() {
        this.verifyCopy = true;
        return this;
    }

    public boolean shouldPackElements() {
        return pack;
    }

    public CopyActionModel<S, T> pack() {
        this.pack = true;
        return this;
    }

    public void resetCurrentTarget() {
        this.skipCurrentTargets = false;
        this.overwriteCurrentTarget = false;
        this.appendSourceToCurrentTargets = false;
        this.resumeCopy = false;
    }

    public boolean hasElementsToCopy() {
        return !isEmpty(elementsToCopy);
    }

    public boolean isCopyAllowed() {
        return source.canRead() && destination.canWrite();
    }

    public boolean isSensitiveToTransferType() {
        return source.isSensitiveToTransferType() ||
                destination.isSensitiveToTransferType();
    }

    public boolean shouldSkip(Streamer source, Streamer target) {
        boolean isTargetOlder = target.lastModified() < source.lastModified();
        return shouldSkipCurrentTargets() ||
                shouldSkipAllExistingTargets() ||
                (shouldOverwriteAllExistingTargetsIfOlder() && !isTargetOlder);
    }

    public boolean shouldOverwrite(Streamer source, Streamer target) {
        boolean isTargetOlder = target.lastModified() < source.lastModified();
        return shouldOverwriteCurrentTarget() ||
                shouldOverwriteAllExistingTargets() ||
                (shouldOverwriteAllExistingTargetsIfOlder() && isTargetOlder);
    }

    public CopyActionModel<S, T> withOverwriteOption(CopyOverwriteOption copyOverwriteOption) {
        copyOverwriteOption.apply(this);
        return this;
    }
}
