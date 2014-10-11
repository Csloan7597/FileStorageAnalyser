package main;

import exceptions.PdfGenerationException;
import graph.FileTreeNode;
import graph.analysis.TreeAnalyser;
import graph.analysis.TreeAnalyserCallable;
import graph.factory.CustomGraphFactory;
import graph.factory.Options;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import utils.JsonFileLoadHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by conor on 06/09/2014.
 * <p>
 * Main class of this project. Takes command line args and performs the following operations:
 * <p>
 * - Load configuration
 * - Generate tree structure from given path
 * - Start analyses as separate threads working on the same tree structure in memory
 * - Wait for analyses to finish
 * - Generate reports for each analysis (as PDF)
 * - Merge PDF reports into one master report
 * - Write master report to the specified file
 * <p>
 * The project can be called with a config file or with just arguments, see docs for more details.
 */
public class Runner {

    /**
     * Run with a config model, containing all the required information to run the analyses.
     *
     * @param config configuration for this run
     */
    public Runner(Map<String, Object> config) {
        // Set correct options & create a factory
        Options options = new Options.Builder()
                .ignoreList((List<String>) config.get("ignoreList"))
                .typeFilters((List<String>) config.get("typeFilters"))
                .maxDepth((Integer) config.get("maxDepth")).build();
        CustomGraphFactory factory = new CustomGraphFactory(options);

        // Use the factory to read in the FS and create a graph
        List<String> paths = (List<String>) config.get("paths");
        String logPath = (String) config.get("logPath");

        List<FileTreeNode> roots = (paths == null) ?
                factory.generateFsGraph(FileSystems.getDefault()) : factory.generateFsGraph(paths);

        // Read in the 'analyser' tokens and create the analyser list
        List<String> analyserNames = ((List<Object>) config.get("analysers")).stream()
                .map(item -> (Map<String, String>) item)
                .map(item -> item.get("className"))
                .collect(Collectors.toList());
        List<TreeAnalyser> tas = resolveAnalysers(paths, analyserNames, roots);

        // Run threads
        runAnalysersInParallel(tas);

        // Get PDFs and print them
        printMergedPdf(logPath, getPdfStreamsFromAnalysers(tas));

        System.out.println("Finished! Your report is ready at path: " + logPath);
    }


    /**
     * For when the application is run with limited command line options. Specific pieces of config are
     * passed here, enough to run the application in its simplest form.
     *
     * @param paths        The root path to analyse files from
     * @param logPath     The path to write the resulting PDF report to
     * @param ignores     A list of files/types to ignore
     * @param typeFilters A list of types to be the only ones included, the opposite of ignores
     * @param maxDepth    The maximum tree depth to delve when traversing files in the filesystem
     * @param analysers   A list of strings representing java class files, which perform analysis
     */
    public Runner(List<String> paths, String logPath, List<String> ignores, List<String> typeFilters, int maxDepth,
                  String analysers) {

        // Set correct options & create a factory
        Options options = new Options.Builder()
                .ignoreList(ignores)
                .typeFilters(typeFilters)
                .maxDepth(maxDepth).build();
        CustomGraphFactory factory = new CustomGraphFactory(options);

        // Use the configured factory to read in the filesystem & create a graph
        List<FileTreeNode> roots = (paths == null) ?
                factory.generateFsGraph(FileSystems.getDefault()) : factory.generateFsGraph(paths);

        // Read in the 'analyser' tokens and create the analyser list
        List<TreeAnalyser> tas = resolveAnalysers(paths, Arrays.asList(analysers.split(",")), roots);

        // Run threads
        runAnalysersInParallel(tas);

        // Get PDFs and print them
        printMergedPdf(logPath, getPdfStreamsFromAnalysers(tas));

        System.out.println("Finished! Your report is ready at path: " + logPath);
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

        Runner runner = null;

        // If a config has been passed in, use this.
        int index = Arrays.asList(args).indexOf("--config");
        if (index != -1 && index != args.length) {
            try {
                Map<String, Object> config = JsonFileLoadHelper.loadJsonFile(args[index + 1]);
                runner = new Runner(config);
            } catch (FileNotFoundException e) {
                System.err.println("Config file specified not found.");
                System.exit(1);
            } catch (IOException e) {
                System.err.println("There was a problem reading in the config file.");
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        } else {

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

            runner = new Runner(Arrays.asList(path.split(",")), logPath, ignores, typeFilters, maxDepth, analysers);
        }
        System.out.println("Done!");
        runner = null;
    }

    /**
     * Given a number of analysers, wraps each in a callable, and runs it in a new thread.
     *
     * @param tas the analysers
     */
    void runAnalysersInParallel(List<TreeAnalyser> tas) {
        // Run the analyses as threads
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(tas.size());
            List<Future<Void>> futures =
                    executorService.invokeAll(tas.stream().map(TreeAnalyserCallable::new).collect(Collectors.toList()));

            // Wait for them to finish
            futures.forEach(f -> {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted running one of the threads");
                } catch (ExecutionException e) {
                    System.err.println("Execution problem with one of the threads");
                }
            });
        } catch (InterruptedException e) {
            System.err.println("Threading issue - interrupted exception");
        }
    }

    /**
     * Given a number of threads containing analysers, generate reports as PDF and return these as a list.
     *
     * @param analysers the analysers
     * @return the pdf reports
     */
    List<ByteArrayOutputStream> getPdfStreamsFromAnalysers(List<TreeAnalyser> analysers) {
        List<ByteArrayOutputStream> pdfStreams = new ArrayList<>();
        analysers.forEach(analyser -> {
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
     *
     * @param logPath    The path to write the resulting PDF report to
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
     * Given a list of strings representing java classes, uses java reflection to load the specified classes, and
     * instantiates these as tree analysers. Adds these to a list, which is then returned.
     *
     * @param paths      the file path to pass to tree analysers
     * @param analysers the analyser package names
     * @param roots      the tree to pass to analysers
     * @return the instantiated tree analysers
     */
    List<TreeAnalyser> resolveAnalysers(List<String> paths, List<String> analysers, List<FileTreeNode> roots) {
        List<TreeAnalyser> tas = new ArrayList<>();
        Class<?>[] expectedConstructorParams = new Class<?>[]{List.class, List.class};
        analysers.forEach(s -> {
            try {
                Class<?> clazz = Class.forName(s);
                Arrays.stream(clazz.getConstructors()).forEach(constructor -> {
                    if (constructor.getParameterCount() == 2 &&
                            Arrays.equals(expectedConstructorParams, constructor.getParameterTypes())) {
                        try {
                            tas.add((TreeAnalyser) constructor.newInstance(roots, paths));
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
}
