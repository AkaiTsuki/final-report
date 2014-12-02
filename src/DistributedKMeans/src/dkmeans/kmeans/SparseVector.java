package dkmeans.kmeans;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiachiliu on 11/18/14.
 */
public class SparseVector implements KMVector {

    private static int N;
    private Map<Integer, Double> sv;

    public SparseVector(int dimension) {
        N = dimension;
        sv = new HashMap<Integer, Double>();
    }

    @Override
    public Double get(int i) {
        if (sv.containsKey(i)) {
            return sv.get(i);
        }
        return 0.0;
    }

    @Override
    public void set(int i, Double value) {
        sv.put(i, value);
    }

    @Override
    public Double distance(KMVector vector) {
        double d = 0.0;
        for (int i = 0; i < N; i++) {
            double x1 = this.get(i);
            double x2 = vector.get(i);
            d += (x1 - x2) * (x1 - x2);
        }
        return Math.sqrt(d);
    }

    @Override
    public int getDimension() {
        return N;
    }

    public static KMVector fromString(String text, int N) {
        KMVector v = new SparseVector(N);
        String[] cols = text.split(",");
        for (int i = 0; i < cols.length; i++) {
            String[] col = cols[i].split(":");
            v.set(Integer.parseInt(col[0]), Double.parseDouble(col[1]));
        }
        return v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, Double> entry : sv.entrySet()) {
            Integer i = entry.getKey();
            Double v = entry.getValue();
            if (sb.length() > 0) sb.append(",");
            sb.append(i).append(":").append(v);
        }

        return sb.toString();
    }
}
