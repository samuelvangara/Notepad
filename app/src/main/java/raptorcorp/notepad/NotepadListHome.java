package raptorcorp.notepad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class NotepadListHome extends AppCompatActivity implements GestureDetector.OnGestureListener {

    SwipeMenuListView notepadList;
    ArrayList<String> notepadListArray;
    ArrayAdapter<String> notepadAdapter;
    public NotepadDAO notepadListDAO = new NotepadDAO();
    private AdView notepadListAdView;
    public EditText notes, title;
    public static float x1, x2;
    public FloatingActionButton mainFab, saveFab, shareFab, deleteFab;
    public static Boolean isFabOpen = false;
    public static Animation fab_open, fab_close, rotate_forward, rotate_backward, slide_to_right, slide_to_left, slide_from_right, slide_from_left;
    public ImageView importantOffButton, importantOnButton;
    public int importantEnabled;
    public static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final int FILE_RESULT_CODE = 1;
    String titleTest,notesTest;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_list_home);

        /** NotepadList Ad */
        /**  MobileAds.initialize(this, "ca-app-pub-8428592077917623~9528816950");
        notepadListAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        notepadListAdView.loadAd(adRequest);*/

        /** Label Definitions*/
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
        slide_to_right = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_to_right);
        slide_to_left = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_to_left);
        slide_from_right = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_right);
        slide_from_left = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_left);
        importantOffButton = findViewById(R.id.importantOff);
        importantOnButton = findViewById(R.id.importantOn);
        notes = findViewById(R.id.notes);
        title = findViewById(R.id.theTitle);
        mainFab = findViewById(R.id.mainFab);
        saveFab = findViewById(R.id.saveFab);
        shareFab = findViewById(R.id.shareFab);
        deleteFab = findViewById(R.id.deleteFab);
        notepadList = findViewById(R.id.notepadListView);
        Context context = getApplicationContext();
        notepadListArray = new ArrayList<>();
        notepadListDAO.DbAndTableCreation(context);
        Cursor retrieveListCursor = notepadListDAO.SelectAllFromNotesMetaData();
        if (retrieveListCursor.moveToFirst()) {
            do {
                notepadListArray.add(retrieveListCursor.getString(0));
            } while (retrieveListCursor.moveToNext());
        }
        notepadAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notepadListArray);
        notepadList.setAdapter(notepadAdapter);
        notepadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String retrieve = notepadListArray.get(i);
                Cursor titleCursor = notepadListDAO.SelectTitleFromNotesMetaDataBasedOnRetrieve(retrieve);
                Cursor notesCursor = notepadListDAO.SelectNotesFromNotesMetaDataBasedOnRetrieve(retrieve);
                Cursor importantEnabledCursor = notepadListDAO.SelectImportantEnabledFromNotesMetaDataBasedOnRetrieve(retrieve);
                if (titleCursor.moveToFirst()) {
                    do {
                         titleTest = titleCursor.getString(0);
                    } while (titleCursor.moveToNext());
                }
                if (notesCursor.moveToFirst()) {
                    do {
                        notesTest = notesCursor.getString(0);
                    } while (notesCursor.moveToNext());
                }
                if (importantEnabledCursor.moveToFirst()) {
                    do {
                        importantEnabled = Integer.parseInt(importantEnabledCursor.getString(0));
                    } while (importantEnabledCursor.moveToNext());
                }
                if (importantEnabled == 1) {
                    Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                    homeIntent.putExtra("title", titleTest);
                    homeIntent.putExtra("notes", notesTest);
                    homeIntent.putExtra("importantEnabled", importantEnabled);
                    notepadListDAO.InsertIntoTitleNotes(titleTest.replace("'", "''"), notesTest.replace("'", "''"), importantEnabled);
                    notepadListDAO.InsertIntoNotesMetaData(titleTest.replace("'", "''"), notesTest.replace("'", "''"), importantEnabled);
                    overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
                    startActivity(homeIntent);
                    finish();
                    isFabOpen();
                } else {
                    Intent homeIntent = new Intent(NotepadListHome.this, NotepadHome.class);
                    homeIntent.putExtra("title", titleTest);
                    homeIntent.putExtra("notes", notesTest);
                    homeIntent.putExtra("importantEnabled", importantEnabled);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
                    startActivity(homeIntent);
                    finish();
                    isFabOpen();
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
                        Cursor importantEnabledCursor = notepadListDAO.SelectImportantEnabledFromNotesMetaDataBasedOnRetrieve(deleteCheck);
                        if (importantEnabledCursor.moveToFirst()) {
                            do {
                                importantEnabled = Integer.parseInt(importantEnabledCursor.getString(0));
                            } while (importantEnabledCursor.moveToNext());
                        }
                        if (importantEnabled == 1) {
                            Toast.makeText(NotepadListHome.this, "Important is enabled, disable it if u wish to delete the file", Toast.LENGTH_SHORT).show();
                        } else {
                            notepadListDAO.DeleteFromTitleNotesBasedOnTitle(notepadListArray.get(position).replace("'", "''"));
                            notepadListDAO.DeleteFromNotesMetaDataBasedOnTitle(notepadListArray.get(position).replace("'", "''"));
                            Intent addIntent = new Intent(NotepadListHome.this, NotepadListHome.class);
                            addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(addIntent);
                            finish();
                        }

                        break;
                }
                return false;
            }
        });
    }

    /**
     * method to return the result of the List browse file.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /** File browse and picker */
            case FILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String FilePath = data.getData().getPath();
                    File newFile = new File(FilePath);
                    String fileName = newFile.getName();
                    String titleName = fileName.substring(0, fileName.indexOf("."));
                    String filePath = newFile.getAbsolutePath().replaceAll(".*:", "");
                    String finalFilePath = filePath.substring(0, filePath.lastIndexOf(File.separator));
                    File file = new File(finalFilePath, fileName);
                    StringBuilder text = new StringBuilder();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    } catch (IOException e) {
                        Toast.makeText(NotepadListHome.this, "There seems to be an issue. Please contact us.", Toast.LENGTH_SHORT).show();
                    }
                    Intent browseIntent = new Intent(this, NotepadHome.class);
                    browseIntent.putExtra("Browsetitle",titleName);
                    browseIntent.putExtra("Browsenotes",text.toString());
                    browseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(browseIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                }
                break;
        }
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
                Intent AboutNotepadIntent = new Intent(this, AboutNotepad.class);
                startActivity(AboutNotepadIntent);
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
                addIntent.putExtra("new","");
                addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(addIntent);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * To close fab whenever open
     */
    public void isFabOpen() {
        if (isFabOpen) {
            mainFab.startAnimation(rotate_backward);
            saveFab.startAnimation(fab_close);
            shareFab.startAnimation(fab_close);
            deleteFab.startAnimation(fab_close);
            saveFab.setClickable(false);
            shareFab.setClickable(false);
            deleteFab.setClickable(false);
            isFabOpen = false;
        }
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                x1 = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = touchevent.getX();
                if (x1 < x2) {
                    isFabOpen();
                    Intent addIntent = new Intent(this, NotepadHome.class);
                    addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(addIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }
                if (x1 > x2) {
                    isFabOpen();
                    Intent addIntent = new Intent(this, NotepadHome.class);
                    addIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(addIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                }
                break;
            }
        }
        return false;
    }
}
