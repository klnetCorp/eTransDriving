package kr.co.klnet.aos.etransdriving.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by sonseongbin on 2017. 3. 19..
 */

public class TTS extends UtteranceProgressListener implements TextToSpeech.OnInitListener{

    private final String TAG = TTS.class.getSimpleName();

    private final int TTS_DURATION_CARRAGE_RETURN = 600;
    private final int TTS_DURATION_SYMBOL_FORCED_IGNORE = 500;
    private final int TTS_DURATION_SYMBOL_ETC = 300;
    private final int TTS_DURATION_SYMBOL_SYMBOL_NUMBER = 200;
    private final int[] TTS_DURATIONS = new int[] {TTS_DURATION_CARRAGE_RETURN, TTS_DURATION_SYMBOL_FORCED_IGNORE, TTS_DURATION_SYMBOL_ETC, TTS_DURATION_SYMBOL_SYMBOL_NUMBER};


    private Context context;
    private TextToSpeech textToSpeech;
    private Locale locale;

    private String toReadTTSText;

    private boolean onCalling;

    //private boolean isDone;
    private int speakType2onDoneCnt;

    public TTS(Context context, Locale locale, String ttsText) {
        this.context = context;
        this.locale = locale;
        textToSpeech = new TextToSpeech(context, this);
        textToSpeech.setOnUtteranceProgressListener(this);

        textToSpeech.setPitch(1.0f);         // 음성 톤은 기본 설정
        textToSpeech.setSpeechRate(1.0f);    // 읽는 속도를 0.9빠르기로 설정

        //isDone = false;
        onCalling = false;
        speakType2onDoneCnt = -1;//일부러 초기값은 onDone() 메서드에서 체크하는 값과는 다르게 한다

        toReadTTSText = ttsText;
    }


    public void speak(String text) {
        //speak_type1(text);
        speak_type2(text);
    }

    public void stop() {
        textToSpeech.stop();
    }

    public void shutdown() {

        textToSpeech.shutdown();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }

    public boolean getBeforeCalling() {
        return onCalling;
    }

    public void setOnCalling(boolean onCalling) {
        this.onCalling = onCalling;
    }

    public boolean getDoneYN() {
        //return isDone;
        return speakType2onDoneCnt == 0 ? true : false;
    }

    @Override
    public void onStart(String utteranceId) {
        Log.d(TAG, "onStart / utteranceID = " + utteranceId);
    }

    @Override
    public void onDone(String utteranceId) {
        //isDone = true;//다 재생했다면 초기화
        //Log.d(TAG, "onDone / utteranceID = " + utteranceId);

        --speakType2onDoneCnt;
        Log.d(TAG, "onDone / utteranceID = " + utteranceId + ", speakType2onDoneCnt:" + speakType2onDoneCnt);
    }

    @Override
    public void onError(String utteranceId) {
        Log.d(TAG, "onError / utteranceID = " + utteranceId);
    }

    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR)
            textToSpeech.setLanguage(locale);

        if(!onCalling)
            speak(null);
    }

//    private void speak_type1(String text) {
//
//        if(text == null) {
//            text = modifiyForRealityText(toReadTTSText);
//        } else {
//            text = modifiyForRealityText(text);
//        }
//
//        if(textToSpeech != null && text != null) { //onCalling 이라면 이번에 읽어주지 않아도 전화가 끊기면 다시 speak가 호출되기 때문
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                String myUtteranceID = "myUtteranceID";
//                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, myUtteranceID);
//            }
//            else {
//                HashMap<String, String> hashMap = new HashMap<>();
//                hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "myUtteranceID");
//                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, hashMap);
//            }
//        }
//
//    }

    private void speak_type2(String text) {

        if(text == null) {
            text = modifiyForRealityText2(toReadTTSText);
        } else {
            text = modifiyForRealityText2(text);
        }

        HashMap<String, String> myHash = new HashMap<String, String>();
        myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done");

        String[] splitspeech = text.split("#duration#");
        speakType2onDoneCnt = splitspeech.length;

        int durationIdx = -1;
        String textExceptDuration = null;
        for (int i = 0; i < splitspeech.length; i++) {

            if (i == 0) { // 첫번째는 무조건 기본으로 실행
                textToSpeech.speak(splitspeech[i].toString().trim(), TextToSpeech.QUEUE_FLUSH, myHash);
            } else {

                //"#duration#" 뒤에는 무조건 TYPE? 가 있따 ?는 TTS_DURATIONS[]의 index 이다
                textExceptDuration = splitspeech[i].toString().substring(1).trim();
                if(textExceptDuration.isEmpty()) {// 캐리지리턴 등이 연속으로 있을 경우는 한번만 duration이 일어나게...
                    --speakType2onDoneCnt;
                } else {
                    durationIdx = Integer.parseInt(splitspeech[i].toString().substring(0, 1));
                    textToSpeech.playSilence(TTS_DURATIONS[durationIdx], TextToSpeech.QUEUE_ADD, null);

                    textToSpeech.speak(textExceptDuration, TextToSpeech.QUEUE_ADD, myHash);
                }
            }
        }
    }

//    private String modifiyForRealityText(String originText) {
//
//        if(originText == null) {
//            Log.e("modifiyForRealityText", "modifiyForRealityText is null!!!");
//            return null;
//        }
//
//        //
//
//        StringBuffer sb = new StringBuffer();
//        String[] syllableArray = originText.trim().split("");
//
//        int sentenceStratIdx = 0;
//
//        for(int i = 0 ; i < syllableArray.length ; i++) {
//
//            //Log.d("modifiyForRealityText", "split[" + i + "]:'" + syllableArray[i] + "'");
//
//            /*
//            1) 숫자만 : ^[0-9]*$
//            2) 영문자만 : ^[a-zA-Z]*$
//            3) 한글만 : ^[가-힣]*$
//            4) 영어 & 숫자만 : ^[a-zA-Z0-9]*$
//             */
//            if(Pattern.matches("^[가-힣]*$", syllableArray[i])){
//                sb.append(syllableArray[i]);
//            } else if(Pattern.matches("^[a-zA-Z0-9]*$", syllableArray[i])){
//                //숫자이면 앞뒤에 스페이스 하나 추가 그렇지 않으면 8D를 '팔디'로 읽지 않고 '에잇디'로 읽으면 현장 용어와 맞지 않으므로
//                sb.append(" ");
//                sb.append(syllableArray[i]);
//                sb.append(" ");
//            } else {
//                //특수기호는 우선 스페이스로 치환
//                if(syllableArray[i].equals("/")) {
//                    sb.append("슬래시");
//                } else if(syllableArray[i].equals("-")) {
//                    sb.append("다시");
//                }else
//                    sb.append(" ");
//            }
//
//        }
//
//        //Log.d("modifiyForRealityText", "sb:'" + sb.toString() + "'");
//        return sb.toString();
//    }


    private String modifiyForRealityText2(String originText) {

        if(originText == null) {
            Log.e("modifiyForRealityText2", "modifiyForRealityText is null!!!");
            return null;
        }

        StringBuffer sb = new StringBuffer();
        String[] syllableArray = originText.trim().split("");

        boolean alpabetNumberMixed = false;
        for(int i = 0 ; i < syllableArray.length ; i++) {

            /*
            1) 숫자만 : ^[0-9]*$
            2) 영문자만 : ^[a-zA-Z]*$
            3) 한글만 : ^[가-힣]*$
            4) 영어 & 숫자만 : ^[a-zA-Z0-9]*$
             */
            if(Pattern.matches("^[가-힣]*$", syllableArray[i])){
                sb.append(syllableArray[i]);
                alpabetNumberMixed = false;
            } else if(Pattern.matches("^[a-z]*$", syllableArray[i])){//알파벳 소문자면
                if(i > 0 && (alpabetNumberMixed || Pattern.matches("^[0-9]*$", syllableArray[i-1]))) {//이전이 숫자였으면 duraion 되도록
                    alpabetNumberMixed = true;
                    sb.append("#duration#");
                    sb.append("3");//TTS_DURATIONS[TTS_DURATION_SYMBOL_SYMBOL_NUMBER]
                }

                sb.append(syllableArray[i]);
            } else if(Pattern.matches("^[A-Z]*$", syllableArray[i])){//알파벳 대문자면
                alpabetNumberMixed = true;
                sb.append("#duration#");
                sb.append("3");//TTS_DURATIONS[TTS_DURATION_SYMBOL_SYMBOL_NUMBER]

                sb.append(syllableArray[i]);
            } else if(Pattern.matches("^[0-9]*$", syllableArray[i])){
                if(i > 0 && (alpabetNumberMixed || Pattern.matches("^[a-zA-Z]*$", syllableArray[i-1]))) {
                    alpabetNumberMixed = true;
                    sb.append("#duration#");
                    sb.append("3");//TTS_DURATIONS[TTS_DURATION_SYMBOL_SYMBOL_NUMBER]
                }

                sb.append(syllableArray[i]);
            } else {
                alpabetNumberMixed = false;
                if(
                        syllableArray[i].equals("\n") || syllableArray[i].equals("\r")

                        ) {
                    sb.append("#duration#");
                    sb.append("0");//TTS_DURATIONS[TTS_DURATION_CARRAGE_RETURN]
                    //Log.d("modifiyForRealityText", "syllableArray[" + i + "]=" + syllableArray[i]);
                } else if(
                        syllableArray[i].equals("/")
                                || syllableArray[i].equals("-")
                                || syllableArray[i].equals(":")
                                || syllableArray[i].equals(",")

                        ) {
                    sb.append("#duration#");
                    sb.append("1");//TTS_DURATIONS[TTS_DURATION_SYMBOL_FORCED_IGNORE]
                    //Log.d("modifiyForRealityText", "syllableArray[" + i + "]=" + syllableArray[i]);
                } else if(
                        syllableArray[i].equals(".")

                        ) {
                    sb.append(syllableArray[i]);
                } else {
                    //스페이스를 넣으면 TTS 로직의 스페이스 띄움만큼의 읽기가 될 것이고
                    //장단점 : 자연스러울테지만 제조사마다의 차이가 있을 수 있어 통일성이 없어질 수도
                    sb.append(" ");

//                    //TTS_DURATIONS[TTS_DURATION_SYMBOL_ETC] 을 넣어주면 우리가 정한 ms 만큼 띈다.
//                    //장단점 : 전체 폰에서 동일할 수 있으나 의도치 않은 부자연스러운 읽기가 또 발생할 수도
//                    sb.append("#duration#");
//                    sb.append("2");//TTS_DURATIONS[TTS_DURATION_SYMBOL_ETC]
                }
            }

        }

        //Log.d("modifiyForRealityText", "sb:'" + sb.toString() + "'");
        return sb.toString();
    }
}
