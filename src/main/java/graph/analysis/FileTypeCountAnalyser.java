package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import graph.Edge;
import graph.FileNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import utils.PDFUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by conor on 07/09/2014.
 */
public class FileTypeCountAnalyser extends TreeAnalyser {

    private DelegateTree<FileNode, Edge> tree;
    private Map<String, Integer> fileTypeCounts = new HashMap<>();
    private String path;

    private String name = "File Type Count Analysis";
    private String desc = "Counts how many files of each type are in the given filesystem";

    private String reportContentStart = "Title:" + PDFUtils.getNewLineString() + " %s. " + PDFUtils.getNewLineString(2) +
            "Description:" + PDFUtils.getNewLineString() + " %s." + PDFUtils.getNewLineString(2) +
            "Results:" + PDFUtils.getNewLineString() +
            "-------------------------------------------------" + PDFUtils.getNewLineString();

    private String reportContentTemplate = "There are %d files of type %s in path %s." + PDFUtils.getNewLineString();

    private String reportContentAfter = PDFUtils.getNewLineString() + "-------------------------------------------------";

    public FileTypeCountAnalyser(DelegateTree<FileNode, Edge> tree, String path) {
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
        Queue<FileNode> tq = new LinkedList<FileNode>();
        tq.add(tree.getRoot());

        while(!tq.isEmpty()) {
            FileNode n = tq.poll();

            if (!fileTypeCounts.containsKey(n.getFileType())) {
                fileTypeCounts.put(n.getFileType(), 1);
            } else {
                fileTypeCounts.put(n.getFileType(), fileTypeCounts.get(n.getFileType())+1);
            }

            if (n.isDirectory()) {
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
        StringBuilder bs = new StringBuilder();
        bs.append(String.format(reportContentStart, name, desc));
        for (AbstractMap.Entry<String, Integer> entry : fileTypeCounts.entrySet()) {
            bs.append(String.format(reportContentTemplate, entry.getValue(), entry.getKey(), path));
        }
        bs.append(reportContentAfter);

        System.out.println(bs.toString());

        PDFUtils.writeWrappedText(page, content, bs.toString(), PDFUtils.getMargin(), 700);
        content.endText();
        content.close();

        return doc;
    }
}
