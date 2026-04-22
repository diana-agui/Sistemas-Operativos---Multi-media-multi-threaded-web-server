import java.io.File;
import java.time.LocalDateTime;

public class Image {
    int id;
    String fileName;
    String filePath;
    long fileSizeBytes;
    LocalDateTime uploadedAt;

    public Image(int id, String filePath) {
        File file = new File(filePath);

        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + filePath);

        this.id            = id;
        this.filePath      = filePath;
        this.fileName      = file.getName();
        this.fileSizeBytes = file.length();
        this.uploadedAt    = LocalDateTime.now();
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