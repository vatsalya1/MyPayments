package com.example.vatsalya.mypayments.step;

/**
 * Created by vatsalya on 1/12/17.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.example.vatsalya.mypayments.adapter.FragmentStepAdapter;
import com.example.vatsalya.mypayments.ui.LoginActivity;
import com.example.vatsalya.mypayments.ui.MainActivity;
import com.example.vatsalya.mypayments.ui.SignupActivity;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;


public class AbstractStepperActivity extends AppCompatActivity implements StepperLayout.StepperListener,
        OnNavigationBarListener {

    private static final String CURRENT_STEP_POSITION_KEY = "position";
    Toolbar myToolbar;
    StepperLayout mStepperLayout;
    private String mAccountNumber;
    private String mAccountName;
    Bundle bundle;
    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private String mVendorAccountID;
    private String mUserID;
    public static AmazonClientManager clientManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_styled_tab);
        clientManager = new AmazonClientManager(this);
        Intent i = getIntent();
        mAccountNumber= i.getStringExtra("AccountID");
        mAccountName = i.getStringExtra("AccountName");
        bundle = new Bundle();
        bundle.putString("AccountID",mAccountNumber);
        bundle.putString("AccountName",mAccountName);



        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        final ActionBar ab = getSupportActionBar();

        ab.setTitle("Add Account");
        ab.setDisplayHomeAsUpEnabled(true);

        mStepperLayout = (StepperLayout) findViewById(R.id.stepperLayout);
        int startingStepPosition = savedInstanceState != null ? savedInstanceState.getInt(CURRENT_STEP_POSITION_KEY) : 0;
        mStepperLayout.setAdapter(new FragmentStepAdapter(getSupportFragmentManager(),this, bundle), startingStepPosition);

        mStepperLayout.setListener(this);
    }

//    @LayoutRes
//    protected abstract int getLayoutResId();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_STEP_POSITION_KEY, mStepperLayout.getCurrentStepPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        final int currentStepPosition = mStepperLayout.getCurrentStepPosition();
        if (currentStepPosition > 0) {
            mStepperLayout.setCurrentStepPosition(currentStepPosition - 1);
        } else {
            finish();
        }
    }

    @Override
    public void onCompleted(View completeButton) {
        context = AbstractStepperActivity.this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        mVendorAccountID = sharedPreferences.getString(getString(R.string.saved_vendor_account_id),"problem");
        mUserID = sharedPreferences.getString(getString(R.string.logged_in_user_id),"problem");
        new AddNewAccount(mVendorAccountID,mUserID).execute();
        //Toast.makeText(this, "onCompleted!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(AbstractStepperActivity.this, MainActivity.class);
        startActivity(i);

    }

    @Override
    public void onError(VerificationError verificationError) {
        Toast.makeText(this, verificationError.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        Toast.makeText(this, "onStepSelected! -> " + newStepPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReturn() {
        finish();
    }

    @Override
    public void onChangeEndButtonsEnabled(boolean enabled) {
        mStepperLayout.setNextButtonVerificationFailed(!enabled);
        mStepperLayout.setCompleteButtonVerificationFailed(!enabled);
    }

    public class AddNewAccount extends AsyncTask<Void, Void, AmazonServiceException> {

        ProgressDialog asyncDialog = new ProgressDialog(AbstractStepperActivity.this);
        private String mVendorAccountID;
        private String mUserID;



        public AddNewAccount(String mVendorAccountID, String mUserID){
            this.mUserID = mUserID;
            this.mVendorAccountID = mVendorAccountID;
        }
        @Override
        protected void onPreExecute() {
            //set message of the dialog
            super.onPreExecute();
            asyncDialog.setMessage(getString(R.string.adding_account));
            //show dialog
            if (asyncDialog == null && asyncDialog.isShowing() == false) {
                asyncDialog.show();
            }
           asyncDialog.show();

        }
        protected AmazonServiceException doInBackground(Void... inputs) {

            String tableStatus = DynamoDBManager.getUserTableStatusFromAbstractStepperActivity();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {

                AmazonServiceException ex = DynamoDBManager.insertAccountInUser(mUserID, mVendorAccountID);
                return ex;
            }
            return null;
        }

        @Override
        protected void onPostExecute(AmazonServiceException result) {
            super.onPostExecute(result);
            //hide the dialog
            asyncDialog.dismiss();
            if(result != null && result.getStatusCode() == 400) {
                Toast.makeText(
                        AbstractStepperActivity.this,
                        "Error adding account"
                                + result.getErrorMessage(), Toast.LENGTH_LONG)
                        .show();


            }

        }

    }

}

