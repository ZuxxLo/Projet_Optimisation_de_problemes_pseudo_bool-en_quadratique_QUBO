import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Calibration {
    public static long TIME_LIMIT_MILLIS = 36790;

    public static final double[] BEST_KNOWN = {
            -70459, -102182, -112475, -96038, -41790,
            -72707, -115145, -187096, -87290, -101785
    };

    public static void calibrate(String folder) throws Exception {
        QUBOEval dummyEval = loadInstance(folder, 1000);
        long referenceTime = 1000;
        List<Boolean> solution = new ArrayList<>(dummyEval.n);
        for (int j = 0; j < dummyEval.n; j++)
            solution.add(false);
        Random rng = new Random(42);
        long calibrationStart = System.currentTimeMillis();
        for (int i = 0; i < 120000; i++) {
            for (int j = 0; j < solution.size(); j++) {
                solution.set(j, rng.nextBoolean());
            }
             dummyEval.evaluate(solution);
        }
        long calibrationEnd = System.currentTimeMillis();
        long calibrationTime = calibrationEnd - calibrationStart;
        double ratio = (double) calibrationTime / referenceTime;
        TIME_LIMIT_MILLIS = (long) (30 * 1000 * ratio);

        System.out.println("Calibration time: " + calibrationTime + " ms, Ratio: " + ratio);
        System.out.println("Adjusted time limit: " + TIME_LIMIT_MILLIS + " ms");
    }

    private static QUBOEval loadInstance(String folder, int instanceId) throws Exception {
        String filename = folder + "/puboi_" + instanceId + ".json";
        return new QUBOEval(filename);
    }



}