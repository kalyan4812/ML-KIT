package com.example.barcodescanner.MLKIT;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.barcodescanner.Analyzers.BarcodeAnalyzer;
import com.example.barcodescanner.Analyzers.FaceAnalyzer;
import com.example.barcodescanner.Analyzers.ImageLabelAnalyzer;
import com.example.barcodescanner.Analyzers.ObjectDetectionAnalyzer;
import com.example.barcodescanner.R;
import com.example.barcodescanner.Analyzers.SelfieSegmentation;
import com.example.barcodescanner.Analyzers.TextAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MLActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    PreviewView mPreviewView;
    ImageView captureImage;
    ImageAnalysis imageAnalysis;
    BarcodeAnalyzer barcodeAnalyzer;
    FaceAnalyzer faceAnalyzer;
    TextAnalyzer textAnalyzer;
    ImageLabelAnalyzer imageLabelAnalyzer;
    ObjectDetectionAnalyzer objectDetectionAnalyzer;
    SelfieSegmentation selfieSegmentation;
    Button face, text, label, selfie, barcode, object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        mPreviewView = findViewById(R.id.camera);
        face = findViewById(R.id.face);
        text = findViewById(R.id.text);
        label = findViewById(R.id.imagelabel);
        selfie = findViewById(R.id.selfie);
        barcode = findViewById(R.id.barcode);
        object = findViewById(R.id.object);
        // faceAnalyzer = new FaceAnalyzer();
        textAnalyzer = new TextAnalyzer();
        objectDetectionAnalyzer = new ObjectDetectionAnalyzer();
        selfieSegmentation = new SelfieSegmentation();
        imageLabelAnalyzer = new ImageLabelAnalyzer();
        barcodeAnalyzer = new ViewModelProvider(MLActivity.this).get(BarcodeAnalyzer.class);
        if (barcodeAnalyzer != null) {
            barcodeAnalyzer.getbarcode().observe(MLActivity.this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            });
        }
        faceAnalyzer = new ViewModelProvider(this).get(FaceAnalyzer.class);
        faceAnalyzer.getbarcode().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
        textAnalyzer = new ViewModelProvider(this).get(TextAnalyzer.class);
        textAnalyzer.getbarcode().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
        imageLabelAnalyzer = new ViewModelProvider(this).get(ImageLabelAnalyzer.class);
        imageLabelAnalyzer.getbarcode().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
        objectDetectionAnalyzer.getObjectDetectLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);


        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), faceAnalyzer);
            }
        });
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), textAnalyzer);
            }
        });
        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), imageLabelAnalyzer);
            }
        });
        selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), selfieSegmentation);
            }
        });
        object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), objectDetectionAnalyzer);
            }
        });
        barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysis.clearAnalyzer();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MLActivity.this), barcodeAnalyzer);
            }
        });

    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    public void stop(View view) {
        imageAnalysis.clearAnalyzer();
    }
}