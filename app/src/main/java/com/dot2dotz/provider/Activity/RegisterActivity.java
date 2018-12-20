package com.dot2dotz.provider.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dot2dotz.provider.Adapter.RegisterDocAdapter;
import com.dot2dotz.provider.Bean.Document;
import com.dot2dotz.provider.Bean.ServiceTypes;
import com.dot2dotz.provider.Dot2dotzApplication;
import com.dot2dotz.provider.Helper.AppHelper;
import com.dot2dotz.provider.Helper.ConnectionHelper;
import com.dot2dotz.provider.Helper.CustomDialog;
import com.dot2dotz.provider.Helper.SharedHelper;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Helper.VolleyMultipartRequest;
import com.dot2dotz.provider.Listeners.AdapterImageUpdateListener;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.MyTextView;
import com.dot2dotz.provider.Utilities.Utilities;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.rilixtech.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.dot2dotz.provider.Dot2dotzApplication.trimMessage;

public class RegisterActivity extends AppCompatActivity implements
        RegisterDocAdapter.ServiceClickListener {

    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    public static final String TAGG = "DocumentActivity";
    private static final int SELECT_PHOTO = 100;
    public static int APP_REQUEST_CODE = 99;
    public static int deviceHeight;
    public static int deviceWidth;
    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String device_token, device_UDID;
    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email, first_name, last_name, mobile_no, password, referral_code;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Boolean fromActivity = false;
    String strViewPager = "";
    Spinner service_type;
    CountryCodePicker ccp;
    RecyclerView recyclerView;
    ImageView uploadImg;
    ArrayList<Document> documentArrayList;
    ArrayList<ServiceTypes> serviceTypeArrayList;
    ArrayList<String> serviceList;
    RegisterDocAdapter documentAdapter;
    ArrayAdapter<String> serviceAdapter;
    Boolean isPermissionGivenAlready = false;
    Document updatedDocument;
    int position = -1;
    ServiceTypes serviceTypes;
    AdapterImageUpdateListener imageUpdateListener;
    AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder;
    UIManager uiManager;
    Spinner serviceSpinner;
    Utilities utils = new Utilities();
    private File des_file;
    private MyTextView mCountryNumber;
    private File isImageFile = null;
    private String blockCharacterSet = "~#^|$%&*!()_-*.,@/";
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    private static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        Log.e(TAGG, "getBitmapFromUri: Resize uri" + uri);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        Log.e(TAGG, "getBitmapFromUri: Height" + deviceHeight);
        Log.e(TAGG, "getBitmapFromUri: width" + deviceWidth);
        int maxSize = Math.min(deviceHeight, deviceWidth);
        if (image != null) {
            Log.e(TAGG, "getBitmapFromUri: Width" + image.getWidth());
            Log.e(TAGG, "getBitmapFromUri: Height" + image.getHeight());
            int inWidth = image.getWidth();
            int inHeight = image.getHeight();
            int outWidth;
            int outHeight;
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            return Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
        } else {
            Toast.makeText(context, context.getString(R.string.valid_image), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Utilities.setLanguage(RegisterActivity.this);
        try {
            Intent intent = getIntent();
            if (intent != null) {

//                if (getIntent().getExtras().containsKey("viewpager")) {
//                    strViewPager = getIntent().getExtras().getString("viewpager");
//                }

//                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
//                    fromActivity = true;
//                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
//                    fromActivity = false;
//                } else {
//                    fromActivity = false;
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }
        findViewById();
        GetToken();
        setupRecyclerView();
        setupServiceSpinner();
        getServiceTypes();
        getDocList();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Pattern ps = Pattern.compile(".*[0-9].*");
                Matcher firstName = ps.matcher(first_name.getText().toString());
                Matcher lastName = ps.matcher(last_name.getText().toString());

                if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                    displayMessage(getString(R.string.email_validation));
                } else if (!Utilities.isValidEmail(email.getText().toString())) {
                    displayMessage(getString(R.string.not_valid_email));
                } else if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                    displayMessage(getString(R.string.first_name_empty));
                } else if (firstName.matches()) {
                    displayMessage(getString(R.string.first_name_no_number));
                } else if (last_name.getText().toString().equals("") || last_name.getText().toString().equalsIgnoreCase(getString(R.string.last_name))) {
                    displayMessage(getString(R.string.last_name_empty));
                } else if (lastName.matches()) {
                    displayMessage(getString(R.string.last_name_no_number));
                } else if (mobile_no.getText().toString().length() < 10 ||
                        mobile_no.getText().toString().equals("") ||
                        mobile_no.getText().toString().equalsIgnoreCase(getString(R.string.mobile_no))) {
                    displayMessage(getString(R.string.mobile_number_empty));
                } else if (password.getText().toString().equals("") || password.getText().toString().equalsIgnoreCase(getString(R.string.password_txt))) {
                    displayMessage(getString(R.string.password_validation));
                } else if (password.length() < 6 || password.length() > 16) {
                    displayMessage(getString(R.string.password_validation1));
                }/* else if (!Utilities.isValidPassword(password.getText().toString().trim())) {
                    displayMessage(getString(R.string.password_validation2));
                }*/ else {
                    if (documentArrayList.size() > 0) {
                        boolean ischeck = true;
                        for (int i = 0; i < documentArrayList.size(); i++) {
                            if (documentAdapter.getServiceListModel().get(i).getBitmap() == null) {
                                ischeck = false;
                                displayMessage(documentAdapter.getServiceListModel().get(i).getName() + " required");
                                return;
                            }
                        }

                        if (ischeck) {
                            if (isInternet) {
                                checkMailAlreadyExit();
                            } else {
                                displayMessage(getString(R.string.something_went_wrong_net));
                            }
                        }

                    }
                }

            }

        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    public void findViewById() {
        mCountryNumber = findViewById(R.id.countryNumber);
        email = (EditText) findViewById(R.id.email);
        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        mobile_no = (EditText) findViewById(R.id.mobile_no);
        password = (EditText) findViewById(R.id.password);
        referral_code = (EditText) findViewById(R.id.referral_code);
        nextICON = (FloatingActionButton) findViewById(R.id.nextIcon);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        email.setText(SharedHelper.getKey(context, "email"));
        first_name.setFilters(new InputFilter[]{filter});
        last_name.setFilters(new InputFilter[]{filter});
        //  ccp = (CountryCodePicker) findViewById(R.id.ccp);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        uploadImg = (ImageView) findViewById(R.id.upload_img);

        service_type = (Spinner) findViewById(R.id.service_type);
        serviceList = new ArrayList<>();
        // ccp.registerPhoneNumberTextView(mobile_no);
    }

    public void checkMailAlreadyExit() {
        customDialog = new CustomDialog(RegisterActivity.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("URL: ", URLHelper.CHECK_MAIL_ALREADY_REGISTERED);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CHECK_MAIL_ALREADY_REGISTERED, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                phoneLogin();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = null;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                if (json.startsWith("The email has already been taken")) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                                //displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        checkMailAlreadyExit();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("mobile", SharedHelper.getKey(RegisterActivity.this, "mobile"));
                object.put("password", SharedHelper.getKey(RegisterActivity.this, "password"));
                utils.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("URL: ", URLHelper.login);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    getProfile();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            utils.print("ErrorInLoginAPI", "" + errorObj.toString());

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500 || response.statusCode == 401) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 422) {
                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }
                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            signIn();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };

            Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();

            Log.e("URL: ", URLHelper.USER_PROFILE_API);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.USER_PROFILE_API, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(RegisterActivity.this, "id", response.optString("id"));
                    SharedHelper.putKey(RegisterActivity.this, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(RegisterActivity.this, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(RegisterActivity.this, "email", response.optString("email"));
                    if (response.optString("avatar").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("avatar"));
                    else
                        SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("avatar"));
                    SharedHelper.putKey(RegisterActivity.this, "gender", "" + response.optString("gender"));
                    SharedHelper.putKey(RegisterActivity.this, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "approval_status", response.optString("status"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));

                    if (response.optJSONObject("service") != null) {
                        JSONObject service = response.optJSONObject("service");
                        JSONObject serviceType = service.optJSONObject("service_type");
                        SharedHelper.putKey(context, "provider_id", service.optString("provider_id"));
                        SharedHelper.putKey(context, "service", serviceType.optString("name"));
                    }
                    SharedHelper.putKey(RegisterActivity.this, "login_by", "manual");
                    GoToMainActivity();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (customDialog != null && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 401) {
                                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                                GoToBeginActivity();
                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }


                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            getProfile();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(RegisterActivity.this, "access_token"));
                    return headers;
                }
            };

            Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    public void phoneLogin() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        uiManager = new SkinManager(SkinManager.Skin.TRANSLUCENT,
                ContextCompat.getColor(this, R.color.cancel_ride_color), R.drawable.banner, SkinManager.Tint.WHITE, 85);
        configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        configurationBuilder.setUIManager(uiManager);
        configurationBuilder.setUIManager(uiManager);
        configurationBuilder.setSMSWhitelist(new String[]{"IN"});
        configurationBuilder.setDefaultCountryCode("IN");
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.setInitialPhoneNumber(new PhoneNumber("+91", mobile_no.getText().toString(), "IN")).build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            if (data != null) {
                AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.e(TAG, "onSuccess: Account Kit" + account.getId());
                        Log.e(TAG, "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());
                        if (AccountKit.getCurrentAccessToken().getToken() != null) {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", AccountKit.getCurrentAccessToken().getToken());
                            PhoneNumber phoneNumber = account.getPhoneNumber();
                            String phoneNumberString = phoneNumber.toString();
                            SharedHelper.putKey(RegisterActivity.this, "mobile", phoneNumberString);
                            signupFinal();
                        } else {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                            SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.False));
                            SharedHelper.putKey(context, "email", "");
                            SharedHelper.putKey(context, "login_by", "");
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
//                            Intent goToLogin = new Intent(RegisterActivity.this, SignIn.class);
//                            goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(goToLogin);
                            finish();
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Log.e(TAG, "onError: Account Kit" + accountKitError);
                        displayMessage("" + getResources().getString(R.string.registration_failed));
                    }
                });
                if (loginResult != null) {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                } else {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                }
                String toastMessage;
                if (loginResult.getError() != null) {
                    toastMessage = loginResult.getError().getErrorType().getMessage();
                    // showErrorActivity(loginResult.getError());
                } else if (loginResult.wasCancelled()) {
                    toastMessage = "Login Cancelled";
                } else {
                    if (loginResult.getAccessToken() != null) {
                        Log.e(TAG, "onActivityResult: Account Kit" + loginResult.getAccessToken().toString());
                        SharedHelper.putKey(this, "account_kit", loginResult.getAccessToken().toString());
                        toastMessage = "Welcome to Moovr...";
                    } else {
                        SharedHelper.putKey(this, "account_kit", "");
                        toastMessage = String.format(
                                "Welcome to Moovr...",
                                loginResult.getAuthorizationCode().substring(0, 10));
                    }
                }

            }
        }
             /* if (requestCode == SELECT_PHOTO && resultCode == activity.RESULT_OK &&
                      data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    try {
                        //bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                        Bitmap resizeImg = getBitmapFromUri(this, uri);
                        if (resizeImg != null && uri!= null && AppHelper.getPath(this, uri)!=null) {
                            Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg, AppHelper.getPath(this, uri));
                            uploadImg.setImageBitmap(reRotateImg);
                            updatedDocument.setBitmap(reRotateImg);

                  imageUpdateListener = (AdapterImageUpdateListener) documentAdapter;
                    imageUpdateListener.onImageSelectedUpdate(reRotateImg, position);

                            documentAdapter.setList(documentArrayList);
                            documentAdapter.notifyDataSetChanged();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
*/
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                e.printStackTrace();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                if (type == 0) {
                    try {
                        Uri capturedImage = Uri.parse(
                                android.provider.MediaStore.Images.Media.insertImage(
                                        getContentResolver(),
                                        imageFile.getAbsolutePath(), null, null));
                        cropImage(capturedImage, imageFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri croppedURI = UCrop.getOutput(data);
            try {
                if (croppedURI != null) {
                    Bitmap resizeImg = getBitmapFromUri(this, croppedURI);
                    if (resizeImg != null && croppedURI != null &&
                            AppHelper.getPath(this, croppedURI) != null) {
                        Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg,
                                AppHelper.getPath(this, croppedURI));
                        updatedDocument.setBitmap(reRotateImg);
                        documentAdapter.setList(documentArrayList);
                        documentAdapter.notifyDataSetChanged();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(RegisterActivity.this)
                    .load(isImageFile)
                    .placeholder(R.drawable.ic_dummy_user)
                    .error(R.drawable.ic_dummy_user)
                    .into(uploadImg);
        }
    }

    private void cropImage(Uri mImageCaptureUri, File imageFile) {
        des_file = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setToolbarTitle(getString(R.string.edit_document_photo));

        UCrop.of(mImageCaptureUri, Uri.fromFile(des_file))
                .withAspectRatio(1, 1)
                .withMaxResultSize(720, 720)//.withMaxResultSize(384, 384)
                .withOptions(options)
                .start(this);
    }

    private void signupFinal() {
        if (helper.isConnectingToInternet()) {

            final CustomDialog customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            customDialog.show();

            Log.e("URL: ", URLHelper.register);
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URLHelper.register, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    customDialog.dismiss();
                    utils.print("SignInResponse", response.toString());
                    SharedHelper.putKey(RegisterActivity.this, "mobile", mobile_no.getText().toString().replace("+91", ""));
                    SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
                    signIn();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                try {
                                    displayMessage(errorObj.optString("message"));
                                } catch (Exception e) {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                            } else if (response.statusCode == 401) {
                                GoToBeginActivity();
                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

                            } else if (response.statusCode == 503) {
                                displayMessage(getString(R.string.server_down));
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }

                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            signupFinal();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> object = new HashMap<>();
                    try {
                        object.put("device_type", "android");
                        object.put("device_id", device_UDID);
                        object.put("device_token", device_token);
                        object.put("login_by", "manual");
                        object.put("first_name", first_name.getText().toString());
                        object.put("last_name", last_name.getText().toString());
                        object.put("email", email.getText().toString());
                        object.put("password", password.getText().toString());
                        object.put("password_confirmation", password.getText().toString());
                        object.put("referral_code", referral_code.getText().toString());
                        object.put("mobile", SharedHelper.getKey(context, "mobile"));

                        //Car Details
                        String car_type = service_type.getAdapter().getItem(service_type.getSelectedItemPosition()).toString();
                        Log.e("car_type_selected", car_type);

                        if (!car_type.equalsIgnoreCase("") && car_type != null) {
                            for (ServiceTypes type : serviceTypeArrayList) {
                                if (type.getName().equals(car_type)) {
                                    object.put("service_type", String.valueOf(type.getId()));
                                }
                            }
                        }

                        Log.e(TAG, "signupFinal: " +
                                documentAdapter.getServiceListModel().toString());

                        for (Document document : documentAdapter.getServiceListModel()) {
                            if (document.getBitmap() != null) {
                                String key = "expires_at[" + document.getId() + "]";
                                object.put(key, document.getExpdate());
                            }
                        }

                        utils.print("InputToRegisterAPI", "" + object);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return object;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    //headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }

                @Override
                protected Map<String, VolleyMultipartRequest.DataPart> getByteData() throws AuthFailureError {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();

                    for (Document document : documentAdapter.getServiceListModel()) {
                        if (document.getBitmap() != null) {
                            String photo = "photos[" + document.getId() + "]";
                            params.put(photo, new VolleyMultipartRequest.DataPart("doc.jpg", AppHelper.getFileDataFromDrawable(document.getBitmap()), "image/jpeg"));
                        }
                    }

                    return params;
                }
            };

            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Dot2dotzApplication.getInstance().addToRequestQueue(volleyMultipartRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                utils.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                utils.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            utils.print(TAG, "Failed to complete token refresh");
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            utils.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            utils.print(TAG, "Failed to complete device UDID");
        }
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupRecyclerView() {
        documentArrayList = new ArrayList<>();
        documentAdapter = new RegisterDocAdapter(documentArrayList, context);
        documentAdapter.setHasStableIds(true);
        documentAdapter.setServiceClickListener(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        RegisterActivity.ItemOffsetDecoration itemDecoration = new RegisterActivity.ItemOffsetDecoration(context, R.dimen._5sdp);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(documentAdapter);
    }

    private void setupServiceSpinner() {
        try {
            ServiceTypes types = new ServiceTypes();
            /*types.setId(0);
            types.setName("Select Service");*/
            serviceList.add(getResources().getString(R.string.select_service));
            serviceAdapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, serviceList);
            serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            service_type.setAdapter(serviceAdapter);
        } catch (Exception setupServiceException) {
            setupServiceException.printStackTrace();
        }
    }

    @Override
    public void onDocImgClick(Document document, int position) {
        updatedDocument = document;
        this.position = position;
        galleryIntent();
       /* if (checkStoragePermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        else
            goToImageIntent();*/

    }

    private void galleryIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                EasyImage.openChooserWithDocuments(RegisterActivity.this, "Select", 0);
            else
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else EasyImage.openChooserWithDocuments(RegisterActivity.this, "Select", 0);
    }

  /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGivenAlready) {
                        goToImageIntent();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permissions.length == 0) {
            return;
        }
        boolean allPermissionsGranted = true;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        }
        if (!allPermissionsGranted) {
            boolean somePermissionsForeverDenied = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied
                    Log.e("denied", permission);
                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.e("allowed", permission);
                    } else {
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if (somePermissionsForeverDenied) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for this action. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            switch (requestCode) {
                case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0) {
                        boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        boolean permission2 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (permission1 && permission2) galleryIntent();
                        else
                            Toast.makeText(getApplicationContext(), "Please give permission", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }



    private void getServiceTypes() {
        try {
            Ion.with(this)
                    .load(URLHelper.GET_SERVICE_TYPE)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            // do stuff with the result or error


                            if (result == null ) {
                                //Toast.makeText(RegisterActivity.this, getResources().getString(R.string.Updating_vehicle_types), Toast.LENGTH_LONG).show();
                                //  finish();
                                getServiceTypes();
                                return;
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(result.toString());

                                if (jsonObject.optJSONArray("service_type") != null) {

                                    Log.e("get_service_types", jsonObject.optJSONArray("service_type").toString());

                                    serviceTypeArrayList = new ArrayList<>();
                                    //serviceList = new ArrayList<String>();

                                    JSONArray serviceTypeArray = jsonObject.optJSONArray("service_type");

                                    for (int i = 0; i < serviceTypeArray.length(); i++) {
                                        serviceTypes = new ServiceTypes();

                                        JSONObject serviceObject = serviceTypeArray.getJSONObject(i);

                                        if (serviceObject != null) {
                                            serviceList.add(serviceObject.optString("name"));

                                            serviceTypes.setId(serviceObject.optInt("id"));
                                            serviceTypes.setName(serviceObject.optString("name"));
                                            serviceTypeArrayList.add(serviceTypes);
                                        }
                                    }

                                    if (serviceList.size() > 0) {
                                        serviceAdapter = new ArrayAdapter<String>(RegisterActivity.this, android.R.layout.simple_spinner_item, serviceList);
                                        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        service_type.setAdapter(serviceAdapter);
                                    }

                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
        } catch (Exception serviceTypeException) {
            serviceTypeException.printStackTrace();
        }
    }

    private void getDocList() {
        if (helper.isConnectingToInternet()) {
            final CustomDialog customDialog = new CustomDialog(activity);
            customDialog.setCancelable(false);
            customDialog.show();

            Log.e("URL: ", URLHelper.GET_REGISTER_DOC);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.GET_REGISTER_DOC, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject result) {
                    customDialog.dismiss();
                    JSONArray response = result.optJSONArray("document");
                    Log.e(TAG, "onResponse: " + response.toString());
                    if (response.length() > 0) {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject doc = response.optJSONObject(i);
                            Document document = new Document();
                            document.setId(doc.optString("id"));
                            document.setName(doc.optString("name"));
                            document.setType(doc.optString("type"));
                            document.setImg(doc.optString("url"));
//                            JSONObject docObj = doc.optJSONObject("document");
//                            try {
//                                if (docObj != null) {
//                                    document.setImg(docObj.optString("url"));
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            documentArrayList.add(document);
                        }
                        if (documentArrayList.size() > 0) {
                            documentAdapter = new RegisterDocAdapter(documentArrayList, context);
                            documentAdapter.setServiceClickListener(RegisterActivity.this);
                            recyclerView.setAdapter(documentAdapter);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);

                    if (response != null && response.data != null) {
                        utils.print("MyTestError1", "" + response.statusCode);
                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));
                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                displayMessage(getString(R.string.something_went_wrong));
                            } else if (response.statusCode == 401) {
                                displayMessage(getString(R.string.invalid_credentials));
                            } else if (response.statusCode == 422) {
                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }

                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            getDocList();
                        }
                    }

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };
            Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    private class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

}