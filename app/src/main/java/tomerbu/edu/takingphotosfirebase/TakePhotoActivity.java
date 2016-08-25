package tomerbu.edu.takingphotosfirebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class TakePhotoActivity extends AppCompatActivity {

    private static final String BUCKET = "gs://takingphotosfirebase.appspot.com";
    private static final int REQUEST_CODE_IMAGE = 9;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //Sign in to Firebase
        FirebaseAuth.getInstance().signInAnonymously();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_take_photo, menu);
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

    public void takePhoto(View v) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Request the permission:
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_IMAGE);
            return;
        }

        try {

            //Directory of the pictures:
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //Create the file:
            File tempFile = File.createTempFile("temp", ".jpg", storageDir);

            //In order to share a file, we need the URI From the Provider:
            photoUri = FileProvider.getUriForFile(this,
                    "tomerbu.edu.takingphotosfirebase.fileprovider",
                    tempFile);

            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePhotoIntent, REQUEST_CODE_IMAGE);

        } catch (IOException e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_IMAGE && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            takePhoto(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {

            // Bitmap b = data.getExtras().getParcelable("data");

            //Bundle extras = intent.getExtras();
            // Bitmap bitamp = extras.getParcelable("data");

            ImageView ivImageCapture = (ImageView) findViewById(R.id.ivImageCapture);
            Picasso.with(this).load(photoUri).into(ivImageCapture);
            saveImageToFirebase();
        }
    }

    private void saveImageToFirebase() {
        final StorageReference sRef = FirebaseStorage.getInstance().
                getReferenceFromUrl(BUCKET).
                child("Images").child(photoUri.getLastPathSegment());

        sRef.putFile(photoUri).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                sRef.getDownloadUrl().
                        addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference dbRef = FirebaseDatabase.
                                getInstance().getReference();

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        dbRef.child("Recipies").child(uid).push().setValue(uri.toString());
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double up = taskSnapshot.getBytesTransferred();
                double total = taskSnapshot.getTotalByteCount();
                double pct = up/total*100;
                Log.d("TomerBu", pct+"");
            }
        });
    }
}
