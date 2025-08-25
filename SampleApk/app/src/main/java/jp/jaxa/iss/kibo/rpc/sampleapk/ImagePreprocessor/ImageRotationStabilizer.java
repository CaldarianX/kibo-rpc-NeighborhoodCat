package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

import java.util.List;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.ImageUtils;

public class ImageRotationStabilizer {
    public static Arucoref stabilizeRotation(Mat image, KiboRpcApi api, Camera_Type camera_type){
        Arucoref outputs = new Arucoref();
        ArucoDetector arucoDetector = new ArucoDetector();
        arucoDetector.setImage(image);
        arucoDetector.detectAruco();

        MatOfInt id = arucoDetector.getMarkerIds();

        if (!id.empty()) {
            int firstId = (int) id.get(0, 0)[0];
            outputs.id = firstId;
        }

        List<Mat> corner = arucoDetector.getMarkerCorners();

        List<Double> angles = ImageUtils.getMarkerAngle2D(corner);

        double zDeg = angles.get(0);
        Mat rotated_image = ImageRotator.rotateWithPadding(image,zDeg);
        outputs.image = rotated_image;
        return outputs;
    }
}
