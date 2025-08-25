package jp.jaxa.iss.kibo.rpc.sampleapk.Camera;

import org.opencv.core.Mat;

public class CameraImageData {
    public Mat navImage;
    public Mat dockImage;

    public CameraImageData(){
        this.navImage = new Mat();
        this.dockImage = new Mat();
    }
    public CameraImageData(Mat navImage,Mat dockImage){
        this.navImage = navImage;
        this.dockImage = dockImage;
    }
}
