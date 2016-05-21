package com.example.mma.imagedetector;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    TextView textViewResult;
    Button buttonUpload;


    EditText editTextIP;
    EditText editTextPort;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private static final int SELECT_PHOTO = 100;

    String mCurrentPhotoPath;

    CardView card;
    ImageView cardImage;
    TextView cardText;
    //ImageView imageView1;

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    ProgressDialog dialog;

    String result;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        card = (CardView) findViewById(R.id.Card);
        cardImage = (ImageView) findViewById(R.id.cardImage);
        cardText = (TextView) findViewById(R.id.textViewCardText);

        //card.animate();
        card.setVisibility(View.GONE);
        card.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

//                cardText.setText("Dog");
//                cardImage.setImageResource(R.drawable.dog);

                if (result != null && result != "") {


                    Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/" + result));
                    startActivity(browse);
                }


            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCurrentPhotoPath == null || mCurrentPhotoPath == "") {
                    // photo not chosen, so throw error

                    selectSourceDialog();

                    return;
                }

// else if (result != null && result != ""){
//
//                    card.setVisibility(View.GONE);
//                    result = "";
//                    Snackbar.make(findViewById(android.R.id.content), "Once more please", Snackbar.LENGTH_SHORT)
//                            .show();
//
//
//                }


                if (connectionAvailable()) {

                    new sendToServer().execute(editTextIP.getText().toString(), editTextPort.getText().toString());

                    Snackbar.make(findViewById(R.id.clayout), "Sending data...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();


                } else {
                    printConnectionErrorWithWifi();
                    return;
                }


            }
        });


        //textViewResult = (TextView) findViewById(R.id.textViewResult);
        //buttonUpload = (Button) findViewById(R.id.buttonUpload);
        //imageView = (ImageView) findViewById(R.id.imageView);

        editTextIP = (EditText) findViewById(R.id.editTextIP);
        editTextPort = (EditText) findViewById(R.id.editTextPort);


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
        if (id == R.id.action_camera) {
            cameraUpload();
        }

        if (id == R.id.action_gallery) {
            galleryUpload();
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


    public void cameraUpload() {


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

    public boolean connectionAvailable() {
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

    private class sendToServer extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {


            if (mCurrentPhotoPath != null && mCurrentPhotoPath != "") {
                pd = ProgressDialog.show(MainActivity.this, "Uploading Image", "Please wait while we upload the image...");

            }

            //this.pd.show();

        }


        @Override
        protected String doInBackground(String... params) {

            result = "Not started.";
            final String hostName = params[0];
            int portNumber = Integer.parseInt(params[1]);

            // params comes from the execute() call: params[0] is the url.
            try {


                String finalPath = mCurrentPhotoPath.replace("file:", "");
                Bitmap bitmap = BitmapFactory.decodeFile(finalPath);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                byte[] encodedString = stream.toByteArray();

                stream.close();

                String lenByteArray = String.valueOf(encodedString.length);
                Log.d("byte length", lenByteArray);

                int lengthString = encodedString.length;
                boolean endTransmission = false;
                int iteratorS = 0;
                int iteratorT = 0;
                int countData = 16384;
                int times = 0;


                Log.d("Error#", "1");
                Log.d("iteratorS", String.valueOf(iteratorS));

                Log.d("loop", String.valueOf(times));


                Socket socket = new Socket(hostName, portNumber);
                OutputStream out = socket.getOutputStream();
                DataOutputStream oute = new DataOutputStream(out);

                while (true) {
                    iteratorS += countData;
                    times++;
                    Log.d("Error#", "1");
                    Log.d("iteratorS", String.valueOf(iteratorS));

                    Log.d("loop", String.valueOf(times));


                    if ((iteratorS >= lengthString)) {
                        iteratorS = iteratorS - countData;
                        countData = (lengthString - iteratorS);
                        iteratorS += countData;
                        endTransmission = true;


                    }
                    oute.write(encodedString, iteratorT, countData);

                    iteratorT += countData;

                    if (endTransmission == true) {
                        oute.flush();
                        Log.d("Wrote ", "End");
                        Log.d("byte length", lenByteArray);
                        oute.close();

                        break;


                    }


                }

                ServerSocket ss = new ServerSocket(50000);
                Socket clientSoc = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
                result = in.readLine();

                // closing all sockets and streams

                ss.close();
                clientSoc.close();
                in.close();
                socket.close();
                out.close();
                oute.close();

                String bytesTransmitted = String.valueOf(iteratorT);
                Log.d("ImageDetectorApp", "Successfully transmitted: " + bytesTransmitted + " Bytes.");


            } catch (IOException ex) {
                ex.printStackTrace();

                pd.dismiss();

                printConnectionError();

                return "";

            }


//            } catch (Exception ex) {
//
//                ex.printStackTrace();
//                if (ex.getCause() == NullPointerException){
//
//                }
//
//                if (ex.getClass().getName() == "ConnectException"){
//
//                }
//
//            }


            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(final String receivedResult) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (receivedResult != null && receivedResult != "") {

                        result = receivedResult.substring(0, 1).toUpperCase() + receivedResult.substring(1, receivedResult.length()).toLowerCase();

                        //textViewResult.setText(result);
                        pd.dismiss();

                        switch (result) {

                            case "Bird":
                                cardImage.setImageResource(R.drawable.bird);
                                break;

                            case "Dog":
                                cardImage.setImageResource(R.drawable.dog);
                                break;

                            case "Cat":
                                cardImage.setImageResource(R.drawable.cat);
                                break;

                            case "Deer":
                                cardImage.setImageResource(R.drawable.deer);
                                break;

                            case "Frog":
                                cardImage.setImageResource(R.drawable.frog);
                                break;

                            case "Horse":
                                cardImage.setImageResource(R.drawable.horse);
                                break;

                            case "None":
                                cardImage.setImageResource(R.drawable.mysterybox);
                                break;


                            default:
                                break;
                        }

                        if (result == "None") {
                            result = "It's a mystery!";
                        }
                        cardText.setText(result);
                        card.setVisibility(View.VISIBLE);

                        Snackbar.make(fab , "Received result", Snackbar.LENGTH_LONG)
                                .setAction("Clear", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        card.setVisibility(View.GONE);
                                    }
                                })
                                .setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                                .show();

                    }

                }
            });


        }


    }

    public void galleryUpload() {

        // get permissions

        // Assume thisActivity is the current activity

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        SELECT_PHOTO);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);


        }


    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != SELECT_PHOTO) {
            Log.d("Image Detector", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("IMAGE DETECTOR", "EXTERNAL STORAGE permission granted - initialize the camera source");

            // work
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);

            return;
        }

        Log.e("IMAGE DETECTOR", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Image Detector")
                .setMessage("Did not get permission")
                .setPositiveButton("Ok", listener)
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("Image Detector", "Image saved at: " + mCurrentPhotoPath);
            notifyOnScreen();

        }

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            mCurrentPhotoPath = "file:" + getPath(this, selectedImage);
            Log.e("Image Detector", "Image selected: " + getPath(this, selectedImage));
            notifyOnScreen();


        }

    }

    public void notifyOnScreen() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Snackbar.make(findViewById(android.R.id.content), "Ready to send picture", Snackbar.LENGTH_SHORT)
//                        .show();

                Snackbar.make(fab, "Ready to send picture", Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

//        Snackbar.make(findViewById(android.R.id.content), "Ready to send picture", Snackbar.LENGTH_LONG)
//                .show();
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

    public void printConnectionError() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("Network Error")
                        .setMessage("Connection error. Please ensure you are on host's network")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                alertDialog.show();

            }
        });


    }

    public void printConnectionErrorWithWifi() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("Network Error")
                        .setMessage("Connection error. Please ensure you are on host's network")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            }
                        }).create();
                alertDialog.show();

            }
        });

    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void selectSourceDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setTitle("Choose choose a photo source")
                .setItems(R.array.sources, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            galleryUpload();

                        } else if (which == 1) {
                            cameraUpload();
                        }
                    }
                }).create();
        alertDialog.show();


    }



}

