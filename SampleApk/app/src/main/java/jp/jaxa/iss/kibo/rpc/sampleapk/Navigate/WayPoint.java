package jp.jaxa.iss.kibo.rpc.sampleapk.Navigate;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

public class WayPoint {
    public Quaternion quaternion;
    public Point point;

    public WayPoint(Quaternion quaternion, Point point){
        this.quaternion = quaternion;
        this.point = point;
    }
}