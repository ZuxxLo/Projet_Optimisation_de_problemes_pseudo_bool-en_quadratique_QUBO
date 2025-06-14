import java.util.ArrayList;
import java.util.List;


    class HybridSAGDOptimiser extends Optimiser {
    private double initialTemp = 1000.0;
    private double finalTemp = 0.1;
    private double learningRate = 0.2; // Adjusted from 0.1
    private static final double EPSILON = 1e-6;
    private static final double SA_FRACTION = 0.7;

    public HybridSAGDOptimiser(QUBOEval eval, long seed) {
        super(eval, seed, "hybrid_sa_gd3");
    }

    private List<Double> binaryToContinuous(List<Boolean> x) {
        List<Double> z = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            z.add(x.get(i) ? -1.0 : 1.0);
        }
        return z;
    }

    private LocalSearchHelper runSA(List<Boolean> initialX, long timeLimit) {
        LocalSearchHelper helper = new LocalSearchHelper(eval, initialX);
        long startTime = System.currentTimeMillis();
        long elapsed;

        while ((elapsed = System.currentTimeMillis() - startTime) < timeLimit) {
            double temp = initialTemp - (initialTemp - finalTemp) * elapsed / timeLimit;
            int u = rng.nextInt(n);
            double delta = helper.getDelta(u);
            if (delta < 0 || rng.nextDouble() < Math.exp(-delta / temp)) {
                helper.flip(u);
            }
        }
        return helper;
    }

    private List<Double> runGD(List<Double> z, long timeLimit) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeLimit) {
            for (int u = 0; u < n; u++) {
                double grad = computeGradient(z, u);
                double newZ = z.get(u) - learningRate * grad;
                z.set(u, newZ);
            }
            projectToBounds(z);

            double currentFitness = eval.fnumerical(z);
            double binaryFitness = eval.evaluate(projectToBinary(z));
            if (currentFitness > binaryFitness + EPSILON) {
                learningRate = Math.max(0.01, learningRate * 0.9);
            } else if (currentFitness < binaryFitness - EPSILON) {
                learningRate = Math.min(0.5, learningRate * 1.1);
            }
        }
        return z;
    }

    private double computeGradient(List<Double> z, int u) {
        double grad = eval.H.get(u);
        for (int i : eval.QnonZero.get(u)) {
            if (i < u) {
                grad += eval.Q.get(i).get(u) * z.get(i);
            } else if (i > u) {
                grad += eval.Q.get(u).get(i) * z.get(i);
            }
        }
        return grad;
    }

    private void projectToBounds(List<Double> z) {
        for (int i = 0; i < n; i++) {
            double zi = z.get(i);
            if (zi > 1 || zi < -1) {
                z.set(i, Math.signum(zi));
            }
        }
    }

    private List<Boolean> projectToBinary(List<Double> z) {
        List<Boolean> x = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            x.add(z.get(i) < 0);
        }
        return x;
    }

    @Override
    public Result optimize() {
        List<Boolean> initialX = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            initialX.add(rng.nextBoolean());
        }

        long saTimeLimit = (long) (Calibration.TIME_LIMIT_MILLIS * SA_FRACTION);
        LocalSearchHelper saResult = runSA(initialX, saTimeLimit);
        List<Boolean> saSolution = saResult.getSolution();
        double saFitness = saResult.getFitness();

        List<Boolean> bestSolution = new ArrayList<>(saSolution);
        double bestFitness = saFitness;

        List<Double> z = binaryToContinuous(saSolution);
        long gdTimeLimit = Calibration.TIME_LIMIT_MILLIS - saTimeLimit;
        z = runGD(z, gdTimeLimit);

        List<Boolean> gdSolution = projectToBinary(z);
        double gdFitness = eval.evaluate(gdSolution);
        if (gdFitness < bestFitness) {
            bestFitness = gdFitness;
            bestSolution = new ArrayList<>(gdSolution);
        }

        return new Result(bestSolution, bestFitness);
    }
}