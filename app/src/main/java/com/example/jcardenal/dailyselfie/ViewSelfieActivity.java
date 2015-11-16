package com.example.jcardenal.dailyselfie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by jcardenal on 15/11/2015.
 */
public class ViewSelfieActivity extends Activity {

    private static final String TAG = "ViewSelfieActivity";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Arrived at onCreate...");
        setContentView(R.layout.selfie_viewer);
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        Log.d(TAG, "Got intent: "+intent);
        if ((intent != null) && Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType().startsWith("image/")){
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = false;
                Log.d(TAG,"Uri path: "+imageUri.getPath());
                Bitmap image =  BitmapFactory.decodeFile(imageUri.getPath(), bmOptions);
                // Update UI to reflect image being shared
                mImageView.setImageBitmap(image);
            }
        }

    }

}
