package com.example.myplaces.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;

import com.example.myplaces.models.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "notes_table";

    private SQLiteDatabase db;

    public DatabaseHelper(Context context){
        super(context, "noteDB.sql", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_CMD = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, date TEXT NOT NULL, location TEXT NOT NULL, description TEXT, image TEXT, heart TEXT);";
        db.execSQL(CREATE_TABLE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE "+TABLE_NAME);
        onCreate(db);
    }

    public void addNote(Note note, int position){
        String nDate = String.format("%02d/%02d/%d", note.getDate().getDate(), note.getDate().getMonth(), note.getDate().getYear());
        String nLocation = note.getLocation().getLongitude()+","+note.getLocation().getLatitude();

        db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("title", note.getTitle());
        contentValues.put("date", nDate);
        contentValues.put("location", nLocation);
        contentValues.put("description", note.getDescription());
        contentValues.put("image", note.getImageLoc().toString());
        contentValues.put("heart", String.valueOf(note.isHeart()));

        if(position<0){
            db.insert(TABLE_NAME, null, contentValues);
        }else{
            db.update(TABLE_NAME, contentValues, "id = "+(position+1), null);
        }
    }

    public List<Note> getNotes(){
        List<Note> notes = new ArrayList<>();

        db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        int indexId = cursor.getColumnIndex("id");
        int indexTitle = cursor.getColumnIndex("title");
        int indexDate = cursor.getColumnIndex("date");
        int indexLocation = cursor.getColumnIndex("location");
        int indexDescription = cursor.getColumnIndex("description");
        int indexImage = cursor.getColumnIndex("image");
        int indexHeart = cursor.getColumnIndex("heart");

        while (cursor.moveToNext()) {
            Note note = new Note();
            Date nDate = new Date();
            Location location = new Location("loc");

            /*date*/
            String sDate = cursor.getString(indexDate);
            String[] sDates = sDate.split("/");
            nDate.setDate(Integer.parseInt(sDates[0]));
            nDate.setMonth(Integer.parseInt(sDates[1]));
            nDate.setYear(Integer.parseInt(sDates[2]));

            /*location*/
            String sLocation = cursor.getString(indexLocation);
            String[] sLocations = sLocation.split(",");
            location.setLongitude(Double.parseDouble(sLocations[0]));
            location.setLatitude(Double.parseDouble(sLocations[1]));

            /*set note*/
            note.setTitle(cursor.getString(indexTitle));
            note.setDate(nDate);
            note.setLocation(location);
            note.setDescription(cursor.getString(indexDescription));
            note.setImageLoc(Uri.parse(cursor.getString(indexImage)));
            note.setHeart(Boolean.valueOf(cursor.getString(indexHeart)));

            notes.add(note);
        }

        cursor.close();
        db.close();
        return notes;
    }

    public void deleteNote(int position){
        db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = " + (position+1), null);
    }

    public void updateHeart(int position, boolean turnOn){
        db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("heart", turnOn);

        db.update(TABLE_NAME, contentValues, "id = "+(position+1), null);
    }

}
