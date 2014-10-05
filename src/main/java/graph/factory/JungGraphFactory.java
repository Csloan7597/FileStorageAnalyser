package graph.factory;

import edu.uci.ics.jung.graph.DelegateTree;
import graph.Edge;
import graph.FileNode;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by conor on 06/09/2014.
 *
 * Factory for creating a JUNG based tree structure, containing information about files in
 * a tree structure. Contains inner class Options, which uses the Builder pattern to allow certain rules to be specified
 * in information retrieval of files, such as type filters or files to ignore. These options should be built and passed
 * to this object.
 */
public class JungGraphFactory {

    // Default options
    private Options options = new Options.Builder().build();

    /**
     * Generate a tree structure from the given FileSystem.
     * @param fs a filesystem
     * @return the generated tree structure
     */
    public DelegateTree<FileNode, Edge> generateFsGraph(FileSystem fs) {
        DelegateTree<FileNode, Edge> t = new DelegateTree<>();
        fs.getRootDirectories().forEach((dir) -> buildGraphFromRoot(dir, t));
        return t;
    }

    /**
     * Generate a tree structure from the given path.
     * @param rootPath the path
     * @return the generated tree structure
     */
    public DelegateTree<FileNode, Edge> generateFsGraph(String rootPath) {
        DelegateTree<FileNode, Edge> t = new DelegateTree<>();
        buildGraphFromRoot(new File(rootPath), t);
        return t;
    }

    /**
     * Iterative function to generate a tree structure from a given filesystem root.
     * Uses a queue and passes pairs, containing node parent and current file to be processed.
     *
     * @param rootFile the file at the top of the tree to be generated
     */
    void buildGraphFromRoot(File rootFile, DelegateTree<FileNode, Edge> tree) {
        final Queue<ImmutablePair<FileNode, File>> fileQueue = new LinkedList<>();
        fileQueue.add(new ImmutablePair<>(null, rootFile));

        while (!fileQueue.isEmpty()) {
            ImmutablePair<FileNode, File> currentPair = fileQueue.poll();
            FileNode parent = currentPair.getLeft();
            File currentFile = currentPair.getRight();
            FileNode n = new FileNode(currentFile);

            if (parent != null && tree.getDepth(parent) == options.getMaxDepth()) {
                break;
            }
            if (parent == null) {
                tree.setRoot(n);
            } else {
                tree.addChild(new Edge(), parent, n);
            }

            File[] files = currentFile.listFiles();
            if (currentFile.isDirectory() && files != null && files.length > 0) {
                Arrays.stream(files).forEach((child) -> fileQueue.add(new ImmutablePair<>(n, child)));
            }
        }
    }

    /**
     * Overload to convert path to file.
     *
     * @param rootPath the path
     */
    private void buildGraphFromRoot(Path rootPath, DelegateTree<FileNode, Edge> dg) {
        buildGraphFromRoot(rootPath.toFile(), dg);
    }

    /**
     * Constructor with options to be set.
     * @param options the options
     */
    public JungGraphFactory(Options options) {
        this.options = options;
    }



    /**
     * Set options for the FS traversal.
     *
     * @param o options
     */
    public void setOptions(Options o) {
        this.options = o;
    }

    /**
     * Allows the client to configure how the graph is set up.
     * For example, a max depth or list of ignore strings.
     */
    public static class Options {
        private final int maxDepth;
        private final List<String> typeFilters;
        private final List<String> ignoreList;

        public static class Builder {
            private int maxDepth = 1000;
            private List<String> typeFilters = Collections.EMPTY_LIST;
            private List<String> ignoreList = Collections.EMPTY_LIST;

            public Builder maxDepth(int maxDepth) {
                this.maxDepth = maxDepth;
                return this;
            }

            public Builder typeFilters(List<String> filters) {
                if (filters != null) {
                    this.typeFilters = filters;
                }
                return this;
            }

            public Builder typeFilters(String... filters) {
                if (filters != null) {
                    this.typeFilters = Arrays.asList(filters);
                }
                return this;
            }

            public Builder ignoreList(List<String> ignores) {
                if (ignores != null) {
                    this.ignoreList = ignores;
                }
                return this;
            }

            public Builder ignoreList(String... ignores) {
                if (ignores != null) {
                    this.typeFilters = Arrays.asList(ignores);
                }
                return this;
            }

            public Options build() {
                return new Options(this);
            }
        }

        public int getMaxDepth() {
            return this.maxDepth;
        }

        public List<String> getTypeFilters() {
            return this.typeFilters;
        }

        public List<String> getIgnoreList() {
            return this.ignoreList;
        }

        public Options(Builder b) {
            this.ignoreList = b.ignoreList;
            this.maxDepth = b.maxDepth;
            this.typeFilters = b.typeFilters;
        }
    }
}
