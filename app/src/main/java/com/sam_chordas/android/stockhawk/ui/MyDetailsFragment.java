package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Richard Mu on 7/21/2016.
 */
public class MyDetailsFragment extends Fragment {

    public MyDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        Log.v("DEBUG", "arguments is " + arguments.getString("historical"));

        View rootView = inflater.inflate(R.layout.activity_line_graph, container, false);
        return rootView;
    }
}
