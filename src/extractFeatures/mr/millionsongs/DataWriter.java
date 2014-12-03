package mr.millionsongs;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by jiachiliu on 11/14/14.
 */
public class DataWriter {
    public static void writeArtistTerms(Map<String, List<String>> map, String destPath) throws Exception {
        PrintWriter writer = new PrintWriter(destPath, "UTF-8");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String artist = entry.getKey();
            List<String> terms = entry.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(artist).append(" ").append(terms);
            writer.println(sb.toString());
        }
    }

    public static void writeFeatureMatrix(List<List<Double>> matrix, String destPath) throws Exception {
        PrintWriter writer = new PrintWriter(destPath, "UTF-8");
        for (List<Double> row : matrix) {
            StringBuilder sb = new StringBuilder();
            for (Double d : row) {
                if (sb.length() > 0) sb.append(",");
                sb.append(d.toString());
            }
            writer.println(sb.toString());
        }
        writer.close();
    }
}
