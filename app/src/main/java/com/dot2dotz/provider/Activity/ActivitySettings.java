package com.dot2dotz.provider.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dot2dotz.provider.Dot2dotzApplication;
import com.dot2dotz.provider.R;

import com.dot2dotz.provider.Helper.CustomDialog;
import com.dot2dotz.provider.Helper.LocaleUtils;
import com.dot2dotz.provider.Helper.SharedHelper;
import com.dot2dotz.provider.Retrofit.ApiInterface;
import com.dot2dotz.provider.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class ActivitySettings extends AppCompatActivity {

    private RadioButton radioEnglish, radioHindi,radioArabic,radioUrdu;
    private LinearLayout lnrEnglish, lnrHindi,lnrArabic,lnrUrdu;
    private int UPDATE_HOME_WORK = 1;
    private ApiInterface mApiInterface;
    private CustomDialog customDialog, customDialogNew;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        Utilities.setLanguage(ActivitySettings.this);
        init();
        //
    }

    private void init() {

        radioEnglish = (RadioButton) findViewById(R.id.radioEnglish);
        radioHindi = (RadioButton) findViewById(R.id.radioHindi);
        radioArabic = (RadioButton) findViewById(R.id.radioArabic);
        radioUrdu = (RadioButton) findViewById(R.id.radioUrdu);



        lnrEnglish = (LinearLayout) findViewById(R.id.lnrEnglish);
        lnrHindi = (LinearLayout) findViewById(R.id.lnrHindi);
        lnrUrdu = (LinearLayout) findViewById(R.id.lnrUrdu);



        lnrArabic = (LinearLayout) findViewById(R.id.lnrArabic);

        backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        /*customDialog = new CustomDialog(ActivitySettings.this);
        customDialog.setCancelable(false);
        customDialog.show();*/

        if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("en")) {
            radioEnglish.setChecked(true);
        } else if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("hi")) {
            radioHindi.setChecked(true);
        }
        else if(SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("ar"))
        {
            radioArabic.setChecked(true);
        }
        else if(SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("ur"))
        {
            radioUrdu.setChecked(true);
        }
        else {
            radioEnglish.setChecked(true);
        }

        lnrEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(true);
                radioHindi.setChecked(false);
                radioArabic.setChecked(false);
                radioUrdu.setChecked(false);
            }
        });

        lnrHindi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(false);
                radioArabic.setChecked(false);
                radioHindi.setChecked(true);
                radioUrdu.setChecked(false);
            }
        });

        lnrArabic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(false);
                radioArabic.setChecked(true);
                radioHindi.setChecked(false);
                radioUrdu.setChecked(false);
            }
        });

        lnrUrdu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(false);
                radioUrdu.setChecked(true);
                radioArabic.setChecked(false);
                radioHindi.setChecked(false);
            }
        });




        radioEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    radioHindi.setChecked(false);
                    radioArabic.setChecked(false);
                    radioEnglish.setChecked(true);
                    SharedHelper.putKey(ActivitySettings.this, "language", "en");

                    setLanguage();
//                    recreate();
                    GoToMainActivity();
                }
            }
        });


        radioHindi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                {
                    radioHindi.setChecked(true);
                    radioArabic.setChecked(false);
                    radioEnglish.setChecked(false);
                    radioUrdu.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "hi");

                    setLanguage();
//                    recreate();
                    GoToMainActivity();
                }
            }
        });

        radioArabic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if (isChecked) {
                    radioEnglish.setChecked(false);
                    radioHindi.setChecked(false);
                    radioArabic.setChecked(true);
                    radioUrdu.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "ar");
                    setLanguage();
                    GoToMainActivity();
                }
            }
        });


        radioUrdu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if (isChecked)
                {
                    radioEnglish.setChecked(false);
                    radioHindi.setChecked(false);
                    radioArabic.setChecked(false);
                    radioUrdu.setChecked(true);
                    SharedHelper.putKey(ActivitySettings.this, "language", "ur");
                    setLanguage();
                    GoToMainActivity();
                }
            }
        });
    }

    public void GoToMainActivity() {
        customDialogNew = new CustomDialog(ActivitySettings.this, getResources().getString(R.string.language_update));
        if (customDialogNew != null)
            customDialogNew.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customDialogNew.dismiss();
                Intent mainIntent = new Intent(ActivitySettings.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        }, 3000);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtils.onAttach(base));
    }

    private void setLanguage() {
        String languageCode = SharedHelper.getKey(ActivitySettings.this, "language");
        LocaleUtils.setLocale(this, languageCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    String strLatitude = "", strLongitude = "", strAddress = "", id = "";

}