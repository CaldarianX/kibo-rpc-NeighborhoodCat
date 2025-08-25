package jp.jaxa.iss.kibo.rpc.sampleapk.AI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetectionResult {
    public float confidence;
    public int classIndex;
    public List<Integer> box; // [x1, y1, x2, y2]

    public DetectionResult(float confidence, int classIndex, List<Integer> box) {
        this.confidence = confidence;
        this.classIndex = classIndex;
        this.box = box;
    }

    public static float iou(List<Integer> boxA, List<Integer> boxB) {
        int xA = Math.max(boxA.get(0), boxB.get(0));
        int yA = Math.max(boxA.get(1), boxB.get(1));
        int xB = Math.min(boxA.get(2), boxB.get(2));
        int yB = Math.min(boxA.get(3), boxB.get(3));

        int interW = Math.max(0, xB - xA);
        int interH = Math.max(0, yB - yA);
        float interArea = interW * interH;

        float areaA = (boxA.get(2) - boxA.get(0)) * (boxA.get(3) - boxA.get(1));
        float areaB = (boxB.get(2) - boxB.get(0)) * (boxB.get(3) - boxB.get(1));

        float union = areaA + areaB - interArea;
        if (union <= 0f) return 0f;
        return interArea / union;
    }

    public static List<DetectionResult> nmsFilter(List<DetectionResult> detectionResults, float iouThresh) {
        Collections.sort(detectionResults, new Comparator<DetectionResult>() {
            public int compare(DetectionResult d1, DetectionResult d2) {
                return Float.compare(d2.confidence, d1.confidence);
            }
        });

        List<DetectionResult> result = new ArrayList<DetectionResult>();

        while (!detectionResults.isEmpty()) {
            DetectionResult best = detectionResults.remove(0);
            result.add(best);

            List<DetectionResult> remaining = new ArrayList<DetectionResult>();
            for (int i = 0; i < detectionResults.size(); i++) {
                DetectionResult det = detectionResults.get(i);
                float overlap = iou(best.box, det.box);
                if (overlap <= iouThresh) {
                    remaining.add(det);
                }
            }

            detectionResults = remaining;
        }

        return result;
    }

}
