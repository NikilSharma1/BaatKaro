package com.example.baatkaro.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.baatkaro.R;
import com.example.baatkaro.chatRelated.Message;
import com.example.baatkaro.chatRelated.PersonalChatActivity;
import com.example.baatkaro.chatRelated.RecentChatListener;
import com.example.baatkaro.chatRelated.RecentMessagesAdapter;
import com.example.baatkaro.databinding.ActivityUsersListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.Constants;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class UsersListActivity extends AppCompatActivity implements RecentChatListener {

    private ActivityUsersListBinding binding;
    private Toolbar toolbar;

    private SharedPreferences sharedPreferences;
    private ArrayList<Message>recent_chats_arraylist;
    private FirebaseFirestore database;
    private RecentMessagesAdapter recentMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar= binding.getRoot().findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);
        database=FirebaseFirestore.getInstance();
        recent_chats_arraylist=new ArrayList<>();
        recentMessagesAdapter=new RecentMessagesAdapter(getApplicationContext(),recent_chats_arraylist,this);
        binding.recentUsers.setAdapter(recentMessagesAdapter);

        //binding.navigationView.getMenu().findItem(R.id.name).setTitle(sharedPreferences.getString(Fields.PERSON_NAME,null));
        setNavContent(binding.navigationView);
        getTokens(); //get tokens from gcm needed for messaging
        setListners();

    }

    private void setListners(){
        binding.floatingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),SelectUserActivity.class));
            }
        });
        RecentChatsListener();
    }

    private void triggerToast(String info){
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
    }

    private void getTokens() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken)
                .addOnFailureListener(this::specialToastTrigger);
    }

    private void specialToastTrigger(Exception e) {
        Toast.makeText(getApplicationContext(),"Please Restart Application",Toast.LENGTH_SHORT).show();
    }

    private void updateToken(String s) {  //s here is our token
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        String user_id=sharedPreferences.getString(Fields.USER_ID,null);
        DocumentReference reference= database.collection(Fields.COLLECTION_NAME)
                .document(user_id);

        reference.update(Fields.FCM_TOKEN,s)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //triggerToast("Token Updated and saved");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        triggerToast("Restart Your App");
                    }
                });
    }

    private void setNavContent(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.signout){
                    signOut();
                }else if(item.getItemId()==R.id.home_menu){
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
        });
    }

    private void signOut() {

        String user_id=sharedPreferences.getString(Fields.USER_ID,null);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference reference= database.collection(Fields.COLLECTION_NAME)
                .document(user_id);
        HashMap<String , Object> updatedData=new HashMap<>();
        updatedData.put(Fields.FCM_TOKEN, FieldValue.delete());
        reference.update(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.clear();
                        editor.apply();
                        triggerToast("Signed Out");
                        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        triggerToast("Sign Out Unsuccessful");
                    }
                });
    }

    private void RecentChatsListener(){
        database.collection(Fields.COLLECTION_RECENT_CHAT)
                .whereEqualTo(Fields.SENDER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null))
                .addSnapshotListener(eventListener);
        database.collection(Fields.COLLECTION_RECENT_CHAT)
                .whereEqualTo(Fields.RECEIVER_PERSON_ID,sharedPreferences.getString(Fields.USER_ID,null))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if(error!=null) return;
            if(value!=null) {

                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                        Message message = new Message();
                        message.senderId = documentChange.getDocument().getString(Fields.SENDER_PERSON_ID);
                        message.receiverId = documentChange.getDocument().getString(Fields.RECEIVER_PERSON_ID);
                        if(sharedPreferences.getString(Fields.USER_ID,null).equals(message.senderId)){
                            message.chat_user_name=documentChange.getDocument().getString(Fields.RECEIVER_NAME);
                            message.chat_user_icon=documentChange.getDocument().getString(Fields.RECEIVER_IMAGE);
                            message.chat_id=documentChange.getDocument().getString(Fields.RECEIVER_PERSON_ID);
                        }else{
                            message.chat_user_name=documentChange.getDocument().getString(Fields.SENDER_NAME);
                            message.chat_user_icon=documentChange.getDocument().getString(Fields.SENDER_IMAGE);
                            message.chat_id=documentChange.getDocument().getString(Fields.SENDER_PERSON_ID);
                        }
                        message.message=documentChange.getDocument().getString(Fields.RECENT_MESSAGE);
                        message.date=documentChange.getDocument().getDate(Fields.TIMESTAMP);
                        recent_chats_arraylist.add(message);
                    }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                        for(int i=0; i<recent_chats_arraylist.size(); i++){
                            String senderid=documentChange.getDocument().getString(Fields.SENDER_PERSON_ID);
                            String receiverid=documentChange.getDocument().getString(Fields.RECEIVER_PERSON_ID);
                            if(recent_chats_arraylist.get(i).senderId.equals(senderid) && recent_chats_arraylist.get(i).receiverId.equals(receiverid)){
                                recent_chats_arraylist.get(i).message=documentChange.getDocument().getString(Fields.RECENT_MESSAGE);
                                recent_chats_arraylist.get(i).date=documentChange.getDocument().getDate(Fields.TIMESTAMP);
                                break;
                            }
                        }
                    }
                }
                Collections.sort(recent_chats_arraylist, new Comparator<Message>() {
                    @Override
                    public int compare(Message t1, Message t2) {
                        return t2.date.compareTo(t1.date);
                    }
                });
                recentMessagesAdapter.notifyDataSetChanged();
                binding.recentUsers.smoothScrollToPosition(0);
            }
        }
    };
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: binding.drawerLayout.openDrawer(GravityCompat.START); // this is for the " <- " button that

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChatClicked(SingleUserData singleUserData) {
        Intent intent=new Intent(getApplicationContext(),PersonalChatActivity.class);

        intent.putExtra(Fields.SELECTED_USER,singleUserData);
        startActivity(intent);
        finish();
    }
}