package by.bsuir.file.storage.app;

import by.bsuir.file.storage.model.StorageContent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
public class StorageController {

    private static final String STORAGE_PATH = "src/main/resources";

    @GetMapping("/storage/**")
    private ResponseEntity<List<StorageContent>> getFile(HttpServletRequest request, HttpServletResponse response) {
        String filePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        final Path path = Paths.get(STORAGE_PATH, filePath);
        File file = path.toFile();
        if (file.isDirectory()) {
            return new ResponseEntity<>(extractStorageContents(file), HttpStatus.OK);
        }
        if (file.exists()) {
            sendFile(response, filePath, path);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private ArrayList<StorageContent> extractStorageContents(File file) {
        final File[] files = file.listFiles();
        final ArrayList<StorageContent> contents = new ArrayList<>();
        if (files == null) {
            return contents;
        }
        for (File component : files) {
            contents.add(new StorageContent(getComponentType(component), component.getName()));
        }
        return contents;
    }

    private void sendFile(HttpServletResponse response, String filePath, Path path) {
        final String fileName = extractFileName(filePath);
        response.setContentType("application/storage");
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        try {
            Files.copy(path, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String extractFileName(String filePath) {
        final String[] splitFilePath = filePath.split("/");
        final int length = splitFilePath.length;
        return splitFilePath[length - 1];
    }

    private String getComponentType(File file) {
        return file.isDirectory() ? "Folder" : "File";
    }
}
