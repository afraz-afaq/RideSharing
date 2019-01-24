package com.example.adeel.ridesharing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikhaellopez.circularimageview.CircularImageView;


public class ProfileFragment extends Fragment {
    private TextView mChangepassword,mPhoneno,mEmail,mRegNo,mDept,mUsername;
    private CircularImageView mProfileImage;
    private Dialog mCustomDialog;
    private Dialog mPasswordDialog;
    Animation fromTop,fromBottom;
    private RelativeLayout mLayout1;
    private LinearLayout mLayout2;
    private CardView mCardView;
    private ImageView mClose;
    private ImageButton mEditPhone;
    private PreferencesClass preferencesClass;
    private Button btnSend,btnupdate;
    private EditText editTextMail,editTextchangephone;
    private PostHelpingMethod postHelpingMethod;
    private ProgressDialog progressDialog;
    private View rootView;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mLayout1=rootView.findViewById(R.id.layout_shape);
        mLayout2=rootView.findViewById(R.id.layout2);
        mPhoneno = rootView.findViewById(R.id.textview_phone);
        mEmail = rootView.findViewById(R.id.textview_email);
        mRegNo = rootView.findViewById(R.id.textview_regno);
        mDept = rootView.findViewById(R.id.textview_dept);
        mProfileImage = rootView.findViewById(R.id.profileimage);
        mUsername = rootView.findViewById(R.id.textview_username);

        preferencesClass = new PreferencesClass(getActivity());
        mPhoneno.setText(preferencesClass.getUserContact());
        mEmail.setText(preferencesClass.getUSER_EMAIL());
        mRegNo.setText(preferencesClass.getUserRegno());
        mDept.setText(preferencesClass.getUserDept());
        mUsername.setText(preferencesClass.getUSER_NAME());
        mEditPhone=(ImageButton) rootView.findViewById(R.id.imageButton_editPhoneno);
        mProfileImage.setImageBitmap(preferencesClass.decodeBase64(preferencesClass.getUserImage()));
        postHelpingMethod = new PostHelpingMethod(getActivity());
        progressDialog = postHelpingMethod.createProgressDialog("Please Wait..","Verifying credentials and sending email.");
        mEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPhone();
            }
        });
        mCustomDialog = new Dialog(getActivity());
        mPasswordDialog = new Dialog(getActivity());
        mCustomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mChangepassword=(TextView)rootView.findViewById(R.id.textView_changePassword);
        mChangepassword.setPaintFlags(mChangepassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mChangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    mPasswordDialog.setContentView(R.layout.changepassword_dialogbox);
                    mClose = (ImageView) mPasswordDialog.findViewById(R.id.imageView_close);
                    btnSend = (Button) mPasswordDialog.findViewById(R.id.button_sendMail);
                    btnSend.setOnClickListener(sendMailListener);
                    editTextMail = (EditText) mPasswordDialog.findViewById(R.id.editText_email);
                    mPasswordDialog.show();
                    mClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPasswordDialog.dismiss();
                        }
                    });
            }
        });


        runtimeAnim();
        return rootView;
    }

    View.OnClickListener sendMailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ConnectivityManager connMgr = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (!(networkInfo != null && networkInfo.isConnected())) {
                //check = true;
                createDialog();
                mPasswordDialog.cancel();

            } else {

                String password = editTextMail.getText().toString();
                if (TextUtils.isEmpty(password))
                    postHelpingMethod.snackbarMessage("Please enter your password", rootView);
                else {
                    progressDialog.show();
                    reAuthUser(rootView);
                }
            }
        }
    };

    private void editPhone(){
        Intent intent = new Intent(getActivity(),PhoneVerification.class); // Added
        intent.putExtra("from","profile"); // Added
        startActivity(intent);
        getActivity().finish();
    }

    private void reAuthUser(final View v){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(preferencesClass.getUSER_EMAIL(), editTextMail.getText().toString());
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mSendMail(v);
                        } else {
                            postHelpingMethod.snackbarMessage(task.getException().getMessage(),v);
                            progressDialog.cancel();
                        }
                    }
                });
    }

    private void mSendMail(final View v) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(preferencesClass.getUSER_EMAIL())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            postHelpingMethod.snackbarMessage("A reset password email has been sent",v);
                            editTextMail.getText().clear();
                            progressDialog.dismiss();
                            mPasswordDialog.cancel();
                        } else {
                            postHelpingMethod.snackbarMessage(task.getException().getMessage(),v);
                            progressDialog.cancel();
                        }
                    }
                });
    }


    private void runtimeAnim() {

        fromTop = AnimationUtils.loadAnimation(getActivity(), R.anim.fall_down);
        mLayout1.setAnimation(fromTop);
        fromBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.from_bottom);
        mLayout2.setAnimation(fromBottom);

    }

    private void createDialog(){

        final Dialog dialogNet = new Dialog(getActivity());
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



}
