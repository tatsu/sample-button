/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.button;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import com.tatsu.things.button.ButtonDriverService;
import com.tatsu.things.led.LedDriverService;

/**
 * Example of using Button driver for toggling a LED.
 */
public class ButtonActivity extends Activity {
    private static final String TAG = ButtonActivity.class.getSimpleName();

    private LedDriverService mLedDriverService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LedDriverService.LedDriverBinder binder = (LedDriverService.LedDriverBinder) service;
            mLedDriverService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLedDriverService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        bindService(new Intent(this, LedDriverService.class), mConnection, Context.BIND_AUTO_CREATE);

        startService(new Intent(this, ButtonDriverService.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            try {
                // Turn on the LED
                mLedDriverService.setLedValue(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            try {
                // Turn off the LED
                mLedDriverService.setLedValue(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        stopService(new Intent(this, ButtonDriverService.class));
        unbindService(mConnection);
    }
}
