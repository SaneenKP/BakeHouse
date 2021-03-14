package com.example.temp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ServicesViewAdapter extends BaseAdapter {

    private android.content.Context context;
    private LayoutInflater inflater;
    private String[] services;
    private int[] serviceImages;

    public ServicesViewAdapter(Context context, String[] services, int[] serviceImages) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.services = services;
        this.serviceImages = serviceImages;
    }

    @Override
    public int getCount() {
        return services.length;
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
        ImageView serviceImage = convertView.findViewById(R.id.service_image);

        serviceName.setText(services[position]);
        serviceImage.setImageResource(serviceImages[position]);
        return convertView;
    }
}
