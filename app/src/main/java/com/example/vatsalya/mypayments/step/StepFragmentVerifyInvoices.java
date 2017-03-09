package com.example.vatsalya.mypayments.step;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.*;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vatsalya on 1/12/17.
 */

public class StepFragmentVerifyInvoices extends Fragment implements BlockingStep {
    private EditText mInvoiceNumberOne;
    private EditText mInvoiceNumberTwo;
    private EditText mInvoiceAmountOne;
    private EditText mInvoiceAmountTwo;
    private String mAccountName;
    private String mAccountNumber;
    public static AmazonClientManager clientManager = null;
    private SharedPreferences sharedPreferences;
    private Context context;
    private SharedPreferences.Editor editor;
    private String mVendorAccountID;
    // public ProgressDialog asyncDialog;


    @Nullable
    private OnNavigationBarListener onNavigationBarListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationBarListener) {
            onNavigationBarListener = (OnNavigationBarListener) context;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        clientManager = new AmazonClientManager(getContext());
        super.onCreate(state);
        final Bundle args = getArguments();
        mAccountNumber = args.getString("AccountID");
        mAccountName = args.getString("AccountName");
//        context = getActivity();
//        sharedPreferences = context.getSharedPreferences(
//                getString(R.string.preference_file_key), MODE_PRIVATE);
//        mVendorAccountID = sharedPreferences.getString(getString(R.string.saved_vendor_account_id),"problem");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_verify_invoices, container, false);
        //updateNavigationBar();
        mInvoiceNumberOne = (EditText)v.findViewById(R.id.invoice_number_one);
        mInvoiceNumberTwo = (EditText)v.findViewById(R.id.invoice_number_two);
        mInvoiceAmountOne = (EditText)v.findViewById(R.id.invoice_amount_one);
        mInvoiceAmountTwo = (EditText)v.findViewById(R.id.invoice_amount_two);

        //initialize your UI

        return v;
    }


    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        if(mInvoiceNumberOne.getText().toString().matches("") ){
            return new VerificationError("Invoice Numbers and Amount Cannot Be Left Blank");
        } else if (mInvoiceNumberTwo.getText().toString().matches("")){
            return new VerificationError("Invoice Numbers and Amount Cannot Be Left Blank");
        } else if (mInvoiceAmountOne.getText().toString().matches("")){
            return new VerificationError("Invoice Numbers and Amount Cannot Be Left Blank");
        } else if (mInvoiceAmountTwo.getText().toString().matches("")){
            return new VerificationError("Invoice Numbers and Amount Cannot Be Left Blank");
        } else
            return null;
    }

    @Override
    public void onSelected() {
        //update UI when selected
        updateNavigationBar();
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //handle error inside of the fragment, e.g. show error on EditText
    }

    private void updateNavigationBar() {
        if (onNavigationBarListener != null) {
            onNavigationBarListener.onChangeEndButtonsEnabled(true);
        }
    }
    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        context = getActivity();
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        mVendorAccountID = sharedPreferences.getString(getString(R.string.saved_vendor_account_id),"problem");

        new VerifyInvoices(mInvoiceNumberOne.getText().toString(),mInvoiceNumberTwo.getText().toString(),
                mInvoiceAmountOne.getText().toString(),mInvoiceAmountTwo.getText().toString() , mVendorAccountID, callback).execute();

    }


    @Override
    @UiThread
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
        callback.goToPrevStep();
    }

    public class VerifyInvoices extends AsyncTask<Void, Void, Boolean> {

      ProgressDialog asyncDialog = new ProgressDialog(getActivity());
        private StepperLayout.OnNextClickedCallback callback;
        private String mInvoiceOne;
        private String mInvoiceTwo;
        private float mAmountOne;
        private float mAmountTwo;
        private String mVendorAccountID;
        private ArrayList<DynamoDBManager.InvoiceDetails> invoiceDetails1;
        private ArrayList<DynamoDBManager.InvoiceDetails> invoiceDetails2;
        private Boolean invoiceVerified = false;



        public VerifyInvoices(String mInvoiceOne, String mInvoiceTwo,String mAmountOne,String mAmountTwo,String mVendorAccountID,
                              StepperLayout.OnNextClickedCallback callback){
            this.mInvoiceOne = mInvoiceOne;
            this.mInvoiceTwo = mInvoiceTwo;
            this.mAmountOne =  Float.parseFloat(mAmountOne);
            this.mAmountTwo =  Float.parseFloat(mAmountTwo);
            this.mVendorAccountID = mVendorAccountID;
            this.callback = callback;
        }
        @Override
        protected void onPreExecute() {
            //set message of the dialog
            super.onPreExecute();
            asyncDialog.setMessage(getString(R.string.progress_verify_invoices_title));
            //show dialog
            if (asyncDialog == null && asyncDialog.isShowing() == false) {
                asyncDialog.show();
            }
            asyncDialog.show();

        }
        protected Boolean doInBackground(Void... inputs) {

          String tableStatus = DynamoDBManager.getInvoiceTableStatusFromStep();
          if (tableStatus.equalsIgnoreCase("ACTIVE")) {

              invoiceDetails1 = DynamoDBManager.getInvoiceDetailsForGivenAccount(mVendorAccountID, mInvoiceOne);
              invoiceDetails2 = DynamoDBManager.getInvoiceDetailsForGivenAccount(mVendorAccountID, mInvoiceTwo);

              if (invoiceDetails2.size() != 0 && invoiceDetails1.size() != 0) {
                  if (invoiceDetails1.get(0).getInvoiceAmount() == mAmountOne && invoiceDetails2.get(0).getInvoiceAmount() == mAmountTwo) {
                      invoiceVerified = true;

                  }
                  return invoiceVerified;
              }
          }
        return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //hide the dialog

            asyncDialog.dismiss();
        if (result == null) {
              invoiceVerified = false;

            }else if (result == true){

            if (asyncDialog != null && asyncDialog.isShowing()) {
                asyncDialog.dismiss();
            }
            callback.goToNextStep();
            }

        }

    }
}
