package com.demo.opengles.world;

import android.opengl.Matrix;

public class MatrixHelper {

    public static final int FLOAT_SIZE = 4;

    public static float[] multiplyMM(float[] m1, float[] m2, float[] m3) {
        return multiplyMM(multiplyMM(m1, m2), m3);
    }

    public static float[] multiplyMM(float[] m1, float[] m2) {
        if (m1 == null && m2 == null) {
            return null;
        } else if (m1 != null && m2 != null) {
            float[] result = new float[m1.length];
            Matrix.multiplyMM(result, 0, m1, 0, m2, 0);
            return result;
        } else {
            return m1 != null ? m1 : m2;
        }
    }

}
