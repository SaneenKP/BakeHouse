package com.example.temp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.temp.Interfaces.EditServiceInterface;
import com.example.temp.Models.ServiceDetails;
import com.example.temp.R;
import java.util.List;

public class ServicesViewAdapter extends BaseAdapter {

    private android.content.Context context;
    private LayoutInflater inflater;
    private List<ServiceDetails> services;
    private EditServiceInterface editServiceInterface;
    private List<String>serviceKeys;

    public ServicesViewAdapter(Context context, List<ServiceDetails> services, List<String> serviceKeys, EditServiceInterface editServiceInterface) {

        inflater = LayoutInflater.from(context);
        this.context = context;
        this.services = services;
        this.editServiceInterface=editServiceInterface;
        this.serviceKeys=serviceKeys;
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
        Log.d("Images" , services.get(position).getCover_pic());
        serviceName.setText(services.get(position).getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editServiceInterface.openVendor(position);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editServiceInterface.editService(services.get(position) , serviceKeys.get(position));
                return true;
            }
        });

        return convertView;
    }
}
