    package com.metropolitan.appchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.metropolitan.appchat.Adapters.TopStatusAdapter;
import com.metropolitan.appchat.Models.Status;
import com.metropolitan.appchat.Models.UserStatus;
import com.metropolitan.appchat.R;
import com.metropolitan.appchat.Models.User;
import com.metropolitan.appchat.Adapters.UsersAdapter;
import com.metropolitan.appchat.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

    public class MainActivity extends AppCompatActivity {

        ActivityMainBinding binding;
        FirebaseDatabase database;
        ArrayList<User>users;
        UsersAdapter usersAdapter;
        TopStatusAdapter statusAdapter;
        ArrayList<UserStatus>userStatuses;
        ProgressDialog dialog;

        User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater()); //Layout inflater instantiates the xml file to its activity
        setContentView(binding.getRoot());

        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();
        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user=snapshot.getValue(User.class);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        usersAdapter = new UsersAdapter(this, users);
        statusAdapter = new TopStatusAdapter(this, userStatuses);
        //binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //from the XML file
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);

        binding.recyclerView.setAdapter(usersAdapter);

        binding.recyclerView.showShimmerAdapter();
        binding.statusList.showShimmerAdapter();


        database.getReference().child("users").addValueEventListener(new ValueEventListener() { //public interface ValueEventListener is used to receive events about changes on data location

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()) { //returns a data sna;shot on firebase--to change the data
                    User user = snapshot1.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                    users.add(user);
                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
                //when data is changed a notification will come
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot: snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status>statuses = new ArrayList<>();
                        for(DataSnapshot statusSnapshot: storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                   binding.statusList.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.status:
                        Intent intent =new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,75);
                        break;
                }

                return false;
            }
        });

    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);


            if(data != null) {
                if(data.getData() != null) {
                    dialog.show();
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    Date date = new Date();
                    StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");

                    reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        UserStatus userStatus = new UserStatus();
                                        userStatus.setName(user.getName());
                                        userStatus.setProfileImage(user.getProfileImage());
                                        userStatus.setLastUpdated(date.getTime());

                                        HashMap<String, Object> obj = new HashMap<>();
                                        obj.put("name", userStatus.getName());
                                        obj.put("profileImage", userStatus.getProfileImage());
                                        obj.put("lastUpdated", userStatus.getLastUpdated());

                                        String imageUrl = uri.toString();
                                        Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .updateChildren(obj);

                                        database.getReference().child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("statuses")
                                                .push()
                                                .setValue(status);

                                        dialog.dismiss();

                                   }
                               });
                           }
                        }
                    });
                }

            }
    }
//ONLINE OFFLINE START
        @Override
        protected void onResume() {
            super.onResume();
            String currentId = FirebaseAuth.getInstance().getUid(); // current id  = get uid from firebaseAuthentication
            database.getReference().child("presence").child(currentId).setValue("Online"); // CurrentID is available at folder pressence, write Online
        }

        @Override
        protected void onPause() {
            super.onPause();
            String currentId = FirebaseAuth.getInstance().getUid();
            database.getReference().child("presence").child(currentId).setValue("Offline");
        }
        //If user gets out of app it will write offline on his status

        //END


        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.search:
                Toast.makeText(this,"Search",Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this,"Settings...",Toast.LENGTH_SHORT).show();
                break;
//MAKES MESSAGES WHEN CLICKING ICONS
        }
            return super.onOptionsItemSelected(item);

        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
        }
    }