package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.util.List;

public class ObjectDetectionAnalyzer implements ImageAnalysis.Analyzer {
    ObjectDetectorOptions options =
            new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableClassification()  // Optional
                    .build();
    ObjectDetector objectDetector;
    public ObjectDetectionAnalyzer(){
        objectDetector = ObjectDetection.getClient(options);
    }
    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage images =
                    InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            objectDetector.process(images)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<DetectedObject>>() {
                                @Override
                                public void onSuccess(List<DetectedObject> detectedObjects) {
                                    for (DetectedObject detectedObject : detectedObjects) {
                                      //  Rect boundingBox = detectedObject.getBoundingBox();
                                     //   Integer trackingId = detectedObject.getTrackingId();
                                        for (DetectedObject.Label label : detectedObject.getLabels()) {

                                            Log.i("object",label.getText().trim());
                                        }
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                    Log.i("object","fail");
                                }
                            }).addOnCompleteListener(new OnCompleteListener<List<DetectedObject>>() {
                @Override
                public void onComplete(@NonNull Task<List<DetectedObject>> task) {
                    image.close();
                }
            });
        }
        image.close();
    }
}
