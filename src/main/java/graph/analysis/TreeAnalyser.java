package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import graph.Edge;
import graph.FileNode;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

/**
 * Created by conor on 07/09/2014.
 */
public abstract class TreeAnalyser {

    public TreeAnalyser(DelegateTree<FileNode, Edge> tree, String path) {
        this.setTree(tree);
        this.setPath(path);
    }

    public abstract String getAnalysisName();

    public abstract String getDescription();

    public abstract void setTree(DelegateTree<FileNode, Edge> tree);

    public abstract void setPath(String path);

    public abstract void doAnalyse() throws AnalysisException;

    public abstract PDDocument generatePdfReport() throws IOException;

    public static class AnalysisException extends Exception {
        public AnalysisException() { super(); }
        public AnalysisException(String message) { super(message); }
        public AnalysisException(String message, Throwable cause) { super(message, cause); }
        public AnalysisException(Throwable cause) { super(cause); }
    }

}
