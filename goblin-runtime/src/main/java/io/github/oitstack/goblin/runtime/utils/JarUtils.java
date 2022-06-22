package io.github.oitstack.goblin.runtime.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午6:36
 */
public class JarUtils {
    private static Logger log = LoggerFactory.getLogger(JarUtils.class);

    public static void extractFromJarToLocation(final JarFile jarFile,
                                             final JarEntry entry,
                                             final String fromRoot,
                                             final File toRoot) throws IOException {

        if (null == entry || entry.isDirectory()) {
            return;
        }
        String destinationName = entry.getName().replaceFirst(fromRoot, "");
        File newFile = initNewFile(destinationName, toRoot);

        try (InputStream is = jarFile.getInputStream(entry)) {
            Files.copy(is, newFile.toPath());
        } catch (IOException e) {
            log.error("Failed to extract classpath resource " + entry.getName() + " from JAR file " + jarFile.getName(), e);
            throw e;
        }
    }

    private static File initNewFile(String destinationName, File toRoot) {

        File newFile = new File(toRoot, destinationName);


        // Create parent directories
        Path parent = newFile.getAbsoluteFile().toPath().getParent();
        parent.toFile().mkdirs();
        newFile.deleteOnExit();
        return newFile;
    }
}
