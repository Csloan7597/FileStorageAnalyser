package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import graph.Edge;
import graph.FileNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import utils.PDFUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by conor on 07/09/2014.
 */
public class FileCountAnalyser extends TreeAnalyser {

    private DelegateTree<FileNode, Edge> tree;
    private int fileCount = 0;
    private String path;

    private String name = "File Count Analysis";
    private String desc = "Counts how many files (not folders) are in the given filesystem";

    private String reportContent = "Title:" + PDFUtils.getNewLineString() + " %s. " + PDFUtils.getNewLineString(2) +
            "Description:" + PDFUtils.getNewLineString() + " %s." + PDFUtils.getNewLineString(2) +
            "Results:" + PDFUtils.getNewLineString() +
            "-------------------------------------------------" + PDFUtils.getNewLineString() +
            "There are %d files in path %s." + PDFUtils.getNewLineString() +
            "-------------------------------------------------";

    public FileCountAnalyser(DelegateTree<FileNode, Edge> tree, String path) {
        super(tree, path);
    }

    @Override
    public String getAnalysisName() {
        return name;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public void setTree(DelegateTree<FileNode, Edge> tree) {
        this.tree = tree;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void doAnalyse() throws AnalysisException {
        Queue<FileNode> tq = new LinkedList<>();
        tq.add(tree.getRoot());

        while(!tq.isEmpty()) {
            FileNode n = tq.poll();
            if (!n.isDirectory()) {
                fileCount++;
            } else {
                tree.getChildren(n).forEach((node) -> tq.add(node));
            }
        }
    }

    @Override
    public PDDocument generatePdfReport() throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);

        PDPageContentStream content = new PDPageContentStream(doc, page);
        PDFUtils.setFont(content);

        content.beginText();
        PDFUtils.writeWrappedText(page, content, String.format(reportContent, name,
                desc, fileCount, path), PDFUtils.getMargin(), 700);
        content.endText();
        content.close();

        return doc;
    }
}
