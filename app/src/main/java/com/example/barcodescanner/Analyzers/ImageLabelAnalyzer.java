package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
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
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class ImageLabelAnalyzer extends ViewModel implements ImageAnalysis.Analyzer {
    ImageLabeler labeler ;
  MutableLiveData<String> mutableLiveData;
    public ImageLabelAnalyzer() {
       labeler= ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(0.6F).build());
       mutableLiveData=new MutableLiveData<>();
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage images =
                    InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            labeler.process(images)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> labels) {
                            for (ImageLabel label : labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                int index = label.getIndex();
                                Log.i("label",text+ "  "+confidence);
                                mutableLiveData.postValue(label.getText());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<ImageLabel>>() {
                @Override
                public void onComplete(@NonNull Task<List<ImageLabel>> task) {
                    image.close();
                }
            });

        }
    }
    public LiveData<String> getbarcode() {
        return mutableLiveData;
    }
}
