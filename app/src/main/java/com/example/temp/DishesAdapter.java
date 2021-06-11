 package com.example.temp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.temp.Models.DishDetails;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.dishesHolder> {

    private final Context context;
    private final List<DishDetails> list;
    private final List<String> dishKeyList;
    private final dishValuesInterface dishValuesInterface;
    private int TOTAL_COUNT = 0;
    private final int[] priceArray;
    private final String[] keyList;
    private final JSONObject dishValues;


    public DishesAdapter(Context context, List<DishDetails> list, List<String> dishKeyList, dishValuesInterface dishValuesInterface) {
        this.context = context;
        this.list = list;
        this.priceArray = new int[list.size()];
        this.keyList = new String[dishKeyList.size()];
        this.dishKeyList = dishKeyList;
        this.dishValuesInterface = dishValuesInterface;
        dishValues = new JSONObject();
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

        Log.d("ADAPTER SET", "ADPTER SETTTTt");
        Glide.with(context)
                .load(list.get(position).getPic())
                .centerCrop()
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
                }catch (Exception e)
                {}

                dishValuesInterface.getCounterValue(priceArray , keyList,dishValues);


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

                Log.d("Total count", Integer.toString(TOTAL_COUNT));
                Log.d("price array" , Integer.toString(priceArray[holder.getAdapterPosition()]));

                try {
                    dishValues.put(dishKeyList.get(holder.getAdapterPosition()), holder.counter.getText());
                }catch (Exception e)
                {}



                dishValuesInterface.getCounterValue(priceArray , keyList, dishValues);

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
        MaterialButton inc , dec;


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
