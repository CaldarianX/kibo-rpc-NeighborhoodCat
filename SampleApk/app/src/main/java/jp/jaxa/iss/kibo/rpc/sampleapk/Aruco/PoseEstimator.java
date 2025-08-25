package jp.jaxa.iss.kibo.rpc.sampleapk.Aruco;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.Arrays;
import java.util.List;

public class PoseEstimator {
    private double markerLength = 0.05;
    private Mat cameraMatrix;
    private MatOfDouble distCoeffs;
    private KiboRpcApi api;
    private Camera_Type camera_type;
    private Mat image;
    private ArucoDetector arucoDetector;
    private Mat rvec = new Mat();
    private Mat tvec = new Mat();

    public PoseEstimator(KiboRpcApi api, Camera_Type camera_type) {
        this.api = api;
        this.camera_type = camera_type;
        this.cameraMatrix = getDefaultMatrix();
        this.distCoeffs = getDefaultDistCoeffs();
        this.arucoDetector = new ArucoDetector();
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
    public void setImage(Mat image){
        this.image = image;
    }

    public Mat getTargetCorner(List<Mat> corners, MatOfInt markerIds, Integer targetZone) {
        if (corners == null || markerIds == null || markerIds.empty()) {
            System.out.println("ArucoEstimatePose: Input is null or empty");
            return new Mat();
        }

        int[] ids = new int[(int) markerIds.total()];
        markerIds.get(0, 0, ids);

        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == targetZone) {
                System.out.println("Found target marker ID: " + targetZone + " at index " + i);
                return corners.get(i);
            }
        }

        System.out.println("Target marker ID " + targetZone + " not found.");
        return new Mat();
    }
    public void poseEstimate(Integer targetZone) {
        arucoDetector.setImage(this.image);
        boolean detected = arucoDetector.detectAruco();

        if (!detected) {
            System.out.println("No ArUco marker detected.");
            return;
        }

        List<Mat> corners = arucoDetector.getMarkerCorners();
        if (corners == null || corners.size() == 0) {
            System.out.println("No marker corners returned.");
            return;
        }

        System.out.println("Corners size: " + corners.size());
        MatOfInt markerIds= arucoDetector.getMarkerIds();
        Mat cornerMat = getTargetCorner(corners,markerIds,targetZone);
        System.out.println("cornerMat size: rows=" + cornerMat.rows() + " cols=" + cornerMat.cols());
        System.out.println(cornerMat);
        System.out.println("Corner coordinates:");
        for (int i = 0; i < cornerMat.cols(); i++) {
            double[] pt = cornerMat.get(0, i);
            if (pt != null && pt.length >= 2) {
                System.out.println("Point " + i + ": x = " + pt[0] + ", y = " + pt[1]);
            } else {
                System.out.println("Point " + i + ": invalid or missing");
            }
        }

        Point[] points = new Point[4];
        for (int i = 0; i < 4; i++) {
            double[] data = cornerMat.get(0, i);  // FIXED from (i, 0)
//            System.out.println("Point " + i + ": " + Arrays.toString(data));
            if (data == null || data.length < 2) {
                System.out.println("Failed to get corner point at index " + i);
                return;
            }
            points[i] = new Point(data[0], data[1]);
        }
//
        MatOfPoint2f imgPoints = new MatOfPoint2f(points);
//
//        // Define object points (3D marker corners)
        MatOfPoint3f objp = new MatOfPoint3f(
                new Point3(-0.5 * markerLength,  0.5 * markerLength, 0),
                new Point3( 0.5 * markerLength,  0.5 * markerLength, 0),
                new Point3( 0.5 * markerLength, -0.5 * markerLength, 0),
                new Point3(-0.5 * markerLength, -0.5 * markerLength, 0)
        );

        rvec = new Mat();
        tvec = new Mat();
//
//        // Pose estimation
        boolean pnpSuccess = Calib3d.solvePnP(objp, imgPoints, cameraMatrix, distCoeffs, rvec, tvec);
        if (!pnpSuccess) {
            System.out.println("solvePnP failed.");
            return;
        }

        System.out.println("solvePnP successful.");
    }
    public Mat getTvec(){
        return tvec;
    }
    public void debugTvec() {
        if (tvec == null || tvec.empty() || tvec.rows() < 3 || tvec.cols() < 1) {
            System.out.println("Invalid tvec.");
            return;
        }

        double tx = tvec.get(0, 0)[0];
        double ty = tvec.get(1, 0)[0];
        double tz = tvec.get(2, 0)[0];

        double distance = Math.sqrt(tx * tx + ty * ty + tz * tz);

        System.out.println("==== Translation Vector (tvec) ====");
        System.out.printf("X (Right)  : %.4f\n", tx);
        System.out.printf("Y (Down)   : %.4f\n", ty);
        System.out.printf("Z (Forward): %.4f\n", tz);
        System.out.printf("3D Distance to Marker: %.4f units\n", distance);
        System.out.println("===================================");
    }
    public double getZdeg(){
        Mat rmat = new Mat();
        Calib3d.Rodrigues(rvec,rmat);

        double[][] R = new double[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                R[row][col] = rmat.get(row, col)[0];
            }
        }

        double sy = Math.sqrt(R[0][0] * R[0][0] + R[1][0] * R[1][0]);

        double x, y, z;
        x = Math.atan2(R[2][1], R[2][2]);     // roll
        y = Math.atan2(-R[2][0], sy);        // pitch
        z = Math.atan2(R[1][0], R[0][0]);    // yaw

        double xDeg = Math.toDegrees(x);
        double yDeg = Math.toDegrees(y);
        double zDeg = Math.toDegrees(z);

        return zDeg;
    }
}
