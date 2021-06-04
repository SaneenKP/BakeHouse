package com.example.temp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ServicesViewAdapter extends BaseAdapter {

    private android.content.Context context;
    private LayoutInflater inflater;
    private List<ServiceDetails> services;
    private List<String> serviceKeys;
    private EditServiceInterface serviceInterface;

    public ServicesViewAdapter(Context context, List<ServiceDetails> services, List<String> serviceKeys, EditServiceInterface serviceInterface) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.services = services;
        this.serviceKeys = serviceKeys;
        this.serviceInterface = serviceInterface;
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
        ImageView serviceImage = convertView.findViewById(R.id.service_image);


        serviceName.setText(services.get(position).getName());
        Glide.with(context).load(services.get(position).getCover_pic()).into(serviceImage);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                serviceInterface.editService(services.get(position) , serviceKeys.get(position));
                return true;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceInterface.openVendor(position);
            }
        });


        return convertView;
    }
}
