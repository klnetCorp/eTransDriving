package kr.co.klnet.aos.etransdriving;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import kr.co.klnet.aos.etransdriving.R;
import kr.co.klnet.aos.etransdriving.adaptor.CloudListItemAdaptor;
import kr.co.klnet.aos.etransdriving.model.CloudTextListItem;
import kr.co.klnet.aos.etransdriving.util.PackageManagerUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFragment extends Fragment {
    final static String TAG = "ImageFragment";

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBTCq4IiYhDfUSP4nh6sdg5vjZuv_lQqzE";// tahk97@gmail.com eTransDriving

    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
//    public static final String FILE_NAME = "temp.jpg";
    private static final int MAX_TEXT_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;
//
    public static final int REQUEST_DONE = 1000;
    public static final int DETECT_FAILED = 1001;
    public static final int RETRY_TAKE_PHOTO = 1002;

    private Bitmap bitmap;

    private ArrayList<CloudTextListItem> cloudTextList;

    @BindView(R.id.layout_root)
    FrameLayout layout_root;

    @BindView(R.id.res_photo)
    ImageView resPhoto;


    @BindView(R.id.res_photo_size)
    TextView resPhotoSize;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.cloud_result_list)
    ListView cloud_result_list;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            switch(msg.what) {
                case REQUEST_DONE: {
                    //
                    progressBar.setVisibility(View.GONE);
                }
                break;
                case DETECT_FAILED: {
                    //
//                    Toast.makeText(getActivity(), "번호를 인식하지 못했습니다", Toast.LENGTH_SHORT).show();
                    EtransDrivingApp.getInstance().showToast( "번호를 인식하지 못했습니다");
                }
                break;
                case RETRY_TAKE_PHOTO: {
                    //
                    CameraFrameActivity activity = (CameraFrameActivity)getActivity();
                    activity.goTakePhoto();
                }
                break;
            }
        }

        ;
    };

    public void imageSetupFragment(Bitmap bitmap) {
        if (bitmap != null) {
            this.bitmap = bitmap;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    View view = getActivity().getCurrentFocus();
                    if(view!=null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }
                }
                return true;
            }
        });

        //check if bitmap exist, set to ImageView
        if (bitmap != null) {
            resPhoto.setImageBitmap(bitmap);
            String info = "이미지 가로크기:" + bitmap.getWidth() + "\n" + "이미지 세로크기:" + bitmap.getHeight();
            resPhotoSize.setText(info);
            uploadImage();
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            Uri photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", getCameraFile());
//            uploadImage(photoUri);
//        }
        uploadImage();
    }

    public File getCameraFile() {
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, PhotoFragment.CAMERA_CROPPED_FILENAME);
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                MAX_DIMENSION);

                resPhoto.setImageBitmap(bitmap);

                requestCloudVision(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
//                Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
                EtransDrivingApp.getInstance().showToast( "번호를 인식하지 못했습니다");
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
//            Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
            EtransDrivingApp.getInstance().showToast( "번호를 인식하지 못했습니다");
        }
    }


    public void uploadImage() {
        requestCloudVision(bitmap);
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getActivity().getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getActivity().getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            Log.i(TAG, "OCR image binary size=" +  imageBytes.length + ", encoded size=" + base64EncodedImage.getContent().length());
            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {
                {
//                    Feature labelDetection = new Feature();
//                    labelDetection.setT.setType("LABEL_DETECTION");
//                    labelDetection.setMaxResults(MAX_LABEL_RESULTS);
//                    add(labelDetection);

                    Feature textDetection = new Feature();
                    textDetection.setType("TEXT_DETECTION");
                    textDetection.setMaxResults(MAX_TEXT_RESULTS);
                    add(textDetection);
                }

            });

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private /* static */ class LableDetectionTask extends AsyncTask<Object, Void, ArrayList<CloudTextListItem>> {

        private final WeakReference<CameraFrameActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(CameraFrameActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected ArrayList<CloudTextListItem> doInBackground(Object... params) {
            ArrayList<CloudTextListItem> result = new ArrayList<CloudTextListItem>();

            EntityAnnotation detailEntity = new EntityAnnotation();

            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                detailEntity.setDescription("because " + e.getContent());
                Log.e(TAG, "exception, GoogleJsonResponseException, what=" + e.getMessage());

            } catch (IOException e) {
                detailEntity.setDescription("because of other IOException " +
                        e.getMessage());
            }

            EntityAnnotation errorEntity = new EntityAnnotation();
            errorEntity.setDescription("인식실패");

            CloudTextListItem errorItem = new CloudTextListItem(errorEntity);
            result.add(errorItem);

            CloudTextListItem errorDetail = new CloudTextListItem(detailEntity);
            result.add(errorDetail);
            return result;
        }

        protected void onPostExecute(ArrayList<CloudTextListItem> result) {
            CameraFrameActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                StringBuilder message = new StringBuilder("OCR 처리상태: OCR 응답 완료.\n\n");
                TextView resPhotoSize = activity.findViewById(R.id.res_photo_size);
            /*
                for (CloudTextListItem item : result) {
                    message.append(String.format(Locale.US, "%.3f: %s", item.getCloudEntity().getScore(), item.getCloudEntity().getDescription()));
                    message.append("\n");
                }

             */

                resPhotoSize.setText(message);
                setCloudResult(result);
            }
        }
    }

    private void requestCloudVision(final Bitmap bitmap) {
        // Switch text to loading

        progressBar.setVisibility(View.VISIBLE);

        String info = "이미지 가로크기:" + bitmap.getWidth() + "\n"
                + "이미지 세로크기:" + bitmap.getHeight() + "\n"
                + getString(R.string.loading_message);

        resPhotoSize.setText(info);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, ArrayList<CloudTextListItem>> labelDetectionTask = new ImageFragment.LableDetectionTask((CameraFrameActivity)getActivity(), prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            String err = "OCR 처리를 진행하는 도중에 문제가 발생했습니다.\n잠시 후 다시 시도해주세요." +
                    e.getMessage();
            Log.d(TAG, err);
            resPhotoSize.setText(err);

            progressBar.setVisibility(View.GONE);

        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private /*static*/ ArrayList<CloudTextListItem> convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("OCR 결과:\n\n");
        ArrayList<CloudTextListItem> textList = new ArrayList<>();

        try {
            ArrayList<CloudTextListItem> cloudTextList = new ArrayList<CloudTextListItem>();

            List<AnnotateImageResponse> results = response.getResponses();
            if (results != null) {
                List<EntityAnnotation> labels = results.get(0).getTextAnnotations();
                if (labels != null) {
                    for (EntityAnnotation label : labels) {
                        message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                        message.append("\n");
                        //CloudTextListItem item = new CloudTextListItem(label);
                        CloudTextListItem item = new CloudTextListItem(label.getDescription());
                        cloudTextList.add(item);
                    }
                } else {
                    message.append("인식실패");
                }
            }

            if (cloudTextList.size() <= 0) {
                //데이터가 없으면 인식실패로 처리
                detectDone("");
            } else {
                //데이터가 있으면, owner 그룹, serialGroup  추천번호 찾기
                textList = detectContainerNo(cloudTextList);

                if (textList.size() <= 0) {
                    ////ownerGroup, serialGroup 조합된 데이터 목록이 없으면 인식실패로 처리
                    detectDone("");
                }
            }

            mHandler.sendEmptyMessage(REQUEST_DONE);
            return textList;
        } catch(Exception e) {
            detectDone("");
        }
        return textList;
    }

    private void setCloudResult(ArrayList<CloudTextListItem> result) {
        try {
            cloudTextList = result;

            final CloudListItemAdaptor adapter = new CloudListItemAdaptor(getActivity(), cloudTextList);
            cloud_result_list.setAdapter(adapter);

            cloud_result_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    String text = adapter.getItem(position).getCloudEntity().getDescription();
                    detectDone(text);
//                    Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                    EtransDrivingApp.getInstance().showToast( text);
                }
            });
        }catch(Exception e) {
            Log.e(TAG, "exception occurred, what=" + e.getMessage());
            detectDone("");
        }
    }

    private void detectDone(String text) {
        if(text.length()<=0) {
            mHandler.sendEmptyMessage(DETECT_FAILED);
        }

//        mHandler.sendEmptyMessage(RETRY_TAKE_PHOTO);

        CameraFrameActivity activity = (CameraFrameActivity)getActivity();
        if(activity!=null && !activity.isFinishing()) {
            activity.finishTakePhoto(text);
        }
    }

    private String correctDigitCode(String conNo) {
//        conNo = "CSQU305438";

        //컨테이너 번호 마지막 digit code 보정을 수행
        int[] alphaCode = new int[32];
        int[] charWeight = new int[32];

        String cntrText = conNo;
        int cntrLen = cntrText.length();
        int result = 0;
        int sum = 0;
//        String checkDigit = cntrText.substring(10,11);
        String resultCntrNo = "";

        //사진에서 10자리 또는 11자리 컨테이너 번호가 넘어온다.
        //체크를 하기 위해서라도 최소한 10자리는 들어와야 함.
        //컨테이너 번호가 10자리 미만이면 return false
        if ( cntrLen < 10 ) {
            return conNo;
        }

        //컨테이너번호의 첫 네자리의 영문자 및 영문대문자 체크
        //컨테이너번호 첫 네자리가 아니면 return false
        for (int i = 0 ; i < 4 ; i++) {
            char chr = cntrText.charAt(i);
            if (chr < 'A' || chr > 'Z') {
                //'입력하신 컨테이너번호의 ' + (i+1) + '번째 문자(' + chr + ')가 대문자가 아닙니다.');
                return conNo;
            }
        }

        //컨테이너번호의 다섯번째 자리 이후의 문자가 숫자인지 체크
        //컨테이너번호의 다섯번째 이후 문자가 숫자가 아니면 return false
        for (int i = 4 ; i < cntrLen-1 ; i++) {
            char chr = cntrText.charAt(i);
            if (chr < '0' || chr > '9') {
                //'입력하신 컨테이너번호의 ' + (i+1) + '번째(' + chr + ')가 숫자가 아닙니다.');
                return conNo;
            }
        }

        //알파벳문자별 값셋팅
        int j = 10;
        for (int i = 0 ; i < 26 ; i++ ) {
            if ( j == 11 || j == 22 || j == 33 ) j++;
            alphaCode[i] = j;
            j++;
        }
        //자리별 가중치 값셋팅
        for (int i = 0 ; i < 10 ; i++ ) {
            charWeight[i] = (int)Math.pow(2, i); //2^i
        }

        if (cntrLen == 10) {
            //컨테이너 번호 11자리 값을 가지고 체크디지트를 체크하는데
            //만약 사진에서 넘어온 컨테이너 번호가 10자리일 경우 체크함수를 실행하기 위해 11자리 맞추려고 마지막에 임의이 값을 넣어서 체크한다.
            //어차피 마지막 자리인 체크디지트 값을 생성할꺼기 때문
            cntrText = cntrText + "9";
            cntrLen = cntrText.length();
        }

        for (int i = 0 ; i < cntrLen-1 ; i++ ) {
            if ( i < 4 ) {
                char ch = cntrText.charAt(i);
                int idx = ch - 65;
                int code = alphaCode[idx];
                sum = sum + code * charWeight[i];
            } else {
                char ch = cntrText.charAt(i);

                sum = sum + ch * charWeight[i];
            }
        }
        result = sum % 11;
        if ( result == 10 )
            result = 0;

        //실제값과 올바른 Check Digit과의 값 비교

        //result : 검증한 체크디지트값
        //checkDigit : 화면에서 넘어온 컨번호의 마지막 값

        //if (result != checkDigit) {
        //alert('컨테이너번호의 체크디지트의 값이 틀립니다.(올바른값 :' + result + ')');
        //return false;
        //}

        //resultCntrNo : 마지막 자리가 정상 체크디지트로 보정된 컨테이너번호
        resultCntrNo = cntrText.substring(0, 10) + result;
        Log.i(TAG, "input=[" + cntrText + "], digit=[" + result + "], correction=[" + resultCntrNo + "]");
        return resultCntrNo;



    }

    public String correctDigitCode2(String containerNumber ){

        //10자리 이상만 처리
//        containerNumber = "CSQU3054383";

        if(containerNumber.length()<10) return containerNumber;

        if(containerNumber.length()>=11) containerNumber = containerNumber.substring(0, 10);

        long sum = 0;
        // Get the last number of the container number which is the check digit.
        int checkDigitToVerify = (containerNumber.length()>=11)?Integer.parseInt( "" + containerNumber.charAt(10)):-1;

        Map<Integer, Integer> checkDigitLastValue = new HashMap();
        checkDigitLastValue.put(0, 0);
        checkDigitLastValue.put(1, 1);
        checkDigitLastValue.put(2, 2);
        checkDigitLastValue.put(3, 3);
        checkDigitLastValue.put(4, 4);
        checkDigitLastValue.put(5, 5);
        checkDigitLastValue.put(6, 6);
        checkDigitLastValue.put(7, 7);
        checkDigitLastValue.put(8, 8);
        checkDigitLastValue.put(9, 9);
        checkDigitLastValue.put(10, 0);

        Map<Character, Integer> equivalentNumericalValues = new HashMap();
        equivalentNumericalValues.put('0', 0);
        equivalentNumericalValues.put('1', 1);
        equivalentNumericalValues.put('2', 2);
        equivalentNumericalValues.put('3', 3);
        equivalentNumericalValues.put('4', 4);
        equivalentNumericalValues.put('5', 5);
        equivalentNumericalValues.put('6', 6);
        equivalentNumericalValues.put('7', 7);
        equivalentNumericalValues.put('8', 8);
        equivalentNumericalValues.put('9', 9);
        equivalentNumericalValues.put('A', 10);
        equivalentNumericalValues.put('B', 12);
        equivalentNumericalValues.put('C', 13);
        equivalentNumericalValues.put('D', 14);
        equivalentNumericalValues.put('E', 15);
        equivalentNumericalValues.put('F', 16);
        equivalentNumericalValues.put('G', 17);
        equivalentNumericalValues.put('H', 18);
        equivalentNumericalValues.put('I', 19);
        equivalentNumericalValues.put('J', 20);
        equivalentNumericalValues.put('K', 21);
        equivalentNumericalValues.put('L', 23);
        equivalentNumericalValues.put('M', 24);
        equivalentNumericalValues.put('N', 25);
        equivalentNumericalValues.put('O', 26);
        equivalentNumericalValues.put('P', 27);
        equivalentNumericalValues.put('Q', 28);
        equivalentNumericalValues.put('R', 29);
        equivalentNumericalValues.put('S', 30);
        equivalentNumericalValues.put('T', 31);
        equivalentNumericalValues.put('U', 32);
        equivalentNumericalValues.put('V', 34);
        equivalentNumericalValues.put('W', 35);
        equivalentNumericalValues.put('X', 36);
        equivalentNumericalValues.put('Y', 37);
        equivalentNumericalValues.put('Z', 38);

        // Step 1,2 and 3
        for(int i = 0; i < containerNumber.length(); i++){
            char ch = containerNumber.charAt(i);
            int code = equivalentNumericalValues.get(ch);
            long result = code * (int)Math.pow( 2, i);
            sum += result;
        }

        // Step 4
        int sumInteger = (int)(sum);

        // Step 5
        sumInteger /=  11;

        // Step 6
        sumInteger *=  11;

        // Step 6
        int last = (int) sum - sumInteger;

        int digit = checkDigitLastValue.get(last % 10);
        String resultCntrNo = containerNumber.substring(0, 10) + digit;

        if( checkDigitToVerify == checkDigitLastValue.get(last % 10)){
            //valid ok
        }else{

        }

        Log.i(TAG, "input=[" + containerNumber + "], digit=[" + digit + "], correction=[" + resultCntrNo + "]");
        return resultCntrNo;

    }

    private ArrayList<CloudTextListItem> detectContainerNo(ArrayList<CloudTextListItem> textList) {
        /*
        컨테이너 번호 규칙
        https://regexr.com
        알파벳3자리(owner code) + 'U' + 숫자6자리(serial no) + 숫자1자리(check digit, option)
        regular expression                            '찾는 문자열 형태'
        expr1: /[a-zA-Z]{3}U[0-9]{7}/g                  'xxxU1234567'
        expr2: /[a-zA-Z]{3}U[0-9]{6}[0-9 -]{2}/g        'xxxU123456 7', 'xxxU123456-7'
         */
        ArrayList<CloudTextListItem> conNoList = new ArrayList<>();
        ArrayList<String> ownerGroup = new ArrayList<>();
        ArrayList<String> serialGroup = new ArrayList<>();

        CloudTextListItem item = textList.get(0);

        //첫번째 데이터만 처리한다.
        //for(CloudTextListItem item : textList) {

            String conNo = item.getDescription();
            String ocrText = new String(conNo);

            //클라우드 비전의 첫번째 데이터는 개행문자 포함된 전체결과를 전달함. 분리해서 확인해야 함.
            String[] ocrList = ocrText.split("\n");
            for (String text : ocrList) {

                //문자열에서 개행문자 제거
                text = text.replaceAll(System.getProperty("line.separator"), "");

                //문자열에서 공백문자 제거
                //text = text.replace(" ", "");
                text = text.replaceAll("\\p{Z}", "");

                //문자열 대문자로 변경
                text = text.toUpperCase();

                Log.i(TAG, "text=" + text);
                //'xxxU1234567' 패턴 검사
                if (isMatchedPattern("[a-zA-Z]{3}U[0-9]{7}", text)) {
                    String correctedNo = correctDigitCode2(text);
//                    correctedNo = correctDigitCode(text);
                    ArrayList<CloudTextListItem> list = new ArrayList<>();
                    list.add(new CloudTextListItem(correctedNo));
                    return list;
                }

                //'xxxU123456-7' 패턴 검사 성공 시, 처리중인 것들 삭제하고 현재 값만 전달
                if (isMatchedPattern("[a-zA-Z]{3}U[0-9]{6}[0-9 -]{2}", text)) {
                    String correctedNo = correctDigitCode2(text);
//                    correctedNo = correctDigitCode(text);

                    ArrayList<CloudTextListItem> list = new ArrayList<>();
                    list.add(new CloudTextListItem(correctedNo));
                    return list;
                }

                //문자열이 영문3자리 + "U"이면 owner 그룹에 추가
                if (isMatchedPattern("[a-zA-Z]{3}U", text)) {
                    Log.i(TAG, "add owner group=" + text);

                    ownerGroup.add(text);
                } else if (isMatchedPattern("[0-9]{6,8}", text)) {
                    //문자열이 숫자6~7자리이면 serial 그룹에 추가
                    Log.i(TAG, "add serial group=" + text);
                    serialGroup.add(text);
                } else {
                    //조건에 걸리지 않으면 현재 데이터 버리고 다음 데이터 처리
                    continue;
                }
            }
//        }

        //ownerGroup 못찾았을 경우 workaround
        if(ownerGroup.size()==0) {
            for (String text : ocrList) {

                //문자열에서 개행문자 제거
                text = text.replaceAll(System.getProperty("line.separator"), "");

                //문자열에서 공백문자 제거
                //text = text.replace(" ", "");
                text = text.replaceAll("\\p{Z}", "");

                //문자열 대문자로 변경
                text = text.toUpperCase();

                Log.i(TAG, "text=" + text);
                //문자열이 영문4자리 이고 숫자 6~8자리이면
                if (isMatchedPattern("[a-zA-Z]{4}[0-9]{6,8}", text)) {
                    StringBuilder sb = new StringBuilder(text);
                    sb.setCharAt(3, 'U');
                    text = sb.toString();

                    String correctedNo = correctDigitCode2(text);
                    ArrayList<CloudTextListItem> list = new ArrayList<>();
                    list.add(new CloudTextListItem(correctedNo));
                    return list;
                } if (isMatchedPattern("[a-zA-Z]{4}", text)) {
                    //문자열이 영문4자리 인것 owner그룹에 추갸
                    Log.i(TAG, "add owner group=" + text);
                    ownerGroup.add(text);

                } else {
                    //조건에 걸리지 않으면 현재 데이터 버리고 다음 데이터 처리
                    continue;
                }
            }
        }

        //ownerGroup 못찾았을 경우 workaround
        if(serialGroup.size()==0) {
            for (String text : ocrList) {

                //문자열에서 개행문자 제거
                text = text.replaceAll(System.getProperty("line.separator"), "");

                //문자열에서 공백문자 제거
                //text = text.replace(" ", "");
                text = text.replaceAll("\\p{Z}", "");

                //문자열 대문자로 변경
                text = text.toUpperCase();

                Log.i(TAG, "text=" + text);
                //문자열이 영문4자리 이면 owner 그룹에 추가
                if (isMatchedPattern("[0-9]{6,8}", text)) {
                    //문자열이 숫자6~7자리이면 serial 그룹에 추가
                    Log.i(TAG, "add serial group=" + text);
                    serialGroup.add(text);
                } else {
                    //조건에 걸리지 않으면 현재 데이터 버리고 다음 데이터 처리
                    continue;
                }
            }
        }

        //알파벳 그룹 + 숫자 그룹 조합하여 목록으로 전달
        for (String owner : ownerGroup) {
            for (String serial : serialGroup) {
                conNoList.add(new CloudTextListItem(owner + serial));
            }
        }

        ArrayList<CloudTextListItem> recommendList = new ArrayList<>();

        for(CloudTextListItem itm : conNoList) {
            String desc = itm.getDescription();
            if(desc.length()>=10) {
                //10~11자리이연 digit 보정
                String corrected = correctDigitCode2(desc);
                recommendList.add(new CloudTextListItem(corrected));

            } else  {
                recommendList.add(new CloudTextListItem(desc));
            }
        }

        return recommendList;
    }

    private boolean isMatchedPattern(String pattern, String text) {
        boolean matched = Pattern.matches(pattern, text);
        if(matched) return true;

        return false;
    }
}
