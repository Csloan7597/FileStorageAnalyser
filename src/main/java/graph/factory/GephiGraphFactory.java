package graph.factory;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;


/**
 * Factory for creating a gephi graph from a filesystem.
 * Implemented as a singleton to avoid gephi static issues.
 *
 * Created by conor on 06/09/2014.
 */
public enum GephiGraphFactory {

    INSTANCE;

    // Gephi static resources
    private ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
    private GraphController gc = Lookup.getDefault().lookup(GraphController.class);
    private Workspace workspace;
    private GraphModel graphModel;

    // Default options
    private Options options = new Options.Builder().build();

    GephiGraphFactory() {
        pc.newProject();
        this.workspace = pc.getCurrentWorkspace();
        this.graphModel = gc.getGraphModel();
    }

    /**
     *
     * @param fs
     * @return
     */
    public DirectedGraph generateFsGraph(FileSystem fs) {
        DirectedGraph dg = graphModel.getDirectedGraph();
        dg.clear();
        fs.getRootDirectories().forEach((dir) -> buildGraphFromRoot(dir));

        System.out.println("Number of nodes: "+dg.getNodeCount());
        System.out.println("Number of Edges: "+dg.getEdgeCount());

        return dg;
    }

    public DirectedGraph generateFsGraph(String rootPath) {
        DirectedGraph dg = graphModel.getDirectedGraph();
        dg.clear();
        buildGraphFromRoot(new File(rootPath));

        System.out.println("Number of nodes: " + dg.getNodeCount());
        System.out.println("Number of Edges: "+dg.getEdgeCount());

        return dg;
    }

    public DirectedGraph generateFsGraph(URI fileRootUri) {
       return generateFsGraph(fileRootUri.getPath());
    }

    /**
     *  Iterative function to generate a tree structure from a given filesystem root.
     *  Uses a queue and passes pairs, containing node parent and current file to be processed.
     *
     * @param rootFile
     */
    private void buildGraphFromRoot(File rootFile) {
        final Queue<ImmutablePair<Node, File>> fileQueue = new LinkedList<>();
        fileQueue.add(new ImmutablePair<Node, File>(null, rootFile));
        DirectedGraph graph = graphModel.getDirectedGraph();

        while (!fileQueue.isEmpty()) {
            ImmutablePair<Node, File> currentPair = fileQueue.poll();
            Node parent = currentPair.getLeft();
            File currentFile = currentPair.getRight();

            Node n = graphModel.factory().newNode(currentFile.getPath());
            n.setLabel(currentFile.isDirectory() ? currentFile.getPath() : currentFile.getName());

            graph.addNode(n);
            if (parent != null) {
                graph.addEdge(graphModel.factory().newEdge(parent, n));
            }
            if (currentFile.isDirectory() && currentFile.listFiles() != null && currentFile.listFiles().length > 0) {
                Arrays.asList(currentFile.listFiles()).forEach((child) -> fileQueue.add(new ImmutablePair<>(n, child)));
            }
        }
    }

    /**
     * Set options for the FS traversal.
     * @param o options
     */
    public void setOptions(Options o) {
        this.options = o;
    }

    /**
     *  Overload to convert path to file.
     * @param rootPath
     */
    private void buildGraphFromRoot(Path rootPath) {
        buildGraphFromRoot(rootPath.toFile());
    }

    /**
     * Allows the client to configure how the graph is set up.
     * For example, a max depth or list of ignore strings.
     */
    public static class Options {
        private int maxDepth;
        private List<String> typeFilters;
        private List<String> ignoreList;

        public static class Builder {
            private int maxDepth = 1000;
            private List<String> typeFilters = Collections.EMPTY_LIST;
            private List<String> ignoreList = Collections.EMPTY_LIST;

            public Builder maxDepth(int maxDepth) {
                this.maxDepth = maxDepth;
                return this;
            }

            public Builder typeFilters(List<String> filters) {
                this.typeFilters = filters;
                return this;
            }
            public Builder typeFilters(String... filters) {
                this.typeFilters = Arrays.asList(filters);
                return this;
            }

            public Builder ignoreList(List<String> ignores) {
                this.ignoreList = ignores;
                return this;
            }
            public Builder ignoreList(String... ignores) {
                this.typeFilters = Arrays.asList(ignores);
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
