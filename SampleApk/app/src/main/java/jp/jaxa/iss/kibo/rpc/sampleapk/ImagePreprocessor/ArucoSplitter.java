package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoRegion;

public class ArucoSplitter {

    public static List<ArucoRegion> splitter(Mat input, List<Mat> markerCorners, MatOfInt markerIds) {
        List<ArucoRegion> regions = new ArrayList<>();

        if (markerCorners.size() == 1) {
            regions.add(new ArucoRegion(input.clone(), markerCorners.get(0), (int) markerIds.get(0, 0)[0]));
        } else if (markerCorners.size() == 2) {
            double x1 = markerCorners.get(0).get(0, 0)[0];
            double x2 = markerCorners.get(1).get(0, 0)[0];

            int center = (int)((x1 + x2) / 2);

            Rect left, right;
            Mat leftCorner, rightCorner;
            int leftId, rightId;

            if (x1 < x2) {
                left = new Rect(0, 0, center, input.rows());
                right = new Rect(center, 0, input.cols() - center, input.rows());
                leftCorner = markerCorners.get(0);
                rightCorner = markerCorners.get(1);
                leftId = (int) markerIds.get(0, 0)[0];
                rightId = (int) markerIds.get(1, 0)[0];
            } else {
                right = new Rect(0, 0, center, input.rows());
                left = new Rect(center, 0, input.cols() - center, input.rows());
                rightCorner = markerCorners.get(0);
                leftCorner = markerCorners.get(1);
                rightId = (int) markerIds.get(0, 0)[0];
                leftId = (int) markerIds.get(1, 0)[0];
            }

            regions.add(new ArucoRegion(new Mat(input, left).clone(), leftCorner, leftId));
            regions.add(new ArucoRegion(new Mat(input, right).clone(), rightCorner, rightId));
        }

        return regions;
    }

}
