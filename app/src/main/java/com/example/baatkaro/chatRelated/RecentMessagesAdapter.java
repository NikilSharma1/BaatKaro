package com.example.baatkaro.chatRelated;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baatkaro.R;

import java.util.ArrayList;

public class RecentMessagesAdapter extends RecyclerView.Adapter<RecentMessagesAdapter.ViewHolder> {
    Context context;
    private ArrayList<Message>messageArrayList;
    public RecentMessagesAdapter(Context context,ArrayList<Message>messageArrayList){
        this.context=context;
        this.messageArrayList=messageArrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.recent_users_item_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(messageArrayList.get(position).chat_user_name);
        holder.recentMessage.setText(messageArrayList.get(position).message);
        holder.icon_image.setImageBitmap(getIconImage(messageArrayList.get(position).chat_user_icon));
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView recentMessage;
        private ImageView icon_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.recent_user_name);
            recentMessage=itemView.findViewById(R.id.user_recent_message);
            icon_image=itemView.findViewById(R.id.user_recent_imageview);
        }
    }
    private Bitmap getIconImage(String recent_icon_image){
        byte[] bytes= Base64.decode(recent_icon_image,Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }
}
