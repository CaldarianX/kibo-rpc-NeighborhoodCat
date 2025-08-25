package jp.jaxa.iss.kibo.rpc.sampleapk.AI;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModelLoader {
    public static Interpreter Load_Yolo_Model(String modelName, Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(modelName);
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            inputStream.close();

            ByteBuffer buffer = ByteBuffer.allocateDirect(modelBytes.length);
            buffer.order(ByteOrder.nativeOrder());
            buffer.put(modelBytes);
            buffer.rewind();
            return new Interpreter(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
