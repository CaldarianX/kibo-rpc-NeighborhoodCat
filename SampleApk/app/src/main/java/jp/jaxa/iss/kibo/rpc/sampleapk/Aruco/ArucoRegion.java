package jp.jaxa.iss.kibo.rpc.sampleapk.Aruco;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;


public class ArucoRegion {
    public Mat image;
    public Point[] corners;
    public Integer id;

    public ArucoRegion(Mat image, Mat rawCorners, Integer id) {
        this.image = image;
        this.corners = extractPoints(rawCorners);
        this.id = id;
    }

    private Point[] extractPoints(Mat rawCorners) {
        Point[] pts = new Point[4];
        for (int i = 0; i < 4; i++) {
            double[] data = rawCorners.get(0, i);
            pts[i] = new Point(data);
        }
        return pts;
    }

}

