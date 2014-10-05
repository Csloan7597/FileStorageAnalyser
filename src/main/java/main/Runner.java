package main;

import edu.uci.ics.jung.graph.DelegateTree;
import exceptions.PdfGenerationException;
import graph.Edge;
import graph.FileNode;
import graph.analysis.TreeAnalyser;
import graph.analysis.TreeAnalyserRunnable;
import graph.factory.JungGraphFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import utils.JsonFileLoadHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by conor on 06/09/2014.
 *
 * Main class of this project. Takes command line args and performs the following operations:
 *
 * - Load configuration
 * - Generate tree structure from given path
 * - Start analyses as separate threads working on the same tree structure in memory
 * - Wait for analyses to finish
 * - Generate reports for each analysis (as PDF)
 * - Merge PDF reports into one master report
 * - Write master report to the specified file
 *
 * The project can be called with a config file or with just arguments, see docs for more details.
 */
public class Runner {

    /**
     * Run with a config model, containing all the required information to run the analyses.
     * @param config configuration for this run
     */
    public Runner(Map<String, Object> config) {
        // Set correct options & create a factory
        JungGraphFactory.Options options = new JungGraphFactory.Options.Builder()
                .ignoreList((List<String>) config.get("ignoreList"))
                .typeFilters((List<String>) config.get("typeFilters"))
                .maxDepth((Integer) config.get("maxDepth")).build();
        JungGraphFactory factory = new JungGraphFactory(options);

        // Use the factory to read in the FS and create a graph
        String path = (String) config.get("path");
        String logPath = (String) config.get("logPath");

        DelegateTree<FileNode, Edge> tree = (path == null) ?
                factory.generateFsGraph(FileSystems.getDefault()) : factory.generateFsGraph(path);

        // Read in the 'analyser' tokens and create the analyser list
        List<String> analyserNames = ((List<Object>) config.get("analysers")).stream()
                .map(item -> (Map<String, String>) item)
                .map(item -> item.get("className"))
                .collect(Collectors.toList());
        List<TreeAnalyser> tas = resolveAnalysers(path, analyserNames, tree);

        // Run the analyses as threads
        Map<TreeAnalyser, Thread> threads = runAnalysersInParallel(tas);

        // Wait for threads to finish
        waitForAnalyserThreadsFinish(threads);

        // Get PDFs and print them
        printMergedPdf(logPath, getPdfStreamsFromAnalysers(threads));

        System.out.println("Finished! Your report is ready at path: " + logPath);
    }

    /**
     * For when the application is run with limited command line options. Specific pieces of config are
     * passed here, enough to run the application in its simplest form.
     * @param path The root path to analyse files from
     * @param logPath The path to write the resulting PDF report to
     * @param ignores A list of files/types to ignore
     * @param typeFilters A list of types to be the only ones included, the opposite of ignores
     * @param maxDepth The maximum tree depth to delve when traversing files in the filesystem
     * @param analysers A list of strings representing java class files, which perform analysis
     */
    public Runner(String path, String logPath, List<String> ignores, List<String> typeFilters, int maxDepth,
                  String analysers) {

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
        List<TreeAnalyser> tas = resolveAnalysers(path, Arrays.asList(analysers.split(",")), tree);

        // Run the analyses as threads
        Map<TreeAnalyser, Thread> threads = runAnalysersInParallel(tas);

        // Wait for threads to finish
        waitForAnalyserThreadsFinish(threads);

        // Get PDF streams and print
        printMergedPdf(logPath, getPdfStreamsFromAnalysers(threads));

        System.out.println("Finished! Your report is ready at path: " + logPath);
    }

    /**
     * Given a number of threads containing analysers, generate reports as PDF and return these as a list.
     * @param threads Map of Analyser to Thread
     * @return the pdf reports
     */
    List<ByteArrayOutputStream> getPdfStreamsFromAnalysers(Map<TreeAnalyser, Thread> threads) {
        List<ByteArrayOutputStream> pdfStreams = new ArrayList<>();
        threads.forEach((analyser, thread) -> {
            try {
                pdfStreams.add(analyser.generatePdfReport());
            } catch (PdfGenerationException p) {
                System.err.println("Error generating pdf for: " + analyser.getAnalysisName());
            }
        });
        return pdfStreams;
    }

    /**
     * Given a specific path to write to and a number of streams, merges the streams into one PDF file and writes it
     * to the filesystem.
     * @param logPath The path to write the resulting PDF report to
     * @param pdfStreams the PDF streams
     */
    void printMergedPdf(String logPath, List<ByteArrayOutputStream> pdfStreams) {
        // Merge and print document
        PDFMergerUtility mergeUtil = new PDFMergerUtility();
        pdfStreams.forEach((pdfStream) -> mergeUtil.addSource(new ByteArrayInputStream(pdfStream.toByteArray())));

        try {
            mergeUtil.setDestinationFileName(logPath);
            mergeUtil.mergeDocuments();
        } catch (COSVisitorException | IOException e) {
            System.err.println("Error merging the document - sorry!");
        }
    }

    /**
     * Given a number of threads / analysers, waits for every thread to finish.
     * @param threads Map of Analyser to Thread
     */
    void waitForAnalyserThreadsFinish(Map<TreeAnalyser, Thread> threads) {
        threads.forEach((analyser, thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Interrupted Exception - Threading issue");
            }
        });
    }

    /**
     * Given a number of analysers, wraps each in a runnable, and runs it in a new thread. Also creates a map of
     * analysers to the newly created thread.
     * @param tas the analysers
     * @return map of analyser to thread
     */
    Map<TreeAnalyser, Thread> runAnalysersInParallel(List<TreeAnalyser> tas) {
        Map<TreeAnalyser, Thread> threads = new HashMap<>();
        tas.forEach(analyser -> {
            Thread t = new Thread(new TreeAnalyserRunnable(analyser));
            threads.put(analyser, t);
            t.start();
        });
        return threads;
    }

    /**
     * Given a list of strings representing java classes, uses java reflection to load the specified classes, and
     * instantiates these as tree analysers. Adds these to a list, which is then returned.
     * @param path the file path to pass to tree analysers
     * @param analysers the analyser package names
     * @param tree the tree to pass to analysers
     * @return the instantiated tree analysers
     */
    List<TreeAnalyser> resolveAnalysers(String path, List<String> analysers, DelegateTree<FileNode, Edge> tree) {
        List<TreeAnalyser> tas = new ArrayList<>();
        Class<?>[] expectedConstructorParams = new Class<?>[]{DelegateTree.class, String.class};
        analysers.forEach(s -> {
            try {
                Class<?> clazz = Class.forName(s);
                Arrays.stream(clazz.getConstructors()).forEach(constructor -> {
                    if (constructor.getParameterCount() == 2 &&
                            Arrays.equals(expectedConstructorParams, constructor.getParameterTypes())) {
                        try {
                            tas.add((TreeAnalyser) constructor.newInstance(tree, path));
                            System.out.println("Preparing to run: " + s);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            System.err.println("Could not instantiate: " + s + ".. ensure this class implements " +
                                    "the TreeAnalyser interface & has the correct constructor. skipping.");
                        }
                    }
                });
            } catch (ClassNotFoundException e) {
                System.err.println("Class: " + s + "not found. make sure you have typed the name correctly. skipping.");
            } catch (NullPointerException e) {
                System.err.println("One of your classNames was not correctly configured - please check and try again.");
            }
        });
        return tas;
    }


    /**
     * USAGE:
     * <p>
     * run --path <path> --maxDepth <max> --ignore<commaseplist> --typeFilter<commaseplist> --logpath <path>
     * --analysers <comseplist>
     *
     * @param args command line args
     */
    public static void main(String[] args) {

        String path = null;
        String logPath = null;
        String analysers = "";
        int maxDepth = 1000;
        List<String> ignores = null;
        List<String> typeFilters = null;

        // If a config has been passed in, use this.
        int index = Arrays.asList(args).indexOf("--config");
        if (index != -1 && index != args.length) {
            try {
                Map<String, Object> config = JsonFileLoadHelper.loadJsonFile(args[index + 1]);
                new Runner(config);
            } catch (FileNotFoundException e) {
                System.err.println("Config file specified not found.");
            } catch (IOException e) {
                System.err.println("There was a problem reading in the config file.");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }

        // Read args if no config supplied
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i].toLowerCase()) {
                case "--path":
                    path = args[i + 1];
                    break;
                case "--maxdepth":
                    maxDepth = Integer.parseInt(args[i + 1]);
                    break;
                case "--ignore":
                    ignores = Arrays.asList(args[i + 1].split(","));
                    break;
                case "--typefilter":
                    typeFilters = Arrays.asList(args[i + 1].split(","));
                    break;
                case "--logpath":
                    logPath = args[i + 1];
                    break;
                case "--analysers":
                    analysers = args[i + 1];
                    break;
                default:
                    break;
            }
        }

        // Run
        new Runner(path, logPath, ignores, typeFilters, maxDepth, analysers);
    }

}
