import java.util.ArrayList;
import java.util.List;

public class LocalSearchHelper {
    private QUBOEval eval;
    private List<Boolean> x;
    private double f;
    private List<Double> delta;
    private int n;

    public LocalSearchHelper(QUBOEval eval, List<Boolean> initialX) {
        this.eval = eval;
        this.n = eval.n;
        this.x = new ArrayList<>(initialX);
        this.f = eval.evaluate(x);
        this.delta = new ArrayList<>(n);
        for (int u = 0; u < n; u++) {
            delta.add(eval.computeDeltaFitness(x, u));
        }
    }

    public double getFitness() {
        return f;
    }

    public List<Boolean> getSolution() {
        return new ArrayList<>(x);
    }

    public double getDelta(int u) {
        return delta.get(u);
    }

    public void flip(int u) {
        f += delta.get(u);
        double signU = x.get(u) ? -1 : 1;
        for (int v : eval.QnonZero.get(u)) {
            if (v != u) {
                double q = (u < v) ? eval.Q.get(u).get(v) : eval.Q.get(v).get(u);
                double signV = x.get(v) ? -1 : 1;
                delta.set(v, delta.get(v) + 4 * q * signU * signV);
            }
        }
        delta.set(u, -delta.get(u));
        x.set(u, !x.get(u));
    }

    public int getBestFlip() {
        int bestU = -1;
        double bestDelta = 0;
        for (int u = 0; u < n; u++) {
            if (delta.get(u) < bestDelta) {
                bestDelta = delta.get(u);
                bestU = u;
            }
        }
        return bestU;
    }
}