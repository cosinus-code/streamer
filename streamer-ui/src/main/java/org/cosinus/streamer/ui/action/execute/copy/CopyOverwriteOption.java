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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Copy options when the target already exists
 */
public enum CopyOverwriteOption {

    OVERWRITE("act_copy_overwrite", CopyActionModel::overwriteCurrentTarget),
    OVERWRITE_ALL("act_copy_overwrite_all", CopyActionModel::overwriteAllExistingTargets),
    SKIP("act_copy_skip", CopyActionModel::skipCurrentTarget),
    CANCEL("act_copy_cancel", null),
    OVERWRITE_OLDER("act_copy_overwrite_older", CopyActionModel::overwriteAllExistingTargetsIfOlder),
    SKIP_ALL("act_copy_skip_all", CopyActionModel::skipAllExistingTargets),
    RENAME("act_copy_rename", null),
    APPEND("act_copy_append", CopyActionModel::appendSourceToCurrentTarget),
    RESUME("act_copy_resume", CopyActionModel::resumeCopy);

    private final String key;

    private final Consumer<CopyActionModel> consumer;

    CopyOverwriteOption(String key, Consumer<CopyActionModel> consumer) {
        this.key = key;
        this.consumer = consumer;
    }

    public void apply(CopyActionModel copyAction) {
        Optional.ofNullable(consumer)
                .ifPresent(c -> c.accept(copyAction));
    }

    @Override
    public String toString() {
        return key;
    }
}
