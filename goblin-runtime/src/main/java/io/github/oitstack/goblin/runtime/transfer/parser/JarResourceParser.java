package io.github.oitstack.goblin.runtime.transfer.parser;

import io.github.oitstack.goblin.runtime.transfer.FileUtils;
import io.github.oitstack.goblin.runtime.utils.JarUtils;
import io.github.oitstack.goblin.runtime.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午8:01
 */
@Slf4j
public class JarResourceParser implements ResourceParser {
    public static final String JAR_PREFIX = ".jar!";
    private static final String GOBLIN_TMP_DIR_PREFIX = ".goblin-tmp-";
    public static final String INTERNAL_PATTERN = "[^!]*!/";

    @Override
    public String getResource(String resourcePath) {

        return extractClassPathResourceToTempLocation(resourcePath);
    }

    @Override
    public boolean support(String resourcePath) {
        return resourcePath.contains(JAR_PREFIX);
    }

    private File createACleanTmpDir() {
        File tmpLocation = FileUtils.createTempDirectory(GOBLIN_TMP_DIR_PREFIX);
        FileUtils.delete(tmpLocation);
        return tmpLocation;
    }

    private String formatInternalPath(String hostPath) {
        return hostPath.replaceAll(INTERNAL_PATTERN, "");
    }

    private String extractClassPathResourceToTempLocation(final String resourcePath) {
        File tmpLocation = createACleanTmpDir();

        String internalPath = formatInternalPath(resourcePath);
        String formatedResourcePath = PathUtils.formatResourceURIToFilePath(resourcePath);
        try (JarFile jarFile = new JarFile(formatedResourcePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(internalPath)) {
                    log.debug("Copying classpath resource(s) from {} to {} to the tmp local dir",
                            resourcePath,
                            tmpLocation);
                    JarUtils.extractFromJarToLocation(jarFile, entry, internalPath, tmpLocation);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to process JAR file when extracting classpath resource: " + resourcePath, e);
        }

        try {
            return tmpLocation.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileUtils.deletePathOnJVMExit(tmpLocation.toPath());
        }
    }
}
