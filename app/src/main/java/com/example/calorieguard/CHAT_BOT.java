package com.example.calorieguard;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class CHAT_BOT extends AppCompatActivity {
    @Override
    protected void onUserLeaveHint() {
        footer.Stop();
        super.onUserLeaveHint();
    }
    @Override
    public void onBackPressed() {
        footer.Stop();
        super.onBackPressed();
    }

    EditText message;
    ImageView imageView;
    Footer footer;
    TextView qus,answer,t1,t2,t3;
    ImageButton send;
    private ObjectAnimator fadeAnimator;
    private Handler mainHandler;
    private static String geminiapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_bot);

        message=(EditText) findViewById(R.id.messageEDT);
        send=(ImageButton) findViewById(R.id.send);
        qus=(TextView)findViewById(R.id.question);
        answer=(TextView)findViewById(R.id.answer);
        imageView=(ImageView)findViewById(R.id.AICHAT);
        t1=(TextView)findViewById(R.id.textView17);
        t2=(TextView)findViewById(R.id.textView18);
        t3=(TextView)findViewById(R.id.textView19);

        mainHandler = new Handler(Looper.getMainLooper());
        View rootView = findViewById(android.R.id.content);
        imageView.setBackgroundResource(R.drawable.aichat);


        String email = getIntent().getExtras().getString("Email");
        Objects.requireNonNull(getSupportActionBar()).hide();

        footer = new Footer(this, rootView, email);
        footer.setChatBackground(R.drawable.circlebg);
        qus.setVisibility(View.GONE);
        t1.setVisibility(View.GONE);
        t2.setVisibility(View.GONE);
        t3.setVisibility(View.GONE);
        answer.setText("Typing ...");
        showAfterdelay(getApplicationContext(),500);


        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qus.setVisibility(View.VISIBLE);
                qus.setText(t1.getText().toString());
                answer.setVisibility(View.VISIBLE);
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                t3.setVisibility(View.GONE);
                answer.setText("Typing ...");
                imageView.setBackgroundResource(R.drawable.ai_chat_active);
                startFadeAnimation();
                CallAPI(t1.getText().toString());
            }
        });
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qus.setVisibility(View.VISIBLE);
                qus.setText(t2.getText().toString());
                answer.setVisibility(View.VISIBLE);
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                t3.setVisibility(View.GONE);
                answer.setText("Typing ...");
                imageView.setBackgroundResource(R.drawable.ai_chat_active);
                startFadeAnimation();
                CallAPI(t2.getText().toString());
            }
        });
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qus.setVisibility(View.VISIBLE);
                qus.setText(t3.getText().toString());
                answer.setVisibility(View.VISIBLE);
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                t3.setVisibility(View.GONE);
                answer.setText("Typing ...");
                imageView.setBackgroundResource(R.drawable.ai_chat_active);
                startFadeAnimation();
                CallAPI(t3.getText().toString());
            }
        });

        message.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);
                t3.setVisibility(View.GONE);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(message.getText().toString().isEmpty()){
                    shakeView(message);
                    vibrateDevice(CHAT_BOT.this);
               }
                else
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String question=message.getText().toString().trim();
                    qus.setVisibility(View.VISIBLE);
                    qus.setText(question);
                    answer.setVisibility(View.VISIBLE);
                    t1.setVisibility(View.GONE);
                    t2.setVisibility(View.GONE);
                    t3.setVisibility(View.GONE);
                    answer.setText("Typing ...");
                    message.setText("");
                    message.clearFocus();
                    imageView.setBackgroundResource(R.drawable.ai_chat_active);
                    startFadeAnimation();
                    CallAPI(question.trim());
                }
            }
        });
    }
    void CallAPI(String Question)
    {
        SharedPreferences preferences = getSharedPreferences("MyPrefKeys", Context.MODE_PRIVATE);
        geminiapi = preferences.getString("api_key", null);

        if(geminiapi==null)
        {
            FirebaseAPIFetcher fetcher = new FirebaseAPIFetcher();
            fetcher.getAPIData().observe(this, apiData -> {
                geminiapi = apiData.geminiAPI;

                SharedPreferences.Editor editor = preferences.edit();
                String encryptedApiKey=encryptString(geminiapi,getString(R.string.XOR_Key));
                editor.putString("api_key", encryptedApiKey);
                editor.apply();

                GenerativeModel gm = new GenerativeModel("gemini-pro",geminiapi);
                GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                Content content = new Content.Builder()
                        .addText(Question.trim())
                        .build();

                Executor executor = Executors.newFixedThreadPool(1);

                ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        addResponse(resultText);
                        // Use the mainHandler to update the UI on the main thread
                        mainHandler.post(() -> {
                            stopFadeAnimation();
                            imageView.setBackgroundResource(R.drawable.aichat);
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        // Use the mainHandler to update the UI on the main thread
                        mainHandler.post(() -> {
                            addResponse("An Error occurred. Please try again !");
                            stopFadeAnimation();
                            imageView.setBackgroundResource(R.drawable.aichat);
                        });
                        Log.d("Error",t.toString());
                        t.printStackTrace();
                    }
                }, executor);
            });
        }
        else{
            geminiapi=encryptString(geminiapi,getString(R.string.XOR_Key));

            GenerativeModel gm = new GenerativeModel("gemini-pro",geminiapi);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Content content = new Content.Builder()
                    .addText(Question.trim())
                    .build();

            Executor executor = Executors.newFixedThreadPool(1);

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    addResponse(resultText);
                    // Use the mainHandler to update the UI on the main thread
                    mainHandler.post(() -> {
                        stopFadeAnimation();
                        imageView.setBackgroundResource(R.drawable.aichat);
                    });
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    // Use the mainHandler to update the UI on the main thread
                    mainHandler.post(() -> {
                        addResponse("An Error occurred. Please try again !");
                        stopFadeAnimation();
                        imageView.setBackgroundResource(R.drawable.aichat);
                    });
                    Log.d("Error",t.toString());
                    t.printStackTrace();
                }
            }, executor);
        }
    }
    void addResponse(String res)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                answer.setText(res);
            }
        });
    }
    public void showAfterdelay(Context context,int milliseconds)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                answer.setText("Hi there,\nIt's JIM, your personal AI assistant.\nHow may I help you?\n\nNot sure where to start?\nYou can try:");
                t1.setVisibility(View.VISIBLE);
                t2.setVisibility(View.VISIBLE);
                t3.setVisibility(View.VISIBLE);
            }
        },milliseconds);
    }

    private void startFadeAnimation() {
        // Create an ObjectAnimator for alpha property
        fadeAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0.7f);
        fadeAnimator.setDuration(500); // 0.5 second
        fadeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        fadeAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        fadeAnimator.start();
    }

    private void stopFadeAnimation() {
        if (fadeAnimator != null) {
            mainHandler.post(() -> {
                imageView.setAlpha(1f);
                fadeAnimator.cancel();
            });
        }
    }
    private void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null) {
            // Check if the device supports vibration effects
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                // For older devices without VibrationEffect
                vibrator.vibrate(200);
            }
        }
    }

    // Helper method to apply a shake animation to a view
    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_animation);
        view.startAnimation(shake);
    }

    private static String encryptString(String input,String XOR_KEY) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            encrypted.append((char) (input.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
        }
        return encrypted.toString();
    }
}
