package com.dot2dotz.provider.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.dot2dotz.provider.Helper.AppHelper;
import com.dot2dotz.provider.Helper.URLHelper;
import com.dot2dotz.provider.Model.DocumentResponse;
import com.dot2dotz.provider.R;
import com.dot2dotz.provider.Utilities.Utilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.dot2dotz.provider.Activity.RegisterActivity.ASK_MULTIPLE_PERMISSION_REQUEST_CODE;


public class PreviewActivity extends AppCompatActivity implements View.OnTouchListener {

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    int mode = NONE;
    private File des_file;
    private String url;
    private int previewPosition;
    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    private ImageView previewImage;
    private ImageView imageUpload;
    private ImageView backArrow;
    private PhotoViewAttacher photoViewAttacher;
    private DocumentResponse document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Utilities.setLanguage(PreviewActivity.this);

        previewImage = findViewById(R.id.previewImage);
        imageUpload = findViewById(R.id.imageUpload);
        backArrow = findViewById(R.id.backArrow);
        document = getIntent().getParcelableExtra("documentResponse");
        showPreview(document);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });
    }

    private void galleryIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                EasyImage.openChooserWithDocuments(PreviewActivity.this, "Select", 0);
            else
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else EasyImage.openChooserWithDocuments(PreviewActivity.this, "Select", 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showPreview(DocumentResponse document) {
        if (document != null) {
            if (document.getUrl() != null && !document.getUrl().isEmpty()) {
                if(!document.getUrl().contains("file:///")) {
                    url = URLHelper.base + "storage" + "/" + document.getUrl();
                    Picasso.with(this)
                            .load(url)
                            .placeholder(R.drawable.doc_placeholder)
                            .error(R.drawable.doc_placeholder)
                            .memoryPolicy(MemoryPolicy.NO_STORE)
                            .into(previewImage);
                } else {
                    previewImage.setImageURI(Uri.parse(document.getUrl()));
                }

                // Animation of image view
                final Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom);
                previewImage.startAnimation(animation);
                previewImage.setOnTouchListener(this);

            } else {
                previewImage.setImageResource(R.drawable.doc_placeholder);
            }
            previewPosition = document.getId();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ImageView view1 = (ImageView) view;
        view1.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;
        dumpEvent(motionEvent);
        // Handle touch events here...
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // first finger down only
                savedMatrix.set(matrix);
                start.set(motionEvent.getX(), motionEvent.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(motionEvent);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, motionEvent);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(motionEvent.getX() - start.x, motionEvent.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    // pinch zooming
                    float newDist = spacing(motionEvent);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view1.setImageMatrix(matrix); // display the transformation on screen
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d("Touch Event", sb.toString());
    }

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
            if (croppedURI != null) {
                Intent newIntent = new Intent();
                newIntent.putExtra("uri",croppedURI.toString());
                newIntent.putExtra("position",previewPosition);
                setResult(1,newIntent);
                finish();
            }
        } else {
            Picasso.with(PreviewActivity.this)
                    .load(url)
                    .placeholder(R.drawable.doc_placeholder)
                    .error(R.drawable.doc_placeholder)
                    .into(previewImage);
        }
    }

    private void cropImage(Uri mImageCaptureUri, File imageFile) {
        des_file = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");
        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        options.setToolbarTitle("Edit Profile Photo");

        UCrop.of(mImageCaptureUri, Uri.fromFile(des_file))
                .withAspectRatio(1, 1)
                .withMaxResultSize(384, 384)
                .withOptions(options)
                .start(this);
    }

}
