/* Copyright (C) 2011-2022 Jorrit "Chainfire" Jongma
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package eu.chainfire.liveboot;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class MainActivity extends Activity implements InAppPurchases.OnPurchaseListener {
    private Handler handler = new Handler();
    private boolean autoExit = true;
    private int autoExitCounter = 0;
    
    private int getDeviceDefaultOrientation() {
        WindowManager windowManager =  (WindowManager) getSystemService(WINDOW_SERVICE);

        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
            || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
          return Configuration.ORIENTATION_LANDSCAPE;
        } else { 
          return Configuration.ORIENTATION_PORTRAIT;
        }
    }    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(getDeviceDefaultOrientation());
        setContentView(R.layout.activity_main);
    }

    private String getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        height = height + getNavigationBarHeight();
        return height +" "+ width;
    }

    private int getNavigationBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (autoExit) {
            final int counter = autoExitCounter;
            handler.postDelayed(new Runnable() {                
                @Override
                public void run() {
                    if (autoExitCounter == counter)
                        finish();
                }
            }, 60000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        autoExit = true;
        autoExitCounter++;
        String test = getScreenSize();
        Log.d("TEST", test);
    }

    public void setAutoExit(boolean autoExit) {
        this.autoExit = autoExit;
    }

    @Override
    public void onPurchase(InAppPurchases.Order order, InAppPurchases.InAppPurchase iap) {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}
