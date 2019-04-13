package com.example.adeel.ridesharing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static boolean seenCheck = false, writingCheck = false;
    private String receiver_id, name, sender_id, messageText;
    private Toolbar mChatToolbar;
    private final int GALLERY_PICK = 1;
    private RecyclerView mRecyclerView;
    private TextView mName, mLastSeen;
    private ImageButton mAdd, mSend;
    private ProgressBar mBar;
    private EditText mMessage;
    private StorageReference MessageImageStorage;
    private FirebaseAuth mAuth;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private ImageView backImage, mUserImage;
    private final List<Messages> messagesList = new ArrayList<>();
    private DatabaseReference mDatabaseReference, ChatRef, statusRef, writingRef;
    ValueEventListener statusValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                if (dataSnapshot.getValue().toString().equals("true")) {
                    seenCheck = true;
                    messageAdapter.notifyDataSetChanged();
                } else {

                }
            }
            }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener writingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                    if (dataSnapshot.getValue().toString().equals("true")) {
                        writingCheck = true;
                        messageAdapter.notifyDataSetChanged();
                    } else {
                        writingCheck = false;
                        messageAdapter.notifyDataSetChanged();
                    }

            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setTitle("");

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);
        mSend = findViewById(R.id.chat_send_btn);
        mMessage = findViewById(R.id.chat_message_view);
        receiver_id = getIntent().getStringExtra("driverId");
        name = getIntent().getStringExtra("driverName");
        mName = findViewById(R.id.custom_bar_title);
        mName.setText(name);
        backImage = findViewById(R.id.back);



        messageAdapter = new MessageAdapter(messagesList);



        mRecyclerView = findViewById(R.id.messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(messageAdapter);



        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        sender_id = mAuth.getUid();
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        AllMessages();
        statusRef = FirebaseDatabase.getInstance().getReference().child("SeenStatus").child(mAuth.getUid());
        statusRef.keepSynced(true);
        statusRef.addValueEventListener(statusValueEventListener);

        writingRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(receiver_id).child(mAuth.getUid()).child("writingstatus");
        writingRef.keepSynced(true);
        writingRef.addValueEventListener(writingValueEventListener);

        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getUid()).child(receiver_id).child("writingstatus").setValue("true");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0)
                    FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getUid()).child(receiver_id).child("writingstatus").setValue("false");
            }
        });

    }

    private void AllMessages() {
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(sender_id).child(receiver_id);
//        ChatRef.keepSynced(true);

        ChatRef.addChildEventListener(seenFunc);
    }


    ChildEventListener seenFunc = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            FirebaseDatabase.getInstance().getReference().child("SeenStatus").child(receiver_id).setValue("true");
            if(!dataSnapshot.getKey().equals("writingstatus")) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messages.setId(dataSnapshot.getKey());
                messages.setReciever(receiver_id);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(messagesList.size() - 1);
                String sms = dataSnapshot.child("from").getValue().toString();
                String sen = dataSnapshot.child("seen").getValue().toString();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }


    };


    @Override
    protected void onStop() {
        super.onStop();
        ChatRef.removeEventListener(seenFunc);
        statusRef.removeEventListener(statusValueEventListener);
        FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getUid()).child(receiver_id).child("writingstatus").setValue("false");
        writingRef.removeEventListener(writingValueEventListener);
    }

    private void sendMessage() {
        messageText = mMessage.getText().toString();
        if (!TextUtils.isEmpty(messageText)) {
            String sendmessRef = "Messages/" + sender_id + "/" + receiver_id;
            String recmessRef = "Messages/" + receiver_id + "/" + sender_id;

            DatabaseReference user_message_key = mDatabaseReference.child("Messages").child(sender_id).child(receiver_id).push();
            String message_push_key = user_message_key.getKey();

            Map messageTextBody = new HashMap<>();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from", sender_id);
            messageTextBody.put("seen", "false");

            Map messageTextDetail = new HashMap<>();
            messageTextDetail.put(sendmessRef + "/" + message_push_key, messageTextBody);
            messageTextDetail.put(recmessRef + "/" + message_push_key, messageTextBody);

            FirebaseDatabase.getInstance().getReference().child("SeenStatus").child(mAuth.getUid()).setValue("false");
            seenCheck = false;

            mDatabaseReference.updateChildren(messageTextDetail, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    mMessage.setText("");
                }
            });


        }

    }




}
