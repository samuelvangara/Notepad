package raptorcorp.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class NotepadListHome extends NotepadHome {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_list_home);
        ListView notepadList = findViewById(R.id.notepadListView);
        final ArrayList<String> notepadListArray = new ArrayList<>();

        Cursor titleCursors = sqLiteDatabase.rawQuery("Select title from NotesMetaData", null);
        if (titleCursors.moveToFirst()) {
            do {
                notepadListArray.add(titleCursors.getString(0));
            } while (titleCursors.moveToNext());
        }

        ArrayAdapter<String> notepadAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notepadListArray);
        notepadList.setAdapter(notepadAdapter);

        notepadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String retrieve = notepadListArray.get(i);
                Log.i("test", retrieve);
                Cursor titleCursor = sqLiteDatabase.rawQuery("Select title from NotesMetaData where title=" + "'" + retrieve + "'", null);
                Cursor notesCursor = sqLiteDatabase.rawQuery("Select notes from NotesMetaData where title=" + "'" + retrieve + "'", null);
                Cursor importantEnabledCursor = sqLiteDatabase.rawQuery("Select importantEnabled from NotesMetaData where title=" + "'" + retrieve + "'", null);
                if (titleCursor.moveToFirst()) {
                    do {
                        title.setText(titleCursor.getString(0));
                    } while (titleCursor.moveToNext());
                }
                if (notesCursor.moveToFirst()) {
                    do {
                        notes.setText(notesCursor.getString(0));
                    } while (notesCursor.moveToNext());
                }
                if (importantEnabledCursor.moveToFirst()) {
                    do {
                        importantEnabled = Integer.parseInt(importantEnabledCursor.getString(0));
                    } while (importantEnabledCursor.moveToNext());
                }
                Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                importantIsOned();
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);

            }
        });
    }
}
