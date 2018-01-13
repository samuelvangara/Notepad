package raptorcorp.notepad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import java.util.Locale;

public class NotepadHome extends AppCompatActivity {

    private static EditText notes,title;
    private static FloatingActionButton mainFab,saveFab,shareFab;
    private static Boolean isFabOpen = false;
    private static Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private static Intent shareIntent,saveIntent;
    private static String notesData,titleData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_home);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        notes = findViewById(R.id.notes);
        title = findViewById(R.id.theTitle);
        mainFab = findViewById(R.id.mainFab);
        saveFab = findViewById(R.id.saveFab);
        shareFab = findViewById(R.id.shareFab);

         /*
        Database Creation Start
         */
        final SQLiteDatabase sqLiteDatabase;
        sqLiteDatabase = openOrCreateDatabase("Notepad.db", Context.MODE_PRIVATE, null);
        sqLiteDatabase.setVersion(1);
        sqLiteDatabase.setLocale(Locale.getDefault());
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS TitleAndNotes(title VARCHAR , notes VARCHAR);");
        Cursor titleCursor = sqLiteDatabase.rawQuery("Select title from TitleAndNotes", null);
        Cursor notesCursor = sqLiteDatabase.rawQuery("Select notes from TitleAndNotes", null);
        if (titleCursor.moveToFirst()) {
            do {
                titleData = titleCursor.getString(0);
            } while (titleCursor.moveToNext());
        }

        if (notesCursor.moveToFirst()) {
            do {
                notesData = notesCursor.getString(0);
            } while (notesCursor.moveToNext());
        }

        title.setText(titleData);
        titleData = title.getText().toString();
        title.setSelection(title.getText().length());
        notes.setText(notesData);
        notesData = notes.getText().toString();
        notes.setSelection(notes.getText().length());

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                titleData = title.getText().toString();
                notesData = notes.getText().toString();
                sqLiteDatabase.execSQL("INSERT INTO TitleAndNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "');");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                     }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                titleData = title.getText().toString();
                notesData = notes.getText().toString();
                sqLiteDatabase.execSQL("INSERT INTO TitleAndNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "');");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /*
        Database Creation End
         */
    }

    public void onMainFabClick(View view){
        animateFAB();
    }

    public void onShareFabClick(View view){
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Title: "+title.getText().toString()+"\n"+"----------------"+"\n"+"Notes: "+notes.getText().toString());
        startActivity(Intent.createChooser(shareIntent,"Share With: "));
    }

    public void onSaveFabClick(View view){

    }

    public void animateFAB() {
        if (isFabOpen) {
            mainFab.startAnimation(rotate_backward);
            saveFab.startAnimation(fab_close);
            shareFab.startAnimation(fab_close);
            saveFab.setClickable(false);
            shareFab.setClickable(false);
            isFabOpen = false;
        } else {
            mainFab.startAnimation(rotate_forward);
            saveFab.startAnimation(fab_open);
            shareFab.startAnimation(fab_open);
            saveFab.setClickable(true);
            shareFab.setClickable(true);
            isFabOpen = true;
        }
    }
}
