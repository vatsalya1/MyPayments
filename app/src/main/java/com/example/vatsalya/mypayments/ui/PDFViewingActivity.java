package com.example.vatsalya.mypayments.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.Constants;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.example.vatsalya.mypayments.adapter.Util;
import com.example.vatsalya.mypayments.step.StepFragmentVerifyAccountNumber;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;


import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import com.stepstone.stepper.StepperLayout;

/**
 * Created by vatsalya on 2/24/17.
 */

public class PDFViewingActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {


    private Toolbar myToolbar;
    public static final String MY_URI = "myURI";
    public static final String MY_KEY = "myKEY";
    private Uri uri;
    private PDFView pdfView;
    private String pdfFileName;
    private Integer pageNumber = 0;
    private static final String TAG = PDFViewActivity.class.getSimpleName();
    private String invoiceKey;
    public static AmazonClientManager clientManager = null;
    CognitoCachingCredentialsProvider credentialsProvider = null;
    AmazonS3 s3;
    // This is the main class for interacting with the Transfer Manager
    private TransferUtility transferUtility;
    private TransferObserver observer;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private long totalBytesToDownload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        transferUtility = Util.getTransferUtility(this);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        Intent i = getIntent();
        invoiceKey = i.getStringExtra(MY_KEY);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        pdfView.setVisibility(View.INVISIBLE);
        if (invoiceKey != null) {
            beginDownload(invoiceKey);
            // displayFromUri(uri);
        } else {
        }
    }


    private void beginDownload(String key) {
        // Location to download files from S3 to. You can choose any accessible
        // file.
        final File file = getFile(this);

        try {
            // Initiate the download
            observer = transferUtility.download(Constants.BUCKET_NAME, key, file);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "onStateChanged: " + id + ", " + state);
                if (state.COMPLETED.equals(observer.getState())) {
                    progressBar.setVisibility(View.INVISIBLE);
                    pdfView.setVisibility(View.VISIBLE);
                    pdfView.fromFile(file)
                            .defaultPage(pageNumber)
                            .onPageChange(PDFViewingActivity.this)
                            .enableAnnotationRendering(true)
                            .onLoad(PDFViewingActivity.this)
                            .scrollHandle(new DefaultScrollHandle(PDFViewingActivity.this))
                            .load();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                        id, bytesTotal, bytesCurrent));
                if (bytesTotal != 0) {
                    int current = (int) (bytesCurrent / bytesTotal) * 100;
                    progressBar.setProgress(current);
                } else {
                    progressBar.setProgress(0);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "onError: " + id, ex);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    } catch (AmazonS3Exception s3e){
            if(s3e.getStatusCode() == 404){
                Toast.makeText(this,"No PDF availaible for the given Invoice",Toast.LENGTH_SHORT).show()
;            }
        }

}

    public File getFile(Context context) {
        File file = null;
        try {
            String fileName = "test";
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observer != null) {
                observer.cleanTransferListener();
            finish();
            }
        }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
            Log.e(TAG, "title = " + meta.getTitle());
            Log.e(TAG, "author = " + meta.getAuthor());
            Log.e(TAG, "subject = " + meta.getSubject());
            Log.e(TAG, "keywords = " + meta.getKeywords());
            Log.e(TAG, "creator = " + meta.getCreator());
            Log.e(TAG, "producer = " + meta.getProducer());
            Log.e(TAG, "creationDate = " + meta.getCreationDate());
            Log.e(TAG, "modDate = " + meta.getModDate());

            printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }
    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));

    }

//    private class DownloadListener implements TransferListener, OnPageChangeListener, OnLoadCompleteListener {
//        // Simply updates the list when notified.
//
//        File file;
//        long totalBytes;
//        public DownloadListener (File file){
//            this.file = file;
//
//        }
//        @Override
//        public void onError(int id, Exception e) {
//            Log.e(TAG, "onError: " + id, e);
//            progressBar.setVisibility(View.INVISIBLE);
//            //updateList();
//        }
//
//        @Override
//        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//
//            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
//                    id, bytesTotal, bytesCurrent));
//            int current =   (int) (bytesCurrent / totalBytesToDownload) *100 ;
//            progressBar.setProgress(current);
//            //updateList();
//        }
//
//        @Override
//        public void onStateChanged(int id, TransferState state) {
//            Log.d(TAG, "onStateChanged: " + id + ", " + state);
//            if(state.COMPLETED.equals(observer.getState())){
//                progressBar.setVisibility(View.INVISIBLE);
//                pdfView.setVisibility(View.VISIBLE);
//                pdfView.fromFile(file)
//                        .defaultPage(pageNumber)
//                        .onPageChange(this)
//                        .enableAnnotationRendering(true)
//                        .onLoad(this)
//                        .scrollHandle(new DefaultScrollHandle(PDFViewingActivity.this))
//                        .load();
//            }
//            //updateList();
//        }
//
//        @Override
//        public void onPageChanged(int page, int pageCount) {
//            pageNumber = page;
//            setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
//        }
//
//        @Override
//        public void loadComplete(int nbPages) {
//            PdfDocument.Meta meta = pdfView.getDocumentMeta();
//            Log.e(TAG, "title = " + meta.getTitle());
//            Log.e(TAG, "author = " + meta.getAuthor());
//            Log.e(TAG, "subject = " + meta.getSubject());
//            Log.e(TAG, "keywords = " + meta.getKeywords());
//            Log.e(TAG, "creator = " + meta.getCreator());
//            Log.e(TAG, "producer = " + meta.getProducer());
//            Log.e(TAG, "creationDate = " + meta.getCreationDate());
//            Log.e(TAG, "modDate = " + meta.getModDate());
//
//            printBookmarksTree(pdfView.getTableOfContents(), "-");
//
//        }
//        public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
//            for (PdfDocument.Bookmark b : tree) {
//
//                Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));
//
//                if (b.hasChildren()) {
//                    printBookmarksTree(b.getChildren(), sep + "-");
//                }
//            }
//        }
//    }
}

