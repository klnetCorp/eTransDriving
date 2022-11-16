package kr.co.klnet.aos.etransdriving;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.klnet.aos.etransdriving.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.klnet.aos.etransdriving.util.DataSet;

public class CameraFrameActivity extends AppCompatActivity implements PhotoFragment.OnFragmentInteractionListener {
    final static String TAG = "CameraFrameActivity";

    final static int REQUEST_PERMISSION_ALL = 1;
    final static int CAMERA_TYPE_CUSTOM = 1;
    final static int CAMERA_TYPE_OEM = 2;

    private final static int CAPTURE_FROM_CAMERA_REQUEST = 1001;


    //private int cameraType_ = CAMERA_TYPE_CUSTOM; //CAMERA_TYPE_OEM //asmyoung old
    private int cameraType_ = CAMERA_TYPE_OEM;   //amsyoung
    static Context context;
    private Bitmap originalBitmap_ = null;
    private Bitmap forOcrBitmap_ = null;
    private Uri originalUri_ = null;
    private Uri forOcrUri_ = null;
    String currentPhotoPath_ = "";

    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_frame_activity);

        String photoType = getIntent().getStringExtra("photoType");
        if("2".equals(photoType)) {
            cameraType_ = CAMERA_TYPE_CUSTOM;
        } else {
            cameraType_ = CAMERA_TYPE_OEM;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                    requestPermissions(PERMISSIONS,
                            REQUEST_PERMISSION_ALL);
            } else {
                goTakePhoto();
            }
        } else {
            goTakePhoto();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_FROM_CAMERA_REQUEST:
                    capturedOemCamera(data);
                    break;
                default:
                    break;
            }
        } else {
            goMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ALL:  {
                if (isPermissionGranted(requestCode, REQUEST_PERMISSION_ALL, grantResults)) {
                    Log.e(TAG, "CommUtil.REQUEST_PERMISSION_ALL - Permission Granted");
                    goTakePhoto();

                } else {
                    Log.e(TAG, "CommUtil.REQUEST_PERMISSION_ALL - Permission Denied");
                    permissionNotGranted();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int duration = 2000;
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            goMain();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public static boolean isPermissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {

        if(requestCode != permissionCode || grantResults.length <= 0) return false;

        for(int result : grantResults) {
            if(result!=PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    void permissionNotGranted() {

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Bitmap img) {
        if (img != null) {
            ImageFragment imageFragment = new ImageFragment();
            imageFragment.imageSetupFragment(img);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.res_photo_layout, imageFragment)
                    //.addToBackStack(null)
                    .commit();
        }
    }


    public void goTakePhoto() {

        if(cameraType_==CAMERA_TYPE_CUSTOM) {
            //start photo fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.res_photo_layout, new PhotoFragment())
                    //.addToBackStack(null)
                    .commit();
        } else {
            captureOemCamera();
        }
    }


    public Bitmap getOriginalBitmap() {
        return originalBitmap_;
    }

    public void setOriginalBitmap(Bitmap bm) {
        originalBitmap_ = bm;
    }

    public Bitmap getOcrBitmap() {
        return forOcrBitmap_;
    }

    public void setOcrBitmap(Bitmap bm) {
        forOcrBitmap_ = bm;
    }

    public void setOcrUri(Uri uri) {
        forOcrUri_ = uri;

    }

    public Uri getOcrUri() {
        return forOcrUri_;
    }

    public void setOriginalUri(Uri uri) {
        originalUri_ = uri;
    }

    public Uri getOriginalUri() {
        return originalUri_;
    }

    public void goMain() {
//        setResult(Activity.RESULT_OK, intent);
        setResult(Activity.RESULT_CANCELED);
        killActivity();
    }

    public void finishTakePhoto(String text) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        intent.putExtra("text", text);
        intent.putExtra("originalImage", getOriginalUri());
        intent.putExtra("ocrImage", getOcrUri());

        setResult(Activity.RESULT_OK, intent);
        killActivity();
    }

    private void killActivity() {
//        ((Activity)context).finishAffinity();
        ((Activity)context).finish();
    }

    private void captureOemCamera() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile("container.jpg");
                } catch (IOException ex) {

                }
                if (photoFile != null) {
                    originalUri_ = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, originalUri_);
                    startActivityForResult(intent, CAPTURE_FROM_CAMERA_REQUEST);
                }
            }
        }
    }

    public File createImageFile(String filename) throws IOException {
        File path = new File(Environment.getExternalStorageDirectory() + "/klnet/");

        String timeStamp = new SimpleDateFormat("MMdd_HHmmssSSS").format(new Date());
        //String imageFileName =  "klnet_" + timeStamp +  "_"  + filename;
        String imageFileName =  "klnet_" + "ocr" +  "_"  + filename;
//        File file = new File(path, imageFileName);
//
//        try {
//            // Make sure the Pictures directory exists.
//            if (path.mkdirs()) {
////                EtransDrivingApp.getInstance().showToast("Not exist :" + path.getName());
//            }
//
//            Log.d("ExternalStorage", "Writed " + path + file.getName());
//
//
//        } catch (Exception e) {
//            // Unable to create file, likely because external storage is
//            // not currently mounted.
//            Log.w("ExternalStorage", "Error writing " + file, e);
//        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        DataSet.getInstance().currentPhotoPath_ = file.getAbsolutePath();

        return file;

    }

    public void capturedOemCamera(Intent data) {
        try {
            //Uri originalUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT); //intent

            InputStream in = getContentResolver().openInputStream(originalUri_);
            Bitmap img = BitmapFactory.decodeStream(in);
            Bitmap resizedBitmap = PhotoFragment.resizeBitmap(img, 1080, 1920);


            String filePath = DataSet.getInstance().currentPhotoPath_;
            ExifInterface exif = null;
            try {
                filePath = getRealPathFromURI(originalUri_);
                exif = new ExifInterface(filePath);
            } catch (Exception e) {
                exif = new ExifInterface(filePath);
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);


            Bitmap rotateBitmap = rotateBitmap(resizedBitmap, orientation);
            onFragmentInteraction(rotateBitmap);


        }catch(Exception e) {e.printStackTrace();

        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
