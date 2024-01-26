package com.example.calorieguard;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.Objects;

public class Login extends AppCompatActivity {
    public Button lg;
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private Button signInButton;
    public EditText email, password;
    public TextView forgotPass, sg;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen splashScreen = androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        progressBar = findViewById(R.id.progressBarana);
        progressBar.setVisibility(View.GONE);

        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);
        String currentUser = sharedPreferences.getString("currentUser", null);

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();


        if (isSignedIn && currentUser != null) {
            String Memail = currentUser;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("Email", Memail);
            startActivity(intent);
            finish();
        } else {
            lg = (Button) findViewById(R.id.buttonlogin);
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

            oneTapClient = Identity.getSignInClient(this);
            signUpRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            // Your server's client ID, not your Android client ID.
                            .setServerClientId(getString(R.string.default_web_client_id))
                            // Show all accounts on the device.
                            .setFilterByAuthorizedAccounts(false)
                            .build())
                    .build();


            ActivityResultLauncher<IntentSenderRequest> activityResultLauncher =
                    registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            try {
                                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
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
                                Log.e("Error",""+e);
                            }
                        }
                    });
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(signInButton, "alpha", 0.5f, 2.0f);
                    alphaAnimator.setDuration(1000); // 1000 milliseconds = 1 second
                    alphaAnimator.setRepeatCount(99);
                    alphaAnimator.start();

                    oneTapClient.beginSignIn(signUpRequest)
                            .addOnSuccessListener(Login.this, new OnSuccessListener<BeginSignInResult>() {
                                @Override
                                public void onSuccess(BeginSignInResult result) {

                                    alphaAnimator.cancel();

                                    IntentSenderRequest intentSenderRequest =
                                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                                    activityResultLauncher.launch(intentSenderRequest);

                                }
                            })
                            .addOnFailureListener(Login.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    alphaAnimator.cancel();
                                    // No Google Accounts found. Just continue presenting the signed-out UI.
                                    Toast.makeText(Login.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("Error",""+e.toString());
                                }
                            });
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
}