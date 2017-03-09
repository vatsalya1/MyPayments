package com.example.vatsalya.mypayments.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.Constants;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.example.vatsalya.mypayments.adapter.InvoiceAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Vatsalya on 11/13/16.
 */

public class AllInvoicesFragment extends android.support.v4.app.Fragment  {


    private RecyclerView recyclerView;
    private InvoiceAdapter invoiceAdapter;
    public static AmazonClientManager clientManager = null;
    CognitoCachingCredentialsProvider credentialsProvider = null;
    public ArrayList<DynamoDBManager.InvoiceDetails> invoicesByVendor;
    AmazonS3 s3;
    private String invoiceNumber;
    public static final String MY_URI = "myURI";
    public static final String MY_KEY = "myKEY";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientManager = new AmazonClientManager(getContext());
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(),
                Constants.IDENTITY_POOL_ID,
                Regions.US_EAST_1 // Region
        );
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_invoices_by_account, container, false);
        //userID = getArguments().getString("userID");

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewFragment);
        recyclerView.setLayoutManager( new LinearLayoutManager(recyclerView.getContext()));


        new GetInvoicesByVendorTask().execute();


        return rootView;
    }



    public class GetInvoicesByVendorTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... inputs) {
            String tableStatus = DynamoDBManager.getInvoiceTableStatusFromFragment();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                invoicesByVendor = DynamoDBManager.getInvoiceByVendor();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        protected void onPostExecute(Void result) {

            invoiceAdapter = new InvoiceAdapter(invoicesByVendor, AllInvoicesFragment.this.getContext(), new InvoiceAdapter.ItemClickCallBack() {
                @Override
                public void onViewClicked(DynamoDBManager.InvoiceDetails item) {
                        boolean isValidFile = true;
                        invoiceNumber = item.getInvoiceNumber();
                        String invoiceKey = "1/" + invoiceNumber + ".pdf";
                           Intent intent = new Intent(AllInvoicesFragment.this.getActivity(), PDFViewingActivity.class);
                           intent.putExtra(MY_KEY, invoiceKey);
                           startActivity(intent);

                }
            });
            recyclerView.setAdapter(invoiceAdapter);

        }
    }

}
