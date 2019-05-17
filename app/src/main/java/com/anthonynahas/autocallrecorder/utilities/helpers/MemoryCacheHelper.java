package com.anthonynahas.autocallrecorder.utilities.helpers;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class that deals with the memory cache to load quickly bitmaps
 * of the profile picture of a contact.
 *
 * @author Anthony Nahas
 * @version 1.0
 * @since 31.03.2017
 */
@Singleton
public class MemoryCacheHelper {

    /************** Memory Cache ***************/
    private LruCache<String, Bitmap> mMemoryCacheForContactsBitmap;
    private Map<String, Long> mMemoryCacheForContactsID;
    private Map<String, String> mMemoryCacheForContactsName;

    @Inject
    public MemoryCacheHelper
            (LruCache<String, Bitmap> mMemoryCacheForContactsBitmap,
             Map<String, Long> mMemoryCacheForContactsID,
             Map<String, String> mMemoryCacheForContactsName) {
        this.mMemoryCacheForContactsBitmap = mMemoryCacheForContactsBitmap;
        this.mMemoryCacheForContactsID = mMemoryCacheForContactsID;
        this.mMemoryCacheForContactsName = mMemoryCacheForContactsName;
    }

//    /**
//     * Initializing the LruCache on runtime
//     */
//    public void init() {
//        final int maxMemorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
//        final int cacheSize = maxMemorySize / 10;
//        mMemoryCacheForContactsBitmap = new LruCache<String, Bitmap>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, Bitmap value) {
//                return value.getByteCount() / 1024;
//            }
//        };
//        mMemoryCacheForContactsName = new HashMap<>(15);
//    }

    /**
     * Get a bitmap of a contact by number from the LruCache
     *
     * @param key - the phone number of a contact
     * @return - the bitmap (avatar profile pic)
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCacheForContactsBitmap.get(key);
    }

    /**
     * Set a bitmap in the LruCache for a specific contact number (key)
     *
     * @param key    - the phone number of a contact
     * @param bitmap - the bitmap (avatar profile pic)
     */
    public void setBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCacheForContactsBitmap.put(key, bitmap);
        }
    }

    /**
     * Load the contact name as string from the hash map
     *
     * @param key - the phone number of a contact
     * @return - the contact name associate to the phone number (key)
     */
    public String getMemoryCacheForContactsName(String key) {
        return mMemoryCacheForContactsName.get(key);
    }

    /**
     * If the phone number is not already stored, store it with the appropriate
     * contact name.
     *
     * @param key   - the phone number of a contact
     * @param value - the contact name associate to the phone number (key)
     */
    public void setContactNameToMemoryCache(String key, String value) {
        if (mMemoryCacheForContactsName.get(key) == null) {
            mMemoryCacheForContactsName.put(key, value);
        }
    }

    /**
     * Load the contact id as long from the hash map
     *
     * @param key - the phone number of a contact
     * @return - the contact name associate to the phone number (key)
     */
    public long getMemoryCacheForContactsID(String key) {
        return mMemoryCacheForContactsID.get(key);
    }

    /**
     * If the phone number is not already stored, store it with the appropriate
     * contact name.
     *
     * @param key   - the phone number of a contact
     * @param value - the contact name associate to the phone number (key)
     */
    public void setContactsIDToMemoryCache(String key, long value) {
        if (mMemoryCacheForContactsID.get(key) == null) {
            mMemoryCacheForContactsID.put(key, value);
        }
    }
}
