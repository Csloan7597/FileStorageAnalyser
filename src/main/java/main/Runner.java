package main;

import edu.uci.ics.jung.graph.DelegateTree;
import graph.Edge;
import graph.FileNode;
import graph.analysis.FileCountAnalyser;
import graph.analysis.FileTypeCountAnalyser;
import graph.analysis.TreeAnalyser;
import graph.analysis.TreeAnalyserRunnable;
import graph.factory.JungGraphFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFMergerUtility;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;

/**
 * Created by conor on 06/09/2014.
 */
public class Runner {

    /*
        TODO: Use DynamicReports to do reports, not this stupid PDF tool!
        http://stackoverflow.com/questions/13808090/create-pdf-and-merge-with-pdfbox
     */

    public Runner(String path, String logPath, List<String> ignores, List<String> typeFilters, int maxDepth,
                  String analysers) throws IOException, TreeAnalyser.AnalysisException {

        // Set correct options & create a factory
        JungGraphFactory.Options options = new JungGraphFactory.Options.Builder()
                .ignoreList(ignores)
                .typeFilters(typeFilters)
                .maxDepth(maxDepth).build();
        JungGraphFactory factory = new JungGraphFactory(options);
                                                                
        // Use the configured factory to read in the filesystem & create a graph
        DelegateTree<FileNode, Edge> tree = (path == null) ?
                 factory.generateFsGraph(FileSystems.getDefault()) : factory.generateFsGraph(path);

        // Read in the 'analyser' tokens and create the analyser list
        List<TreeAnalyser> tas = new ArrayList<>();
        for (String s : analysers.split(",")) {
            switch (s.toLowerCase().trim()) {
                // Add other matching cases here and they drop down
                case "filetypecount" : tas.add(new FileTypeCountAnalyser(tree, path)); break;
                case "filecount" :  tas.add(new FileCountAnalyser(tree, path)); break;
            }
        }

        // Run the analyses as threads
        List<PDDocument> docs = new ArrayList<>();
        Map<TreeAnalyser, Thread> threads = new HashMap<>();
        for (TreeAnalyser analyser : tas) {
            TreeAnalyserRunnable tar = new TreeAnalyserRunnable(analyser);
            Thread t = new Thread(tar);
            t.start();
            threads.put(analyser, t);
        }

        // Wait for all threads to finish
        for (AbstractMap.Entry<TreeAnalyser, Thread> entry : threads.entrySet()) {
            try {
                entry.getValue().join();
                docs.add(entry.getKey().generatePdfReport());
            } catch (InterruptedException e) {
                System.err.println("Interrupted Exception");
            }
        }

        // Merge documents
        PDDocument master = new PDDocument();
        PDFMergerUtility mergeUtil = new PDFMergerUtility();
        docs.forEach( (doc) ->
        {
            try {
                mergeUtil.appendDocument(master, doc);
            } catch (IOException e) {
                System.err.println("Document was not added");
            }
        });

        try {
            mergeUtil.mergeDocuments();
        } catch (COSVisitorException e) {
            System.err.println("Document was not merged");
        }

        try {  // Print the report
            master.save(logPath);
            master.close();
        } catch (COSVisitorException e) {
            System.err.println("Could not save PDF");
        }
    }


    /**
     * USAGE:
     *
     *  run --path <path> --maxDepth <max> --ignore<commaseplist> --typeFilter<commaseplist> --logpath <path>
     *      --analysers <comseplist>
     *
     * @param args
     * @throws IOException
     * @throws TreeAnalyser.AnalysisException
     */
    public static void main(String[] args) throws IOException, TreeAnalyser.AnalysisException {

        String path = null;
        String logPath = null;
        String analysers = "";
        int maxDepth = 1000;
        List<String> ignores = null;
        List<String> typeFilters = null;

        // Read args
        for (int i = 0; i < args.length-1; i++) {
            switch (args[i].toLowerCase()) {
                case "--path" : path = args[i+1]; break;
                case "--maxdepth" : maxDepth = Integer.parseInt(args[i+1]); break;
                case "--ignore" : ignores = Arrays.asList(args[i + 1].split(",")); break;
                case "--typefilter" : typeFilters = Arrays.asList(args[i + 1].split(",")); break;
                case "--logpath" : logPath = args[i + 1]; break;
                case "--analysers" : analysers = args [i + 1]; break;
                default : break;
            }
        }

        // Validate args
        // TODO validate args

        // Run
        Runner r = new Runner(path, logPath, ignores, typeFilters, maxDepth, analysers);
    }

}
