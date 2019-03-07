package com.example.adeel.ridesharing;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.adeel.ridesharing.R.id.container;
import static com.example.adeel.ridesharing.R.id.regNo;
import static com.example.adeel.ridesharing.R.styleable.CoordinatorLayout;

/**
 * Created by Afraz on 1/21/2019.
 */

public class CarBottomSheet extends BottomSheetDialogFragment {

    private Button mDelete;
    private Button mCancel;

    private String deleteStatus;
    private String uId, vReg, from;

    View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle bundle = getArguments();
            uId = bundle.getString("uId");
            from = bundle.getString("from");
            if(from.equals("car"))
                vReg = bundle.getString("carReg");
            else
                vReg = bundle.getString("bikeReg");

            deleteCar();
        }
    };

    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CarBottomSheet.super.getDialog().cancel();
        }
    };


    public static BottomSheetDialogFragment getInstance() {
        return new BottomSheetDialogFragment();
    }

    public interface GetDeleteStatus{
        public void onDeleteStatusPassed(String deleteStatus);
    }

    GetDeleteStatus getDeleteStatus;
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        getDeleteStatus = (GetDeleteStatus)context;
    }



    private void getDeleteStatus(String deleteStatus
    ){ getDeleteStatus.onDeleteStatusPassed(deleteStatus);}



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View chooserView = inflater.inflate(R.layout.bottom_sheet_car_delete,container,false);
        mDelete = chooserView.findViewById(R.id.carDelete);
        mDelete.setOnClickListener(deleteListener);
        mCancel = chooserView.findViewById(R.id.carCancel);
        mCancel.setOnClickListener(cancelListener);

        return chooserView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void deleteCar(){
        DatabaseReference carRef;
        if(from.equals("car")) {
            carRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Cars").child(vReg);
            deleteStatus = "car";
        }
        else {
            carRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("Bikes").child(vReg);
            deleteStatus = "bike";
        }
        carRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Removed!!", Toast.LENGTH_SHORT).show();
                    getDeleteStatus.onDeleteStatusPassed(deleteStatus);
                    CarBottomSheet.super.getDialog().cancel();
                }
                else{
                    deleteStatus = "false";
                    getDeleteStatus.onDeleteStatusPassed(deleteStatus);
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            }
        });
    }


}
