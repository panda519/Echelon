package com.anthonynahas.autocallrecorder.utilities.asyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by A on 09.06.16.
 */
public class AudioFileAsyncTask extends AsyncTask<Void,Void,Cursor> {

    private static final String TAG = AudioFileAsyncTask.class.getSimpleName();

    private String mID;
    private Context mContext;

    public AudioFileAsyncTask(String ID, Context context) {
        mID = ID;
        mContext = context;
    }

    @Override
    protected Cursor doInBackground(Void... params) {

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION };

        String selection = MediaStore.Images.Media._ID + " like ? ";
        String [] args =  {mID};

        Cursor audioCursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, selection, args, null);

        if(audioCursor != null){
            Log.d(TAG,"cursor count = " + audioCursor.getCount());
            audioCursor.moveToFirst();
            return audioCursor;
        }

        return null;
    }
}
