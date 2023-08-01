package com.example.baatkaro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.baatkaro.R;
import com.example.baatkaro.databinding.ActivityUsersListBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class UsersListActivity extends AppCompatActivity {

    private ActivityUsersListBinding binding;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar= binding.getRoot().findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);
        drawerLayout=binding.drawerLayout;
        navigationView=binding.navigationView;
        recyclerView=binding.usersRecyclerview;
        setNavContent(navigationView);
        getTokens(); //get tokens from gcm needed for messaging
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
                        triggerToast("Token Updated and saved");
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: drawerLayout.openDrawer(GravityCompat.START); // this is for the " <- " button that
                //android adds itself with toolbar when line 32 code is written
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}