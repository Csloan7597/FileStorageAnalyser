package graph.analysis;

/**
 * Created by conor on 09/09/2014.
 */
public class TreeAnalyserRunnable implements Runnable {

    private TreeAnalyser analyser;

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
        } catch (TreeAnalyser.AnalysisException e) {
            e.printStackTrace();
        }
    }
}
