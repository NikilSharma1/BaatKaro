package com.example.baatkaro.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.baatkaro.R;
import com.example.baatkaro.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String imageData;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        imageData="";
        sharedPreferences=getSharedPreferences(Fields.PREFERENCES_NAME,MODE_PRIVATE);

        clickOnviews();

    }

    private void setSharedPreferences(String name,String Id){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(Fields.SIGN_IN_STATUS,true);
        editor.putString(Fields.USER_ID,Id);
        editor.putString(Fields.PERSON_NAME,name);
        editor.putString(Fields.PERSON_IMAGE,imageData);
        editor.commit();
    }

    private void initiateSignUp(){

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        HashMap<String ,Object> userdetail=new HashMap<>();
        userdetail.put(Fields.PERSON_NAME,binding.editName.getText().toString().trim());
        userdetail.put(Fields.PERSON_EMAIL,binding.editEmail.getText().toString().trim());
        userdetail.put(Fields.PERSON_PASSWORD,binding.editPassword.getText().toString().trim());
        userdetail.put(Fields.PERSON_IMAGE,imageData);
        database.collection(Fields.COLLECTION_NAME)
                .add(userdetail)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        setSharedPreferences(binding.editName.getText().toString().trim(),documentReference.getId());
                        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),"Error Occurred Please Try Again",Toast.LENGTH_SHORT).show();
                });
    }

    private void triggerToast(String info){
        Toast.makeText(getApplicationContext(),info,Toast.LENGTH_SHORT).show();
    }

    private void clickOnviews(){
        binding.signInTextview.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        });

        binding.imageTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                launcher.launch(intent);
            }
        });
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verifyDetails()){
                    initiateSignUp();
                }
            }
        });
    }

    private final ActivityResultLauncher<Intent> launcher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        if(result!=null){
                            Uri imageUriData=result.getData().getData();
                            try{
                                InputStream inputStream=getContentResolver().openInputStream(imageUriData);
                                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                                binding.imageView.setImageBitmap(bitmap);
                                binding.imageTextview.setVisibility(View.INVISIBLE);
                                imageData= imageToString(bitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    );

    private String imageToString(Bitmap bitmap){
        int newwidth=104;
        int newheight= bitmap.getHeight()*newwidth/bitmap.getWidth();
        Bitmap newbitmap=Bitmap.createScaledBitmap(bitmap,newwidth,newheight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        newbitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private boolean verifyDetails(){
        if(imageData==null){
            triggerToast("Pick an Image");
            return false;
        }else if(binding.editName.getText().toString().trim().isEmpty()){
            triggerToast("Enter Name");
            return false;
        }else if(binding.editEmail.getText().toString().trim().isEmpty()){
            triggerToast("Enter Email");
            return false;
        }else if(binding.editPassword.getText().toString().trim().isEmpty()){
            triggerToast("Enter Password");
            return false;
        }else if(binding.editConPassword.getText().toString().trim().isEmpty()){
            triggerToast("Confirm Password");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.editEmail.getText().toString()).matches()){
            triggerToast("Enter Valid Email Address");
            return false;
        }else if(!binding.editPassword.getText().toString().trim().equals(binding.editConPassword.getText().toString().trim())){
            triggerToast("Passwords Don't Match");
            return false;
        }else{
            return true;
        }
    }
}