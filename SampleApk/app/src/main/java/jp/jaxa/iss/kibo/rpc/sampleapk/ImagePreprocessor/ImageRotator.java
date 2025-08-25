package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageRotator {
    public static Mat rotate(Mat img,double angleDeg){
        int h = img.rows();
        int w = img.cols();

        Point center = new Point(w / 2.0, h / 2.0);

        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angleDeg, 1.0);

        Mat rotatedImg = new Mat();
        Imgproc.warpAffine(img, rotatedImg, rotationMatrix, new Size(w, h), Imgproc.INTER_LINEAR);

        return rotatedImg;
    }
    public static Mat rotateWithPadding(Mat src, double angleDeg) {
        int w = src.cols();
        int h = src.rows();

        // 1. Compute center and rotation matrix
        Point center = new Point(w / 2.0, h / 2.0);
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angleDeg, 1.0);

        // 2. Create a RotatedRect and get the rotated corner points
        RotatedRect rotatedRect = new RotatedRect(new Point(0, 0), new Size(w, h), angleDeg);
        Point[] corners = new Point[4];
        rotatedRect.points(corners);

        // 3. Compute bounding box of rotated image manually
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Point pt : corners) {
            if (pt.x < minX) minX = pt.x;
            if (pt.y < minY) minY = pt.y;
            if (pt.x > maxX) maxX = pt.x;
            if (pt.y > maxY) maxY = pt.y;
        }

        double bboxWidth = maxX - minX;
        double bboxHeight = maxY - minY;

        // 4. Adjust transformation to center result in new canvas
        double dx = bboxWidth / 2.0 - w / 2.0;
        double dy = bboxHeight / 2.0 - h / 2.0;

        rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + dx);
        rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + dy);

        // 5. Perform rotation with padded canvas filled with white
        Mat dst = new Mat();
        Imgproc.warpAffine(
                src, dst, rotationMatrix,
                new Size(bboxWidth, bboxHeight),
                Imgproc.INTER_LINEAR,
                Core.BORDER_CONSTANT,
                new Scalar(255, 255, 255)  // white background
        );

        return dst;
    }

}
