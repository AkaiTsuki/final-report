import mr.millionsongs.DataReader;
import mr.millionsongs.DataWriter;
import mr.millionsongs.hdf5_getters;
import ncsa.hdf.object.h5.H5File;

import java.io.File;
import java.util.*;

public class Main {

    public static List<String> listFile(String path) {
        File dir = new File(path);
        List<String> list = new LinkedList<String>();

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                List<String> subList = listFile(f.getAbsolutePath());
                list.addAll(subList);
            } else if (f.getName().endsWith(".h5")) {
                list.add(f.getAbsolutePath());
            }
        }

        return list;
    }

    public static void test() throws Exception {
        int numOfSongs = 0;
        Set<String> trackIds = new HashSet<String>();
        Set<Integer> years = new HashSet<Integer>();
        Set<String> artists = new HashSet<String>();
        Set<Integer> modes = new HashSet<Integer>();
        Set<Integer> keys = new HashSet<Integer>();
        Map<String, Integer> termFreq = new HashMap<String, Integer>();

        List<String> files = listFile("dataset/MillionSongSubset/data/");
        for (String f : files) {
            H5File h5 = hdf5_getters.hdf5_open_readonly(f);
            int nSongs = hdf5_getters.get_num_songs(h5);
            numOfSongs += nSongs;
            trackIds.add(hdf5_getters.get_track_id(h5));
            years.add(hdf5_getters.get_year(h5));
            artists.add(hdf5_getters.get_artist_id(h5));
            modes.add(hdf5_getters.get_mode(h5));
            keys.add(hdf5_getters.get_key(h5));
            h5.close();
            try {
                String[] terms = hdf5_getters.get_artist_terms(h5);
                for (String s : terms) {
                    if (termFreq.containsKey(s)) {
                        termFreq.put(s, termFreq.get(s) + 1);
                    } else {
                        termFreq.put(s, 1);
                    }
                }
            } catch (Exception e) {

            }

            System.out.print("Current Process: " + numOfSongs + "/" + files.size() + "\r");
//            System.out.println(Arrays.toString(hdf5_getters.get_artist_terms(h5)));
        }

        System.out.println("Number of Songs: " + numOfSongs);
        System.out.println("Number of Tracks: " + trackIds.size());
        System.out.println("Number of Years: " + years.size());
        System.out.println("Number of Artists: " + artists.size());
        System.out.println("Number of Modes: " + modes.size());
        System.out.println("Number of Keys: " + keys.size());
        System.out.println("Number of Aritist Terms: " + termFreq.size());
        System.out.println("Detail of Aritist Terms: " + termFreq);
        System.out.println(years);
    }

    public static void main(String[] args) throws Exception {
        DataWriter.writeFeatureMatrix(DataReader.extractFeature(), "matrix.txt");
    }
}
