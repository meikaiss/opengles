package com.demo.opengles.world.game;

import android.opengl.GLES20;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

/**
 * 世界坐标系与屏幕的方向关系，当人眼朝向屏幕时，X轴向右，Y轴向上，Z轴垂直于屏幕正方向朝向人眼
 * <p>
 * Created by meikai on 2021/10/16.
 */
public class World {
    private static final String TAG = "World";
    private static final float openglIdentitySize = 1.0f; //opengl归一化坐标系的标准宽高，通常定义为1.0

    private int viewWidth;
    private int viewHeight;
    private float worldSizeScale = 1; //世界坐标系的缩放比例，即世界范围的宽高深是归一化坐标的10倍

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public boolean resetMatrixFlag; //重新计算透视矩阵的标记

    public float eyeX = 0f;
    public float eyeY = -20f;
    public float eyeZ = 2f;
    private float speed = 0.12f; //移动控制器满半径时的世界坐标系移动速度

    private float horizontalAngle = 0; //视线方向在XY平面投影与Y轴的夹角
    private float verticalAngle = 90; //视线方向与Z轴的夹角
    private float directionDelta = 0.5f; //方向控制器每一帧移动的角度
    private float directionRadius = 20f;
    /**
     * 观察方向向量
     * 眼睛位置向量+观察方向向量 = 焦点位置向量
     * <p>
     * 观察方向向量的值域所形成的区域是一个圆柱体，底面圆形的半径为directionRadius，圆柱高度为2*directionRadius
     */
    public float[] direction = {0f, directionRadius, 0f};

    public float scaleFactor = 1.0f;

    public int getWidth() {
        return viewWidth;
    }

    public int getHeight() {
        return viewHeight;
    }

    public float[] getMVPMatrix() {
        return mMVPMatrix;
    }

    public void create() {
        GLES20.glClearColor(0f, 0f, 0f, 1.0f);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    public void change(GL10 gl, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
        resetWorldMatrix();
    }

    public void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (resetMatrixFlag) {
            resetWorldMatrix();
            resetMatrixFlag = false;
        }
    }

    private void resetWorldMatrix() {
        //计算宽高比
        float ratio = (float) viewWidth / viewHeight * openglIdentitySize;
        /**
         * 设置透视投影，用于创建一个锥形视景体
         * 需要注意的是 near 和 far 变量的值必须要大于 0。因为它们都是相对于视点的距离，也就是照相机的距离。
         * 注意：针对透视矩阵，near越小，视觉效果越小；near越大，视觉效果越大。近near近小远大
         *
         * 定义：设置透视投影矩阵，用于定义物理世界中的一个锥形视景体，描述将物理世界中哪一部分范围的景色投影到二维屏幕中。
         * 理解：透视投影中，视点理解为眼睛，近平面理解为屏幕，远平面理解为观察的最远平面，近平面与远平面之间的物体可被投影到近平面（即屏幕）上。视点与近平面的射线在近平面与远平面的部分是四棱锥，这部分四棱锥就是可以显示出的物理世界的物体。视点与近平面越近，则看到的范围越广，则同一个物体在视角范围内的占比就越小，从而投影到近平面上就越小。
         * 奥义：与通常所说的近大远小的结论相反，根本原因在于参照系不同。透视投影矩阵中所说的“近”是指视点与近平面的距离，近平面指的是计算机屏幕。
         * 避坑：在理解透视投影时，暂时不考虑相机矩阵，完全把相机当作固定在原点位置。相机矩阵是在透视投影矩阵的基础上再次包装而得到的算法。
         * 代码：创建方法有两种：frustumM 和 perspectiveM
         */
        Matrix.frustumM(mProjectMatrix, 0,
                -ratio * worldSizeScale, ratio * worldSizeScale,
                -1 * worldSizeScale, 1 * worldSizeScale,
                1f * worldSizeScale, 500 * worldSizeScale);

        /**
         * 理解透视投影矩阵与相机矩阵的关系
         * 1、透视投影矩阵用于创建一个锥形视景体，规则是在世界坐标系中以原点为起点，向Z轴正方向发射四条射线组一个四棱锥，再由near近面与far远面进行切割，剩下一个四棱台即为世界坐标系中可以被此模型显示出的景物。
         * 2、相机矩阵用于定义以何种方式观察视景体。有三个要素：眼睛位置、眼睛观察的焦点位置、眼睛的上方向。这三个要素都会影响眼睛所接收到的图像。
         *   2.1、眼睛位置的影响可以类比现实中，在你面前有一张桌子，你站着看这张桌子和蹲着看这张桌子，你眼睛所看到的桌子的样子肯定是不一样的。
         *   2.2、焦点位置的影响可以这样想像，当你的眼睛盯着这张桌子看时，你眼睛的余光肯定还能观察到桌子四周的景物，只不过这些景物很模糊，但可以肯定的是这张桌子一定出现在你眼睛所观察到的景物的中间位置。但当你抬头看向远方时，你眼睛的焦点定位到远方，此时你眼睛的余光仍然能模糊的看到视野下方存在一张桌子。这种不同就是焦点位置对实际观察结果的影响
         *   2.3、上方向可以这样理解，你站立看这张桌子，和你倒立着但用绳子把你的脚吊起来，使倒立时眼睛位置与站立时眼睛位置相同，这时你大脑所接收到的眼睛的景物肯定是不同的
         */

        float centerX = eyeX + direction[0];
        float centerY = eyeY + direction[1];
        float centerZ = eyeZ + direction[2];

        float upX = 0f;
        float upY = 0f;
        float upZ = 1f;

        /**
         * 设置相机位置
         * 当用视图矩阵确定了照相机的位置时，要确保物体距离视点的位置在 near 和 far 的区间范围内，否则就会看不到物体。
         * 注意：
         * a、针对相机矩阵，视觉效果为近大远小
         * b、up的方向不能与视线方向平行，否则无法预览，视线方向由眼睛和焦点决定
         */
        Matrix.setLookAtM(mViewMatrix, 0,
                eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                upX, upY, upZ);

        if (eyeX - centerX == 0 && eyeY - centerY == 0 && upX == 0 && upY == 0) {
            throw new IllegalStateException("观察方向不能与相机上方向平行，在矩阵数据模型上也不存在这种情况");
        }

        int testFlag = 0;
        if (testFlag == 1) {
            Matrix.setLookAtM(mViewMatrix, 0,
                    0, 0, 20,
                    0, 0, 0,
                    0f, 0f, 1f);
        }

        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        //放大缩小世界
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, scaleFactor, scaleFactor, scaleFactor);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, scaleMatrix, 0);
    }

    /**
     * 通过位置控制器View的触摸位置计算当前眼睛的位置坐标
     *
     * @param viewTouchDeltaX 位置控制器View的触摸位置坐标.x
     * @param viewTouchDeltaY 位置控制器View的触摸位置坐标.y
     */
    public void moveXYChange(int viewTouchDeltaX, int viewTouchDeltaY) {
        float radius = (float) Math.sqrt(Math.pow(viewTouchDeltaX, 2) + Math.pow(viewTouchDeltaY, 2));

        float moveDeltaX = (speed * viewTouchDeltaX / radius);
        float moveDeltaY = (speed * viewTouchDeltaY / radius);


        float realDealX = (float) (moveDeltaY * Math.sin(Math.toRadians(horizontalAngle)) + moveDeltaX * Math.cos(Math.toRadians(horizontalAngle)));
        float realDealY = (float) (moveDeltaY * Math.cos(Math.toRadians(horizontalAngle)) - moveDeltaX * Math.sin(Math.toRadians(horizontalAngle)));
        eyeX += realDealX;
        eyeY += realDealY;

        resetMatrixFlag = true;
    }

    public void moveZChange(int viewTouchZ) {
        eyeZ = viewTouchZ;

        eyeZ = Math.max(0, eyeZ);
        eyeZ = Math.min(100, eyeZ);

        resetMatrixFlag = true;
    }

    /**
     * 通过方向控制器View的触摸位置计算观察方向向量
     *
     * @param viewTouchDeltaX 方向控制器View的触摸位置坐标.x，控制视线在世界坐系xy面的朝向
     * @param viewTouchDeltaY 方向控制器View的触摸位置坐标.y，控制视线在世界坐系与z轴的朝向
     */
    public void directionChange(int viewTouchDeltaX, int viewTouchDeltaY) {
        resetMatrixFlag = true;
        float radius = (float) Math.sqrt(Math.pow(viewTouchDeltaX, 2) + Math.pow(viewTouchDeltaY, 2));

        float directionDeltaHor = (directionDelta * Math.abs(viewTouchDeltaX) / radius);
        horizontalAngle += (directionDeltaHor * (viewTouchDeltaX >= 0 ? 1 : -1));
        horizontalAngle = (horizontalAngle + 360) % 360;//水平方向可以循环观察
        direction[0] = (float) (directionRadius * Math.sin(horizontalAngle / 180 * Math.PI));
        direction[1] = (float) (directionRadius * Math.cos(horizontalAngle / 180 * Math.PI));

        float directionDeltaVer = (directionDelta * Math.abs(viewTouchDeltaY) / radius);
        verticalAngle += (directionDeltaVer * (viewTouchDeltaY > 0 ? -1 : 1));
        verticalAngle = Math.min(180, verticalAngle);
        verticalAngle = Math.max(0, verticalAngle); //竖直方向禁止循环观察
        direction[2] = (float) (directionRadius * Math.cos(verticalAngle / 180 * Math.PI));
    }

    public void onScale(float scaleFactorParam) {
        resetMatrixFlag = true;

        scaleFactor *= scaleFactorParam;
        scaleFactor = Math.max(scaleFactor, 0);
    }

    public void eyeXYZ(float eyeX, float eyeY, float eyeZ) {
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.eyeZ = eyeZ;
        resetMatrixFlag = true;
    }

    public void directionXYZ(float directionX, float directionY, float directionZ) {
        direction = new float[]{directionX, directionY, directionZ};
    }

}