package com.metropolitan.appchat.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.metropolitan.appchat.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberAct extends AppCompatActivity {
ActivityPhoneNumberBinding binding;
FirebaseAuth auth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent = new Intent(PhoneNumberAct.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        getSupportActionBar().hide();

        binding.PNumberBox.requestFocus();


        binding.ContinueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PhoneNumberAct.this,OTPActivity.class);
            intent.putExtra("phoneNumber",binding.PNumberBox.getText().toString());
                startActivity(intent);
            }
            });
    }
}