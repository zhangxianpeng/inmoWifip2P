package com.inmo.wifip2p;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.inmo.inmowifip2p.broadcast.DirectBroadcastReceiver;
import com.inmo.inmowifip2p.callback.DirectActionListener;
import com.inmo.inmowifip2p.model.FileTransfer;
import com.inmo.inmowifip2p.service.WifiServerService;

import java.io.File;
import java.util.Collection;

public class ReceiveFileActivity extends BaseActivity {

    private ImageView iv_image;

    private TextView tv_log;

    private ProgressDialog progressDialog;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private boolean connectionInfoAvailable;

    private BroadcastReceiver broadcastReceiver;

    private WifiServerService wifiServerService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiServerService.WifiServerBinder binder = (WifiServerService.WifiServerBinder) service;
            wifiServerService = binder.getService();
            wifiServerService.setProgressChangListener(progressChangListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (wifiServerService != null) {
                wifiServerService.setProgressChangListener(null);
                wifiServerService = null;
            }
            bindService();
        }
    };

    private final DirectActionListener directActionListener = new DirectActionListener() {
        @Override
        public void wifiP2pEnabled(boolean enabled) {
            log("wifiP2pEnabled: " + enabled);
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            log("onConnectionInfoAvailable");
            log("isGroupOwner：" + wifiP2pInfo.isGroupOwner);
            log("groupFormed：" + wifiP2pInfo.groupFormed);
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionInfoAvailable = true;
                if (wifiServerService != null) {
                    startService(WifiServerService.class);
                }
            }
        }

        @Override
        public void onDisconnection() {
            connectionInfoAvailable = false;
            log("onDisconnection");
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            log("onSelfDeviceAvailable");
            log(wifiP2pDevice.toString());
        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            log("onPeersAvailable,size:" + wifiP2pDeviceList.size());
            for (WifiP2pDevice wifiP2pDevice : wifiP2pDeviceList) {
                log(wifiP2pDevice.toString());
            }
        }

        @Override
        public void onChannelDisconnected() {
            log("onChannelDisconnected");
        }
    };

    private final WifiServerService.OnProgressChangListener progressChangListener = new WifiServerService.OnProgressChangListener() {
        @Override
        public void onProgressChanged(final FileTransfer fileTransfer, final int progress) {
            runOnUiThread(() -> {
                progressDialog.setMessage("文件名： " + fileTransfer.getFileName());
                progressDialog.setProgress(progress);
                progressDialog.show();
            });
        }

        @Override
        public void onTransferFinished(final File file) {
            runOnUiThread(() -> {
                progressDialog.cancel();
                if (file != null && file.exists()) {
                    Glide.with(ReceiveFileActivity.this).load(file.getPath()).into(iv_image);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        initView();
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if (wifiP2pManager == null) {
            finish();
            return;
        }
        channel = wifiP2pManager.initialize(this, getMainLooper(), directActionListener);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, directActionListener);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
        bindService();
    }

    private void initView() {
        setTitle("接收文件");
        iv_image = findViewById(R.id.iv_image);
        tv_log = findViewById(R.id.tv_log);
        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(ReceiveFileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            boolean isWifiEnable = checkWifiIsEnable();
            if(!isWifiEnable) {
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, 0);
            }
            removeGroup();
            wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("createGroup onSuccess");
                    dismissLoadingDialog();
                    showToast("createGroup onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    log("createGroup onFailure: " + reason);
                    dismissLoadingDialog();
                    showToast("createGroup onFailure");
                }
            });
        });
        findViewById(R.id.btnRemoveGroup).setOnClickListener(v -> removeGroup());
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("正在接收文件");
        progressDialog.setMax(100);
    }

    private boolean checkWifiIsEnable() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return null != wifiManager && wifiManager.isWifiEnabled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiServerService != null) {
            wifiServerService.setProgressChangListener(null);
            unbindService(serviceConnection);
        }
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(this, WifiServerService.class));
        if (connectionInfoAvailable) {
            removeGroup();
        }
    }

    private void removeGroup() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("removeGroup onSuccess");
                showToast("removeGroup onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                log("removeGroup onFailure");
                showToast("removeGroup onFailure");
            }
        });
    }

    private void log(String log) {
        tv_log.append(log + "\n");
        tv_log.append("Receive----------" + "\n");
    }

    private void bindService() {
        Intent intent = new Intent(ReceiveFileActivity.this, WifiServerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(checkWifiIsEnable()) {
                Log.e("zhangxianpeng","wifi已经打开");
            } else{
                Log.e("zhangxianpeng","wifi还没有打开");
            }
        }
    }
}