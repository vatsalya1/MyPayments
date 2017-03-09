package com.example.vatsalya.mypayments.adapter;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;
import com.example.vatsalya.mypayments.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Vatsalya on 10/5/16.
 */

public class UserAccountAdapter extends RecyclerView.Adapter<UserAccountAdapter.UserAccountHolder>{

    private List<String> vendorAccountByUser;
    private ArrayList<String> listData;
    private LayoutInflater inflater;
    private ItemClickCallBack itemClickCallBack;
    private Context context;
    private GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

    public interface ItemClickCallBack {
        void onItemClick(int p , String[] mAccountName);
        void onDeleteClicked(int p, String mVendorAccountToDelete);
    }

    public void setItemClickCallBack (final ItemClickCallBack itemClickCallBack){
        this.itemClickCallBack = itemClickCallBack;

    }

    public UserAccountAdapter(List<String> vendorAccountByUser, Context c){
        this.context = c;
        this.vendorAccountByUser = vendorAccountByUser;
        listData = new ArrayList<> (vendorAccountByUser);
        this.inflater = LayoutInflater.from(c);

    }

    @Override
    public UserAccountHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.account_card,parent,false);

        return new UserAccountHolder(view);
    }

    @Override
    public void onBindViewHolder(UserAccountHolder holder, int position) {
        String item = listData.get(position);
        String[] mAccountName = item.split("\\-",0);
        holder.accountName.setText(item);
        Glide.clear(holder.accountImage);
        Glide.get(context).clearMemory();
        File cacheDir = Glide.getPhotoCacheDir(context);
        if (cacheDir.isDirectory()) {
            for (File child : cacheDir.listFiles()) {
                if (!child.delete()) {
                    Log.w(TAG, "cannot delete: " + child);
                }
            }
        }
        Uri uri = Uri.parse("https://s3.amazonaws.com/ximbo/vendorImages/" + mAccountName[0] + ".svg");
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
                .into(holder.accountImage);

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class UserAccountHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView accountName;
        private ImageView accountImage;
        private View container;
        private ImageButton deleteAccount;
        public UserAccountHolder(View itemView) {
            super(itemView);
            accountName = (TextView)itemView.findViewById(R.id.account_name);
            accountImage = (ImageView) itemView.findViewById(R.id.account_photo);
            container = itemView.findViewById(R.id.account_card);
            container.setOnClickListener(this);
            deleteAccount = (ImageButton)itemView.findViewById(R.id.delete_imageView);
            deleteAccount.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.account_card){
                String item = listData.get(getAdapterPosition());
                String[] mAccountName = item.split("\\-",0);
                itemClickCallBack.onItemClick(getAdapterPosition(), mAccountName);
            }
            if(v.getId() == R.id.delete_imageView){
                String item = listData.get(getAdapterPosition());
                String[] mAccountName = item.split("\\-",0);

                itemClickCallBack.onDeleteClicked(getAdapterPosition(), item);
            }

        }
    }
}
