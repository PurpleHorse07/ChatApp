package com.apps.sayantan.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    ProgressBar progress;
    RecyclerView msgList;
    EditText messageEdit;
    Button sendMsg;

    List<ItemDataModel> messages;
    ViewAdapter messageAdapter;

    //Initialising Firebase Database objects
    FirebaseDatabase database;              //Entry-point to firebase database
    DatabaseReference dbRef;                //Refer specific part of database
    ChildEventListener childListener;       //Response on change in database children

    //Initializing Firebase Authentication objects
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener stateListener;       //Check sign in/out status of current user

    //Initializing Firebase Storage objects
    FirebaseStorage storage;                //Entry-point to firebase storage
    StorageReference stRef;                 //Refer specific part of storage

    FirebaseRemoteConfig remoteConfig;

    String username = "Anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize references to views
        progress = findViewById(R.id.progress);
        msgList = findViewById(R.id.messagelist);
        messageEdit = findViewById(R.id.editmessage);
        sendMsg = findViewById(R.id.sendbtn);

        //Initialize database objects
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        remoteConfig = FirebaseRemoteConfig.getInstance();

        dbRef = database.getReference().child("messages");        //refer to rootnode.childnode
        stRef = storage.getReference().child("chat_photos");

        // Initialize message RecyclerView and its adapter
        messages = new ArrayList<>();
        messageAdapter = new ViewAdapter(messages);
        msgList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        msgList.setHasFixedSize(true);
        msgList.setAdapter(messageAdapter);

        // Initialize progress bar
        progress.setVisibility(ProgressBar.INVISIBLE);

        // Enable Send button only when there's text to send
        messageEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendMsg.setEnabled(true);
                } else {
                    sendMsg.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedIn(user.getDisplayName());
                } else {
                    onSignedOut();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        remoteConfig.setConfigSettings(configSettings);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("msg_len", 1000);
        remoteConfig.setDefaults(configMap);
        fetchConfig();
    }

    private void fetchConfig() {
        long cacheExpiration = 3600;
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        remoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        remoteConfig.activateFetched();
                        Long len = remoteConfig.getLong("str_len");
                        messageEdit.setFilters(new InputFilter[]{
                                new InputFilter.LengthFilter(len.intValue())
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void onSignedOut() {
        username = "Anonymous";
        if (childListener != null) {
            dbRef.removeEventListener(childListener);
            childListener = null;
        }
    }

    private void onSignedIn(String displayName) {
        username = displayName;
        if (childListener == null) {
            childListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ItemDataModel childMessage = dataSnapshot.getValue(ItemDataModel.class);
                    messages.add(childMessage);
                    messageAdapter = new ViewAdapter(messages);
                    msgList.setAdapter(messageAdapter);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            dbRef.addChildEventListener(childListener);
        }
    }

    // ImagePickerButton shows an image picker to upload a image for a message
    public void photoPicker(View view) {
        Intent photo = new Intent(Intent.ACTION_GET_CONTENT);
        photo.setType("image/jpeg");
        photo.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(photo, "Complete action using"), RC_PHOTO_PICKER);
    }

    // Send Message to the database
    public void sendMessage(View view) {
        ItemDataModel message = new ItemDataModel(messageEdit.getText().toString(), username, null);
        dbRef.push().setValue(message);
        messageEdit.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth.addAuthStateListener(stateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        auth.removeAuthStateListener(stateListener);
        if (childListener != null) {
            dbRef.removeEventListener(childListener);
            childListener = null;
        }
        messages.clear();
        messageAdapter = new ViewAdapter(messages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference imRef = null;
            if (imageUri != null) {
                imRef = stRef.child(imageUri.getLastPathSegment());
            } else {
                Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
            }
            if (imRef != null) {
                imRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri download = taskSnapshot.getUploadSessionUri();
                        if (download != null) {
                            dbRef.push().setValue(new ItemDataModel(null, username, download.toString()));
                        } else {
                            Toast.makeText(MainActivity.this, "Cannot store image URI to database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Reference to storage is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                auth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
