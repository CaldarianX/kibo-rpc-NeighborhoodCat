package jp.jaxa.iss.kibo.rpc.sampleapk.AI;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.ImageUtils;

public class NMSProcessor {
    Mat debug_image_ai  = new Mat();
    public Map<Integer, String> classMap = new HashMap<Integer, String>() {{
        put(0, "coral");
        put(1, "treasure_box");
        put(2, "compass");
        put(3, "coin");
        put(4, "fossil");
        put(5, "shell");
        put(6, "key");
        put(7, "letter");
        put(8, "diamond");
        put(9, "crystal");
        put(10, "emerald");
    }};

    public Map<String, Scalar> classColors = new HashMap<String, Scalar>() {{
        put("coral", new Scalar(180, 105, 255));
        put("treasure_box", new Scalar(19, 69, 139));
        put("compass", new Scalar(0, 0, 255));
        put("coin", new Scalar(11, 134, 184));
        put("fossil", new Scalar(45, 82, 160));
        put("shell", new Scalar(85, 115, 139));
        put("key", new Scalar(34, 34, 178));
        put("letter", new Scalar(63, 133, 205));
        put("diamond", new Scalar(139, 0, 0));
        put("crystal", new Scalar(139, 61, 72));
        put("emerald", new Scalar(0, 100, 0));
    }};
    public List<DetectionResult> postProcessing(float[][] predict_output, Interpreter interpreter, Mat image, Point[] ArucoCorner){

        Mat debug_image = image.clone();
        Imgproc.cvtColor(debug_image,debug_image,Imgproc.COLOR_GRAY2BGR);

        int imageWidth = image.cols(); 
        int imageHeight = image.rows();

        int[] expect_shape = interpreter.getOutputTensor(0).shape();
        int batch = expect_shape[0];
        int classPerPrediction = expect_shape[1];
        int numDetections = expect_shape[2];

        System.out.println("Output tensor shape: [" + batch + ", " + classPerPrediction + ", " + numDetections + "]");
        int classIndexStartAt = 4;
        int numClasses = classPerPrediction - classIndexStartAt;

        predict_output = ImageUtils.transpose(predict_output);

        System.out.println("After transpose: [" + predict_output.length + "][" + predict_output[0].length + "]");
        List<DetectionResult> rawDetectionResults = new ArrayList<>();
        for (int i = 0; i < numDetections; i++) {
            float[] pred = predict_output[i];

            float cx = pred[0];
            float cy = pred[1];
            float w = pred[2];
            float h = pred[3];

            int x1 = (int) ((cx - w / 2) * imageWidth);
            int y1 = (int) ((cy - h / 2) * imageHeight);
            int x2 = (int) ((cx + w / 2) * imageWidth);
            int y2 = (int) ((cy + h / 2) * imageHeight);

            float maxConfidence = 0f;
            int maxClassIndex = -1;

            for (int j = classIndexStartAt; j < classPerPrediction; j++) {
                float score = pred[j];
                if (score > maxConfidence && score > 0.3f) {
                    maxConfidence = score;
                    maxClassIndex = j - classIndexStartAt;
                }
            }

            if (maxConfidence > 0.3f && maxClassIndex != -1 &&
                    ImageUtils.is_valid_box(image,x1,y1,x2,y2) &&
                    !ImageUtils.isInsideMaskRegion(ArucoCorner,x1,y1,x2,y2)) {

                List<Integer> box = Arrays.asList(x1, y1, x2, y2);
                rawDetectionResults.add(new DetectionResult(maxConfidence, maxClassIndex, box));

            }
        }
        List<DetectionResult> finalDetectionResults = DetectionResult.nmsFilter(rawDetectionResults, 0.5f);

        return finalDetectionResults;
    }
    public ZoneInfo mapToZoneInfo(List<DetectionResult> finalDetectionResults, Mat debug_image) {
        if (debug_image.channels() == 1) {
            Imgproc.cvtColor(debug_image, debug_image, Imgproc.COLOR_GRAY2BGR);
        }
        int landmark_amount = 0;
        int tresure_amount = 0;
        Map<String, Integer> landmarkFrequency = new HashMap<>();
        Map<String, Integer> treasureFrequency = new HashMap<>();

        for (DetectionResult det : finalDetectionResults) {
            int x1 = det.box.get(0);
            int y1 = det.box.get(1);
            int x2 = det.box.get(2);
            int y2 = det.box.get(3);

            System.out.println("Box: [" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + "]");
            int classIdx = det.classIndex;
            float conf = det.confidence;

            String classname = classMap.get(classIdx);
            Scalar color = classColors.getOrDefault(classname, new Scalar(255, 0, 0));

            if (classIdx < 8) {
                landmark_amount++;
                landmarkFrequency.put(classname, landmarkFrequency.getOrDefault(classname, 0) + 1);
            } else {
                tresure_amount++;
                treasureFrequency.put(classname, treasureFrequency.getOrDefault(classname, 0) + 1);
            }

            Rect rect = new Rect(new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2));
            Imgproc.rectangle(debug_image, rect, color, 3);
            Imgproc.putText(debug_image, classname + " " + String.format("%.2f", conf),
                    new org.opencv.core.Point(x1, y1 - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 1, color, 2);

        }
        debug_image_ai = debug_image;
        String landmarkType = getMajorityClass(landmarkFrequency, "none");

        String treasureType = getMajorityClass(treasureFrequency, "none");

        return new ZoneInfo(landmark_amount, tresure_amount, treasureType, landmarkType);
    }
    private String getMajorityClass(Map<String, Integer> freqMap, String defaultValue) {
        String majority = defaultValue;
        int max = 0;
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() >= max) {
                max = entry.getValue();
                majority = entry.getKey();
            }
        }
    
        return majority;
    }
    public Mat get_debug_image(){
        return debug_image_ai;
    }
}
