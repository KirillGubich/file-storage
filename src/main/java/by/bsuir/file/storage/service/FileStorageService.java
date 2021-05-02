package by.bsuir.file.storage.service;

import by.bsuir.file.storage.model.FileInfo;
import by.bsuir.file.storage.model.StorageContent;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

@Component
public class FileStorageService implements StorageService {

    private static final String STORAGE_PATH = "src/main/resources";

    @Override
    public boolean copy(Path sourcePath, Path destPath) {
        if (sourcePath == null || destPath == null) {
            return false;
        }
        final FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(destPath.toFile());
            Files.copy(sourcePath, outputStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean save(MultipartFile fileToSave, Path filePath) {
        if (fileToSave == null) {
            return false;
        }
        final File file = filePath.toFile();
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] bytes = fileToSave.getBytes();
            stream.write(bytes);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getStoragePath() {
        return STORAGE_PATH;
    }

    @Override
    public ArrayList<StorageContent> extractStorageContents(File file) {
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

    @Override
    public FileInfo readFileInfo(Path filePath) throws IOException {
        BasicFileAttributes fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
        final File file = filePath.toFile();
        final String path = file.getPath();
        final long size = fileAttributes.size();
        return new FileInfo(file.getName(), path, size);
    }

    @Override
    public boolean delete(File file) {
        if (file == null) {
            return false;
        }
        if (!file.isDirectory()) {
            return file.delete();
        }
        try {
            FileUtils.deleteDirectory(file);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getComponentType(File file) {
        return file.isDirectory() ? "Folder" : "File";
    }
}
