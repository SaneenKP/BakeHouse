package com.example.temp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;


import com.bumptech.glide.Glide;
import com.example.temp.Interfaces.DishValueInterface;
import com.example.temp.Interfaces.EditDishInterface;
import com.example.temp.Models.DishDetails;
import com.example.temp.R;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.dishesHolder> {

    private Context context;
    private List<DishDetails> list;
    private List<String> dishKeyList;
    private DishValueInterface dishValuesInterface;
    private int TOTAL_COUNT = 0;
    private int[] priceArray;
    private String[] keyList;
    private JSONObject dishValues;
    private JSONObject dishNameAndQuantity;
    private EditDishInterface editDishInterface;


    public DishesAdapter(Context context, List<DishDetails> list, List<String> dishKeyList,
                         DishValueInterface dishValuesInterface,
                         EditDishInterface editDishInterface,JSONObject dishNameAndQuantity) {
        this.context = context;
        this.list = list;
        this.keyList = new String[dishKeyList.size()];
        this.dishKeyList = dishKeyList;
        this.dishValuesInterface = dishValuesInterface;
        this.editDishInterface = editDishInterface;
        this.dishValues = new JSONObject();
        this.dishNameAndQuantity=new JSONObject();
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

        this.priceArray = new int[list.size()];
        Picasso.get().
                load(list.get(position).getPic()).
                placeholder(R.drawable.ic_baseline_image_24)
                .into(holder.dishImage);
        holder.dishName.setText(list.get(position).getName());
        holder.price.setText(list.get(position).getPrice()+" \u20B9");

        holder.dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                int count = Integer.parseInt(holder.counter.getText().toString());
                if (count != 0)
                    count--;


                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;


                try {
                    dishValues.put(dishKeyList.get(holder.getAdapterPosition()), holder.counter.getText());
                    dishNameAndQuantity.put(list.get(holder.getAdapterPosition()).getName(), holder.counter.getText());
                }catch (Exception e)
                {}

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dishValuesInterface.getCounterValue(priceArray , keyList,dishValues,dishNameAndQuantity);
                    }
                }).start();
                holder.counter.setText(count+"");


            }
        });


        holder.inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int count = Integer.parseInt(holder.counter.getText().toString());
                count++;

                TOTAL_COUNT = count*list.get(holder.getAdapterPosition()).getPrice();
                priceArray[holder.getAdapterPosition()] = TOTAL_COUNT;


                try {
                    dishValues.put(dishKeyList.get(holder.getAdapterPosition()), holder.counter.getText());
                    dishNameAndQuantity.put(list.get(holder.getAdapterPosition()).getName(), holder.counter.getText());
                }catch (Exception e)
                {}

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dishValuesInterface.getCounterValue(priceArray , keyList, dishValues,dishNameAndQuantity);
                    }
                }).start();
                holder.counter.setText(Integer.toString(count));


            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editDishInterface.editDish(list.get(holder.getAdapterPosition()) , dishKeyList.get(holder.getAdapterPosition()));
                return true;
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
