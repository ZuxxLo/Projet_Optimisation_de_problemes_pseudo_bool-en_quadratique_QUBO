import java.util.ArrayList;
import java.util.List;

class SimulatedAnnealingOptimiser extends Optimiser {
    private double initialTemp;
    private double finalTemp;

    public SimulatedAnnealingOptimiser(QUBOEval eval, long seed) {
        super(eval, seed, "simulated_annealing");
        this.initialTemp = 1000.0;
        this.finalTemp = 0.1;
    }

    @Override
    public Result optimize() {
        List<Boolean> initialX = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            initialX.add(rng.nextBoolean());
        }

        LocalSearchHelper helper = new LocalSearchHelper(eval, initialX);
        List<Boolean> bestSolution = helper.getSolution();
        double bestFitness = helper.getFitness();

        long startTime = System.currentTimeMillis();
        long elapsed;

        while ((elapsed = System.currentTimeMillis() - startTime) < Calibration.TIME_LIMIT_MILLIS) {
            double temp = initialTemp - (initialTemp - finalTemp) * elapsed / Calibration.TIME_LIMIT_MILLIS;
            int u = rng.nextInt(n);
            double delta = helper.getDelta(u);

            if (delta < 0 || rng.nextDouble() < Math.exp(-delta / temp)) {
                helper.flip(u);
                double currentFitness = helper.getFitness();
                if (currentFitness < bestFitness) {
                    bestFitness = currentFitness;
                    bestSolution = helper.getSolution();
                }
            }
        }
        return new Result(bestSolution, bestFitness);
    }
}