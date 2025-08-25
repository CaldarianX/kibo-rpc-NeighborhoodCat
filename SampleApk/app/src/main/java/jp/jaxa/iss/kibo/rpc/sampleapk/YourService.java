package jp.jaxa.iss.kibo.rpc.sampleapk;
import android.os.SystemClock;

import gov.nasa.arc.astrobee.Kinematics;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import jp.jaxa.iss.kibo.rpc.sampleapk.AI.ModelLoader;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoDetector;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.PoseEstimator;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.CameraImageHandler;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import jp.jaxa.iss.kibo.rpc.sampleapk.MissionState.MissionState;
import jp.jaxa.iss.kibo.rpc.sampleapk.Log.Log;
import jp.jaxa.iss.kibo.rpc.sampleapk.Navigate.Navigator;
import jp.jaxa.iss.kibo.rpc.sampleapk.Pipeline.FullPipeline;

import org.opencv.core.Mat;

import org.tensorflow.lite.Interpreter;

public class YourService extends KiboRpcService {
    public MissionState CurrentMissionState = MissionState.Init;
    public boolean isDebug = true;
    public Mat navImage;
    public Mat dockImage;


    public Log log;
    public jp.jaxa.iss.kibo.rpc.sampleapk.Navigate.Navigator navigator;
    public ArucoDetector arucoDetector;
    public CameraImageHandler cameraImageHandler;
    public PoseEstimator arucoDockEstimatePose;
    public PoseEstimator arucoNavEstimatePose;
    public FullPipeline perceptionEngine;
    @Override
    protected void runPlan1(){
        while(CurrentMissionState != MissionState.Mission_Complete){
            switch(CurrentMissionState){
                case Init:
                    Start_Mission();
                    CurrentMissionState = MissionState.Navigate;
                    break;
                case Navigate:
                    Start_Navigate();
                    if (navigator.getCurrent_Navigation_Zone() >= 4){
                        CurrentMissionState = MissionState.Report_Object;
                    }
                    else {
                        CurrentMissionState = MissionState.Capture_Image;
                    }
                    break;
                case Capture_Image:
                    Start_Capture_Image();
                    CurrentMissionState = MissionState.Navigate;
                    break;
                case Report_Object:
                    Start_Report_Object();
                    CurrentMissionState = MissionState.Astronaut;
                    break;
                case Astronaut:
                    Start_Report_Astronaut();
                    CurrentMissionState = MissionState.Go_To_Lost_Item;
                    break;
                case Go_To_Lost_Item:
                    Start_Go_To_Lost_Item();
                    CurrentMissionState = MissionState.Finish;
                    break;
                case Finish:
                    Start_Finish();
                    CurrentMissionState = MissionState.Mission_Complete;
                    break;
            }
        }
    }
    public void Start_Mission(){

        api.startMission();

        log = new Log(isDebug);
        log.log("Start Mission");
        navigator = new Navigator(this.api);
        arucoDetector = new ArucoDetector();
        cameraImageHandler = new CameraImageHandler(api,navigator);
        arucoDockEstimatePose = new PoseEstimator(this.api, Camera_Type.DockCam);
        arucoNavEstimatePose  = new PoseEstimator(this.api, Camera_Type.NavCam);
        Interpreter interpreter = ModelLoader.Load_Yolo_Model("Ascalon_YOLOV8.tflite",this);
        perceptionEngine = new FullPipeline(cameraImageHandler,navigator,interpreter,this.api,this);
        if(interpreter != null){
            log.log("Load Model Success :) ");
            log.log(String.valueOf(interpreter.getInputTensorCount()));
        }
        else{
            log.log("Load Model Failed :( ");
        }
    }
    public void checkKinematics(){
        Kinematics kinematics = api.getRobotKinematics();
        log.log("Robot Kinematics");
        log.log("Confidence : " + kinematics.getConfidence().toString());
        log.log("Position :  " + kinematics.getPosition().toString());
        log.log("Orientation :  " + kinematics.getOrientation().toString());
    }

    public void Processing_Image(){
        perceptionEngine.pipeline();
    }
    public void Start_Navigate(){
        log.log("Navigate");
        navigator.MoveToNextOasis();
        checkKinematics();
    }
    public void Start_Capture_Image(){
        log.log("Capture Image");
        SystemClock.sleep(3000);
        cameraImageHandler.captureAndSave("",true);
        Processing_Image();
    }
    public void Start_Report_Object(){
        log.log("Report Object");
        perceptionEngine.ReportObject();
    }
    public void Start_Report_Astronaut(){
        api.reportRoundingCompletion();
        SystemClock.sleep(3000);

        cameraImageHandler.captureAndSave("_Astronaut",true);
        Processing_Image();

        api.notifyRecognitionItem();
        perceptionEngine.debug();
    }
    public void Start_Go_To_Lost_Item(){
        Integer lostZoneID = perceptionEngine.getLostItemZone();
        System.out.println("Lost Item is : " + String.valueOf(lostZoneID));
        if(lostZoneID == 1){
            System.out.println("Go to area 1");
            navigator.MoveToOasis(1);
        }
        else if (lostZoneID == 2 || lostZoneID == 3){
            System.out.println("Go to area 2");
            navigator.MoveToOasis(2);
        }
        else if(lostZoneID == 4){
            System.out.println("Go to area 3");
            navigator.MoveToOasis(3);
        }
        else{
            System.out.println("Who are you dude?");
        }
        checkKinematics();
        SystemClock.sleep(3000);
        cameraImageHandler.captureAndSave("_Report_Object", false);
        navigator.MoveToAruco(lostZoneID, this.cameraImageHandler);
        SystemClock.sleep(3000);
        cameraImageHandler.captureAndSave("_Report_Object_Final",false);
        api.notifyRecognitionItem();
    }
    public void Start_Finish(){
        log.log("Finish");
        api.takeTargetItemSnapshot();
    }
}