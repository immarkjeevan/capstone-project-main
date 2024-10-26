package com.example.teachassist;

import Astatic android.widget.Toast.LENGTH_LONG;
import static com.example.teachassist.ChatActivity.JSON;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class APIrequests {

    OkHttpClient client = new OkHttpClient();
    private String apikey = "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";
    private Context context;
    private String ThreadID;
    private String assistantidStudentMonitoring = "asst_YVzBJWTcX0SLe5Z4iEkNyfnP";
    private String runThreadID;

    // Pass ChatActivity through constructor
    private ChatActivity chatActivity;

    public APIrequests(Context context, ChatActivity chatActivity) {
        this.context = context;
        this.chatActivity = chatActivity;  // Reference to active ChatActivity instance
    }

    public interface ThreadIdCallback {
        void onThreadIdRetrieved(String threadId);
        void onError(String errorMessage);
    }


    public interface VectorStoreCallback {
        void onVectorIdReceived(String vectorid);
        void onError(String errorMessage);  // To handle errors
    }





    // Create the thread if the user is new to the app
    void createThread(String uid) {
        JSONObject jsonbody = new JSONObject();

        RequestBody body = RequestBody.create(jsonbody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apikey)
                .header("OpenAI-Beta", "assistants=v2")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("ThreadCreationError", "Failed to create thread: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse thread creation response
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String threadId = jsonObject.getString("id");

                        // Once the thread is created, create the vector store for it
                        createVectorStore(threadId, new VectorStoreCallback() {
                            @Override
                            public void onVectorIdReceived(String vectorId) {
                                // Now save thread and vectorID to Firebase
                                saveThreadToFirebase(uid, threadId, vectorId);
                                modifythread(threadId,vectorId);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e("VectorStoreError", "Failed to create vector store: " + errorMessage);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSONParsingError", "Failed to parse thread creation response: " + e.getMessage());
                    }
                } else {
                    Log.e("ThreadError", "Thread creation unsuccessful: " + response.message());
                }
            }
        });
    }

    void modifythread(String thread, String vector){
        JSONObject jsonBody = new JSONObject();
        JSONObject toolResources = new JSONObject();
        JSONObject fileSearch = new JSONObject();

        try {
            // Set up file_search object with vector store IDs
            fileSearch.put("vector_store_ids", new JSONArray().put(vector)); // Replace with actual ID

            // Add file_search to tool_resources
            toolResources.put("file_search", fileSearch);

            // Add tool_resources to the main JSON body
            jsonBody.put("tool_resources", toolResources);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the JSON object to a string
        String jsonString = jsonBody.toString();

        // Create the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Define the request body
        RequestBody body = RequestBody.create(
                jsonString,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/"+thread) // Replace with the correct URL
                .header("Authorization", "Bearer "+apikey) // Replace with your API key
                .header("Content-Type", "application/json")
                .header("OpenAI-Beta","assistants=v2")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful response
                    String responseBody = response.body().string();
                    System.out.println("Response: " + responseBody);
                } else {
                    // Handle error response
                    System.out.println("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }
        });


    }




    public void addmessagetothread(String uid, String question) {
        getThreadIdByUid(uid, new ThreadIdCallback() {
            @Override
            public void onThreadIdRetrieved(String threadId) {
                ThreadID = threadId;  // Store threadId
                sendMessageToThread(question);
            }

            @Override
            public void onError(String errorMessage) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    // Method to send the message to the thread
    private void sendMessageToThread(String question) {
        ((Activity) context).runOnUiThread(() ->
                Toast.makeText(context, "This is your Thread: " + ThreadID, Toast.LENGTH_LONG).show());

        JSONObject requestbody = new JSONObject();
        try {
            requestbody.put("role", "user");
            requestbody.put("content", question);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(requestbody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + ThreadID + "/messages")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apikey)
                .header("OpenAI-Beta", "assistants=v2")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to add message: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Message added to Thread: " + ThreadID, Toast.LENGTH_LONG).show());
                    runthread(ThreadID);
                } else {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    // Run the thread
    void runthread(String thread) {
        JSONObject requestbody = new JSONObject();
        try {
            requestbody.put("assistant_id", assistantidStudentMonitoring);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(requestbody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + ThreadID + "/runs")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apikey)
                .header("OpenAI-Beta", "assistants=v2")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to Run thread: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Convert the response body to string
                        String responsebody = response.body().string();

                        // Log the entire response for debugging purposes
                        Log.d("ResponseBody", "Response: " + responsebody);

                        // Parse the response body as a JSONObject
                        JSONObject jsonResponse = new JSONObject(responsebody);

                        String runId = jsonResponse.getString("id");

                            // Log the extracted runId for debugging
                            Log.d("RunID", "Extracted Run ID: " + runId);

                            // Assign the runId to a class-level variable for later use
                            runThreadID = runId;

                            // Ensure that we are on the UI thread when showing the Toast
                            ((Activity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "RunID extracted: " + runId, Toast.LENGTH_LONG).show());

                            // Call your checkrunstatus method with the runId if needed
                            checkrunstatus();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Log the exception for debugging
                        Log.e("JSONParsing", "Error parsing JSON response: " + e.getMessage());
                    }
                } else {
                    // Log the response code and message in case of failure
                    Log.e("HTTPError", "Response not successful: " + response.code() + " " + response.message());
                }
            }
        });
    }

    public void checkrunstatus() {
        while (true) {
            try {
                // 1. Send HTTP request
                String response = sendHttpRequest(client);
                Log.d("ResponseBodyforrunstatus", "Response: " + response);

                // 2. Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response);
                String status = jsonResponse.getJSONArray("data")
                        .getJSONObject(0)
                        .getString("status");

                // 3. Check if the status is "completed"
                if ("completed".equals(status)) {
                    // Call the next function
                    getassistantmessage();
                    break;
                } else {
                    // Wait for 3 seconds before sending another request
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String sendHttpRequest(OkHttpClient client) throws Exception {
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/"+ThreadID+"/runs/"+runThreadID+"/steps")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apikey)
                .header("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {

            String responsebody = response.body().string();

            return responsebody;
        }
    }

    // Get the assistant's message
    void getassistantmessage() {
        ((Activity) context).runOnUiThread(() ->
                Toast.makeText(context, "Getting the assistant message: " + ThreadID, Toast.LENGTH_LONG).show());

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/threads/" + ThreadID + "/messages")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apikey)
                .header("OpenAI-Beta", "assistants=v2")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Failed to get Message List: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String apiresponse = response.body().string();
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Payload Received", Toast.LENGTH_LONG).show());
                    getAssistantMessage(apiresponse);
                } else {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    public void getAssistantMessage(String jsonPayload) {
        String assistantMessage = null;
        try {
            // Parse the JSON payload into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonPayload);

            // Get the "data" array from the JSON object
            JSONArray dataArray = jsonObject.getJSONArray("data");

            // Loop through the "data" array
            for (int i = 0; i < dataArray.length(); i++) {
                // Get each message object
                JSONObject messageObject = dataArray.getJSONObject(i);

                // Check if the "role" is "assistant"
                if (messageObject.getString("role").equals("assistant")) {
                    // Get the "content" array
                    JSONArray contentArray = messageObject.getJSONArray("content");

                    // Extract the "value" of the first content (assuming it's text)
                    JSONObject contentObject = contentArray.getJSONObject(0);
                    assistantMessage = contentObject.getJSONObject("text").getString("value");

                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Call the ChatActivity's addResponse method to add the assistant's message to the chat UI
        if (assistantMessage != null) {
            chatActivity.addResponse(assistantMessage);  // Call ChatActivity to update UI
        }
    }

    // Get the thread of the user
    public void getThreadIdByUid(String uid, ThreadIdCallback callback) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("threads");

        Query query = databaseReference.orderByChild("UID").equalTo(uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot threadSnapshot : dataSnapshot.getChildren()) {
                        String threadId = threadSnapshot.child("threadID").getValue(String.class);
                        callback.onThreadIdRetrieved(threadId);
                        return;
                    }
                } else {
                    callback.onError("No threads found for this user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Failed to read data: " + databaseError.getMessage());
            }
        });
    }

    public void createVectorStore(String threadId, VectorStoreCallback callback) {
        // Construct the JSON request body
        String json = "{\"name\": \"" + "Store for: " + threadId + "\"}";

        // Create request body for OkHttp
        RequestBody body = RequestBody.create(json, JSON);

        // Build the API request
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/vector_stores")
                .header("Authorization", "Bearer " + apikey)
                .header("Content-Type", "application/json")
                .header("OpenAI-Beta", "assistants=v2")
                .post(body)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                callback.onError("Request failed: " + e.getMessage());  // Handle failure via callback
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        // Parse the response body as a JSONObject
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        // Extract the vector ID from the JSON response
                        String vectorId = jsonObject.getString("id");

                        // Pass the vector ID back via the callback
                        callback.onVectorIdReceived(vectorId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onError("Failed to parse JSON: " + e.getMessage());
                    }
                } else {
                    callback.onError("Unsuccessful response: " + response.message());
                }
            }
        });
    }

    public void saveThreadToFirebase(String uid, String threadId, String vectorId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("threads");

        // Create a HashMap to store thread details
        HashMap<String, Object> threadData = new HashMap<>();
        threadData.put("UID", uid);
        threadData.put("threadID", threadId);
        threadData.put("vectorID", vectorId);  // Store the retrieved vector ID

        databaseReference.push().setValue(threadData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Thread and vector store saved successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseError", "Failed to save thread: " + e.getMessage());
                });
    }






}
