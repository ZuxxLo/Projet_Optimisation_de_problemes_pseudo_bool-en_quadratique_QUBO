import java.util.ArrayList;
import java.util.List;

class GradientDescentOptimiser extends Optimiser {
    private double learningRate;
    private static final double EPSILON = 1e-6;

    public GradientDescentOptimiser(QUBOEval eval, long seed) {
        super(eval, seed, "gradient_descent1changeddynamicLR");
        this.learningRate = 0.2;
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
        List<Double> z = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            z.add(2 * rng.nextDouble() - 1);
        }

        List<Boolean> bestSolution = projectToBinary(z);
        double bestFitness = eval.evaluate(bestSolution);
        long startTime = System.currentTimeMillis();
        int iteration = 0;

        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            for (int u = 0; u < n; u++) {
                double grad = computeGradient(z, u);
                double newZ = z.get(u) - learningRate * grad;
                z.set(u, newZ);
            }
            projectToBounds(z);


            List<Boolean> x = projectToBinary(z);
            double fitness = eval.evaluate(x);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = new ArrayList<>(x);
            }


            double currentFitness = eval.fnumerical(z);
//            if (currentFitness > bestFitness + EPSILON) {
//                learningRate = Math.max(0.001, learningRate * 0.9);
//            } else if (currentFitness < bestFitness - EPSILON) {
//                learningRate = Math.min(0.1, learningRate * 1.1);
//            }
            if (currentFitness > bestFitness + EPSILON) {
                learningRate = Math.max(0.01, learningRate * 0.9);
            } else if (currentFitness < bestFitness - EPSILON) {
                learningRate = Math.min(0.5, learningRate * 1.1);
            }

            iteration++;
        }

        List<Boolean> finalX = projectToBinary(z);
        double finalFitness = eval.evaluate(finalX);
        if (finalFitness < bestFitness) {
            bestFitness = finalFitness;
            bestSolution = finalX;
        }

        return new Result(bestSolution, bestFitness);
    }
}