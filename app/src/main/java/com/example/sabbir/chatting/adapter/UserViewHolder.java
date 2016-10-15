package com.example.sabbir.chatting.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by sabbi on 10/15/2016.
 */

public class UserViewHolder extends RecyclerView.ViewHolder{
    public TextView textView1,textView2;

    public UserViewHolder(View itemView)
    {
        super(itemView);

        textView1=(TextView) itemView.findViewById(android.R.id.text1);
        textView2=(TextView) itemView.findViewById(android.R.id.text2);
    }
}
