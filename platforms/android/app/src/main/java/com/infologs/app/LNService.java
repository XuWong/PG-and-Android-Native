package com.infologs.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class LNService extends Service {

    String output = "";
    String input = "";
    OutputStream os = null;
    BufferedReader br = null;
    Process process = null;
    String password = "123456";
    String tag = "uln";
    int delay = 10 * 1000;
    String address = null;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LNService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LNService.this;
        }
    }

    private ServiceCallbacks serviceCallbacks;

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Ucot", "service on creat");
//        !!!be careful with this function
//                the folder existence does not mean that the client can be run
//                because the files, e.g., ucot, key, may be damged
        File folder = new File(getFilesDir() + "/.ucot/node");
        if (folder.exists()) {
            delay = 1000;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
//            generate folder
                    File folder = new File(getFilesDir() + "/.ucot/node");
                    if (!folder.exists()) {
                        folder.mkdirs();
                        updateUI("Generate Folder\n");
                    }
//            copy ucot
                    File ucot = new File(getFilesDir() + "/.ucot/ucot");
                    AssetManager am = getApplication().getAssets();
                    if (!ucot.exists()) {
                        updateUI("Copy ucot\n");
                        InputStream is = am.open("ucot");
                        FileOutputStream fos = new FileOutputStream(ucot);
                        byte[] buffer = new byte[1024];
                        int byteCount = 0;
                        while ((byteCount = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, byteCount);
                        }
                        fos.flush();
                        is.close();
                        fos.close();
                        ucot.setExecutable(true, true);
                    }
//            copy genesis
                    File genesis = new File(getFilesDir() + "/.ucot/genesis.json");
                    if (!genesis.exists()) {
                        updateUI("Copy Genesis\n");
                        InputStream is = am.open("genesis.json");
                        FileOutputStream fos = new FileOutputStream(genesis);
                        byte[] buffer = new byte[1024];
                        int byteCount = 0;
                        while ((byteCount = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, byteCount);
                        }
                        fos.flush();
                        is.close();
                        fos.close();
                    }
//            init Blockchain

                    File ucotFile = new File(getFilesDir() + "/.ucot/node/ucot");
                    if (!ucotFile.exists())
                        ucotFile.mkdirs();
                    if (ucotFile.exists() && ucotFile.isDirectory()) {
//                empty folder, generate key
                        if (ucotFile.list().length == 0) {
                            try {
                                updateUI("Init client\n");
                                String rootPath = getFilesDir() + "/.ucot/";
                                Runtime runtime = Runtime.getRuntime();
                                process = runtime.exec(rootPath + "ucot --datadir " + rootPath + "node init " + rootPath + "genesis.json");
                                InputStream is = process.getInputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                os = process.getOutputStream();
                                output = null;
                                while ((output = br.readLine()) != null) {
                                    Log.e("Ucot", output);
                                    updateUI("res:" + output + "\n");
                                    if (output.contains("database=lightchaindata")) {
                                        Log.i(tag, "init database");
                                        try {
                                            if (br != null)
                                                br.close();
                                            if (os != null) {
                                                os.close();
                                                os = null;
                                            }
                                            if (process != null)
                                                process.destroy();
                                            break;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (br != null)
                                        br.close();
                                    if (os != null) {
                                        os.close();
                                        os = null;
                                    }
                                    if (process != null)
                                        process.destroy();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
//            copy static-nodes list
                    File staticNode = new File(getFilesDir() + "/.ucot/node/geth/static-nodes.json");
                    if (!staticNode.exists()) {
                        updateUI("Copy static node \n");
                        InputStream is = am.open("static-nodes.json");
                        FileOutputStream fos = new FileOutputStream(staticNode);
                        byte[] buffer = new byte[1024];
                        int byteCount = 0;
                        while ((byteCount = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, byteCount);
                        }
                        fos.flush();
                        is.close();
                        fos.close();
                    }
//              init account
                    File keyFile = new File(getFilesDir() + "/.ucot/node/keystore");
                    if (!keyFile.exists())
                        keyFile.mkdirs();
                    if (keyFile.exists() && keyFile.isDirectory()) {
//                empty folder, generate key
                        if (keyFile.list().length == 0) {
                            try {
                                updateUI("Init account\n");
                                String rootPath = getFilesDir() + "/.ucot/";
                                Runtime runtime = Runtime.getRuntime();
                                process = runtime.exec(rootPath + "ucot --datadir " + rootPath + "node account new");
                                InputStream is = process.getInputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                os = process.getOutputStream();
                                output = null;
                                while ((output = br.readLine()) != null) {
                                    Log.e("Ucot", output);
                                    updateUI("res:" + output + "\n");
                                    if (output.startsWith("!! Unsupported terminal")) {
                                        try {
                                            os.write((password + "\n").getBytes());
                                            os.flush();
                                            Log.e("Ucot", "input pass");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (output.startsWith("Passphrase")) {
                                        try {
                                            os.write((password + "\n").getBytes());
                                            os.flush();
                                            Log.e("Ucot", "input pass");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (output.startsWith("Address")) {
                                        String address = output.substring(output.indexOf("{") + 1, output.indexOf("}"));
                                        SharedPreferences.Editor spe = getSharedPreferences(getString(R.string.sp_name), MODE_PRIVATE).edit();
                                        spe.putString(getString(R.string.sp_address), address);
                                        spe.apply();

                                        Log.i(tag, "obtain address");
                                        try {
                                            if (br != null)
                                                br.close();
                                            if (os != null) {
                                                os.close();
                                                os = null;
                                            }
                                            if (process != null)
                                                process.destroy();
                                            break;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                }
//                    try {
//                        process.waitFor();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (br != null)
                                        br.close();
                                    if (os != null) {
                                        os.close();
                                        os = null;
                                    }
                                    if (process != null)
                                        process.destroy();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                }
                catch (Exception e){e.printStackTrace();}
                SharedPreferences sp = getSharedPreferences(getString(R.string.sp_name), MODE_PRIVATE);
                address = sp.getString(getString(R.string.sp_address), null);
                input = getFilesDir() + "/.ucot/ucot --datadir " + getFilesDir() + "/.ucot/node --networkid 15 --port 30604 --rpc --rpcport 3333 --ipcdisable --nodiscover --unlock " + address + " --syncmode light console";
                Log.i("Ucot", input);
                updateUI("res:" + output + "\n");
                try {
                    Runtime runtime = Runtime.getRuntime();
                    process = runtime.exec(input);
                    br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    os = process.getOutputStream();
                    output = null;
                    while ((output = br.readLine()) != null) {
                        Log.e("Ucot", output);
                        updateUI(output);
                    }
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null)
                            br.close();
                        if (os != null) {
                            os.close();
                            os = null;
                        }
                        if (process != null)
                            process.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        Timer timerPass= new Timer();
        timerPass.schedule(new TimerTask() {
            @Override
            public void run() {
                sendCommand(password);
            }
        },delay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(tag, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(tag, "onBind() executed");
        return mBinder;
    }

    private void updateUI(String content) {
        Log.i(tag, "Service update ui by callBack: " + content);
        if (serviceCallbacks != null) {
            serviceCallbacks.callBack(content);
        }
    }

    public void sendCommand(String content) {

        Log.i("Ucot", "try to send command: " + content);
        if (os != null) {
            try {
//                updateUI("send Command: " + content + "\n");
                os.write((content + "\n").getBytes());
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i(tag, "Light client is not ready yet.");
//            Toast.makeText(getApplicationContext(), "Light client is not ready in service.", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
