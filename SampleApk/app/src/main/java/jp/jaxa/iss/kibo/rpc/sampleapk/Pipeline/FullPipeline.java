package jp.jaxa.iss.kibo.rpc.sampleapk.Pipeline;

import android.content.Context;

import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.AI.ZoneInfo;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoRegion;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.CameraImageHandler;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import jp.jaxa.iss.kibo.rpc.sampleapk.Navigate.Navigator;
import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.ImageUtils;

public class FullPipeline {
    private CameraImageHandler cameraImageHandler;
    private PipelineImageProcessing imageProcessorNav;
    private PipelineImageProcessing imageProcessorDock;
    private PipelineDetection detectionPipeline;
    private Navigator navigator;
    private KiboRpcApi api;
    private Context context;
    private Interpreter interpreter;
    private int current_navigation_zone;
    private Map<Integer, ZoneInfo> zoneMap = new HashMap<>();
    public FullPipeline(CameraImageHandler cameraImageHandler, Navigator navigator,Interpreter interpreter,KiboRpcApi api, Context context) {
        this.cameraImageHandler = cameraImageHandler;
        this.api = api;
        this.navigator = navigator;
        this.context = context;
        this.imageProcessorNav = new PipelineImageProcessing();
        this.imageProcessorDock = new PipelineImageProcessing();
        this.detectionPipeline = new PipelineDetection();
        this.interpreter = interpreter;
    }
    public void pipeline() {
        Mat navImage = cameraImageHandler.cameraImageData.navImage;
        Mat dockImage = cameraImageHandler.cameraImageData.dockImage;

        current_navigation_zone = navigator.getCurrent_Navigation_Zone();

        if (current_navigation_zone !=4) {

            System.out.println("################### NAV PIC ########################");
            List<ArucoRegion> navRegions = imageProcessorNav.process(navImage, api, Camera_Type.NavCam);
            List<ZoneInfo> zoneDetections = detectionPipeline.process(interpreter,navRegions,api);

            ImageUtils.saveImages(imageProcessorNav.split_image, api, "Split_Nav", current_navigation_zone);
            ImageUtils.saveImagesLowerSize(imageProcessorNav.rotated_image, api, "Rotate_Nav", current_navigation_zone);
            ImageUtils.saveImages(imageProcessorNav.cropItem_Image, api, "Cropped_Nav", current_navigation_zone);

            for (ZoneInfo data : zoneDetections) {
                zoneMap.put(data.id, data);
            }
        
        } else if (current_navigation_zone == 4) {

            System.out.println("################### DOCK PIC ########################");
            List<ArucoRegion> dockRegions = imageProcessorDock.process(dockImage, api, Camera_Type.DockCam);
            List<ZoneInfo> zoneDetections = detectionPipeline.process(interpreter,dockRegions,api);

            ImageUtils.saveImages(imageProcessorDock.split_image, api, "Split_Dock", current_navigation_zone);
            ImageUtils.saveImagesLowerSize(imageProcessorDock.rotated_image, api, "Rotate_Dock", current_navigation_zone);
            ImageUtils.saveImages(imageProcessorDock.cropItem_Image, api, "Cropped_Dock", current_navigation_zone);

            for (ZoneInfo data : zoneDetections) {
                zoneMap.put(data.id, data);
            }
        }
    }
    public void debug() {
        System.out.println("######################## DEBUG #########################");
        for (Map.Entry<Integer, ZoneInfo> entry : zoneMap.entrySet()) {
            Integer id = entry.getKey();
            ZoneInfo info = entry.getValue();
            System.out.println("Zone ID: " + id);
            System.out.println("  Landmark Amount: " + info.landmark_amount);
            System.out.println("  Treasure Amount: " + info.tresure_amount);
            System.out.println("  Landmark Type: " + info.getLandmark_type());
            System.out.println("  Treasure Type: " + info.treasure_type);
            System.out.println();
        }
    }
    public void ReportObject(){
        for(int i = 101;i<105;i++){
            ZoneInfo target = zoneMap.get(i);
            if(target != null){
                api.setAreaInfo(i - 100, target.landmark_type, target.landmark_amount);
            }
        }
    }
    public Integer getLostItemZone(){
        ZoneInfo lostItem = zoneMap.get(100);

        if (lostItem == null || lostItem.treasure_type.equals("none")) return 1;
        String treasureTarget = lostItem.treasure_type;

        List<Integer> matchingZone = new ArrayList<>();
        for(int i = 101;i<105;i++){
            ZoneInfo target = zoneMap.get(i);
            if(target != null && !target.treasure_type.equals("none") && (target.treasure_type.equals(treasureTarget))){
                matchingZone.add(i);
            }
        }
        if(matchingZone.size()==1){
            return matchingZone.get(0)-100;
        }

        List<Integer> unassignedZones = new ArrayList<>();
        for (int id = 101; id <= 104; id++) {
            if (!zoneMap.containsKey(id)) {
                unassignedZones.add(id);
            }
        }

        if (!unassignedZones.isEmpty()) {
            Collections.shuffle(unassignedZones);
            return unassignedZones.get(0)-100;
        }

        return null;
    }

}
