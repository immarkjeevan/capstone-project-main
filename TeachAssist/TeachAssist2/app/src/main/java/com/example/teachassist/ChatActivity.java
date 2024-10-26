package com.example.teachassist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 1;
    private String threadID;
    private String apikey = "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";
    RecyclerView recyclerview;
    EditText messageeditText;
    ImageButton sendbutton, sendfilebutton, backbutton;

    List<ChatMessage> messageList;
    ChatAdapter chatAdapter;
    APIrequests apIrequests;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase mDatabase;
    DatabaseReference mUserRef;


    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        messageList = new ArrayList<>();
        recyclerview = findViewById(R.id.recyclerView);
        messageeditText = findViewById(R.id.messageEditText);
        sendbutton = findViewById(R.id.sendButton);
        sendfilebutton = findViewById(R.id.sendfileButton);
        backbutton = findViewById(R.id.backButton);
        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference("threads");

        chatAdapter = new ChatAdapter(messageList);
        apIrequests = new APIrequests(this, this);
        recyclerview.setAdapter(chatAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerview.setLayoutManager(llm);

        // Go back to home screen
        backbutton.setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Send message on click
        sendbutton.setOnClickListener(view -> {
            String question = messageeditText.getText().toString().trim();
            addToChat(question, ChatMessage.SENT_BY_ME);
            messageeditText.setText("");
            apIrequests.addmessagetothread(mAuth.getCurrentUser().getUid(), question);
        });

        // Send file on click
        sendfilebutton.setOnClickListener(view -> {
            // Open file picker using Storage Access Framework
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_SELECT_CODE);
        });
    }

    // Adding message to the chat layout
    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new ChatMessage(message, sentBy));
            chatAdapter.notifyDataSetChanged();
            recyclerview.smoothScrollToPosition(chatAdapter.getItemCount());
        });
    }

    // Adding a response from the bot
    void addResponse(String response) {
        addToChat(response, ChatMessage.SENT_BY_BOT);
    }

    // Handle file selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    try {
                        // Open an InputStream to read the file
                        InputStream inputStream = getContentResolver().openInputStream(fileUri);
                        if (inputStream != null) {
                            // Read the InputStream into a byte array
                            byte[] fileData = readBytes(inputStream);
                            // Proceed to upload the file data
                            uploadFile(fileData, fileUri);
                        } else {
                            addResponse("Unable to open file.");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        addResponse("File not found: " + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        addResponse("Error reading file: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Helper method to convert InputStream to byte array
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    // Upload file using OkHttp
    private void uploadFile(byte[] fileData, Uri fileUri) {
        // Determine the MIME type from Uri
        String mimeType = getContentResolver().getType(fileUri);
        if (mimeType == null) {
            mimeType = "application/octet-stream";  // Default MIME type
        }

        // Extract the file name from the Uri
        String fileName = getFileName(fileUri);

        // Log the MIME type and file name to debug
        Log.d("ChatActivity", "Uploading file with MIME type: " + mimeType + ", file name: " + fileName);

        // Create a RequestBody for the file content (byte array)
        RequestBody fileBody = RequestBody.create(fileData, MediaType.parse(mimeType));  // Use actual MIME type

        // Create MultipartBody for the request
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("purpose", "assistants")  // Purpose field
                .addFormDataPart("file", fileName, fileBody)  // File field with the actual file name
                .build();

        // Build the request with the Authorization header
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/files")  // OpenAI files API endpoint
                .header("Authorization", "Bearer " + apikey)
                .post(requestBody)
                .build();

        // Execute the request asynchronously and log errors
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("ChatActivity", "API request failed: " + e.getMessage());
                addResponse("Failed to upload the file: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ChatActivity", "File uploaded successfully: " + responseBody);
                    addResponse("File uploaded successfully! Response: " + responseBody);

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String id = jsonObject.getString("id");
                        Log.d("ChatActivity", "The extracted ID is: " + id);
                        addResponse(id);
                        getvectorid(id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        addResponse("Error parsing response: " + e.getMessage());
                    }
                } else {
                    String errorBody = response.body().string();
                    Log.e("ChatActivity", "Failed to upload file. Response code: " + response.code() + ". Response: " + errorBody);
                    addResponse("Failed to upload the file. Response code: " + response.code());
                }
            }
        });
    }

    // Helper method to extract the file name from Uri
    private String getFileName(Uri uri) {
        String fileName = "filename"; // Default name if extraction fails

        // Query the content resolver to get the file name
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }

        return fileName;
    }

    private void attachfiletostore(String fileid,String vector){
        JSONObject requestbody = new JSONObject();
        try {
            requestbody.put("file_id",fileid);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody body = RequestBody.create(requestbody.toString(), JSON);

        // Build the API request
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/vector_stores/"+vector+"/files")
                .header("Authorization", "Bearer " + apikey)
                .header("Content-Type", "application/json")
                .header("OpenAI-Beta", "assistants=v2")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful response
                    String responseBody = response.body().string();
                    addResponse(responseBody);
                } else {
                    // Handle error response
                    addResponse(response.toString());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
            }
        });



    }


   void getvectorid(String id){
        String fileid=id;
        Query query = mUserRef.orderByChild("UID").equalTo(mAuth.getCurrentUser().getUid().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if data exists
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve user data here
                        String vector = userSnapshot.child("vectorID").getValue(String.class);
                        addResponse(vector);
                        attachfiletostore(fileid,vector);
                    }
                } else {
                    System.out.println("No user found with the specified UID.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }
    }

