package com.example.barcodescanner.Analyzers;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class BarcodeAnalyzer extends ViewModel implements ImageAnalysis.Analyzer, LifecycleObserver {
    private BarcodeScanner scanner ;
    private MutableLiveData<String> barcode;

    public BarcodeAnalyzer() {
        barcode = new MutableLiveData<>();
        scanner = BarcodeScanning.getClient( new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_PDF417)
                .build());
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
      //  Log.i("check", "analyzed");
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();

        if (mediaImage != null) {
            InputImage    images = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());


        //    InputImage imagek= InputImage.fromBitmap(Bitmap.createBitmap(images), image.getImageInfo().getRotationDegrees());
            Task<List<Barcode>> result = scanner.process(images)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // Task completed successfully
                            // ...
                            for (Barcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();
                                Log.i("check",rawValue+"  "+valueType);
                                // See API reference for complete list of supported types

                                }

                           // barcode.postValue(barcodes.get(0).getRawValue() + " " + barcodes.get(0).getFormat());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                            Log.i("check","fail");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });
        }
        image.close();

    }

    public LiveData<String> getbarcode() {
        return barcode;
    }
}
