package com.example.vatsalya.mypayments.step;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

/**
 * Created by vatsalya on 2/7/17.
 */

public class StepFragmentAddAccount extends Fragment implements BlockingStep {
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
        View v = inflater.inflate(R.layout.step_add_account, container, false);
        updateNavigationBar();

        return v;
    }


    @Override
    public VerificationError verifyStep() {
        //return null if the user can go to the next step, create a new VerificationError instance otherwise
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

    }

    @Override
    @UiThread
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
        callback.goToPrevStep();
    }

}

