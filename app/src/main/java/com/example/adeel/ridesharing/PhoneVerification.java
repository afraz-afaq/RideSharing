package com.example.adeel.ridesharing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.kofigyan.stateprogressbar.StateProgressBar;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class PhoneVerification extends AppCompatActivity {


    private static final String TAG = "PhoneAuth";

    private EditText phoneText;
    private EditText codeText;
    private LinearLayout sendLayout, verifyLayout, continueLayout;
    private TextView mResendCode, textView_phoneno;
    String[] descriptionData = {"Send Code", "Verify", "Continue"};
    StateProgressBar stateProgressBar;
    private Button sendButton;
    private Button resendButton;
    private Button signoutButton;
    String number, email, password, uID;
    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;
    PostHelpingMethod postHelpingMethod;
    private PreferencesClass preferencesClass;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        postHelpingMethod = new PostHelpingMethod(PhoneVerification.this);
        stateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
        stateProgressBar.setStateDescriptionData(descriptionData);

        textView_phoneno = findViewById(R.id.textView_phoneno);
        mResendCode = findViewById(R.id.textView_resendCode);
        mResendCode.setPaintFlags(mResendCode.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        preferencesClass = new PreferencesClass(PhoneVerification.this);
        phoneText = (EditText) findViewById(R.id.editText_phone);
        sendLayout = findViewById(R.id.step1_container);
        verifyLayout = findViewById(R.id.step2_container);
        continueLayout = findViewById(R.id.step3_container);
        codeText = (PinView) findViewById(R.id.pinView);
        sendButton = (Button) findViewById(R.id.button_sendCode);
        mResendCode = (TextView) findViewById(R.id.textView_resendCode);


        PreferencesClass preferencesClass = new PreferencesClass(PhoneVerification.this);
// Added
        Intent intent = getIntent();
        if(!getIntent().getStringExtra("from").equals("profile"))
            phoneText.setText(getIntent().getStringExtra("phoneNumber"));

// Added
        fbAuth = FirebaseAuth.getInstance();
        uID = fbAuth.getUid();
        email = preferencesClass.getUSER_EMAIL();
        password = preferencesClass.getUSER_Password();

        Toast.makeText(this, email + "  " + password + "  " + uID+"    "+getIntent().getStringExtra("profileNumber"), Toast.LENGTH_SHORT).show();
    }

    public void sendCode(View view) {
        progressDialog = postHelpingMethod.createProgressDialog("Sending Code","Please wait.....");
        progressDialog.show();
        Pattern sPattern = Pattern.compile("^((\\+923)\\d{9})$");
        if(TextUtils.isEmpty(phoneText.getText().toString())) {
            postHelpingMethod.snackbarMessage("Please enter your current phone number", view);
            progressDialog.cancel();
        }
        else if((phoneText.getText().toString()).equals(preferencesClass.getUserContact()) && getIntent().getStringExtra("from").equals("profile")) {
            postHelpingMethod.snackbarMessage("Contact already verified",view);
            progressDialog.cancel();
        }
        else if(!sPattern.matcher(phoneText.getText().toString()).matches()) {
            postHelpingMethod.snackbarMessage("Enter number in correct format eg: +923400998747", view);
            progressDialog.cancel();
        }
        else {
//                            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("phonestatus").setValue("false");
            number = phoneText.getText().toString();
            sendLayout.setVisibility(View.GONE);
            verifyLayout.setVisibility(View.VISIBLE);
            stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
            textView_phoneno.setText(phoneText.getText().toString());
            setUpVerificatonCallbacks();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    verificationCallbacks);
        }

    }

    private void changePhoneStatus(String status){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uID).child("phonestatus").setValue(status);
        preferencesClass.setUserPhoneStatus(status);
    }

    private void changePhoneNumber(){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uID).child("contact").setValue(phoneText.getText().toString());
        preferencesClass.setUserContact(number);
    }

    private void setUpVerificatonCallbacks() {
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Toast.makeText(PhoneVerification.this, "Verified", Toast.LENGTH_SHORT).show();
                codeText.setText("");
                progressDialog.dismiss();
                verifyLayout.setVisibility(View.GONE);
                continueLayout.setVisibility(View.VISIBLE);
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.d(TAG, "Invalid credential: " + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // SMS quota exceeded
                    Log.d(TAG, "SMS Quota exceeded.");
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                phoneVerificationId = verificationId;
                resendToken = token;
                Toast.makeText(PhoneVerification.this, token.toString(), Toast.LENGTH_SHORT).show();
                changePhoneStatus("false");
                changePhoneNumber();
                progressDialog.dismiss();
            }
        };
    }

    public void verifyCode(View view) {
        progressDialog = postHelpingMethod.createProgressDialog("Verifying Code","Please wait.....");
        progressDialog.show();
        String code = codeText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    codeText.setText("");
                    FirebaseUser user = task.getResult().getUser();
                    String phoneNumber = user.getPhoneNumber();
                    Toast.makeText(PhoneVerification.this, "Verified", Toast.LENGTH_SHORT).show();
                    fbAuth.getCurrentUser().delete();
                    fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(PhoneVerification.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                }
                                // if user enters wrong email.
                                catch (Exception e) {
                                    //postHelpingMethod.snackbarMessage("Authentication Error: " + e.getMessage(), view);
                                    return;
                                    // TODO: take your actions!
                                }
                            } else {
                                changePhoneStatus("true");
                                changePhoneNumber();
                                progressDialog.dismiss();
                                verifyLayout.setVisibility(View.GONE);
                                continueLayout.setVisibility(View.VISIBLE);
                                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);

                            }
                        }
                    });

                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
        });
    }



    public void resendCode(View view) {
        progressDialog = postHelpingMethod.createProgressDialog("Sending Code","Please wait.....");
        progressDialog.show();
        number = phoneText.getText().toString();
        setUpVerificatonCallbacks();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    public void continueTo(View view) {
        exit();
    }

    public void exit(){
        changePhoneStatus("true");
        changePhoneNumber();
        Intent intent = new Intent(PhoneVerification.this, MainActivity.class);;
        if(getIntent().getStringExtra("from").equals("profile"))
            intent.putExtra("FromProfile","yes");

        startActivity(intent);
        finish();
    }

//    @Override
//    public void onBackPressed() {
//            moveTaskToBack(true);
//            super.onBackPressed();
//
//
//    }

}
