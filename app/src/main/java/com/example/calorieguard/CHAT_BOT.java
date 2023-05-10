package com.example.calorieguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CHAT_BOT extends AppCompatActivity {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    EditText message;
    TextView qus,answer,back;
    ImageButton send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_bot);

        message=(EditText) findViewById(R.id.messageEDT);
        send=(ImageButton) findViewById(R.id.send);
        qus=(TextView)findViewById(R.id.question);
        answer=(TextView)findViewById(R.id.answer);
        back=(TextView)findViewById(R.id.backtomain);

        qus.setVisibility(View.GONE);
        answer.setText("Typing ...");
        showAfterdelay(getApplicationContext(),1500);

        Objects.requireNonNull(getSupportActionBar()).hide();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!message.getText().toString().isEmpty()){
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String question=message.getText().toString().trim();
                qus.setVisibility(View.VISIBLE);
                qus.setText(question);
                answer.setVisibility(View.VISIBLE);
                answer.setText("Typing ...");
                message.setText("");
                CallAPI(question.trim());}
                else
                {
                    Toast.makeText(CHAT_BOT.this, "Write Something ...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void CallAPI(String Question)
    {
        //okhttp
        JSONObject jsonBody=new JSONObject();
        try {
            jsonBody.put("model","text-davinci-003");
            jsonBody.put("prompt",Question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        RequestBody body=RequestBody.create(jsonBody.toString(),JSON);
        Request request=new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer ")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load message due to "+e.getMessage());
                Log.e("ERROR",""+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful())
                {
                    JSONObject jsonObject=null;
                    try{
                        jsonObject=new JSONObject(response.body().string());
                        JSONArray jsonArray=jsonObject.getJSONArray("choices");
                        String result=jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());

                    }catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    addResponse("Failed to load message due to "+ Objects.requireNonNull(response.body()).toString());
                    Log.e("ERROR", ""+Objects.requireNonNull(response.body()).toString());
                }
            }
        });
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
                answer.setText("Hi there,\nIt's JIM, your personal AI assistant.\nHow can I help you?");
            }
        },milliseconds);
    }
}
