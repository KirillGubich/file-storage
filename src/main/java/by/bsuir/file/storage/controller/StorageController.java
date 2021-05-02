package by.bsuir.file.storage.controller;

import by.bsuir.file.storage.model.FileInfo;
import by.bsuir.file.storage.model.Headers;
import by.bsuir.file.storage.model.StorageContent;
import by.bsuir.file.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController()
public class StorageController {

    private final StorageService storageService;
    private static final String BYTES_SIZE_LABEL = " Bytes";
    private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
    private static final String STORAGE_CONTENT_TYPE = "application/storage";
    private static final String CONTENT_HEADER_VALUE = "attachment; filename=";

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/storage/**")
    public ResponseEntity<List<StorageContent>> fetchFile(HttpServletRequest request, HttpServletResponse response) {
        final File file = extractFile(request);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        if (file.isDirectory()) {
            final ArrayList<StorageContent> contents = storageService.extractStorageContents(file);
            return ResponseEntity.ok().body(contents);
        }
        try {
            sendFile(response, file.toPath());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/storage/**")
    public ResponseEntity<Object> deleteFile(HttpServletRequest request) {
        final File file = extractFile(request);
        if (!file.exists()) {
            return ResponseEntity.badRequest().build();
        }
        boolean deletedSuccessfully = storageService.delete(file);
        return deletedSuccessfully ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
    }


    @RequestMapping(value = "/storage/**", method = {RequestMethod.HEAD})
    public ResponseEntity<FileInfo> fetchFileInfo(HttpServletRequest request) {
        final File file = extractFile(request);
        if (!file.exists() || file.isDirectory()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            final FileInfo fileInfo = storageService.readFileInfo(file.toPath());
            HttpHeaders headers = fillHeaders(fileInfo);
            return ResponseEntity.ok().headers(headers).build();
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/storage/**")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("fileName") String fileName,
                                             HttpServletRequest request) {
        final File extractedFile = extractFile(request);
        final Path destPath = Paths.get(extractedFile.getAbsolutePath(), fileName);
        final String copyHeader = request.getHeader(Headers.COPY);
        if (copyHeader != null) {
            return copyFile(destPath, copyHeader);
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        boolean successfullySaved = storageService.save(file, destPath);
        return successfullySaved ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
    }

    private ResponseEntity<String> copyFile(Path destPath, String copyHeader) {
        Path srcPath = Paths.get(storageService.getStoragePath(), copyHeader);
        final boolean copiedSuccessfully = storageService.copy(srcPath, destPath);
        return copiedSuccessfully ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
    }

    private void sendFile(HttpServletResponse response, Path path) throws IOException {
        final String fileName = path.getFileName().toString();
        response.setContentType(STORAGE_CONTENT_TYPE);
        response.addHeader(CONTENT_DISPOSITION_HEADER_NAME, CONTENT_HEADER_VALUE + fileName);
        Files.copy(path, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private File extractFile(HttpServletRequest request) {
        String filePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        final Path path = Paths.get(storageService.getStoragePath(), filePath);
        return path.toFile();
    }

    private HttpHeaders fillHeaders(FileInfo fileInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(Headers.NAME, fileInfo.getName());
        headers.add(Headers.PATH, fileInfo.getPath());
        headers.add(Headers.SIZE, fileInfo.getSize() + BYTES_SIZE_LABEL);
        return headers;
    }
}
