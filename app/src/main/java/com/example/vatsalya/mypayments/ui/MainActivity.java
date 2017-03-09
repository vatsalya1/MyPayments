package com.example.vatsalya.mypayments.ui;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.vatsalya.mypayments.R;
import com.example.vatsalya.mypayments.adapter.AmazonClientManager;
import com.example.vatsalya.mypayments.adapter.DynamoDBManager;
import com.example.vatsalya.mypayments.adapter.UserAccountAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout = null;
    private ViewPager viewPager= null;
    private String userID= null;
    private FirebaseAuth mAuth = null;
    private Date mTodaysDate;
    private String mNewUser = null;
    private UserAccountAdapter userAccountAdapter = null;
    public static AmazonClientManager clientManager = null;
    private DynamoDBManager.UserDetails userDetails = null;
    private ViewPagerAdapter adapter = null;
    private Bundle bundle;
    private AccountFragment fragObj = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        userID = i.getStringExtra("UserID");
        mNewUser = i.getStringExtra("SignUpActivity");
        DateFormat df = new SimpleDateFormat("MMM d, yyyy");
        String now = df.format(new Date());
        Toolbar toolbar = (Toolbar)findViewById(R.id.mToolbar);
        toolbar.setTitle(now);

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_reorder_home_24dp);
        ab.setDisplayHomeAsUpEnabled(true);
        bundle = new Bundle();
        bundle.putString("userID", userID);


        userDetails = new DynamoDBManager.UserDetails();

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) findViewById(R.id.navigation_view);
        if (navView != null){
            setupDrawerContent(navView);
        }
        // using view pager so that more tabs can be added to the home screen
        viewPager = (ViewPager)findViewById(R.id.tab_viewpager);
        clientManager = new AmazonClientManager(this);

        setupAccountsViewPager(viewPager);


        mAuth = FirebaseAuth.getInstance();

    }

    private void setupAccountsViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putString("userID", userID);
        AccountFragment fragObj = new AccountFragment();
        fragObj.setArguments(bundle);
        adapter.addFrag(fragObj, "Coordinator Layout");
        viewPager.setAdapter(adapter);
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);

                switch (menuItem.getItemId()) {

                    case R.id.drawer_coordinator:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.drawer_signOut:
                        signOut();
                        break;

                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
    private void signOut() {
        mAuth.signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
    }


    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id){
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }



//
//public class GetVendorAccountByUserTask extends AsyncTask<Void, Void, List<String>> {
//
//    protected List<String> doInBackground(Void... inputs) {
//        String tableStatus = DynamoDBManager.getUserTableStatus1();
//        if (tableStatus.equalsIgnoreCase("ACTIVE")) {
//            userDetails = DynamoDBManager.getUserDetails(userID);
//            List<String> vendorAccount = userDetails.getVendorAccount();
//            return vendorAccount;
//        }
//        return null;
//    }
//
//    protected void onPostExecute(List<String> result) {
//
//        adapter.addFrag(fragObj, "Coordinator Layout");
//        viewPager.setAdapter(adapter);
//        }
//    }

}

