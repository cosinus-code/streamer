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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.consumer.BinaryStreamSaver;
import org.cosinus.streamer.api.error.ConsumedStreamNotMatchException;
import org.cosinus.streamer.api.stream.binary.BinaryStream;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.progress.CopyProgressModel;
import org.cosinus.streamer.ui.error.ActionCancelledException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.streamer.ui.error.SkipActionException;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconSize;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static javax.swing.JOptionPane.*;
import static org.cosinus.swing.dialog.OptionsDialog.DEFAULT_OPTION;
import static org.cosinus.swing.dialog.OptionsDialog.INFORMATION_MESSAGE;
import static org.cosinus.swing.util.Formatter.formatDate;
import static org.cosinus.swing.util.Formatter.formatMemorySize;

/**
 * {@link SwingProgressWorker} for copying streamers from a source system to target system
 */
public class CopyWorker<S extends DirectoryStreamer, T extends DirectoryStreamer>
    extends SwingProgressWorker<CopyProgressModel> {

    private static final Logger LOG = LogManager.getLogger(CopyWorker.class);

    @Autowired
    private Translator translator;

    @Autowired
    private IconHandler iconHandler;

    private final CopyActionModel<S, T> copyModel;

    private final S source;

    private final T destination;

    public CopyWorker(CopyActionModel<S, T> copyModel,
                      Window parentWindow) {
        super(parentWindow,
              copyModel.getActionId(),
              new CopyProgressModel(copyModel.getActionId()));
        this.copyModel = copyModel;
        this.source = copyModel.getSource();
        this.destination = copyModel.getDestination();
    }

    @Override
    protected void doWork() {
        long totalSize = source.getTotalSize(copyModel.getSourceFilter());
        long freeSpace = destination.getFreeSpace();
        if (totalSize > freeSpace) {
            if (!dialogHandler.confirm("act_copy_no_free_space")) {
                return;
            }
        }

        progress.startTotalProgress(totalSize);
        setProgress(progress);

        try (Stream<? extends Streamer> streamers = source.flatStream(copyModel.getSourceFilter())) {
            streamers
                .forEach(streamerToCopy -> {
                    checkActionStatus();
                    Path relativePath = getRelativePath(source, streamerToCopy);
                    Path targetPath = destination.getPath().resolve(relativePath);
                    if (streamerToCopy.isDirectory()) {
                        DirectoryStreamer target = destination.createDirectoryStreamer(targetPath);
                        if (!target.exists()) {
                            target.save();
                        }
                        return;
                    }

                    BinaryStreamer target = destination.createBinaryStreamer(targetPath);
                    progress.startStreamerProgress(streamerToCopy, target);
                    setProgress(progress);
                    if (target.exists() && copyModel.shouldSkip(source, target)) {
                        LOG.trace("Action skipped: copy " + source.getPath() + " to " + target.getPath());
                        progress.updateStreamerProgress(source.getSize());
                        progress.finishStreamerProgress();
                        setProgress(progress);
                        return;
                    }

                    BinaryStreamer binaryStreamer = (BinaryStreamer) streamerToCopy;
                    saveBinaryStreamer(binaryStreamer, prepareTarget(binaryStreamer, target));
                });
        }
    }

    protected Path getRelativePath(DirectoryStreamer source, Streamer streamer) {
        Path sourcePath = copyModel.getSourcePath();
        Path streamerPath = streamer.getPath();

        return streamerPath.subpath(ofNullable(sourcePath)
                                        .filter(streamerPath::startsWith)
                                        .map(Path::getNameCount)
                                        .orElse(0),
                                    streamerPath.getNameCount());
    }

    protected BinaryStreamer prepareTarget(BinaryStreamer source, BinaryStreamer target) {
        copyModel.resetCurrentTarget();
        return target.exists() ?
            copyModel.isForceNewInsteadOverwrite() ?
                applyAutogeneratedName(target) :
                applyOverwriteOption(source, target) :
            target;
    }

    protected void saveBinaryStreamer(BinaryStreamer source, BinaryStreamer target) {
        boolean append = copyModel.shouldAppendSourceToCurrentTarget() || copyModel.shouldResumeCopy();
        try (BinaryStream binaryStream = source.stream();
             BinaryStreamSaver binarySaver = target.saver(append)) {

            if (copyModel.shouldResumeCopy() && !resume(binaryStream, source, target)) {
                return;
            }

            binarySaver.consume(binaryStream, bytes -> {
                checkActionStatus();
                progress.updateStreamerProgress(bytes.length);
                setProgress(progress);
            });

            if (binaryStream.checksum() != binarySaver.checksum()) {
                throw new ConsumedStreamNotMatchException("Consumed stream verification failed");
            }
        } catch (SkipActionException ex) {
            LOG.trace("Action skipped: copy " + source.getPath() + " to " + target.getPath());
        } catch (ConsumedStreamNotMatchException ex) {
            if (!dialogHandler.confirm(parentWindow,
                                       translator.translate("act_copy_check_error",
                                                            source.getPath(),
                                                            target.getPath()))) {
                throw new ActionCancelledException();
            }
        } catch (IOException | UncheckedIOException copyException) {
            throw new ActionException(copyException,
                                      "act_copy_error",
                                      source.getPath(),
                                      target.getPath());
        } finally {
            progress.finishStreamerProgress();
            setProgress(progress);
        }
    }

    protected boolean resume(BinaryStream binaryStream, Streamer source, Streamer target) throws IOException {
        long skippedBytes = binaryStream.skipBytes(target.getSize());
        if (skippedBytes != target.getSize()) {
            int option = dialogHandler.showConfirmationDialog(parentWindow,
                                                              translator.translate("act_copy_resume_not_match",
                                                                                   target.getPath(),
                                                                                   source.getPath()),
                                                              translator.translate("act_copy_resume_confirmation"),
                                                              YES_NO_CANCEL_OPTION);
            if (option == CANCEL_OPTION) {
                throw new ActionCancelledException();
            }

            if (option != YES_OPTION) {
                return false;
            }
        }

        progress.updateStreamerProgress(skippedBytes);
        return true;
    }

    private BinaryStreamer applyAutogeneratedName(BinaryStreamer target) {
        //TODO:
        return null;
//        return IntStream.range(1, Integer.MAX_VALUE)
//                .mapToObj(Integer::toString)
//                .map(i -> target.getName() + "(" + i + ")")
//                .map(name -> target.getParent().create(name,
//                                                       false))
//                .filter(streamer -> !streamer.exists())
//                .findFirst()
//                .orElse(target);
    }

    private BinaryStreamer applyOverwriteOption(BinaryStreamer source, BinaryStreamer target) {

        if (copyModel.shouldSkipAllExistingTargets() ||
            copyModel.shouldOverwriteAllExistingTargets() ||
            copyModel.shouldOverwriteAllExistingTargetsIfOlder()) {
            return target;
        }

        CopyOverwriteOption overwriteOption = dialogHandler.showCustomOptionDialog(
            parentWindow,
            translator.translate("act_copy_file_exists"),
            getOverwriteMessage(source, target),
            DEFAULT_OPTION,
            INFORMATION_MESSAGE,
            iconHandler.findIconByFile(source.getPath().toFile(), IconSize.X32).orElse(null),
            3,
            450,
            CopyOverwriteOption.values());

        if (overwriteOption == null || overwriteOption == CopyOverwriteOption.CANCEL) {
            throw new ActionCancelledException();
        }

        if (overwriteOption == CopyOverwriteOption.RENAME) {
            //TODO:
//            BinaryStreamer newTarget = dialogHandler.showInputDialog(parentWindow,
//                                                                     translator.translate("act_copy_new_name"))
//                    .map(newName -> target.getParent().create(newName,
//                                                              false))
//                    .orElseThrow(ActionCancelledException::new);
//            return newTarget.exists() ?
//                    applyOverwriteOption(source, newTarget) :
//                    newTarget;
        }

        copyModel.withOverwriteOption(overwriteOption);

        return target;
    }

    protected String getOverwriteMessage(Streamer source, Streamer target) {
        return format("%s\n\n%s\n%s\n\n",
                      translator.translate("act_copy_already_exists",
                                           target.getPath()),
                      translator.translate("act_copy_source_mofified",
                                           formatDate(source.lastModified()),
                                           formatMemorySize(source.getSize())),
                      translator.translate("act_copy_target_modified",
                                           formatDate(target.lastModified()),
                                           formatMemorySize(target.getSize())));
    }
}
