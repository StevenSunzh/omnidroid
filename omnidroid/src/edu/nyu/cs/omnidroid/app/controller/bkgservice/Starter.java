/*******************************************************************************
 * Copyright 2009 Omnidroid - http://code.google.com/p/omnidroid 
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *******************************************************************************/
package edu.nyu.cs.omnidroid.app.controller.bkgservice;

import edu.nyu.cs.omnidroid.app.R;
import edu.nyu.cs.omnidroid.app.controller.OmnidroidManager;
import edu.nyu.cs.omnidroid.app.view.simple.UtilUI;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * This broadcast receiver detect intents including System Boot, OmniStart and OmniRestart to
 * complete necessary operations
 */
public class Starter extends BroadcastReceiver {

  public void onReceive(Context context, Intent intent) {

    if (android.content.Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      // Start the background monitoring service on boot
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      if (sharedPreferences
          .getBoolean(context.getString(R.string.pref_key_omnidroid_enabled), true)) {
        OmnidroidManager.enable(context, true);
      }
      UtilUI.loadNotifications(context);
    } else if ("OmniStart".equals(intent.getAction())) {
      // Start the background monitoring service by request
      OmnidroidManager.enable(context, true);
    } else if ("OmniRestart".equals(intent.getAction())) {
      /*
       * Restart the background monitoring service on request (maybe we loaded new data and need a
       * restart)
       */
      OmnidroidManager.enable(context, false);
      OmnidroidManager.enable(context, true);
    } else if ("OmniStop".equals(intent.getAction())) {
      // Stop the background monitoring services by request
      OmnidroidManager.enable(context, false);
    }
  }

}
