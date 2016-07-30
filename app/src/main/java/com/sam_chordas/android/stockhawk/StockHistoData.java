package com.sam_chordas.android.stockhawk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Richard Mu on 7/25/2016.
 */
public class StockHistoData implements Parcelable {

    private String mSymbol;
    private String mDate;
    private String mOpen;
    private String mHigh;
    private String mLow;
    private String mClose;
    private String mVolume;
    private String mAdj_close;

    public StockHistoData(String symbol, String date, String open, String high,
                          String low, String close, String volume, String adj_close) {
        mSymbol = symbol;
        mDate = date;
        mOpen = open;
        mHigh = high;
        mLow = low;
        mClose = close;
        mVolume = volume;
        mAdj_close = adj_close;
    }

    public String getSym() {
        return mSymbol;
    }

    public String getDate() {
        return mDate;
    }

    public String getOpened() {
        return mOpen;
    }

    public String getHigh() {
        return mHigh;
    }

    public String getLow() {
        return mLow;
    }

    public String getClose() {
        return mClose;
    }

    public String getVolume() {
        return mVolume;
    }

    public String getAdj_close() {
        return mAdj_close;
    }

    public void setSymbol(String symbol) {
        mSymbol = symbol;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setOpen(String open) {
        mOpen = open;
    }

    public void setHigh(String high) {
        mHigh = high;
    }

    public void setLow(String low) {
        mLow = low;
    }

    public void setClose (String close) {
        mClose = close;
    }

    public void setVolume(String volume) {
        mVolume = volume;
    }

    public void setAdj_close(String adj_close) {
        mAdj_close = adj_close;
    }

    // Parcelling part
    public StockHistoData(Parcel in){
        String[] data = new String[8];

        in.readStringArray(data);
        this.mSymbol = data[0];
        this.mDate = data[1];
        this.mOpen = data[2];
        this.mHigh = data[3];
        this.mLow = data[4];
        this.mClose = data[5];
        this.mVolume = data[6];
        this.mAdj_close = data[7];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.mSymbol,
                this.mDate,
                this.mOpen,
                this.mHigh,
                this.mLow,
                this.mClose,
                this.mVolume,
                this.mAdj_close});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public StockHistoData createFromParcel(Parcel in) {
            return new StockHistoData(in);
        }

        public StockHistoData[] newArray(int size) {
            return new StockHistoData[size];
        }
    };
}
