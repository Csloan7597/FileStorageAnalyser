package graph.analysis;

import exceptions.AnalysisException;

/**
 * Created by conor on 09/09/2014.
 *
 * Simple runnable class which wraps around a tree analyser, allowing analyses to
 * be executed in parallel without necessitating this in the tree analyser type hierarchy.
 */
public class TreeAnalyserRunnable implements Runnable {

    private final TreeAnalyser analyser;

    /**
     *
     * @param analyser analyser to run in this thread
     */
    public TreeAnalyserRunnable(TreeAnalyser analyser) {
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
    public void run() {
        try {
            analyser.doAnalyse();
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
    }
}
