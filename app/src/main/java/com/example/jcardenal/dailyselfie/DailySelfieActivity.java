package com.example.jcardenal.dailyselfie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailySelfieActivity extends AppCompatActivity {

    private final String TAG = "DailySelfieActivity";

    private final String FILE_NAME = "DailySelfieData";

    static final int REQUEST_TAKE_PHOTO = 1;

    private static final int DAILYSELFIE_NOTIFICATION_ID = 1;

    private PictureAdapter adapter;
    private ListView pictures;

    private Selfie mCurrentSelfie;

    private Intent mNotificationReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;

    private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_selfie);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.dailyselfie_toolbar);
        setSupportActionBar(myToolbar);

        adapter = new PictureAdapter(this);
        pictures= (ListView) findViewById(R.id.listView);
        pictures.setAdapter(adapter);
        pictures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked on position " + position);
                Intent shareIntent = new Intent(DailySelfieActivity.this, ViewSelfieActivity.class);
                shareIntent.setAction(Intent.ACTION_SEND);
                Selfie s = adapter.getItem(position);
                shareIntent.putExtra(Intent.EXTRA_STREAM, s.getURI());
                shareIntent.setType("image/jpeg");
                startActivity(shareIntent);
            }
        });

        new LoadSelfiesTask().execute(this);
        adapter.notifyDataSetChanged();

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(DailySelfieActivity.this,
                AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
               DailySelfieActivity.this, 0, mNotificationReceiverIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
                INITIAL_ALARM_DELAY,
                mNotificationReceiverPendingIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_takepicture:
                // User chose the "Take picture" item...
                dispatchTakePictureIntent();
                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            adapter.add(mCurrentSelfie);
            galleryAddPic();
            adapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected  void onPause() {
        super.onPause();
        new SaveSelfiesTask().execute(this);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentSelfie = new Selfie(timeStamp,image.getAbsolutePath());
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG,"IOException caught!!");
                Log.e(TAG,ex.getMessage());
            }
            // Continue only if the File was successfully created
            Log.d(TAG,"photoFile:"+photoFile);
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentSelfie.getPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void writeToFile() throws FileNotFoundException {

        FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
        int count = adapter.getCount();

        for (int i=0; i<count; i++) {
            pw.println(adapter.getItem(i).toString());
        }

        pw.close();
    }

    private void readFromFile() throws IOException {
        FileInputStream fis = openFileInput(FILE_NAME);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        while ((line = br.readLine()) != null) {
            String components[] = line.split(" ");
            adapter.add(new Selfie(components[0],components[1]));
        }
        br.close();
    }


    class LoadSelfiesTask extends AsyncTask<Context, Integer, String> {

        @Override
        protected String doInBackground(Context... resId) {
            String result = null;
            try {
                readFromFile();
            } catch (IOException ex) {
                Log.e(TAG,ex.getMessage());
                result = ex.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                Toast.makeText(pictures.getContext(),"Error: "+result,Toast.LENGTH_LONG).show();
        }
    }

    class SaveSelfiesTask extends AsyncTask<Context, Integer, String> {

        @Override
        protected String doInBackground(Context... resId) {
            String result = null;

            try {
                writeToFile();
            } catch (FileNotFoundException ex) {
                Log.e(TAG, ex.getMessage());
                result = ex.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                Toast.makeText(pictures.getContext(),"Error: "+result,Toast.LENGTH_LONG).show();
        }
    }
}
