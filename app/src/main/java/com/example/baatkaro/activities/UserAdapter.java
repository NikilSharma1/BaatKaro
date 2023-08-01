package com.example.baatkaro.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.baatkaro.R;

import java.util.ArrayList;
import android.util.Base64;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private ArrayList<SingleUserData> userDataArrayList;
    private Context context;
    public UserAdapter(Context context,ArrayList<SingleUserData>arrayList){
        this.context=context;
        this.userDataArrayList=arrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_list_item_view,parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(userDataArrayList.get(position).getName());
        holder.email.setText(userDataArrayList.get(position).getEmail());
        byte[] bytes= Base64.decode(userDataArrayList.get(position).getImage(),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        holder.imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return userDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView name;
        private TextView email;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.userlist_imageview);
            name=itemView.findViewById(R.id.userlist_name);
            email=itemView.findViewById(R.id.userlist_email);
        }
    }
}
