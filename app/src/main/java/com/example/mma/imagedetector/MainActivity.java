package com.example.mma.imagedetector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView textViewResult;
    Button buttonUpload;
    ImageView imageView;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (connectionAvailable()) {

                    new sendToServer().execute();

                } else {
                    textViewResult.setText("no network available");
                }

                Snackbar.make(view, "Sending data...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        textViewResult = (TextView) findViewById(R.id.textViewResult);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        imageView = (ImageView) findViewById(R.id.imageView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void uploadOnClick(View view) {
//
//        if (connectionAvailable()){
//
//            new sendToServer().execute();
//
//        } else {
//            textViewResult.setText("no network available");
//        }
//
//    }


    public void uploadOnClick(View view) {


//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }

        dispatchTakePictureIntent();


    }

    public boolean connectionAvailable(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // connection available
            return true;
        } else {
            return false;
        }
    }

    private class sendToServer extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            if (mCurrentPhotoPath == null){
                return "no Photo captured";
            }

            String result = "Not started.";
            String hostName = "192.168.8.100";
            int portNumber = 10000;


            Socket clientSocket = null;

            // params comes from the execute() call: params[0] is the url.
            try {

                clientSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //out.println("Android robot talking!");

                out.println(b64string());

                result = "done sending message!";

                String receivedMessage = null;

                receivedMessage = in.readLine();
                result = receivedMessage;

//                do {
//                    receivedMessage = in.readLine();
//                    result = receivedMessage;
//
//                } while (receivedMessage != null);



            } catch (IOException e) {
                result = "Failed to send...";
            }

            return result;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(final String result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewResult.setText(result);
                }
            });
        }


    }

    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Image Detector", "error creating image file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.e("Image Detector", "Image saved at: " + mCurrentPhotoPath);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = Environment.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);


        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap() {



        String finalPath = mCurrentPhotoPath.replace("file:","");
        Bitmap bitmap = BitmapFactory.decodeFile(finalPath);

        File imagefile = new File(finalPath);

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(imagefile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        return stream.toByteArray();
    }


    String b64string(){

        // get the base 64 string
        String imgString = Base64.encodeToString(getBytesFromBitmap(), Base64.NO_WRAP);
        Log.v("base 64", imgString);
        return imgString;

    }

}

