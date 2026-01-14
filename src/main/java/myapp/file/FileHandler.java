package myapp.file;


import myapp.handler.HandlerMapping;
import myapp.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    /*
     * c
     * */

    @HandlerMapping(method = HttpMethod.POST, path = "/files")
    public void upload(HttpRequest request, HttpResponse response) {
        logger.debug("[upload 호출!]");

        /*
         * 1. request
         */
        UploadedFile file = BodyParser.getMultipart(request);

        if (file == null) {
            response.badRequest();
            response.setBody("file not found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        /*
         * 2. 비즈니스 로직
         */
        try {
            // 1. 디스크 저장
            Path uploadDir = Paths.get("upload");
            Files.createDirectories(uploadDir);

            String storedName = UUID.randomUUID() + "_" + file.fileName();
            Path storedPath = uploadDir.resolve(storedName);

            Files.write(storedPath, file.data());


            // 2. 메모리 DB 저장
            String path = "/upload/" + storedName;

            FileInfo savedFile = FileDatabase.save(
                    file.fileName(),          // originalName
                    storedName,               // storedName
                    file.contentType(),       // contentType
                    file.data().length,       // size
                    path                      // path
            );


            /*
             * 3. response
             */

            String json = """
                    { "path": "%s" }
                    """.formatted(savedFile.path());


            response.ok();
            response.addHeader(HttpHeader.CONTENT_TYPE.value(), "application/json");
            response.setBody(json.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            response.internalServerError();
            response.setBody("upload failed".getBytes(StandardCharsets.UTF_8));
        }
    }


    /*
     * r
     * */

    @HandlerMapping(method = HttpMethod.GET, path = "/files/{fileName}")
    public void getFile(HttpRequest request, HttpResponse response) {

        /*
         * 1. request
         */

        String encodedFileName = request.getPathVariable("fileName");

        if (encodedFileName == null || encodedFileName.isBlank()) {
            response.badRequest();
            return;
        }

        // URL 디코딩
        String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);

        // 디렉토리 트래버설 방지
        if (fileName.contains("..")) {
            response.badRequest();
            return;
        }

        /*
         * 2. 비즈니스 로직
         */

        Path filePath = Paths.get("upload", fileName);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            response.notFound();
            return;
        }

        byte[] fileData;
        String contentType;

        try {
            fileData = Files.readAllBytes(filePath);
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            response.internalServerError();
            return;
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        /*
         * 3. response
         */

        response.ok();
        response.addHeader(HttpHeader.CONTENT_TYPE.value(), contentType);
        response.setBody(fileData);
    }


}
