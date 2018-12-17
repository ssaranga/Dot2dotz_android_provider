package com.dot2dotz.provider.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dot2dotz.provider.Model.FeedBack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.dot2dotz.provider.Activity.SignIn;
import com.dot2dotz.provider.Adapter.FlowAdapter;
import com.dot2dotz.provider.Adapter.LocationsAdapter;
import com.dot2dotz.provider.Bean.Flows;
import com.dot2dotz.provider.Bean.Locations;
import com.dot2dotz.provider.Dot2dotzApplication;
import com.squareup.picasso.Picasso;
import com.dot2dotz.provider.Activity.MainActivity;
import com.dot2dotz.provider.Activity.Offline;
import com.dot2dotz.provider.Activity.ShowProfile;
import com.dot2dotz.provider.Activity.WaitingForApproval;
import com.dot2dotz.provider.Helper.ConnectionHelper;
import com.dot2dotz.provider.Helper.CustomDialog;
import com.dot2dotz.provider.Helper.DataParser;
import com.dot2dotz.provider.Helper.SharedHelper;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Helper.User;
import com.dot2dotz.provider.Retrofit.ApiInterface;
import com.dot2dotz.provider.Retrofit.RetrofitClient;
import com.dot2dotz.provider.Services.CustomFloatingViewService;
import com.dot2dotz.provider.Services.FloatingViewService;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.philio.pinentry.PinEntryView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Url;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.dot2dotz.provider.Dot2dotzApplication.trimMessage;

public class Map extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnCameraMoveListener, FlowAdapter.FlowadapterListener {

    public static final int REQUEST_LOCATION = 1450;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    public static SupportMapFragment mapFragment = null;
    public static String TAG = "Map";
    public Handler ha;
    public String myLat = "", myLng = "";
    String CurrentStatus = " ", PreviousStatus = " ", request_id = " ";
    int method;
    ImageView backArrow_multiple;
    LocationsAdapter locationAdapter;
    Activity activity;
    Context context;
    CountDownTimer countDownTimer;
    int value = 0;
    AlertDialog cancelDialog;
    android.app.AlertDialog cancelReasonDialog;
    Marker currentMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    ParserTask parserTask;
    ImageView imgCurrentLoc, info;
    boolean normalPlay = false;
    String s_address = "", d_address = "";

    //content layout 01
    TextView txt01Pickup, txt01Timer;
    TextView txt01UserName, txtSchedule;
    ImageView img01User;
    RatingBar rat01UserRating;

    //content layer 02
    ImageView img02User;
    TextView txt02UserName, src_address;
    RatingBar rat02UserRating;
    TextView txt02ScheduledTime, txt02From, txt02To, topSrcDestTxtLbl;

    //content layer 03
    ImageView img03User, img04User;
    TextView txt03UserName, txt04UserName;
    RatingBar rat03UserRating, rat04UserRating;
    ImageView img03Call, img03Status1, img03Status2, img03Status3;

    //content layer 04
    TextView txt04InvoiceId, txt04BasePrice, txt04Distance, txt04Tax, txt04Total, txt04AmountToPaid;
    TextView txt04PaymentMode, txt04Commision, lblProviderName;
    ImageView paymentTypeImg;

    //content layer 05
    ImageView img05User;
    RatingBar rat05UserRating;
    EditText edt05Comment;

    //Button layer 01
    Button btn_01_status, btn_confirm_payment, btn_rate_submit;
    Button btn_go_offline;
    LinearLayout lnrGoOffline;

    //Button layer 02
    Button btn_02_accept, btn_02_reject, btn_cancel_ride;

    //map layout
    LinearLayout ll_01_mapLayer;

    //content layout
    LinearLayout ll_01_contentLayer_accept_or_reject_now;
    LinearLayout ll_02_contentLayer_accept_or_reject_later;
    LinearLayout ll_03_contentLayer_service_flow;
    LinearLayout ll_04_contentLayer_payment;
    LinearLayout ll_05_contentLayer_feedback;

    RecyclerView location_recyclerview;
    LinearLayout errorLayout;
    RelativeLayout multiple_flow;
    FlowAdapter flowAdapter;
    RecyclerView flow_recycler;
    ArrayList<Flows> flowArrayList = new ArrayList<>();
    public static boolean isFlowStarted = false;
    String arrived_otp = "";

    //menu icon
    ImageView menuIcon;
    int NAV_DRAWER = 0;
    DrawerLayout drawer;
    Utilities utils = new Utilities();
    MediaPlayer mPlayer;
    ImageView imgNavigationToSource;
    LinearLayout navigate_layout;
    String crt_lat = "", crt_lng = "";
    boolean timerCompleted = false;
    TextView destination;
    ConnectionHelper helper;
    LinearLayout destinationLayer;
    View view;
    boolean doubleBackToExitPressedOnce = false;

    //Animation
    Animation slide_down, slide_up;

    //Distance calculation
    TextView lblDistanceTravelled;
    boolean scheduleTrip = false;
    boolean showBatteryAlert = true;
    private String token;

    //map variable
    private GoogleMap mMap;
    private double srcLatitude = 0;
    private double srcLongitude = 0;
    private double destLatitude = 0;
    private double destLongitude = 0;
    private LatLng sourceLatLng;
    private LatLng destLatLng;
    private LatLng currentLatLng;
    private String bookingId;
    private String address;
    private User user = new User();
    private ImageView sos;
    FeedBack feedBack;
    ArrayList<FeedBack> feedback_array;
    RadioGroup rg;
    int cancelReason;

    //Button layout
    private CustomDialog customDialog;
    private Object previous_request_id = " ";
    private String count;
    private JSONArray statusResponses;
    private String feedBackRating, feedBackComment, flowOtp = "", flowStatus = "";

    ArrayList<Locations> locationArrayList = new ArrayList<>();
    android.app.AlertDialog reasonDialog;
    String currentStatus_new = "";
    String nav_s_address = "", nav_d_address = "";
    private String destinationad;

    public Map() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        }
        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }

        findViewById(view);
        Utilities.setLanguage(getActivity());

        // setupRecyclerView();
        token = SharedHelper.getKey(context, "access_token");
        helper = new ConnectionHelper(context);

        //permission to access location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            setUpMapIfNeeded();
            MapsInitializer.initialize(activity);
        }

        ha = new Handler();

        btn_01_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentStatus.equalsIgnoreCase("ARRIVED")) {
                    showOtpDialog(-1);
                } else {
                    update(CurrentStatus, request_id);
                    multiple_flow.setVisibility(View.VISIBLE);
                }

            }
        });

        btn_confirm_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        btn_rate_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        btn_go_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        errorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imgCurrentLoc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Double crtLat, crtLng;
                if (!crt_lat.equalsIgnoreCase("") && !crt_lng.equalsIgnoreCase("")) {
                    crtLat = Double.parseDouble(crt_lat);
                    crtLng = Double.parseDouble(crt_lng);
                    if (crtLat != null && crtLng != null) {
                        LatLng loc = new LatLng(crtLat, crtLng);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(16).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
        });

        info.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                Intent i = new Intent(context, MultipleFlow.class);
//                startActivity(i );
                multiple_flow.setVisibility(View.VISIBLE);
            }
        });
        btn_02_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;
                }
                handleIncomingRequest("Accept", request_id);
            }
        });

        btn_02_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;
                }
                handleIncomingRequest("Reject", request_id);
            }
        });

        btn_cancel_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (NAV_DRAWER == 0) {
                        drawer.openDrawer(Gravity.START);
                    } else {
                        NAV_DRAWER = 0;
                        drawer.closeDrawers();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        backArrow_multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                multiple_flow.setVisibility(View.GONE);
            }
        });

        img03Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = SharedHelper.getKey(context, "provider_mobile_no");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "provider_mobile_no")));
                        startActivity(intent);
                    }
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }
            }
        });

        navigate_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatus_new.equalsIgnoreCase("SEARCHING")) {
                    //Uri naviUri = Uri.parse("http://maps.google.com/maps?f=d&hl=en&daddr=" + destinationad + "&mode=d");
                    Uri naviUri = Uri.parse("google.navigation:q=" + destinationad + "&mode=d");
                    //Uri gmmIntentUri = Uri.parse("google.navigation:q=new delhi&mode=d");
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                } else {
                    //Uri naviUri2 = Uri.parse("http://maps.google.com/maps?f=d&hl=en&saddr=" + s_address + "&daddr=" + destinationad + "&mode=d");
                    Uri naviUri2 = Uri.parse("google.navigation:q=" + destinationad + "&mode=d");
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri2);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }


                //Check if the application has draw over other apps permission or not?
                //This permission is by default available for API<23. But for API > 23
                //you have to ask for the permission in runtime.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
//                    //If the draw over permission is not available open the settings screen
//                    //to grant the permission.
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + context.getPackageNamePlease click BACK again to exit()));
//                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//                } else {
//                    initializeView();
//                }


                showCustomFloatingView(context, true);
            }
        });
        statusCheck();
        return view;
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                        utils.print("Location error", "Location error " + connectionResult.getErrorCode());
                    }
                }).build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);

                        } catch (NullPointerException | IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            try {
                                status.startResolutionForResult(activity, REQUEST_LOCATION);
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });
//	        }

    }

    private void findViewById(View view) {
        //Menu Icon
        menuIcon = view.findViewById(R.id.menuIcon);
        backArrow_multiple = view.findViewById(R.id.backArrow_multiple);
        imgCurrentLoc = view.findViewById(R.id.imgCurrentLoc);
        info = view.findViewById(R.id.info);
        drawer = activity.findViewById(R.id.drawer_layout);
        flow_recycler = view.findViewById(R.id.flow_recycler);
        //map layer
        ll_01_mapLayer = view.findViewById(R.id.ll_01_mapLayer);
        multiple_flow = view.findViewById(R.id.multiple_flow);
        //Button layer 01
        btn_01_status = view.findViewById(R.id.btn_01_status);
        btn_rate_submit = view.findViewById(R.id.btn_rate_submit);
        btn_confirm_payment = view.findViewById(R.id.btn_confirm_payment);

        //Button layer 02
        btn_02_accept = view.findViewById(R.id.btn_02_accept);
        btn_02_reject = view.findViewById(R.id.btn_02_reject);
        btn_cancel_ride = view.findViewById(R.id.btn_cancel_ride);
        btn_go_offline = view.findViewById(R.id.btn_go_offline);

//        Button btn_tap_when_arrived, btn_tap_when_pickedup,btn_tap_when_dropped,  btn_tap_when_paid, btn_rate_user
        //content layer
        ll_01_contentLayer_accept_or_reject_now = view.findViewById(R.id.ll_01_contentLayer_accept_or_reject_now);
        ll_02_contentLayer_accept_or_reject_later = view.findViewById(R.id.ll_02_contentLayer_accept_or_reject_later);
        ll_03_contentLayer_service_flow = view.findViewById(R.id.ll_03_contentLayer_service_flow);
        ll_04_contentLayer_payment = view.findViewById(R.id.ll_04_contentLayer_payment);
        ll_05_contentLayer_feedback = view.findViewById(R.id.ll_05_contentLayer_feedback);
        lnrGoOffline = view.findViewById(R.id.lnrGoOffline);
        imgNavigationToSource = view.findViewById(R.id.imgNavigationToSource);
        navigate_layout = view.findViewById(R.id.navigate_layout);
        location_recyclerview = view.findViewById(R.id.list_location);
        //content layout 01
        txt01Pickup = view.findViewById(R.id.txtPickup);
        txt01Timer = view.findViewById(R.id.txt01Timer);
        img01User = view.findViewById(R.id.img01User);
        txt01UserName = view.findViewById(R.id.txt01UserName);
        txtSchedule = view.findViewById(R.id.txtSchedule);
        rat01UserRating = view.findViewById(R.id.rat01UserRating);
        sos = view.findViewById(R.id.sos);
        LayerDrawable drawable = (LayerDrawable) rat01UserRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);

        //content layer 02
        img02User = view.findViewById(R.id.img02User);
        txt02UserName = view.findViewById(R.id.txt02UserName);
        src_address = view.findViewById(R.id.src_address);
        rat02UserRating = view.findViewById(R.id.rat02UserRating);
        LayerDrawable stars02 = (LayerDrawable) rat02UserRating.getProgressDrawable();
        stars02.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        txt02ScheduledTime = view.findViewById(R.id.txt02ScheduledTime);
        lblDistanceTravelled = view.findViewById(R.id.lblDistanceTravelled);
        txt02From = view.findViewById(R.id.txt02From);
        txt02To = view.findViewById(R.id.txt02To);

        //content layer 03
        img03User = view.findViewById(R.id.img03User);
        img04User = view.findViewById(R.id.img04User);
        txt03UserName = view.findViewById(R.id.txt03UserName);
        txt04UserName = view.findViewById(R.id.txt04UserName);
        rat03UserRating = view.findViewById(R.id.rat03UserRating);
        rat04UserRating = view.findViewById(R.id.rat04UserRating);
        LayerDrawable drawable_02 = (LayerDrawable) rat03UserRating.getProgressDrawable();
        drawable_02.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        img03Call = view.findViewById(R.id.img03Call);
        img03Status1 = view.findViewById(R.id.img03Status1);
        img03Status2 = view.findViewById(R.id.img03Status2);
        img03Status3 = view.findViewById(R.id.img03Status3);

        //content layer 04
        txt04InvoiceId = view.findViewById(R.id.invoice_txt);
        txt04BasePrice = view.findViewById(R.id.txt04BasePrice);
        txt04Distance = view.findViewById(R.id.txt04Distance);
        txt04Tax = view.findViewById(R.id.txt04Tax);
        txt04Total = view.findViewById(R.id.txt04Total);
        txt04AmountToPaid = view.findViewById(R.id.txt04AmountToPaid);
        txt04PaymentMode = view.findViewById(R.id.txt04PaymentMode);
        txt04Commision = view.findViewById(R.id.txt04Commision);
        destination = view.findViewById(R.id.destination);
        lblProviderName = view.findViewById(R.id.lblProviderName);
        paymentTypeImg = view.findViewById(R.id.paymentTypeImg);
        errorLayout = view.findViewById(R.id.lnrErrorLayout);
        destinationLayer = view.findViewById(R.id.destinationLayer);

        //content layer 05
        img05User = view.findViewById(R.id.img05User);
        rat05UserRating = view.findViewById(R.id.rat05UserRating);

        LayerDrawable stars05 = (LayerDrawable) rat05UserRating.getProgressDrawable();
        stars05.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        edt05Comment = view.findViewById(R.id.edt05Comment);

        topSrcDestTxtLbl = view.findViewById(R.id.src_dest_txt);

        //Load animation
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (doubleBackToExitPressedOnce) {
                        getActivity().finish();
                        return false;
                    }

                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(getActivity(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 5000);
                    return true;
                }
                return false;
            }
        });

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSosDialog();
            }
        });

        destinationLayer.setOnClickListener(this);
        ll_01_contentLayer_accept_or_reject_now.setOnClickListener(this);
        // ll_03_contentLayer_service_flow.setOnClickListener(this);
        // ll_04_contentLayer_payment.setOnClickListener(this);
        //  ll_05_contentLayer_feedback.setOnClickListener(this);
        lnrGoOffline.setOnClickListener(this);
        errorLayout.setOnClickListener(this);
    }

    private void mapClear() {
        if (parserTask != null) {
            parserTask.cancel(true);
            parserTask = null;
        }

        if (!crt_lat.equalsIgnoreCase("") && !crt_lat.equalsIgnoreCase("")) {
            LatLng myLocation = new LatLng(Double.parseDouble(crt_lat), Double.parseDouble(crt_lng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        if (mMap != null) {
            mMap.clear();
        }
        srcLatitude = 0;
        srcLongitude = 0;
        destLatitude = 0;
        destLongitude = 0;
    }

    public void clearVisibility() {

        try {
            if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.VISIBLE) {
                ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_down);
            } else if (ll_02_contentLayer_accept_or_reject_later.getVisibility() == View.VISIBLE) {
                ll_02_contentLayer_accept_or_reject_later.startAnimation(slide_down);
            } else if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                info.setVisibility(View.GONE);

                ll_03_contentLayer_service_flow.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_05_contentLayer_feedback.getVisibility() == View.VISIBLE) {
                ll_05_contentLayer_feedback.startAnimation(slide_down);
            }

            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
            ll_02_contentLayer_accept_or_reject_later.setVisibility(View.GONE);
            ll_03_contentLayer_service_flow.setVisibility(View.GONE);
            multiple_flow.setVisibility(View.GONE);
            ll_04_contentLayer_payment.setVisibility(View.GONE);
            ll_05_contentLayer_feedback.setVisibility(View.GONE);
            lnrGoOffline.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
//                        //Toast.makeText(SignInActivity.this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
                        setUpMapIfNeeded();
                        MapsInitializer.initialize(activity);

                        if (ContextCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            if (mGoogleApiClient == null) {
                                buildGoogleApiClient();
                            }
                            setUpMapIfNeeded();
                            MapsInitializer.initialize(activity);

                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }
                break;
            case 2:
                try {
                    if (grantResults.length > 0) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            // Permission Granted
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "provider_mobile_no")));
                            startActivity(intent);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission Granted
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + SharedHelper.getKey(context, "sos")));
                        startActivity(intent);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.provider_map));
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {
            setupMap();
        }
    }

    private void setSourceLocationOnMap(LatLng latLng) {
   /*     if (mMap != null){
            mMap.clear();
            if (latLng != null){
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
                MarkerOptions options = new MarkerOptions().position(latLng).anchor(0.5f, 0.5f);
                options.position(latLng).isDraggable();
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }*/
    }

    private void setPickupLocationOnMap()
    {
        try
        {
            if (mMap != null) {
                mMap.clear();
            }
            sourceLatLng = currentLatLng;
//            sourceLatLng = new LatLng(Double.parseDouble(SharedHelper.getKey(context, "current_lat")), Double.parseDouble(SharedHelper.getKey(context, "current_lng")));
            destLatLng = new LatLng(srcLatitude, srcLongitude);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(destLatLng).zoom(16).build();
            MarkerOptions options = new MarkerOptions();
            options.position(destLatLng).isDraggable();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            if (sourceLatLng != null && destLatLng != null)
            {
                String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
                FetchUrl fetchUrl = new FetchUrl();
                fetchUrl.execute(url);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDestinationLocationOnMap() {
        sourceLatLng = currentLatLng;
        if (SharedHelper.getKey(context, "current_lat").length() > 0 && SharedHelper.getKey(context, "current_lng").length() > 0) {
//            sourceLatLng = new LatLng(Double.parseDouble(SharedHelper.getKey(context, "current_lat")), Double.parseDouble(SharedHelper.getKey(context, "current_lng")));
            destLatLng = new LatLng(destLatitude, destLongitude);
            try {
                if (sourceLatLng != null && destLatLng != null) {
                    String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
                    FetchUrl fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressWarnings("MissingPermission")
    private void setupMap() {
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setOnCameraMoveListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.style_json));

            if (!success) {
                Log.e("Map:Style", "Style parsing failed.");
            } else {
                Log.e("Map:Style", "Style Applied.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Map:Style", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        // do other tasks here
        setupMap();


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
//                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            if (currentMarker != null) {
                currentMarker.remove();
            }

            MarkerOptions markerOptions1 = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
            currentMarker = mMap.addMarker(markerOptions1);

            if (value == 0) {
                myLat = String.valueOf(location.getLatitude());
                myLng = String.valueOf(location.getLongitude());

                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(myLocation);
//                    Marker marker = mMap.addMarker(markerOptions);
//                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                    mMap.setPadding(0, 0, 0, 135);
//                    mMap.getUiSettings().setZoomControlsEnabled(true);
//                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                checkStatus();

                //check status every 3 sec
                ha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call function
                        checkStatus();
                        ha.postDelayed(this, 3000);
                    }
                }, 3000);

                value++;

            }

            crt_lat = String.valueOf(location.getLatitude());
            crt_lng = String.valueOf(location.getLongitude());
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            SharedHelper.putKey(context, "current_lat", "" + crt_lat);
            SharedHelper.putKey(context, "current_lng", "" + crt_lng);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCameraMove() {
        utils.print("Current marker", "Zoom Level " + mMap.getCameraPosition().zoom);
        if (currentMarker != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(currentMarker.getPosition())) {
                utils.print("Current marker", "Current Marker is not visible");
                if (imgCurrentLoc.getVisibility() == View.GONE) {
                    imgCurrentLoc.setVisibility(View.VISIBLE);
                }
            } else {
                utils.print("Current marker", "Current Marker is visible");
                if (imgCurrentLoc.getVisibility() == View.VISIBLE) {
                    imgCurrentLoc.setVisibility(View.GONE);
                }
                if (mMap.getCameraPosition().zoom < 16.0f) {
                    if (imgCurrentLoc.getVisibility() == View.GONE) {
                        imgCurrentLoc.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(double source_latitude, double source_longitude, double dest_latitude, double dest_longitude) {

        // Origin of route
        String str_origin = "origin=" + source_latitude + "," + source_longitude;

        // Destination of route
        String str_dest = "destination=" + dest_latitude + "," + dest_longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + context.getResources().getString(R.string.google_map_api);

        return url;
    }

    private void checkStatus() {
        try {
            /* Battery status check */
            if (Utilities.getBatteryLevel(context)) {
                if (showBatteryAlert) {
                    Utilities.notify(context, activity);
                    showBatteryAlert = false;
                }
            }

            if (helper.isConnectingToInternet()) {

                if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                    if (CurrentStatus.equalsIgnoreCase("DROPPED") ||
                            CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                        updateLiveTracking(crt_lat, crt_lng);
                    }
                }

                String url = URLHelper.base + "api/provider/trip?latitude=" + crt_lat + "&longitude=" + crt_lng;

                utils.print("Destination Current Lat", "" + crt_lat);
                utils.print("Destination Current Lng", "" + crt_lng);

                Log.e("URL: ", url);
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("CheckStatus", "" + response.toString());
                        try {
                            if (response.optJSONArray("requests").length() > 0) {
                                arrived_otp = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("arrived_otp");
                                JSONObject jsonObject = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optJSONObject("user");
                                if (jsonObject != null) {
                                    user.setFirstName(jsonObject.optString("first_name"));
                                    user.setLastName(jsonObject.optString("last_name"));
                                    user.setEmail(jsonObject.optString("email"));
                                    if (jsonObject.optString("picture").startsWith("http"))
                                        user.setImg(jsonObject.optString("picture"));
                                    else
                                        user.setImg(URLHelper.base + "storage/" + jsonObject.optString("picture"));
                                    user.setRating(jsonObject.optString("rating"));
                                    user.setMobile(jsonObject.optString("mobile"));
                                    bookingId = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("booking_id");
                                    address = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("s_address");
                                    SharedHelper.putKey(context, "is_track", response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("is_track"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (response.optString("account_status").equals("new") || response.optString("account_status").equals("onboarding") || response.optString("account_status").equals("banned")) {
                            ha.removeMessages(0);
                            Intent intent = new Intent(activity, WaitingForApproval.class);
                            activity.startActivity(intent);
                            activity.finish();
                        } else {

                            if (response.optString("service_status").equals("offline")) {
                                ha.removeMessages(0);
//                    Intent intent = new Intent(activity, Offline.class);
//                    activity.startActivity(intent);
                                goOffline();
                            } else {
                                if (response.optJSONArray("requests") != null && response.optJSONArray("requests").length() > 0) {
                                    JSONObject statusResponse = null;
                                    try {
                                        statusResponses = response.optJSONArray("requests");
                                        statusResponse = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request");
                                        s_address = statusResponse.optString("s_address");
                                        d_address = statusResponse.optString("d_address");
                                        request_id = response.optJSONArray("requests").getJSONObject(0).optString("request_id");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    if ((statusResponse != null) && (request_id != null)) {
                                        if ((!previous_request_id.equals(request_id) || previous_request_id.equals(" ")) && mMap != null) {
                                            previous_request_id = request_id;
                                            srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                            srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                            destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                            destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));
                                            //noinspection deprecation
                                            setSourceLocationOnMap(currentLatLng);
//                                            setPickupLocationOnMap();
//                                            setDestinationLocationOnMap();
                                            sos.setVisibility(View.GONE);
                                        }
                                        utils.print("Cur_and_New_status :", "" + CurrentStatus + "," + statusResponse.optString("status"));

                                        try {
                                            JSONArray userdrop = statusResponse.getJSONArray("userdrop");
                                            if (userdrop != null) {
                                                locationArrayList = new ArrayList<>();
                                                flowArrayList = new ArrayList<>();
                                                for (int i = 0; i < userdrop.length(); i++) {

                                                    Locations location = new Locations();
                                                    Flows flows = new Flows();

                                                    location.setsAddress(userdrop.getJSONObject(0).optString("s_address"));
                                                    location.setdAddress(userdrop.getJSONObject(i).optString("d_address"));
                                                    location.setGoods(userdrop.getJSONObject(i).optString("service_items"));
                                                    location.setUserName(userdrop.getJSONObject(i).optString("user_name"));
                                                    location.setUserMobile(userdrop.getJSONObject(i).optString("user_mobile"));
                                                    locationArrayList.add(location);

                                                    flows.setdeliveryAddress(userdrop.getJSONObject(i).optString("d_address"));
                                                    flows.setcomments(userdrop.getJSONObject(i).optString("service_items"));
                                                    flows.setSource_lat(userdrop.getJSONObject(i).optString("s_latitude"));
                                                    flows.setSource_long(userdrop.getJSONObject(i).optString("s_longitude"));
                                                    flows.setDestination_lat(userdrop.getJSONObject(i).optString("d_latitude"));
                                                    flows.setDestination_long(userdrop.getJSONObject(i).optString("d_longitude"));
                                                    flows.setStatus(userdrop.getJSONObject(i).optString("status"));
                                                    flowStatus = userdrop.getJSONObject(i).optString("status");
                                                    flows.setId(userdrop.getJSONObject(i).optString("id"));
                                                    flows.setUser_request_id(userdrop.getJSONObject(i).optString("user_request_id"));
                                                    flows.setOtp(userdrop.getJSONObject(i).optString("arrived_otp"));
                                                    flowOtp = userdrop.getJSONObject(i).optString("otp");
                                                    flowArrayList.add(flows);

                                                }

                                                // Log.e(TAG, "userdrop: "+ userdrop);

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        if (!PreviousStatus.equals(statusResponse.optString("status"))) {
                                            PreviousStatus = statusResponse.optString("status");
                                            clearVisibility();
                                            utils.print("responseObj(" + request_id + ")", statusResponse.toString());

                                            utils.print("Cur_and_New_status :", "" + CurrentStatus + "," + statusResponse.optString("status"));
                                            if (!statusResponse.optString("status").equals("SEARCHING")) {
                                                timerCompleted = false;
                                                if (mPlayer != null && mPlayer.isPlaying()) {
                                                    mPlayer.stop();
                                                    mPlayer = null;
                                                    countDownTimer.cancel();
                                                }
                                            }
                                            if (statusResponse.optString("status").equals("SEARCHING")) {
                                                scheduleTrip = false;
                                                isFlowStarted = false;

                                                if (!timerCompleted) {
                                                    setValuesTo_ll_01_contentLayer_accept_or_reject_now(statusResponses);
                                                    if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                        ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_up);
                                                    }
                                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.VISIBLE);
                                                }
                                                CurrentStatus = "STARTED";
                                            } else if (statusResponse.optString("status").equals("STARTED")) {
                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                if (ll_03_contentLayer_service_flow.getVisibility() == View.GONE) {
                                                    //ll_03_contentLayer_service_flow.startAnimation(slide_up);
                                                }

//                                                Intent i = new Intent(context, MultipleFlow.class);
//                                                startActivity(i );
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                //multiple_flow.setVisibility(View.VISIBLE);
                                                setupRecyclerView_status();
                                                src_address.setText(s_address);
                                                // info.setVisibility(View.VISIBLE);
                                                btn_01_status.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_arrived));
                                                CurrentStatus = "ARRIVED";
                                                sos.setVisibility(View.GONE);
                                                if (srcLatitude == 0 && srcLongitude == 0 && destLatitude == 0 && destLongitude == 0) {
                                                    mapClear();
                                                    srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                                    srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                                    destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                                    destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));
                                                    //noinspection deprecation
                                                    //
                                                    setSourceLocationOnMap(currentLatLng);
//                                                    setPickupLocationOnMap();
//                                                    setDestinationLocationOnMap();
                                                }
                                                img03Status1.setImageResource(R.drawable.arrived);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                sos.setVisibility(View.GONE);
                                                btn_cancel_ride.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                String address = statusResponse.optString("s_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else
                                                    destination.setText(getAddress(statusResponse.optString("s_latitude"),
                                                            statusResponse.optString("s_longitude")));
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.pick_up));
                                            } else if (statusResponse.optString("status").equals("ARRIVED")) {
//                                                if(flowStatus.equals("DROPPED")) {
//                                                    showOtpDialog();
//                                                }
                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                multiple_flow.setVisibility(View.VISIBLE);
                                                // multiple_flow.setVisibility(View.VISIBLE);
                                                setupRecyclerView_status();
                                                src_address.setText(s_address);
                                                info.setVisibility(View.VISIBLE);
                                                btn_01_status.setVisibility(View.GONE);
                                                // btn_01_status.setText(context.getResources().getString(R.string.tap_when_pickedup));
                                                sos.setVisibility(View.GONE);
                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                CurrentStatus = "PICKEDUP";
                                                setSourceLocationOnMap(currentLatLng);
//                                                setDestinationLocationOnMap();
                                                btn_cancel_ride.setVisibility(View.GONE);
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                String address = statusResponse.optString("d_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else
                                                    destination.setText(getAddress(statusResponse.optString("d_latitude"),
                                                            statusResponse.optString("d_longitude")));
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                            } else if (statusResponse.optString("status").equals("PICKEDUP")) {
                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                setupRecyclerView_status();
                                                src_address.setText(s_address);
                                                info.setVisibility(View.VISIBLE);
                                                btn_01_status.setVisibility(View.GONE);
                                                multiple_flow.setVisibility(View.VISIBLE);
                                                // btn_01_status.setText(context.getResources().getString(R.string.tap_when_dropped));
                                                sos.setVisibility(View.VISIBLE);
                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                img03Status2.setImageResource(R.drawable.pickup_select);
                                                CurrentStatus = "DROPPED";
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                btn_cancel_ride.setVisibility(View.GONE);
                                                String address = statusResponse.optString("d_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else {
                                                    destination.setText(getAddress(statusResponse.optString("d_latitude"), statusResponse.optString("d_longitude")));
                                                }
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                                mapClear();

                                                srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                                srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                                destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                                destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));

                                                setSourceLocationOnMap(currentLatLng);
//                                                setDestinationLocationOnMap();
                                            } else if (statusResponse.optString("status").equals("DROPPED")
                                                    && statusResponse.optString("paid").equals("0")) {
                                                setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                }
                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                img03Status1.setImageResource(R.drawable.arrived);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                //  btn_01_status.setText(context.getResources().getString(R.string.tap_when_paid));
                                                btn_01_status.setVisibility(View.GONE);
                                                sos.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.GONE);
                                                CurrentStatus = "COMPLETED";
                                            } else if (statusResponse.optString("status").equals("DROPPED") &&
                                                    statusResponse.optString("paid").equals("1")) {
                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                }
                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                sos.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.GONE);
                                                CurrentStatus = "RATE";
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            } else if (statusResponse.optString("status").equals("COMPLETED")) {
                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                }
                                                edt05Comment.setText("");
                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                sos.setVisibility(View.GONE);
                                                destinationLayer.setVisibility(View.GONE);
                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                CurrentStatus = "RATE";
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            } else if (statusResponse.optString("status").equals("SCHEDULED")) {
                                                if (mMap != null) {
                                                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                        return;
                                                    }
                                                    mMap.clear();
                                                }
                                                clearVisibility();
                                                CurrentStatus = "SCHEDULED";
                                                if (lnrGoOffline.getVisibility() == View.GONE) {
                                                    lnrGoOffline.startAnimation(slide_up);
                                                }
                                                lnrGoOffline.setVisibility(View.VISIBLE);
                                                utils.print("statusResponse", "null");
                                                destinationLayer.setVisibility(View.GONE);
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            }
                                        }
                                    } else {
                                        if (mMap != null) {
                                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                return;
                                            }
                                            timerCompleted = false;
                                            mMap.clear();
                                            if (mPlayer != null && mPlayer.isPlaying()) {
                                                mPlayer.stop();
                                                mPlayer = null;
                                                countDownTimer.cancel();
                                            }

                                        }
//                                        if (isMyServiceRunning(LocationTracking.class)) {
//                                            activity.stopService(service_intent);
//                                        }
//                                        LocationTracking.distance = 0.0f;

                                        clearVisibility();
                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                        destinationLayer.setVisibility(View.GONE);
                                        CurrentStatus = "ONLINE";
                                        PreviousStatus = "NULL";
                                        utils.print("statusResponse", "null");
                                    }

                                } else {
                                    timerCompleted = false;
                                    if (cancelDialog != null) {
                                        if (cancelDialog.isShowing()) {
                                            cancelDialog.dismiss();
                                        }
                                    }

                                    if (PreviousStatus.equalsIgnoreCase("STARTED")) {
                                        Toast.makeText(context, context.getResources().getString(R.string.user_busy), Toast.LENGTH_SHORT).show();
                                    }

                                    if (PreviousStatus.equalsIgnoreCase("ARRIVED")) {
                                        Toast.makeText(context, context.getResources().getString(R.string.user_busy), Toast.LENGTH_SHORT).show();
                                    }

                                    if (cancelReasonDialog != null) {
                                        if (cancelReasonDialog.isShowing()) {
                                            cancelReasonDialog.dismiss();
                                        }
                                    }
                                    if (!PreviousStatus.equalsIgnoreCase("NULL")) {
                                        utils.print("response", "null");
                                        if (mMap != null) {
                                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                return;
                                            }
                                            mMap.clear();
                                        }
                                        if (mPlayer != null && mPlayer.isPlaying()) {
                                            mPlayer.stop();
                                            mPlayer = null;
                                            countDownTimer.cancel();
                                        }
                                        clearVisibility();
                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                        destinationLayer.setVisibility(View.GONE);
                                        CurrentStatus = "ONLINE";
                                        PreviousStatus = "NULL";
                                        utils.print("statusResponse", "null");
                                    }
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            utils.print("Error", error.toString());
                            //errorHandler(error);
                            timerCompleted = false;
                            mapClear();
                            clearVisibility();
                            CurrentStatus = "ONLINE";
                            PreviousStatus = "NULL";
                            lnrGoOffline.setVisibility(View.VISIBLE);
                            destinationLayer.setVisibility(View.GONE);
                            if (mPlayer != null && mPlayer.isPlaying()) {
                                mPlayer.stop();
                                mPlayer = null;
                                countDownTimer.cancel();
                            }
//                        if (errorLayout.getVisibility() != View.VISIBLE) {
//                            errorLayout.setVisibility(View.VISIBLE);
//                            sos.setVisibility(View.GONE);
//                        }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }) {
                    @Override
                    public java.util.Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "XMLHttpRequest");
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };
                Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            } else {
                displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOtpDialog(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.otp_dialog, null);
        Button submitBtn = view.findViewById(R.id.submit_btn);
        final EditText reason = view.findViewById(R.id.reason_etxt);
        final PinEntryView pinView = view.findViewById(R.id.pinView);

        builder.setView(view);
        final AlertDialog otpDialog = builder.create();
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        otpDialog.setCancelable(true);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (pos == -1) {
                        if (arrived_otp.equalsIgnoreCase(pinView.getText().toString())) {
                            otpDialog.dismiss();
                            update(CurrentStatus, request_id);
                            multiple_flow.setVisibility(View.VISIBLE);

                        } else {
                            // OTP wrong
                            Toast.makeText(context, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (flowArrayList.get(pos).getOtp().equalsIgnoreCase(pinView.getText().toString())) {
                            otpDialog.dismiss();

                            update_new("STARTED", flowArrayList.get(pos).getId(), flowArrayList.get(pos).getUser_request_id(), pos);

                        } else {
                            multiple_flow.setVisibility(View.VISIBLE);
                            // OTP wrong
                            Toast.makeText(context, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        otpDialog.show();
    }

    private void setValuesTo_ll_01_contentLayer_accept_or_reject_now(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!status.getJSONObject(0).optString("time_left_to_respond").equals("")) {
                count = status.getJSONObject(0).getString("time_left_to_respond");
            } else {
                count = "0";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        countDownTimer = new CountDownTimer(Integer.parseInt(count) * 1000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                txt01Timer.setText("" + millisUntilFinished / 1000);
                if (mPlayer == null) {
                    mPlayer = MediaPlayer.create(context, R.raw.alert_tone);
                } else {
                    if (!mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                }
                timerCompleted = false;

            }

            public void onFinish() {

                try {
                    txt01Timer.setText("0");
                    mapClear();
                    clearVisibility();
                    if (mMap != null) {
                        mMap.clear();
                    }
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        mPlayer.stop();
                        mPlayer = null;
                    }

                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                    CurrentStatus = "ONLINE";
                    PreviousStatus = "NULL";
                    lnrGoOffline.setVisibility(View.VISIBLE);
                    destinationLayer.setVisibility(View.GONE);
                    timerCompleted = true;
                    handleIncomingRequest("Reject", request_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        countDownTimer.start();

        try {
            if (!statusResponse.optString("schedule_at").trim().equalsIgnoreCase("") && !statusResponse.optString("schedule_at").equalsIgnoreCase("null")) {
                txtSchedule.setVisibility(View.VISIBLE);
                String strSchedule = "";
                try {
                    strSchedule = getDate(statusResponse.optString("schedule_at")) + "th " + getMonth(statusResponse.optString("schedule_at"))
                            + " " + getYear(statusResponse.optString("schedule_at")) + " at " + getTime(statusResponse.optString("schedule_at"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                txtSchedule.setText("Scheduled at : " + strSchedule);
            } else {
                txtSchedule.setVisibility(View.GONE);
            }

            final JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                if (!user.optString("picture").equals("null")) {
                    //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                    else
                        Picasso.with(context).load(URLHelper.base + "storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                } else {
                    img01User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img01User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });
                txt01UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));


                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    rat01UserRating.setRating(Float.valueOf(user.getString("rating")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        txt01Pickup.setText(address);
        setupRecyclerView();

    }

    private void setValuesTo_ll_03_contentLayer_service_flow(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                if (!user.optString("mobile").equals("null")) {
                    SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString("mobile"));
                } else {
                    SharedHelper.putKey(context, "provider_mobile_no", "");
                }

                if (!user.optString("picture").equals("null")) {
                    //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    else
                        Picasso.with(context).load(URLHelper.base + "storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                } else {
                    img03User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img03User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });

                txt03UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));
                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    rat03UserRating.setRating(Float.valueOf(user.getString("rating")));
                } else {
                    rat03UserRating.setRating(0);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setValuesTo_ll_04_contentLayer_payment(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            txt04InvoiceId.setText(context.getResources().getString(R.string.invoice) + " " + bookingId);
            txt04BasePrice.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("fixed"));
            txt04Distance.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("distance"));
            txt04Tax.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("tax"));
            txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + ""
                    + statusResponse.getJSONObject("payment").optString("payable"));
            Integer totalPaid = statusResponse.getJSONObject("payment").optInt("total") + statusResponse.getJSONObject("payment").optInt("wallet") + statusResponse.getJSONObject("payment").optInt("discount");
            txt04Total.setText(SharedHelper.getKey(context, "currency") + "" + totalPaid);
            txt04PaymentMode.setText(statusResponse.getString("payment_mode"));
            txt04Commision.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("commision"));
            if (statusResponse.getString("payment_mode").equals("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money_icon);
                btn_confirm_payment.setVisibility(View.VISIBLE);
            } else {
                paymentTypeImg.setImageResource(R.drawable.pay_u_money);
                btn_confirm_payment.setVisibility(View.GONE);
            }

            try {
                JSONObject user = statusResponse.getJSONObject("user");
                if (user != null) {
                    if (!user.optString("mobile").equals("null")) {
                        SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString("mobile"));
                    } else {
                        SharedHelper.putKey(context, "provider_mobile_no", "");
                    }

                    if (!user.optString("picture").equals("null")) {
                        //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                        if (user.optString("picture").startsWith("http"))
                            Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                        else
                            Picasso.with(context).load(URLHelper.base + "storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                    } else {
                        img04User.setImageResource(R.drawable.ic_dummy_user);
                    }
                    final User userProfile = this.user;
                    img04User.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("user", userProfile);
                            startActivity(intent);
                        }
                    });

                    txt04UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setValuesTo_ll_05_contentLayer_feedback(JSONArray status) {
        rat05UserRating.setRating(1.0f);
        feedBackRating = "1";
        rat05UserRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                utils.print("rating", rating + "");
                if (rating < 1.0f) {
                    rat05UserRating.setRating(1.0f);
                    feedBackRating = "1";
                }
                feedBackRating = String.valueOf((int) rating);
            }
        });
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
            JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                lblProviderName.setText(context.getResources().getString(R.string.rate_your_trip) +
                        " " + user.optString("first_name") + " " + user.optString("last_name"));
                if (!user.optString("picture").equals("null")) {
//                    new DownloadImageTask(img05User).execute(user.getString("picture"));
                    //Glide.with(activity).load(URLHelper.base+"storage/"+user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                    else
                        Picasso.with(context).load(URLHelper.base + "storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                } else {
                    img05User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img05User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        feedBackComment = edt05Comment.getText().toString();
    }

    private void update(final String status, String id) {
        customDialog = new CustomDialog(activity);
        customDialog.setCancelable(false);
        customDialog.show();
        if (status.equals("ONLINE")) {

            JSONObject param = new JSONObject();
            try {
                param.put("service_status", "offline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("URL: ", URLHelper.UPDATE_AVAILABILITY_API);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.UPDATE_AVAILABILITY_API, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    if (response != null) {
                        if (response.optJSONObject("service").optString("status").equalsIgnoreCase("offline")) {
                            goOffline();
                        } else {
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            String url;
            JSONObject param = new JSONObject();
            if (status.equals("RATE")) {
                url = URLHelper.base + "api/provider/trip/" + id + "/rate";
                try {
                    param.put("rating", feedBackRating);
                    param.put("comment", edt05Comment.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                utils.print("Input", param.toString());
            } else {

                url = URLHelper.base + "api/provider/trip/" + id;
                try {
                    param.put("_method", "PATCH");
                    param.put("status", status);

                    if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                        if (CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                            param.put("address", getAddress(crt_lat, crt_lng));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.e("URL: ", url);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    customDialog.dismiss();
                    if (response.optJSONObject("requests") != null) {
                        utils.print("request", response.optJSONObject("requests").toString());
                    }

                    if (status.equals("RATE")) {
                        clearVisibility();
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                        LatLng myLocation = new LatLng(Double.parseDouble(crt_lat), Double.parseDouble(crt_lng));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mapClear();
                        if (mMap != null) {
                            mMap.clear();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    customDialog.dismiss();
                    utils.print("Error", error.toString());
                    if (status.equals("RATE")) {
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                    }
                    //errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        }
    }

    //Cancel Request with Reason
    public void cancelRequest(String id, Integer cancelReason) {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("cancel_reason", cancelReason);
            Log.e("", "request_id" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("URL: ", URLHelper.CANCEL_REQUEST_API);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.CANCEL_REQUEST_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                customDialog.dismiss();
                utils.print("CancelRequestResponse", response.toString());
                Toast.makeText(context, "" + context.getResources().getString(R.string.request_cancel), Toast.LENGTH_SHORT).show();
                mapClear();
                clearVisibility();
                lnrGoOffline.setVisibility(View.VISIBLE);
                destinationLayer.setVisibility(View.GONE);
                CurrentStatus = "ONLINE";
                PreviousStatus = "NULL";
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                String json = null;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(context.getResources().getString(R.string.something_went_wrong));
                                e.printStackTrace();
                            }
                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(context.getResources().getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(context.getResources().getString(R.string.server_down));
                        } else {
                            displayMessage(context.getResources().getString(R.string.please_try_again));
                        }

                    } catch (Exception e) {
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        e.printStackTrace();
                    }

                } else {
                    displayMessage(context.getResources().getString(R.string.please_try_again));
                }
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                Log.e("", "Access_Token" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void handleIncomingRequest(final String status, String id) {
        if (!((Activity) context).isFinishing()) {
            customDialog = new CustomDialog(activity);
            customDialog.setCancelable(false);
            customDialog.show();
        }
        String url = URLHelper.base + "api/provider/trip/" + id;

        if (status.equals("Accept")) {
            method = Request.Method.POST;
        } else {
            method = Request.Method.DELETE;
        }

        Log.e("URL: ", url);//vikash
        //final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(method, url, null, new Response.Listener<JSONArray>() {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if (status.equals("Accept")) {
                    customDialog.dismiss();
                    Toast.makeText(context, context.getResources().getString(R.string.request_accept), Toast.LENGTH_SHORT).show();
                } else {
                    if (!timerCompleted) {

                        try {
                            txt01Timer.setText("0");
                            mapClear();
                            clearVisibility();
                            if (mMap != null) {
                                mMap.clear();
                            }

                            if (mPlayer != null && mPlayer.isPlaying()) {
                                mPlayer.stop();
                                mPlayer = null;
                            }

                            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                            CurrentStatus = "ONLINE";
                            PreviousStatus = "NULL";
                            lnrGoOffline.setVisibility(View.VISIBLE);
                            destinationLayer.setVisibility(View.GONE);
                            timerCompleted = true;
                            customDialog.dismiss();
                            Toast.makeText(context, "" + context.getResources().getString(R.string.request_reject), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            if (customDialog != null && customDialog.isShowing())
                                customDialog.dismiss();
                            e.printStackTrace();
                        }
                    } else {
//                            Toast.makeText(context, ""+context.getResources().getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(context, "" + context.getResources().getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
                        try {
                            txt01Timer.setText("0");
                            mapClear();
                            clearVisibility();
                            if (mMap != null) {
                                mMap.clear();
                            }

                            if (mPlayer != null && mPlayer.isPlaying()) {
                                mPlayer.stop();
                                mPlayer = null;
                            }

                            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                            CurrentStatus = "ONLINE";
                            PreviousStatus = "NULL";
                            lnrGoOffline.setVisibility(View.VISIBLE);
                            destinationLayer.setVisibility(View.GONE);
                            timerCompleted = true;

                            customDialog.dismiss();
                            //handleIncomingRequest("Reject", request_id);
                        } catch (Exception e) {
                            if (customDialog != null && customDialog.isShowing())
                                customDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                utils.print("Error", error.toString());
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void errorHandler(VolleyError error) {
        utils.print("Error", error.toString());
        String json = null;
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {

            try {
                JSONObject errorObj = new JSONObject(new String(response.data));
                utils.print("ErrorHandler", "" + errorObj.toString());
                if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                    try {
                        displayMessage(errorObj.optString("message"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                    }
                } else if (response.statusCode == 401) {
                    SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
                    GoToBeginActivity();
                } else if (response.statusCode == 422) {
                    json = Dot2dotzApplication.trimMessage(new String(response.data));
                    if (json != "" && json != null) {
                        displayMessage(json);
                    } else {
                        displayMessage(context.getResources().getString(R.string.please_try_again));
                    }

                } else if (response.statusCode == 503) {
                    displayMessage(context.getResources().getString(R.string.server_down));
                } else {
                    displayMessage(context.getResources().getString(R.string.please_try_again));
                }

            } catch (Exception e) {
                e.printStackTrace();
                displayMessage(context.getResources().getString(R.string.something_went_wrong));
            }
        } else {
            displayMessage(context.getResources().getString(R.string.please_try_again));
        }
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", context.getResources().getString(R.string.False));
        Intent mainIntent = new Intent(activity, SignIn.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void goOffline() {
        try {
            FragmentManager manager = MainActivity.fragmentManager;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content, new Offline());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer = null;
        }
        ha.removeCallbacksAndMessages(null);
        super.onDestroy();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, "Request Cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            showCustomFloatingView(getActivity(), false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public String getAddress(String strLatitude, String strLongitude) {
        Geocoder geocoder;
        List<Address> addresses;
        String address = "", city = "";
        try {
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            double latitude = Double.parseDouble(strLatitude);
            double longitude = Double.parseDouble(strLongitude);

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address.length() > 0 || city.length() > 0)
            return address + ", " + city;
        else
            return context.getResources().getString(R.string.no_address);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {

        super.onPause();
        if (customDialog != null) {
            if (customDialog.isShowing()) {
                customDialog.dismiss();
            }
        }
        if (ha != null) {
            ha.removeCallbacksAndMessages(null);
        }
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cancel_confirm));

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                showReasonDialog();
                getFeedback();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        cancelDialog = builder.create();
        cancelDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        cancelDialog.show();
    }

    private void showReasonDialog() {

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.cancel_dialog, null);
        rg = view.findViewById(R.id.reasons);
        final RadioButton[] radioButton = new RadioButton[1];

        for (int i = 0; i < feedback_array.size(); i++) {
            radioButton[0] = new RadioButton(context);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            radioButton[0].setLayoutParams(params);
            radioButton[0].setText(feedback_array.get(i).getReason());
            radioButton[0].setId(i);
            rg.addView(radioButton[0], i);
            rg.setGravity(View.FOCUS_RIGHT);

        }

        final String[] reason = {""};
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                          @Override
                                          public void onCheckedChanged(RadioGroup group, int checkedId) {
                                              radioButton[0] = view.findViewById(checkedId);
                                              reason[0] = radioButton[0].getText().toString();
                                              for (int i = 0; i < feedback_array.size(); i++) {
                                                  if (reason[0].equalsIgnoreCase(feedback_array.get(i).getReason())) {
                                                      cancelReason = feedback_array.get(i).getId();
                                                  }

                                              }
                                          }
                                      }
        );

        Button submitBtn = view.findViewById(R.id.submit_btn);
        builder.setView(view)
                .setCancelable(true);
        reasonDialog = builder.create();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  cancelReason = reason[0];
                if (cancelReason == -1) {
                    displayMessage(getString(R.string.give_your_feedback));
                } else {
                    cancelRequest(request_id, cancelReason);
                }

                reasonDialog.dismiss();
            }
        });

        Window window = reasonDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        reasonDialog.show();
    }

    private void showSosDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.sos_confirm));

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancelRequest(request_id);
                dialog.dismiss();
                String mobile = SharedHelper.getKey(context, "sos");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 3);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + mobile));
                        startActivity(intent);
                    }
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            ha.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //call function
                    checkStatus();
                    ha.postDelayed(this, 3000);
                }
            }, 3000);

            context.stopService(new Intent(context, CustomFloatingViewService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void updateLiveTracking(String latitude, String longitude) {
        ApiInterface mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);

        Call<ResponseBody> call = mApiInterface.getLiveTracking("XMLHttpRequest", "Bearer " + token,
                request_id, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCCESS", "SUCCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCCESS", "bodyString" + bodyString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        context.startService(new Intent(context, FloatingViewService.class));
        activity.finish();
    }

    @SuppressLint("NewApi")
    private void showCustomFloatingView(Context context, boolean isShowOverlayPermission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            final Intent intent = new Intent(context, CustomFloatingViewService.class);
            ContextCompat.startForegroundService(context, intent);
            return;
        }

        if (Settings.canDrawOverlays(context)) {
            final Intent intent = new Intent(context, CustomFloatingViewService.class);
            ContextCompat.startForegroundService(context, intent);
            return;
        }

        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    @Override
    public void onnavigationClick(Flows flows) {
        //    Toast.makeText(context,"clicked",Toast.LENGTH_LONG).show();
        multiple_flow.setVisibility(View.GONE);
        mapClear();
        srcLatitude = Double.valueOf(flows.getSource_lat());
        srcLongitude = Double.valueOf(flows.getSource_long());
        destLatitude = Double.valueOf(flows.getDestination_lat());
        destLongitude = Double.valueOf(flows.getDestination_long());
        setSourceLocationOnMap(currentLatLng);
//        setPickupLocationOnMap();
        setDestinationLocationOnMap();
        topSrcDestTxtLbl.setText(context.getResources().getString(R.string.pick_up));
        destinationad = flows.getdeliveryAddress();
        destination.setText(flows.getdeliveryAddress());
    }

    @Override
    public void onstausClick(Flows flows, int pos) {

        try {
            String status = "";
            String id = "";
            String user_request_id = "";
            currentStatus_new = flows.getStatus();
            nav_s_address = getAddress(crt_lat, crt_lng);
            nav_d_address = flows.getdeliveryAddress();

            if (currentStatus_new.equalsIgnoreCase("COMPLETED")) {
                return;
            }

            if (currentStatus_new.equalsIgnoreCase("STARTED")) {
                showOtpDialog(pos);
            } else {
                id = flows.getId();
                user_request_id = flows.getUser_request_id();
                update_new(currentStatus_new, id, user_request_id, pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void statusUpdate(int position, String status) {

        if (position >= 0) {
            flowArrayList.get(position).setStatus(status);
            refreshAdapter();
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                if (mMap != null) {
                    mMap.clear();
                }

                MarkerOptions markerOptions = new MarkerOptions().title("Source")
                        .position(sourceLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                if (mMap != null) {
                    mMap.addMarker(markerOptions);
                    MarkerOptions markerOptions1 = new MarkerOptions().title("Destination")
                            .position(destLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                    mMap.addMarker(markerOptions);
                    mMap.addMarker(markerOptions1);
                }

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                LatLngBounds bounds;
                builder.include(sourceLatLng);
                builder.include(destLatLng);
                if (CurrentStatus.equalsIgnoreCase("STARTED")) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sourceLatLng).zoom(16).build();
                    MarkerOptions markerOptionsq = new MarkerOptions();
                    markerOptionsq.position(sourceLatLng);
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    bounds = builder.build();
                    int padding = 320; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                }

                mMap.getUiSettings().setMapToolbarEnabled(false);


                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor(context.getResources().getString(0 + R.color.colorAccent)));

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null && points != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    private void setupRecyclerView() {
        locationAdapter = new LocationsAdapter(locationArrayList, context);
        location_recyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        location_recyclerview.setAdapter(locationAdapter);
    }

    private void setupRecyclerView_status() {
        for (int i = 0; i < flowArrayList.size(); i++) {
            Flows flows = flowArrayList.get(i);
            if (!flows.getStatus().equalsIgnoreCase("SEARCHING") && !flows.getStatus().equalsIgnoreCase("COMPLETED")) {
                isFlowStarted = true;
            }

        }
        flowAdapter = new FlowAdapter(flowArrayList, context);
        flow_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        flow_recycler.setAdapter(flowAdapter);
        flowAdapter.setFlowadapterListener(this);
    }

    private void update_new(final String status, String id, String user_request_id, final int position) {
        customDialog = new CustomDialog(activity);
        customDialog.setCancelable(false);
        customDialog.show();
        String url;
        JSONObject param = new JSONObject();
        url = URLHelper.base + "api/provider/request/service";
        try {
            // param.put("_method", "PATCH");
            param.put("status", status);
            param.put("id", id);
            param.put("requestid", user_request_id);
            Log.e(TAG, "update_new: " + param);
            if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                if (CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                    param.put("address", getAddress(crt_lat, crt_lng));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("URL: ", url);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                customDialog.dismiss();
                //Toast.makeText(context,"success",Toast.LENGTH_LONG).show();
                if (response != null) {
                    utils.print("request_multi", response.toString());
                    statusUpdate(position, response.optString("status"));
                    isFlowStarted = !response.optString("status").equalsIgnoreCase("COMPLETED");
                }

                //setupRecyclerView_status();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
                utils.print("Error", error.toString());
            }
        }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + token);
                Log.e(TAG, "Authorization: " + "Bearer " + token);
                return headers;
            }
        };
        Dot2dotzApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void refreshAdapter() {
        flowAdapter.setListModels(flowArrayList);
        flowAdapter.notifyDataSetChanged();
    }

    private void getFeedback() {
        try {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);

            if (customDialog != null) {
                customDialog.show();
            }

            Log.e("URL: ", URLHelper.GET_FEEDBACK);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                    URLHelper.GET_FEEDBACK, new JSONArray(),
                    new com.android.volley.Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                utils.print("GetServices", response.toString());
                                if ((customDialog != null) && (customDialog.isShowing()))
                                    customDialog.dismiss();
                                if (response.length() > 0) {

                                    feedback_array = new ArrayList<>();
                                    if (response.length() > 0) {

                                        for (int i = 0; i < response.length(); i++) {
                                            feedBack = new FeedBack();
                                            feedBack.setReason(response.getJSONObject(i).optString("reason"));
                                            feedBack.setId(response.getJSONObject(i).optInt("id"));
                                            feedback_array.add(feedBack);
                                        }
                                    }
                                    showReasonDialog();

                                    Log.e(TAG, "feedback_array: " + feedback_array.toString());
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if ((customDialog != null) && (customDialog.isShowing()))
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
                                        e.printStackTrace();
                                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                displayMessage(context.getResources().getString(R.string.something_went_wrong));
                            }
                        } else {
                            displayMessage(context.getResources().getString(R.string.please_try_again));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + "Bearer" + " "
                            + SharedHelper.getKey(context, "access_token"));
                    Log.e(TAG, "getHeaders: " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };

            Dot2dotzApplication.getInstance().addToRequestQueue(jsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}