package dkmeans.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiachiliu on 11/16/14.
 * Replace all hashed userid and songid with numeric values, which represent the row and column id.
 */
public class MatrixFileIdConverter {

    private Map<String, Integer> userMap = new HashMap<String, Integer>();
    private Map<String, Integer> songMap = new HashMap<String, Integer>();

    public void createFile(String src, String userFile, String songFile, String dest) throws Exception {
        initUserMap(userFile);
        initSongMap(songFile);
        System.out.format("userMap size: %d\n", userMap.size());
        System.out.format("songMap size: %d\n", songMap.size());

        createMatrixFile(src, dest);
    }

    private void createMatrixFile(String src, String dest) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(src));
        PrintWriter writer = new PrintWriter(dest, "UTF-8");
        String line = null;
        int total = 48373586;
        int current = 0;
        while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) {
                String[] tokens = line.split("\t");
                Integer userId = userMap.get(tokens[0].trim());
                Integer songId = songMap.get(tokens[1].trim());
                Integer count = Integer.parseInt(tokens[2]);
                StringBuilder sb = new StringBuilder();
                sb.append(userId).append("\t").append(songId).append("\t").append(count);
                writer.println(sb.toString());
                current++;
                if(current % 10000 == 1)
                    System.out.print("Progress: " + current + "/" + total + "\r");
            }
        }
        System.out.println();
        br.close();
        writer.close();
    }

    private void initUserMap(String path) throws Exception {
        initMap(path, userMap);
    }

    private void initSongMap(String path) throws Exception {
        initMap(path, songMap);
    }

    private void initMap(String path, Map<String, Integer> map) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));

        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) {
                String[] tokens = line.split("\t");
                map.put(tokens[0], Integer.parseInt(tokens[1]));
            }
        }

        br.close();
    }

    public static void main(String[] args) throws Exception {
        MatrixFileIdConverter g = new MatrixFileIdConverter();
        String src = "/Users/jiachiliu/IdeaProjects/Hadoop/data/train_triplets.txt";
        String users = "/Users/jiachiliu/IdeaProjects/Hadoop/data/users.txt";
        String songs = "/Users/jiachiliu/IdeaProjects/Hadoop/data/songs.txt";
        String dest = "/Users/jiachiliu/IdeaProjects/Hadoop/data/matrix.txt";
        g.createFile(src, users, songs, dest);
    }
}
