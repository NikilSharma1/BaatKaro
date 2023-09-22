package com.example.baatkaro.chatRelated;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Filter;
import android.widget.Toast;

import com.example.baatkaro.R;
import com.example.baatkaro.activities.Fields;
import com.example.baatkaro.activities.SingleUserData;
import com.example.baatkaro.activities.UsersListActivity;
import com.example.baatkaro.databinding.ActivityPersonalChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import java.util.HashMap;
import java.util.Locale;

public class PersonalChatActivity extends AppCompatActivity {

    private ActivityPersonalChatBinding binding;
    private SingleUserData user;
    private SharedPreferences sharedPreferences;
    private MessageAdapter messageAdapter;
    private FirebaseFirestore database;
    private ArrayList<Message>messageArrayList;
    private LinearLayoutManager layoutManager;
    private String chat_id=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPersonalChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //let's get the data from the intent that was called from selectuseractivity or recent chat lists
        user=(SingleUserData) getIntent().getSerializableExtra(Fields.SELECTED_USER); //RECEIVER USER i.e person with whom we are chatting

        //let's set the textview with user.name
        binding.userName.setText(user.getName());

        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);
        database=FirebaseFirestore.getInstance();
        clickOnViews();

        messageArrayList=new ArrayList<>();
        layoutManager=(LinearLayoutManager) binding.messageRecyclerview.getLayoutManager();
        layoutManager.setStackFromEnd(true);
        messageAdapter=new MessageAdapter(getApplicationContext(),messageArrayList,sharedPreferences.getString(Fields.USER_ID,null));
        binding.messageRecyclerview.setAdapter(messageAdapter);

        restoreMessages();
    }

    private void clickOnViews(){
        binding.sendImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeMessage();
            }
        });
        binding.backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UsersListActivity.class));
            }
        });
    }
    private void storeMessage(){
        HashMap<String,Object> message =new HashMap<>();
        message.put(Fields.SENDER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null));
        message.put(Fields.RECEIVER_PERSON_ID,user.getId());
        message.put(Fields.MESSAGE,binding.messageEdittext.getText().toString());
        message.put(Fields.TIMESTAMP,new Date());
        database.collection(Fields.COLLECTION_CHAT_NAME)
                .add(message)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Please Resend the message",Toast.LENGTH_SHORT).show();
                    }
                });
        if(chat_id!=null){
            updateRecentChat(binding.messageEdittext.getText().toString());
        }else{
            HashMap<String,Object>recent_message_details=new HashMap<>();
            recent_message_details.put(Fields.SENDER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null));
            recent_message_details.put(Fields.SENDER_NAME,sharedPreferences.getString(Fields.PERSON_NAME,null));
            recent_message_details.put(Fields.SENDER_IMAGE,sharedPreferences.getString(Fields.PERSON_IMAGE,null));
            recent_message_details.put(Fields.RECEIVER_PERSON_ID,user.getId());
            recent_message_details.put(Fields.RECEIVER_NAME,user.getName());
            recent_message_details.put(Fields.RECEIVER_IMAGE,user.getImage());
            recent_message_details.put(Fields.RECENT_MESSAGE,binding.messageEdittext.getText().toString());
            recent_message_details.put(Fields.TIMESTAMP,new Date());
            addRecentChat(recent_message_details);
        }
        binding.messageEdittext.setText(null);
    }
    //this gives the reference from where the eventlistener has to listen to give back desired results
    private void restoreMessages(){
        database.collection(Fields.COLLECTION_CHAT_NAME)
                .whereEqualTo(Fields.SENDER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null))
                .whereEqualTo(Fields.RECEIVER_PERSON_ID,user.getId())
                .addSnapshotListener(eventListener);
        database.collection(Fields.COLLECTION_CHAT_NAME)
                .whereEqualTo(Fields.SENDER_PERSON_ID,user.getId())
                .whereEqualTo(Fields.RECEIVER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null))
                .addSnapshotListener(eventListener);
    }
    //converts Date object into simple date format (i.e human understandable
    //it listens to the changes made in firestore collection and then report backs to the UI
    private final EventListener<QuerySnapshot> eventListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if(error!=null) return;
            if(value!=null){
                int len=messageArrayList.size();
                for(DocumentChange documentChange:value.getDocumentChanges()){
                    if(documentChange.getType()==DocumentChange.Type.ADDED){
                        Message message=new Message();
                        message.senderId=documentChange.getDocument().getString(Fields.SENDER_PERSON_ID);
                        message.receiverId=documentChange.getDocument().getString(Fields.RECEIVER_PERSON_ID);
                        message.message=documentChange.getDocument().getString(Fields.MESSAGE);
                        message.date=documentChange.getDocument().getDate(Fields.TIMESTAMP);
                        message.time=new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(message.date);
                        messageArrayList.add(message);
                    }
                }
                Collections.sort(messageArrayList, new Comparator<Message>() {
                    @Override
                    public int compare(Message t1, Message t2) {
                        return t1.date.compareTo(t2.date);
                    }
                });
                if(len==0){
                    messageAdapter.notifyDataSetChanged();
                }else{
                    messageAdapter.notifyItemRangeInserted(messageArrayList.size(), messageArrayList.size());
                    binding.messageRecyclerview.smoothScrollToPosition(messageArrayList.size()-1);
                }
            }
            if(chat_id==null){
                checkForSpecificChat();
            }
        }
    };
    private final OnCompleteListener<QuerySnapshot> chatOncompletelistener=new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                chat_id=documentSnapshot.getId();
            }
        }
    };
    private void checkForchats(String senderId,String receiverId){
        database.collection(Fields.COLLECTION_RECENT_CHAT)
                .whereEqualTo(Fields.SENDER_PERSON_ID,senderId)
                .whereEqualTo(Fields.RECEIVER_PERSON_ID,receiverId)
                .get()
                .addOnCompleteListener(chatOncompletelistener);
    }
    private void checkForSpecificChat(){
        if(messageArrayList.size()!=0){
            checkForchats(sharedPreferences.getString(Fields.USER_ID,null),user.getId()); //user here is receiver
            checkForchats(user.getId(),sharedPreferences.getString(Fields.USER_ID,null));
        }
    }
    private void addRecentChat(HashMap<String,Object>recent_chat){
        database.collection(Fields.COLLECTION_RECENT_CHAT)
                .add(recent_chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        chat_id=documentReference.getId();
                    }
                });
    }
    private void updateRecentChat(String recent_message){
        DocumentReference documentReference=database.collection(Fields.COLLECTION_RECENT_CHAT).document(chat_id);
        documentReference.update(Fields.RECENT_MESSAGE,recent_message,Fields.TIMESTAMP,new Date());
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), UsersListActivity.class));
        finish();
    }
}