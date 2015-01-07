package com.str2.ta_02studentattendancechecker;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.util.*;
import com.google.api.client.auth.oauth2.*;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by The Administrator on 1/7/2015.
 */
public class SettingsManager {

    private static String TAG = "Ta-02 S.A.C.";

    static final int REQUEST_CODE_PICK_ACCOUNT = 1;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 2;

    private static SpreadsheetService sheetService;

    private static Activity activity;
    private static String scope;
    private static String email;

    public static boolean isInternetPresent(){
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    public static boolean isAccountChosen(){
        return true; //for now
    }

    //when the "Pick Account" button is pressed
    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        Log.i(TAG, "Account Picker Intent will be sent");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    /*
    //in the Activity; for handling intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                Log.i(TAG, "Account Picker succeeded");
                new GetUsernameTask(MainActivity.this, mEmail, SCOPE).execute();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Log.i(TAG, "Account Picker failed");
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR){
            if(resultCode == RESULT_OK){
                Log.i(TAG, "Access granted");
                new GetUsernameTask(MainActivity.this, mEmail, SCOPE).execute();
            } else {
                Log.i(TAG, "Access denied");
                pickUserAccount();
            }
        }
    }
    */

    public class GetUsernameTask extends AsyncTask<String, Void, Void>{
        String scope;
        String email;

        GetUsernameTask(String name, String scope) {
            this.scope = scope;
            this.email = name;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(activity, email, scope);
            } catch (UserRecoverableAuthException userAuthEx) {
                Log.i(TAG, "A UserRecoverableAuthException occurred");
                //app not authorized for account
                Intent intent = userAuthEx.getIntent();
                activity.startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            } catch (GoogleAuthException fatalEx) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                Log.i(TAG, "A GoogleAuthException occurred");
                Log.i(TAG, fatalEx.getMessage());
            }
            return null;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String token = fetchToken();
                if (token != null) {
                    // Insert the good stuff here.
                    // Use the token to access the user's Google data.
                    sheetService = new SpreadsheetService("Ta-02 Student Attendance Checker");
                    sheetService.setOAuth2Credentials(new Credential(BearerToken
                            .authorizationHeaderAccessMethod())
                            .setFromTokenResponse(new TokenResponse().setAccessToken(token)));
                    Log.i(TAG, token);

                    try {
                        SheetEdit ce = new SheetEdit(sheetService, new PrintStream(System.out));
                        for (int j = 0; j < ce.getSheetList().size(); j++) {
                            Log.i("Sheetlist", ce.showSheet(j, ce.getSheetList()));
                        }
                    } catch (AuthenticationException authEx){
                        Log.e(TAG, "An Auth Exception occurred", authEx);
                        Log.i(TAG, Log.getStackTraceString(authEx));
                    } catch (IOException io){
                        Log.i(TAG, "An IOException occurred");
                        Log.i(TAG, io.toString());
                    } catch (ServiceException se){
                        Log.i(TAG, "A Service Exception occurred");
                        Log.i(TAG, se.toString());
                    } catch (Exception e){
                        Log.i(TAG, "A different exception occurred");
                        Log.i(TAG, e.toString());
                    }
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                Log.i(TAG, "An IOException occurred");
            }
            return null;
        }
    }

    //call these methods in a non-UI Thread (eg. AsyncTask)
    public static String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(activity, email, scope);
        } catch (UserRecoverableAuthException userAuthEx) {
            Log.i(TAG, "A UserRecoverableAuthException occurred");
            //app not authorized for account
            Intent intent = userAuthEx.getIntent();
            activity.startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
        } catch (GoogleAuthException fatalEx) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            Log.i(TAG, "A GoogleAuthException occurred");
            Log.i(TAG, fatalEx.getMessage());
        }
        return null;
    }

    public static void sheetMa(String... params) {
        try {
            String token = fetchToken();
            if (token != null) {
                // Insert the good stuff here.
                // Use the token to access the user's Google data.
                sheetService = new SpreadsheetService("Ta-02 Student Attendance Checker");
                sheetService.setOAuth2Credentials(new Credential(BearerToken
                        .authorizationHeaderAccessMethod())
                        .setFromTokenResponse(new TokenResponse().setAccessToken(token)));
                Log.i(TAG, token);

                try {
                    SheetEdit ce = new SheetEdit(sheetService, new PrintStream(System.out));
                    for (int j = 0; j < ce.getSheetList().size(); j++) {
                        Log.i("Sheetlist", ce.showSheet(j, ce.getSheetList()));
                    }
                } catch (AuthenticationException authEx){
                    Log.e(TAG, "An Auth Exception occurred", authEx);
                    Log.i(TAG, Log.getStackTraceString(authEx));
                } catch (IOException io){
                    Log.i(TAG, "An IOException occurred");
                    Log.i(TAG, io.toString());
                } catch (ServiceException se){
                    Log.i(TAG, "A Service Exception occurred");
                    Log.i(TAG, se.toString());
                } catch (Exception e){
                    Log.i(TAG, "A different exception occurred");
                    Log.i(TAG, e.toString());
                }
            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
            Log.i(TAG, "An IOException occurred");
        }
    }

    public static void setActivityVar(Activity a){
        activity = a;
    }
}
