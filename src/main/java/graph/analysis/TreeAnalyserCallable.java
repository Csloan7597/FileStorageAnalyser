package graph.analysis;

import exceptions.AnalysisException;

import java.util.concurrent.Callable;

/**
 * Created by conor on 09/09/2014.
 *
 * Simple runnable class which wraps around a tree analyser, allowing analyses to
 * be executed in parallel without necessitating this in the tree analyser type hierarchy.
 */
public class TreeAnalyserCallable implements Callable<Void> {

    private final TreeAnalyser analyser;

    /**
     *
     * @param analyser analyser to run in this thread
     */
    public TreeAnalyserCallable(TreeAnalyser analyser) {
        this.analyser = analyser;
    }

    /**
     * Get the analyser this thread is running.
     * @return the analyser
     */
    public TreeAnalyser getAnalyser() {
        return analyser;
    }

    @Override
    public Void call() {
        try {
            analyser.doAnalyse();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
        return null;
    }
}
