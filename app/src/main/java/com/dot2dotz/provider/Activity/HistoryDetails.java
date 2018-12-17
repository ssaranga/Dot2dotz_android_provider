package com.dot2dotz.provider.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.dot2dotz.provider.Adapter.TripAdapter;
import com.dot2dotz.provider.Bean.Flows;
import com.dot2dotz.provider.Dot2dotzApplication;
import com.dot2dotz.provider.Helper.ConnectionHelper;
import com.dot2dotz.provider.Helper.CustomDialog;
import com.dot2dotz.provider.Helper.SharedHelper;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Helper.User;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.MyBoldTextView;
import com.dot2dotz.provider.Utilities.ScreenshotType;
import com.dot2dotz.provider.Utilities.ScreenshotUtils;
import com.dot2dotz.provider.Utilities.Utilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.dot2dotz.provider.Dot2dotzApplication.trimMessage;

public class HistoryDetails extends AppCompatActivity {

    public JSONObject jsonObject;
    Activity activity;
    Context context;
    Boolean isInternet;
    ConnectionHelper helper;
    CustomDialog customDialog;
    TextView tripAmount, tripDate;
    TextView paymentType;
    TextView tripComments, tripProviderName, tripSource, lblTitle, tripId;
    TextView invoice_txt;
    TextView txt04Total, txt04AmountToPaid;
    ImageView tripImg, tripProviderImg, paymentTypeImg;
    RatingBar tripProviderRating;
    View viewLayout;
    ImageView backArrow, iv_shareInvoice;
    LinearLayout parentLayout, lnrComments, lnrInvoiceSub, lnrInvoice;
    String tag = "";
    Button btnCancelRide, btnClose, btnViewInvoice;
    Utilities utils = new Utilities();
    TextView lblBookingID, lblDistanceCovered, lblTimeTaken, lblBasePrice, lblDistancePrice, lblTaxPrice, lblWaitingPrice;
    LinearLayout lnrBookingID, lnrDistanceTravelled, lnrTimeTaken, lnrBaseFare, lnrDistanceFare, lnrTax;
    RecyclerView tripRv;
    TripAdapter tripAdapter;
    ArrayList<Flows> tripArrayList = new ArrayList<>();
    private MyBoldTextView discount;
    private LinearLayout discountLayout;
    RelativeLayout rel_header;
    ScrollView sv_parentInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
        Utilities.setLanguage(HistoryDetails.this);
        findViewByIdAndInitialize();
        try {
            Intent intent = getIntent();
            String post_details = intent.getExtras().getString("post_value");
            tag = intent.getExtras().getString("tag");
            jsonObject = new JSONObject(post_details);
        } catch (Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        //setup recycler view
        tripArrayList = new ArrayList<>();
        tripRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        tripAdapter = new TripAdapter(tripArrayList, context);
        tripRv.setAdapter(tripAdapter);

        if (jsonObject != null) {

            if (tag.equalsIgnoreCase("past_deliveries")) {
                btnCancelRide.setVisibility(View.GONE);
                lnrComments.setVisibility(View.VISIBLE);
                getRequestDetails();
                lblTitle.setText(getResources().getString(R.string.past_trips));
                btnViewInvoice.setVisibility(View.VISIBLE);
            } else {
                btnCancelRide.setVisibility(View.VISIBLE);
                lnrComments.setVisibility(View.GONE);
                getUpcomingDetails();
                lblTitle.setText(getResources().getString(R.string.upcoming_trips));
                btnViewInvoice.setVisibility(View.GONE);
            }
        }
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void findViewByIdAndInitialize() {
        activity = HistoryDetails.this;
        context = HistoryDetails.this;
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        parentLayout.setVisibility(View.GONE);
        tripAmount = (TextView) findViewById(R.id.tripAmount);
        tripDate = (TextView) findViewById(R.id.tripDate);
        paymentType = (TextView) findViewById(R.id.paymentType);
        paymentTypeImg = (ImageView) findViewById(R.id.paymentTypeImg);
        tripProviderImg = (ImageView) findViewById(R.id.tripProviderImg);
        tripRv = (RecyclerView) findViewById(R.id.trip_rv);
        tripImg = (ImageView) findViewById(R.id.tripImg);
        tripComments = (TextView) findViewById(R.id.tripComments);
        tripProviderName = (TextView) findViewById(R.id.tripProviderName);
        tripProviderRating = (RatingBar) findViewById(R.id.tripProviderRating);
        tripSource = (TextView) findViewById(R.id.tripSource);
        invoice_txt = (TextView) findViewById(R.id.invoice_txt);
        txt04Total = (TextView) findViewById(R.id.txt04Total);
        txt04AmountToPaid = (TextView) findViewById(R.id.txt04AmountToPaid);
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        tripId = (TextView) findViewById(R.id.trip_id);
        viewLayout = (View) findViewById(R.id.ViewLayout);
        btnCancelRide = (Button) findViewById(R.id.btnCancelRide);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnViewInvoice = (Button) findViewById(R.id.btnViewInvoice);
        lnrComments = (LinearLayout) findViewById(R.id.lnrComments);
        lnrInvoice = (LinearLayout) findViewById(R.id.lnrInvoice);
        lnrInvoiceSub = (LinearLayout) findViewById(R.id.lnrInvoiceSub);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        discount = findViewById(R.id.discount);
        discountLayout = findViewById(R.id.discountLayout);
        iv_shareInvoice = (ImageView) findViewById(R.id.iv_shareInvoice);
        rel_header = (RelativeLayout) findViewById(R.id.rel_header);
        sv_parentInvoice = (ScrollView) findViewById(R.id.sv_parentInvoice);

        lnrBookingID = (LinearLayout) findViewById(R.id.lnrBookingID);
        lnrDistanceTravelled = (LinearLayout) findViewById(R.id.lnrDistanceTravelled);
        lnrTimeTaken = (LinearLayout) findViewById(R.id.lnrTimeTaken);
        lnrBaseFare = (LinearLayout) findViewById(R.id.lnrBaseFare);
        lnrDistanceFare = (LinearLayout) findViewById(R.id.lnrDistanceFare);
        lnrTax = (LinearLayout) findViewById(R.id.lnrTax);

        lblBookingID = (TextView) findViewById(R.id.lblBookingID);
        lblDistanceCovered = (TextView) findViewById(R.id.lblDistanceCovered);
        lblTimeTaken = (TextView) findViewById(R.id.lblTimeTaken);
        lblBasePrice = (TextView) findViewById(R.id.lblBasePrice);
        lblTaxPrice = (TextView) findViewById(R.id.lblTaxPrice);
        lblDistancePrice = (TextView) findViewById(R.id.lblDistancePrice);
        lblWaitingPrice = (TextView) findViewById(R.id.lblWaitingPrice);

        LayerDrawable drawable = (LayerDrawable) tripProviderRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lnrInvoice.setVisibility(View.GONE);
            }
        });

        btnViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lnrInvoice.setVisibility(View.VISIBLE);
            }
        });

        lnrInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lnrInvoice.setVisibility(View.GONE);
            }
        });

        lnrInvoiceSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getString(R.string.cencel_request))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                cancelRequest();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        iv_shareInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot(ScreenshotType.CUSTOM);
            }
        });
    }


    private void setDetails(JSONArray response) {
        if (response != null && response.length() > 0) {
            Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
            if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                Log.e("History Details", "onResponse: Currency" + SharedHelper.getKey(context, "currency"));
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("total"));
            } else {
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
            }
            String form;
            if (tag.equalsIgnoreCase("past_deliveries")) {
                form = response.optJSONObject(0).optString("assigned_at");
            } else {
                form = response.optJSONObject(0).optString("schedule_at");
            }
            try {
                tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                JSONArray userdrop = response.optJSONObject(0).getJSONArray("userdrop");
                if (userdrop != null) {
                    tripArrayList.clear();
                    for (int i = 0; i < userdrop.length(); i++) {
                        Flows flows = new Flows();
                        flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                        flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                        tripArrayList.add(flows);
                    }
                    tripAdapter.notifyDataSetChanged();


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tripId.setText(response.optJSONObject(0).optString("booking_id"));
            paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
            if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money1);
            } else {
                paymentTypeImg.setImageResource(R.drawable.visa_icon);
            }
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                    Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                else
                    Picasso.with(activity).load(URLHelper.base + "storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
            }
            final JSONArray res = response;
            tripProviderImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (res.optJSONObject(0).optJSONObject("user") != null) {
                        JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");
                    }
                    User user = new User();
                    user.setFirstName(jsonObject.optString("first_name"));
                    user.setLastName(jsonObject.optString("last_name"));
                    user.setEmail(jsonObject.optString("email"));
                    if (jsonObject.optString("picture").startsWith("http"))
                        user.setImg(jsonObject.optString("picture"));
                    else
                        user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                    user.setRating(jsonObject.optString("rating"));
                    user.setMobile(jsonObject.optString("mobile"));
                    Intent intent = new Intent(context, ShowProfile.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            });
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                if (response.optJSONObject(0).optJSONObject("user").optString("rating") != null &&
                        !response.optJSONObject(0).optJSONObject("user").optString("rating").equalsIgnoreCase(""))
                    tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("user").optString("rating")));
                else {
                    tripProviderRating.setRating(0);
                }
            }
            /*if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") &&
                    !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("")) {
                tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
            } else {
                tripComments.setText(getString(R.string.no_comments));
            }*/
            if (response.optJSONObject(0).optJSONObject("user") != null) {
                tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
                if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                    viewLayout.setVisibility(View.GONE);
                } else {
                    tripSource.setText(response.optJSONObject(0).optString("s_address"));
                }
            }
            parentLayout.setVisibility(View.VISIBLE);
        }
    }

    public void getRequestDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        Log.e("URL: ", URLHelper.GET_HISTORY_DETAILS_API);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.GET_HISTORY_DETAILS_API + "?request_id=" + jsonObject.optString("id"), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {


                utils.print("Get Trip details", response.toString());
                if (response != null && response.length() > 0) {
                    Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
                    if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                        Log.e("History Details", "onResponse: Currency" + " " + SharedHelper.getKey(context, "currency"));
                        tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("payable"));
                    } else {
                        tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
                    }

                    Float Estimatedfare = Float.valueOf(response.optJSONObject(0).optJSONObject("payment").optString("fixed")) + Float.valueOf(response.optJSONObject(0).optJSONObject("payment").optString("distance"));

                    lblBasePrice.setText(SharedHelper.getKey(context, "currency") + "" + Estimatedfare);
                    lblDistancePrice.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("distance"));
                    lblWaitingPrice.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("waiting_charge"));
                    lblTaxPrice.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optInt("tax"));
                    lblBookingID.setText("" + response.optJSONObject(0).optString("booking_id"));
                    if (response.optJSONObject(0).optInt("distance") != 0) {
                        lblDistanceCovered.setText(response.optJSONObject(0).optInt("distance") + " mi");
                    }
                    if (response.optJSONObject(0).optString("travel_time") != null &&
                            !response.optJSONObject(0).optString("travel_time").isEmpty()) {
                        lblTimeTaken.setText(response.optJSONObject(0).optString("travel_time") + " mins");
                    } else {
                        lblTimeTaken.setText("" + "0" + " mins");
                    }
                    if (response.optJSONObject(0).optJSONObject("payment") != null &&
                            response.optJSONObject(0).optJSONObject("payment").optInt("discount") != 0) {
                        discount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("discount"));
                    } else {
                        discountLayout.setVisibility(View.GONE);
                    }

                    try {
                        JSONArray userdrop = response.optJSONObject(0).getJSONArray("userdrop");
                        if (userdrop != null) {
                            tripArrayList.clear();
                            for (int i = 0; i < userdrop.length(); i++) {
                                Flows flows = new Flows();
                                flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                                flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                                tripArrayList.add(flows);
                            }
                            tripAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    txt04Total.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("total"));
                    txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("payable"));
                    String form;

                    if (tag.equalsIgnoreCase("past_deliveries")) {
                        form = response.optJSONObject(0).optString("assigned_at");
                    } else {
                        form = response.optJSONObject(0).optString("schedule_at");
                    }

                    try {
                        tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
                    if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                        paymentTypeImg.setImageResource(R.drawable.money1);
                    } else {
                        paymentTypeImg.setImageResource(R.drawable.visa_icon);
                    }

                    if (response.optJSONObject(0).optJSONObject("user") != null) {
                        if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                            Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                        else
                            Picasso.with(activity).load(URLHelper.base + "storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                    }

                    final JSONArray res = response;
                    tripProviderImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");

                            User user = new User();
                            user.setFirstName(jsonObject.optString("first_name"));
                            user.setLastName(jsonObject.optString("last_name"));
                            user.setEmail(jsonObject.optString("email"));

                            if (jsonObject.optString("picture").startsWith("http"))
                                user.setImg(jsonObject.optString("picture"));
                            else
                                user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                            user.setRating(jsonObject.optString("rating"));
                            user.setMobile(jsonObject.optString("mobile"));
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        }
                    });

                    tripId.setText(" " + response.optJSONObject(0).optString("booking_id"));
                    if (response.optJSONObject(0).optJSONObject("rating") != null) {
                        if (response.optJSONObject(0).optJSONObject("rating").optString("user_rating") != null &&
                                !response.optJSONObject(0).optJSONObject("rating").optString("user_rating").equalsIgnoreCase("")) {
                            tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("rating").optString("user_rating")));
                        } else {
                            tripProviderRating.setRating(0);
                        }
                    }

                    if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") && !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("") && !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("null")) {
                        tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
                    } else {
                        tripComments.setText(getString(R.string.no_comments));
                    }

                    if (response.optJSONObject(0).optJSONObject("user") != null) {
                        tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
                        if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                            viewLayout.setVisibility(View.GONE);
                        } else {
                            tripSource.setText(response.optJSONObject(0).optString("s_address"));
                        }
                    }
                    parentLayout.setVisibility(View.VISIBLE);
                }
                customDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                                e.printStackTrace();
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
                        e.printStackTrace();
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getRequestDetails();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                utils.print("Token", "" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        Dot2dotzApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    public void cancelRequest() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", jsonObject.optString("id"));
            utils.print("", "request_id" + jsonObject.optString("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("URL: ", URLHelper.CANCEL_REQUEST_API);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CANCEL_REQUEST_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                utils.print("CancelRequestResponse", response.toString());
                customDialog.dismiss();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                                e.printStackTrace();
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
                        e.printStackTrace();
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        cancelRequest();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                utils.print("", "Access_Token" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }


    public void displayMessage(String toastString) {
        Snackbar.make(findViewById(R.id.parentLayout), toastString, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, SignIn.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void getUpcomingDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        Log.e("URL: ", URLHelper.UPCOMING_TRIP_DETAILS);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URLHelper.UPCOMING_TRIP_DETAILS + "?request_id=" + jsonObject.optString("id"), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                setDetails(response);
                utils.print("Get Upcoming Details", response.toString());
                customDialog.dismiss();
                parentLayout.setVisibility(View.VISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                                e.printStackTrace();
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
                        e.printStackTrace();
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getUpcomingDetails();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        Dot2dotzApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String monthName = new SimpleDateFormat("MMM").format(cal.getTime());
        return monthName;
    }

    private String getDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String dateName = new SimpleDateFormat("dd").format(cal.getTime());
        return dateName;
    }

    private String getYear(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String yearName = new SimpleDateFormat("yyyy").format(cal.getTime());
        return yearName;
    }

    private String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String timeName = new SimpleDateFormat("hh:mm a").format(cal.getTime());
        return timeName;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*  Method which will take screenshot on Basis of Screenshot Type ENUM  */
    private void takeScreenshot(ScreenshotType screenshotType) {
        Bitmap b = null;
        switch (screenshotType) {
            case FULL:
                //If Screenshot type is FULL take full page screenshot i.e our root content.
                b = ScreenshotUtils.getScreenShot(sv_parentInvoice);
                break;
            case CUSTOM:
                //If Screenshot type is CUSTOM

                rel_header.setVisibility(View.GONE);//set the visibility to INVISIBLE of first button
                btnViewInvoice.setVisibility(View.GONE);//set the visibility to VISIBLE of hidden text

                b = ScreenshotUtils.getScreenShot(sv_parentInvoice);

                //After taking screenshot reset the button and view again
                rel_header.setVisibility(View.VISIBLE);//set the visibility to VISIBLE of first button again
                btnViewInvoice.setVisibility(View.VISIBLE);//set the visibility to INVISIBLE of hidden text

                //NOTE:  You need to use visibility INVISIBLE instead of GONE to remove the view from frame else it wont consider the view in frame and you will not get screenshot as you required.
                break;
        }

        //If bitmap is not null
        if (b != null) {
            //showScreenShotImage(b);//show bitmap over imageview

            File saveFile = ScreenshotUtils.getMainDirectoryName(this);//get the path to save screenshot
            File file = ScreenshotUtils.store(b, "screenshot" + screenshotType + ".jpg", saveFile);//save the screenshot to selected path
            shareScreenshot(file);//finally share screenshot
        } else
            //If bitmap is null show toast message
            Toast.makeText(this, R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();
    }

    /*   *//*  Show screenshot Bitmap *//*
    private void showScreenShotImage(Bitmap b) {
        imageView.setImageBitmap(b);
    }*/

    /*  Share Screenshot  */
    private void shareScreenshot(File file) {
        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text));
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

}