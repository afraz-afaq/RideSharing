package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
    private PostHelpingMethod postHelpingMethod;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postHelpingMethod = new PostHelpingMethod(this);
        dialog = postHelpingMethod.createDialog("Sending...");
        setContentView(R.layout.activity_forgetpassword);
        mSendMail=findViewById(R.id.button_sendemail);
        mEmail = findViewById(R.id.editText_email);
        mAuth = FirebaseAuth.getInstance();
        mSendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                Toast.makeText(getBaseContext(),"Clicked",Toast.LENGTH_SHORT).show();
                mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), "***********")
                        .addOnCompleteListener(
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            try {
                                                throw task.getException();
                                            }
                                            // if user enters wrong email.
                                            catch (FirebaseAuthInvalidUserException invalidEmail) {

                                                Toast.makeText(getBaseContext(), "Email does not exist", Toast.LENGTH_LONG).show();
                                                dialog.cancel();
                                                return;
                                                // TODO: take your actions!
                                            }
                                            // if user enters wrong password.
                                            catch (FirebaseAuthInvalidCredentialsException wrongPassword) {

                                                mSendMail();


                                                // TODO: Take your action
                                            } catch (Exception e) {
                                                //Log.d(TAG, "onComplete: " + e.getMessage());
                                                Toast.makeText(getBaseContext(),e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                        );


            }
        });

    }




    private void mSendMail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(mEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getBaseContext(), "A reset password email has been sent", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                            mEmail.getText().clear();
                        } else {
                            dialog.cancel();
                            Toast.makeText(getBaseContext(), "Error while sending reset email", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
