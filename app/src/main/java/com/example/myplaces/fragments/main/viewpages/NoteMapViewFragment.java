package com.example.myplaces.fragments.main.viewpages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.myplaces.R;
import com.example.myplaces.database.DatabaseHelper;
import com.example.myplaces.models.Note;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.myplaces.MainActivity.LOCATION_PERMISSION_REQUEST;


public class NoteMapViewFragment extends Fragment {

    private View view;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            setMap(googleMap);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_note_map_view, container, false);
        this.view = view;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void setMap(GoogleMap googleMap) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        android.location.Location location = task.getResult();
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                                LatLng latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                                int hasPermission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                                }else{
                                    googleMap.setMyLocationEnabled(true);
                                }

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));

                                setNotesLocations(googleMap);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    private void setNotesLocations(GoogleMap googleMap){
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        List<Note> notes = databaseHelper.getNotes();

        for(int i=0 ; i<notes.size() ; i++){
            Note note = notes.get(i);
            LatLng latLng = new LatLng(note.getLocation().getLatitude(), note.getLocation().getLongitude());
            String date = String.format("%02d/%02d/%d", note.getDate().getDate(), note.getDate().getMonth()+1, note.getDate().getYear());
            googleMap.addMarker(new MarkerOptions().position(latLng).title(note.getTitle()).snippet(date));
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull @NotNull Marker marker) {

                int position = Integer.parseInt(marker.getId().substring(1));

                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                bundle.putString("note_title", notes.get(position).getTitle());
                bundle.putString("note_description", notes.get(position).getDescription());
                bundle.putBoolean("note_heart", notes.get(position).isHeart());
                bundle.putSerializable("note_date", notes.get(position).getDate());
                bundle.putParcelable("note_location", notes.get(position).getLocation());
                bundle.putParcelable("note_image", notes.get(position).getImageLoc());

                Navigation.findNavController(view).navigate(R.id.action_main_to_note, bundle);

                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_error_permission_location), Toast.LENGTH_SHORT).show();
            }
        }
    }
}