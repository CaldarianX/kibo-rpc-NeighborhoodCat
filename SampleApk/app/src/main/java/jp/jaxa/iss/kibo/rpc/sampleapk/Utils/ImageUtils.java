package jp.jaxa.iss.kibo.rpc.sampleapk.Utils;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoRegion;

public class ImageUtils {
    public static List<Mat> extratImages(List<ArucoRegion> regions){
        List<Mat> images = new ArrayList<>();
        for(int i =0;i<regions.size();i++){
            images.add(regions.get(i).image.clone());
        }
        return images;
    }
    public static void saveImages(List<Mat> images, KiboRpcApi api, String Prefix, Integer current_navigation_zone) {
        for (int i = 0; i < images.size(); i++) {
            api.saveMatImage(images.get(i), Prefix + current_navigation_zone + "_" + i + ".png");
        }
    }
    public static void saveImagesLowerSize(List<Mat> images, KiboRpcApi api, String Prefix, Integer current_navigation_zone) {
        for (int i = 0; i < images.size(); i++) {
            Mat resized_image = new Mat();
            Imgproc.resize(images.get(i), resized_image, new Size(images.get(i).cols() * 0.5, images.get(i).rows() * 0.5));
            api.saveMatImage(resized_image, Prefix + current_navigation_zone + "_" + i + ".png");
        }
    }

    public static Bitmap Convert_Mat_TO_Bitmap(Mat mat){
        Mat rgbMat = new Mat();
        if(mat.channels() ==1 ){
            Imgproc.cvtColor(mat,rgbMat,Imgproc.COLOR_GRAY2BGR);
        }
        else if (mat.channels() == 4){
            Imgproc.cvtColor(mat,rgbMat,Imgproc.COLOR_RGBA2BGR);
        }
        else if (mat.channels() == 3){
            Imgproc.cvtColor(mat,rgbMat,Imgproc.COLOR_BGR2RGB);
        } else{
            throw new IllegalArgumentException("Unsupported Mat format " + mat.channels() + "channels");
        }


        Bitmap bmp = Bitmap.createBitmap(rgbMat.cols(),rgbMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbMat,bmp);

        return bmp;
    }

    public static List<Double> getMarkerAngle2D(List<Mat> markerCorners) {
        List<Double> angles = new ArrayList<>();

        for (Mat corner : markerCorners) {
            double[] topLeft = corner.get(0, 0);  // [x, y]
            double[] topRight = corner.get(0, 1); // [x, y]

            double dx = topRight[0] - topLeft[0];
            double dy = topRight[1] - topLeft[1];

            double angleRad = Math.atan2(dy, dx);
            double angleDeg = Math.toDegrees(angleRad);
            angles.add(angleDeg);
        }

        return angles;
    }
    public static ByteBuffer preprocessing(Bitmap bitmap){
        int inputSize = 320;
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,inputSize,inputSize,true);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);
        inputBuffer.order(ByteOrder.nativeOrder());

        for(int y = 0;y<inputSize;y++){
            for(int x = 0;x<inputSize;x++){
                int pixel = resized.getPixel(x,y);

                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8)  & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;


                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }
        }
        return inputBuffer;
    }
    public static float[][] transpose(float[][] input) {
        int rows = input.length;
        int cols = input[0].length;

        float[][] transposed = new float[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = input[i][j];
            }
        }

        return transposed;
    }
    public static boolean isInsideMaskRegion(Point[] maskPolygon, int x1, int y1, int x2, int y2) {
        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;
        Point center = new Point(centerX, centerY);

        return Imgproc.pointPolygonTest(new MatOfPoint2f(maskPolygon), center, false) >= 0;
    }
    public static boolean is_valid_box(Mat image, int x1, int y1, int x2, int y2) {
        int width = image.cols();
        int height = image.rows();

        int marginX = (int)(0.07 * width);
        int marginY = (int)(0.07 * height);

        if (x1 < marginX || y1 < marginY || x2 > (width - marginX) || y2 > (height - marginY)) {
            return false;
        }

        Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
        Mat subImage = new Mat(image, roi);

        Mat resized = new Mat();
        Imgproc.resize(subImage, resized, new Size(32, 32));

        Map<String, Integer> colorCounts = new HashMap<>();
        int totalPixels = resized.rows() * resized.cols();

        for (int y = 0; y < resized.rows(); y++) {
            for (int x = 0; x < resized.cols(); x++) {
                double[] pixel = resized.get(y, x);
                int b = (int)(pixel[0] / 10) * 10;
                String key = b + "_";
                colorCounts.put(key, colorCounts.getOrDefault(key, 0) + 1);
            }
        }

        int maxCount = Collections.max(colorCounts.values());
        double dominantRatio = maxCount / (double) totalPixels;

        if (dominantRatio > 0.9) {
            System.out.println("Box rejected: flat color area (dominant color ratio = " + dominantRatio + ")");
            return false;
        }

        return true;
    }
}
