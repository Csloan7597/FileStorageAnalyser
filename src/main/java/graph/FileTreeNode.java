package graph;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by conor on 07/09/2014.
 *
 * Model representing a file, with certain choice information stored, avoiding storing too much information
 * or repeatedly accessing the file system when performing analysis. This approach also allows for caching and other
 * ways of speeding this tool up upon reuse.
 */
public class FileTreeNode {

    private final int depth;

    private String path;
    private final String id;
    private final boolean isDirectory;
    private String fileType;
    private long fileSize;

    private final FileTreeNode parent;
    private CopyOnWriteArrayList<FileTreeNode> children = new CopyOnWriteArrayList<>();

    /**
     * Constructor which extracts useful information from a given file.
     * @param file the file which this node will represent
     */
    public FileTreeNode(File file, FileTreeNode parent) {
        this.parent = parent;
        this.depth = (parent == null) ? 1 : parent.getDepth() + 1;
        this.path = file.getPath();
        this.id = file.getPath();
        this.isDirectory = file.isDirectory();
        this.fileType = isDirectory ? "directory" : file.getName().substring(file.getName().lastIndexOf('.') + 1);
        this.fileSize = file.getTotalSpace();
    }

    /**
     * The depth of this node in the tree structure
     * @return depth
     */
    public int getDepth() {
        return this.depth;
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
     * Get the path this node represents
     * @return
     */
    public String getPath() { return this.path; }

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

    public FileTreeNode getParent() {
        return parent;
    }

    public CopyOnWriteArrayList<FileTreeNode> getChildren() {
        return (CopyOnWriteArrayList<FileTreeNode>) this.children.clone();
    }

    public void addChild(FileTreeNode fileNode) {
        this.children.add(fileNode);
    }
}
