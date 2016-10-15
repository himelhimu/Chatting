package com.example.sabbir.chatting.adapter;

import com.example.sabbir.chatting.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

/**
 * Created by sabbi on 10/15/2016.
 */

public class UserRecyclerAdapter extends FirebaseRecyclerAdapter<User,UserViewHolder>{

    public UserRecyclerAdapter(Class<User>modelClass, int modelLayout, Class<UserViewHolder>viewHolderClass, Query query)
    {
        super(modelClass,modelLayout,viewHolderClass,query);
    }

    @Override
    protected void populateViewHolder(UserViewHolder viewHolder, User user, int position) {
        viewHolder.textView1.setText(user.getName());
        viewHolder.textView2.setText(user.getEmail());
    }
}
