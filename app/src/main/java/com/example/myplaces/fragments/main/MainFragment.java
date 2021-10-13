package com.example.myplaces.fragments.main;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myplaces.LoginActivity;
import com.example.myplaces.R;
import com.example.myplaces.fragments.main.viewpages.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    /*requests*/
    private final int POS_LIST_VIEW = 0, POS_MAP_VIEW = 1;

    /*data*/
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    SharedPreferences sharedPreferences;

    /*visuals*/
    private ViewPager viewPager;
    private ImageView logoutIv, listViewIv, mapViewIv;
    private FloatingActionButton addFab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        viewPager = view.findViewById(R.id.view_page);
        logoutIv = view.findViewById(R.id.logout_btn);
        listViewIv = view.findViewById(R.id.list_view_btn);
        mapViewIv = view.findViewById(R.id.map_view_btn);
        addFab = view.findViewById(R.id.add_fab);
        TextView helloTv = view.findViewById(R.id.hello_tv);

        sharedPreferences = getActivity().getSharedPreferences("my_places_sp", Context.MODE_PRIVATE);
        helloTv.setText(getContext().getResources().getString(R.string.hello)+sharedPreferences.getString("username",""));

        setViewPager();
        setLogoutIv();
        setNavigationBtns();
        setAddFab();
    }

    private void setViewPager(){
        NoteListViewFragment noteListViewFragment = new NoteListViewFragment();
        NoteMapViewFragment noteMapViewFragment = new NoteMapViewFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(),0);
        viewPagerAdapter.addFragment(noteListViewFragment);
        viewPagerAdapter.addFragment(noteMapViewFragment);

        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position == POS_LIST_VIEW){
                    listViewIv.setColorFilter(getContext().getResources().getColor(R.color.hippie_blue));
                    mapViewIv.setColorFilter(getContext().getResources().getColor(R.color.grey3));

                }else if (position == POS_MAP_VIEW){
                    listViewIv.setColorFilter(getContext().getResources().getColor(R.color.grey3));
                    mapViewIv.setColorFilter(getContext().getResources().getColor(R.color.hippie_blue));
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void setLogoutIv(){
        logoutIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("email", null);
                editor.putString("username", null);
                editor.putString("password", null);
                editor.commit();

                firebaseAuth.signOut();

                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
                getActivity().finish();
            }
        });
    }

    private void setNavigationBtns(){
        listViewIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(POS_LIST_VIEW);
            }
        });

        mapViewIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(POS_MAP_VIEW);
            }
        });
    }

    private void setAddFab(){
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(addFab).navigate(R.id.action_main_to_note);
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public ViewPagerAdapter(@NonNull @NotNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @NotNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment){
            fragments.add(fragment);
        }
    }

}