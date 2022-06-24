package com.example.testdialogtemi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.CheckResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.constants.SdkConstants;
import com.robotemi.sdk.face.ContactModel;
import com.robotemi.sdk.face.OnFaceRecognizedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.listener.OnDistanceToLocationChangedListener;
import com.robotemi.sdk.permission.OnRequestPermissionResultListener;
import com.robotemi.sdk.permission.Permission;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        Robot.TtsListener,
        OnRequestPermissionResultListener,
        OnFaceRecognizedListener,
        OnDistanceToLocationChangedListener,
        OnGoToLocationStatusChangedListener,
        Robot.AsrListener{

    Robot robot;
    int question = 0;
    int[] answers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    double total= 0;
    double rating = 0;
    String firstName;
    String lastName;
    Boolean personFound = false;
    String userName;

    private EditText etName;

    int conversationStatus = 0; //0 - StandBy, 1 - Searching, 2 - Person Found, 3 - In Conversation

    private List<String> locations = new ArrayList<>();;
    Iterator<String> itr = null;
    int size = 0;
    int locationNumber = 0;

    Random rand = new Random();

    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final int REQUEST_CODE_NORMAL = 0;
    private static final int REQUEST_CODE_FACE_START = 1;
    private static final int REQUEST_CODE_FACE_STOP = 2;
//    private static final int REQUEST_CODE_MAP = 3;
//    private static final int REQUEST_CODE_SEQUENCE_FETCH_ALL = 4;
//    private static final int REQUEST_CODE_SEQUENCE_PLAY = 5;
//    private static final int REQUEST_CODE_START_DETECTION_WITH_DISTANCE = 6;
//    private static final int REQUEST_CODE_SEQUENCE_PLAY_WITHOUT_PLAYER = 7;
//    private static final int REQUEST_CODE_GET_MAP_LIST = 8;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    String[] questionsText = {
            "Hi! I have a few questions to ask you! Whenever you are ready just press Start!",
            "How often do you need help looking after yourself?",
            "How about when doing household tasks? Do you often need help?",
            "Do you often feel challenged when getting around your home or community?",
            "Do you ever feel that your health affects negatively your relationships with friends and family?",
            "Regarding your vision, how often do you struggle with seeing clearly?",
            "Do you ever feel any difficulty hearing clearly?",
            "How often do you have a hard time communicating with others?",
            "Do you ever feel difficulty when trying to sleep?",
            "How often do you feel anxious, worried or depressed?",
            "How often do you experience pain or discomfort?"

    };

    String[] AnswerNever = {
            "That's great to hear!",
            "Great news then!",
            "I'm glad to hear that",
            "Awesome! Let's keep going",
            "I'm no doctor but that sounds like good news to me!"
    };

    String[] AnswerRarely = {
            "That's good to hear!",
            "Good news then!",
            "Good, let's continue then!",
            "You even sound well!"
    };

    String[] AnswerAlways = {
            "Oh i'm sorry to hear that. Hope you feel better soon!",
            "Oh that's too bad. But I bet you'll be better in no time!",
            "Sorry you feel that way",
            "Oh that's a shame. But keep going strong!",
            ""
    };

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        verifyStoragePermissions(this);
        etName = findViewById(R.id.etName);
        robot = Robot.getInstance();
        robot.addOnRequestPermissionResultListener(this);
        robot.addOnFaceRecognizedListener(this);

        if(!robot.isSelectedKioskApp()){
            Log.i("PEDRO", ">>>>Is not selected as kiosk App");
            robot.requestToBeKioskApp();
        } else Log.i("PEDRO", ">>>>Is selected as kiosk App");

        /*if(robot.checkSelfPermission(Permission.FACE_RECOGNITION) != Permission.GRANTED) {
            robot.startFaceRecognition();
            Log.i("PEDRO", ">>>>Face Recognition Started");
        }*/


        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                    /*robot.startFaceRecognition();
                    Log.i("PEDRO", ">>>>Face Recognition Started");

                //TtsRequest ttsRequest = TtsRequest.create("Olá! Tenho umas perguntas para lhe fazer. Quando estiver pronto diga iniciar!", true, TtsRequest.Language.valueToEnum(14));
                TtsRequest ttsRequest = TtsRequest.create("Can I try to recognize you?", false);
                robot.speak(ttsRequest);
                sleepy(6);
                robot.finishConversation();
                robot.wakeup();
            }
        }, 3000);*/

    }


    @Override
    protected void onDestroy() {
        robot.removeOnRequestPermissionResultListener(this);
        robot.removeOnFaceRecognizedListener(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        robot.addOnRobotReadyListener(this);
        robot.addTtsListener(this);
        robot.addAsrListener(this);
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addOnDistanceToLocationChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
        robot.removeTtsListener(this);
        if (robot.checkSelfPermission(Permission.FACE_RECOGNITION) == Permission.GRANTED) {
            robot.stopFaceRecognition();
        }
        robot.removeAsrListener(this);
        robot.removeOnGoToLocationStatusChangedListener(this);
        robot.removeOnDistanceToLocationChangedListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                // Robot.getInstance().onStart() method may change the visibility of top bar.
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void questionnaireSelected(View view){
        setContentView(R.layout.activity_main);
    }

    public void openBrowser(View view) {
        startActivity(browserIntent);
    }

    public void requestFace(View view) {
        if (robot.checkSelfPermission(Permission.FACE_RECOGNITION) == Permission.GRANTED) {
            Log.i("PEDRO", ">>>>Face Recognition permission already granted");
            return;
        }
        List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.FACE_RECOGNITION);
        robot.requestPermissions(permissions, REQUEST_CODE_NORMAL);
        Log.i("PEDRO", ">>>>Face Recognition granted");
    }

    public void requestSettings(View view) {
        if (robot.checkSelfPermission(Permission.SETTINGS) == Permission.GRANTED) {
            Log.i("PEDRO", ">>>>Settings permission already granted");
            return;
        }
        List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.SETTINGS);
        robot.requestPermissions(permissions, REQUEST_CODE_NORMAL);
    }

    public void requestMap(View view) {
        if (robot.checkSelfPermission(Permission.MAP) == Permission.GRANTED) {
            Toast.makeText(this, "You already had MAP permission.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.MAP);
        robot.requestPermissions(permissions, REQUEST_CODE_NORMAL);
    }

    public void confirmUser(View view){
        List<ContactModel> contactModelList = null;
        for (ContactModel contactModel : contactModelList) {
            String first_name = contactModel.getFirstName();
            String last_name = contactModel.getLastName();
            if(etName.getText().toString().toLowerCase().trim().equals(first_name + " " + last_name)){
                userName = first_name + " " + last_name;
            } else {
                Toast.makeText(this, "User doesn't exist. Try again. ", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @CheckResult
    private boolean requestPermissionIfNeeded(Permission permission) {
        if (robot.checkSelfPermission(permission) == Permission.GRANTED) {
            return false;
        }
        robot.requestPermissions(Collections.singletonList(permission), MainActivity.REQUEST_CODE_FACE_START);
        return true;
    }

    @Override
    public void onRequestPermissionResult(@NotNull Permission permission, int grantResult, int requestCode) {
        if (grantResult == Permission.DENIED) {
            return;
        }
        // Permission is granted. Continue the action or workflow in your app.
        switch (permission) {
            case FACE_RECOGNITION:
                if (requestCode == REQUEST_CODE_FACE_START) {
                    robot.startFaceRecognition();
                } else if (requestCode == REQUEST_CODE_FACE_STOP) {
                    robot.stopFaceRecognition();
                }
                break;
        }

    }

    public void answerNever(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        /*int rand_int1 = rand.nextInt(5);
        TtsRequest ttsRequest = TtsRequest.create(AnswerNever[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();*/
        answers[question-1] = 4;
        question++;
        nextQuestion(question);
    }

    public void answerRarely(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        /*int rand_int1 = rand.nextInt(4);
        TtsRequest ttsRequest = TtsRequest.create(AnswerRarely[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();*/
        answers[question-1] = 3;
        question++;
        nextQuestion(question);
    }

    public void answerSometimes(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        /*TtsRequest ttsRequest = TtsRequest.create("Could be worse, I guess!", false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();*/
        answers[question-1] = 2;
        question++;
        nextQuestion(question);
    }

    public void answerAlways(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        /*int rand_int1 = rand.nextInt(5);
        TtsRequest ttsRequest = TtsRequest.create(AnswerAlways[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();*/
        answers[question-1] = 1;
        question++;
        nextQuestion(question);
    }



    public void nextQuestion(int question) {
        nextLayout();
        if(question==11){
            for (int answer : answers) {
                total = total + answer;
            }
            rating = total/answers.length;

            if(rating>=3){
                TtsRequest ttsRequest = TtsRequest.create("Great! You seem to be feeling really well!", false);
                robot.speak(ttsRequest);
            } else if(rating>=2){
                TtsRequest ttsRequest = TtsRequest.create("Thank you. Hope you feel even better next time!", false);
                robot.speak(ttsRequest);
            } else if(rating>=1){
                TtsRequest ttsRequest = TtsRequest.create("I'm sorry you're not feeling your best. I'm sure you'll soon get better!", false);
                robot.speak(ttsRequest);
            }
            sleepy(6);
            TtsRequest ttsRequest = TtsRequest.create("Those are all my questions for now. We can take a break now! Thank you! I'll go back to my base", false);
            robot.speak(ttsRequest);
            sleepy(10);
            robot.goTo("home base");
        }
    }

    public void nextLayout(){
        switch(question){
            case 1: setContentView(R.layout.first_question);
            break;
            case 2: setContentView(R.layout.second_question);
            break;
            case 3: setContentView(R.layout.third_question);
            break;
            case 4: setContentView(R.layout.fourth_question);
            break;
            case 5: setContentView(R.layout.fifth_question);
            break;
            case 6: setContentView(R.layout.sixth_question);
            break;
            case 7: setContentView(R.layout.seventh_question);
            break;
            case 8: setContentView(R.layout.eighth_question);
            break;
            case 9: setContentView(R.layout.ninth_question);
            break;
            case 10: setContentView(R.layout.tenth_question);
            break;
            case 11: setContentView(R.layout.dialog_finish);
        }
    }

    public void nextLocation(){
        /*if(itr.hasNext()){
            robot.goTo(itr.next());
        } */
        if(locationNumber < (size-1)){
            locationNumber ++;
            robot.goTo(locations.get(locationNumber));
        }else {
            robot.goTo("home base");
        }
    }


    /*public void lookAround(){
        Log.i("PEDRO", ">>>>Looking around");
        robot.turnBy(100, 1);
        sleepy(4);
        Log.i("PEDRO", ">>>>Turning other way");
        robot.turnBy(-200, 1);
        sleepy(7);
        Log.i("PEDRO", ">>>>Back to position");
        robot.turnBy(100, 1);
        sleepy(6);
        //nextLocation();
        //robot.goTo("home base");
    }*/



    public void startFaceRecognition(View view) {
        if (requestPermissionIfNeeded(Permission.FACE_RECOGNITION)) {
            return;
        }
        locations = robot.getLocations();
        size = locations.size();
        itr = locations.listIterator();
        robot.startFaceRecognition();
        Log.i("PEDRO", ">>>>Face Recognition started");
        //robot.goTo("consroom");
        nextLocation();
    }

    @Override
    public void onAsrResult(@NotNull String asrResult) {
        Log.i("PEDRO", ">>>>Asr Result: " + asrResult);
        try {
            Bundle metadata = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            if (metadata == null) return;
            if (!robot.isSelectedKioskApp()) return;
            if (!metadata.getBoolean(SdkConstants.METADATA_OVERRIDE_NLU)) return;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        if (asrResult.contains("start")) {
            conversationStatus = 3;
            robot.tiltAngle(40);
            Log.i("PEDRO", ">>>>Questionnaire Started");
            TtsRequest ttsRequest = TtsRequest.create("Ok Let's Start! Please answer with never, rarely, sometimes or always.", false);
            robot.speak(ttsRequest);
            sleepy(10);
            robot.finishConversation();
            question++;
            nextQuestion(question);
        }else if(asrResult.contains("not ready")||asrResult.contains("go away")||asrResult.contains("don't want")){
            TtsRequest ttsRequest = TtsRequest.create("Ok Sorry! I'll come back later when you're free", false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();
            robot.goTo("home base");
        }else if (asrResult.contains("never")) {
            if(conversationStatus == 3){
                Log.i("PEDRO", ">>>>Answer Heard");
                /*int rand_int1 = rand.nextInt(5);
                TtsRequest ttsRequest = TtsRequest.create(AnswerNever[rand_int1], false);
                robot.speak(ttsRequest);
                sleepy(4);
                robot.finishConversation();*/
                answers[question-1] = 4;
                question++;
                nextQuestion(question);
            }
        } else if (asrResult.contains("rarely")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            /*int rand_int1 = rand.nextInt(4);
            TtsRequest ttsRequest = TtsRequest.create(AnswerRarely[rand_int1], false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();*/
            answers[question-1] = 3;
            question++;
            nextQuestion(question);
        } else if (asrResult.contains("sometimes")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            /*TtsRequest ttsRequest = TtsRequest.create("Could be worse, I guess!", false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();*/
            answers[question-1] = 2;
            question++;
            nextQuestion(question);
        } else if (asrResult.contains("always")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            /*int rand_int1 = rand.nextInt(5);
            TtsRequest ttsRequest = TtsRequest.create(AnswerAlways[rand_int1], false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();*/
            answers[question-1] = 1;
            question++;
            nextQuestion(question);
        } else if(asrResult.contains("End Call")){
            setContentView(R.layout.activity_main);
        } else if(asrResult.contains("I'm here")){
            TtsRequest ttsRequest = TtsRequest.create("Sorry. Didn't see you. Can you come closer so I can see your face?", false);
            robot.speak(ttsRequest);
            sleepy(4);
        }
        else {
            Log.i("PEDRO", ">>>>Answer Misheard");
            robot.askQuestion("Oh sorry I didn't catch what you said. Do you mind repeating? Please use the words Never, Rarely, Sometimes or Always");
        }


    }

    /*public void disableKiosk(View view){
        robot.requestPermissions(Collections.singletonList(Permission.FACE_RECOGNITION), REQUEST_CODE_FACE_START);
        robot.setKioskModeOn(false);
        if(!robot.isKioskModeOn()){
            Log.i("PEDRO", ">>>>Kiosk Mode turned off");
        }
    }/*/

    @Override
    public void onDistanceToLocationChanged(@NotNull Map<String, Float> distances) {
        for (String location : distances.keySet()) {
            Log.i("onDistanceToLocation", "location:" + location + ", distance:" + distances.get(location));
        }
    }

    @Override
    public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {

    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionId, @NotNull String description) {

        if(!location.equals("home base")){
            label:
            switch (status) {
                case OnGoToLocationStatusChangedListener.START:
                    Log.i("PEDRO", ">>>>Status=" + status );
                    break;

                case OnGoToLocationStatusChangedListener.CALCULATING:
                    Log.i("PEDRO", ">>>>Status=" + status);
                    break;

                case OnGoToLocationStatusChangedListener.GOING:
                    Log.i("PEDRO", ">>>>Status=" + status + "Going to: " + location);
                    break;

                case OnGoToLocationStatusChangedListener.COMPLETE:
                    robot.tiltAngle(25);
                    sleepy(8);
                    nextLocation();
                    /*Log.i("PEDRO", ">>>>Status=" + status + ", Arrived at " + location);
                    sleepy(10);
                    Log.i("PEDRO", ">>>>Looking around in " + location);
                    robot.turnBy(100, 1);
                    sleepy(15);
                    Log.i("PEDRO", ">>>>Turning other way");
                    robot.turnBy(-200, 1);
                    sleepy(15);
                    nextLocation();*/


                    /*switch (location) {
                        case "consroom":
                            robot.goTo("p2");
                            break label;
                        case "p2":
                            robot.goTo("startpoint");
                            break label;
                        case "startpoint":
                            robot.goTo("home base");
                            break label;
                    }*/
                    break;

                case OnGoToLocationStatusChangedListener.ABORT:
                    Log.i("PEDRO", ">>>>Status=" + status);
                    break;
            }

        }

    }

    public void sleepy(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public void onConversationStatusChanged(int status, @NotNull String s) {
            //Log.i("PEDRO", ">>>>Status=" + status);
            //sleepy(2);
    }*/

    @Override
    public void onFaceRecognized(@NotNull List<ContactModel> contactModelList) {
        Log.i("PEDRO", ">>>>Face Recognized");
        for (ContactModel contactModel : contactModelList) {
            firstName = contactModel.getFirstName();
            lastName = contactModel.getLastName();
            if(firstName.contains("Pedro")){
                Log.i("PEDRO", ">>>>Pedro Identified");
                System.out.print("Pedro Identified");
                personFound = true;
                robot.stopMovement();
                robot.skidJoy(1,1);
                System.out.print("Movement stopped");
                Log.i("PEDRO", ">>>>Movement Stopped");
                robot.stopFaceRecognition();
                robot.askQuestion("Hi, "+ firstName + ". Glad I found you! I have a few questions to ask you! Whenever you're ready just say Start!");
                //robot.startTelepresence("Pedro Custódio", "Pedro Custódio");
            }
        }
    }
}
