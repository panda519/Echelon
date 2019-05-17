package com.anthonynahas.autocallrecorder.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.anthonynahas.autocallrecorder.models.Record;
import com.anthonynahas.autocallrecorder.configurations.Constant;
import com.anthonynahas.autocallrecorder.providers.RecordDbContract;
import com.anthonynahas.autocallrecorder.providers.RecordsQueryHandler;
import com.anthonynahas.autocallrecorder.utilities.helpers.ContactHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.DropBoxHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.FileHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.PreferenceHelper;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import org.chalup.microorm.MicroOrm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * IntentService that deals with the record service as well as with the db.
 * The content values will be sent from the record service and forwarded to insert
 * them in the db.
 * Please note that this class will be always launched after that the record service hat
 * stopped!
 *
 * @author Anthony Nahas
 * @version 1.2
 * @since 29.04.16
 */
public class FetchIntentService extends IntentService {

    private static final String TAG = FetchIntentService.class.getSimpleName();

    @Inject
    RecordsQueryHandler mRecordsQueryHandler;

    @Inject
    PreferenceHelper mPreferenceHelper;

    @Inject
    FileHelper mFileHelper;

    @Inject
    ContactHelper mContactHelper;

    @Inject
    Constant mConstant;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchIntentService() {
        super("FetchIntentService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() fetching..");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        Log.d(TAG, "sRecordFile = " + RecordService.sRecordFile);
        //getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(RecordService.sRecordFile)));

        Record record = intent.getParcelableExtra(mConstant.REC_PARC_KEY);

        //String [] projectALL = new String[] { "*" };
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION};


        String whereClause = "("
                + MediaStore.Audio.Media.DISPLAY_NAME
                + " == "
                + record.getDate()
                + mFileHelper.getAudioFileSuffix(mPreferenceHelper.getOutputFormat());
        //String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=? ";
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " like ? ";
        String suffixFile = mFileHelper.getAudioFileSuffix(mPreferenceHelper.getOutputFormat());
        String[] args = {record.getDate() + suffixFile}; // TODO: 09.05.2017 args should be dynamic


        // TODO: 11.05.17 query should be achieved with asyncquery handler
//        Cursor audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                projection, selection, args, null);
//
//        String data;
//        String displayName;
//
//        if (audioCursor != null && audioCursor.moveToFirst()) {

//        Log.d(TAG, "cursor count = " + audioCursor.getCount());
        // TODO: 08.05.17 cursor with try and catch
//            record.set_ID(audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
//        record.setSize(audioCursor.getInt(audioCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
//        record.setDuration(audioCursor.getInt(audioCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
//        data = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//        displayName = audioCursor.getString(audioCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        // do what ever you want here

//            audioCursor.close();

        long contactID = mContactHelper.getContactID(record.getNumber());

        if (contactID != -1) {
            record.setContactID(contactID);
        }

//            getApplicationContext().getContentResolver().insert(RecordDbContract.CONTENT_URL, values);
        Log.d(TAG, record.toString());
        mRecordsQueryHandler.startInsert(0, null, RecordDbContract.CONTENT_URL, new MicroOrm().toContentValues(record));

        if (mPreferenceHelper.canUploadOnDropBox()) {
            Log.d(TAG, "onUpload()");
//            uploadAudioFile(data, displayName);

        }
    }

//    }

    private boolean uploadAudioFile(String filePath, String fileName) {
        DropBoxHelper dropBoxHelper = new DropBoxHelper(this);
        File tmpFile = new File(filePath);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(tmpFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            dropBoxHelper.getApi().putFileOverwrite(DropBoxHelper.getDropboxFileDir() + fileName, fis, tmpFile.length(), null);
            //showToastFromService("File has been uploaded!");
            return true;
        } catch (DropboxUnlinkedException e) {
            Log.e("DbExampleLog", "User has unlinked.");
        } catch (DropboxException e) {
            Log.e("DbExampleLog", "Something went wrong while uploading.");
        }
        //showToastFromService("Error occurred while processing the upload request");
        return false;
    }

    private void showToastFromService(final String msg) {
        Handler h = new Handler(getApplicationContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
