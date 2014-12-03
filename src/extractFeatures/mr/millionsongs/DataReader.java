package mr.millionsongs;

import ncsa.hdf.object.h5.H5File;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by jiachiliu on 11/14/14.
 */
public class DataReader {

    public static final String PATH = "dataset/MillionSongSubset/data/";

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

    public static Map<String, List<String>> findAllArtisitsTerms() throws Exception {
        List<String> files = listFile(PATH);
        Map<String, List<String>> termsMap = new HashMap<String, List<String>>();
        int numOfSongs = 0;
        for (String f : files) {
            System.out.print("Current Process: " + numOfSongs + "/" + files.size() + "\r");
            H5File h5 = hdf5_getters.hdf5_open_readonly(f);
            String artist = hdf5_getters.get_artist_id(h5);
            if (!termsMap.containsKey(artist)) {
                String[] terms = null;
                try {
                    terms = hdf5_getters.get_artist_terms(h5);
                } catch (Exception e) {

                }
                if (terms != null) {
                    List<String> termList = Arrays.asList(terms);
                    termsMap.put(artist, termList);
                }
            }
            numOfSongs++;
            h5.close();
        }
        return termsMap;
    }

    public static List<List<Double>> extractFeature() throws Exception {
        List<String> files = listFile(PATH);
        List<List<Double>> matrix = new LinkedList<List<Double>>();
        int numOfSongs = 0;
        files = files.subList(6000, files.size());
        for (String f : files) {
            System.out.println("Current Process: " + numOfSongs + "/" + files.size() + " file name: "+f+" matrix size: " + matrix.size());
            numOfSongs++;
            H5File h5 = hdf5_getters.hdf5_open_readonly(f);
            List<Double> rec = new LinkedList<Double>();
            double familiarity = 0.0;
            try {
                familiarity = hdf5_getters.get_artist_familiarity(h5);
            } catch (Exception e) {
            }

            double artistHot = 0.0;
            try {
                artistHot = hdf5_getters.get_artist_hotttnesss(h5);
            } catch (Exception e) {
            }

            double numOfSimilarArtists = 0.0;
            try {
                numOfSimilarArtists = hdf5_getters.get_similar_artists(h5).length;
            } catch (Exception e) {
            }

            double numOfArtistTerms = 0.0;
            try {
                numOfArtistTerms = hdf5_getters.get_artist_terms(h5).length;
            } catch (Exception e) {
            }
            double duration = 0.0;
            try {
                duration = hdf5_getters.get_duration(h5);
            } catch (Exception e) {
            }

            double endOfFadeIn = 0.0;
            try {
                endOfFadeIn = hdf5_getters.get_end_of_fade_in(h5);
            } catch (Exception e) {
            }

            double key = 0.0;
            try {
                key = hdf5_getters.get_key(h5);
            } catch (Exception e) {
            }
            double keyConfidence = 0.0;
            try {
                keyConfidence = hdf5_getters.get_key_confidence(h5);
            } catch (Exception e) {
            }

            double loudness = 0.0;
            try {
                loudness = hdf5_getters.get_loudness(h5);
            } catch (Exception e) {
            }
            double mode = 0.0;
            try {
                mode = hdf5_getters.get_mode(h5);
            } catch (Exception e) {
            }
            double modeConfidence = 0.0;
            try {
                modeConfidence = hdf5_getters.get_mode_confidence(h5);
            } catch (Exception e) {
            }
            double startOfFadeOut = 0.0;
            try {
                startOfFadeOut = hdf5_getters.get_start_of_fade_out(h5);
            } catch (Exception e) {
            }
            double tempo = 0.0;
            try {
                tempo = hdf5_getters.get_tempo(h5);
            } catch (Exception e) {
            }
            double year = 0.0;
            try {
                year = hdf5_getters.get_year(h5);
            } catch (Exception e) {
            }
            double songHot = 0.0;
            try {
                songHot = hdf5_getters.get_song_hotttnesss(h5);
                if(Double.isNaN(songHot)) songHot = 0.0;
            } catch (Exception e) {
            }

            rec.add(familiarity);
            rec.add(artistHot);
            rec.add(numOfSimilarArtists);
            rec.add(numOfArtistTerms);
            rec.add(duration);
            rec.add(endOfFadeIn);
            rec.add(key);
            rec.add(keyConfidence);
            rec.add(loudness);
            rec.add(mode);
            rec.add(modeConfidence);
            rec.add(startOfFadeOut);
            rec.add(tempo);
            rec.add(year);
            rec.add(songHot);
            matrix.add(rec);

            h5.close();
        }
        return matrix;
    }
}
