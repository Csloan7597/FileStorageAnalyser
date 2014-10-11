package graph.analysis;

import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.FileTreeNode;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by conor on 07/09/2014.
 */
public abstract class TreeAnalyser {

    /**
     * Standard constructor for a tree analyser. Specifies that a tree and a path
     * are required for any of these to run.
     * @param tree The tree structure to analyse
     * @param paths The path in the filesystem the given tree represents
     */
    public TreeAnalyser(List<FileTreeNode> tree, List<String> paths) {
        this.setTree(tree);
        this.setPaths(paths);
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
    public abstract void setTree(List<FileTreeNode> tree);

    /**
     * Set the filesystem path which is being analysed.
     * @param paths the filesystem paths
     */
    public abstract void setPaths(List<String> paths);

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
