package dkmeans.kmeans;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jiachiliu on 11/29/14.
 */
public class LocalKMeans {

    private List<KMVector> centroids;
    private int N;

    public LocalKMeans(int N) {
        centroids = new ArrayList<KMVector>();
        this.N = N;
    }

    public void fit(List<KMVector> train, int k) {
        setupCentroids(train, k);
        do {
            List<List<KMVector>> clusters = new ArrayList<List<KMVector>>();

            for (int i = 0; i < k; i++) {
                clusters.add(new LinkedList<KMVector>());
            }

            for (KMVector v : train) {
                double minDist = Double.MAX_VALUE;
                int cluster = -1;
                for (int i = 0; i < centroids.size(); i++) {
                    KMVector c = centroids.get(i);
                    double d = v.distance(c);
                    if (d < minDist) {
                        minDist = d;
                        cluster = i;
                    }
                }
                clusters.get(cluster).add(v);
            }
            List<KMVector> newCentroids = calculateNewCentroids(clusters);
            if (converge(newCentroids)) {
                centroids = newCentroids;
                break;
            } else {
                centroids = newCentroids;
            }
        } while (true);
    }

    private boolean converge(List<KMVector> newCentroids) {
        double sum = 0.0;
        for (int i = 0; i < newCentroids.size(); i++) {
            KMVector c1 = centroids.get(i);
            KMVector c2 = newCentroids.get(i);
            sum += c1.distance(c2);
        }

        return sum < 10;
    }

    private List<KMVector> calculateNewCentroids(List<List<KMVector>> clusters) {
        List<KMVector> centroids = new ArrayList<KMVector>();

        for (List<KMVector> cluster : clusters) {
            KMVector centroid = new DenseVector(N);
            int N = cluster.get(0).getDimension();
            for (KMVector v : cluster) {
                for (int j = 0; j < N; j++) {
                    double sum = centroid.get(j) + v.get(j);
                    centroid.set(j, sum);
                }
            }
            for (int i = 0; i < N; i++) {
                centroid.set(i, (centroid.get(i) / cluster.size()));
            }
            centroids.add(centroid);
        }
        return centroids;
    }

    private void setupCentroids(List<KMVector> train, int k) {
        for (int i = 0; i < k; i++) {
            KMVector c = new DenseVector(train.get(0).getDimension());
            for (int d = 0; d < train.get(0).getDimension(); d++) {
                c.set(d, train.get(i).get(d));
            }
            centroids.add(c);
        }
    }

    public List<KMVector> getCentroids() {
        return centroids;
    }
}
