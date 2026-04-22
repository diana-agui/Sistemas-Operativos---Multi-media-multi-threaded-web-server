import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class Image {
    int id;
    String fileName;
    String filePath;
    long fileSizeBytes;
    LocalDateTime uploadedAt;
    byte[] imageData;       // actual image bytes

    public Image(int id, String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + filePath);

        this.id            = id;
        this.filePath      = filePath;
        this.fileName      = file.getName();
        this.fileSizeBytes = file.length();
        this.uploadedAt    = LocalDateTime.now();
        this.imageData     = Files.readAllBytes(file.toPath()); // reads the image into memory
    }

    // Save image to a new location on disk
    public void saveTo(String destinationPath) throws IOException {
        Files.write(new File(destinationPath).toPath(), imageData);
    }

    public boolean exists() {
        return new File(filePath).exists();
    }

    @Override
    public String toString() {
        return String.format("[ID:%-3d] %-20s | %6.1f KB | %s",
            id, fileName, fileSizeBytes / 1024.0, uploadedAt);
    }
}
