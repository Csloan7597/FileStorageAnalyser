package graph.analysis;

import exceptions.AnalysisException;
import exceptions.PdfGenerationException;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

/**
 * Created by conor on 09/09/2014.
 *
 * Simple runnable class which wraps around a tree analyser, allowing analyses to
 * be executed in parallel without necessitating this in the tree analyser type hierarchy.
 */
public class TreeAnalyserCallable implements Callable<ByteArrayOutputStream> {

    private final TreeAnalyser analyser;

    /**
     *
     * @param analyser analyser to run in this thread
     */
    public TreeAnalyserCallable(TreeAnalyser analyser) {
        this.analyser = analyser;
    }

    @Override
    public ByteArrayOutputStream call() {
        try {
            analyser.doAnalyse();
            return analyser.generatePdfReport();
        } catch (AnalysisException e) {
            System.err.println("Error analysing: " + analyser.getAnalysisName() + "skipping..");
        } catch (PdfGenerationException e) {
            System.err.println("Error generating PDF for: " + analyser.getAnalysisName() + "skipping..");
        }
        return null;
    }
}
