package jp.jaxa.iss.kibo.rpc.sampleapk.Pipeline;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.List;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;
import jp.jaxa.iss.kibo.rpc.sampleapk.AI.DetectionResult;
import jp.jaxa.iss.kibo.rpc.sampleapk.AI.NMSProcessor;
import jp.jaxa.iss.kibo.rpc.sampleapk.AI.ModelRunner;
import jp.jaxa.iss.kibo.rpc.sampleapk.AI.ZoneInfo;
import jp.jaxa.iss.kibo.rpc.sampleapk.Aruco.ArucoRegion;

public class PipelineDetection {
    public List<ZoneInfo> process(Interpreter interpreter, List<ArucoRegion> regions, KiboRpcApi api){
        List<ZoneInfo> zoneInfos = new ArrayList<>();
        for(int i =0;i<regions.size();i++){
            NMSProcessor detectionPostProcessor = new NMSProcessor();
            float[][] output = ModelRunner.Predict(interpreter,regions.get(i).image);
            List<DetectionResult> detectionResults = detectionPostProcessor.postProcessing(output,interpreter,regions.get(i).image,regions.get(i).corners);
            ZoneInfo zoneDetection     = detectionPostProcessor.mapToZoneInfo(detectionResults,regions.get(i).image);
            Integer zoneID = regions.get(i).id;
            zoneDetection.setId(zoneID);
            Mat debug_image = detectionPostProcessor.get_debug_image();

            String zoneLabel = "Zone: " + zoneID;

            Imgproc.putText(debug_image, zoneLabel, new Point(10, 20),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);

            api.saveMatImage(debug_image,"AI_Debug_"+zoneLabel+".png");

            zoneInfos.add(zoneDetection);
        }
        return zoneInfos;
    }
}
