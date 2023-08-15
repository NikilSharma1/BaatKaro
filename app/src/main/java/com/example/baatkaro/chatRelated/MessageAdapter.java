package com.example.baatkaro.chatRelated;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baatkaro.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message>messageArrayList;
    private Context context;
    private String senderId; //this doesn't mean that first person is sender but id here belongs to the person that sent current message
    public MessageAdapter(Context context,ArrayList<Message>arrayList,String senderId){
        this.context=context;
        this.messageArrayList=arrayList;
        this.senderId=senderId;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==1){
            view= LayoutInflater.from(context).inflate(R.layout.sent_message_layout,parent,false);
            return new SentMessageViewHolder(view);
        }
            view= LayoutInflater.from(context).inflate(R.layout.received_message_layout,parent,false);
        return new ReceivedMessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(messageArrayList.get(position).senderId.equals(senderId)){
            return 1; //here it means it is the first person as senderId matches
        }else return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Log.d("info",messageArrayList.get(position).message+" "+messageArrayList.size());
        if(getItemViewType(position)==1){
            //Log.d("info","sender");
            ((SentMessageViewHolder) holder).sentmessage.setText(messageArrayList.get(position).message);
        }else{
            //Log.d("info","receiver");
            ((ReceivedMessageViewHolder)holder).receivedmessage.setText(messageArrayList.get(position).message);
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        public TextView receivedmessage;
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedmessage=itemView.findViewById(R.id.received_message);
        }
    }
    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView sentmessage;
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentmessage=itemView.findViewById(R.id.sent_message);
        }
    }

}
