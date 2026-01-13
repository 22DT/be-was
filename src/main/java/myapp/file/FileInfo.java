package myapp.file;

public record FileInfo(
        Long fileId,
        String originalName,    // 사용자가 올린 이름
        String storedName,      // UUID 붙은 실제 파일명
        String contentType,
        long size,
        String path             // /upload/uuid_xxx.png
) {
}
