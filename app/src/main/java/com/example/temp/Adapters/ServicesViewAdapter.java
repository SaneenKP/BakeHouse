package com.example.temp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.example.temp.Models.ServiceDetails;
import com.example.temp.R;

import java.util.List;

public class ServicesViewAdapter extends BaseAdapter {

    private android.content.Context context;
    private LayoutInflater inflater;
    private List<ServiceDetails> services;

    public ServicesViewAdapter(Context context, List<ServiceDetails> services) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.services = services;

    }

    @Override
    public int getCount() {
        return services.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
        public long getItemId(int position) {
        return 0;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.service_view , null);
        TextView serviceName = convertView.findViewById(R.id.service_name);
       // AppCompatImageView serviceImage = convertView.findViewById(R.id.service_image);
        Log.d("Images" , services.get(position).getCover_pic());
        serviceName.setText(services.get(position).getName());
//        Glide.with(context).load(services.get(position).
//                getCover_pic()).into(serviceImage);
        return convertView;
    }
}
