package by.bsuir.file.storage.service;

import by.bsuir.file.storage.model.FileInfo;
import by.bsuir.file.storage.model.StorageContent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@Component
public class FileStorageService implements StorageService {
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
        return file.delete();
    }

    private String getComponentType(File file) {
        return file.isDirectory() ? "Folder" : "File";
    }

    private String convertToDate(FileTime fileTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(fileTime.toMillis());
    }
}
