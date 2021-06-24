package com.example.temp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.temp.Interfaces.vendorsCallListenerInterface;
import com.example.temp.Models.VendorDetails;
import com.example.temp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class VendorsListAdapter extends RecyclerView.Adapter<VendorsListAdapter.VendorHolder>{

    private Context context;
    private List<VendorDetails> list;
    private vendorsCallListenerInterface clickListener;

    public VendorsListAdapter(Context context, List<VendorDetails> list , vendorsCallListenerInterface clickListener) {
        this.context = context;
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public VendorsListAdapter.VendorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.vendordetails_view,parent,false);

        return new VendorHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorsListAdapter.VendorHolder holder, int position) {

        holder.name.setText(list.get(position).getName());
        holder.address.setText(list.get(position).getAddress());
        holder.description.setText(list.get(position).getDescription());
        holder.number.setText(list.get(position).getNumber());

        Picasso.get().load(list.get(position).getProfile_pic())
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(holder.coverpic);


        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.getVendorNumber(list.get(position).getNumber());
            }
        });

        holder.number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.getVendorNumber(list.get(position).getNumber());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VendorHolder extends RecyclerView.ViewHolder {

         private ImageView coverpic;
         private MaterialTextView name;
         private MaterialTextView address;
         private MaterialButton number;
         private MaterialTextView description;
         private MaterialButton call;

        public VendorHolder(@NonNull View itemView) {
            super(itemView);

            coverpic = itemView.findViewById(R.id.profile_pic);
            name = itemView.findViewById(R.id.vendor_name);
            address = itemView.findViewById(R.id.vendor_address);
            number = itemView.findViewById(R.id.vendor_ph);
            description = itemView.findViewById(R.id.vendor_description);
            call = itemView.findViewById(R.id.call_button);


        }
    }

}
