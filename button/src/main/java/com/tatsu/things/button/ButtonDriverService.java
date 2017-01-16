package com.tatsu.things.button;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.google.android.things.userdriver.InputDriver;
import com.google.android.things.userdriver.UserDriverManager;

import java.io.IOException;

public class ButtonDriverService extends Service {
    private static final String TAG = ButtonDriverService.class.getSimpleName();

    private static final String DRIVER_NAME = "Button";
    private static final int DRIVER_VERSION = 1;

    private Button mDevice;
    private int mKeycode;
    private InputDriver mDriver;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a new driver instance
        try {
            mDevice = new Button(BoardDefaults.getGPIOForButton(),
                    Button.LogicState.PRESSED_WHEN_LOW);
            mDevice.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button b, boolean pressed) {
                    // A state change has occurred
                    triggerEvent(pressed);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        mKeycode = KeyEvent.KEYCODE_SPACE;

        mDriver = InputDriver.builder(InputDevice.SOURCE_CLASS_BUTTON)
                .setName(DRIVER_NAME)
                .setVersion(DRIVER_VERSION)
                .setKeys(new int[]{mKeycode})
                .build();

        // Register with the framework
        UserDriverManager manager = UserDriverManager.getManager();
        manager.registerInputDriver(mDriver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDriver != null) {
            UserDriverManager manager = UserDriverManager.getManager();
            manager.unregisterInputDriver(mDriver);
            mDriver = null;
        }
        if (mDevice != null) {
            try {
                mDevice.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Injects the events into the driver.
     *
     * @param pressed whether the button is pressed ot not.
     */
    private void triggerEvent(boolean pressed) {
        int action = pressed ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP;
        KeyEvent[] events = new KeyEvent[] {new KeyEvent(action, mKeycode)};

        if (!mDriver.emit(events)) {
            Log.w(TAG, "Unable to emit key event");
        }
    }
}
