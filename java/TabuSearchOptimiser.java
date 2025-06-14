import java.util.ArrayList;
import java.util.List;

public class TabuSearchOptimiser extends Optimiser {
    private int tabuTenure;

    public TabuSearchOptimiser(QUBOEval eval, long seed, int tabuTenure) {
        super(eval, seed, "TabuSearch");
        this.tabuTenure = tabuTenure;
    }

    @Override
    public Result optimize() {
        long startTime = System.currentTimeMillis();

        List<Boolean> x = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            x.add(rng.nextBoolean());
        }
        LocalSearchHelper helper = new LocalSearchHelper(eval, x);

        double bestFitness = helper.getFitness();
        List<Boolean> bestSolution = helper.getSolution();
        int[] tabuUntil = new int[n];
        int iteration = 0;

        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            iteration++;

            int bestU = -1;
            double bestDelta = Double.POSITIVE_INFINITY;
            for (int u = 0; u < n; u++) {
                double deltaU = helper.getDelta(u);
                if ((iteration > tabuUntil[u] || helper.getFitness() + deltaU < bestFitness) && deltaU < bestDelta) {
                    bestDelta = deltaU;
                    bestU = u;
                }
            }

            if (bestU == -1) {
                break;
            }

            helper.flip(bestU);
            tabuUntil[bestU] = iteration + tabuTenure;

            if (helper.getFitness() < bestFitness) {
                bestFitness = helper.getFitness();
                bestSolution = helper.getSolution();
            }
        }

        return new Result(bestSolution, bestFitness);
    }
}