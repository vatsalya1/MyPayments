package com.example.vatsalya.mypayments.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;


/**
 * Created by Vatsalya on 10/4/16.
 */

public class NoAccountFragment extends Fragment  {

    public static AmazonClientManager clientManager = null;
    private DynamoDBManager.UserDetails userDetails;
    private String userID;


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        clientManager = new AmazonClientManager(getContext());
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootView = inflater.inflate(R.layout.fragment_no_accounts, container, false);
        userID = getArguments().getString("userID");


        FloatingActionButton button = (FloatingActionButton)rootView.findViewById(R.id.fab2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoAccountFragment.this.getActivity(), AddNewAccount.class);
                startActivity(intent);

            }
        });


        return rootView;
    }

}
