package myapp.file;

public record UploadedFile(
        String name,            // HTML <input>의 name 속성
        String fileName,        // 파일 원본명
        String contentType,     // MIME 타입
        byte[] data             // 실제 바이너리 데이터
) {
}
