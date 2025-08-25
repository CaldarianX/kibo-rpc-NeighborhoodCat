package jp.jaxa.iss.kibo.rpc.sampleapk.Pipeline;

import android.media.Image;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoRegion;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.ArucoSplitter;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.Arucoref;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.ImageRotationStabilizer;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.ImageUndistorter;
import jp.jaxa.iss.kibo.rpc.sampleapk.ImagePreprocessor.ItemFrameExtractor;
import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.ImageUtils;

public class PipelineImageProcessing {

    ArucoDetector arucoDetector = new ArucoDetector();
    List<Mat> split_image = new ArrayList<>();
    List<Mat> rotated_image = new ArrayList<>();
    List<Mat> cropItem_Image = new ArrayList<>();
    List<ArucoRegion> regions = new ArrayList<>();
    public PipelineImageProcessing(){

    }

    public List<ArucoRegion> process(Mat image, KiboRpcApi api, Camera_Type camera_type){
        clear();
        System.out.println("Starting Process");
        arucoDetector.setImage(image);
        boolean ret = arucoDetector.detectAruco();
        List<Mat> corners  = arucoDetector.getMarkerCorners();
        MatOfInt markerids = arucoDetector.getMarkerIds();
        Integer markerAmount = arucoDetector.getAmountMarker();
        if(ret == false){
            return regions;
        }
        if(markerAmount == 0){
            return regions;
        }
        System.out.println("Start Split Image");
        regions = ArucoSplitter.splitter(image,corners,markerids);
        split_image = ImageUtils.extratImages(regions);
        System.out.println("Rotated StabilizeRotation");
        for(int i =0;i<regions.size();i++){
            Arucoref outputs = ImageRotationStabilizer.stabilizeRotation(regions.get(i).image,api,camera_type);
            regions.get(i).image = outputs.image;
            regions.get(i).id = outputs.id;
        }
        
        rotated_image = ImageUtils.extratImages(regions);

        System.out.println("Start ExtractItemFrame");
        for(int i =0;i<regions.size();i++){

            Mat newImage = ItemFrameExtractor.extractItemFrame(regions.get(i).image);
            regions.get(i).image = newImage;
        }
        cropItem_Image = ImageUtils.extratImages(regions);

        return regions;
    }
    public void clear(){
        regions = new ArrayList<>();
        split_image = new ArrayList<>();
        rotated_image = new ArrayList<>();
        cropItem_Image = new ArrayList<>();
    }

}
