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
package io.github.oitstack.goblin.runtime.utils;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @Author CuttleFish
 * @Date 2022/3/9 上午10:34
 */
public class PathUtils {
    private final static char SLASH = '/';
    private final static char DOUBLE_BACK_SLASH = '\\';
    private final static String DOUBLE_SLASH = "//";
    private final static String WIN_PATH_PATTERN = "\"^[a-zA-Z]:\\\\/.*\"";

    /**
     * Recursively delete a directory and all its subdirectories and files.
     *
     * @param dir path to the directory to delete.
     */
    public static void delAllFromDir(final Path dir) {
        try {
            if (dir == null) {
                return;
            } else if (!Files.exists(dir)) {
                return;
            } else {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });

            }

        } catch (IOException e) {
        }
    }


    /**
     * Create a MinGW compatible path based on usual Windows path
     *
     * @param path a usual windows path
     * @return a MinGW compatible path
     */
    public static String createMinGWPath(String path) {
        String mingwPath = path.replace(DOUBLE_BACK_SLASH, SLASH);
        int driveLetterIndex;
        if (mingwPath.matches(WIN_PATH_PATTERN)) {
            driveLetterIndex = 0;
        } else {
            driveLetterIndex = 1;
        }
        // drive-letter must be lower case
        char driveLetter = Character.toLowerCase(mingwPath.charAt(driveLetterIndex));
        String virtualPath = mingwPath.substring(driveLetterIndex + 1);

        mingwPath = DOUBLE_SLASH + driveLetter + virtualPath;
        mingwPath = mingwPath.replace(":", "");
        return mingwPath;
    }

    public static String formatResourceURIToFilePath(final String resource) {
        try {
            // Convert any url-encoded characters (e.g. spaces) back into unencoded form
            return URLDecoder.decode(resource.replaceAll("\\+", "%2B"), Charsets.UTF_8.name())
                    .replaceFirst("jar:", "")
                    .replaceFirst("file:", "")
                    .replaceAll("!.*", "");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
