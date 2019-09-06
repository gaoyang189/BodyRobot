/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;


public abstract class SerialPortActivity extends AppCompatActivity {

    private static final String TAG = "SerialPortActivity";

    protected SerialApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private ThreadChoose2 mThreadChoose2;
    private ThreadChoose3 mThreadChoose3;
    private  int choosePort = 0;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "ReadThread run: ");
            int size;
            mApplication.changeSerialPort();
            SerialPort mSerialPort1 = null;
            try {
                mSerialPort1 = mApplication.getSerialPort("/dev/ttyS1");
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream mInputStream1 = mSerialPort1.getInputStream();
            while (!isInterrupted()) {
                try {

                    if (mInputStream1 == null) return;
                    byte[] buffer = new byte[64];
                    size = mInputStream1.read(buffer);
                    if (size > 0) {
                        if(choosePort != 1){
                            choosePort(mSerialPort1);
                        }
                        choosePort = 1;
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class ThreadChoose2 extends Thread {

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "ReadThread run: ");
            int size;
            mApplication.changeSerialPort();
            SerialPort mSerialPort2 = null;
            try {
                mSerialPort2 = mApplication.getSerialPort("/dev/ttyS2");
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream mInputStream2 = mSerialPort2.getInputStream();
            while (!isInterrupted()) {
                try {
                    if (mInputStream2 == null) return;
                    byte[] buffer = new byte[64];
                    size = mInputStream2.read(buffer);
                    if (size > 0) {
                        if(choosePort != 2) {
                            choosePort(mSerialPort2);
                        }
                        choosePort = 2;
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ThreadChoose3 extends Thread {

        @Override
        public void run() {
            super.run();
            int size;
            mApplication.changeSerialPort();
            SerialPort mSerialPort3 = null;
            try {
                mSerialPort3 = mApplication.getSerialPort("/dev/ttyS3");
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream mInputStream3 = mSerialPort3.getInputStream();
            Log.d(TAG, "ReadThread run: ");
            while (!isInterrupted()) {
                try {
                    if (mInputStream3 == null) return;
                    byte[] buffer = new byte[64];
                    size = mInputStream3.read(buffer);
                    if (size > 0) {
                        if(choosePort != 3) {
                            choosePort(mSerialPort3);
                        }
                        choosePort = 3;
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = getAppDelegate();
        try {
//            thread1.start();
//            thread2.start();
//            thread3.start();
//            mSerialPort = mApplication.getSerialPort("/dev/ttyS1");
//            Thread.sleep(500);
//            if(mSerialPort.getInputStream().available() == 0){
//                mApplication.changeSerialPort();
//                mSerialPort = mApplication.getSerialPort("/dev/ttyS2");
//                Thread.sleep(500);
//            }
//            if(mSerialPort.getInputStream().available() == 0){
//                mApplication.changeSerialPort();
//                mSerialPort = mApplication.getSerialPort("/dev/ttyS3");
//                Thread.sleep(500);
//            }
//            if(mSerialPort.getInputStream().available() == 0){
//                mApplication.changeSerialPort();
//                mSerialPort = mApplication.getSerialPort("/dev/ttyS1");
//            }
//            Log.d(TAG, "getSerialPort: " + mSerialPort);
//            mOutputStream = mSerialPort.getOutputStream();
//            mInputStream = mSerialPort.getInputStream();
			/* Create a receiving thread */
            mThreadChoose2 = new ThreadChoose2();
            mThreadChoose3 = new ThreadChoose3();
            mThreadChoose2.start();
            mThreadChoose3.start();
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
    }


    private void choosePort(SerialPort mSerialPort){
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
    }
    /**
     * 向串口发数据
     * @param array byte[]
     */
    protected void writeBytes(byte[] array) {
        try {
            if(mOutputStream != null) {
                for(int i=0;i<array.length;i++) {
                    mOutputStream.write(array[i]);

//                    Thread.sleep(10);
                }
                mOutputStream.flush();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    protected abstract SerialApplication getAppDelegate();

    @Override
    protected void onDestroy() {
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mApplication.closeSerialPort();
        super.onDestroy();
    }

    public void hideNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
