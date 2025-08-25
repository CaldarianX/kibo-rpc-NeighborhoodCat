package jp.jaxa.iss.kibo.rpc.sampleapk.AI;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.sampleapk.Utils.ImageUtils;

public class ModelRunner {
    private Interpreter interpreter;

    public static float[][] Predict(Interpreter interpreter,Mat image) {
        ByteBuffer input = ImageUtils.preprocessing(ImageUtils.Convert_Mat_TO_Bitmap(image));
        int [] expect_shape = interpreter.getOutputTensor(0).shape();
        int batch = expect_shape[0];
        int valuesPerPrediction = expect_shape[1];
        int numDetections = expect_shape[2];

        float[][][] output = new float[batch][valuesPerPrediction][numDetections];
        interpreter.run(input, output);
        float[][] output2 = output[0];

        return output2;
    }
}
