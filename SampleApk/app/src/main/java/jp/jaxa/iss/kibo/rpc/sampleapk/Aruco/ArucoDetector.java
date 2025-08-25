package jp.jaxa.iss.kibo.rpc.sampleapk.Aruco;

import android.content.Context;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class ArucoDetector {
    private Mat image;
    private List<Mat> markerCorners;
    private MatOfInt markerIds;

    public ArucoDetector() {

    }
    public void setImage(Mat image) {
        this.image = image;
    }
    public boolean detectAruco() {
        if (image == null) {
            System.out.println("Image is null");
            return false;
        }
        try {
            Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_1000);

            DetectorParameters parameters = DetectorParameters.create();

            markerCorners = new ArrayList<>();
            markerIds = new MatOfInt();

            Aruco.detectMarkers(image, dictionary, markerCorners, markerIds, parameters);

            if (markerIds.empty()) {
                System.out.println("Aruco Not Found");
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public Integer getAmountMarker() {
        return markerCorners.size();
    }

    public MatOfInt getMarkerIds() {
        return markerIds;
    }

    public List<Mat> getMarkerCorners() {
        return markerCorners;
    }
    public void printDebugInfo() {
        System.out.println("[ArucoDetector] Debug Info");
        System.out.println("Image is set: " + (image != null));
        System.out.println("Markers detected: " + (markerIds != null ? markerIds.rows() : 0));

        if (markerIds != null && !markerIds.empty()) {
            int[] ids = markerIds.toArray();
            System.out.print("Marker IDs: [");
            for (int i = 0; i < ids.length; i++) {
                System.out.print(ids[i]);
                if (i < ids.length - 1) System.out.print(", ");
            }
            System.out.println("]");
        }

        if (markerCorners != null) {
            System.out.println("Each marker has " + (markerCorners.size() > 0 ? markerCorners.get(0).rows() : 0) + " corner points.");
            System.out.println("Total marker corner sets: " + markerCorners.size());
        }
    }
}