package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sam_chordas on 11/9/15.
 */
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

  @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

  }

  private GestureDetector gestureDetector;
  private OnItemClickListener listener;

  public interface OnItemClickListener{
    public void onItemClick(View v, int position);
  }

  public RecyclerViewItemClickListener(Context context, OnItemClickListener listener) {
    this.listener = listener;
    gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      @Override public boolean onSingleTapUp(MotionEvent e) {
        return true;
      }
    });
  }

  @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
    View childView = view.findChildViewUnder(e.getX(), e.getY());
    Drawable background = view.getBackground();
    int currentBackgroundColor = Color.TRANSPARENT;
    if (background instanceof ColorDrawable) {
      currentBackgroundColor = ((ColorDrawable) background).getColor();
    }
    if (childView != null) {
      if (e.getAction() == MotionEvent.ACTION_DOWN) {
        childView.setBackgroundColor(Color.GREEN);
      } else if (e.getAction() == MotionEvent.ACTION_UP) {
        childView.setBackgroundColor(currentBackgroundColor);
      }
    }
    if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
      listener.onItemClick(childView, view.getChildPosition(childView));
      return true;
    }
    return false;
  }

  @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }
}
