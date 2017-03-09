package com.example.vatsalya.mypayments.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.example.vatsalya.mypayments.adapter.EmptyRecyclerView;
import com.example.vatsalya.mypayments.adapter.UserAccountAdapter;
import com.example.vatsalya.mypayments.step.AbstractStepperActivity;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Vatsalya on 10/4/16.
 */

public class AccountFragment extends Fragment implements UserAccountAdapter.ItemClickCallBack, DeleteAccountDialogFragment.DeleteAccountDialogListener{
//    private RecyclerView recyclerView;
    private EmptyRecyclerView recyclerView;
    private UserAccountAdapter userAccountAdapter;
    public static AmazonClientManager clientManager = null;
    private DynamoDBManager.UserDetails userDetails;
    private DynamoDBManager.UserDetails newUserDetails;
    private String userID;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientManager = new AmazonClientManager(getContext());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);
        userID = getArguments().getString("userID");

//        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager( new LinearLayoutManager(recyclerView.getContext()));
        recyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setEmptyView(rootView.findViewById(R.id.list_empty));

        FloatingActionButton button = (FloatingActionButton)rootView.findViewById(R.id.fab2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountFragment.this.getActivity(), AddNewAccount.class);
                startActivity(intent);

            }
        });
        new GetVendorAccountByUserTask().execute();

        return rootView;
    }

    @Override
    public void onItemClick(int p , String[] mAccountName) {

        Intent intent = new Intent(AccountFragment.this.getContext() , InvoiceByVendorListActivity.class);
        intent.putExtra("Account",mAccountName[0]);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(int p, String mVendorAccountID) {
        showAlertDialog(p,mVendorAccountID);


    }
    private void showAlertDialog(int p,String mVendorAccountID) {

        DeleteAccountDialogFragment alertDialog = DeleteAccountDialogFragment.newInstance("Delete your account at " + mVendorAccountID , mVendorAccountID,p);
        alertDialog.setTargetFragment(AccountFragment.this, 0);
        alertDialog.show(getFragmentManager(), "dialog");

    }

    @Override
    public void onDialogPositiveClick(Boolean positive , int p , String mVendorAccountID) {

        new DeleteVendorAccount(mVendorAccountID,userID,p).execute();
    }

    @Override
    public void onDialogNegativeClick(Boolean positive) {

    }


    //Get a list of accounts linked to the user id

    public class GetVendorAccountByUserTask extends AsyncTask<Void, Void, List<String>> {
        private SharedPreferences sharedPreferences;
        private String userID;
        private Context context;

        @Override
        protected void onPreExecute() {
            //set message of the dialog
            super.onPreExecute();

            context = getActivity();
            sharedPreferences = context.getSharedPreferences(
                    getString(R.string.preference_file_key), MODE_PRIVATE);
            userID = sharedPreferences.getString(getString(R.string.logged_in_user_id),"problem");

        }

        protected List<String> doInBackground(Void... inputs) {

        String tableStatus = DynamoDBManager.getUserTableStatusFromFragment();
        if (tableStatus.equalsIgnoreCase("ACTIVE")) {
            userDetails = DynamoDBManager.getUserDetails(userID);
            List<String> vendorAccount = userDetails.getVendorAccount();
            return vendorAccount;
        }
        return null;
    }

    protected void onPostExecute(List<String> result) {
            if(result != null) {
                userAccountAdapter = new UserAccountAdapter(result, AccountFragment.this.getContext());
                userAccountAdapter.setItemClickCallBack(AccountFragment.this);
            }else{
                userAccountAdapter = null;
            }
        recyclerView.setAdapter(userAccountAdapter);


        }
    }

    public class DeleteVendorAccount extends AsyncTask<Void, Void, int[]> {

        ProgressDialog asyncDialog = new ProgressDialog(AccountFragment.this.getActivity());
        private String mVendorAccountID;
        private String mUserID;
        private int mPosition;



        public DeleteVendorAccount(String mVendorAccountID, String mUserID,int mPosition){
            this.mUserID = mUserID;
            this.mVendorAccountID = mVendorAccountID;
            this.mPosition = mPosition;
        }
        @Override
        protected void onPreExecute() {
            //set message of the dialog
            super.onPreExecute();
            asyncDialog.setMessage(getString(R.string.deleting_account));
            //show dialog
            if (asyncDialog == null && asyncDialog.isShowing() == false) {
                asyncDialog.show();
            }
            asyncDialog.show();

        }
        protected int[] doInBackground(Void... inputs) {

            String tableStatus = DynamoDBManager.getUserTableStatusFromAccountFragment();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {

      //          AmazonServiceException ex =
                 newUserDetails = DynamoDBManager.deleteAccountInUser(mUserID, mVendorAccountID);

                int[] pos = {mPosition};
                return pos;
            }
            return null;
        }

        @Override
        protected void onPostExecute(int[] result) {
            super.onPostExecute(result);
            //hide the dialog
            asyncDialog.dismiss();
            userDetails.getVendorAccount().remove(result[0]);
            recyclerView.removeViewAt(result[0]);
            userAccountAdapter.notifyItemRemoved(result[0]);
            userAccountAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
//            if(result != null && result.getStatusCode() == 400) {
//                Toast.makeText(
//                        AccountFragment.this.getActivity(),
//                        "Error adding account"
//                                + result.getErrorMessage(), Toast.LENGTH_LONG)
//                        .show();
//
//
//            }

        }

    }
}


