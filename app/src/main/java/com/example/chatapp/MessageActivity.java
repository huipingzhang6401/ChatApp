package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessageActivity extends AppCompatActivity {

    private String reciever_name = null;
    private DatabaseReference mDatabase;
    private RecyclerView mMessageList;
    private FirebaseUser mUser;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUsers;
    public FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        FirebaseApp.initializeApp(this);

        editMessage = (EditText) findViewById(R.id.editMessageE);

        reciever_name = getIntent().getExtras().getString("recieverName");

        mMessageList = (RecyclerView) findViewById(R.id.messageRec);
        mMessageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // makes message list start displaying from the bottom of screen
        mMessageList.setLayoutManager(linearLayoutManager);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid()).child("Messages");


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() ==  null){
                    Intent loginIntent = new Intent(MessageActivity.this,RegisterActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // prevents user from going back to previous activity
                    startActivity(loginIntent);
                }
            }
        };

    }

    public void sendButtonClicked(View view) {
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        FirebaseApp.initializeApp(this);
        final String messageValue = editMessage.getText().toString().trim();
        if(!TextUtils.isEmpty(messageValue)){
            final DatabaseReference newPost = mDatabase.push();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    newPost.child("content").setValue(messageValue);
                    newPost.child("receiver").setValue(reciever_name);
                    newPost.child("sender").setValue(mUser.getUid());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mMessageList.scrollToPosition(mMessageList.getAdapter().getItemCount()); // sets the recycler view to last message
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Message, MessageActivity.MessageViewHolder> FBRAMessage = new FirebaseRecyclerAdapter<Message, MessageActivity.MessageViewHolder>(
                Message.class,
                R.layout.single_message_layout,
                MessageActivity.MessageViewHolder.class,
                mDatabase.orderByChild("receiver").equalTo(reciever_name)

        ) {
            @Override
            protected void populateViewHolder(MessageActivity.MessageViewHolder viewHolder, Message model, int position) {
               viewHolder.setUsername(model.getUsername());
               viewHolder.setContent(model.getContent());
            }
        };
        mMessageList.setAdapter(FBRAMessage);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        // note this class is usually static but had to gain access to an
        View mView;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setContent(String content){
            TextView message_content = (TextView) mView.findViewById(R.id.messageText);
            message_content.setText(content);
        }

        public void setUsername(String username){
            TextView username_content = (TextView) mView.findViewById(R.id.usernameText);
            username_content.setText(username);
        }
    }

    public void signOutBtnClicked(View view){
        mAuth.signOut();
    }
}