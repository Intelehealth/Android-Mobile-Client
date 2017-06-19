package io.intelehealth.client.video_library;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import io.intelehealth.client.R;

public class VideoLibraryActivity extends AppCompatActivity implements VideoLibraryFragment.OnFragmentInteractionListener {

    final String LOG_TAG = VideoLibraryActivity.class.getSimpleName();
    Toolbar mToolbar;


    File rootFile;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_library);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            if (isExternalStorageWritable()) {
                /*
                File[] files = getExternalFilesDirs(null);
                if (files != null || files.length != 0) {
                    rootFile = files[files.length - 1];
                    rootFile = new File(rootFile.getAbsolutePath() + File.separator + "Intelehealth Videos");
                    if (!rootFile.exists()) rootFile.mkdir();
                    openFragment(rootFile.getAbsolutePath());
                     }
                */

                rootFile = getExtVideoStorageDir(this, "Intellehealth Videos");
                openFragment(rootFile.getAbsolutePath());
            }

        } else {
            Toast.makeText(this, "This feature is only available to devices with SD cards.", Toast.LENGTH_LONG).show();
        }


    }

    private void openFragment(String filepath) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment videoFragment = new VideoLibraryFragment();
        fragmentTransaction.replace(R.id.video_fragment_FrameLayout, videoFragment);
        Bundle bundle = new Bundle();
        bundle.putString("FILEPATH", filepath);   //parameters are (key, value).
        videoFragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void openFragmentAddToBackstack(String filepath) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment videoFragment = new VideoLibraryFragment();
        fragmentTransaction.replace(R.id.video_fragment_FrameLayout, videoFragment);
        fragmentTransaction.addToBackStack(null);
        Bundle bundle = new Bundle();
        bundle.putString("FILEPATH", filepath);   //parameters are (key, value).
        videoFragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    /*private void closeFragment(String filepath){
        if(filepath==rootFile.getAbsolutePath()) {
            onBackPressed();
        }
        else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment videoFragment = new VideoLibraryFragment();
            fragmentTransaction.replace(R.id.video_fragment_FrameLayout, videoFragment);
            Bundle bundle = new Bundle();
            bundle.putString("FILEPATH", filepath);   //parameters are (key, value).
            videoFragment.setArguments(bundle);
            fragmentTransaction.commit();
        }
    }*/


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getExtVideoStorageDir(Context context, String folderName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), folderName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }
}



