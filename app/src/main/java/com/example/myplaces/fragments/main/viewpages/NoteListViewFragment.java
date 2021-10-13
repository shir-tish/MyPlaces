package com.example.myplaces.fragments.main.viewpages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplaces.R;
import com.example.myplaces.adapters.NotesListAdapter;
import com.example.myplaces.database.DatabaseHelper;
import com.example.myplaces.models.Note;


import java.util.ArrayList;
import java.util.List;


public class NoteListViewFragment extends Fragment {

    /*visuals*/
    RecyclerView notesRecycler;
    TextView noNotesTv;

    /*data*/
    List<Note> notes = new ArrayList<>();
    DatabaseHelper databaseHelper;
    NotesListAdapter notesListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list_view, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        databaseHelper = new DatabaseHelper(getContext());

        notesRecycler = view.findViewById(R.id.recycle_note_list);
        noNotesTv = view.findViewById(R.id.no_notes_tv);

        setNotesRecycler();
    }

    private void setNotesRecycler(){
        notesRecycler.setHasFixedSize(true);
        notesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        setNotes();
    }

    private void setNotes(){
        notes.clear();
        notes = databaseHelper.getNotes();

        if(notes.size()>0) {
            notesRecycler.setVisibility(View.VISIBLE);
            noNotesTv.setVisibility(View.GONE);

            notesListAdapter = new NotesListAdapter(getContext(), notes);
            notesListAdapter.setListener(new NotesListAdapter.NotesListListener() {
                @Override
                public void onItemClicked(int position) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putString("note_title", notes.get(position).getTitle());
                    bundle.putString("note_description", notes.get(position).getDescription());
                    bundle.putBoolean("note_heart", notes.get(position).isHeart());
                    bundle.putSerializable("note_date", notes.get(position).getDate());
                    bundle.putParcelable("note_location", notes.get(position).getLocation());
                    bundle.putParcelable("note_image", notes.get(position).getImageLoc());

                    Navigation.findNavController(notesRecycler).navigate(R.id.action_main_to_note, bundle);
                }

                @Override
                public void onHeartClicked(int position, boolean turnOn) {
                    databaseHelper.updateHeart(position, turnOn);
                    notes.get(position).setHeart(turnOn);
                }

                @Override
                public void onDeleteClicked(int position) {
                    openDeleteDialog(position);
                }
            });
            notesRecycler.setAdapter(notesListAdapter);
        }else{
            notesRecycler.setVisibility(View.GONE);
            noNotesTv.setVisibility(View.VISIBLE);
        }
    }

    private void openDeleteDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getResources().getString(R.string.delete_note_title)+notes.get(position).getTitle());
        builder.setMessage(getContext().getResources().getString(R.string.delete_note_message)
                +getContext().getResources().getString(R.string.quots)
                +notes.get(position).getTitle()+getContext().getResources().getString(R.string.quots));
        builder.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                databaseHelper.deleteNote(position);

                notes.remove(position);
                notesListAdapter.notifyDataSetChanged();
                //notesListAdapter.notifyItemRemoved(position);

                if(notes.size()>0) {
                    notesRecycler.setVisibility(View.VISIBLE);
                    noNotesTv.setVisibility(View.GONE);
                }else{
                    notesRecycler.setVisibility(View.GONE);
                    noNotesTv.setVisibility(View.VISIBLE);
                }

                dialog.dismiss();
            }
        }).setNegativeButton(getContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }




}