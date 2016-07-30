package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Richard Mu on 7/21/2016.
 */
public class MyDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle extras = getIntent().getExtras();
            extras.putParcelableArrayList(MyStocksActivity.DATA_TAG, getIntent().getParcelableArrayListExtra(MyStocksActivity.DATA_TAG));

            MyDetailsFragment fragment = new MyDetailsFragment();
            fragment.setArguments(extras);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_container, fragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }
}
