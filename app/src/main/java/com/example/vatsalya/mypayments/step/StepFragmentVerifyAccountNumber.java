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
import android.widget.TextView;
import android.widget.Toast;

import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by vatsalya on 1/12/17.
 */

public class StepFragmentVerifyAccountNumber extends Fragment implements BlockingStep {
    private String mAccountNumber;
    private TextView mAccountID;
    private String mAccountName;
    private TextView mAccountTextView;
    public static AmazonClientManager clientManager = null;
    private DynamoDBManager.VendorAccountDetails vendorAccountDetailsResult;
    public String mAccountNumberToVerify;
    private Boolean mAccountVerified = false;


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

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step_verify_account_number, container, false);
        updateNavigationBar();
        mAccountID = (TextView) v.findViewById(R.id.account_number_to_verify);
        mAccountTextView= (TextView) v.findViewById(R.id.account_number_textview);
        //mAccountID.setText(mAccountNumber);
        mAccountTextView.setText("Your Account At " + mAccountName);
        //initialize your UI

        return v;
    }


    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
        if(mAccountID.getText().toString().matches("") ){
            return new VerificationError("Please Enter Account Number To Verify");
        }else
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
        mAccountID.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake_error));
    }

    private void updateNavigationBar() {
        if (onNavigationBarListener != null) {
            onNavigationBarListener.onChangeEndButtonsEnabled(true);
        }
    }

    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        mAccountNumberToVerify = mAccountNumber + "-" + mAccountID.getText().toString();
        new VerifyAccountNumber(mAccountNumberToVerify,callback).execute();
    }

    @Override
    @UiThread
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
        callback.goToPrevStep();
    }

    public class VerifyAccountNumber extends AsyncTask<Void, Void, DynamoDBManager.VendorAccountDetails> {

        ProgressDialog asyncDialog = new ProgressDialog(getActivity());
        private DynamoDBManager.VendorAccountDetails vendorAccountDetails;
        private StepperLayout.OnNextClickedCallback callback;
        private String mAccountNumberToVerify;
        private SharedPreferences sharedPreferences;
        private Context context;
        private SharedPreferences.Editor editor;

        public VerifyAccountNumber(String mAccountNumberToVerify,StepperLayout.OnNextClickedCallback callback){
            this.mAccountNumberToVerify = mAccountNumberToVerify;
            this.callback = callback;
        }
        @Override
        protected void onPreExecute() {
            //set message of the dialog
            asyncDialog.setMessage(getString(R.string.tab_title));
            //show dialog
            asyncDialog.show();
            super.onPreExecute();
        }
        protected DynamoDBManager.VendorAccountDetails doInBackground(Void... inputs) {
            String tableStatus = DynamoDBManager.getVendorAccountTableStatus();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                vendorAccountDetails = DynamoDBManager.getVendorAccountDetails(mAccountNumberToVerify);
                return vendorAccountDetails;
            } else {
                    return null;
                }

        }

        @Override
        protected void onPostExecute(DynamoDBManager.VendorAccountDetails result) {
            //hide the dialog

            asyncDialog.dismiss();
            if (result == null) {
                mAccountVerified = false;
                mAccountID.setText("");
                mAccountID.setHint("Account Number Could Not Be Verified");
                mAccountID.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shake_error));
            }else{
                context = getActivity();
                sharedPreferences = context.getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString(getString(R.string.saved_vendor_account_id), result.getVendorAccountID().toString());
                editor.commit();
                mAccountVerified = true;
                callback.goToNextStep();
            }
            super.onPostExecute(result);
        }

    }
}
