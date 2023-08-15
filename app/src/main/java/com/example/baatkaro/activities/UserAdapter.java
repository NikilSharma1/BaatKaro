package com.example.baatkaro.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.baatkaro.R;
import com.example.baatkaro.chatRelated.PersonalChatActivity;

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
        View view= LayoutInflater.from(context).inflate(R.layout.user_list_item_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(userDataArrayList.get(position).getName());
        holder.email.setText(userDataArrayList.get(position).getEmail());

        //Log.d("info",userDataArrayList.get(position).getImage()+" <- size of arr user adap");
        try{
            byte[] bytes= Base64.decode(userDataArrayList.get(position).getImage(),Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            holder.imageView.setImageBitmap(bitmap);
        }catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, PersonalChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Fields.SELECTED_USER,userDataArrayList.get(position));
                context.startActivity(intent);
            }
        });
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
