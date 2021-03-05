package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.nio.ByteBuffer;

public class SelfieSegmentation implements ImageAnalysis.Analyzer {
    SelfieSegmenterOptions options =
            new SelfieSegmenterOptions.Builder()
                    .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
                    .enableRawSizeMask()
                    .build();
    Segmenter segmenter ;
    public SelfieSegmentation(){
        segmenter = Segmentation.getClient(options);
    }
    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage images =
                    InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            Task<SegmentationMask> result =segmenter.process(images)
                            .addOnSuccessListener(
                                    new OnSuccessListener<SegmentationMask>() {
                                        @Override
                                        public void onSuccess(SegmentationMask segmentationMask) {
                                            ByteBuffer mask = segmentationMask.getBuffer();
                                            int maskWidth = segmentationMask.getWidth();
                                            int maskHeight = segmentationMask.getHeight();

                                            for (int y = 0; y < maskHeight; y++) {
                                                for (int x = 0; x < maskWidth; x++) {
                                                    // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                                    float foregroundConfidence = mask.getFloat();
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
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<SegmentationMask>() {
                        @Override
                        public void onComplete(@NonNull Task<SegmentationMask> task) {
                            image.close();
                        }
                    });
        }
    }
}
