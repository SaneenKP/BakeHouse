 package com.example.temp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temp.Interfaces.dishValuesInterface;
import com.example.temp.Models.DishDetails;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.dishesHolder> {

    private final Context context;
    private final List<DishDetails> list;
    private final List<String> dishKeyList;
    private final com.example.temp.Interfaces.dishValuesInterface dishValuesInterface;
    private int TOTAL_COUNT = 0;
    private final int[] priceArray;
    private final String[] keyList;
    private final JSONObject dishValues;
    private final JSONObject dishNameAndQuantity;


    public DishesAdapter(Context context, List<DishDetails> list,  List<String> dishKeyList, dishValuesInterface dishValuesInterface) {
        this.context = context;
        this.list = list;
        this.priceArray = new int[list.size()];
        this.keyList = new String[dishKeyList.size()];
        this.dishKeyList = dishKeyList;
        this.dishValuesInterface = dishValuesInterface;
        dishValues = new JSONObject();
        dishNameAndQuantity = new JSONObject();
    }


    @NonNull
    @Override
    public DishesAdapter.dishesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.dish_view , parent , false);
        dishesHolder ds = new dishesHolder(v);

        return ds;
    }

    @Override
    public void onBindViewHolder(@NonNull DishesAdapter.dishesHolder holder, int position) {

        Picasso.get()
                .load(list.get(position).getPic())
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(holder.dishImage);
        holder.dishName.setText(list.get(position).getName());
        holder.price.setText(Integer.toString(list.get(position).getPrice())+" \u20B9");

        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int count = Integer.parseInt(holder.counter.getText().toString());
                if (count != 0)
                    count--;

                holder.counter.setText(count+"");

                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;


                try {
                    dishValues.put(dishKeyList.get(holder.getAdapterPosition()), holder.counter.getText());
                    dishNameAndQuantity.put(list.get(holder.getAdapterPosition()).getName(), holder.counter.getText());
                }catch (Exception e)
                {}

                dishValuesInterface.getCounterValue(priceArray , keyList,dishValues , dishNameAndQuantity);


            }
        });


        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = Integer.parseInt(holder.counter.getText().toString());
                count++;
                holder.counter.setText(Integer.toString(count));

                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;


                try {
                    dishValues.put(dishKeyList.get(holder.getAdapterPosition()), holder.counter.getText());
                    dishNameAndQuantity.put(list.get(holder.getAdapterPosition()).getName(), holder.counter.getText());
                    Log.d("dish values" , dishValues.toString());
                }catch (Exception e)
                {}

                dishValuesInterface.getCounterValue(priceArray , keyList, dishValues , dishNameAndQuantity);

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class dishesHolder extends RecyclerView.ViewHolder {

        AppCompatImageView dishImage;
        MaterialTextView dishName , counter ,  price;
        AppCompatImageButton inc , dec;


        public dishesHolder(@NonNull View itemView) {
            super(itemView);

            dishName = itemView.findViewById(R.id.dish_name);
            dishImage = itemView.findViewById(R.id.dish_image);
            counter = itemView.findViewById(R.id.num);
            inc = itemView.findViewById(R.id.inc);
            dec = itemView.findViewById(R.id.dec);
            price = itemView.findViewById(R.id.price);

        }
    }

}
