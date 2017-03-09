package com.example.vatsalya.mypayments.ui;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by vatsalya on 3/1/17.
 */

public class DeleteAccountDialogFragment extends DialogFragment {


        /* The activity that creates an instance of this dialog fragment must
         * implement this interface in order to receive event callbacks.
         * Each method passes the DialogFragment in case the host needs to query it. */
        public interface DeleteAccountDialogListener {
            public void onDialogPositiveClick(Boolean positive, int position , String accountToDelete);
            public void onDialogNegativeClick(Boolean positive);
        }
    DeleteAccountDialogListener deleteAccountDialogListener;

        // Use this instance of the interface to deliver action events
        DeleteAccountDialogFragment mListener;
        public DeleteAccountDialogFragment() {
        // Empty constructor required for DialogFragment
        }
    public static DeleteAccountDialogFragment newInstance(String title , String accountToDelete , int position) {
        DeleteAccountDialogFragment frag = new DeleteAccountDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("accountToDelete",accountToDelete);
        args.putInt("position",position);
        frag.setArguments(args);
        return frag;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteAccountDialogListener = (DeleteAccountDialogListener) getTargetFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final String accountToDelete = getArguments().getString("accountToDelete");
        final int position = getArguments().getInt("position");

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage("Are you sure?");
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccountDialogListener.onDialogPositiveClick(true, position, accountToDelete);
                // on success
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccountDialogListener.onDialogNegativeClick(false);
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }
}