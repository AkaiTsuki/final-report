package dkmeans.kmeans;

/**
 * Created by jiachiliu on 11/17/14.
 */
public interface KMVector {
    /**
     * Get the value at column i
     *
     * @param i column index
     * @return the value of that column
     */
    Double get(int i);

    /**
     * Set the given value to column
     *
     * @param i     column index
     * @param value value to set
     */
    void set(int i, Double value);

    /**
     * Caculate the distance between two vector
     *
     * @param vector a KMVector
     * @return the distance between the two vectors
     */
    Double distance(KMVector vector);

    /**
     * @return dimension of the vector
     */
    int getDimension();
}
