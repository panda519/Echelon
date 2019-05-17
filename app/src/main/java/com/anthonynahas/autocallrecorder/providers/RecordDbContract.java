package com.anthonynahas.autocallrecorder.providers;

import android.net.Uri;

/**
 * this class implement some structure and values to be used for the content provider
 *
 * @author Anthony Nahas
 * @version 1.0
 * @see "http://www.sqlitetutorial.net/sqlite-where"
 * @since 29.4.16
 */
public class RecordDbContract {
    //db
    public static final String DATABASE_NAME = "Records.db";
    public static String DATABASE_PATH = DATABASE_NAME;
    public static String DATABASE_PATH_SDCARD = "/mnt/sdcard/anahas/"; // TODO: 07.05.17 context.getExternalSotrage... https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir%28java.lang.String%29
    public static final int DATABASE_VERSION = 2;

    /**
     * The authority of the Auto Call Recorder app provider
     */
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "com.anthonynahas.autocallrecorder";
    public static final String PATH = RecordItem.TABLE_NAME;
    public static final String URL = SCHEME + AUTHORITY + "/" + PATH;

    public static final String URI_OFFSET = SCHEME + AUTHORITY + "/" + PATH + "/limit/";

    //Uri.parse("content://" + RecordDbContract.AUTHORITY + "/" +
    //RecordDbContract.RecordItem.TABLE_NAME + "/limit/" + limit);

    /**
     * THe content URI for the top-level BlogItem authority
     */
    public static final Uri CONTENT_URL = Uri.parse(URL);

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd";

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd";


    /**
     * this class implement the structure of a record item in the database
     *
     * @author Anthony Nahas
     * @version 1.2
     * @since 29.4.16
     */
    public static abstract class RecordItem extends RecordDbContract {

        //table
        public static final String TABLE_NAME = "Records";

        //columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_NUMBER = "number";
        public static final String COLUMN_CONTACT_ID = "contactID";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_IS_INCOMING = "isIncoming";
        public static final String COLUMN_IS_LOVE = "isLove";
        public static final String COLUMN_IS_LOCKED = "isLocked";
        public static final String COLUMN_IS_TO_DELETE = "isToDelete";
        public static final String COLUMN_AUDIO_BASE64 = "audioBase64";

        public static String[] ALL_COLUMNS = new String[]
                {
                        COLUMN_ID,
                        COLUMN_PATH,
                        COLUMN_NUMBER,
                        COLUMN_CONTACT_ID,
                        COLUMN_DATE,
                        COLUMN_SIZE,
                        COLUMN_DURATION,
                        COLUMN_IS_INCOMING,
                        COLUMN_IS_LOVE,
                        COLUMN_IS_LOCKED,
                        COLUMN_IS_TO_DELETE,
                        COLUMN_AUDIO_BASE64
                };
    }

    /**
     * Class that extends the properties of a record item which will be stored in the db.
     *
     * @author Anthony Nahas
     * @version 1.0
     * @since 23.05.2017
     */
    public static abstract class Extended {
        //additional custom columns
        public static final String COLUMN_TOTAL_CALLS = "totalCalls";
        public static final String COLUMN_TOTAL_INCOMING_CALLS = "totalIncomingCalls";
        public static final String COLUMN_TOTAL_OUTGOING_CALLS = "totalOutgoingCalls";
    }
}
