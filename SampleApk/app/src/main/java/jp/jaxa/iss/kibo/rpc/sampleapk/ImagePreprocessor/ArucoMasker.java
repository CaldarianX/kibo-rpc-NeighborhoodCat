package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;

public class ArucoMasker {
    public static Mat maskAruco(Mat rotatedImg){
        ArucoDetector arucoDetector = new ArucoDetector();
        arucoDetector.setImage(rotatedImg);
        arucoDetector.detectAruco();
        List<Mat> corners = arucoDetector.getMarkerCorners();
        Integer amount = arucoDetector.getAmountMarker();

        if(amount == 0){
            return null;
        }

        double centerX = 0, centerY = 0;

        Mat cornerMat = corners.get(0);
        for (int i = 0; i < cornerMat.cols(); i++) {
            double[] pt = cornerMat.get(0, i);

            System.out.println("Point " + i + ": x = " + pt[0] + ", y = " + pt[1]);
            centerX += pt[0];
            centerY += pt[1];
        }
        centerX /= 4.0;
        centerY /= 4.0;



        Point[] paddedPoints = new Point[4];


        for (int i = 0; i < cornerMat.cols(); i++) {
            double[] pt = cornerMat.get(0, i);

            double vx = pt[0] - centerX;
            double vy = pt[1] - centerY;
            double norm = Math.sqrt(vx * vx + vy * vy);

            double ux = (norm != 0) ? (vx / norm) : 0;
            double uy = (norm != 0) ? (vy / norm) : 0;

            int PADDING = 3;
            double px = pt[0] + ux * PADDING;
            double py = pt[1] + uy * PADDING;

            paddedPoints[i] = new Point(px, py);
        }

        MatOfPoint poly = new MatOfPoint(paddedPoints);
        List<MatOfPoint> polyList = new ArrayList<>();
        polyList.add(poly);
        Mat masked_img = rotatedImg.clone();
        Scalar meanColor = Core.mean(rotatedImg);
        Imgproc.fillPoly(masked_img, polyList, meanColor);

        return masked_img;
    }
}
