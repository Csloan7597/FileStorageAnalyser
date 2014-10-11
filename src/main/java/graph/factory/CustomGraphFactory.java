package graph.factory;

import graph.FileTreeNode;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by conor on 11/10/2014.
 */
public class CustomGraphFactory {

    // Default options
    private Options options = new Options.Builder().build();

    /**
     * Set options for the FS traversal.
     *
     * @param o options
     */
    public void setOptions(Options o) {
        this.options = o;
    }

    /**
     * Generate a tree structure from the given FileSystem.
     * @param fs a filesystem
     * @return the generated tree structure
     */
    public List<FileTreeNode> generateFsGraph(FileSystem fs) {
        List<FileTreeNode> fileNodes = new ArrayList<>();
        fs.getRootDirectories().forEach((dir) -> fileNodes.add(buildGraphFromRoot(dir)));
        return fileNodes;
    }

    /**
     * Generate a tree structure from the given path.
     * @param rootPaths the paths
     * @return the generated tree structure
     */
    public List<FileTreeNode> generateFsGraph(List<String> rootPaths) {
        List<FileTreeNode> fileNodes = new ArrayList<>();
        List<File> files = rootPaths.stream().map(File::new).collect(Collectors.toList());
        files.forEach((file) -> fileNodes.add(buildGraphFromRoot(file)));
        return fileNodes;
    }

    /**
     * Iterative function to generate a tree structure from a given filesystem root.
     * Uses a queue and passes pairs, containing node parent and current file to be processed.
     *
     * @param rootFile the file at the top of the tree to be generated
     */
    FileTreeNode buildGraphFromRoot(File rootFile) {
        final Queue<ImmutablePair<FileTreeNode, File>> fileQueue = new LinkedList<>();
        fileQueue.add(new ImmutablePair<>(null, rootFile));
        FileTreeNode root = null;

        while (!fileQueue.isEmpty()) {
            ImmutablePair<FileTreeNode, File> currentPair = fileQueue.poll();
            FileTreeNode parent = currentPair.getLeft();
            File currentFile = currentPair.getRight();
            FileTreeNode n = new FileTreeNode(currentFile, parent);

            if (parent != null  && n.getDepth() >= options.getMaxDepth()) {
                break;
            }
            if (parent == null) {
                root = n;
            } else {
                parent.addChild(n);
            }

            File[] files = currentFile.listFiles();
            if (currentFile.isDirectory() && files != null && files.length > 0) {
                Arrays.stream(files).forEach((child) -> fileQueue.add(new ImmutablePair<>(n, child)));
            }
        }

        return root;
    }

    /**
     * Overload to convert path to file.
     *
     * @param rootPath the path
     */
    private FileTreeNode buildGraphFromRoot(Path rootPath) {
        return buildGraphFromRoot(rootPath.toFile());
    }

    /**
     * Constructor with options to be set.
     * @param options the options
     */
    public CustomGraphFactory(Options options) {
        this.options = options;
    }

}
