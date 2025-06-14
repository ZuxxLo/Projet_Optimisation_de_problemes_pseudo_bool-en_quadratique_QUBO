import java.util.*;
import java.io.*;

public class Main {
    private static QUBOEval loadInstance(String folder, int instanceId) throws Exception {
        String filename = folder + "/puboi_" + instanceId + ".json";
        return new QUBOEval(filename);
    }

    public static void main(String[] args) throws Exception {
        String folder = "C:\\Users\\docteur\\Desktop\\master1\\s2\\opt\\projet-2025-m-1-optimisation-nassim\\instances";

        // Calibrate once
        if (Calibration.TIME_LIMIT_MILLIS == -1) {
            Calibration.calibrate(folder);
        }

        // Test all algorithms
        Class<?>[] optimizerClasses = {
                //  RandomSearchOptimiser.class,
                //ILS.class,
                //TabuSearchOptimiser.class
                //GeneticAlgorithm.class
               // SimulatedAnnealingOptimiser.class
                GradientDescentOptimiser.class
                //HybridSAGDOptimiser.class
        };

        for (int id = 990; id <= 999; id++) {
            QUBOEval eval = loadInstance(folder, id);
            System.out.println("Testing instance " + id);

            for (Class<?> optimizerClass : optimizerClasses) {
                Optimiser optimiser;
                if (optimizerClass == RandomSearchOptimiser.class) {
                    optimiser = new RandomSearchOptimiser(eval, 42);
                } else if (optimizerClass == IteratedLocalSearchOptimiser.class) {
                    optimiser = new ILS(eval, 42, 5);
                }
                 else if  ( optimizerClass == TabuSearchOptimiser.class){ // TabuSearchOptimiser
                    optimiser = new TabuSearchOptimiser(eval, 42,10);
                }
                 else if (optimizerClass== GeneticAlgorithm.class)
                 {
                     optimiser = new GeneticAlgorithm(eval, 42, 50, 0.01, 3);
                }
                 else if (optimizerClass== SimulatedAnnealingOptimiser.class){
                     optimiser = new SimulatedAnnealingOptimiser(eval, 42);
                } else if(optimizerClass == GradientDescentOptimiser.class) {
                     optimiser = new GradientDescentOptimiser(eval, 42 );
                } else {
                     optimiser = new HybridSAGDOptimiser(eval,42);
                }


                double totalFitness = 0;
                double bestFitnessRun = Double.POSITIVE_INFINITY;
                int runs = 30;

                for (int run = 0; run < runs; run++) {
                    Optimiser.Result result = optimiser.optimize();
                    totalFitness += result.bestFitness;
                    System.out.println("id: " + id + "| bestFitness: " + result.bestFitness + "| run: " + run);
                    optimiser.writeToCSV(id, result.bestFitness, run);
                    if (result.bestFitness < bestFitnessRun) {
                        bestFitnessRun = result.bestFitness;
                    }
                }

                double avgFitness = totalFitness / runs;
                System.out.printf("%s - Instance %d: Avg Fitness = %.2f, Best Fitness = %.2f, Best Known = %.2f%n",
                        optimiser.getClass().getSimpleName(), id, avgFitness, bestFitnessRun, Calibration.BEST_KNOWN[id - 990]);
            }
        }
    }
}