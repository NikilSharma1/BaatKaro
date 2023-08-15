package com.example.baatkaro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.baatkaro.R;
import com.example.baatkaro.databinding.ActivitySelectUserBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SelectUserActivity extends AppCompatActivity {

    private ActivitySelectUserBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySelectUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);
        progressStatus(true); //data is loading
        getData();
    }


    private void getData() {

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Fields.COLLECTION_NAME)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressStatus(false);
                        if(task.isSuccessful() && task.getResult()!=null){
                            ArrayList<SingleUserData>list=new ArrayList<>();
                            for(QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()){
                                //Log.i("info",queryDocumentSnapshot.getId()+"  "+sharedPreferences.getString(Fields.USER_ID,null));
                                if(queryDocumentSnapshot.getId().equals(sharedPreferences.getString(Fields.USER_ID,null))){
                                    continue;
                                }
                                SingleUserData userData=new SingleUserData();
                                userData.setName(queryDocumentSnapshot.getString(Fields.PERSON_NAME));
                                userData.setEmail(queryDocumentSnapshot.getString(Fields.PERSON_EMAIL));
                                userData.setImage(queryDocumentSnapshot.getString(Fields.PERSON_IMAGE));
                                userData.setToken(queryDocumentSnapshot.getString(Fields.FCM_TOKEN));
                                userData.setId(queryDocumentSnapshot.getId());

                                list.add(userData);
                            }
                            //Log.d("info",list.size()+" <- size of arr");
                            if(list.size()>0){

                                UserAdapter userAdapter=new UserAdapter(getApplicationContext(),list);
                                binding.selectRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                binding.selectRecyclerview.setAdapter(userAdapter);
                            }else{
                                Toast.makeText(getApplicationContext(),"Failed to Load",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Failed to Fetch",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void progressStatus(Boolean status){
        if(status){
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }
}