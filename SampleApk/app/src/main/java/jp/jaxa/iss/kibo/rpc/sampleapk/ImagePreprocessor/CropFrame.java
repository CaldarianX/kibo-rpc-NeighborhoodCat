package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.Arrays;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;

public class CropFrame {
    public static Rect getTargetArea(Mat rotatedImg) {
        ArucoDetector arucoDetector = new ArucoDetector();
        arucoDetector.setImage(rotatedImg);
        arucoDetector.detectAruco();
        List<Mat> corners = arucoDetector.getMarkerCorners();
        Integer amount = arucoDetector.getAmountMarker();

        if(amount == 0){
            return null;
        }
        Mat cornerMat = corners.get(0);

        Point[] pts = new Point[4];
        for (int i = 0; i < 4; i++) {
            double[] data = cornerMat.get(0, i);
            if (data == null || data.length < 2) return null;
            pts[i] = new Point(data[0], data[1]);
        }

        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            if (pts[i].x < xMin) xMin = pts[i].x;
            if (pts[i].x > xMax) xMax = pts[i].x;
            if (pts[i].y < yMin) yMin = pts[i].y;
            if (pts[i].y > yMax) yMax = pts[i].y;
        }
        double width = xMax - xMin;
        double height = yMax - yMin;

        Point corner1 = null, corner2 = null, corner3 = null, corner4 = null; // TL, TR, BL, BR
        double minSum = Double.MAX_VALUE, maxSum = Double.MIN_VALUE;
        double minDiff = Double.MAX_VALUE, maxDiff = Double.MIN_VALUE;

        double[] sum = new double[4];
        double[] diff = new double[4];
        for (int i = 0; i < 4; i++) {
            sum[i] = pts[i].x + pts[i].y;
            diff[i] = pts[i].x - pts[i].y;
        }

        int i1 = argmin(sum);   // top-left
        int i2 = argmin(diff);  // top-right
        int i3 = argmax(diff);  // bottom-left
        int i4 = argmax(sum);   // bottom-right

        corner1 = pts[i1];
        corner2 = pts[i2];
        corner3 = pts[i3];
        corner4 = pts[i4];

        Point new_topleft     = new Point(corner1.x - 4.2 * width, corner1.y - 0.25 * width);
        Point new_topright    = new Point(corner1.x - 0.15 * width, corner1.y - 0.25 * width);
        Point new_bottomleft  = new Point(corner3.x - 4.2 * width, corner3.y + 1.75 * height);
        Point new_bottomright = new Point(corner3.x - 0.15 * width, corner3.y + 1.75 * height);

        double[] xs = { new_topleft.x, new_topright.x, new_bottomleft.x, new_bottomright.x };
        double[] ys = { new_topleft.y, new_topright.y, new_bottomleft.y, new_bottomright.y };

        double minX = xs[0], maxX = xs[0], minY = ys[0], maxY = ys[0];
        for (int i = 1; i < 4; i++) {
            if (xs[i] < minX) minX = xs[i];
            if (xs[i] > maxX) maxX = xs[i];
            if (ys[i] < minY) minY = ys[i];
            if (ys[i] > maxY) maxY = ys[i];
        }

        int finalXmin = (int)Math.floor(minX)-(int)width/2;
        int finalXmax = (int)Math.ceil(maxX)+(int)width/2;
        int finalYmin = (int)Math.floor(minY)-(int)height;
        int finalYmax = (int) ((int)Math.ceil(maxY)+(int)height*1.5);

        finalXmin = Math.max(finalXmin, 0);
        finalYmin = Math.max(finalYmin, 0);
        finalXmax = Math.min(finalXmax, rotatedImg.cols());
        finalYmax = Math.min(finalYmax, rotatedImg.rows());

        Rect roi = new Rect(finalXmin, finalYmin, finalXmax - finalXmin, finalYmax - finalYmin);
        return roi;
    }
    public static int argmin(double[] array) {
        int minIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[minIdx]) {
                minIdx = i;
            }
        }
        return minIdx;
    }

    public static int argmax(double[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

}
