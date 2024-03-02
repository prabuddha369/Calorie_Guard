package com.example.calorieguard;

import android.Manifest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Lens extends AppCompatActivity {
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
    private SurfaceView surfaceView;
    private ImageView clicked,clicked_long;
    private TextView snap,feature,nofood;
    private Button addit, retake;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private List<DataModel> dataList = new ArrayList<>();
    private Footer footer;
    private ProgressBar pg;
    private boolean isProcessingPicture = false;
    private CameraSource cameraSource;
    public String resultText;
    public JSONArray jsonArray = null;
    private String[] texts = {
            "Scanning...",
            "Recognizing foods...",
            "Identifying patterns...",
            "Refining weight detection algorithms...",
            "Optimizing algorithms...",
            "Scanning database...",
            "Analyzing results...",
            "Processing information...",
            "Calculating probabilities...",
            "Verifying data integrity...",
            "Cross-referencing information...",
            "Detecting anomalies...",
            "Generating reports...",
            "Enhancing accuracy...",
            "Fine-tuning parameters...",
            "Validating results...",
            "Refining predictions..."
    };

    private int currentIndex = 0;
    private Handler handler;
    private static final int Per = 100;
    int i = 2;
    private static String geminiapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lens);
        View rootView = findViewById(android.R.id.content);

        Objects.requireNonNull(getSupportActionBar()).hide();

        surfaceView = findViewById(R.id.surfaceView);
        OverlayDrawable overlayDrawable = new OverlayDrawable(this); // Adjust the corner size as needed
        surfaceView.setBackground(overlayDrawable);

        snap = findViewById(R.id.snap);
        retake = (Button) findViewById(R.id.retake);
        addit = (Button) findViewById(R.id.addIt);
        clicked = (ImageView) findViewById(R.id.clicked);
        clicked_long=findViewById(R.id.clicked_long);
        feature=findViewById(R.id.feature);
        nofood=findViewById(R.id.nofood);
        recyclerView = findViewById(R.id.recyclerViewScan);
        pg=findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter(dataList);
        recyclerView.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());

        startCameraSource();

        addit.setVisibility(View.GONE);
        retake.setVisibility(View.GONE);

        String email = getIntent().getExtras().getString("Email");

        footer = new Footer(this, rootView, email);
        footer.setLensBackground(R.drawable.circlebg);
        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    takeSnapFromSurfaceView();

                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProcessingPicture = false;
                dataList.clear();
                feature.setVisibility(View.GONE);
                surfaceView.setVisibility(View.VISIBLE);
                clicked.setVisibility(View.GONE);
                retake.setVisibility(View.GONE);
                addit.setVisibility(View.GONE);
                nofood.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                snap.setVisibility(View.VISIBLE);
            }
        });

        addit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    jsonArray = new JSONArray(resultText);

                    // Create an array to hold item data
                    String[][] itemData = new String[jsonArray.length()][3]; // 3 fields: Name, Quantity, Calorie

                    for (int i = 0; i < jsonArray.length(); i++) {
                        DataModel data = dataList.get(i);
                        String name = data.getNameScan();
                        String weight = data.getWeightScan();
                        String calorie = data.getCalorieScan();

                        // Populate itemData array
                        itemData[i][0] = name;
                        itemData[i][1] = weight;
                        itemData[i][2] = calorie;
                    }

                    showCustomAlertDialog(itemData, email);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    private void startCameraSource() {
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Toast.makeText(this, "Text recognizer is not operational.", Toast.LENGTH_SHORT).show();
            return;
        }

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
                // Handle any cleanup here if needed
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                // Optional: Handle the detected text blocks if needed
                // This is where you receive detection results if you plan to process them in real-time
            }
        });

        Point screenPoint = getScreenDimensions();

        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(screenPoint.y, screenPoint.x)
                .setRequestedFps(2.0f)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Lens.this, new String[]{Manifest.permission.CAMERA}, Per);
                    }

                    cameraSource.start(holder);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    // Helper method to get the screen dimensions
    private Point getScreenDimensions() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    private void takeSnapFromSurfaceView() throws CameraAccessException {
        SharedPreferences preferences = getSharedPreferences("MyPrefKeys", Context.MODE_PRIVATE);
        geminiapi = preferences.getString("api_key", null);

        if (isProcessingPicture) {
            return; // Ignore multiple clicks while processing a picture
        }

        isProcessingPicture = true;

        if (cameraSource != null) {
            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {

                    feature.setVisibility(View.VISIBLE);
                    // Start updating text every 1 second
                    startTextUpdate();

                    // Convert the byte array to a Bitmap
                    Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
// Check if rotation is needed
                    boolean rotationNeeded = false;
                    if (originalBitmap.getWidth() > originalBitmap.getHeight()) {
                        rotationNeeded = true;
                    }
// Rotate the original bitmap by 90 degrees if needed
                    Matrix matrix = new Matrix();
                    if (rotationNeeded) {
                        matrix.postRotate(90);
                    }

                    Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);


                    // Crop the rotated bitmap into a square shape
                    int size = Math.min(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
                    int x = (rotatedBitmap.getWidth() - size) / 2;
                    int y = (rotatedBitmap.getHeight() - size) / 2;
                    Bitmap squareBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size);

                    // Display the square image in the clicked ImageView
                    clicked_long.setImageBitmap(rotatedBitmap);
                    clicked_long.setVisibility(View.VISIBLE);
                    clicked_long.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    // Hide the SurfaceView since we have a captured image to display
                    surfaceView.setVisibility(View.GONE);

                    snap.setVisibility(View.GONE);
                    pg.setVisibility(View.VISIBLE);

                    if(geminiapi==null)
                    {
                        FirebaseAPIFetcher fetcher = new FirebaseAPIFetcher();
                        fetcher.getAPIData().observe(Lens.this, apiData -> {
                            geminiapi = apiData.geminiAPI;

                            SharedPreferences preferences = getSharedPreferences("MyPrefKeys", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            String encryptedApiKey=encryptString(geminiapi,getString(R.string.XOR_Key));
                            editor.putString("api_key", encryptedApiKey);
                            editor.apply();

                            GenerativeModel gm = new GenerativeModel("gemini-pro-vision",geminiapi);
                            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                            Content content = new Content.Builder()
                                    .addText(getString(R.string.Special_Promt))
                                    .addImage(squareBitmap)
                                    .build();

                            Executor executor = Executors.newFixedThreadPool(1);

                            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                                @Override
                                public void onSuccess(GenerateContentResponse result) {
                                    resultText = result.getText();
                                    Log.d("Error",resultText);
                                    try {
                                        StringBuilder macros= new StringBuilder();
                                        jsonArray = new JSONArray(resultText);
                                        // Iterate over the JSONArray and process each JSONObject
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            try {
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                                // Access individual values
                                                String name = jsonObject.optString("Name");
                                                String quantity = jsonObject.optString("Quantity");
                                                int calorie = jsonObject.optInt("Calorie");
                                                String protein = jsonObject.optString("Protein");
                                                String carbohydrate = jsonObject.optString("Carbohydrate");
                                                String sugar = jsonObject.optString("Sugar");
                                                String fat = jsonObject.optString("Fat");
                                                String glycemicIndex = jsonObject.optString("Glycemic Index");

                                                macros.append("Protein: ").append(protein).append("\nCarbohydrate: ").append(carbohydrate).append("\nSugar: ").append(sugar).append("\nFat: ").append(fat).append("\nGlycemic Index: ").append(glycemicIndex);

                                                Log.d("Macros",macros.toString());
                                                dataList.add(new DataModel(Integer.toString(calorie), quantity,macros.toString(),name));
                                                macros.setLength(0);
                                            } catch (JSONException e) {
                                                break;
                                            }
                                        }

                                        Lens.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                clicked.setVisibility(View.VISIBLE);
                                                clicked.setImageBitmap(squareBitmap);
                                                clicked.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                                clicked_long.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                                adapter = new RecyclerViewAdapter(dataList);
                                                recyclerView.setAdapter(adapter);
                                                pg.setVisibility(View.GONE);
                                                retake.setVisibility(View.VISIBLE);
                                                addit.setVisibility(View.VISIBLE);
                                                stopTextUpdate();
                                                feature.setVisibility(View.GONE);
                                            }
                                        });

                                    } catch (JSONException e) {
                                        Lens.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                clicked.setVisibility(View.VISIBLE);
                                                clicked.setImageBitmap(squareBitmap);
                                                clicked.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                clicked_long.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.GONE);
                                                nofood.setVisibility(View.VISIBLE);
                                                pg.setVisibility(View.GONE);
                                                retake.setVisibility(View.VISIBLE);
                                                addit.setVisibility(View.GONE);
                                                stopTextUpdate();
                                                feature.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Throwable t) {
                                    t.printStackTrace();
                                }
                            }, executor);
                        });
                    }
                    else {
                        geminiapi=encryptString(geminiapi,getString(R.string.XOR_Key));

                        GenerativeModel gm = new GenerativeModel("gemini-pro-vision",geminiapi);
                        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                        Content content = new Content.Builder()
                                .addText(getString(R.string.Special_Promt))
                                .addImage(squareBitmap)
                                .build();

                        Executor executor = Executors.newFixedThreadPool(1);

                        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                            @Override
                            public void onSuccess(GenerateContentResponse result) {
                                resultText = result.getText();
                                Log.d("Error",resultText);
                                try {
                                    StringBuilder macros= new StringBuilder();
                                    jsonArray = new JSONArray(resultText);
                                    // Iterate over the JSONArray and process each JSONObject
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                                            // Access individual values
                                            String name = jsonObject.optString("Name");
                                            String quantity = jsonObject.optString("Quantity");
                                            int calorie = jsonObject.optInt("Calorie");
                                            String protein = jsonObject.optString("Protein");
                                            String carbohydrate = jsonObject.optString("Carbohydrate");
                                            String sugar = jsonObject.optString("Sugar");
                                            String fat = jsonObject.optString("Fat");
                                            String glycemicIndex = jsonObject.optString("Glycemic Index");

                                            macros.append("Protein: ").append(protein).append("\nCarbohydrate: ").append(carbohydrate).append("\nSugar: ").append(sugar).append("\nFat: ").append(fat).append("\nGlycemic Index: ").append(glycemicIndex);

                                            Log.d("Macros",macros.toString());
                                            dataList.add(new DataModel(Integer.toString(calorie),quantity,macros.toString(),name));
                                            macros.setLength(0);
                                        } catch (JSONException e) {
                                            break;
                                        }
                                    }

                                    Lens.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            clicked.setVisibility(View.VISIBLE);
                                            clicked.setImageBitmap(squareBitmap);
                                            clicked.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                            clicked_long.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                            adapter = new RecyclerViewAdapter(dataList);
                                            recyclerView.setAdapter(adapter);
                                            pg.setVisibility(View.GONE);
                                            retake.setVisibility(View.VISIBLE);
                                            addit.setVisibility(View.VISIBLE);
                                            stopTextUpdate();
                                            feature.setVisibility(View.GONE);
                                        }
                                    });

                                } catch (JSONException e) {
                                    Lens.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            clicked.setVisibility(View.VISIBLE);
                                            clicked.setImageBitmap(squareBitmap);
                                            clicked.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            clicked_long.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.GONE);
                                            nofood.setVisibility(View.VISIBLE);
                                            pg.setVisibility(View.GONE);
                                            retake.setVisibility(View.VISIBLE);
                                            addit.setVisibility(View.GONE);
                                            stopTextUpdate();
                                            feature.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Throwable t) {
                                t.printStackTrace();
                            }
                        }, executor);
                    }

                    isProcessingPicture = false;
                }
            });
        } else {
            Toast.makeText(this, "CameraSource not available", Toast.LENGTH_SHORT).show();
            isProcessingPicture = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Per) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } else {
                Toast.makeText(this, "Requires Camera Permission to work", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCustomAlertDialog(String[][] itemsData, String commonEmail) {
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        // You may need to modify this part based on how you want to display information for multiple items
        titleTextView.setText("For which meal you are having these items?");

        Button option1Button = dialogView.findViewById(R.id.option1Button);
        Button option2Button = dialogView.findViewById(R.id.option2Button);
        Button option3Button = dialogView.findViewById(R.id.option3Button);
        Button option4Button = dialogView.findViewById(R.id.option4Button);

        final AlertDialog alertDialog = builder.setView(dialogView).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.custom_alert_bg);
                }
            }
        });

        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOptionClick("B", itemsData, commonEmail);
                alertDialog.dismiss();
            }
        });

        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOptionClick("L", itemsData, commonEmail);
                alertDialog.dismiss();
            }
        });

        option3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOptionClick("D", itemsData, commonEmail);
                alertDialog.dismiss();
            }
        });

        option4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOptionClick("S", itemsData, commonEmail);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void handleOptionClick(String type, String[][] itemsData, String commonEmail) {
        Intent intent = new Intent(getApplicationContext(), BucketList.class);
        DBHelper dbHelper = new DBHelper(this);

        for (String[] itemData : itemsData) {
            String email = commonEmail;
            String f = itemData[0];
            String wt = itemData[1];
            String cals = itemData[2];

            if (dbHelper.doesItemExist(email, type + " " + f + " " + wt)) {
                dbHelper.insertItemData(email, type + " " + f + " " + wt + "(" + Integer.toString(i++) + ")", cals+" cal");
            } else {
                dbHelper.insertItemData(email, type + " " + f + " " + wt, cals+" cal");
            }
        }

        // You may need to modify this part based on how you want to handle multiple items
        intent.putExtra("Email", commonEmail);
        startActivity(intent);
    }


    private void startTextUpdate() {
        // Create a Runnable to update the text
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Update text
                feature.setText(texts[currentIndex]);

                // Move to the next text (looping back to the start if necessary)
                currentIndex = (currentIndex + 1) % texts.length;

                // Call the same runnable after 1 second
                handler.postDelayed(this, 3000);
            }
        };

        // Call the runnable for the first time
        handler.post(runnable);
    }

    private void stopTextUpdate() {
        handler.removeCallbacksAndMessages(null); // Remove all callbacks and messages
    }

    private static String encryptString(String input,String XOR_KEY) {
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            encrypted.append((char) (input.charAt(i) ^ XOR_KEY.charAt(i % XOR_KEY.length())));
        }
        return encrypted.toString();
    }

    public static String calculateUpdatedNutrition(String originalNutritionInfo, String weight) {
        double factor = Double.parseDouble(weight) / 100.0;

        StringBuilder updatedNutritionInfo = new StringBuilder();

        String[] lines = originalNutritionInfo.split("\n");

        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String nutrient = parts[0].trim();
                if(nutrient.trim().equals("Glycemic Index")){
                    int originalValue = Integer.parseInt(parts[1].replaceAll("g", "").trim());

                    updatedNutritionInfo.append(nutrient)
                            .append(": ")
                            .append(Integer.toString(originalValue))
                            .append(" g\n");
                }
                else {
                    double originalValue = Double.parseDouble(parts[1].replaceAll("g", "").trim());
                    double updatedValue = originalValue * factor;

                    updatedNutritionInfo.append(nutrient)
                            .append(": ")
                            .append(String.format("%.1f", updatedValue))
                            .append(" g\n");
                }
            }
        }

        return updatedNutritionInfo.toString().trim();
    }

    public static double extractDensityValue(String densityString) {
        // Assume the density string is in the format "0.5 g/cm^3"
        String[] parts = densityString.split(" "); // Split by space
        try {
            return Double.parseDouble(parts[0]); // Parse the first part as a double
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle the case where parsing fails or the array index is out of bounds
            e.printStackTrace(); // Log the exception
            return 0.0; // Return a default value
        }
    }

    // Method to extract volume value from a given string
    public static double extractVolumeValue(String volumeString) {
        // Assume the volume string is in the format "200 cm^3"
        String[] parts = volumeString.split(" "); // Split by space
        try {
            return Double.parseDouble(parts[0]); // Parse the first part as a double
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Handle the case where parsing fails or the array index is out of bounds
            e.printStackTrace(); // Log the exception
            return 0.0; // Return a default value
        }
    }
}