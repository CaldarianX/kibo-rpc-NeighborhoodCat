package jp.jaxa.iss.kibo.rpc.sampleapk.Utils;
import gov.nasa.arc.astrobee.types.Point;
import java.lang.Math;

public class PointUtils {
    public static double distance(Point p1,Point p2){
        double sum_of_two_power = 0;

        sum_of_two_power += Math.pow((p1.getX()-p2.getX()),2);
        sum_of_two_power += Math.pow((p1.getY()-p2.getY()),2);
        sum_of_two_power += Math.pow((p1.getZ()-p2.getZ()),2);

        return Math.sqrt(sum_of_two_power);
    }
}
