package raptorcorp.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;

public class NotepadListHome extends NotepadHome implements GestureDetector.OnGestureListener {

    SwipeMenuListView notepadList;
    ArrayList<String> notepadListArray;
    ArrayAdapter<String> notepadAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_list_home);
        notepadList = findViewById(R.id.notepadListView);
        notepadListArray = new ArrayList<>();
        Cursor titleCursors = sqLiteDatabase.rawQuery("Select title from NotesMetaData", null);
        if (titleCursors.moveToFirst()) {
            do {
                notepadListArray.add(titleCursors.getString(0));
            } while (titleCursors.moveToNext());
        }
        notepadAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notepadListArray);
        notepadList.setAdapter(notepadAdapter);
        notepadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String retrieve = notepadListArray.get(i);
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
                if (importantEnabled == 1) {
                    Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                    sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                    overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
                    startActivity(homeIntent);
                } else {
                    Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
                    startActivity(homeIntent);
                }
            }
        });

        /** List Swipe */
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.WHITE));
                deleteItem.setWidth(100);
                deleteItem.setIcon(R.drawable.dustbin);
                menu.addMenuItem(deleteItem);
            }
        };
        notepadList.setMenuCreator(creator);
        notepadList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        String deleteCheck = notepadListArray.get(position);
                        Cursor importantEnabledCursor = sqLiteDatabase.rawQuery("Select importantEnabled from NotesMetaData where title=" + "'" + deleteCheck + "'", null);
                        if (importantEnabledCursor.moveToFirst()) {
                            do {
                                importantEnabled = Integer.parseInt(importantEnabledCursor.getString(0));
                            } while (importantEnabledCursor.moveToNext());
                        }
                        if (importantEnabled == 1) {
                            Intent addIntent = new Intent(NotepadListHome.this, NotepadListHome.class);
                            addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(addIntent);
                            Toast.makeText(NotepadListHome.this, "Important is enabled, disable it if u wish to delete the file", Toast.LENGTH_LONG).show();
                        } else {
                            sqLiteDatabase.execSQL("DELETE FROM NotesMetaData WHERE title=" + "'" + notepadListArray.get(position).replace("'", "''") + "';");
                            sqLiteDatabase.execSQL("DELETE FROM TitleNotes WHERE title=" + "'" + notepadListArray.get(position).replace("'", "''") + "';");
                             Intent addIntent = new Intent(NotepadListHome.this, NotepadListHome.class);
                            addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(addIntent);
                        }

                        break;
                }
                return false;
            }
        });
    }

    /** On Back Pressed*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Menu functionality
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notepad_list, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contactUs:
                isFabOpen();
                Intent contactUsIntent = new Intent(this, ContactusActivity.class);
                startActivity(contactUsIntent);
                break;
            case R.id.Browse:
                isFabOpen();
                Intent browseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                browseIntent.setType("text/plain");
                startActivityForResult(browseIntent, FILE_RESULT_CODE);
                break;
            case R.id.addlist:
                isFabOpen();
                Intent addIntent = new Intent(this, NotepadHome.class);
                addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                title.setText("");
                notes.setText("");
                importantIsOffed();
                importantEnabled = 0;
                startActivity(addIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gestures
     */

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x1 = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = touchevent.getX();
                if (x1 < x2) {
                    Intent listIntent = new Intent(NotepadListHome.this, NotepadListHome.class);
                    startActivity(listIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }
                if (x1 > x2) {
                    Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                    startActivity(homeIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                }
                break;
            }
        }
        return false;
    }

}
