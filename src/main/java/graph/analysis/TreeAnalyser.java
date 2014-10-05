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

    /**
     * Standard constructor for a tree analyser. Specifies that a tree and a path
     * are required for any of these to run.
     * @param tree The tree structure to analyse
     * @param path The path in the filesystem the given tree represents
     */
    public TreeAnalyser(DelegateTree<FileNode, Edge> tree, String path) {
        this.setTree(tree);
        this.setPath(path);
    }

    /**
     * Return the name of this analysis, for use in any report/logging.
     * @return the analysis name
     */
    public abstract String getAnalysisName();

    /**
     * Return a brief description of this analysis, for use in any report/logging.
     * @return the description
     */
    public abstract String getDescription();

    /**
     * Set the tree to be analysed by this analyser.
     * @param tree a tree
     */
    public abstract void setTree(DelegateTree<FileNode, Edge> tree);

    /**
     * Set the filesystem path which is being analysed.
     * @param path the filesystem path
     */
    public abstract void setPath(String path);

    /**
     * Perform the analysis action, saving the results for future consumption.
     * @throws AnalysisException if there is a problem with the analysis
     */
    public abstract void doAnalyse() throws AnalysisException;

    /**
     * Once the analysis is complete, this can be used to get a byte stream representing a PDF file
     * report, containing the results of the analysis.
     * @return The analysis results as PDF stream
     * @throws PdfGenerationException if there is a problem generating the PDF report
     */
    public abstract ByteArrayOutputStream generatePdfReport() throws PdfGenerationException;

}
