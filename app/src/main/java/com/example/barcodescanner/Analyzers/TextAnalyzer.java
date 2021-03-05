package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
import android.graphics.Point;
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
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;

public class TextAnalyzer extends ViewModel implements ImageAnalysis.Analyzer {
    TextRecognizer recognizer ;
    MutableLiveData<String> mutableLiveData;

    public TextAnalyzer(){
        recognizer=TextRecognition.getClient();
        mutableLiveData=new MutableLiveData<>();
    }
    @Override
    public void analyze(@NonNull ImageProxy image) {
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            InputImage images =
                    InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
            Task<Text> result =
                    recognizer.process(images)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {

                                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                                        Log.i("text",block.getLines().toString()+ "   "+block.getText());
                                        mutableLiveData.postValue(block.getText());
                                    }

                                   /* for (Text.TextBlock block : result.getTextBlocks()) {
                                        String blockText = block.getText();
                                        Point[] blockCornerPoints = block.getCornerPoints();
                                        Rect blockFrame = block.getBoundingBox();
                                        for (Text.Line line : block.getLines()) {
                                            String lineText = line.getText();
                                            Point[] lineCornerPoints = line.getCornerPoints();
                                            Rect lineFrame = line.getBoundingBox();
                                            for (Text.Element element : line.getElements()) {
                                                String elementText = element.getText();
                                                Point[] elementCornerPoints = element.getCornerPoints();
                                                Rect elementFrame = element.getBoundingBox();
                                            }
                                        }
                                    }*/
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Text>() {
                        @Override
                        public void onComplete(@NonNull Task<Text> task) {
                            image.close();
                        }
                    });
           // image.close();
        }
    }
    public LiveData<String> getbarcode() {
        return mutableLiveData;
    }
}
