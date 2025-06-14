import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm extends Optimiser {
    private final int populationSize;
    private final double mutationRate;
    private final int tournamentSize;

    public GeneticAlgorithm(QUBOEval eval, long seed, int populationSize,
                            double mutationRate, int tournamentSize) {
        super(eval, seed, "GeneticAlgorithm");
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
    }

    @Override
    public Result optimize() {
        long startTime = System.currentTimeMillis();


        List<List<Boolean>> population = new ArrayList<>();
        List<Double> fitnesses = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            List<Boolean> individual = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
                individual.add(rng.nextBoolean());
            }
            population.add(individual);
            fitnesses.add(eval.evaluate(individual));
        }

        double bestFitness = Double.POSITIVE_INFINITY;
        List<Boolean> bestSolution = null;

        while (System.currentTimeMillis() - startTime < Calibration.TIME_LIMIT_MILLIS) {
            List<List<Boolean>> parents = new ArrayList<>();
            for (int i = 0; i < populationSize; i++) {
                parents.add(tournamentSelection(population, fitnesses, rng));
            }

            List<List<Boolean>> offspring = new ArrayList<>();
            for (int i = 0; i < populationSize; i += 2) {
                List<Boolean> parent1 = parents.get(i);
                List<Boolean> parent2 = parents.get(i + 1);
                List<Boolean> child1 = new ArrayList<>(parent1);
                List<Boolean> child2 = new ArrayList<>(parent2);
                int crossoverPoint = rng.nextInt(n);
                for (int j = crossoverPoint; j < n; j++) {
                    child1.set(j, parent2.get(j));
                    child2.set(j, parent1.get(j));
                }
                offspring.add(child1);
                offspring.add(child2);
            }

            for (List<Boolean> individual : offspring) {
                for (int j = 0; j < n; j++) {
                    if (rng.nextDouble() < mutationRate) {
                        individual.set(j, !individual.get(j));
                    }
                }
            }

            List<Double> offspringFitnesses = new ArrayList<>();
            for (List<Boolean> individual : offspring) {
                double fitness = eval.evaluate(individual);
                offspringFitnesses.add(fitness);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = new ArrayList<>(individual);
                }
            }

            for (int i = 0; i < populationSize; i++) {
                int worstIndex = getWorstIndex(fitnesses);
                population.set(worstIndex, offspring.get(i));
                fitnesses.set(worstIndex, offspringFitnesses.get(i));
            }
        }

        return new Result(bestSolution, bestFitness);
    }

    private List<Boolean> tournamentSelection(List<List<Boolean>> population,
                                              List<Double> fitnesses, Random rng) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            candidates.add(rng.nextInt(populationSize));
        }
        int bestCandidate = candidates.get(0);
        for (int candidate : candidates) {
            if (fitnesses.get(candidate) < fitnesses.get(bestCandidate)) {
                bestCandidate = candidate;
            }
        }
        return new ArrayList<>(population.get(bestCandidate));
    }

    private int getWorstIndex(List<Double> fitnesses) {
        int worstIndex = 0;
        for (int i = 1; i < fitnesses.size(); i++) {
            if (fitnesses.get(i) > fitnesses.get(worstIndex)) {
                worstIndex = i;
            }
        }
        return worstIndex;
    }
}