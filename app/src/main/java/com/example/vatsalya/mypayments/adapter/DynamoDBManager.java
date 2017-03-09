package com.example.vatsalya.mypayments.adapter;

/**
 * Created by Vatsalya on 9/25/16.
 */

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.example.vatsalya.mypayments.step.AbstractStepperActivity;
import com.example.vatsalya.mypayments.step.StepFragmentVerifyAccountNumber;
import com.example.vatsalya.mypayments.step.StepFragmentVerifyInvoices;
import com.example.vatsalya.mypayments.ui.AccountFragment;
import com.example.vatsalya.mypayments.ui.AddNewAccount;
import com.example.vatsalya.mypayments.ui.AllInvoicesFragment;
import com.example.vatsalya.mypayments.ui.MainActivity;
import com.example.vatsalya.mypayments.ui.SignupActivity;

import java.util.ArrayList;
import java.util.List;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";



  /*
     * Scans the table and returns the list of invoices.
     */
    public static ArrayList<InvoiceDetails> getInvoiceByVendor() {

        AmazonDynamoDBClient ddb = AllInvoicesFragment.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<InvoiceDetails> result = mapper.scan(
                    InvoiceDetails.class, scanExpression);

            ArrayList<InvoiceDetails> resultList = new ArrayList<InvoiceDetails>();
            for (InvoiceDetails up : result) {
                resultList.add(up);
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            AllInvoicesFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }
    /*
        * Retrieves all of the attribute/value pairs for the specified user.
        */
    public static UserDetails getUserDetails(String userID) {

        AmazonDynamoDBClient ddb = AccountFragment.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserDetails userDetails = mapper.load(UserDetails.class,
                    userID);

            return userDetails;

        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }
    /*
            * Retrieves all of the attribute/value pairs for the specified vendorAccount.
            */
    public static VendorAccountDetails getVendorAccountDetails(String vendorAccountID) {

        AmazonDynamoDBClient ddb = StepFragmentVerifyAccountNumber.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            VendorAccountDetails vendorAccountDetails = mapper.load(VendorAccountDetails.class,
                    vendorAccountID);


            return vendorAccountDetails;

        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }
    /*
    * Retrieves all of the attribute/value pairs for the specified invoice.
    */
    public static ArrayList<InvoiceDetails> getInvoiceDetailsForGivenAccount(String vendorAccountID,String invoiceNumber) {

        AmazonDynamoDBClient ddb = StepFragmentVerifyInvoices.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            InvoiceDetails invoiceToFind = new InvoiceDetails();
            invoiceToFind.setAccountCustomerID(vendorAccountID);

            Condition rangeKeyCondition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withS(invoiceNumber));

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(invoiceToFind)
                    .withRangeKeyCondition("invoiceNumber", rangeKeyCondition)
                    .withConsistentRead(false);
            PaginatedQueryList<InvoiceDetails> invoiceDetails = mapper.query(InvoiceDetails.class,
                    queryExpression);

            ArrayList<InvoiceDetails> resultList = new ArrayList<InvoiceDetails>();
            for (InvoiceDetails up : invoiceDetails) {
                resultList.add(up);
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
    * Retrieves all of the attribute/value pairs for the specified user.
    */
/*
     * Inserts ten users with userNo from 1 to 10 and random names.
     */
    public static AmazonServiceException insertUsers(UserDetails newUser) {
        Log.d(TAG, "Inserting user");
        AmazonDynamoDBClient ddb = SignupActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {

            Log.d(TAG, "Inserting user");
            mapper.save(newUser);
            Log.d(TAG, "User inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
            return ex;
        }
        return null;

    }

    public static AmazonServiceException insertAccountInUser(String userID, String vendorAccountID) {
        Log.d(TAG, "Inserting user");
        AmazonDynamoDBClient ddb = AbstractStepperActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {


            List<String> vendorAccountList = new ArrayList<>();
            //vendorAccountList.add(vendorAccountID);
            Log.d(TAG, "Inserting account in user table");
            UserDetails userDetails = mapper.load(UserDetails.class, userID);
            vendorAccountList = userDetails.getVendorAccount();
            int listSize = vendorAccountList.size();
            vendorAccountList.add(listSize -1,vendorAccountID);
            userDetails.setVendorAccount(vendorAccountList);
            mapper.save(userDetails);
            Log.d(TAG, "Account inserted");

        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
            return ex;
        }
        return null;

    }
    public static UserDetails deleteAccountInUser(String userID, String vendorAccountID) {
        Log.d(TAG, "Deleting Account");
        AmazonDynamoDBClient ddb = AccountFragment.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);


            List<String> vendorAccountList = new ArrayList<>();
            //vendorAccountList.add(vendorAccountID);
            Log.d(TAG, "Deleting account in user table");
            UserDetails userDetails = mapper.load(UserDetails.class, userID);
            vendorAccountList = userDetails.getVendorAccount();
            vendorAccountList.remove(vendorAccountID);
            userDetails.setVendorAccount(vendorAccountList);
            mapper.save(userDetails);
            Log.d(TAG, "Account inserted");
            return userDetails;

    }
    public static String getUserTableStatus() {

        try {
            AmazonDynamoDBClient ddb = SignupActivity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.USER_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getUserTableStatusFromAbstractStepperActivity() {

        try {
            AmazonDynamoDBClient ddb = AbstractStepperActivity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.USER_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
    public static String getUserTableStatusFromAccountFragment() {

        try {
            AmazonDynamoDBClient ddb = AccountFragment.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.USER_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
    public static String getVendorAccountTableStatus() {

        try {
            AmazonDynamoDBClient ddb = StepFragmentVerifyAccountNumber.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.VENDOR_ACCOUNT_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
    public static String getUserTableStatus1() {

        try {
            AmazonDynamoDBClient ddb = MainActivity.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.USER_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            SignupActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
    public static String getInvoiceTableStatusFromFragment() {

        try {
            AmazonDynamoDBClient ddb = AllInvoicesFragment.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.INVOICE_TABLE_NAME) ;
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
    public static String getInvoiceTableStatusFromStep() {

        try {
            AmazonDynamoDBClient ddb = StepFragmentVerifyInvoices.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.INVOICE_TABLE_NAME) ;
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getUserTableStatusFromFragment() {

        try {
            AmazonDynamoDBClient ddb = AccountFragment.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.USER_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            AccountFragment.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static String getVendorTableStatus() {

        try {
            AmazonDynamoDBClient ddb = AddNewAccount.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.VENDOR_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            AddNewAccount.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }
/*
     * Scans the table and returns the list of invoices.
     */
   public static ArrayList<VendorDetails> getVendorDetails() {

        AmazonDynamoDBClient ddb = AddNewAccount.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<VendorDetails> result = mapper.scan(
                    VendorDetails.class, scanExpression);

            ArrayList<VendorDetails> resultList = new ArrayList<VendorDetails>();
            for (VendorDetails up : result) {
                resultList.add(up);
            }
            return resultList;

        } catch (AmazonServiceException ex) {
            AddNewAccount.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }



    @DynamoDBTable(tableName = Constants.TEST_TABLE_NAME)
    public static class InvoiceDetails {
        //private int accountID;
        private String accountCustomerID;;
        private String customerNumber;
        private String invoiceNumber;
        private String invoiceDate;
        private String dueDate;
        private float invoiceAmount;

//        @DynamoDBHashKey(attributeName = "accountID")
//        public int getAccountID() {
//            return accountID;
//        }
//
//        public void setAccountID(int accountID) {
//            this.accountID = accountID;
//        }
        @DynamoDBHashKey(attributeName = "accountCustomerID")
        public String getAccountCustomerID() {
            return accountCustomerID;
        }

        public void setAccountCustomerID(String accountCustomerID) {
            this.accountCustomerID = accountCustomerID;
        }

        @DynamoDBAttribute(attributeName = "customerNumber")
        public String getCustomerNumber() {
            return customerNumber;
        }

        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }

        @DynamoDBRangeKey(attributeName = "invoiceNumber")
        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }


        @DynamoDBAttribute(attributeName = "invoiceDate")
        public String getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        @DynamoDBAttribute(attributeName = "dueDate")
        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        @DynamoDBAttribute(attributeName = "invoiceAmount")
        public float getInvoiceAmount() {
            return invoiceAmount;
        }

        public void setInvoiceAmount(float invoiceAmount) {
            this.invoiceAmount = invoiceAmount;
        }
    }

    @DynamoDBTable(tableName = Constants.USER_TABLE_NAME)
    public static class UserDetails {
        private String userID;
        private String emailAddress;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String notificationID;
        private List<String> vendorAccount;


        @DynamoDBHashKey(attributeName = "userID")
        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        @DynamoDBAttribute(attributeName = "emailAddress")
        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        @DynamoDBAttribute(attributeName = "firstName")
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }


        @DynamoDBAttribute(attributeName = "lastName")
        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @DynamoDBAttribute(attributeName = "phoneNumber")
        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @DynamoDBAttribute(attributeName = "notificationID")
        public String getNotificationID() {
            return notificationID;
        }

        public void setNotificationID(String notificationID) {
            this.notificationID = notificationID;
        }

        @DynamoDBAttribute(attributeName = "vendorAccount")
        public List<String> getVendorAccount() {
            return vendorAccount;
        }

        public void setVendorAccount(List<String> vendorAccount) {
            this.vendorAccount = vendorAccount;
        }

    }

    @DynamoDBTable(tableName = Constants.VENDOR_TABLE_NAME)
    public static class VendorDetails {
        private String vendorID;
        private String vendorName;

        @DynamoDBHashKey(attributeName = "vendorID")
        public String getVendorID() {
            return vendorID;
        }

        public void setVendorID(String vendorID) {
            this.vendorID = vendorID;
        }

        @DynamoDBAttribute(attributeName = "vendorName")
        public String getVendorName() {
            return vendorName;
        }

        public void setVendorName(String vendorName) {
            this.vendorName = vendorName;
        }

    }
    @DynamoDBTable(tableName = Constants.VENDOR_ACCOUNT_TABLE_NAME)
    public static class VendorAccountDetails {
        private String vendorAccountID;
        private String vendorAccountName;

        @DynamoDBHashKey(attributeName = "vendorAccountID")
        public String getVendorAccountID() {
            return vendorAccountID;
        }

        public void setVendorAccountID(String vendorAccountID) {
            this.vendorAccountID = vendorAccountID;
        }

        @DynamoDBAttribute(attributeName = "vendorAccountName")
        public String getVendorAccountName() {
            return vendorAccountName;
        }

        public void setVendorAccountName(String vendorAccountName) {
            this.vendorAccountName = vendorAccountName;
        }

    }
}