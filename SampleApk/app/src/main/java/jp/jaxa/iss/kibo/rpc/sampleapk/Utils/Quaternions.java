package jp.jaxa.iss.kibo.rpc.sampleapk.Utils;

import gov.nasa.arc.astrobee.types.Quaternion;

public class Quaternions {

    public static Quaternion fromRPY(float roll, float pitch, float yaw) {
        float cy = (float) Math.cos(yaw * 0.5);
        float sy = (float) Math.sin(yaw * 0.5);
        float cp = (float) Math.cos(pitch * 0.5);
        float sp = (float) Math.sin(pitch * 0.5);
        float cr = (float) Math.cos(roll * 0.5);
        float sr = (float) Math.sin(roll * 0.5);

        float w = cr * cp * cy + sr * sp * sy;
        float x = sr * cp * cy - cr * sp * sy;
        float y = cr * sp * cy + sr * cp * sy;
        float z = cr * cp * sy - sr * sp * cy;

        return new Quaternion(x, y, z, w);
    }

    // Optional helper: Convert degrees to radians
    public static float deg(float degrees) {
        return (float) Math.toRadians(degrees);
    }
}
