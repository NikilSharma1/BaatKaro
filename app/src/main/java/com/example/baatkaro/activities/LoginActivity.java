package com.example.baatkaro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.baatkaro.MainActivity;
import com.example.baatkaro.R;
import com.example.baatkaro.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);

        triggerToast(String.valueOf(sharedPreferences.getBoolean(Fields.SIGN_IN_STATUS,false))+" 65");
        if(sharedPreferences.getBoolean(Fields.SIGN_IN_STATUS,false)){
            Intent intent=new Intent(getApplicationContext(), UsersListActivity.class);
            triggerToast(String.valueOf(sharedPreferences.getBoolean(Fields.SIGN_IN_STATUS,false)));
            startActivity(intent);
        }
        clickOnTextViews();
    }
    public void clickOnTextViews(){
        binding.newaccounttextview.setOnClickListener(view -> {
           startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
        });
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verifyLoginDetails()){
                    Login();
                }
            }
        });

    }

    private void Login(){
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Fields.COLLECTION_NAME)
                .whereEqualTo(Fields.PERSON_EMAIL,binding.loginEmail.getText().toString().trim())
                .whereEqualTo(Fields.PERSON_PASSWORD,binding.loginPassword.getText().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                            DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putBoolean(Fields.SIGN_IN_STATUS,true);
                            editor.putString(Fields.USER_ID,documentSnapshot.getId());
                            editor.putString(Fields.PERSON_NAME,documentSnapshot.getString(Fields.PERSON_EMAIL));
                            editor.putString(Fields.PERSON_IMAGE,documentSnapshot.getString(Fields.PERSON_IMAGE));
                            editor.commit();
                            Intent intent=new Intent(getApplicationContext(), UsersListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            triggerToast("Login Unsuccessful");
                        }
                    }
                });
    }

    private void triggerToast(String info){
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
    }

    private boolean verifyLoginDetails(){
        if(binding.loginEmail.getText().toString().trim().isEmpty()){
            triggerToast("Enter Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.loginEmail.getText().toString().trim()).matches()){
            triggerToast("Valid Email");
            return false;
        }else if(binding.loginPassword.getText().toString().trim().isEmpty()){
            triggerToast("Enter Email");
            return false;
        }else{
            return true;
        }
    }
}