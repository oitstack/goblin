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

import io.github.oitstack.goblin.runtime.utils.PathUtils;
import io.github.oitstack.goblin.runtime.utils.PlatformUtils;
import io.github.oitstack.goblin.runtime.utils.TarUtils;
import io.github.oitstack.goblin.runtime.transfer.parser.ResourceParserChain;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author CuttleFish
 * @Date 2022/3/9 上午10:18
 */
@Slf4j
public class MountableFile implements Transferable {


    public static final String SLASH = "/";

    private final String path;
    private final Integer forcedFileMode;
    @Getter(lazy = true)
    private final String filesystemPath = resolveLocalFilesystemPath();

    @Getter(lazy = true)
    private final String resolvedPath = resolveRuntimePath();


    private MountableFile(String path, Integer forcedFileMode) {
        this.path = path;
        this.forcedFileMode = forcedFileMode;
    }

    /**
     * generate a {@link MountableFile} from docker host filesystem
     */
    public static MountableFile generateFromHostPath(final String path) {
        return generateFromHostPath(path, null);
    }


    /**
     * Obtains a {@link MountableFile} corresponding to a file on the docker host filesystem.
     */
    public static MountableFile generateFromHostPath(final String path, Integer mode) {
        return generateFromHostPath(Paths.get(path), mode);
    }

    /**
     * Obtains a {@link MountableFile} corresponding to a file on the docker host filesystem.
     */
    public static MountableFile generateFromHostPath(final Path path, Integer mode) {
        return new MountableFile(path.toAbsolutePath().toString(), mode);
    }


    /**
     * Obtain a path that the Docker daemon should be able to use to volume mount a file/resource
     * into a container. If this is a classpath resource residing in a JAR, it will be extracted to
     * a temporary location so that the Docker daemon is able to access it.
     */
    private String resolveRuntimePath() {
        String resourcePath = ResourceParserChain.getInstance().start(this.path);

        // Special case for Windows
        if (PlatformUtils.IS_WINDOWS && resourcePath.startsWith(SLASH)) {
            // Remove leading /
            resourcePath = resourcePath.substring(1);
        }

        return resourcePath;
    }

    /**
     * Obtain a path in local filesystem that the Docker daemon should be able to use to volume mount a file/resource
     * into a container. If this is a classpath resource residing in a JAR, it will be extracted to
     * a temporary location so that the Docker daemon is able to access it.
     * <p>
     *
     */
    private String resolveLocalFilesystemPath() {
        String resourcePath = ResourceParserChain.getInstance().start(this.path);

        if (PlatformUtils.IS_WINDOWS && resourcePath.startsWith(SLASH)) {
            resourcePath = PathUtils.createMinGWPath(resourcePath).substring(1);
        }

        return resourcePath;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void transferTo(final TarArchiveOutputStream outputStream, String destinationPathInTar) {
        TarUtils.recursiveCopyTar(destinationPathInTar, this.getResolvedPath(), this.getResolvedPath(), outputStream);
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }


    @Override
    public long getSize() {

        final File file = new File(this.getResolvedPath());
        if (file.isFile()) {
            return file.length();
        } else {
            return 0;
        }
    }

    @Override
    public String getDescription() {
        return this.getResolvedPath();
    }

    @Override
    public int getFileMode() {
        return getUnixFileMode(this.getResolvedPath());
    }

    private int getUnixFileMode(final String pathAsString) {
        final Path path = Paths.get(pathAsString);
        if (this.forcedFileMode != null) {
            return this.getModeValue(path);
        }
        return FileUtils.getUnixFileMode(path);
    }


    private int getModeValue(final Path path) {
        int result = Files.isDirectory(path) ? FileModeEnums.DIR_000.getMode() : FileModeEnums.FILE_000.getMode();
        return result | this.forcedFileMode;
    }
}
