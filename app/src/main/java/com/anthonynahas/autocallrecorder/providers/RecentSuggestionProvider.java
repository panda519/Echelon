package com.anthonynahas.autocallrecorder.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by A on 17.06.16.
 *
 * @author Anthony Nahas
 * @version 1.0
 * @since 17.06.16
 */
@Deprecated
public class RecentSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "RecentSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    //public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public RecentSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
