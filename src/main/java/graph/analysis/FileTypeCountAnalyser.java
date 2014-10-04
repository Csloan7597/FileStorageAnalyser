package graph.analysis;

import edu.uci.ics.jung.graph.DelegateTree;
import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.Edge;
import graph.FileNode;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.ReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import utils.DynamicReportStylesHelper;
import utils.PDFUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private String reportTitleAsHtml = "Title: <b>%s</b>    Path: <i>%s</i>.<br/><br/> Description: <i> %s. </i><br/>";

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
    public ByteArrayOutputStream generatePdfReport() throws PdfGenerationException {
        try {
            TextColumnBuilder<String> fileTypeCol =
                    DynamicReports.col.column("File Type", "file_type", DynamicReports.type.stringType());
            TextColumnBuilder<Integer>fileCountCol =
                    DynamicReports.col.column("File Count", "file_count", DynamicReports.type.integerType());
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ReportBuilder<JasperReportBuilder> report = DynamicReports.report()
                    .setColumnTitleStyle(DynamicReportStylesHelper.columnTitleStyle())
                    .title(Components.text(String.format(reportTitleAsHtml, name, path, desc)).
                            setStyle(DynamicReportStylesHelper.styledMarkupStyle()))
                    .columns(//add columns
                        fileTypeCol, fileCountCol
                    )
                    .setDataSource(createDataSource())
                    .summary(
                            DynamicReports.cht.pieChart()
                                    .setTitle("Pie chart")
                                    .setStyle(DynamicReportStylesHelper.boldStyle())
                                    .setKey(fileTypeCol)
                                    .series(DynamicReports.cht.serie(fileCountCol)))
                    .toPdf(outputStream);
            return outputStream;
        } catch (DRException e) {
            System.err.println("Error generating PDF" + e.getMessage());
            throw new PdfGenerationException("Error generating PDF with DynamicReports", e);
        }
    }

    private JRDataSource createDataSource() {
        DRDataSource dataSource = new DRDataSource("file_type", "file_count");
        fileTypeCounts.forEach( (fileType, count) -> dataSource.add(fileType, count));
        return dataSource;
    }

}
