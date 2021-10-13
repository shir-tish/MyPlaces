package com.example.myplaces.fragments.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myplaces.R;
import com.example.myplaces.database.DatabaseHelper;
import com.example.myplaces.models.Note;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.myplaces.MainActivity.LOCATION_PERMISSION_REQUEST;
import static com.example.myplaces.MainActivity.STORAGE_PERMISSION_REQUEST;


public class NoteFragment extends Fragment {

    /*visuals*/
    private ImageView backIv, heartIv, deleteIv, imageIcon, imageIv;
    private EditText titleEt;
    private TextInputEditText dateEt, descriptionEt;
    private FloatingActionButton moreFab, cameraFab, galleryFab, clearFab;
    private TextView dateChangeTv;
    private android.widget.Button saveBtn, cancelBtn;

    /*requests*/
    private final int CAMERA_REQUEST = 1, GALLERY_REQUEST = 2;

    /*data*/
    DatabaseHelper databaseHelper;
    private Note note;
    private boolean fabIsOpen = false;
    private int position =-1;

    /*animations*/
    private Animation fabOpen, fabClose, fabRClockwise, fabRAntiClockwise;

    /*save image*/
    private Uri imageUri;
    private File imageFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        databaseHelper = new DatabaseHelper(getContext());

        backIv = view.findViewById(R.id.back_btn);
        heartIv = view.findViewById(R.id.heart_btn);
        deleteIv = view.findViewById(R.id.delete_btn);
        imageIcon = view.findViewById(R.id.image_icon);
        imageIv = view.findViewById(R.id.image_iv);
        titleEt = view.findViewById(R.id.title_et);
        dateEt = view.findViewById(R.id.date_et);
        dateChangeTv = view.findViewById(R.id.date_change_tv);
        descriptionEt = view.findViewById(R.id.description_et);
        moreFab = view.findViewById(R.id.more_fab);
        cameraFab = view.findViewById(R.id.camera_fab);
        galleryFab = view.findViewById(R.id.gallery_fab);
        clearFab = view.findViewById(R.id.delete_fab);
        saveBtn = view.findViewById(R.id.save_btn);
        cancelBtn = view.findViewById(R.id.cancel_btn);

        setData();
        setBackIv();
        setHeartIv();
        setDeleteIv();
        setImageIv();
        setImageFabs();
        setTitleEt();
        setDateEt();
        setDescriptionEt();
        setSaveBtn();
        setCancelBtn();
    }

    private void setData(){
        this.note = new Note();

        if (getArguments() != null) {
            this.position = getArguments().getInt("position");
            this.note.setTitle(getArguments().getString("note_title"));
            this.note.setDescription(getArguments().getString("note_description"));
            this.note.setHeart(getArguments().getBoolean("note_heart"));
            this.note.setDate((Date)getArguments().getSerializable("note_date"));
            this.note.setLocation(getArguments().getParcelable("note_location"));
            this.note.setImageLoc(getArguments().getParcelable("note_image"));

        }else{
            setCurDate();
        }
    }

    private void setCurDate(){
        Calendar calendar = Calendar.getInstance();
        Date curDate = new Date();
        curDate.setDate(calendar.get(Calendar.DAY_OF_MONTH));
        curDate.setMonth(calendar.get(Calendar.MONTH));
        curDate.setYear(calendar.get(Calendar.YEAR));
        note.setDate(curDate);
    }

    private void startDatePickerDialog(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = String.format("%02d/%02d/%d", dayOfMonth, month+1, year);
                dateEt.setText(date);
                Date nDate = new Date();
                nDate.setDate(dayOfMonth);
                nDate.setMonth(month);
                nDate.setYear(year);
                note.setDate(nDate);

                dateEt.setError(null);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.TimePicker, dateSetListener, note.getDate().getYear(), note.getDate().getMonth(), note.getDate().getDate());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setTitle(R.string.set_date);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void setBackIv(){
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCloseDialog();
            }
        });
    }

    private void setHeartIv(){
        if (note.isHeart()){
            heartIv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_heart_on));
            heartIv.setColorFilter(getContext().getResources().getColor(R.color.valencia));
        }

        heartIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!note.isHeart()){
                    heartIv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_heart_on));
                    heartIv.setColorFilter(getContext().getResources().getColor(R.color.valencia));
                    note.setHeart(true);
                }else {
                    heartIv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_heart_off));
                    heartIv.setColorFilter(getContext().getResources().getColor(R.color.grey1));
                    note.setHeart(false);
                }
            }
        });
    }

    private void setDeleteIv(){
        if (position == -1)
            deleteIv.setVisibility(View.GONE);
        deleteIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDeleteDialog();
            }
        });
    }

    private void setImageIv(){
        if(!note.getImageLoc().toString().isEmpty()){
            Glide.with(this).load(note.getImageLoc()).into(imageIv);
            imageIcon.setVisibility(View.GONE);
        }else{
            imageIcon.setVisibility(View.VISIBLE);
        }
    }

    private void setImageFabs(){
        fabOpen = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_close);
        fabRClockwise = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_rotate_clockwise);
        fabRAntiClockwise = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_rotate_anticlockwise);

        moreFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabIsOpen){
                    closeMoreFab();
                } else{
                    openMoreFab();
                }
            }
        });

        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pic.jpg");
                imageUri = FileProvider.getUriForFile(getContext(), "com.example.myplaces", imageFile);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
                closeMoreFab();
            }
        });

        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST);
                closeMoreFab();
            }
        });

        clearFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Glide.with(getContext()).clear(imageIv);

                closeMoreFab();
                
                note.setImageLoc(Uri.EMPTY);
                imageIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    private void closeMoreFab(){
        cameraFab.startAnimation(fabClose);
        galleryFab.startAnimation(fabClose);
        if(note.getImageLoc() != Uri.EMPTY){
            clearFab.startAnimation(fabClose);
        }

        moreFab.startAnimation(fabRClockwise);

        cameraFab.setClickable(false);
        galleryFab.setClickable(false);
        clearFab.setClickable(false);

        cameraFab.setVisibility(View.GONE);
        galleryFab.setVisibility(View.GONE);
        clearFab.setVisibility(View.GONE);

        fabIsOpen = false;
    }

    private void openMoreFab(){
        cameraFab.startAnimation(fabOpen);
        galleryFab.startAnimation(fabOpen);
        if(!note.getImageLoc().toString().isEmpty())
            clearFab.startAnimation(fabOpen);

        moreFab.startAnimation(fabRAntiClockwise);

        cameraFab.setClickable(true);
        galleryFab.setClickable(true);
        clearFab.setClickable(true);

        cameraFab.setVisibility(View.VISIBLE);
        galleryFab.setVisibility(View.VISIBLE);
        if(!note.getImageLoc().toString().isEmpty())
            clearFab.setVisibility(View.VISIBLE);

        fabIsOpen = true;
    }

    private void setDateEt(){
        if (note.getDate()!=null){
            String date = String.format("%02d/%02d/%d", note.getDate().getDate(), note.getDate().getMonth()+1, note.getDate().getYear());
            dateEt.setText(date);
        }
        dateEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePickerDialog();
            }
        });

        dateChangeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePickerDialog();

            }
        });
    }

    private void setTitleEt(){
        if(!note.getTitle().isEmpty() || note.getTitle()!=null){
            titleEt.setText(note.getTitle());
        }
    }

    private void setDescriptionEt(){
        if(!note.getDescription().isEmpty() || note.getDescription()!=null){
            descriptionEt.setText(note.getDescription());
        }
    }

    private void setSaveBtn(){
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkData()){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int hasPermission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                        if(hasPermission != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                        }else{
                            onLocationGranted();
                        }
                    }

                }
            }
        });
    }

    private void onLocationGranted(){
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasPermission == PackageManager.PERMISSION_GRANTED) {

                client.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        android.location.Location location = task.getResult();
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                Location cur_location = new Location("loc");
                                cur_location.setLongitude(addresses.get(0).getLongitude());
                                cur_location.setLatitude(addresses.get(0).getLatitude());
                                note.setLocation(cur_location);

                                int hasPermission = getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                                if(hasPermission != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
                                }else {
                                    onStorageGranted();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        }
    }

    private void onStorageGranted(){
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        databaseHelper.addNote(note, position);

        Navigation.findNavController(titleEt).popBackStack();
    }

    private void setCancelBtn(){
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCloseDialog();
            }
        });
    }

    private boolean checkData(){
        if (titleEt.getText().toString().isEmpty()){
            Toast.makeText(getContext(), getContext().getResources().getText(R.string.toast_error_title).toString(), Toast.LENGTH_SHORT).show();
            return false;
        }

        note.setTitle(titleEt.getText().toString());
        note.setDescription(descriptionEt.getText().toString());

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK){
            switch (requestCode){
                case CAMERA_REQUEST:
                    Glide.with(this).load(Drawable.createFromPath(imageFile.getAbsolutePath())).into(imageIv);
                    note.setImageLoc(Uri.parse(imageFile.getAbsolutePath()));
                    imageIcon.setVisibility(View.GONE);
                    break;
                case GALLERY_REQUEST:
                    Uri selectPhoto = data.getData();
                    Glide.with(this).load(selectPhoto).into(imageIv);
                    note.setImageLoc(selectPhoto);
                    imageIcon.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void openCloseDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.exit_edit_notes_title));
        builder.setMessage(getContext().getResources().getString(R.string.exit_edit_notes_message));
        builder.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Navigation.findNavController(titleEt).popBackStack();
                dialog.dismiss();
            }
        }).setNegativeButton(getContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_error_permission_location), Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == STORAGE_PERMISSION_REQUEST){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getResources().getString(R.string.toast_error_permission_storage), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openDeleteDialog(){
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.delete_note_title)+note.getTitle());
        builder.setMessage(getContext().getResources().getString(R.string.delete_note_message)
                +getContext().getResources().getString(R.string.quots)
                +note.getTitle()+getContext().getResources().getString(R.string.quots));
        builder.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteNote(position);
                dialog.dismiss();
                Navigation.findNavController(titleEt).popBackStack();
            }
        }).setNegativeButton(getContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}