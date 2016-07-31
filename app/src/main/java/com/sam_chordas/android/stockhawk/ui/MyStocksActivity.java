package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHistoData;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private static final String LOG_TAG = MyStocksActivity.class.getSimpleName();
    public static final String DATA_TAG = "historical_data";
  private CharSequence mTitle;
  private Intent mServiceIntent;
  private ItemTouchHelper mItemTouchHelper;
  private static final int CURSOR_LOADER_ID = 0;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;
  private Cursor mCursor;
  private TextView mEmptyView;
    private RecyclerView mRecyclerView;
    private Handler mHandler;
  boolean isConnected;
  public static final String ACTION_DATA_UPDATED =
          "com.sam_chordas.android.stockhawk.ACTION_DATA_UPDATED";
    private static final String DETAILS_TAG = "historical";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;
    ConnectivityManager cm =
        (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    isConnected = activeNetwork != null &&
        activeNetwork.isConnectedOrConnecting();
    setContentView(R.layout.activity_my_stocks);
    // The intent service is for executing immediate pulls from the Yahoo API
    // GCMTaskService can only schedule tasks, they cannot execute immediately
    mServiceIntent = new Intent(this, StockIntentService.class);
      mHandler = new Handler(Looper.getMainLooper()) {
              @Override
              public void handleMessage(Message msg) {
                  decompressAndDisplay(msg.getData().getString(DETAILS_TAG));
              }
      };
    if (savedInstanceState == null){
      // Run the initialize task service so that some stocks appear upon an empty database
      mServiceIntent.putExtra("tag", "init");
      if (isConnected){
        startService(mServiceIntent);
      } else{
        networkToast();
      }
    }
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    if (isConnected) {
                        mCursor.moveToPosition(position);
                        String symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                        // Add the stock to DB
                        mServiceIntent.putExtra("tag", "get");
                        mServiceIntent.putExtra("symbol", symbol);
                        mServiceIntent.putExtra(StockIntentService.EXTRA_MESSENGER, new Messenger(mHandler));
                        startService(mServiceIntent);
                        // Let user know we are in the process of retrieving this stock's
                        // specific details since this might take some time
                        String progressStr = getString(R.string.progress_str, symbol);
                        Toast toast =
                                Toast.makeText(MyStocksActivity.this, progressStr,
                                        Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();
                    } else {
                        networkToast();
                    }
                }
            }));
    mRecyclerView.setAdapter(mCursorAdapter);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      fab.setContentDescription(getString(R.string.a11y_add_button));
    fab.attachToRecyclerView(mRecyclerView);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isConnected){
          new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
              .content(R.string.content_test)
              .inputType(InputType.TYPE_CLASS_TEXT)
              .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                  // On FAB click, receive user input. Make sure the stock doesn't already exist
                  // in the DB and proceed accordingly
                  Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                      new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                      new String[] { input.toString() }, null);
                  if (c.getCount() != 0) {
                    Toast toast =
                        Toast.makeText(MyStocksActivity.this, getString(R.string.already_saved),
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                    toast.show();
                    return;
                  } else {
                    // First check if user inputted anything
                      // If nothing, just exit out w/o wasting
                      // time querying anything
                      if (!input.toString().isEmpty()) {
                          // Add the stock to DB
                          mServiceIntent.putExtra("tag", "add");
                          mServiceIntent.putExtra("symbol", input.toString());
                          startService(mServiceIntent);
                          // Let user know we are in the process of adding the stock
                          // since this might take some time
                          String addStr = getString(R.string.adding_str, input.toString());
                          Toast toast =
                                  Toast.makeText(MyStocksActivity.this, addStr,
                                          Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                          toast.show();
                      }
                  }
                }
              })
              .show();
        } else {
          networkToast();
        }

      }
    });

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(mRecyclerView);

    mTitle = getTitle();
    if (isConnected){
      long period = 3600L;
      long flex = 10L;
      String periodicTag = "periodic";

      // create a periodic task to pull stocks once every hour after the app has been opened. This
      // is so Widget data stays up to date.
      PeriodicTask periodicTask = new PeriodicTask.Builder()
          .setService(StockTaskService.class)
          .setPeriod(period)
          .setFlex(flex)
          .setTag(periodicTag)
          .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
          .setRequiresCharging(false)
          .build();
      // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
      // are updated.
      GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }
  }

    public void decompressAndDisplay(String json) {
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        ArrayList<StockHistoData> listOfResults = new ArrayList<>();
        final String QUERY_TAG = "query";
        final String RESULTS_TAG = "results";
        final String QUOTE_TAG = "quote";
        final String SYMBOL_TAG = "Symbol";
        final String DATE_TAG = "Date";
        final String OPEN_TAG = "Open";
        final String HIGH_TAG = "High";
        final String LOW_TAG = "Low";
        final String CLOSE_TAG = "Close";
        final String VOL_TAG = "Volume";
        final String ADJ_TAG = "Adj_Close";

        try {
            jsonObject = new JSONObject(json);
            if (jsonObject != null && jsonObject.length() != 0){
                jsonObject = jsonObject.getJSONObject(QUERY_TAG);
                if (jsonObject != null && jsonObject.length() != 0){
                    resultsArray = jsonObject.getJSONObject(RESULTS_TAG).getJSONArray(QUOTE_TAG);
                    if (resultsArray != null && resultsArray.length() != 0){
                        for (int i = 0; i < resultsArray.length(); i++){
                            jsonObject = resultsArray.getJSONObject(i);
                            listOfResults.add(new StockHistoData(jsonObject.getString(SYMBOL_TAG),
                                                                 Utils.formatDateString(jsonObject.getString(DATE_TAG)),
                                                                 jsonObject.getString(OPEN_TAG),
                                                                 jsonObject.getString(HIGH_TAG),
                                                                 jsonObject.getString(LOW_TAG),
                                                                 jsonObject.getString(CLOSE_TAG),
                                                                 jsonObject.getString(VOL_TAG),
                                                                 jsonObject.getString(ADJ_TAG)));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        Intent intent = new Intent(this, MyDetailsActivity.class).putExtra(DATA_TAG, listOfResults);
        startActivity(intent);
    }

  @Override
  public void onResume() {
    super.onResume();
      // Recheck for network in case it changed
      ConnectivityManager cm =
              (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      isConnected = activeNetwork != null &&
              activeNetwork.isConnectedOrConnecting();
    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
  }

  public void networkToast(){
    Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
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

    if (id == R.id.action_change_units){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
      updateWidgets();
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateWidgets() {
    Context context = this;
    // Setting the package ensures that only components in our app will receive the broadcast
    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
            .setPackage(context.getPackageName());
    context.sendBroadcast(dataUpdatedIntent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    // This narrows the return to only the stocks that are most current.
    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
        QuoteColumns.ISCURRENT + " = ?",
        new String[]{"1"},
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
      updateEmptyView();
    updateWidgets();
    mCursor = data;
  }

    private void updateEmptyView() {
        mEmptyView = (TextView) findViewById(R.id.recyclerview_empty);
        if (mCursorAdapter.getItemCount() == 0 ||
                !isConnected) {
            if (mEmptyView != null) {
                int message = R.string.no_items;
                if (!isConnected) {
                    message = R.string.network_down;
                }
                mEmptyView.setText(message);
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        } else {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }
}
