package io.github.oitstack.goblin.runtime.utils;

import io.github.oitstack.goblin.runtime.transfer.FileUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午6:46
 */
public class TarUtils {
    private static Logger log = LoggerFactory.getLogger(TarUtils.class);

    static class ResolvedFile {
        final File sourceFile;
        final File sourceRootPath;
        final String relativePathToSourceFile;

        private ResolvedFile(String filePath, String rootPath) throws IOException {
            final File sourceFile = new File(filePath).getCanonicalFile();     // e.g. /foo/bar/baz
            final File sourceRootPath = new File(rootPath).getCanonicalFile();     // e.g. /foo
            final String relativePathToSourceFile = sourceRootPath.toPath().relativize(sourceFile.toPath()).toFile().toString();
            this.sourceFile = sourceFile;
            this.sourceRootPath = sourceRootPath;
            this.relativePathToSourceFile = relativePathToSourceFile;// e.g. /bar/baz

        }

        public static ResolvedFile of(String filePath, String rootPath) throws IOException {
            return new ResolvedFile(filePath, rootPath);
        }

        public File getSourceFile() {
            return sourceFile;
        }

        public File getSourceRootPath() {
            return sourceRootPath;
        }

        public String getRelativePathToSourceFile() {
            return relativePathToSourceFile;
        }
    }

    /*
     * Recursively copies a file/directory into a TarArchiveOutputStream
     */
    public static void recursiveCopyTar(String entryFilename, String rootPath, String itemPath, TarArchiveOutputStream tarArchive) {
        try {
            ResolvedFile resoveledFile = ResolvedFile.of(itemPath, rootPath);

            copyFileToArchive(tarArchive, resoveledFile, entryFilename);

            final File[] children = resoveledFile.getSourceFile().listFiles();
            if (children != null) {
                // recurse into child files/directories
                for (final File child : children) {
                    TarUtils.recursiveCopyTar(entryFilename, resoveledFile.getSourceRootPath().getCanonicalPath(), child.getCanonicalPath(), tarArchive);
                }
            }
        } catch (IOException e) {
            log.error("Error when copying TAR file entry: {}", itemPath, e);
            throw new UncheckedIOException(e); // fail fast
        }
    }

    private static void copyFileToArchive(TarArchiveOutputStream tarArchive, ResolvedFile resoveledFile, String entryFilename) throws IOException {
        try {
            final TarArchiveEntry tarEntry = assemblyTarEntry(resoveledFile, entryFilename);

            tarArchive.putArchiveEntry(tarEntry);

            if (resoveledFile.getSourceFile().isFile()) {
                Files.copy(resoveledFile.getSourceFile().toPath(), tarArchive);
            }
        } finally {
            // a directory entry merely needs to exist in the TAR file - there is no data stored yet
            tarArchive.closeArchiveEntry();
        }


    }

    private static TarArchiveEntry assemblyTarEntry(ResolvedFile resoveledFile, String entryFilename) throws IOException {
        final String tarEntryFilename = formatEntryFileName(entryFilename, resoveledFile);
        final TarArchiveEntry tarEntry = new TarArchiveEntry(resoveledFile.getSourceFile(), tarEntryFilename);
        // TarArchiveEntry automatically sets the mode for file/directory, but we can update to ensure that the mode is set exactly (inc executable bits)
        tarEntry.setMode(FileUtils.getUnixFileMode(Paths.get(resoveledFile.getSourceFile().getCanonicalPath())));
        return tarEntry;
    }

    private static String formatEntryFileName(String entryFilename, ResolvedFile resoveledFile) {
        final String tarEntryFilename;
        if (resoveledFile.getRelativePathToSourceFile().isEmpty()) {
            tarEntryFilename = entryFilename; // entry filename e.g. xyz => xyz
        } else {
            tarEntryFilename = entryFilename + "/" + resoveledFile.getRelativePathToSourceFile(); // entry filename e.g. /xyz/bar/baz => /foo/bar/baz
        }
        return tarEntryFilename.replaceAll("^/", "");
    }
}
