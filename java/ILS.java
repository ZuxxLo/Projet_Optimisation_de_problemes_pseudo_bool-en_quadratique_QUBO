import java.util.ArrayList;
import java.util.List;

public class ILS extends Optimiser {
    private int perturbationStrength;

    public ILS(QUBOEval eval, long seed, int perturbationStrength) {
        super(eval, seed, "ILS");
        this.perturbationStrength = perturbationStrength;
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

        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            while (true) {
                int u = helper.getBestFlip();
                if (u == -1 || helper.getDelta(u) >= 0) {
                    break;
                }
                helper.flip(u);
                if (helper.getFitness() < bestFitness) {
                    bestFitness = helper.getFitness();
                    bestSolution = helper.getSolution();
                }
            }

            List<Integer> toFlip = new ArrayList<>();
            while (toFlip.size() < perturbationStrength) {
                int u = rng.nextInt(n);
                if (!toFlip.contains(u)) {
                    toFlip.add(u);
                }
            }
            for (int u : toFlip) {
                helper.flip(u);
            }

            if (helper.getFitness() < bestFitness) {
                bestFitness = helper.getFitness();
                bestSolution = helper.getSolution();
            }
        }

        return new Result(bestSolution, bestFitness);
    }
}