package com.freakydev.ghostcam3;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    private CameraBridgeViewBase mOpenCvCameraView;
    private BackgroundSubtractorMOG2 subMOG2;
    private final static double LEARNING_RATE = 0.01;
    private Mat backgroundFrame;
    private Mat currentFrame;
    private boolean applyEffect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        getPermission();
        OpenCVLoader.initDebug();

        Button startButton = (Button)findViewById(R.id.button_start);
        subMOG2 = Video.createBackgroundSubtractorMOG2();

        startButton.setOnClickListener(view -> {
            applyEffect = true;
            backgroundFrame = currentFrame;
            MOG2Subbing();
        });

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat outputFrame;
                currentFrame = inputFrame.rgba();

                if(applyEffect) {
                    outputFrame = process(currentFrame);
                }else{
                    outputFrame = currentFrame;
                }

                return outputFrame;
            }
        });
    }

    private void MOG2Subbing(){
        subMOG2 = Video.createBackgroundSubtractorMOG2();
        backgroundFrame = new Mat();
        // You can configure some parameters. For example:
        //subMOG2.setDetectShadows(false);
    }

    public Mat process(Mat inputImage) {
        subMOG2.apply(inputImage, backgroundFrame, LEARNING_RATE);
        return backgroundFrame;
    }

    private void getPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            Log.d("GhostCamLog", "camera off");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mOpenCvCameraView.enableView();
        Log.d("GhostCamLog", "camera on");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
            Log.d("GhostCamLog", "camera off");
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }
}