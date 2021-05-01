package by.bsuir.file.storage.service;

import by.bsuir.file.storage.model.FileInfo;
import by.bsuir.file.storage.model.StorageContent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public interface StorageService {

    ArrayList<StorageContent> extractStorageContents(File file);

    boolean delete(File file);

    FileInfo readFileInfo(Path filePath) throws IOException;
}
