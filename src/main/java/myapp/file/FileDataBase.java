package myapp.file;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FileDataBase {
    private static final Map<Long, FileInfo> files = new ConcurrentHashMap<>();
    private static final AtomicLong File_ID_SEQUENCE = new AtomicLong(1);

    public static Long nextFileId() {
        return File_ID_SEQUENCE.getAndIncrement();
    }

    public static FileInfo save(String originalName, String storedName, String contentType, long size, String path) {
        Long id = nextFileId();
        FileInfo file = new FileInfo(id, originalName, storedName, contentType, size, path);

        files.putIfAbsent(id, file);

        return file;
    }

    public static FileInfo findById(Long fileId) {
        return files.get(fileId);
    }


    public static FileInfo findByPath(String path) {
        for (FileInfo file : files.values()) {
            if (file.path().equals(path)) {
                return file;
            }
        }
        return null;
    }

}
