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

    /**
     * 根据模型矩阵和视图矩阵计算法线矩阵
     *
     * @param modelMatrix 4x4的矩阵
     * @param viewMatrix  4x4的矩阵
     * @return 法线矩阵
     */
    public static float[] invertTransposeMatrix(float[] modelMatrix, float[] viewMatrix) {
        float[] modelViewMatrix = new float[16];
        Matrix.multiplyMM(modelViewMatrix, 0, modelMatrix, 0, viewMatrix, 0);

        return invertTransposeMatrix(modelViewMatrix);
    }


    /**
     * @param matrix 4x4矩阵
     * @return 法线矩阵
     */
    public static float[] invertTransposeMatrix(float[] matrix) {
        float[] invertMatrix = new float[16];
        Matrix.invertM(invertMatrix, 0, matrix, 0);

        float[] transposeMatrix = new float[16];
        Matrix.transposeM(transposeMatrix, 0, invertMatrix, 0);

        return transposeMatrix;
    }

}
