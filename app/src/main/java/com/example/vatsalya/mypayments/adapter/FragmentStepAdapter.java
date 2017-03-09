package com.example.vatsalya.mypayments.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.step.StepFragmentAddAccount;
import com.example.vatsalya.mypayments.step.StepFragmentVerifyAccountNumber;
import com.example.vatsalya.mypayments.step.StepFragmentVerifyInvoices;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.adapter.AbstractStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

/**
 * Created by vatsalya on 1/12/17.
 */

public class FragmentStepAdapter extends AbstractFragmentStepAdapter {
    private static final String CURRENT_STEP_POSITION_KEY = "position";
    private Bundle bundle;
    private String mAccountNumber;
    private String mAccountName;

    public FragmentStepAdapter(FragmentManager fm, Context context, Bundle bundle) {
        super(fm, context);
        mAccountNumber = bundle.getString("AccountID");
        mAccountName = bundle.getString("AccountName");
    }

    @Override
    public Step createStep(int position) {

        switch (position) {
            case 0: {
                final StepFragmentVerifyAccountNumber step = new StepFragmentVerifyAccountNumber();
                Bundle b = new Bundle();
                b.putInt(CURRENT_STEP_POSITION_KEY, position);
                b.putString("AccountID", mAccountNumber);
                b.putString("AccountName", mAccountName);
                step.setArguments(b);
                return step;
            }
            case 1: {
                final StepFragmentVerifyInvoices step = new StepFragmentVerifyInvoices();
                Bundle b = new Bundle();
                b.putInt(CURRENT_STEP_POSITION_KEY, position);
                b.putString("AccountID", mAccountNumber);
                b.putString("AccountName", mAccountName);
                step.setArguments(b);
                return step;
            }
            case 2: {
                final StepFragmentAddAccount step = new StepFragmentAddAccount();
                Bundle b = new Bundle();
                b.putInt(CURRENT_STEP_POSITION_KEY, position);
                b.putString("AccountID", mAccountNumber);
                b.putString("AccountName", mAccountName);
                step.setArguments(b);
                return step;
            }
            default: {
                throw new IllegalArgumentException("Unsupported position: " + position);
            }
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        //Override this method to set Step title for the Tabs, not necessary for other stepper types
        switch (position) {
            case 0:
                return new StepViewModel.Builder(context)
                        .setTitle(R.string.tab_title_verify_account_number) //can be a CharSequence instead
                        .create();
            case 1:
                return new StepViewModel.Builder(context)
                        .setTitle(R.string.tab_title_verify_invoices) //can be a CharSequence instead
                        .create();
            case 2:
                return new StepViewModel.Builder(context)
                        .setTitle(R.string.tab_title_verify_invoices) //can be a CharSequence instead
                        .create();

            default:
                throw new IllegalArgumentException("Unsupported position: " + position);


        }
    }
}