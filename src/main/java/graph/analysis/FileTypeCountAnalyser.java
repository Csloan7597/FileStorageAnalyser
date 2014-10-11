package graph.analysis;

import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.FileTreeNode;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import utils.DynamicReportStylesHelper;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Created by conor on 07/09/2014.
 *
 * Tree analyser which, for a given filesystem path, counts the occurrence of each different type of file found.
 * This is then compiled into a table and other formats.
 */
public class FileTypeCountAnalyser extends TreeAnalyser {

    private List<FileTreeNode> tree;
    private List<String> paths;

    private final Map<String, Integer> fileTypeCounts = new HashMap<>();
    private static final String name = "File Type Count Analysis";
    private static final String desc = "Counts how many files of each type are in the given filesystem";
    private static final String reportTitleAsHtml = "Title: <b>%s</b>    Path: <i>%s</i>.<br/><br/> Description: <i> %s. </i><br/>";

    public FileTypeCountAnalyser(List<FileTreeNode> tree, List<String> paths) {
        super(tree, paths);
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
    public void setTree(List<FileTreeNode> tree) {
        this.tree = tree;
    }

    @Override
    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public void doAnalyse() throws AnalysisException {

        Queue<FileTreeNode> tq = new LinkedList<>();
        tree.forEach(tq::add);

        while (!tq.isEmpty()) {
            FileTreeNode n = tq.poll();
            if (!fileTypeCounts.containsKey(n.getFileType())) {
                fileTypeCounts.put(n.getFileType(), 1);
            } else {
                fileTypeCounts.put(n.getFileType(), fileTypeCounts.get(n.getFileType()) + 1);
            }

            if (n.isDirectory()) { n.getChildren().forEach(tq::add); }
        }
    }

    @Override
    public ByteArrayOutputStream generatePdfReport() throws PdfGenerationException {
        try {
            TextColumnBuilder<String> fileTypeCol =
                    DynamicReports.col.column("File Type", "file_type", DynamicReports.type.stringType());
            TextColumnBuilder<Integer> fileCountCol =
                    DynamicReports.col.column("File Count", "file_count", DynamicReports.type.integerType());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DynamicReports.report()
                    .setColumnTitleStyle(DynamicReportStylesHelper.columnTitleStyle())
                    .title(Components.text(String.format(reportTitleAsHtml, name, paths, desc)).
                            setStyle(DynamicReportStylesHelper.styledMarkupStyle()))
                    .columns(//add columns
                            fileTypeCol, fileCountCol
                    )
                    .setDataSource(createDataSource())
                    .summary(
                            DynamicReports.cht.pieChart()
                                    .setTitle("Pie Chart of File Types in specified files")
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

    /**
     * Generate the data into the PDF report being generated.
     * @return data source to inject into PDF report
     */
    private JRDataSource createDataSource() {
        DRDataSource dataSource = new DRDataSource("file_type", "file_count");
        fileTypeCounts.forEach(dataSource::add);
        return dataSource;
    }

}
