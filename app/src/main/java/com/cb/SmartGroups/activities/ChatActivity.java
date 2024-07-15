package com.cb.SmartGroups.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.adapters.recyclerviews.MessagesListAdapter;
import com.cb.SmartGroups.utils.cards.ChatMessageCard;
import com.cb.SmartGroups.utils.cards.groups.GroupCard;
import com.cb.SmartGroups.utils.customdatamodels.UserType;
import com.cb.SmartGroups.utils.firesoredatamodels.StorageFile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {

    // Firestore
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    // Storage
    private StorageReference groupStorageRef;

    // Views
    private EditText messageInput;
    private MaterialButton sendMessage;
    private MaterialButton sendFile;

    // Reference to chatroom collection
    private CollectionReference chatroomRef;

    // Reference to file storage collection
    private CollectionReference storageRef;

    private ArrayList<ChatMessageCard> messageList;
    private MessagesListAdapter messageAdapter;

    // Card with all the information
    private GroupCard card;

    private int userType;

    // Reference to the group document
    DocumentReference groupRef;

    RecyclerView messageListContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatactivity);

        // Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Views
        messageInput = findViewById(R.id.messageInput);
        sendMessage = findViewById(R.id.sendMessage);
        sendFile = findViewById(R.id.sendFile);

        // Get card to know where we have to write the messages
        Gson gson = new Gson();
        String cardAsString = getIntent().getStringExtra("cardAsString");
        card = gson.fromJson(cardAsString, GroupCard.class);

        // Reference to Storage custom of this group
        groupStorageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child(card.getCourseName() + "/" + card.getSubjectName() + "/" + card.getGroupName());

        // Get userType
        userType = getIntent().getIntExtra("userType", 0);

        // Reference to the group document
        groupRef = fStore
                .collection("CoursesOrganization")
                .document(card.getCourseName())
                .collection("Subjects")
                .document(card.getSubjectName())
                .collection(card.getCollectionId())
                .document(card.getGroupId());

        if (card.getHasTeacher()) {
            chatroomRef = groupRef.collection("ChatRoomWithTeacher");
            storageRef = groupRef.collection("StorageWithTeacher");
        } else {
            chatroomRef = groupRef.collection("ChatRoomWithoutTeacher");
            storageRef = groupRef.collection("StorageWithoutTeacher");
        }

        // Recyclerview setup
        messageListContainer = findViewById(R.id.messageListContainer);

        messageList = new ArrayList<ChatMessageCard>();
        messageAdapter = new MessagesListAdapter(messageList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messageListContainer.setAdapter(messageAdapter);
        messageListContainer.setLayoutManager(layoutManager);

        // Listen for added message in the chatroom
        chatroomRef
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {

                    if (error != null) {
                        return;
                    } else if (queryDocumentSnapshots == null) {
                        return;
                    }

                    messageList.clear();
                    populateMessages(queryDocumentSnapshots);
                });

        // Setup for send message button
        sendMessage.setOnClickListener(v -> {
            if (!messageInput.getText().toString().isEmpty()) {
                sendMessage(messageInput.getText().toString(), null);
            }
        });

        sendFile.setOnClickListener(v -> {
            selectFile();
        });

    }

    private void sendMessage(String messageInputText, StorageFile fileRef) {
        String collectionPath;

        if (userType == UserType.TYPE_STUDENT) {
            collectionPath = "Students";
        } else {
            collectionPath = "Teachers";
        }

        fStore
                .collection(collectionPath)
                .document(fAuth.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fullName = (String) documentSnapshot.get("FullName");

                    String messageTitle;
                    String message;

                    String filename;
                    if (fileRef == null) {
                        messageTitle = fullName;
                        message = messageInputText;
                        filename = null;
                    } else {
                        messageTitle = "File uploaded";
                        message = null;
                        filename = fileRef.getFileName();
                    }

                    Timestamp timestamp = Timestamp.now();

                    Date date = new Date();
                    DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

                    ChatMessageCard messageCard = new ChatMessageCard(messageTitle, fAuth.getUid(), message, timestamp, df.format(date), fileRef, fullName, filename);
                    chatroomRef
                            .add(messageCard)
                            .addOnSuccessListener(documentReference -> {
                                if (userType == UserType.TYPE_STUDENT) {
                                    groupRef.update("hasVisibility", true);
                                }
                                messageInput.getText().clear();
                            });
                });

    }

    private void populateMessages(QuerySnapshot queryDocumentSnapshots) {
        for (QueryDocumentSnapshot messageDoc : queryDocumentSnapshots) {
            ChatMessageCard message = messageDoc.toObject(ChatMessageCard.class);
            messageList.add(message);
        }
        messageListContainer.scrollToPosition(messageList.size() - 1);
        messageAdapter.notifyDataSetChanged();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadFile(data.getData());
        }

    }

    private void uploadFile(Uri data) {

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getExtensionFromMimeType(cR.getType(data));

        if (type.equals("pdf") || type.equals("jpg") || type.equals("jpeg") || type.equals("png")) {

            String fileNameWithExtension = getFileName(data);

            StorageReference fileRef = groupStorageRef.child("/" + fileNameWithExtension);

            fileRef.putFile(data).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(this, "File uploaded", Toast.LENGTH_SHORT).show();

                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Date date = new Date();
                    DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
                    df.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

                    if (userType == UserType.TYPE_STUDENT) {
                        fStore
                                .collection("Students")
                                .document(fAuth.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    StorageFile fileReference = new StorageFile((String) documentSnapshot.get("FullName"), fileNameWithExtension, df.format(date), uri.toString());
                                    storageRef
                                            .add(fileReference)
                                            .addOnSuccessListener(documentReference -> {
                                                sendMessage(null, fileReference);
                                            });
                                });
                    } else {
                        fStore
                                .collection("Teachers")
                                .document(fAuth.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    StorageFile fileReference = new StorageFile((String) documentSnapshot.get("FullName"), fileNameWithExtension, df.format(date), uri.toString());
                                    storageRef
                                            .add(fileReference)
                                            .addOnSuccessListener(documentReference -> {
                                                sendMessage(null, fileReference);
                                            });
                                });
                    }
                });

            });

        } else {
            Toast.makeText(this, "File not supported. Files with .pdf, .jpg, .jpeg and .png extension are supported", Toast.LENGTH_LONG).show();
        }

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
