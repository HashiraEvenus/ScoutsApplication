package com.metropolitan.appchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.metropolitan.appchat.Models.User;
import com.metropolitan.appchat.databinding.ActivitySetupProfileBinding;

public class setupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding; //Binds activity set up profile
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater()); //tells binding what it "is"

        setContentView(binding.getRoot()); // binding gets view from root
//DIALOG MESSAGE START
        dialog= new ProgressDialog(this);
        dialog.setMessage("Updating your Scouting Profile...");
        dialog.setCancelable(false);
//DIALOG END
        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        //The instance variables for each of the variables above(auth, database, storage) is being gotten
        getSupportActionBar().hide(); //hides the app's bar on top
        binding.imageView.setOnClickListener(new View.OnClickListener(){    //Binds with click Listener( button)
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(); // new intent
                intent.setAction(Intent.ACTION_GET_CONTENT); //intent variable takes action to get the content
                intent.setType("image/*");//image static

            startActivityForResult(intent,45 );
            }
        });

        binding.ContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameBox.getText().toString();
                if (name.isEmpty()){
                    binding.nameBox.setError("Please type a name");
                    return;
                }

                dialog.show(); //SHOWS DIALOG
                if (selectedImage !=null){
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                     String imageUrl = uri.toString();

                                     String uid = auth.getUid();

                                     String phone = auth.getCurrentUser().getPhoneNumber();

                                     String name = binding.nameBox.getText().toString();


                                     User user= new User(uid,name,phone,imageUrl);

                                     database.getReference()  //makes the reference and creates the files in Firebase database
                                             .child("users")
                                             .child(uid)
                                             .setValue(user)
                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void aVoid) {
                                                     dialog.dismiss(); //deletes the dialog box
                                                Intent intent = new Intent(setupProfileActivity.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                                 }
                                             });
                                    }
                                });
                            }
                        }
                    });
                    //guides the user in a folder of default pics to choose
                    //in case he doesn't have any
                } else{
                    {
                        String uid = auth.getUid();

                        String phone = auth.getCurrentUser().getPhoneNumber();




                        User user= new User(uid,name,phone,"No Image");

                        database.getReference()  //makes the reference and creates the files in Firebase database
                                .child("users")
                                .child(uid)
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss(); //deletes the dialog box
                                        Intent intent = new Intent(setupProfileActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                }

                // ELSE -- WHAT TO DO IF PICTURE IS NOT CHOSEN (allows to continue to the profile updating even without a picture
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data !=null)
        {
        if (data.getData()!=null)
        {
            binding.imageView.setImageURI(data.getData());
            selectedImage = data.getData();
        }

        }
    }
}