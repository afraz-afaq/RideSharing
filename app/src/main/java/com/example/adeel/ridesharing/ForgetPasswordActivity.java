package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgetPasswordActivity extends AppCompatActivity {
    private Button mSendMail;
    private EditText mEmail;
    FirebaseAuth mAuth;
    private Toolbar mtoolbar;
    private PostHelpingMethod postHelpingMethod;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        postHelpingMethod = new PostHelpingMethod(this);
        dialog = postHelpingMethod.createDialog("Sending...");
        setContentView(R.layout.activity_forgetpassword);
        mSendMail = findViewById(R.id.button_sendemail);
        mEmail = findViewById(R.id.editText_email);
        mAuth = FirebaseAuth.getInstance();
        mSendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (TextUtils.isEmpty(mEmail.getText().toString())) {
                    postHelpingMethod.snackbarMessage("Please enter your email", view);
                } else {
                    dialog.show();
                    mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), "***********")
                            .addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                dialog.dismiss();
                                                postHelpingMethod.snackbarMessage(task.getException().getMessage(),view);
                                            }
                                        }
                                    }
                            );

                }
            }
        });

    }

    private void mSendMail(final View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(mEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            postHelpingMethod.snackbarMessage("A reset password email has been sent", view);
                            dialog.cancel();
                            mEmail.getText().clear();
                        } else {
                            dialog.cancel();
                            postHelpingMethod.snackbarMessage("Error while sending reset email", view);
                        }
                    }
                });
    }
}
