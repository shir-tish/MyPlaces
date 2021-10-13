package com.example.myplaces.fragments.login;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.myplaces.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class loginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_login, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        android.widget.Button signUpBtn = view.findViewById(R.id.sign_up_btn);
        android.widget.Button signInBtn = view.findViewById(R.id.sign_in_btn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_login_to_signUp);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_login_to_signIn);
            }
        });
    }

    protected StringBuffer computeMD5Hash(String password){
        try {
            //Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer MD5Hash = new StringBuffer();
            for (int i=0 ; i<messageDigest.length ; i++){
                String h = Integer.toHexString(0XFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                MD5Hash.append(h);
            }

            return MD5Hash;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;

    }

    protected void setBackBtn(ImageView backBtn){
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).popBackStack();
            }
        });
    }
}