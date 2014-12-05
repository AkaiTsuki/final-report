import dkmeans.kmeans.KMeansController;
import dkmeans.kmeans.LocalKMeansController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void calculateTimeDiff(String t1, String t2) throws ParseException {
        String format = "yyy-MM-dd hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date d1 = sdf.parse(t1);
        Date d2 = sdf.parse(t2);

        long diff = d2.getTime() - d1.getTime();

        int diffSecs = (int) diff / 1000;
        int diffHours = diffSecs / 3600;
        diffSecs = (int) diffSecs % 3600;
        int diffMin = diffSecs / 60;
        diffSecs = (int) diffSecs % 60;

        System.out.format("Diff hours: %d, Diff min: %d, Diff secs: %d", diffHours, diffMin, diffSecs);

    }


    public static void main(String[] args) throws Exception {
//        KMeansController controller = new KMeansController();
//        controller.train(args);

//        calculateTimeDiff("2014-11-30 22:56:23","2014-11-30 23:31:01");
        LocalKMeansController c = new LocalKMeansController();
        c.run(args);
    }
}
