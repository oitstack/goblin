/*
 * Copyright 2022 OPPO Goblin Project
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
package io.github.oitstack.goblin.runtime.transfer;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 * @Author CuttleFish
 * @Date 2022/3/9 上午10:16
 */
public interface Transferable {

    int DEFAULT_FILE_MODE = FileModeEnums.FILE_644.getMode();
    int DEFAULT_DIR_MODE = FileModeEnums.DIR_755.getMode();


    /**
     * Get file mode. Default is 0100644.
     *
     * @return file mode
     * @see Transferable#DEFAULT_FILE_MODE
     */
    default int getFileMode() {
        return DEFAULT_FILE_MODE;
    }

    /**
     * Size of an object.
     *
     * @return size in bytes
     */
    long getSize();

    /**
     * transfer content of this Transferable to the output stream. <b>Must not</b> close the stream.
     *
     * @param tarArchiveOutputStream stream to output
     * @param destination
     */
    default void transferTo(TarArchiveOutputStream tarArchiveOutputStream, final String destination) {
    }

    byte[] getBytes();

    default String getDescription() {
        return "";
    }
}
