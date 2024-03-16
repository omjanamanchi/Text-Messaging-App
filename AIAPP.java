package com.example.textmessagingapp;

import static android.view.View.GONE;

import static com.example.textmessagingapp.Message.SENT_BY_BOT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIAPP extends AppCompatActivity {
    public RecyclerView recyclerView;
    public TextView welcomeTextView;
    public EditText messageEditText;
    public ImageButton sendButton;
    public int levelCheck = 0;

    public boolean currentlyRunning = false;
    public boolean greetingsCheck = false;
    public boolean topicsCheck = false;
    public boolean topicsCheck2 = false;
    public int topicResponseNum = 0;
    public String topic = "";
    public int topicTempVal = 0;

    public String[] userGreetings = new String[]{"Hello!", "Good Morning!", "Hello World!"};
    public String[] negativeResponses = new String[]{"Idk if I've already told you this, but I will only start once you address me with respect.", "I will only be able to start once you talk to me properly.", "Please talk to me properly, then I will start."};
    public String[] greetings = new String[]{"Hello!", "Hola!", "Bonjour!", "Nihao!", "Namaste!"};
    public String afterGreeting = " I can talk about Superheros, Space, or Indian Food. What would you like to talk about today?";
    public String[] topicChoices = new String[]{"Superheros", "Space", "Indian Food"};

    public String[] superherosExtension = new String[]{"Who is the superhero that is also known as the “Man of Steel?”", "What year was the first Iron Movie movie released?", "What is Batman’s real name? ", "How did Spider-man get his superpowers?", "What is Superman’s weakness?"};
    public String[] superherosResponses = new String[]{"Superman", "2008", "Bruce Wayne", "He was bitten by a radioactive spider", "Kryptonite"};
    List<String> superherosExtension2 = Arrays.asList(superherosExtension);
    List<String> superherosResponses2 = Arrays.asList(superherosResponses);

    public String[] spaceExtension = new String[]{"How old is the Universe?", "How many constellations are there?", "Which planet has a hexagonal-shaped storm?", "What are the storms produced by the sun called?", "Which constellation represents a hunter and weapons?"};
    public String[] spaceResponses = new String[]{"13.7 billion years", "88", "Saturn", "Solar Storms", "Orion"};
    List<String> spaceExtension2 = Arrays.asList(spaceExtension);
    List<String> spaceResponses2 = Arrays.asList(spaceResponses);

    public String[] foodExtension = new String[]{"Sambar or Chutney for Dosa at 7AM in the morning?", "Favorite Indian Snack?", "Favorite Indian Sweet?", "What Part of India has the Best Cuisine?", "Favorite Indian Restaurant?"};
    public String[] foodResponses = new String[]{"Bro, sambar 11 times out of 10.", "Samosas have their charm, but my favorite is the puffs they sell in Chennai stalls near the malls.", "Kajju Khatli is my favorite.", "Maybe I'm biased, but South India clearly wins in the food department.", "Adayar Ananda Bhavan; they never fail to impress."};
    List<String> foodExtension2 = Arrays.asList(foodExtension);
    List<String> foodResponses2 = Arrays.asList(foodResponses);

    List<Message> messageList;
    MessageAdapter messageAdapter;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aiapp);

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);

        messageList = new ArrayList<>();

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(levelCheck == -1){
                    levelCheck = 0;
                }
                String question = messageEditText.getText().toString().trim();
                addToChat(question, Message.SENT_BY_ME);
                currentlyRunning = false;
                thinkAboutResponse(question);
                messageEditText.setText("");
                welcomeTextView.setVisibility(View.GONE);
            }
        });
    }

    void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            public void run() {
                addToChat(response, Message.SENT_BY_BOT);
                if(levelCheck == 3) { levelCheck = 2; }
            }
        }, 3000);

    }

    void thinkAboutResponse(String question){

        if((question.equals("Goodbye") || question.equals("End Convo") || question.equals("Bye")) && levelCheck!=0){
            addResponse("Goodbye! Fun talking with you.");
            levelCheck = -1;
            greetingsCheck = false;
            topicsCheck = false;
            topicsCheck2 = false;
        }

        //state 0 responses
        if(levelCheck == 0 && !currentlyRunning){
            if(question.equals("Goodbye")){
                addResponse("Why are you saying goodbye?");
            }
            for(String s : userGreetings){
                if(question.equals(s)){
                    currentlyRunning = true;
                    greetingsCheck = true;
                }
            }
            if(greetingsCheck && levelCheck == 0){
                Random r = new Random();
                int randomNumber = r.nextInt(greetings.length);
                String temp = greetings[randomNumber];
                String temp2 = temp + "" + afterGreeting;
                addResponse(temp2);
                levelCheck = 1;
            }
            else if (greetingsCheck == false){
                Random r = new Random();
                int rN2 = r.nextInt(greetings.length);
                addResponse(negativeResponses[rN2]);
            }
        }

        //state 1 responses
        if(levelCheck==1 && !currentlyRunning){
            for(String s2 : topicChoices){
                if(question.equals(s2)){
                    currentlyRunning = true;
                    topicsCheck = true;
                    topic = s2;
                }
            }
            if(topicsCheck && levelCheck == 1){

                addResponse("Cool! Let's Talk about " + topic + "! Give me a talking point and I'll give you my thoughts.");
                topicsCheck = false;
                levelCheck = 2;
                currentlyRunning = true;
            }
            else if (topicsCheck == false){
                addResponse("That wasn't one of the choices genius! Try again . . . ");
            }
        }

        //state 2 responses
        if(levelCheck == 2 && !currentlyRunning){
            if(question.equals("Switch")){
                levelCheck = 1;
                topic = "-";
                addResponse("Ok! I can talk about Superheros, Space, or Indian Food. What would you like to talk about today?");
            }

            if(topic.equals("Superheros")){
                int x = -1;
                for(String s3 : superherosExtension){
                    x++;
                    if(question.equals(s3)){
                        currentlyRunning = true;
                        topicsCheck2 = true;
                        topic = "Superheros";
                    }
                }
            }
            else if(topic.equals("Space")){
                int x = -1;
                for(String s3 : spaceExtension){
                    x++;
                    if(question.equals(s3)){
                        currentlyRunning = true;
                        topicsCheck2 = true;
                        topic = "Space";
                    }
                }

            }
            else if(topic.equals("Indian Food")){
                int x = -1;
                for(String s3 : foodExtension){
                    x++;
                    if(question.equals(s3)){
                        currentlyRunning = true;
                        topicsCheck2 = true;
                        topic = "Food";
                    }
                }

            }
            if(topicsCheck2 && levelCheck == 2){
                if(topic.equals("Superheros")){
                    int tempTopicVal = superherosExtension2.indexOf(question);
                    if(tempTopicVal>=0){
                        addResponse(superherosResponses2.get(tempTopicVal));
                        levelCheck = 3;
                    }
                    else{
                        addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
                        levelCheck= 3;
                    }

                }
                else if(topic.equals("Space")){
                    int tempTopicVal = spaceExtension2.indexOf(question);
                    if(tempTopicVal>=0)
                    {
                        addResponse(spaceResponses2.get(tempTopicVal));
                        levelCheck = 3;
                    }
                    else{
                        addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
                    }


                }
                else if(topic.equals("Food")){
                    int tempTopicVal = foodExtension2.indexOf(question);
                    if(tempTopicVal >= 0){
                        addResponse(foodResponses2.get(tempTopicVal));
                    }
                    else{
                        addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
                    }

                }
                else if (topic.equals("")){
                    addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
                }
                else if(topic.equals("Superheros") || topic.equals("Space") || topic.equals("Indian Food")){
                    addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
                }
            }
            else if(!topicsCheck2){
                addResponse("I don't have a good response on that topic? Could you try again? If you'd like to switch topics, type 'Switch'.");
            }
        }
    }

}