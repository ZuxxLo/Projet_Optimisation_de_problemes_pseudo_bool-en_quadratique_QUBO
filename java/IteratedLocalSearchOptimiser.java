import java.util.ArrayList;
import java.util.List;

class IteratedLocalSearchOptimiser extends Optimiser {
    public IteratedLocalSearchOptimiser(QUBOEval eval, long seed) {
        super(eval, seed, "ils");
    }

    private List<Boolean> localSearch(List<Boolean> x, double[] deltas) {
        List<Boolean> current = new ArrayList<>(x);
        double currentFitness = eval.evaluate(current);
        boolean improved;
        do {
            improved = false;
            for (int u = 0; u < n; u++) {
                if (deltas[u] < 0) {
                    current.set(u, !current.get(u));
                    currentFitness += deltas[u];
                    updateDeltas(deltas, current, u);
                    improved = true;
                    break;
                }
            }
        } while (improved);
        return current;
    }

    private void updateDeltas(double[] deltas, List<Boolean> x, int j) {
        boolean xjOld = x.get(j);
        x.set(j, !xjOld);
        for (int u : eval.QnonZero.get(j)) {
            if (u < j) {
                deltas[u] += 4 * eval.Q.get(u).get(j) * (x.get(u) ? -1 : 1) * (xjOld ? 1 : -1);
            } else if (u > j) {
                deltas[u] += 4 * eval.Q.get(j).get(u) * (x.get(u) ? -1 : 1) * (xjOld ? 1 : -1);
            }
        }
        deltas[j] = 2 * deltas[j];
        x.set(j, xjOld);
    }

    private List<Boolean> perturb(List<Boolean> x) {
        List<Boolean> perturbed = new ArrayList<>(x);
        int flips = rng.nextInt(n / 10) + 1;
        for (int i = 0; i < flips; i++) {
            int pos = rng.nextInt(n);
            perturbed.set(pos, !perturbed.get(pos));
        }
        return perturbed;
    }

    private double[] computeAllDeltas(List<Boolean> x) {
        double[] deltas = new double[n];
        for (int u = 0; u < n; u++) {
            deltas[u] = eval.computeDeltaFitness(x, u);
        }
        return deltas;
    }

    @Override
    public Result optimize() {
        List<Boolean> bestSolution = new ArrayList<>(n);
        double bestFitness = Double.POSITIVE_INFINITY;
        List<Boolean> current = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            current.add(rng.nextBoolean());
            bestSolution.add(false);
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            double[] deltas = computeAllDeltas(current);
            List<Boolean> localOptimum = localSearch(current, deltas);
            double fitness = eval.evaluate(localOptimum);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = new ArrayList<>(localOptimum);
            }
            current = perturb(localOptimum);
        }
        return new Result(bestSolution, bestFitness);
    }
}