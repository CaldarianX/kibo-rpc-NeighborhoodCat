package jp.jaxa.iss.kibo.rpc.sampleapk.Navigate;

import org.opencv.core.Mat;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.PoseEstimator;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.CameraImageHandler;
import jp.jaxa.iss.kibo.rpc.sampleapk.Camera.Camera_Type;
import jp.jaxa.iss.kibo.rpc.sampleapk.Log.Log;
import jp.jaxa.iss.kibo.rpc.sampleapk.Robot.RobotState;
import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.Quaternions;

import java.util.HashMap;
import java.util.Map;

public class Navigator{
    private KiboRpcApi api;
    private Integer Current_Navigation_Zone = -1;
    private Map<Integer, Integer> ZoneQuaternion = createZoneMap();
    private RobotState robotState;
    private Log log;

    private double[][] pointsData = {
        {10.4, -10.1, 4.4},     //init   0
        {10.925d,-9.8,5.0},    // airlock   0  area1    1
        {10.8d,-8.3,4.8},     // 2and3     1 area2&3.   3
        {10.9d,-6.875,4.945},       // 4   = 8   2  area 4. 4
        {11.143,-6.6707,4.9654} // astronaut = 9     0
    };

    private Quaternion[] QuaternionData = {
            Quaternions.fromRPY(Quaternions.deg(0), Quaternions.deg(-10f), Quaternions.deg(-90f)),

            Quaternions.fromRPY(Quaternions.deg(0), Quaternions.deg(90f), Quaternions.deg(0)),

            Quaternions.fromRPY(Quaternions.deg(0), Quaternions.deg(0), Quaternions.deg(-180f))
    };
    private static Map<Integer, Integer> createZoneMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 0);
        map.put(1, 0);
        map.put(2, 1);
        map.put(3, 2);
        map.put(4, 0);
        return map;
    }

    public Navigator(KiboRpcApi api){
        this.api = api;
        log = new Log(true);
        robotState =  new RobotState(this.api);
    }

    private Point GetNewTargetPoint(){
        return new Point(
                pointsData[Current_Navigation_Zone][0],
                pointsData[Current_Navigation_Zone][1],
                pointsData[Current_Navigation_Zone][2]
        );
    }
    private Point GetNewTargetPoint(Integer index){
        return new Point(
                pointsData[index][0],
                pointsData[index][1],
                pointsData[index][2]
        );
    }
    private Quaternion GetCurrentQuaternion(){
        return QuaternionData[ZoneQuaternion.get(Current_Navigation_Zone)];
    }
    private Quaternion GetCurrentQuaternion(Integer index){
        return QuaternionData[ZoneQuaternion.get(index)];
    }


    public Integer getCurrent_Navigation_Zone() {
        return Current_Navigation_Zone;
    }

    public void MoveToNextOasis(){
        Current_Navigation_Zone += 1;
        Quaternion quaternion    = GetCurrentQuaternion();
        Point point              = GetNewTargetPoint();

        MoveTo(1,point,quaternion);
    }
    public void MoveToOasis(Integer OasisAreaNumber){
        Current_Navigation_Zone  = OasisAreaNumber;

        Quaternion quaternion    = GetCurrentQuaternion(OasisAreaNumber);
        Point point              = GetNewTargetPoint(OasisAreaNumber);

        MoveTo(1,point,quaternion);
    }
    public void MoveTo(Integer maxadjest,Point point,Quaternion quaternion){
        Integer count = maxadjest;
        api.moveTo(point,quaternion,false);
        while(count > 0){
            api.moveTo(point,quaternion,false);
            count -=1;
        }
    }

    public double CapPosition(double position,double maxval,double minval){
        if(position > maxval){
            position = Math.min(position,maxval-0.02);
        }
        else if(position < minval){
            position = Math.max(position,minval+0.2);
        }
        return position;
    }

    public void MoveToAruco(Integer targetZone, CameraImageHandler cameraImageHandler){
        Mat navCam = cameraImageHandler.cameraImageData.navImage;

        PoseEstimator arucoEstimatePose = new PoseEstimator(this.api, Camera_Type.NavCam);
        arucoEstimatePose.setImage(navCam);
        arucoEstimatePose.poseEstimate(targetZone+100);
        arucoEstimatePose.debugTvec();
        Mat tvec = arucoEstimatePose.getTvec();

        if(tvec == null){
            return;
        }

        double tx = tvec.get(0, 0)[0];
        double ty = tvec.get(1, 0)[0];
        double tz = tvec.get(2, 0)[0];
        if(targetZone == 1){
            System.out.println("Go to area 1");
            Point current_position = GetNewTargetPoint(1);
            double astrobee_to_zone = Math.abs(current_position.getY()-(-10.58));
            double zDis = Math.abs(tz);
            double correct_scale = astrobee_to_zone/zDis;

            System.out.println(String.valueOf(astrobee_to_zone) + " - " + String.valueOf(zDis));
            System.out.println(String.valueOf(correct_scale));

            Point new_position = new Point(CapPosition(current_position.getX()+(tx*correct_scale)+0.0422,11.48f,10.42),
                                           -10.2 +0.3,
                                           CapPosition(current_position.getZ()+(ty*correct_scale)+0.0826,5.57,4.82)
            );
            api.moveTo(new_position,GetCurrentQuaternion(1),false);
        }
        else if (targetZone == 2){
            System.out.println("Go to area 2");
            Point current_position = GetNewTargetPoint(2);
            double astrobee_to_zone = Math.abs(current_position.getZ()-3.76203);
            double zDis = Math.abs(tz);
            double correct_scale = astrobee_to_zone/zDis;

            System.out.println(String.valueOf(astrobee_to_zone) + " - " + String.valueOf(zDis));
            System.out.println(String.valueOf(correct_scale));

            Point new_position = new Point(
                                           CapPosition((current_position.getX()+(ty*correct_scale)+0.0826),11.55f,10.3f),
                                           CapPosition((current_position.getY()+(tx*correct_scale)+0.0422),-8.5,-9.25),
                                           4.32 +0.3
            );
            api.moveTo(new_position,GetCurrentQuaternion(2),false);
        }
        else if(targetZone == 3){
            System.out.println("Go to area 2");
            Point current_position = GetNewTargetPoint(2);
            double astrobee_to_zone = Math.abs(current_position.getZ()-3.76203);
            double zDis = Math.abs(tz);
            double correct_scale = astrobee_to_zone/zDis;

            System.out.println(String.valueOf(astrobee_to_zone) + " - " + String.valueOf(zDis));
            System.out.println(String.valueOf(correct_scale));

            Point new_position = new Point(CapPosition((current_position.getX()+(ty*correct_scale)+0.0826),11.55f,10.3f),
                                           CapPosition((current_position.getY()+(tx*correct_scale)+0.0422),-7.45f,-8.4f),
                                           4.32 +0.3
            );
            api.moveTo(new_position,GetCurrentQuaternion(2),false);
        }
        else if(targetZone == 4){
            System.out.println("Go to area 3");
            Point current_position = GetNewTargetPoint(3);
            double astrobee_to_zone = Math.abs(current_position.getX()-9.866984);
            double zDis = Math.abs(tz);
            double correct_scale = astrobee_to_zone/zDis;

            System.out.println(String.valueOf(astrobee_to_zone) + " - " + String.valueOf(zDis));
            System.out.println(String.valueOf(correct_scale));

            Point new_position = new Point(10.3 +0.3,
                                            CapPosition((current_position.getY()-(tx*correct_scale)-0.0422),-6.365,-7.34),
                                            CapPosition((current_position.getZ()+(ty*correct_scale)+0.0826),5.57,4.32)
                                            );
            api.moveTo(new_position,GetCurrentQuaternion(3),false);
        }
        else{
            System.out.println("Who are you dude? Inside Navigator");
        }
    }
}
