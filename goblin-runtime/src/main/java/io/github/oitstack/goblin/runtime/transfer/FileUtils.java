package io.github.oitstack.goblin.runtime.transfer;

import io.github.oitstack.goblin.runtime.utils.MixAll;
import io.github.oitstack.goblin.runtime.utils.PathUtils;
import io.github.oitstack.goblin.runtime.utils.PlatformUtils;
import org.apache.commons.compress.archivers.tar.TarConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @Author CuttleFish
 * @Date 2022/6/6 下午6:13
 */
public class FileUtils {
    private static final String OS_MAC_TMP_DIR = "/tmp";
    public static final int DEFAULT_FILE_MODE = FileModeEnums.FILE_644.getMode();
    public static final int DEFAULT_DIR_MODE = FileModeEnums.DIR_755.getMode();//

    public static File createTempDirectory(String tempDir) {
        try {
            if (PlatformUtils.IS_MAC) {
                return Files.createTempDirectory(Paths.get(OS_MAC_TMP_DIR), tempDir).toFile();
            } else {
                return Files.createTempDirectory(tempDir).toFile();
            }
        } catch (IOException e) {
            return new File(tempDir + MixAll.randomString(5));
        }
    }


    public static void delete(File file) {
        try {
            file.delete();
        } catch (Exception e) {

        }
    }

    public static void deletePathOnJVMExit(final Path path) {
        Runtime.getRuntime().addShutdownHook(MixAll.NAMED_THREAD_FACTORY.newThread(() -> PathUtils.delAllFromDir(path)));

    }

    public static int getUnixFileMode(final Path path) {
        try {
            Map<String, Object> fileAttributes = Files.readAttributes(path, "unix:mode");
            // Truncate mode bits for z/OS
            return dealWithIBMMachine(path, (int) fileAttributes.get("mode"));
        } catch (IOException | UnsupportedOperationException e) {
            // fallback for non-posix environments
            int mode = DEFAULT_FILE_MODE;
            if (Files.isDirectory(path)) {
                mode = DEFAULT_DIR_MODE;
            } else {
                mode = Files.isExecutable(path) ? (DEFAULT_FILE_MODE | 0b1001001) : DEFAULT_FILE_MODE;
            }
            return mode;
        }
    }

    private static int dealWithIBMMachine(Path path, int unixMode) {
        if (PlatformUtils.IS_IBM_MACHINE) {
            unixMode &= TarConstants.MAXID;
            unixMode |= Files.isDirectory(path) ? 0b100000000000000 : 0b1000000000000000;
        }
        return unixMode;
    }


}
