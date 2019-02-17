package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private  static String TAG = "LOGIN";

    private Button mSignin, buttonNet;
    private EditText mEmail;
    private EditText mPassword;
    private TextView mForgotpassword;
    private Dialog dialogNet;
    int count = 1;
    private LinearLayout mLayer1, mLayer2;
    ////
    private FirebaseAuth mAuth;
    Boolean check = false;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private PreferencesClass preferencesClass;
    private PostHelpingMethod postHelpingMethod;
    DatabaseReference mDatabaseReference;
    private ValueEventListener valueEventListener= new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            PreferencesClass preferencesClass = new PreferencesClass(LoginActivity.this);
            preferencesClass.setUserPassword(mPassword.getText().toString());
            preferencesClass.saveUser(dataSnapshot);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                progressDialog.cancel(); //Added
            //progressDialog.cancel();
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };



    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
//
        //if(!postHelpingMethod.checkConnection())
            //createDialog();

        mAuth = FirebaseAuth.getInstance();

        postHelpingMethod = new PostHelpingMethod(LoginActivity.this);
        progressDialog = postHelpingMethod.createProgressDialog("Logging In", "Please wait while we check your credentials.");
        preferencesClass = new PreferencesClass(LoginActivity.this);


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                        return;
                                    }
                                    String token = task.getResult().getToken();

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());

                                    databaseReference.child("token").setValue(token);
                                    // Log ""and toasten
                                    //String msg = getString(R.string.msg_token_fmt, token);
                                    //Log.d(TAG, msg);
                                    //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                                }
                            });
                    if (preferencesClass.getUSER_NAME() == null) {
                        progressDialog.show(); //Added
                        savePreferences();
                    }else {

                        progressDialog.show(); //Added
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                            progressDialog.cancel(); //Added
                    }

                }

            }
        };

        mSignin = (Button) findViewById(R.id.button_signin);

        mEmail = (EditText) findViewById(R.id.editText_email);
        mPassword = (EditText) findViewById(R.id.editText_password);
        mForgotpassword = (TextView) findViewById(R.id.textView_forgotPassword);
        mForgotpassword.setPaintFlags(mForgotpassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mLayer1 = (LinearLayout) findViewById(R.id.layer_layout1);
        mLayer2 = (LinearLayout) findViewById(R.id.layer_layout2);

        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String getEmail, getPassword;
                getEmail = mEmail.getText().toString().trim();
                getPassword = mPassword.getText().toString().trim();

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                if (!(networkInfo != null && networkInfo.isConnected())) {
                    //check = true;
                        createDialog();

                } else {
                    //check = false;
                    if (TextUtils.isEmpty(getEmail) && TextUtils.isEmpty(getPassword)) {

                        postHelpingMethod.snackbarMessage("Email and Password is required", view);
                    } else if (TextUtils.isEmpty(getEmail)) {
                        postHelpingMethod.snackbarMessage("Please enter your email", view);
                        mEmail.requestFocus();
                    } else if (TextUtils.isEmpty(getPassword)) {
                        postHelpingMethod.snackbarMessage("Please enter your password", view);
                        mPassword.requestFocus();
                    } else {

                        progressDialog.show();
                        mAuth.signInWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    try {
                                        throw task.getException();
                                    }
                                    // if user enters wrong email.
                                    catch (Exception e) {
                                        progressDialog.cancel();
                                        postHelpingMethod.snackbarMessage("Authentication Error: " + e.getMessage(), view);
                                        return;
                                        // TODO: take your actions!
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });


        ///

        mPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mPassword.getRight() - mPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (count % 2 == 1) {
                            mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            mPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_black_24dp, 0, R.drawable.ic_visibility_black_24dp, 0);
                            count++;
                            return true;
                        } else {
                            mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            mPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_black_24dp, 0, R.drawable.ic_visibility_off_black_24dp, 0);
                            count++;
                        }
                    }
                }
                return false;
            }
        });

        mPassword.setHint(getString(R.string.password));

        checkAPI();
        mForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
        //setupSignin();
    }

    private void createDialog(){

        final Dialog dialogNet = new Dialog(LoginActivity.this);
        dialogNet.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNet.setContentView(R.layout.nointernet_dialog);
        Button buttonNet = dialogNet.findViewById(R.id.button_changephone);
        ImageView buttonClose = dialogNet.findViewById(R.id.imageView_close);
        dialogNet.show();
        buttonNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNet.cancel();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNet.cancel();
            }
        });

    }


    private void savePreferences() {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid().toString());
        check = true;
        mDatabaseReference.addValueEventListener(valueEventListener);
    }

    private void checkAPI() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mLayer1.setBackgroundResource(R.drawable.layer1);
            mLayer2.setBackgroundResource(R.drawable.layer2);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG,"In on Destroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"In on Stop");
        mAuth.removeAuthStateListener(firebaseAuthListener);
        if(check)
            mDatabaseReference.removeEventListener(valueEventListener);
    }




}
