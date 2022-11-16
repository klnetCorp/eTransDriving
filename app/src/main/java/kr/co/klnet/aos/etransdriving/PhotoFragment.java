package kr.co.klnet.aos.etransdriving;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.util.CommonUtil;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.MotionEvent.INVALID_POINTER_ID;
import static java.lang.Math.abs;


public class PhotoFragment extends Fragment implements SurfaceHolder.Callback {

    final static String TAG = "PhotoFragment";
    final static int CAPTURE_PICTURE_WIDTH = 1280;
    final static int CAPTURE_PICTURE_HEIGHT = 960;
    final static String CAMERA_FULL_FILENAME = "full_image.jpg";
    final static String CAMERA_CROPPED_FILENAME = "cropped_image.jpg";

    final static int DEFAULT_BOX_WIDTH_PERCENT = 80;
    final static int DEFAULT_BOX_HEIGHT_PERCENT = 80;


    int frameMinWidth = 320;
    int frameMinHeight = 160;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    Context context;

//    @BindView(R.id.seekBarZoom)
//    SeekBar seekBarZoom;

    @BindView(R.id.preview_layout)
    LinearLayout previewLayout;

    @BindView(R.id.border_camera)
    View borderCamera;
    @BindView(R.id.res_border_size)
    TextView resBorderSizeTV;

    @BindView(R.id.seekBarHorizontal)
    SeekBar seekBarHorizontal;

    @BindView(R.id.seekBarVertical)
    VerticalSeekBar seekBarVertical;

    private OnFragmentInteractionListener mListener;

    Camera.Size previewSizeOptimal;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bitmap bitmap);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getContext();

        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, view);

        surfaceView = (SurfaceView) view.findViewById(R.id.camera_preview_surface);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        frameMinWidth = borderCamera.getWidth();
        frameMinHeight = borderCamera.getHeight();

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                return onTouchHandler(v, event);
            }
        });

        seekBarHorizontal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, seekBar.toString() + "::onProgressChanged, progress=" + progress);
                calculateWidth(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, seekBar.toString() + "::onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, seekBar.toString() + "::onStopTrackingTouch");
            }
        });

        seekBarVertical.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i(TAG, seekBar.toString() + "::onProgressChanged, progress=" + progress);
                calculateHeight(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, seekBar.toString() + "::onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, seekBar.toString() + "::onStopTrackingTouch");
            }
        });

        calculateWidth(DEFAULT_BOX_WIDTH_PERCENT);
        calculateHeight(DEFAULT_BOX_HEIGHT_PERCENT);

        seekBarHorizontal.setProgress(DEFAULT_BOX_WIDTH_PERCENT);
        seekBarVertical.setProgress(DEFAULT_BOX_HEIGHT_PERCENT);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                //get preview sizes
                List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

                //find optimal - it very important
                previewSizeOptimal = getOptimalPreviewSize(previewSizes, parameters.getPictureSize().width,
                        parameters.getPictureSize().height);

                //set parameters
                if (previewSizeOptimal != null) {
                    parameters.setPreviewSize(previewSizeOptimal.width, previewSizeOptimal.height);
                }

                if (camera.getParameters().getFocusMode().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                if (camera.getParameters().getFlashMode().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                }

                int resWidth;
                int resHeight;
                resWidth = camera.getParameters().getPictureSize().width;
                resHeight = camera.getParameters().getPictureSize().height;

                if(resWidth>resHeight)
                    parameters.setPictureSize(resWidth, resHeight);
                else
                    parameters.setPictureSize(resHeight, resWidth);

                camera.setParameters(parameters);

                //rotate screen, because camera sensor usually in landscape mode
                Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                if (display.getRotation() == Surface.ROTATION_0) {
                    camera.setDisplayOrientation(90);
                } else if (display.getRotation() == Surface.ROTATION_270) {
                    camera.setDisplayOrientation(180);
                }

                updatePreviewInfo();

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }


    @OnClick(R.id.make_photo_button)
    void makePhoto() {
        if (camera != null) {
            camera.takePicture(myShutterCallback,
                    myPictureCallback_RAW, myPictureCallback_JPG);

        }
    }

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };


    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmapPicture
                    = BitmapFactory.decodeByteArray(data, 0, data.length);

            Bitmap croppedBitmap = null;

            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (display.getRotation() == Surface.ROTATION_0) {

                //rotate bitmap, because camera sensor usually in landscape mode
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapPicture, 0, 0, bitmapPicture.getWidth(), bitmapPicture.getHeight(), matrix, true);

                //calculate aspect ratio
                float koefX = (float) rotatedBitmap.getWidth() / (float) previewLayout.getWidth();
                float koefY = (float) rotatedBitmap.getHeight() / (float) previewLayout.getHeight();

                //get viewfinder border size and position on the screen
                int x1 = borderCamera.getLeft();
                int y1 = borderCamera.getTop();

                int x2 = borderCamera.getWidth();
                int y2 = borderCamera.getHeight();

                //calculate position and size for cropping
                int cropStartX = Math.round(x1 * koefX);
                int cropStartY = Math.round(y1 * koefY);

                int cropWidthX = Math.round(x2 * koefX);
                int cropHeightY = Math.round(y2 * koefY);

                //check limits and make crop
                if (cropStartX + cropWidthX <= rotatedBitmap.getWidth() && cropStartY + cropHeightY <= rotatedBitmap.getHeight()) {
                    croppedBitmap = Bitmap.createBitmap(rotatedBitmap, cropStartX, cropStartY, cropWidthX, cropHeightY);
                } else {
                    croppedBitmap = null;
                }

                //save file
                rotatedBitmap = resizeBitmap(rotatedBitmap, 1080, 1920);

                CameraFrameActivity activity = (CameraFrameActivity)getActivity();
                activity.setOriginalBitmap(rotatedBitmap);
                Uri originalUri = createImageFile(CAMERA_FULL_FILENAME, rotatedBitmap);
                activity.setOriginalUri(originalUri);


                //save result
                if (croppedBitmap != null) {
                    croppedBitmap = resizeBitmap(croppedBitmap, 1080, 1920);
                    activity.setOcrBitmap(croppedBitmap);
                    Uri ocrUri = createImageFile(CAMERA_CROPPED_FILENAME, croppedBitmap);
                    activity.setOcrUri(ocrUri);
                }

            } else if (display.getRotation() == Surface.ROTATION_270) {
                // for Landscape mode
            }

            //pass to another fragment
            if (mListener != null) {
                if (croppedBitmap != null)
                    mListener.onFragmentInteraction(croppedBitmap);
            }

            if (camera != null) {
                camera.startPreview();
            }
        }
    };

    private double touch_interval_X =.0f;
    private double touch_interval_Y =.0f;
    private int zoom_in_count = 0;
    private int zoom_out_count = 0;
    private int touch_zoom = 0;

    public boolean onTouchHandler(View v, MotionEvent event) {
        switch (event.getAction()  & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: // 싱글 터치
                camera.autoFocus(new Camera.AutoFocusCallback() { // 오토 포커스 설정

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        return;
                    }
                });
                break;
            case MotionEvent.ACTION_MOVE: // 터치 후 이동 시
                if(event.getPointerCount() == 2) { // 터치 손가락 2개일 때
                    double now_interval_X = (double) abs(event.getX(0) - event.getX(1)); // 두 손가락 X좌표 차이 절대값
                    double now_interval_Y = (double) abs(event.getY(0) - event.getY(1)); // 두 손가락 Y좌표 차이 절대값
                    if(touch_interval_X < now_interval_X && touch_interval_Y < now_interval_Y) { // 이전 값과 비교
                        //확대
                        Camera.Parameters parameters = camera.getParameters();

                        zoom_in_count++;

                        if(zoom_in_count > 5) { // 카운트를 세는 이유 : 너무 많은 호출을 줄이기 위해
                            zoom_in_count = 0;
                            touch_zoom += 5;
//                            seekBarZoom.setProgress(touch_zoom/6);

                            if(parameters.getMaxZoom() < touch_zoom)
                                touch_zoom = parameters.getMaxZoom();
                            parameters.setZoom(touch_zoom);
                            camera.setParameters(parameters);
                        }
                    }

                    if(touch_interval_X > now_interval_X && touch_interval_Y > now_interval_Y) {
                        //축소
                        zoom_out_count++;

                        if(zoom_out_count > 5) {
                            Camera.Parameters parameters = camera.getParameters();

                            zoom_out_count = 0;
                            touch_zoom -= 10;

//                            seekBarZoom.setProgress(touch_zoom/6);

                            if(0 > touch_zoom)
                                touch_zoom = 0;

                            parameters.setZoom(touch_zoom);
                            camera.setParameters(parameters);
                        }
                    }

                    touch_interval_X = (double) abs(event.getX(0) - event.getX(1));
                    touch_interval_Y = (double)  abs(event.getY(0) - event.getY(1));
                }

                break;
        }

        return true;
    }

    private void calculateWidth(int xProgress) {

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int newW = size.x/2 - CommonUtil.dpToPx(80) + (size.x/2 * xProgress) / 100;

        Log.i(TAG, "height=" + newW);
        resizeFrame(newW, -1);
    }

    private void calculateHeight(int yProgress) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int newH = size.y/2 - CommonUtil.dpToPx(280) + (size.y/2 * yProgress) / 100;

        Log.i(TAG, "width=" + newH);
        resizeFrame(-1, newH);
    }


    private void resizeFrame(int w, int h) {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)borderCamera.getLayoutParams();

        if(w!=-1) params.width = w;
        if(h!=-1) params.height = h;

        borderCamera.setLayoutParams(params);

        updatePreviewInfo();
    }

    public Uri createImageFile(String filename, final Bitmap bitmap) {
//        File path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);

        String timeStamp = new SimpleDateFormat("MMdd_HHmmssSSS").format(new Date());
        //String imageFileName =  "klnet_" + timeStamp +  "_"  + filename;
        String imageFileName =  "klnet_" + "ocr" +  "_"  + filename;

        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = null;
        try {
            file = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
//        final File file = new File(path, imageFileName);

        try {
            // Make sure the Pictures directory exists.
//            if (path.mkdirs()) {
////                Toast.makeText(context, "Not exist :" + path.getName(), Toast.LENGTH_SHORT).show();
//                Log.d("ExternalStorage", "디렉토리생성 " + path.getName());
//            }

            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, os);
            os.flush();
            os.close();
            //Log.d("ExternalStorage", "Writed " + path + file.getName());



            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
            EtransDrivingApp.getInstance().showToast("이미지 처리중... ");
//            Toast.makeText(context, "이미지 처리중... ", Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }

        return Uri.fromFile(file);

    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public void updatePreviewInfo() {
        //write some info
        int x1 = previewLayout.getWidth();
        int y1 = previewLayout.getHeight();

        int x2 = borderCamera.getWidth();
        int y2 = borderCamera.getHeight();

        String info = "프리뷰 가로크기:" + String.valueOf(x1) + "\n" + "프리뷰 세로크기:" + String.valueOf(y1) + "\n" +
                "선택영역 가로크기:" + String.valueOf(x2) + "\n" + "선택영역 세로크기:" + String.valueOf(y2);
        resBorderSizeTV.setText(info);

    }

    public static Bitmap resizeBitmap(Bitmap photoBm, int maxWidth, int maxHeight) {
        //Convert your photo to a bitmap
//        Bitmap photoBm = (Bitmap) "your Bitmap image";
        //get its orginal dimensions
        int bmOriginalWidth = photoBm.getWidth();
        int bmOriginalHeight = photoBm.getHeight();
        double originalWidthToHeightRatio =  1.0 * bmOriginalWidth / bmOriginalHeight;
        double originalHeightToWidthRatio =  1.0 * bmOriginalHeight / bmOriginalWidth;

        //call the method to get the scaled bitmap
        photoBm = getScaledBitmap(photoBm, bmOriginalWidth, bmOriginalHeight,
                originalWidthToHeightRatio, originalHeightToWidthRatio,
                maxHeight, maxWidth);
        return photoBm;

    }

    private static Bitmap getScaledBitmap(Bitmap bm, int bmOriginalWidth, int bmOriginalHeight, double originalWidthToHeightRatio, double originalHeightToWidthRatio, int maxHeight, int maxWidth) {
        if(bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {
            Log.v(TAG, "RESIZING bitmap FROM w=" + bmOriginalWidth + ", h=" +  bmOriginalHeight);

            if(bmOriginalWidth > bmOriginalHeight) {
                bm = scaleDeminsFromWidth(bm, maxWidth, bmOriginalHeight, originalHeightToWidthRatio);
            } else {
                bm = scaleDeminsFromHeight(bm, maxHeight, bmOriginalHeight, originalWidthToHeightRatio);
            }

            Log.v(TAG, "RESIZED bitmap TO w=" + bm.getWidth() + ", h=" +  bm.getHeight());
        }
        return bm;
    }

    private static Bitmap scaleDeminsFromHeight(Bitmap bm, int maxHeight, int bmOriginalHeight, double originalWidthToHeightRatio) {
        int newHeight = (int) Math.min(maxHeight, bmOriginalHeight * .55);
        int newWidth = (int) (newHeight * originalWidthToHeightRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

    private static Bitmap scaleDeminsFromWidth(Bitmap bm, int maxWidth, int bmOriginalWidth, double originalHeightToWidthRatio) {
        //scale the width
        int newWidth = (int) Math.min(maxWidth, bmOriginalWidth * .75);
        int newHeight = (int) (newWidth * originalHeightToWidthRatio);
        bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return bm;
    }

}






