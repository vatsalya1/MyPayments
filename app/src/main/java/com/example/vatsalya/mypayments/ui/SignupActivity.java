package com.example.vatsalya.mypayments.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    private final static String USER_ID = "UserID";


    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPhoneNumber;
    private TextView mAlreadyAMember;
    public static AmazonClientManager clientManager = null;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]
    private DynamoDBManager.UserDetails newUser = null;
    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        clientManager = new AmazonClientManager(this);

        // Views
        mFirstName = (EditText) findViewById(R.id.input_firstname);
        mLastName = (EditText) findViewById(R.id.input_lastname);
        mEmailField = (EditText) findViewById(R.id.input_email);
        mPasswordField = (EditText) findViewById(R.id.input_password);
        mPhoneNumber = (EditText) findViewById(R.id.input_phone);
        mAlreadyAMember = (TextView) findViewById(R.id.link_login);


        newUser = new DynamoDBManager.UserDetails();


        // Buttons
        findViewById(R.id.btn_signup).setOnClickListener(this);
        mAlreadyAMember.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    newUser.setEmailAddress(mEmailField.getText().toString());
                    newUser.setFirstName(mFirstName.getText().toString());
                    newUser.setLastName(mLastName.getText().toString());
                    newUser.setPhoneNumber(mPhoneNumber.getText().toString());
                    newUser.setUserID(user.getUid());

                    new SetUser().execute();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]

        // Amazon clientmanager


    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();

                        }

                        // [START_EXCLUDE]

                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

//

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            String userID = user.getUid();
            context = SignupActivity.this;
            sharedPreferences = context.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            editor.putString(getString(R.string.logged_in_user_id), userID);
            editor.commit();

            Intent i = new Intent(SignupActivity.this, MainActivity.class);

            i.putExtra(USER_ID, userID);
            i.putExtra(TAG, "true");
            startActivity(i);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_signup:
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;

            case R.id.link_login:
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                break;

        }
    }

    private class SetUser extends AsyncTask<Void, Void, AmazonServiceException> {

        protected AmazonServiceException doInBackground(Void... inputs) {

            String tableStatus = DynamoDBManager.getUserTableStatus();
            if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                AmazonServiceException ex =  DynamoDBManager.insertUsers(newUser);
                return ex;
            }

            return null;
        }

        protected void onPostExecute(AmazonServiceException result) {

            if(result != null && result.getStatusCode() == 400) {
                Toast.makeText(
                        SignupActivity.this,
                        "Error creating user"
                                + result.getErrorMessage(), Toast.LENGTH_LONG)
                        .show();
//            } else if (!result.equalsIgnoreCase("ACTIVE")) {
//
//                Toast.makeText(
//                        SignupActivity.this,
//                        "The test table is not ready yet.\nTable Status: "
//                                + result, Toast.LENGTH_LONG)
//                        .show();
            }
        }

    }


}
