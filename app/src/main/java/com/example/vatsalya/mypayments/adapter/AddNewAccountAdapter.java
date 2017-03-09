package com.example.vatsalya.mypayments.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.step.AbstractStepperActivity;


import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Vatsalya on 10/18/16.
 */

public class AddNewAccountAdapter extends RecyclerView.Adapter<AddNewAccountAdapter.MyViewHolder> {

    private ArrayList<DynamoDBManager.VendorDetails> vendorDetailsArrayList;
    private Context context;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mAccountName;
        public ImageView mAccountImage;
        public SwipeRevealLayout swipeLayout;
        public TextView mAddAccount;
        public String mAccountNumber;

        public MyViewHolder(View itemView) {
            super(itemView);
            mAccountName = (TextView) itemView.findViewById(R.id.account_name);
            mAccountImage= (ImageView) itemView.findViewById(R.id.account_photo);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            mAddAccount = (TextView) itemView.findViewById(R.id.add_account);

            mAddAccount.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   Toast.makeText(context, "Refresh Click" + getAdapterPosition()
                                                           , Toast.LENGTH_SHORT).show();

                                                   Intent i = new Intent(context, AbstractStepperActivity.class);
                                                   i.putExtra("AccountName",mAccountName.getText());
                                                   i.putExtra("AccountID",mAccountNumber);
                                                   context.startActivity(i);
                                               }
                                           }

            );

        }
    }


    public AddNewAccountAdapter(Context context, ArrayList<DynamoDBManager.VendorDetails> vendorDetailsArrayList) {
        this.vendorDetailsArrayList = vendorDetailsArrayList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_add_new_account, parent, false);
        return new MyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DynamoDBManager.VendorDetails vendorDetails = vendorDetailsArrayList.get(position);
        binderHelper.bind(holder.swipeLayout, vendorDetails.getVendorName());
        binderHelper.setOpenOnlyOne(true);
        holder.mAccountName.setText(vendorDetails.getVendorName());
        holder.mAccountNumber = vendorDetails.getVendorID();
        //String url = "https://s3.amazonaws.com/ximbo/vendorImages/" + vendorDetails.getVendorID() +".png";
        //holder.mAccountImage.setImageAlpha(movie.getImage());

        Uri uri = Uri.parse("https://s3.amazonaws.com/ximbo/vendorImages/" + vendorDetails.getVendorID() +".svg");
        requestBuilder = Glide.with(context)
                .using(Glide.buildStreamModelLoader(Uri.class, context), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_error)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());
        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                // SVG cannot be serialized so it's not worth to cache it
                .load(uri)
                .into(holder.mAccountImage);
//        Glide
//                .with(context)
//                .load(url)
//                .crossFade()
//                .placeholder(R.mipmap.ic_launcher)
//                .into(holder.mAccountImage);

    }

    @Override
    public int getItemCount() {
        return vendorDetailsArrayList.size();
    }


}

