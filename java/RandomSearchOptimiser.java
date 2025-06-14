import java.util.List;
import java.util.ArrayList;

class RandomSearchOptimiser extends Optimiser {
    public RandomSearchOptimiser(QUBOEval eval, long seed) {
        super(eval, seed, "random_search");
    }

    @Override
    public Result optimize() {
        List<Boolean> currentSolution = new ArrayList<>(n);
        List<Boolean> bestSolution = new ArrayList<>(n);
        double bestFitness = Double.POSITIVE_INFINITY;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < n; i++) {
            currentSolution.add(false);
            bestSolution.add(false);
        }

        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            for (int i = 0; i < n; i++) {
                currentSolution.set(i, rng.nextBoolean());
            }
            double fitness = eval.evaluate(currentSolution);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = new ArrayList<>(currentSolution);
            }
        }
        return new Result(bestSolution, bestFitness);
    }
}