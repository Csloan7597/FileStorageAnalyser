package graph.analysis;

import exceptions.AnalysisException;
import exceptions.PdfGenerationException;
import graph.FileTreeNode;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import utils.DynamicReportStylesHelper;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * Created by conor on 11/10/2014.
 */
public class FileInfoAnalyser extends TreeAnalyser {

    private DRDataSource dataSource;
    private List<FileTreeNode> tree;
    private List<String> paths;

    private static final String name = "File Info Analysis";
    private static final String desc = "Provides information on files under the selected paths";
    private static final String reportTitleAsHtml = "Title: <b>%s</b>    Path: <i>%s</i>.<br/><br/> Description: <i> %s. </i><br/>";

    public FileInfoAnalyser(List<FileTreeNode> tree, List<String> paths) { super(tree, paths); }

    @Override
    public String getAnalysisName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.desc;
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
        this.dataSource = new DRDataSource("path", "size", "type", "hidden", "root_path", "depth");
        for (FileTreeNode t : tree) {
            Queue<FileTreeNode> tq = new LinkedList<>();
            tq.add(t);
            while (!tq.isEmpty()) {
                FileTreeNode n = tq.poll();
                if (!n.isDirectory()) {
                    dataSource.add(n.getPath().substring(t.getPath().length()), n.getFileSize() / 1024, n.getFileType(),
                            n.isHidden(), t.getPath(), n.getDepth());
                } else {
                    n.getChildren().forEach(tq::add);
                }
            }
        }
    }

    @Override
    public ByteArrayOutputStream generatePdfReport() throws PdfGenerationException {
        try {
            TextColumnBuilder<String> pathCol =
                    DynamicReports.col.column("File Path", "path", DynamicReports.type.stringType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());
            TextColumnBuilder<Long> sizeCol =
                    DynamicReports.col.column("File Size (kb)", "size", DynamicReports.type.longType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());
            TextColumnBuilder<String> typeCol =
                    DynamicReports.col.column("File Type", "type", DynamicReports.type.stringType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());
            TextColumnBuilder<Boolean> hiddenCol =
                    DynamicReports.col.column("Hidden?", "hidden", DynamicReports.type.booleanType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());
            TextColumnBuilder<String> rootPathCol =
                    DynamicReports.col.column("Root Path", "root_path", DynamicReports.type.stringType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());
            TextColumnBuilder<Integer> depthCol =
                    DynamicReports.col.column("Depth from root", "depth", DynamicReports.type.integerType())
                            .setStyle(DynamicReportStylesHelper.centeredStyle());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DynamicReports.report()
                    .setColumnTitleStyle(DynamicReportStylesHelper.columnTitleStyle())
                    .title(Components.text(String.format(reportTitleAsHtml, name, paths, desc)).
                            setStyle(DynamicReportStylesHelper.styledMarkupStyle()))
                    .columns(//add columns
                            pathCol.setWidth(35), sizeCol.setWidth(5),
                            typeCol.setWidth(10), hiddenCol.setWidth(10),
                            rootPathCol.setWidth(35), depthCol.setWidth(5)
                    )
                    .setDataSource(this.dataSource)
                    .toPdf(outputStream);
            return outputStream;
        } catch (DRException e) {
            System.err.println("Error generating PDF" + e.getMessage());
            throw new PdfGenerationException("Error generating PDF with DynamicReports", e);
        }
    }
}
