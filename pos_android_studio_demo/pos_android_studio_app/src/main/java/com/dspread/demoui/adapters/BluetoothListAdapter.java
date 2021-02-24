package com.dspread.demoui.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.demoui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Time:2020/8/25
 * Author:Qianmeng Chen
 * Description:
 */
public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.ViewHolder> {
    private List<BluetoothDevice> list ;
    private Context context;
    public BluetoothListAdapter(Context context,List<BluetoothDevice> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.bt_qpos_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        BluetoothDevice device = list.get(i);
        if(device.getBondState() == BluetoothDevice.BOND_BONDED){
            viewHolder.m_Icon.setBackgroundResource(R.drawable.bluetooth_blue);
        }else{
            viewHolder.m_Icon.setBackgroundResource(R.drawable.bluetooth_blue_unbond);
        }
        viewHolder.m_TitleName.setText(device.getName() + "(" + device.getAddress() + ")");
        viewHolder.itemView.setTag(device);
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public void addDevice(List<BluetoothDevice> deviceList){
        boolean a = false;
//        if(list.size() > 0) {
//            for (int i = 0; i < list.size(); i++) {
//                if (device.getAddress().equals(list.get(i).getAddress())) {
//                    a = true;
//                    break;
//                } else {
//                    continue;
//                }
//            }
//            if (!a) {
//                list.add(0,device);
//            }
//        }else{
//            list.add(0,device);
//        }
//        this.list.add(0,device);
        this.list = deviceList;
        notifyDataSetChanged();
//        notifyItemInserted(0);
//        notifyItemRangeChanged(0,list.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView m_Icon;
        private TextView m_TitleName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            m_Icon = itemView.findViewById(R.id.item_iv_icon);
            m_TitleName = itemView.findViewById(R.id.item_tv_lable);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            BluetoothDevice device = (BluetoothDevice) itemView.getTag();
            onItemClickListener.onItemClick(device);
        }
    }

    // OnItemClickListener
    private static OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }
}
