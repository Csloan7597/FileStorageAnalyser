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
import static net.sf.dynamicreports.report.builder.DynamicReports.*;


/**
 * Created by conor on 07/09/2014.
 *
 * Tree analyser which simply counts the number of files in a given path.
 * This does not include directories.
 */
public class FileCountAnalyser extends TreeAnalyser {

    private List<FileTreeNode> tree;

    private Map<String, Integer> fileCounts = new HashMap<>();
    private List<String> paths;

    private final String name = "File Count Analysis";
    private final String desc = "Counts how many files (not folders) are in the given filesystem";

    private final String reportTitleAsHtml = "Title: <b>%s</b>    Path: <i>%s</i>.<br/><br/> Description: <i> %s. </i><br/>";

    public FileCountAnalyser(List<FileTreeNode> tree, List<String> paths) {
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
        for (FileTreeNode root : tree) {
            int fileCount = 0;
            Queue<FileTreeNode> tq = new LinkedList<>();
            tq.add(root);

            while (!tq.isEmpty()) {
                FileTreeNode n = tq.poll();
                if (!n.isDirectory()) {
                    fileCount++;
                } else {
                    n.getChildren().forEach(tq::add);
                }
            }
            fileCounts.put(root.getPath(), fileCount);
        }
    }

    @Override
    public ByteArrayOutputStream generatePdfReport() throws PdfGenerationException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TextColumnBuilder<String> pathCol = DynamicReports.col.column("Path", "path", DynamicReports.type.stringType());
            TextColumnBuilder<Integer> fileCountCol = DynamicReports.col.column("File Count", "file_count", DynamicReports.type.integerType());

            DynamicReports.report()
                    .setColumnTitleStyle(DynamicReportStylesHelper.columnTitleStyle())
                    .title(Components.text(String.format(reportTitleAsHtml, name, paths.toString(), desc)).
                            setStyle(DynamicReportStylesHelper.styledMarkupStyle()))
                    .columns(//add columns
                            pathCol,
                            fileCountCol
                    )
                    .setDataSource(createDataSource())
                    .subtotalsAtPageFooter(
                            sbt.sum(fileCountCol).setLabel("Total: ").setLabelStyle(DynamicReportStylesHelper.boldStyle())
                    )
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
        DRDataSource dataSource = new DRDataSource("path", "file_count");
        fileCounts.forEach(dataSource::add);
        return dataSource;
    }
}
