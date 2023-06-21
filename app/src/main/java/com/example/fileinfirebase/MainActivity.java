package com.example.fileinfirebase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private ActivityResultLauncher<Intent> launchGalleryForResult;
    private ActivityResultLauncher<Intent> launchCameraForResult;
    private FirebaseService firebaseService;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        createGalleryLauncher();
        createCameraLauncher();
        firebaseService = new FirebaseService();

        Map<String, Bitmap> imagesMap = new LinkedHashMap<>(); // create a LinkedHashMap to store the images

        firebaseService.getAllImages(imageNames -> {
            for (String imageName : imageNames) {
                firebaseService.downloadImage(imageName, bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imagesMap.put(imageName, bitmap); // store the image in the map
                    if (imagesMap.size() == imageNames.size()) { // check if all images have been downloaded
                        runOnUiThread(() -> {
                            for (Bitmap image : imagesMap.values()) { // display the images in order
                                imageView.setImageBitmap(image);
                            }
                        });
                    }
                }, e -> {
                    Log.i("Firebase", "Image download failed: " + e);
                });
            }
        }, e -> {
            Log.i("Firebase", "Image retrieval failed: " + e);
        });
    }



    private void createGalleryLauncher() {
        launchGalleryForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageView.setImageURI(data.getData());
                        }
                    }
                }
        );
    }



    private void createCameraLauncher() {
        launchCameraForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(bitmap);
                        firebaseService.saveImage(getBytes(bitmap));

                    }
                }
        );
    }

    public void cameraBtnPressed(View view) {
        Log.i("Camera Button", "Camera button is Pressed");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launchCameraForResult.launch(intent);
    }

    public void galleryBtnPressed(View view) {
        Log.i("Gallery Button", "Gallery button is Pressed");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        launchGalleryForResult.launch(intent);
    }


    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private void saveToGallery(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        String title = "my_image_title";
        String description = "my_image_description";
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                title,
                description
        );
        Log.i("Firebase", "Image saved to gallery: " + savedImageURL);
    }

}