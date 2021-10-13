package com.example.myplaces.fragments.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplaces.MainActivity;
import com.example.myplaces.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class SignUpFragment extends loginFragment {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPreferences;

    /*visuals*/
    private TextInputEditText usernameEt, emailEt, passwordEt;
    private CheckBox rememberCb;
    private android.widget.Button loginBtn;
    private TextView signInTv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        sharedPreferences = getActivity().getSharedPreferences("my_places_sp", Context.MODE_PRIVATE);

        ImageView backBtn = view.findViewById(R.id.back_btn);
        usernameEt = view.findViewById(R.id.username_et);
        emailEt = view.findViewById(R.id.email_et);
        passwordEt = view.findViewById(R.id.password_et);
        rememberCb = view.findViewById(R.id.remember_cb);
        loginBtn = view.findViewById(R.id.login_btn);
        signInTv = view.findViewById(R.id.sign_in_tv);

        setBackBtn(backBtn);
        setLoginBtn();
        setSignInTv();
    }
    

    private void setLoginBtn(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()){

                    StringBuffer stringBuffer = computeMD5Hash(passwordEt.getText().toString());
                    String emPass = stringBuffer.toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(emailEt.getText().toString(), emPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                DocumentReference documentReference = fireStore.collection("Users").document(user.getUid());

                                Map<String,Object> userMap = new HashMap<>();
                                userMap.put("Email",emailEt.getText().toString());
                                userMap.put("Username",usernameEt.getText().toString());
                                userMap.put("Password",emPass);

                                documentReference.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        editor.putString("username", usernameEt.getText().toString());

                                        if(rememberCb.isChecked()){
                                            editor.putString("email", emailEt.getText().toString());
                                            editor.putString("password", emPass);
                                        }else{
                                            editor.putString("email", null);
                                            editor.putString("password", null);
                                        }

                                        editor.commit();

                                        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_creating_user), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            firebaseAuth.fetchSignInMethodsForEmail(emailEt.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<SignInMethodQueryResult> task) {
                                    if (!task.getResult().getSignInMethods().isEmpty()){
                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_email_exists), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                }
            }
        });
    }

    private void setSignInTv(){
        signInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
                Navigation.findNavController(v).navigate(R.id.action_login_to_signIn);
            }
        });
    }

    private boolean checkData(){
        if (usernameEt.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_username_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(emailEt.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_email_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailEt.getText().toString()).matches()){
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_email_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(passwordEt.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(passwordEt.getText().toString().length()<6){
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_error_password_short), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}