package jp.jaxa.iss.kibo.rpc.sampleapk.Camera;

import org.opencv.core.Mat;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.ImageUndistorter;
import jp.jaxa.iss.kibo.rpc.sampleapk.Navigate.Navigator;


public class CameraImageHandler {
    private KiboRpcApi api;
    private Navigator nav;
    private ImageUndistorter imageUndistorter;
    public CameraImageData cameraImageData;

    public CameraImageHandler(KiboRpcApi api,Navigator nav){
        this.api = api;
        this.nav = nav;
        cameraImageData = new CameraImageData();
        imageUndistorter = new ImageUndistorter(api);
    }

    public void captureAndSave(String subfix,boolean undistort){
        captureCameraImages(undistort);
        saveCapturedImage(subfix);
    }

    public void captureCameraImages(boolean undistort){
        Mat navImage = api.getMatNavCam();
        Mat dockImage = api.getMatDockCam();
        if(undistort) {
            navImage = imageUndistorter.undistort(navImage, Camera_Type.NavCam);
            dockImage = imageUndistorter.undistort(dockImage, Camera_Type.DockCam);
        }
        cameraImageData = new CameraImageData(navImage,dockImage);
    }

    public void saveCapturedImage(String subfix){
        Mat navImage = cameraImageData.navImage;
        Mat dockImage = cameraImageData.dockImage;
        Integer currentZone = nav.getCurrent_Navigation_Zone();
        if(navImage == null || dockImage == null){
            return;
        }
        api.saveMatImage(navImage, "Nav_" + currentZone + subfix +  ".png");
        api.saveMatImage(dockImage,"Dock_"+currentZone+ subfix + ".png");
    }
}
