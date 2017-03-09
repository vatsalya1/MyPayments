package com.example.vatsalya.mypayments.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AddNewAccountAdapter;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;

import java.util.ArrayList;

/**
 * Created by Vatsalya on 10/18/16.
 */

public class AddNewAccount extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AddNewAccountAdapter mAdapter;
    public static AmazonClientManager clientManager = null;
    private ArrayList<DynamoDBManager.VendorDetails> vendorDetailsArrayList;
    private Context context;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_account);
        myToolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(myToolbar);
        final ActionBar ab = getSupportActionBar();

        ab.setTitle("Accounts");
        ab.setDisplayHomeAsUpEnabled(true);

        context = getApplicationContext();
        clientManager = new AmazonClientManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_add_new_accounts);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        new GetAllVendorAccountsTask().execute();

    }


    public class GetAllVendorAccountsTask extends AsyncTask<Void, Void, ArrayList<DynamoDBManager.VendorDetails>> {

        protected ArrayList<DynamoDBManager.VendorDetails> doInBackground(Void... inputs) {
            String tableStatus = DynamoDBManager.getVendorTableStatus();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                vendorDetailsArrayList = DynamoDBManager.getVendorDetails();

                return vendorDetailsArrayList;
            }
            return null;
        }

        protected void onPostExecute(ArrayList<DynamoDBManager.VendorDetails> result) {
            mAdapter = new AddNewAccountAdapter(context,result);
            recyclerView.setAdapter(mAdapter);

        }

    }
}
