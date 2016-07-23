package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

  private String mDetailsJson;
  private boolean mWasSuccess;
  private Bundle mExtras, mDataToReturn;
  public static final String EXTRA_MESSENGER="com.sam_chordas.android.stockhawk.service.EXTRA_MESSENGER";

  public StockIntentService(){
    super(StockIntentService.class.getName());
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    mExtras = intent.getExtras();
    if (intent.getStringExtra("tag").equals("add") || intent.getStringExtra("tag").equals("get")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    if (stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args)) != GcmNetworkManager.RESULT_SUCCESS) {
      mWasSuccess = false;
    } else {
      //mDetailsJson = stockTaskService.getDetailsJson();
      mWasSuccess = true;
    }
  }

  @Override
  public void onDestroy() {
    if (!mWasSuccess) {
      Toast toast =
              Toast.makeText(this, "Could not find stock quote",
                      Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
      toast.show();
    }
    /*if (mDetailsJson != null) {
      mDataToReturn = new Bundle();
      mDataToReturn.putString("historical", mDetailsJson);
      Messenger messenger=(Messenger)mExtras.get(EXTRA_MESSENGER);
      Message msg=Message.obtain();
      msg.setData(mDataToReturn);
      try {
        messenger.send(msg);
      }
      catch (android.os.RemoteException e1) {
        Log.w(getClass().getName(), "Exception sending message", e1);
      }
    }*/
  }
}
