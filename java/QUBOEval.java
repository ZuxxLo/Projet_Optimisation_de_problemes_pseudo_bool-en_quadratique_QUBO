/*
  QUBOEval.java

 Author:
  Sebastien Verel,
  Univ. du Littoral Côte d'Opale, France.

  Date: 2025/03/11


    To compile using org.json package:
    javac -cp .:json-20250107.jar *.java

    To execute:
    java -cp .:json-20250107.jar Main ../cpp/puboi_1000.json
*/

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
    QUBO function:
      x binary string: for all i, x_i = 0 or 1
          f(x) = beta +
                 \sum_{i=0}^{n-1} q_i (-1)^x_i +
                 \sum_{i=0}^{n-2} \sum_{j = i + 1}^{n-1} q_{ij} (-1)^x_i (-1)^x_j
 */
public class QUBOEval {
    // Problem dimension
    public int n;

    // Constant (order 0 term)
    public double beta;

    // Linear terms
    public List<Double> H;

    // Quadratic terms
    public List<List<Double>> Q;

    // Useful: index of non-zero terms in Q: Q[i][ QnonZero[i][k] ] != 0
    List<List<Integer>> QnonZero;

    public QUBOEval(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            readJson(scanner);
        } catch (FileNotFoundException e) {
            System.err.println("Impossible to open file " + filename);
        }
    }
    public double computeDeltaFitness(List<Boolean> x, int u) {
        double sum = H.get(u);
        for (int i : QnonZero.get(u)) {
            if (i < u) {
                sum += Q.get(i).get(u) * (x.get(i) ? -1 : 1);
            } else if (i > u) {
                sum += Q.get(u).get(i) * (x.get(i) ? -1 : 1);
            }
        }
        return -2 * (x.get(u) ? -1 : 1) * sum;
    }
    public double evaluate(List<Boolean> x) {
        double res = beta;

        for (int i = 0; i < n; i++) {
            if (x.get(i))
                res -= H.get(i);
            else
                res += H.get(i);
        }

        for (int i = 0; i < n - 1; i++) {
            if (x.get(i)) {
                for (int j : QnonZero.get(i)) {
                    if (i < j) {
                        if (x.get(j))
                            res += Q.get(i).get(j);
                        else
                            res -= Q.get(i).get(j);
                    }
                }
            } else {
                for (int j : QnonZero.get(i)) {
                    if (i < j) {
                        if (x.get(j))
                            res -= Q.get(i).get(j);
                        else
                            res += Q.get(i).get(j);
                    }
                }
            }
        }

        return res;
    }

    public double fnumerical(List<Double> z) {
        double res = beta;

        for (int i = 0; i < n; i++)
            res += H.get(i) * z.get(i);

        for (int i = 0; i < n - 1; i++)
            for (int j : QnonZero.get(i))
                if (i < j)
                    res += Q.get(i).get(j) * z.get(i) * z.get(j);

        return res;
    }

    public void print(int format) {
        printOn(System.out, format);
    }

    private void readJson(Scanner scanner) {
        StringBuilder jsonContent = new StringBuilder();
        while (scanner.hasNextLine()) {
            jsonContent.append(scanner.nextLine());
        }

        JSONObject jsonObject = new JSONObject(jsonContent.toString());
        JSONObject problem = jsonObject.getJSONObject("problem");
        n = problem.getInt("n");
        JSONArray terms = problem.getJSONArray("terms");

        beta = 0;
        H = new ArrayList<>(n);
        Q = new ArrayList<>(n);
        QnonZero = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            H.add(0.0);
            Q.add(new ArrayList<>());
            QnonZero.add(new ArrayList<>());
            for (int j = 0; j < n; j++) {
                Q.get(i).add(0.0);
            }
        }

        for (int ind = 0; ind < terms.length(); ++ind) {
            JSONObject term = terms.getJSONObject(ind);
            JSONArray ids = term.getJSONArray("ids");
            double wk = term.getDouble("w");
            List<Integer> idList = new ArrayList<>();

            for (int j = 0; j < ids.length(); ++j) {
                idList.add(ids.getInt(j));
            }

            if (idList.size() > 2) {
                System.err.println("Error: degree of Walsh/QUBO expansion is not quadratic.");
                System.exit(1);
            } else {
                if (idList.size() == 0) {
                    beta = wk;
                } else if (idList.size() == 1) {
                    H.set(idList.get(0), wk);
                } else {
                    Q.get(idList.get(0)).set(idList.get(1), wk);
                    QnonZero.get(idList.get(0)).add(idList.get(1));
                    QnonZero.get(idList.get(1)).add(idList.get(0));
                }
            }
        }
    }

    private void printOn(PrintStream os, int format) {
        if (format == 0) {
            printOnJson(os);
        } else {
            printOnTxt(os);
        }
    }

    private void printOnTxt(PrintStream os) {
        os.println(n);

        if (beta != 0)
            os.println(beta);

        for (int i = 0; i < n; i++) {
            if (H.get(i) != 0)
                os.println(i + " " + H.get(i));
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Q.get(i).get(j) != 0)
                    os.println(i + " " + j + " " + Q.get(i).get(j));
            }
        }
    }

    private void printOnJson(PrintStream os) {
        os.print("{\"problem\":{");
        os.print("\"n\":" + n + ",");
        os.print("\"terms\":[");

        boolean firstTerm = true;

        if (beta != 0) {
            os.print("{\"w\":" + beta + ",");
            os.print("\"ids\":[]}");
            firstTerm = false;
        }

        for (int i = 0; i < n; i++) {
            if (H.get(i) != 0) {
                if (!firstTerm)
                    os.print(",");
                else
                    firstTerm = false;

                os.print("{\"w\":" + H.get(i) + ",");
                os.print("\"ids\":[" + i + "]}");
            }
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (Q.get(i).get(j) != 0) {
                    if (!firstTerm)
                        os.print(",");
                    else
                        firstTerm = false;

                    os.print("{\"w\":" + Q.get(i).get(j) + ",");
                    os.print("\"ids\":[" + i + "," + j + "]}");
                }
            }
        }

        os.print("]}");
    }
}