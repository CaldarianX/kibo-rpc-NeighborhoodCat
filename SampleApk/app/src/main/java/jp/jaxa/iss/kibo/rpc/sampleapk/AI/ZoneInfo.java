package jp.jaxa.iss.kibo.rpc.sampleapk.AI;

import org.tensorflow.lite.Interpreter;

public class ZoneInfo {
    public Integer landmark_amount;
    public Integer  tresure_amount;
    public String treasure_type;
    public String landmark_type;
    public Integer id;

    public ZoneInfo(Integer landmark_amount,Integer tresure_amount,String treasure_type,String landmark_type){
        this.tresure_amount  = tresure_amount;
        this.landmark_amount = landmark_amount;
        this.treasure_type = treasure_type;
        this.landmark_type = landmark_type;
    }
    public String getLandmark_type() {
        return landmark_type;
    }

    public void setId(Integer id){
        this.id = id;
    }
}
