package com.example.calorieguard;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.Objects;

public class Login extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private Button signInButton;
    public EditText email, password;
    public TextView forgotPass, sg,lg;
    private static String webclientid,geminiapi;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private boolean flg=false;
    private static final int REQ_ONE_TAP = 2;
    private boolean showOneTapUI = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen splashScreen = androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        FirebaseAPIFetcher fetcher = new FirebaseAPIFetcher();
        fetcher.getAPIData().observe(this, apiData -> {
            // Use the fetched API data here
            geminiapi = apiData.geminiAPI;
            SharedPreferences preferences = getSharedPreferences("MyPrefKeys", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            String encryptedApiKey=encryptString(geminiapi,getString(R.string.XOR_Key));
            editor.putString("api_key", encryptedApiKey);
            editor.apply();

            webclientid = apiData.webClientId;

            oneTapClient = Identity.getSignInClient(this);
            signUpRequest = BeginSignInRequest.builder()
                    .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                            .setSupported(true)
                            .build())
                    .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            // Your server's client ID, not your Android client ID.
                            .setServerClientId(webclientid)
                            // Only show accounts previously used to sign in.
                            .setFilterByAuthorizedAccounts(false)
                            .build())
                    // Automatically sign in when exactly one credential is retrieved.
                    .setAutoSelectEnabled(true)
                    .build();
            flg=true;
        });

        handler = new Handler();

        Toast t1 = Toast.makeText(Login.this, "Google accounts are loading...", Toast.LENGTH_SHORT);

        progressBar = findViewById(R.id.progressBarana);
        progressBar.setVisibility(View.GONE);

        Objects.requireNonNull(getSupportActionBar()).hide();


        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);
        String currentUser = sharedPreferences.getString("currentUser", null);


        if (isSignedIn && currentUser != null) {
            String Memail = currentUser;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("Email", Memail);
            startActivity(intent);
            finish();
        } else {
            lg = findViewById(R.id.buttonlogin);
            sg = (TextView) findViewById(R.id.buttonsignup);
            email = (EditText) findViewById(R.id.editTextTextEmailAddress);
            forgotPass = (TextView) findViewById(R.id.forgotPass);
            password = (EditText) findViewById(R.id.editTextTextPassword);
//            myAuth = FirebaseAuth.getInstance();
            signInButton=(Button) findViewById(R.id.googleSignInButton);

            forgotPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //coming soon
                }
            });


            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if(showOneTapUI && flg) {
                        showOneTapUI=false;
                        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(signInButton, "alpha", 0.5f, 2.0f);
                        alphaAnimator.setDuration(1000); // 1000 milliseconds = 1 second
                        alphaAnimator.setRepeatCount(99);
                        alphaAnimator.start();

                        oneTapClient.beginSignIn(signUpRequest)
                                .addOnSuccessListener(Login.this, new OnSuccessListener<BeginSignInResult>() {
                                    @Override
                                    public void onSuccess(BeginSignInResult result) {

                                        alphaAnimator.cancel();
                                        signInButton.setAlpha(1);

                                        try {
                                            startIntentSenderForResult(
                                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                                    null, 0, 0, 0);
                                        } catch (IntentSender.SendIntentException e) {
                                            Toast.makeText(Login.this, "Couldn't start One Tap UI: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("One Tap UI Error", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                                        }

                                    }
                                })
                                .addOnFailureListener(Login.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        alphaAnimator.cancel();
                                        signInButton.setAlpha(1);
                                        // No Google Accounts found. Just continue presenting the signed-out UI.
                                        Toast.makeText(Login.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                                        Log.d("Error",""+e.toString());
                                    }
                                });
                        // Post a delayed task to reset 'touched' after 10 seconds
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showOneTapUI = true;
                            }
                        }, 120000);
                    } else if (!showOneTapUI) {
                      t1.cancel();
                      t1.show();
                    }
                }
            });

            sg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Signup.class);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }
            });

            lg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Email = email.getText().toString();
                    String pass = password.getText().toString();
                    if (!Email.isEmpty() && !pass.isEmpty()) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (SecurePreferencesManager.checkCredentials(Login.this, Email, pass)) {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("Email", Email);
                            startActivity(intent);
                            finish();
                            email.setText("");
                            password.setText("");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isSignedIn", true);
                            editor.putString("currentUser",Email);
                            editor.apply();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Try Again!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        vibrateDevice(Login.this);
                        if(Email.isEmpty())
                        {
                            shakeView(email);
                        }
                        if(pass.isEmpty())
                        {
                            shakeView(password);
                        }
                    }
                }
            });
        }
    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_animation);
        view.startAnimation(shake);
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

    private static String encryptString(String input,String XOR_KEY) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            encrypted.append((char) (input.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
        }
        return encrypted.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                DBHelper dbHelper = new DBHelper(this);
                dbHelper.close();

                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getId();

                if (dbHelper.doesUserExist(idToken)) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Email", idToken);
                    startActivity(intent);
                    finish();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isSignedIn", true);
                    editor.putString("currentUser", idToken);
                    editor.apply();
                } else {
                    Intent intent = new Intent(getApplicationContext(), UserData.class);
                    intent.putExtra("Email", idToken);
                    intent.putExtra("Name", credential.getDisplayName());
                    intent.putExtra("DpUrl", Objects.requireNonNull(credential.getProfilePictureUri()).toString());
                    startActivity(intent);
                    finish();
                }
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.CANCELED:
                        Toast.makeText(this, "Google SignIn Temporarily Closed for 2 minutes", Toast.LENGTH_LONG).show();
                        Log.d("One Tap Diaglog Closed", "One-tap dialog was closed.");
                        showOneTapUI = false;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showOneTapUI = true;
                            }
                        }, 120000);
                        break;
                    case CommonStatusCodes.NETWORK_ERROR:
                        Toast.makeText(this, "One-tap encountered a network error", Toast.LENGTH_SHORT).show();
                        Log.d("Internet Error", "One-tap encountered a network error.");
                        break;
                    default:
                        Toast.makeText(this, "Couldn't get credential from result." + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("No Credentials", "Couldn't get credential from result." + e.getLocalizedMessage());
                        break;
                }
            }
        }
    }
}