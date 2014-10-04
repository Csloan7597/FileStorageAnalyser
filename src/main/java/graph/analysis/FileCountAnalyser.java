package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.Edge;
import graph.FileNode;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.ReportBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import utils.DynamicReportStylesHelper;
import utils.PDFUtils;

import java.io.ByteArrayOutputStream;
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

    private String reportTitleAsHtml = "Title: <b>%s</b>    Path: <i>%s</i>.<br/><br/> Description: <i> %s. </i><br/>";
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
    public ByteArrayOutputStream generatePdfReport() throws PdfGenerationException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ReportBuilder<JasperReportBuilder> report = DynamicReports.report()
                    .setColumnTitleStyle(DynamicReportStylesHelper.columnTitleStyle())
                    .title(Components.text(String.format(reportTitleAsHtml, name, path, desc)).
                            setStyle(DynamicReportStylesHelper.styledMarkupStyle()))
                    .columns(//add columns
                            DynamicReports.col.column("Path", "path", DynamicReports.type.stringType()),
                            DynamicReports.col.column("File Count", "file_count", DynamicReports.type.integerType())
                    )
                    .setDataSource(createDataSource())
                    .toPdf(outputStream);
            return outputStream;
        } catch (DRException e) {
            System.err.println("Error generating PDF" + e.getMessage());
            throw new PdfGenerationException("Error generating PDF with DynamicReports", e);
        }
    }

    private JRDataSource createDataSource() {
        DRDataSource dataSource = new DRDataSource("path", "file_count");
        dataSource.add(path, fileCount);
        return dataSource;
    }
}
