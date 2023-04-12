package com.metropolitan.appchat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.metropolitan.appchat.Models.Message;
import com.metropolitan.appchat.R;
import com.metropolitan.appchat.databinding.ItemReceivedBinding;
import com.metropolitan.appchat.databinding.ItemSentBinding;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter { //implemented methods
    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVED = 2;

    String senderRoom;
    String receiverRoom;



    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new sentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_received, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVED;
        }
    }

        //returns the view type of the item at POSITION
        //For the recyclerView


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);
        int reactions[] = new int[]{
                R.drawable.realaot,
                R.drawable.fb_love,
                R.drawable.fb_haha,
                R.drawable.ic_fb_wow,
                R.drawable.fb_sad,
                R.drawable.fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass() == sentViewHolder.class) {
                sentViewHolder viewHolder = (sentViewHolder)holder;
                viewHolder.binding.feel.setImageResource(reactions[pos]);
                viewHolder.binding.feel.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
                viewHolder.binding.feel.setImageResource(reactions[pos]);
                viewHolder.binding.feel.setVisibility(View.VISIBLE);


            }

            message.setFeel(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);



            return true; // true is closing popup, false is requesting a new selection
        });
        if (holder.getClass() == sentViewHolder.class) {
            sentViewHolder viewHolder = (sentViewHolder) holder;

            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholderscout)
                        .into(viewHolder.binding.image);
            }

            viewHolder.binding.message.setText(message.getMessage());

            if (message.getFeel() >= 0) {
                viewHolder.binding.feel.setImageResource(reactions[message.getFeel()]); //this if is for the firebase database as reactions are stored in INT
                viewHolder.binding.feel.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feel.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });


        } else {

            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholderscout)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.message.setText(message.getMessage());

            if (message.getFeel() >= 0) {


                //THE RECEIVER NOW WILL SEE THE SENDERS ACTUAL REACTION
                //if not for this, he will always see the reaction with the id 1
                viewHolder.binding.feel.setImageResource(reactions[message.getFeel()]);
                viewHolder.binding.feel.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feel.setVisibility(View.GONE);
            }


            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() { //Code for Ontouch reactions
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });

        }
    }


    @Override
    public int getItemCount() {

        return messages.size();
    }

    public class sentViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;
        public sentViewHolder(@NonNull View itemView) {

            super(itemView);
            binding=ItemSentBinding.bind(itemView);
        }
    } //viewHolder describes item view and metadata about its place withing recycler view
public class ReceiverViewHolder extends RecyclerView.ViewHolder{
    ItemReceivedBinding binding;
    public ReceiverViewHolder(@NonNull View itemView) {

        super(itemView);
        binding=ItemReceivedBinding.bind(itemView);

        //super is the constructor of itemview and it creates it and then we call it via Binding
    }
}
}


