package com.dot2dotz.provider.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.dot2dotz.provider.Adapter.DocumentAdapter;
import com.dot2dotz.provider.Adapter.RegisterDocAdapter;
import com.dot2dotz.provider.Bean.Document;
import com.dot2dotz.provider.Dot2dotzApplication;
import com.dot2dotz.provider.Helper.AppHelper;
import com.dot2dotz.provider.Helper.ConnectionHelper;
import com.dot2dotz.provider.Helper.CustomDialog;
import com.dot2dotz.provider.Helper.SharedHelper;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Helper.VolleyMultipartRequest;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.Utilities;
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

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.dot2dotz.provider.Dot2dotzApplication.trimMessage;

public class DocumentActivity extends AppCompatActivity implements RegisterDocAdapter.ServiceClickListener {

    public static final String TAGG = "DocumentActivity";
    public static final String TAG = "DocumentActivity";
    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    Utilities utils = new Utilities();
    RecyclerView recyclerView;
    ImageView uploadImg;
    ArrayList<Document> documentArrayList;
    RegisterDocAdapter documentAdapter;
    private File isImageFile = null;
    ConnectionHelper helper;
    Boolean isInternet;
    int position = -1;
    Document updatedDocument;
    public static int deviceHeight;
    public static int deviceWidth;
    ImageView backArrow;
    private File des_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        Utilities.setLanguage(DocumentActivity.this);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        uploadImg = (ImageView) findViewById(R.id.upload_img);
        helper = new ConnectionHelper(DocumentActivity.this);
        isInternet = helper.isConnectingToInternet();
        setupRecyclerView();
        getDocList();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void setupRecyclerView() {
        documentArrayList = new ArrayList<>();
        documentAdapter = new RegisterDocAdapter(documentArrayList, DocumentActivity.this);
        documentAdapter.setHasStableIds(true);
        documentAdapter.setServiceClickListener(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(DocumentActivity.this, 2);
        DocumentActivity.ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(DocumentActivity.this, R.dimen._5sdp);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(documentAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                        uploadImg.setImageBitmap(reRotateImg);
                        updateDocumentImage();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(DocumentActivity.this)
                    .load(isImageFile)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(uploadImg);
        }
    }

    private void getDocList() {
        if (helper.isConnectingToInternet()) {
            final CustomDialog customDialog = new CustomDialog(DocumentActivity.this);
            customDialog.setCancelable(false);
            customDialog.show();
            int provider_id = Integer.parseInt(SharedHelper.getKey(this, "id"));
//            String url = URLHelper.base + "api/provider/documents?provider_id=" + provider_id ;
            String url = URLHelper.base + "api/provider/documents";
            Log.e("URL: ", url);
            final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, new JSONArray(), new com.android.volley.Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    customDialog.dismiss();
//                    JSONArray response = result.optJSONArray("document");
                    Log.e(TAG, "onResponse: " + response.toString());
                    if (response.length() > 0) {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject doc = response.optJSONObject(i);
                            Document document = new Document();
                            document.setImg(doc.optString("url"));
                            JSONObject docObj = doc.optJSONObject("document");

                            try {
                                if (docObj != null) {
                                    document.setId(docObj.optString("id"));
                                    document.setName(docObj.optString("name"));
                                    document.setType(docObj.optString("type"));

                                    if (docObj.optString("expires_at") != null) {
                                        document.setExpdate(docObj.optString("expires_at"));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            documentArrayList.add(document);
                        }
                        if (documentArrayList.size() > 0) {
                            documentAdapter = new RegisterDocAdapter(documentArrayList, DocumentActivity.this);
                            documentAdapter.setServiceClickListener(DocumentActivity.this);
                            recyclerView.setAdapter(documentAdapter);
                        }
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
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
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(DocumentActivity.this, "access_token"));
                    return headers;
                }
            };
            Dot2dotzApplication.getInstance().addToRequestQueue(jsonArrayRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

    @Override
    public void onDocImgClick(Document document, int pos) {
        updatedDocument = document;
        this.position = pos;
        galleryIntent();
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

    public void displayMessage(String toastString) {
//        utils.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(DocumentActivity.this, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private void galleryIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                EasyImage.openChooserWithDocuments(DocumentActivity.this, "Select", 0);
            else
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else EasyImage.openChooserWithDocuments(DocumentActivity.this, "Select", 0);
    }

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

    private void cropImage(Uri mImageCaptureUri, File imageFile) {
        des_file = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setToolbarTitle(getString(R.string.edit_document_photo));

        UCrop.of(mImageCaptureUri, Uri.fromFile(des_file))
                .withAspectRatio(1, 1)
                .withMaxResultSize(384, 384)
                .withOptions(options)
                .start(this);
    }

    private void updateDocumentImage() {
        if (helper.isConnectingToInternet()) {

            final CustomDialog customDialog = new CustomDialog(DocumentActivity.this);
            customDialog.setCancelable(false);
            customDialog.show();

            Log.e("URL: ", URLHelper.GET_DOC_PAGE);
            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URLHelper.GET_DOC_PAGE, new com.android.volley.Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    customDialog.dismiss();
                    String res = new String(response.data);
                    utils.print("ProfileUpdateRes", "" + res);
                    Log.e(TAG, "onResponse: " + res);
                    documentArrayList.clear();
                    documentArrayList = new ArrayList<>();
                    getDocList();
                }
            }, new com.android.volley.Response.ErrorListener() {
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
                            if (response.getClass().equals(TimeoutError.class)) {
                                updateDocumentImage();
                                return;
                            }
                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                try {
                                    displayMessage(errorObj.optString("message"));
                                } catch (Exception e) {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                            } else if (response.statusCode == 401) {
//                                GoToBeginActivity();
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
                            updateDocumentImage();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("provider_id", SharedHelper.getKey(DocumentActivity.this, "id"));
                    params.put("document_id", updatedDocument.getId());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + SharedHelper.getKey(DocumentActivity.this, "access_token"));
                    Log.e("Authorization", "Bearer " + SharedHelper.getKey(DocumentActivity.this, "access_token"));
                    return headers;
                }

                @Override
                protected Map<String, VolleyMultipartRequest.DataPart> getByteData() throws AuthFailureError {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                    String photo = "document[" + updatedDocument.getId() + "]";
                    params.put("document", new VolleyMultipartRequest.DataPart("doc.jpg", AppHelper.getFileDataFromDrawable(uploadImg.getDrawable()), "image/jpeg"));
                    return params;
                }
            };
            Dot2dotzApplication.getInstance().addToRequestQueue(volleyMultipartRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
    }

}
