package tomerbu.edu.takingphotosfirebase;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class TakePhotoActivity extends AppCompatActivity {

    private static final String BUCKET = "gs://takingphotosfirebase.appspot.com";
    private static final int REQUEST_CODE_IMAGE = 9;
    private static final String PREFS_GOT_IT = "GotIt";
    private Uri photoUri;
    private Typeface tf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showTutorialIfNeeded();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //Sign in to Firebase
        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                initRecycler();
            }
        });
        tf = Typeface.createFromAsset(getAssets(), "elliniaclm-bolditalic-webfont.ttf");
        TextView tvTutorial = (TextView) findViewById(R.id.tvTutorial);
        tvTutorial.setTypeface(tf);

        //changeFonts(getWindow().getDecorView());

    }

    private void showTutorialIfNeeded() {
        SharedPreferences prefs = getSharedPreferences("Tutorial", MODE_PRIVATE);
        boolean showPrefs = prefs.getBoolean(PREFS_GOT_IT, false);
        if (showPrefs){
            findViewById(R.id.tutorial).animate().rotation(360).setDuration(2000);
            findViewById(R.id.tutorial).setVisibility(View.VISIBLE);

        }
    }

    public void saveGotIt(View view) {
        SharedPreferences prefs = getSharedPreferences("Tutorial", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFS_GOT_IT, false);
        editor.commit();

        findViewById(R.id.tutorial).setVisibility(View.GONE);
    }



/*    void changeFonts(View v){
        //If it's a layout... iter over all the children
        if (v instanceof ViewGroup){
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View childView = vg.getChildAt(i);
                changeFonts(childView);
            }
        }
        if (v instanceof TextView){
            TextView tv = (TextView) v;
            tv.setTypeface(tf);
        }
    }*/

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

            // Bitmap b = dataSnapshotArrayList.getExtras().getParcelable("dataSnapshotArrayList");

            //Bundle extras = intent.getExtras();
            // Bitmap bitamp = extras.getParcelable("dataSnapshotArrayList");

            ImageView ivImageCapture = (ImageView) findViewById(R.id.ivImageCapture);
            Picasso.with(this).load(photoUri).rotate(180).into(ivImageCapture);
            saveImageToFirebase();
        }
    }

    private void initRecycler(){
        RecyclerView rvImageRows = (RecyclerView) findViewById(R.id.rvImageRows);
        rvImageRows.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvImageRows.setAdapter(new FireImageAdapter(this));
    }


    private void saveImageToFirebase() {
        String bucket = "gs://takingphotosfirebase.appspot.com";

        final StorageReference sRef = FirebaseStorage.getInstance().
                getReferenceFromUrl(bucket).child("Images")
                .child(photoUri.getLastPathSegment());


        sRef.putFile(photoUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(TakePhotoActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //The Uri is the web Storage Uri.
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().
                                getReference().child("Recipes").
                                child(uid).push();



                        ref.setValue(uri.toString());

                        String key = ref.getKey();
                        Toast.makeText(TakePhotoActivity.this, key, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showGallery(View view) {
        ImageListFragment f = new ImageListFragment();
        f.show(getSupportFragmentManager(), "Dialog");
    }


}
