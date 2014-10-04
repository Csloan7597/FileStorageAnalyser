package graph;

import java.io.File;

/**
 * Created by conor on 07/09/2014.
 */
public class FileNode {

    private String path;
    private String id;
    private boolean isDirectory;
    private String fileType;
    private long fileSize;

    public FileNode(File file) {
        this.path = file.getPath();
        this.id = file.getPath();
        this.isDirectory = file.isDirectory();
        this.fileType = isDirectory ? "directory" : file.getName().substring(file.getName().lastIndexOf('.') + 1);
        this.fileSize = file.getTotalSpace();
    }

    public String getId() {
        return id;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() { return this.fileType; }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        String out = "Id: %s, Path: %s, isDir: %s, fileType: %s, fileSize: %d. ";
        return String.format(out, id, path, isDirectory, fileType, fileSize);
    }
}
