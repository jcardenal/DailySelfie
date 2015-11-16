package com.example.jcardenal.dailyselfie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcardenal on 14/11/2015.
 */
public class PictureAdapter extends BaseAdapter {

    private final String TAG = "PictureAdapter";

    private final Context context;
    private final List<Selfie> values = new ArrayList<Selfie>();
    private ImageView imageView;

    public PictureAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.picture_view, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.date);
        imageView = (ImageView) rowView.findViewById(R.id.thumbnail);
        Bitmap scaled = getScaledBitmap(values.get(position).getPath());
        imageView.setImageBitmap(scaled);
        ImageButton button = (ImageButton) rowView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Do you really want to delete this picture?")
                        .setPositiveButton("Oh! Yes!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                delete(position);
                            }
                        })
                        .setNegativeButton("Nah, was kiddin'", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create().show();

            }
        });
        textView.setText(values.get(position).getTitle());
        return rowView;
    }

    @Override
    public Selfie getItem(int id) {
        return values.get(id);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
      return values.size();
    }


    public void add(Selfie pic) {
        values.add(pic);
    }

    public void delete(int pos) {
        Selfie s = values.get(pos);
        File f = new File(s.getPath());
        f.delete();
        values.remove(pos);
        notifyDataSetChanged();
    }

    public void clear() {
        values.clear();
    }

    private Bitmap getScaledBitmap(String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth() ;
        int targetH = imageView.getHeight();

        targetW = targetW >0?targetW:64;
        targetH = targetH >0?targetH:64;


        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

    }

}
