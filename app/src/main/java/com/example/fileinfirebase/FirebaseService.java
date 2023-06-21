package com.example.fileinfirebase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FirebaseService {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference root;

    public FirebaseService() {
        root = storage.getReference();
    }

    public void saveImage(byte[] img) {
        if (img == null) {
            Log.i("Firebase", "Image upload failed: Image is null");
            return;
        }
        StorageReference imgRef = root.child(randomPathString());
        UploadTask task = imgRef.putBytes(img);
        task.addOnSuccessListener(taskSnapshot -> {
            Log.i("Firebase", "Image upload successful");
        });
        task.addOnFailureListener(e -> {
            Log.i("Firebase", "Image upload failed: " + e);
        });
    }


    public void downloadImage(String name, OnSuccessListener<byte[]> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference imgRef = root.child(name);
        imgRef.getBytes(1024 * 1024).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }

    public void getAllImages(OnSuccessListener<List<String>> onSuccessListener, OnFailureListener onFailureListener) {
        List<String> list = new ArrayList<>();
        StorageReference imagesRef = root.child("images");
        imagesRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                String name = item.getName();
                if (name != null && !name.isEmpty()) { // add null check and empty check
                    list.add(name);
                }
            }
            onSuccessListener.onSuccess(list);
        }).addOnFailureListener(onFailureListener);
    }




    public String randomPathString() {
        String path = "";
        for (int i = 0; i < 10; i++) {
            path += (char) (Math.random() * 26 + 'a');
        }
        return path + "_" + LocalDate.now().toString() + "_" + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + ".jpg";
    }


}
