package com.inmo.inmowifip2p.util;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.p2p.WifiP2pDevice;

import com.inmo.inmowifip2p.R;

public class WifiP2pUtils {

    public static String getDeviceStatus(Context context, int deviceStatus) {
        Resources res = context.getResources();
        String[] stringArray = res.getStringArray(R.array.wifi_p2p_status);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return stringArray[0];
            case WifiP2pDevice.INVITED:
                return stringArray[1];
            case WifiP2pDevice.CONNECTED:
                return stringArray[2];
            case WifiP2pDevice.FAILED:
                return stringArray[3];
            case WifiP2pDevice.UNAVAILABLE:
                return stringArray[4];
            default:
                return stringArray[5];
        }
    }

}
