package es.ucm.fdi.iw;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.*;

public class Unzipper {
    public static File unzipToTemp(File inputZip) throws IOException {
        Path tmpDir = Files.createTempDirectory("iw-unzip").toAbsolutePath();

        // adapted from https://stackoverflow.com/a/59581898/15472
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(inputZip))) {
            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
                Path resolvedPath = tmpDir.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(tmpDir)) {
                    // see: https://snyk.io/research/zip-slip-vulnerability
                    throw new IOException("Entry with an illegal path: " + ze.getName());
                }
                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zipIn, resolvedPath);
                }
            }
        }

        return tmpDir.toFile();
    }

    public static void cleanupTemp(File tempDir) throws IOException {
        // adapted from https://stackoverflow.com/a/35989142/15472
        try (Stream<Path> walk = Files.walk(tempDir.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .peek(System.out::println)
                .forEach(File::delete);
        }
    }
}
