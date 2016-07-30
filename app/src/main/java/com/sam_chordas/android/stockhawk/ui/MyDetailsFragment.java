package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.StockHistoData;

import java.util.ArrayList;

/**
 * Created by Richard Mu on 7/21/2016.
 */
public class MyDetailsFragment extends Fragment {

    LineChartView lineChart;
    static final String LABEL = " Price: ";
    TextView symbolLabel, stockSymbol, startLabel, startTrade, endLabel, endTrade, volumeLabel, volume;
    String symbol, startDate, start, endDate, end, vol;

    ArrayList<StockHistoData> stocks = new ArrayList<>();

    public MyDetailsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        stocks = arguments.getParcelableArrayList(MyStocksActivity.DATA_TAG);
        
        View rootView = inflater.inflate(R.layout.activity_line_graph, container, false);
        lineChart = (LineChartView) rootView.findViewById(R.id.linechart);

        symbolLabel = (TextView) rootView.findViewById(R.id.stock_detail_symbol_label);
        stockSymbol = (TextView) rootView.findViewById(R.id.stock_detail_symbol);
        startLabel = (TextView) rootView.findViewById(R.id.stock_detail_first_label);
        startTrade = (TextView) rootView.findViewById(R.id.stock_detail_first);
        endLabel = (TextView) rootView.findViewById(R.id.stock_detail_last_label);
        endTrade = (TextView) rootView.findViewById(R.id.stock_detail_last);
        volumeLabel = (TextView) rootView.findViewById(R.id.stock_detail_volume_label);
        volume = (TextView) rootView.findViewById(R.id.stock_detail_volume);

        symbol = stocks.get(0).getSym();

        stockSymbol.setText(symbol);
        symbolLabel.setContentDescription(getString(R.string.a11y_stock_symbol, symbol));
        stockSymbol.setContentDescription(getString(R.string.a11y_stock_symbol, symbol));

        LineSet dataset = new LineSet();

        int minValue = 0;
        int maxValue = 0;
        for (int i = stocks.size()-1; i >=0; i--) {
            if (i == stocks.size()-1) {
                startDate = stocks.get(i).getDate();
                start = String.format("%.2f", Float.parseFloat(stocks.get(i).getClose()));
                startLabel.setText(startDate + LABEL);
                startLabel.setContentDescription(getString(R.string.a11y_price_start, startDate, start));
                startTrade.setText(start);
                startTrade.setContentDescription(getString(R.string.a11y_price_start, startDate, start));
            }
            if (i == 0) {
                endDate = stocks.get(i).getDate();
                end = String.format("%.2f",Float.parseFloat(stocks.get(i).getClose()));
                vol = stocks.get(i).getVolume();
                endLabel.setText(endDate + LABEL);
                endLabel.setContentDescription(getString(R.string.a11y_price_end, endDate, end));
                endTrade.setText(String.format("%.2f", Float.parseFloat(stocks.get(i).getClose())));
                endTrade.setContentDescription(getString(R.string.a11y_price_end, endDate, end));
                volumeLabel.setContentDescription(getString(R.string.a11y_volume, vol));
                volume.setText(vol);
                volume.setContentDescription(getString(R.string.a11y_volume, vol));
            }
            String label = stocks.get(i).getDate();
            float value = Float.valueOf(stocks.get(i).getClose());
            if (value > maxValue) maxValue = (int) value;
            dataset.addPoint(label, value);
        }

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        lineChart.addData(dataset);

        lineChart.setAxisThickness(5)
                .setAxisColor(Color.DKGRAY)
                .setAxisBorderValues(minValue, (maxValue * 2))
                .setStep(maxValue/5)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXAxis(false)
                .setYAxis(true)
                .setGrid(ChartView.GridType.FULL, 10, 10, gridPaint);

        lineChart.show();

        return rootView;
    }
}
