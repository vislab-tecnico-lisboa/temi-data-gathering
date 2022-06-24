package com.example.temihealthassistant;

import static java.lang.System.currentTimeMillis;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.gson.JsonObject;


import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
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
import com.robotemi.sdk.listeners.OnDetectionDataChangedListener;
import com.robotemi.sdk.model.DetectionData;
import com.robotemi.sdk.navigation.listener.OnCurrentPositionChangedListener;
import com.robotemi.sdk.navigation.model.Position;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        Robot.TtsListener,
        OnRequestPermissionResultListener,
        OnFaceRecognizedListener,
        OnDistanceToLocationChangedListener,
        OnGoToLocationStatusChangedListener,
        Robot.AsrListener,
        OnDetectionDataChangedListener,
        OnCurrentPositionChangedListener{

    Robot robot;
    int question = 0;
    int[] answers = {0, 0, 0, 0};
    double total= 0;
    double rating = 0;
    String firstName;
    String lastName;
    Boolean personFound = false;
    String userName;

    private String DISTANCE_TAG = "Distance";
    private String DETECTION_DATA_TAG = "Detection";
    private String FACE_RECOGNIZED_TAG = "FaceRecognition";
    private String HANDLER_TAG = "Handler";
    private String NONE_VALUE = "None";

    String currentPositionValue;
    String currentGoalValue;
    String lastFaceDetectionValue;

    JSONObject currentPosition = new JSONObject();
    JSONObject lastDetection = new JSONObject();
    JSONObject currentGoal = new JSONObject();
    JSONObject lastFaceDetection = new JSONObject();
    final JSONObject json = new JSONObject();

    private EditText etName;

    int conversationStatus = 0; //0 - StandBy, 1 - Searching, 2 - Person Found, 3 - In Conversation

    private List<String> locations = new ArrayList<>();
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

    //private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    //MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));

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

    private final List<String> AnswersText = new ArrayList<>();



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

        Log.i("PEDRO", "APP Initialized");
        /*try {
            System.out.println(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

        //String ipAddress = "10.1.20.87";
        byte[] addr = new byte[]{10,1,20,87};

        try {
            InetAddress  inet = InetAddress.getByAddress(addr);
            boolean reachable = inet.isReachable(5000);
            if(reachable) Log.i("PEDRO", "Ping");
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*try {
            MongoClient mongoClient = new MongoClient("10.0.2.15", 27017);
            Log.i("PEDRO", "MongoDB CLient");
            DB database = mongoClient.getDB("test");
            Log.i("PEDRO", "GET Database");
            DBCollection collection = database.getCollection("custom");
            Log.i("PEDRO", "Create Collection");
            BasicDBObject document = new BasicDBObject();
            document.put("name", "Shubham");
            document.put("company", "Baeldung");
            collection.insert(document);
            Log.i("PEDRO", "Insert Document");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/



        //executor.scheduleAtFixedRate(runnable, 0, 100, TimeUnit.MILLISECONDS);


        /*Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("delayed hello world");
            }
        },  4);*/

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 1 second
                        try {
                            json.put("Position", currentPosition.toString());
                            json.put("Goal", currentGoal.toString());
                            json.put("Person", lastFaceDetection.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("PEDRO", json.toString());
                    }
                }, 1000);
            }
        });*/
        /*if(robot.checkSelfPermission(Permission.FACE_RECOGNITION) != Permission.GRANTED) {
            robot.startFaceRecognition();
            Log.i("PEDRO", ">>>>Face Recognition Started");
        }*/


    }

    Runnable runnable = new Runnable() {
        public void run() {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println(Arrays.toString(AnswersText.toArray()));

            try {
                if(currentPosition.length() == 0) currentPositionValue = NONE_VALUE; else currentPositionValue = currentPosition.toString();
                if(currentGoal.length() == 0)currentGoalValue= NONE_VALUE; else currentGoalValue = currentGoal.toString();
                if(lastFaceDetection.length() == 0) lastFaceDetectionValue= NONE_VALUE; else lastFaceDetectionValue = lastFaceDetection.toString();
                json.put("Position", currentPositionValue);
                json.put("Goal", currentGoalValue);
                json.put("Person", lastFaceDetectionValue);
                json.put("Questions", Arrays.toString(questionsText));
                json.put("Answers", Arrays.toString(AnswersText.toArray()));
                json.put("TimeStamp", timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("PEDRO", json.toString());

        }
    };


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
        robot.addOnDetectionDataChangedListener(this);
        robot.addOnCurrentPositionChangedListener(this);
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
        robot.removeOnDetectionDataChangedListener(this);
        robot.removeOnCurrentPositionChangedListener(this);
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
        int rand_int1 = rand.nextInt(5);
        TtsRequest ttsRequest = TtsRequest.create(AnswerNever[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();
        answers[question-1] = 4;
        question++;
        nextQuestion(question);
    }

    public void answerRarely(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        int rand_int1 = rand.nextInt(4);
        TtsRequest ttsRequest = TtsRequest.create(AnswerRarely[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();
        answers[question-1] = 3;
        question++;
        nextQuestion(question);
    }

    public void answerSometimes(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        TtsRequest ttsRequest = TtsRequest.create("Could be worse, I guess!", false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();
        answers[question-1] = 2;
        question++;
        nextQuestion(question);
    }

    public void answerAlways(View view){
        Log.i("PEDRO", ">>>>Answer Heard");
        int rand_int1 = rand.nextInt(5);
        TtsRequest ttsRequest = TtsRequest.create(AnswerAlways[rand_int1], false);
        robot.speak(ttsRequest);
        sleepy(4);
        robot.finishConversation();
        answers[question-1] = 1;
        question++;
        nextQuestion(question);
    }



    public void nextQuestion(int question) {
        nextLayout();
        if(question<=9) {
            robot.askQuestion(questionsText[question]);
            Log.i("PEDRO", ">>>>Question asked");
        } else if(question==10){
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
            case 5: setContentView(R.layout.dialog_finish);
        }
    }

    public void nextLocation(){
        /*if(itr.hasNext()){
            robot.goTo(itr.next());
        } */
        JSONObject nextLocation = new JSONObject();

        if(locationNumber < (size-1)){
            locationNumber ++;
            try {
                nextLocation.put("Next Location", locations.get(locationNumber));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            currentGoal = nextLocation;
            robot.goTo(locations.get(locationNumber));
        }else {
            try {
                nextLocation.put("Next Location", "home base");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            currentGoal = nextLocation;
            robot.goTo("home base");
        }
    }


    public void lookAround(){
        Log.i("PEDRO", ">>>>Looking around");
        robot.turnBy(100, 1);
        sleepy(4);
        Log.i("PEDRO", ">>>>Turning other way");
        robot.turnBy(-200, 1);
        sleepy(7);
        Log.i("PEDRO", ">>>>Back to position");
        robot.turnBy(100, 1);
        sleepy(6);
        nextLocation();
        //robot.goTo("home base");
    }



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
                AnswersText.add("Never");
                int rand_int1 = rand.nextInt(5);
                TtsRequest ttsRequest = TtsRequest.create(AnswerNever[rand_int1], false);
                robot.speak(ttsRequest);
                sleepy(4);
                robot.finishConversation();
                answers[question-1] = 4;
                question++;
                nextQuestion(question);
            }
        } else if (asrResult.contains("rarely")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            AnswersText.add("Rarely");
            int rand_int1 = rand.nextInt(4);
            TtsRequest ttsRequest = TtsRequest.create(AnswerRarely[rand_int1], false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();
            answers[question-1] = 3;
            question++;
            nextQuestion(question);
        } else if (asrResult.contains("sometimes")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            AnswersText.add("Sometimes");
            TtsRequest ttsRequest = TtsRequest.create("Could be worse, I guess!", false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();
            answers[question-1] = 2;
            question++;
            nextQuestion(question);
        } else if (asrResult.contains("always")) {
            Log.i("PEDRO", ">>>>Answer Heard");
            AnswersText.add("Always");
            int rand_int1 = rand.nextInt(5);
            TtsRequest ttsRequest = TtsRequest.create(AnswerAlways[rand_int1], false);
            robot.speak(ttsRequest);
            sleepy(4);
            robot.finishConversation();
            answers[question-1] = 1;
            question++;
            nextQuestion(question);
        } else if(asrResult.contains("End Call")){
            setContentView(R.layout.activity_main);
        }
        else if(asrResult.contains("I'm here")){
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
                    TtsRequest ttsRequest = TtsRequest.create("Hey! Is anybody here? I'm looking for Pedro!", false);
                    robot.speak(ttsRequest);
                    sleepy(4);
                    robot.tiltAngle(25);
                    lookAround();
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
        JSONObject newFaceDetection = new JSONObject();
        for (ContactModel contactModel : contactModelList) {
            firstName = contactModel.getFirstName();
            lastName = contactModel.getLastName();
            try {
                newFaceDetection.put("First Name", contactModel.getFirstName());
                newFaceDetection.put("Last Name", contactModel.getLastName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("PEDRO", newFaceDetection.toString());
            lastFaceDetection = newFaceDetection;

            if(firstName.contains("Pedro")){
                Log.i("PEDRO", ">>>>Pedro Identified");
                System.out.print("Pedro Identified");
                personFound = true;
                robot.stopMovement();
                System.out.print("Movement stopped");
                Log.i("PEDRO", ">>>>Movement Stopped");
                robot.stopFaceRecognition();
                robot.askQuestion("Hi, "+ firstName + ". Glad I found you! I have a few questions to ask you! Whenever you're ready just say Start!");
                //robot.startTelepresence("Pedro Custódio", "Pedro Custódio");
            }
        }
    }

    @Override
    public void onDetectionDataChanged(@NonNull DetectionData detectionData) {
        JSONObject newDetection =  new JSONObject();
        try {
            newDetection.put("isDetected", Boolean.valueOf(detectionData.isDetected()));
            newDetection.put("Distance", detectionData.getDistance());
            newDetection.put("angle", detectionData.getAngle());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("PEDRO", newDetection.toString());

        lastDetection = newDetection;
    }

    @Override
    public void onCurrentPositionChanged(@NonNull Position position) {
        JSONObject newPosition = new JSONObject();
        try{
            newPosition.put("x", position.getX());
            newPosition.put("y", position.getY());
            newPosition.put("yaw", position.getYaw());
            newPosition.put("tiltAngle", position.getTiltAngle());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("PEDRO", newPosition.toString());

        currentPosition = newPosition;

    }

   /* public void getLocations(View view){
        locations = robot.getLocations();

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, locations);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener((parent, view1, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
            view1.animate().setDuration(2000).alpha(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            locations.remove(item);
                            adapter.notifyDataSetChanged();
                            view1.setAlpha(1);
                        }
                    });
        });

    }*/

   /* public void locationSettings(View view){

        Intent myIntent = new Intent(MainActivity.this, LocationActivity.class);
        MainActivity.this.startActivity(myIntent);

        /*setContentView(R.layout.location_settings);
        final ListView listview = (ListView) findViewById(R.id.listview);

        locations = robot.getLocations();

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, locations);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener((parent, view1, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
            view1.animate().setDuration(2000).alpha(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            locations.remove(item);
                            adapter.notifyDataSetChanged();
                            view1.setAlpha(1);
                        }
                    });
        });
    }

    private static class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }*/

}
