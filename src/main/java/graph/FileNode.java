package graph;

import java.io.File;

/**
 * Created by conor on 07/09/2014.
 *
 * Model representing a file, with certain choice information stored, avoiding storing too much information
 * or repeatedly accessing the file system when performing analysis. This approach also allows for caching and other
 * ways of speeding this tool up upon reuse.
 */
public class FileNode {

    private String path;
    private final String id;
    private final boolean isDirectory;
    private String fileType;
    private long fileSize;

    /**
     * Constructor which extracts useful information from a given file.
     * @param file the file which this node will represent
     */
    public FileNode(File file) {
        this.path = file.getPath();
        this.id = file.getPath();
        this.isDirectory = file.isDirectory();
        this.fileType = isDirectory ? "directory" : file.getName().substring(file.getName().lastIndexOf('.') + 1);
        this.fileSize = file.getTotalSpace();
    }

    /**
     * get the ID of this node / file
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Whether this node represents a directory, if not it represents a file.
     * @return whether the node file is a directory
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Set the path this node represents.
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the file type of this node's file.
     * @param fileType
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * get this file node's type.
     * @return the file type
     */
    public String getFileType() {
        return this.fileType;
    }

    /**
     * set the size of this file (in bytes).
     * @param fileSize the size to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * get the size of this node's file (in bytes).
     * @return the file size in bytes
     */
    public long getFileSize() {
        return this.fileSize;
    }

    @Override
    public String toString() {
        String out = "Id: %s, Path: %s, isDir: %s, fileType: %s, fileSize: %d. ";
        return String.format(out, id, path, isDirectory, fileType, fileSize);
    }
}
