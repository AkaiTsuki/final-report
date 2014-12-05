package dkmeans.kmeans;

/**
 * Created by jiachiliu on 11/19/14.
 */
public class DenseVector implements KMVector {
    private double[] vals;

    public DenseVector(int N) {
        vals = new double[N];
    }


    @Override
    public Double get(int i) {
        return vals[i];
    }

    @Override
    public void set(int i, Double value) {
        vals[i] = value;
    }

    @Override
    public Double distance(KMVector vector) {
        double d = 0.0;
        for (int i = 0; i < vals.length; i++) {
            double x1 = this.get(i);
            double x2 = vector.get(i);
            d += (x1 - x2) * (x1 - x2);
        }
        return Math.sqrt(d);
    }

    @Override
    public int getDimension() {
        return vals.length;
    }

    public static KMVector fromString(String text, int N) {
        KMVector v = new SparseVector(N);
        String[] cols = text.split(",");
        for (int i = 0; i < cols.length; i++) {
            v.set(i, Double.parseDouble(cols[i]));
        }
        return v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < vals.length; i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(vals[i]);
        }

        return sb.toString();
    }
}
