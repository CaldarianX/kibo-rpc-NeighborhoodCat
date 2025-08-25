package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;

public class ItemFrameExtractor {

    public static Mat extractItemFrame(Mat rotatedImg){
        Mat masked_image = ArucoMasker.maskAruco(rotatedImg);
        Rect roi = CropFrame.getTargetArea(rotatedImg);
        if(masked_image == null){
            System.out.println("Maked_Image is null------------------------------------------------------------------");
            return rotatedImg;
        }
        if(roi == null){
            System.out.println("Roi is null-------------------------------------------------------------");
            return rotatedImg;
        }
        return new Mat(masked_image,roi);
    }

}
