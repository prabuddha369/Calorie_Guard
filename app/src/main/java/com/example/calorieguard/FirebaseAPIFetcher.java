package com.example.calorieguard;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseAPIFetcher {

    private static final String TAG = "FirestoreAPIFetcher";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference apiDocRef = db.collection("API").document("APIData"); // Assuming document name is "APIData"

    private MutableLiveData<APIData> mAPIData = new MutableLiveData<>();

    public LiveData<APIData> getAPIData() {
        fetchAPIData();
        return mAPIData;
    }

    private void fetchAPIData() {
        apiDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String webClientId = documentSnapshot.getString("WebClientID");
                            String geminiAPI = documentSnapshot.getString("GeminiAPI");
                            mAPIData.setValue(new APIData(webClientId, geminiAPI));
                        } else {
                            Log.w(TAG, "No API data found in Firestore");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching API data: ", e);
                    }
                });
    }

    public class APIData {
        public String webClientId;
        public String geminiAPI;

        public APIData(String webClientId, String geminiAPI) {
            this.webClientId = webClientId;
            this.geminiAPI = geminiAPI;
        }
    }
}
