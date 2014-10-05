package graph.analysis;

import exceptions.AnalysisException;

/**
 * Created by conor on 09/09/2014.
 */
public class TreeAnalyserRunnable implements Runnable {

    private final TreeAnalyser analyser;

    public TreeAnalyserRunnable(TreeAnalyser analyser) {
        this.analyser = analyser;
    }

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
