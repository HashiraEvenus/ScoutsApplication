package com.metropolitan.appchat.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.metropolitan.appchat.Adapters.MessagesAdapter;
import com.metropolitan.appchat.Models.Message;
import com.metropolitan.appchat.R;
import com.metropolitan.appchat.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message>messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;
    String senderUid;
    String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar); //sets the support bar as toolbar instead of the default we had from activity chat
        database=FirebaseDatabase.getInstance(); //gets Database instance of the above
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        messages=new ArrayList<>();



        String name=getIntent().getStringExtra("name");
        String profile=getIntent().getStringExtra("image");

        binding.name.setText(name);
        Glide.with(ChatActivity.this)
                .load(profile)
                .placeholder(R.drawable.avatar)
                .into(binding.profile) ;// used the imported Glide class on this activity to load the PROFILE
        //that is in the activity chat xml

        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        receiverUid=getIntent().getStringExtra("uid");
       senderUid= FirebaseAuth.getInstance().getUid();

       database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   String status = snapshot.getValue(String.class);
                   if(!status.isEmpty()) {
                       if(status.equals("Offline")){
                           binding.status.setVisibility(View.GONE);
                       }else {
                           binding.status.setText(status);
                           binding.status.setVisibility(View.VISIBLE);
                       }
                   }
//IF USER GETS OUT OF APP INSTEAD OF OFFLINE THE STATUS BAR DISAPPEARS
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

        senderRoom=senderUid + receiverUid;

        receiverRoom=receiverUid + senderUid;

        adapter= new MessagesAdapter(this,messages,senderRoom ,receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);



        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Message message = snapshot1.getValue(Message.class); //gets value from message CLASS
                            message.setMessageId(snapshot1.getKey()); //gets key snapshot1 from firebase
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();//notifies in firebase when data receives change
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText=binding.messageBox.getText().toString();
                Date date= new Date();
                Message message= new Message(messageText,senderUid,date.getTime());
                binding.messageBox.setText("");

                String randomKey = database.getReference()
                        .push()
                        .getKey();

                // PUSH Method generates unique key every time a new child is added to the Firebase reference
                // without push, the app would crush after clicking on the chat

                HashMap<String, Object> lastMessageObj = new HashMap<>();
                lastMessageObj.put("lastMsg",message.getMessage());         //sets the value last message in the database
                lastMessageObj.put("lastMsgTime",date.getTime());           //set the time of the lastMessage



                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .updateChildren(lastMessageObj);

                database.getReference()
                        .child("chats")
                        .child(receiverRoom)
                        .updateChildren(lastMessageObj);

                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                    }
                });

            }
        });
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);

            }
        });

        //TYPING STATUS START
       final  Handler handler = new Handler();

        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("Typing now...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }
            //THESE 2 Say that if the user is typing, instead of ONLINE write TYPING NOW and if he stops typing it will go back to Online until he starts again
            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
        //END
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle(name);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==25){

            if(data!=null){
                if(data.getData() !=null){
                    Uri selectedImage =data.getData();
                    Calendar calendar =Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful())
                            {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filepath = uri.toString();
                                        String messageText=binding.messageBox.getText().toString();
                                        Date date= new Date();
                                        Message message= new Message(messageText,senderUid,date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filepath);
                                        binding.messageBox.setText("");

                                        String randomKey = database.getReference()
                                                .push()
                                                .getKey();

                                        // PUSH Method generates unique key every time a new child is added to the Firebase reference
                                        // without push, the app would crush after clicking on the chat

                                        HashMap<String, Object> lastMessageObj = new HashMap<>();
                                        lastMessageObj.put("lastMsg",message.getMessage());         //sets the value last message in the database
                                        lastMessageObj.put("lastMsgTime",date.getTime());           //set the time of the lastMessage



                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .updateChildren(lastMessageObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(receiverRoom)
                                                .updateChildren(lastMessageObj);

                                        database.getReference()
                                                .child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {

                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                            }
                                        });
                                        //Toast.makeText(ChatActivity.this,filepath,Toast.LENGTH_SHORT).show();


                                    }
                                });
                            }
                        }
                    });

                }
            }
        }
    }

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

    @Override
    public boolean onSupportNavigateUp() {finish();
        return super.onSupportNavigateUp();
    }
}