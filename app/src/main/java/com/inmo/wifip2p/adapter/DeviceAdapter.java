package com.inmo.wifip2p.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inmo.inmowifip2p.util.WifiP2pUtils;
import com.inmo.wifip2p.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private final List<WifiP2pDevice> wifiP2pDeviceList;
    private Context mContext;

    private OnClickListener clickListener;

    public DeviceAdapter(Context context, List<WifiP2pDevice> wifiP2pDeviceList) {
        this.wifiP2pDeviceList = wifiP2pDeviceList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        view.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick((Integer) v.getTag());
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.tv_deviceName.setText(wifiP2pDeviceList.get(position).deviceName);
        holder.tv_deviceAddress.setText(wifiP2pDeviceList.get(position).deviceAddress);
        holder.tv_deviceDetails.setText(WifiP2pUtils.getDeviceStatus(mContext, wifiP2pDeviceList.get(position).status));
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return wifiP2pDeviceList.size();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface OnClickListener {

        void onItemClick(int position);

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_deviceName;

        private final TextView tv_deviceAddress;

        private final TextView tv_deviceDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tv_deviceName = itemView.findViewById(R.id.tv_deviceName);
            tv_deviceAddress = itemView.findViewById(R.id.tv_deviceAddress);
            tv_deviceDetails = itemView.findViewById(R.id.tv_deviceDetails);
        }

    }

}
