package jp.jaxa.iss.kibo.rpc.sampleapk.Robot;

import java.util.logging.Logger;

import gov.nasa.arc.astrobee.Kinematics;
import gov.nasa.arc.astrobee.types.Point;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.Log.Log;

public class RobotState {
    private KiboRpcApi api;
    private Log log;
    private Kinematics kinematics;
    public RobotState(KiboRpcApi api){
        this.api = api;
        this.log = new Log(true);
        this.kinematics = api.getRobotKinematics();
    }

    public void fetchState(){
        kinematics = api.getRobotKinematics();
    }

    public Point getRobotPosition(){
        return this.kinematics.getPosition();
    }

    public void debugKinematics(){
        log.log("Robot Kinematics");
        log.log("Confidence : " + kinematics.getConfidence().toString());
        log.log("Position :  " + kinematics.getPosition().toString());
        log.log("Orientation :  " + kinematics.getOrientation().toString());
    }
}
