package jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgproc.Imgproc;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;

public class ImageUndistorter {
    private Mat cameraMatrix;
    private MatOfDouble distCoeffs;
    private Camera_Type camera_type;
    private KiboRpcApi api;
    public ImageUndistorter(KiboRpcApi api){
        this.api = api;
    }
    public Mat undistort(Mat image, Camera_Type camera_type) {
        this.camera_type  = camera_type;
        this.cameraMatrix = this.getDefaultMatrix();
        this.distCoeffs   = this.getDefaultDistCoeffs();

        Mat undistorted = new Mat();
        Calib3d.undistort(image, undistorted, cameraMatrix, distCoeffs);
        return undistorted;
    }
    private Mat getDefaultMatrix() {
        if (camera_type == Camera_Type.DockCam) {
            return new MatOfDouble(api.getDockCamIntrinsics()[0]).reshape(1, 3);
        } else {
            return new MatOfDouble(api.getNavCamIntrinsics()[0]).reshape(1, 3);
        }
    }

    private MatOfDouble getDefaultDistCoeffs() {
        if (camera_type == Camera_Type.DockCam) {
            return new MatOfDouble(api.getDockCamIntrinsics()[1]);
        } else {
            return new MatOfDouble(api.getNavCamIntrinsics()[1]);
        }
    }
}