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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
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
        return height +";"+ width;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with writing the file

            } else {
                // Permission denied, handle it accordingly (e.g., show a message to the user)
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        autoExit = true;
        autoExitCounter++;
        String test = getScreenSize();
        Log.d("TEST", test);
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it from the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE
            );
        } else {
            // Permission is already granted, proceed with writing the file
            writeToFileInDocuments(test);
        }
        /** String jsonString;
        try {
            jsonString = getJsonFromString(test);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        assert jsonString != null;
        Log.d("TEST2", jsonString);
        saveJsonToFile(jsonString, "data.txt"); **/




    }

    private void writeToFileInDocuments(String test) {
        String fileName = "liveboot.txt";
        String content = test;

        // Get the Documents directory on external storage
        File documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Create a File object for the specified file in the Documents directory
        File file = new File(documentsDirectory, fileName);

        try {
            // Create a FileOutputStream for the file
            FileOutputStream fos = new FileOutputStream(file);

            // Write the content to the file
            fos.write(content.getBytes());

            // Close the FileOutputStream
            fos.close();

            // Log success
            Log.d("TEST", "File written to Documents directory: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            // Handle IOException if there is an issue writing the file
        }
    }

    private void saveJsonToFile(String jsonString, String fileName) {

        try {
            // Accessing the application's internal storage
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Writing the JSON string to the file
            fos.write(jsonString.getBytes());

            // Closing the file output stream
            fos.close();

            // Optional: Notify the user that the operation was successful

            Toast.makeText(this, "JSON data saved to " + Context.MODE_PRIVATE +" "+ fileName, Toast.LENGTH_SHORT).show();
            Log.d("TEST3",Context.MODE_PRIVATE +""+ fileName );

        } catch (Exception e) {
            e.printStackTrace();
            // Handle any exceptions that may occur during file writing
        }
    }

    private String getJsonFromString(String test) throws JSONException {
        String jsonString;
        try {
            // Creating a JSON object
            JSONObject jsonObject = new JSONObject();
            String[] values = test.split(";");
            // Adding key-value pairs to the JSON object
            jsonObject.put("height", values[0]);
            jsonObject.put("width", values[1]);

            // Converting the JSON object to a string
            jsonString = jsonObject.toString();

            // Printing the JSON string
            Log.d("JSON Example", jsonString);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
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
