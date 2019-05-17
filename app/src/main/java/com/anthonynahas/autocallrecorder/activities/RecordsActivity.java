package com.anthonynahas.autocallrecorder.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.anthonynahas.autocallrecorder.R;
import com.anthonynahas.autocallrecorder.activities.deprecated.SettingsActivity;
import com.anthonynahas.autocallrecorder.adapters.RecordsAdapter;
import com.anthonynahas.autocallrecorder.dagger.annotations.android.HandlerToWaitForLoading;
import com.anthonynahas.autocallrecorder.dagger.annotations.keys.activities.RecordsActivityKey;
import com.anthonynahas.autocallrecorder.events.loading.OnLoadingBegin;
import com.anthonynahas.autocallrecorder.events.loading.OnLoadingDone;
import com.anthonynahas.autocallrecorder.events.search.OnQueryChangedEvent;
import com.anthonynahas.autocallrecorder.listeners.SearchListener;
import com.anthonynahas.autocallrecorder.models.Record;
import com.anthonynahas.autocallrecorder.configurations.Constant;
import com.anthonynahas.autocallrecorder.fragments.dialogs.InputDialog;
import com.anthonynahas.autocallrecorder.fragments.dialogs.RecordsDialog;
import com.anthonynahas.autocallrecorder.providers.RecordDbContract;
import com.anthonynahas.autocallrecorder.providers.RecordsContentProvider;
import com.anthonynahas.autocallrecorder.utilities.helpers.ContactHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.DialogHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.FileHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.PreferenceHelper;
import com.anthonynahas.autocallrecorder.utilities.helpers.SQLiteHelper;
import com.anthonynahas.autocallrecorder.utilities.support.ActionModeSupport;
import com.anthonynahas.autocallrecorder.utilities.support.ItemClickSupport;
import com.anthonynahas.autocallrecorder.views.managers.WrapContentLinearLayoutManager;
import com.anthonynahas.ui_animator.sample.SampleMainActivity;
import com.arlib.floatingsearchview.FloatingSearchView;

import org.chalup.microorm.MicroOrm;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

/**
 * Activity that displays only rubbished records from the end user.
 * https://stackoverflow.com/jobs/144551/software-engineer-android-centralway-numbrs?med=clc
 *
 * @author Anthony Nahas
 * @version 1.0
 * @since 01.06.17
 */

public class RecordsActivity extends AppActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ItemClickSupport.OnItemClickListener,
        ItemClickSupport.OnItemLongClickListener,
        FloatingSearchView.OnQueryChangeListener,
        FloatingSearchView.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = RecordsActivity.class.getSimpleName();

    @Inject
    @RecordsActivityKey
    ActionModeSupport mActionModeSupport;

    @Inject
    RecordsAdapter mAdapter;

    @Inject
    SearchListener mSearchListener;

    @Inject
    RecordsDialog mRecordsDialog;

    @Inject
    InputDialog mInputDialog;

    @Inject
    SQLiteHelper mSQLiteHelper;

    @Inject
    PreferenceHelper mPreferenceHelper;

    @Inject
    FileHelper mFileHelper;

    @Inject
    ContactHelper mContactHelper;

    @Inject
    DialogHelper mDialogHelper;

    @Inject
    Constant mConstant;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.floating_search_view)
    FloatingSearchView mSearchView;

    @BindView(R.id.progressbar)
    ProgressBar mProgressBar;

    private SwipeRefreshLayout mSwipeContainer;

    private Toolbar mToolbar;
    private Context mContext;
    private Activity mAppCompatActivity;

    private BroadcastReceiver mBroadcastReceiver;

    private int mLoaderManagerID;
    private Bundle mArguments;

    public enum args {
        title,
        projection,
        selection,
        selectionArguments,
        limit,
        offset
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        ButterKnife.bind(this);

        mContext = this;
        mAppCompatActivity = this;
        String activityTitle = getIntent().getStringExtra(args.title.name());
        mArguments = prepareArguments(activityTitle);
        mLoaderManagerID = 0;


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(activityTitle);
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(mConstant.ACTION_MODE_COUNTER)) {
                    mActionModeSupport.updateToolbarCounter(intent.getBooleanExtra(mConstant.IS_CHECKED_KEY, false));
                }
            }
        };

        // Lookup the swipe container view
//        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
//        mSwipeContainer.setOnRefreshListener(this);
//        // Configure the refreshing colors
//        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        // specify an adapter (see also next example)
//        mAdapter = new RecordsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        mRecyclerView.getItemAnimator().setAddDuration(mConstant.RECYCLER_VIEW_ANIMATION_DELAY);
        mRecyclerView.getItemAnimator().setRemoveDuration(mConstant.RECYCLER_VIEW_ANIMATION_DELAY);
        mRecyclerView.getItemAnimator().setMoveDuration(mConstant.RECYCLER_VIEW_ANIMATION_DELAY);
        mRecyclerView.getItemAnimator().setChangeDuration(mConstant.RECYCLER_VIEW_ANIMATION_DELAY);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(this);
        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(this);

        // TODO: 02.06.17 refresh on scrolling the recyclerview
        mSearchView.setOnQueryChangeListener(mSearchListener.init(mArguments));
        mSearchView.setOnMenuItemClickListener(this);

        mActionModeSupport.setActionBar(getSupportActionBar());
        mActionModeSupport.setToolbar(mToolbar);
        mActionModeSupport.setAdapter(mAdapter);

        refreshCursorLoader();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mBroadcastReceiver, new IntentFilter(mConstant.ACTION_MODE_COUNTER));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActionModeSupport.cancelActionMode();
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, final View v) {
        if (mAdapter.isActionMode()) {
            mActionModeSupport.onClickCheckBox(position, v);
        } else {
            Bundle args = new Bundle();
            args.putParcelable(mConstant.REC_PARC_KEY, mAdapter.getRecordsList().get(position));
            mRecordsDialog.setArguments(args);
            mRecordsDialog.show(getSupportFragmentManager(), RecordsDialog.TAG);
        }
    }

    @Override
    public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
        mActionModeSupport.enterActionMode(position, v);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mActionModeSupport.inflateMenu(getMenuInflater(), menu, R.menu.action_mode_menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (mAdapter.isActionMode()) {
                    cancelActionMode();
                } else {
                    finish();
                }
                return true;

            case R.id.menu_action_delete:
                mAdapter.deleteRecordsSelected();
                cancelActionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isActionMode()) {
            cancelActionMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh() {
        refreshCursorLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (!mProgressBar.isShown()) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        String[] projection = args.getStringArray(RecordsActivity.args.projection.name());
        String selection = args.getString(RecordsActivity.args.selection.name());
        String[] selectionArgs = args.getStringArray(RecordsActivity.args.selectionArguments.name());
        String sort = mPreferenceHelper.getSortSelection()
                + mPreferenceHelper.getSortArrange();
        int limit = args.getInt(RecordsActivity.args.limit.name());
        int offset = args.getInt(RecordsActivity.args.offset.name());

        switch (id) {
            case 0:
                Uri uri = RecordDbContract.CONTENT_URL
                        .buildUpon()
                        .appendQueryParameter(RecordsContentProvider.QUERY_PARAMETER_LIMIT,
                                String.valueOf(limit))
                        .appendQueryParameter(RecordsContentProvider.QUERY_PARAMETER_OFFSET,
                                String.valueOf(offset))
                        .build();
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sort);

            default:
                throw new IllegalArgumentException("no loader id handled!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapData((new MicroOrm().listFromCursor(data, Record.class)));
        mHandlerToWait.postDelayed(new Runnable() {
            @Override
            public void run() {
//                onLoadingMore = false;
                mProgressBar.setVisibility(View.GONE);
//                mSwipeContainer.setRefreshing(false);
            }
        }, 2000);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Perform an action when a menu item is selected from the
     * floating search view
     *
     * @param item the selected item
     */
    @Override
    public void onActionMenuItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_add_demo_record:
                mInputDialog.show(mAppCompatActivity, "Contact ID");
                break;
            case R.id.action_start_sample_animations:
                startActivity(new Intent(getApplicationContext(), SampleMainActivity.class));
                break;
            case R.id.action_sort:
                mDialogHelper.openSortDialog((AppCompatActivity) getApplicationContext());
                break;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueryChangedEvent(OnQueryChangedEvent event) {
        refreshCursorLoader(event.args);
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        Log.d(TAG, "oldQuery = " + oldQuery + " | newQuery = " + newQuery);

        String contactIDsArguments = mSQLiteHelper
                .convertArrayToInOperatorArguments(mContactHelper
                        .getContactIDsByName(mContactHelper
                                .getContactCursorByName(newQuery)));

        String selection = RecordDbContract.RecordItem.COLUMN_NUMBER
                + " LIKE ?"
                + " OR "
                + RecordDbContract.RecordItem.COLUMN_CONTACT_ID
                + " IN "
                + contactIDsArguments;
        //+ "= 682";
        String[] selectionArgs = new String[]{"%" + newQuery + "%"};

        Bundle args = (Bundle) mArguments.clone();
        args.putString(RecordsActivity.args.selection.name(), selection);
        args.putStringArray(RecordsActivity.args.selectionArguments.name(), selectionArgs);

        refreshCursorLoader(args);
    }



    @Deprecated
    private Bundle prepareArguments(String activityTitle) {
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        int limit = 15; //default
        int offset = 0; //default

        Bundle args = new Bundle();

        if (getResources().getString(R.string.title_activity_rubbished_records).equals(activityTitle)) {
            projection = new String[]{"*"};
            selection = RecordDbContract.RecordItem.COLUMN_IS_TO_DELETE + " = 1";
        } else if (getResources().getString(R.string.title_activity_locked_records).equals(activityTitle)) {
            projection = new String[]{"*"};
            //selection = RecordDbContract.RecordItem.COLUMN_IS_LOCKED + " = 1";
        }

        args.putStringArray(RecordsActivity.args.projection.name(), projection);
        args.putString(RecordsActivity.args.selection.name(), selection);
        args.putStringArray(RecordsActivity.args.selectionArguments.name(), selectionArgs);
        args.putInt(RecordsActivity.args.limit.name(), limit);
        args.putInt(RecordsActivity.args.offset.name(), offset);

        return args;

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    protected void onLoadingBegin(OnLoadingBegin event) {
        if (!mProgressBar.isShown()) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    protected void onLoadingDone(OnLoadingDone event) {
        mHandlerToWait.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
//                mSwipeContainer.setRefreshing(false);
            }
        }, 2000);
    }

    private void refreshCursorLoader() {
        getSupportLoaderManager().restartLoader(mLoaderManagerID, mArguments, this);
    }

    private void refreshCursorLoader(Bundle args) {
        android.widget.ProgressBar bar = new android.widget.ProgressBar(this);
        bar.setIndeterminate(true);
        getSupportLoaderManager().restartLoader(mLoaderManagerID, args, this);
    }

    public void refreshCursorLoader(int newOffset) {
        mArguments.putInt(RecordsActivity.args.offset.name(), newOffset);
        getSupportLoaderManager().restartLoader(mLoaderManagerID, mArguments, this);
    }

    private void cancelActionMode() {
        mActionModeSupport.cancelActionMode();
        refreshCursorLoader();
    }
}
