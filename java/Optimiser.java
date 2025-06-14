import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

abstract class Optimiser {
    protected QUBOEval eval;
    protected int n;
    protected Random rng;
    protected String csvFileName;

    public Optimiser(QUBOEval eval, long seed, String algorithmName) {
        this.eval = eval;
        this.n = eval.n;
        this.rng = new Random(seed);
        this.csvFileName = algorithmName + "_results.csv";
    }

    public abstract Result optimize();

    public static class Result {
        public List<Boolean> bestSolution;
        public double bestFitness;

        public Result(List<Boolean> bestSolution, double bestFitness) {
            this.bestSolution = new ArrayList<>(bestSolution);
            this.bestFitness = bestFitness;
        }

        @Override
        public String toString() {
            return "Best Fitness: " + bestFitness + ", Solution: " + bestSolution;
        }
    }

    // Method to write results to CSV (to be called by subclasses)
    protected void writeToCSV(int instanceId, double fitness, int run) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(csvFileName, true))) {
            if (run == 0 && instanceId == 990) { // Write header only once
                writer.println("InstanceID,Run,Fitness,BestKnown");
            }
            writer.printf(Locale.US,"%d,%d,%.2f,%.2f%n", instanceId, run, fitness, Calibration.BEST_KNOWN[instanceId - 990]);
        } catch (java.io.IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }
}