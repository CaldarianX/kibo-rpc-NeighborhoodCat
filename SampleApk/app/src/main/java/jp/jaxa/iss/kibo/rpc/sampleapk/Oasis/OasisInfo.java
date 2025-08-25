package jp.jaxa.iss.kibo.rpc.sampleapk.Oasis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OasisInfo {
    private Map<Integer, List<double[]> > oasisPosition = new HashMap<>();

    public OasisInfo(){

    }
    public void addOasisPosition(Integer areaNumber,double[] newPosition){
        if(newPosition.length < 3){
            throw new IllegalArgumentException("Position must be an array of length 3.");
        }
        List<double[]> positions = oasisPosition.get(areaNumber);
        if(newPosition == null){
            positions = new ArrayList<>();
            oasisPosition.put(areaNumber,positions);
        }
        positions.add(newPosition);
    }
}