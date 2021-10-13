package com.example.myplaces.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myplaces.R;
import com.example.myplaces.models.Note;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private final List<Note> notes;
    private final Context context;
    private NotesListListener listener;

    public NotesListAdapter(Context context, List<Note> notes){
        this.context = context;
        this.notes = notes;}

    public interface NotesListListener{
        void onItemClicked(int position);
        void onHeartClicked(int position, boolean turnOn);
        void onDeleteClicked(int position);
    }

    public void setListener(NotesListListener listener){this.listener = listener;}

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image, heart, delete;
        TextView title, date, location, distance, description;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_iv);
            heart = itemView.findViewById(R.id.heart);
            delete = itemView.findViewById(R.id.delete);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            location = itemView.findViewById(R.id.location);
            distance = itemView.findViewById(R.id.distance);
            description = itemView.findViewById(R.id.description);
        }

        private void bind(Note note){
            if(note.getImageLoc()!=null)
                Glide.with(context).load(note.getImageLoc()).into(image);
            if(note.isHeart()){
                heart.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_on));
                heart.setColorFilter(context.getResources().getColor(R.color.valencia));
            }
            title.setText(note.getTitle());
            description.setText(note.getDescription());
            String nDate = String.format("%02d/%02d/%d", note.getDate().getDate(), note.getDate().getMonth()+1, note.getDate().getYear());
            String nDay = android.text.format.DateFormat.format("EEEE", note.getDate().getDate()).toString();
            date.setText(nDate+", "+nDay);

            setLocation(note, location, distance);
        }
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_list_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bind(notes.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(position);
            }
        });

        holder.heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!notes.get(position).isHeart()){
                    holder.heart.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_on));
                    holder.heart.setColorFilter(context.getResources().getColor(R.color.valencia));
                    listener.onHeartClicked(position,true);
                }else {
                    holder.heart.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_off));
                    holder.heart.setColorFilter(context.getResources().getColor(R.color.grey1));
                    listener.onHeartClicked(position,false);
                }
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void setLocation(Note note, TextView locationTv, TextView distanceTv){
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                client.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
                    @Override
                    public void onComplete(@NonNull Task<android.location.Location> task) {
                        android.location.Location location = task.getResult();
                        if (location != null) {
                            try {
                                /*set location*/
                                Location note_location = new Location("A");
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(note.getLocation().getLatitude(), note.getLocation().getLongitude(), 1);
                                note_location.setLongitude(note.getLocation().getLongitude());
                                note_location.setLatitude(note.getLocation().getLatitude());
                                locationTv.setText(addresses.get(0).getAddressLine(0));

                                /*set cur location*/
                                Location cur_location = new Location("B");
                                geocoder = new Geocoder(context, Locale.getDefault());
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                cur_location.setLongitude(addresses.get(0).getLongitude());
                                cur_location.setLatitude(addresses.get(0).getLatitude());

                                float fDistance = cur_location.distanceTo(note_location)/1000;
                                String sDistance = "0";
                                if (fDistance>0)
                                    sDistance = String.format("%.2f", fDistance);

                                distanceTv.setText(sDistance+" "+context.getResources().getString(R.string.from_you));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

}
