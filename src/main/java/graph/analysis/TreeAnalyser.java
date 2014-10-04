package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.Edge;
import graph.FileNode;

import java.io.ByteArrayOutputStream;

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

    public abstract ByteArrayOutputStream generatePdfReport() throws PdfGenerationException;

}
