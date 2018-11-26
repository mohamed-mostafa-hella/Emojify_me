package com.example.hoomo.emojify_advanced;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private Bitmap mResultsBitmap;
    private String savedpas;
    private String mCurrentPhotoPath;
    private boolean saved=false;
    private boolean imgvis=false;
    private Button eMojjifyButton;
    private Button load_me;
    private ImageView mImageView;
    private TextView mTitleTextView;
    private FloatingActionButton mSaveFab , mShareFab , mClearFab ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize the value of the view to be able set Visibility of them
        eMojjifyButton=findViewById(R.id.emojify_button);
        load_me=findViewById(R.id.load_img);
        mTitleTextView=findViewById(R.id.title_text_view);
        mShareFab=findViewById(R.id.share_button);
        mClearFab=findViewById(R.id.clear_button);
        mSaveFab=findViewById(R.id.save_button);
        mImageView=findViewById(R.id.image_view);
        //give the mResultsBitmap initial value
        mResultsBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.frown);
    }

    //initial intent to open the gallery to chose image and the result get in onActivityResult
    public void load_me(View view) {
        //the intent
        Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
        //the type of the intent yo load only the image from the gallery
        gallery.setType("image/*");
        //request the the result of the intent (grt intent  to start)
        startActivityForResult(gallery, RESULT_LOAD_IMAGE);
    }

    /////////////////////////////////////////////////

    //check if the permission of write in the storaae id granted if not requst it else launch the camera
    public void emojifyMe(View view) {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ){

            //request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);

        }else{
            launchCamera();
        }
    }

    //create intent to open the camera ant give it a bath to save the image on it to get the full reslution
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //get the uri of the file
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                //but it in the intent to save the photo in it
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //start the intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //create the temporary file to take the photo in it
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //get the taken bitMap which is in the temporary file
    private Bitmap getimagefrombath(){
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

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

    //delete the temporary file of the image
    private void delete(){
        File file = new File(mCurrentPhotoPath);
        file.delete();
    }

/////////////////////////////////////////////////////////

    //take the bitMap and process it
    private void pocessAndSetImage (Bitmap setbetmaap) {
        //change the state from home to show the image and the float button
        eMojjifyButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        load_me.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.VISIBLE);
        mClearFab.setVisibility(View.VISIBLE);
        imgvis=true;

        //instance from Emojifier class
        Emojifier myEmojifier = new Emojifier();

        //crate the mResultsBitmap and process the bitMap
        mResultsBitmap=setbetmaap;
        mResultsBitmap =myEmojifier.detectFaces1(this,mResultsBitmap);
        //set bitMap in the image view
        mImageView.setImageBitmap(mResultsBitmap);
    }

    //return the app to the home (default state)
    public void clearImage(View view) {
        mImageView.setImageResource(0);
        eMojjifyButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
        load_me.setVisibility(View.VISIBLE);
        mSaveFab.setVisibility(View.GONE);
        mShareFab.setVisibility(View.GONE);
        mClearFab.setVisibility(View.GONE);
        saved=false;
        imgvis=false;
    }

    //save the image im the storage if it is not saved
    public void saveMe(View view) {
        if(!saved){
            //get the path of where the image saved
            savedpas=BitmapUtils.saveImage(this,mResultsBitmap);
            saved=true;
        }
    }

    //share the image but after save it in the external storage
    public void shareMe(View view) {
        if(!saved)saveMe(null);
        BitmapUtils.shareImage(this,savedpas);
    }
/////////////////////////////////////////////////////////
    //save instance to save the var during stop like landScape
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //put the variable in the bundle to save it to restore in when start with old state
        outState.putBoolean("imgvis",imgvis);
        outState.putBoolean("saved",saved);
        outState.putString("savedpas",savedpas);
        outState.putString("mCurrentPhotoPath",mCurrentPhotoPath);

        //convert bitMap to byteArray to but in bundle
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mResultsBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] mbyteArray = stream.toByteArray();
        //but the converted byteArray in the bundle
        outState.putByteArray("mResultsBitmap",mbyteArray);

    }

    //restore the saved instance if there is an state saved
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //restore the value of the variable that store before
        imgvis=savedInstanceState.getBoolean("imgvis");
        saved=savedInstanceState.getBoolean("saved");
        savedpas=savedInstanceState.getString("savedpas");
        mCurrentPhotoPath=savedInstanceState.getString("mCurrentPhotoPath");

        //get the byte array that saved in saveInstant which was the resalt bitMap
        byte[] mm = savedInstanceState.getByteArray("mResultsBitmap");
        //convert the byteArray to bitMap ant set it
        mResultsBitmap = BitmapFactory.decodeByteArray(mm, 0, mm.length);

        // if the old view was display the image it restore it else it be the default state the home one
        if(imgvis){
            eMojjifyButton.setVisibility(View.GONE);
            mTitleTextView.setVisibility(View.GONE);
            load_me.setVisibility(View.GONE);
            mSaveFab.setVisibility(View.VISIBLE);
            mShareFab.setVisibility(View.VISIBLE);
            mClearFab.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(mResultsBitmap);
        }

    }

    //to give the permission if it is not the request result came here
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //permission to write in the external storage to create the temporary file
        //if there is a granted permission launch the camera else Toast permission denied
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            launchCamera();
        }else {
            Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    //result of calling intent came here with it is requestCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ){
            //image was taken by the camera
            //getimagefrombath return bitMap of taken img and send it broses
            pocessAndSetImage(getimagefrombath());

            //delete the temporary file which created to take photo int it
            delete();
        }else if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            //load image from galary
            //the data is in the extase of the data intent get the bitMap that include the image and send it to process
            if(data.getData()==null){
                pocessAndSetImage( (Bitmap)data.getExtras().get("data") );
            }else{
                try {
                    pocessAndSetImage ( MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData())  );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //when back button is clicked
    @Override
    public void onBackPressed() {
        if ( imgvis ) {
            //if image is visible clear
            clearImage(null);
        } else {
            //else do the default back press
            super.onBackPressed();
        }
    }
}
