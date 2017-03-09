package com.example.vatsalya.mypayments.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.vatsalya.mypayments.R;

import java.util.ArrayList;

/**
 * Created by Vatsalya on 11/12/16.
 */

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceHolder>{

    private ArrayList<DynamoDBManager.InvoiceDetails> listData;
    private LayoutInflater inflater;

    public interface ItemClickCallBack {
   //     void onRowClicked(int position);
        void onViewClicked(DynamoDBManager.InvoiceDetails item);
    }
    private final ItemClickCallBack itemClickCallBack;

    public InvoiceAdapter(ArrayList<DynamoDBManager.InvoiceDetails> listData, Context c, ItemClickCallBack itemClickCallBack ){

        this.listData = listData;
        this.inflater = LayoutInflater.from(c);
        this.itemClickCallBack = itemClickCallBack;

    }


    @Override
    public InvoiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.invoices_by_account_card,parent,false);

        return new InvoiceHolder(view);
    }

    @Override
    public void onBindViewHolder(InvoiceHolder holder, int position) {
        DynamoDBManager.InvoiceDetails item = listData.get(position);
        //holder.invoiceNumber.setText(item.getInvoiceNumber());
        holder.bind(item, itemClickCallBack);
    }


    @Override
    public int getItemCount() {
        return listData.size();
    }

     class InvoiceHolder extends RecyclerView.ViewHolder {
        private TextView invoiceNumber;
        private View container;
        private ImageButton viewInvoice;
        public InvoiceHolder(View itemView) {
            super(itemView);
            invoiceNumber = (TextView) itemView.findViewById(R.id.invoice_number_value);
            container = itemView.findViewById(R.id.card_invoice);
            viewInvoice = (ImageButton) itemView.findViewById(R.id.view_invoice);
        }

         public void bind(final DynamoDBManager.InvoiceDetails item, final ItemClickCallBack listener) {

             invoiceNumber.setText(item.getInvoiceNumber());
             viewInvoice.setOnClickListener(new View.OnClickListener() {
                 @Override public void onClick(View v) {
                     listener.onViewClicked(item);
                 }
             });
         }



            //viewInvoice.setOnClickListener(this);

            //container.setOnClickListener(this);

        }

    }
