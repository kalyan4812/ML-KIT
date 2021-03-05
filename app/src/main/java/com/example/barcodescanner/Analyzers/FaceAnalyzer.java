package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class FaceAnalyzer extends ViewModel implements ImageAnalysis.Analyzer {
    FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();

    FaceDetector detector;
    MutableLiveData<String> mutableLiveData;

    public FaceAnalyzer() {
        detector = FaceDetection.getClient(highAccuracyOpts);
        mutableLiveData=new MutableLiveData<>();
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage images =
                    InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            Task<List<Face>> result =
                    detector.process(images)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<Face>>() {
                                        @Override
                                        public void onSuccess(List<Face> faces) {
                                            // Task completed successfully
                                            // ...
                                            for (Face face : faces) {
                                                Rect bounds = face.getBoundingBox();
                                                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                                                Log.i("face",
                                                         face.getLeftEyeOpenProbability() + " " + face.getRightEyeOpenProbability()
                                                        + "  " + face.getSmilingProbability());
                                                mutableLiveData.postValue("LEFT OPEN : "+face.getLeftEyeOpenProbability() + "\n "
                                                        +"RIGHT EYE OPEN :"+ face.getRightEyeOpenProbability()
                                                        + " \n " +"SMILE :" +face.getSmilingProbability());
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Face>> task) {
                            image.close();
                        }
                    });
        }

    }
    public LiveData<String> getbarcode() {
        return mutableLiveData;
    }
}
