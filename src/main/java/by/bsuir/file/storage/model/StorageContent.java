package by.bsuir.file.storage.model;

import java.util.Objects;

public class StorageContent {

    private final String type;
    private final String name;

    public StorageContent(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageContent that = (StorageContent) o;
        return Objects.equals(type, that.type) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "ResponseContainer{" +
                "type='" + type + '\'' +
                ", content='" + name + '\'' +
                '}';
    }
}
