package raptorcorp.notepad;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class NotepadHome extends AppCompatActivity implements GestureDetector.OnGestureListener {

    public EditText notes, title;
    public static float x1, x2;
    public static FloatingActionButton mainFab, saveFab, shareFab, deleteFab;
    public static Boolean isFabOpen = false;
    public static Animation fab_open, fab_close, rotate_forward, rotate_backward, slide_to_right, slide_to_left, slide_from_right, slide_from_left;
    public static ImageView importantOffButton, importantOnButton;
    public static String notesData, titleData;
    public int importantEnabled;
    public SQLiteDatabase sqLiteDatabase;
    public static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final int LOCK_REQUEST_CODE = 221;
    public static final int SECURITY_SETTING_REQUEST_CODE = 233;
    public static final int FILE_RESULT_CODE = 1;
    public static Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_home);

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

        /**Requesting external storage permission.*/
        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(NotepadHome.this, PERMISSIONS, PERMISSION_ALL);
        }

        /**Database Creation. DbName:Notepad.db,TableName:TitleNotes*/
        sqLiteDatabase = openOrCreateDatabase("Notepad.db", Context.MODE_PRIVATE, null);
        sqLiteDatabase.setVersion(1);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS TitleNotes(title VARCHAR , notes VARCHAR , importantEnabled VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS NotesMetaData(title VARCHAR UNIQUE, notes VARCHAR , importantEnabled VARCHAR);");
        Cursor titleCursor = sqLiteDatabase.rawQuery("Select title from TitleNotes", null);
        Cursor notesCursor = sqLiteDatabase.rawQuery("Select notes from TitleNotes", null);
        Cursor importantEnabledCursor = sqLiteDatabase.rawQuery("Select importantEnabled from TitleNotes", null);
        if (importantEnabledCursor.moveToFirst()) {
            do {
                importantEnabled = Integer.parseInt(importantEnabledCursor.getString(0));
            } while (importantEnabledCursor.moveToNext());
        }

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
                titleData = title.getText().toString().replace("'", "''");
                notesData = notes.getText().toString().replace("'", "''");
                sqLiteDatabase.execSQL("INSERT INTO TitleNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "'" + "," + "'" + importantEnabled + "'" + ");");
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
                titleData = title.getText().toString().replace("'", "''");
                notesData = notes.getText().toString().replace("'", "''");
                sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "'" + "," + "'" + importantEnabled + "'" + ");");
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        /** Important Enabled. */
        if (importantEnabled == 1) {
            authenticateApp();
            importantIsOned();
        } else {
            importantIsOffed();
        }
    }

    /**
     * Important Off button functionality
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void importantOffOnClick(View view) {
        importantIsOned();
        importantEnabled = 1;
        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
        Toast.makeText(this, getResources().getString(R.string.important_Enabled), Toast.LENGTH_SHORT).show();
    }

    /**
     * Important On button functionality
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void importantOnOnClick(View view) {
        importantIsOffed();
        importantEnabled = 0;
        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
        Toast.makeText(this, getResources().getString(R.string.important_Disabled), Toast.LENGTH_SHORT).show();
    }

    /**
     * Fingerprint authentication.
     */
    public void authenticateApp() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent i = keyguardManager.createConfirmDeviceCredentialIntent(getResources().getString(R.string.unlock), getResources().getString(R.string.confirm_pattern));
            try {
                startActivityForResult(i, LOCK_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                Toast.makeText(this, getResources().getString(R.string.setting_label), Toast.LENGTH_SHORT).show();
                try {
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * method to return the result of the authentication.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, getResources().getString(R.string.unlock_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.unlock_failed), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    System.exit(0);
                }
                break;
            case SECURITY_SETTING_REQUEST_CODE:
                if (isDeviceSecure()) {
                    Toast.makeText(this, getResources().getString(R.string.device_is_secure), Toast.LENGTH_SHORT).show();
                    authenticateApp();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.security_device_cancelled), Toast.LENGTH_SHORT).show();
                }
                break;
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
                        Toast.makeText(NotepadHome.this, "There seems to be an issue. Please contact us.", Toast.LENGTH_SHORT).show();
                    }
                    importantIsOffed();
                    importantEnabled = 0;
                    if (titleName.isEmpty()) {
                        title.setText("");
                        notes.setText(text);
                    } else {
                        title.setText(titleName);
                        notes.setText(text);
                    }
                }
                break;
        }
    }

    /**
     * method to return whether device has screen lock enabled or not.
     */
    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure();
    }

    /**
     * Delete Fab button functionality
     */
    public void onDeleteFabClick(View view) {
        isFabOpen();
        if (importantEnabled == 0) {
            sqLiteDatabase.execSQL("DELETE FROM NotesMetaData WHERE title=" + "'" + title.getText().toString().replace("'", "''") + "'" + " AND notes=" + "'" + notes.getText().toString().replace("'", "''") + "';");
            sqLiteDatabase.execSQL("DELETE FROM TitleNotes WHERE title=" + "'" + title.getText().toString().replace("'", "''") + "'" + " AND notes=" + "'" + notes.getText().toString().replace("'", "''") + "'" + ";");
            title.setText("");
            notes.setText("");
        } else {
            Toast.makeText(NotepadHome.this, "Important is enabled, disable it if u wish to delete the file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Main Fab button functionality
     */
    public void onMainFabClick(View view) {
        animateFAB();
    }

    /**
     * Share Fab button functionality
     */
    public void onShareFabClick(View view) {
        isFabOpen();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (title.getText().toString().isEmpty()) {
            calendar = Calendar.getInstance();
            String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "   " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, calFormat + "\n" + "----------------" + "\n" + notes.getText().toString() + "\n" + "----------------");
            startActivity(Intent.createChooser(shareIntent, "Share With: "));
        } else {
            shareIntent.putExtra(Intent.EXTRA_TEXT, title.getText().toString() + "\n" + "----------------" + "\n" + notes.getText().toString() + "\n" + "----------------");
            startActivity(Intent.createChooser(shareIntent, "Share With: "));
        }
    }

    /**
     * Save Fab button functionality
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void onSaveFabClick(View view) {
        try {
            isFabOpen();
            if (title.getText().toString().isEmpty()) {
                calendar = Calendar.getInstance();
                String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + ":" + (calendar.get(Calendar.MONTH) + 1) + ":" + calendar.get(Calendar.YEAR) + ":" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                File notesFile = new File(path.toString() + "/Notepad", calFormat + "." + "txt");
                notesFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(notesFile, false);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.append(notes.getText().toString());
                outputStreamWriter.close();
                fileOutputStream.close();
                Toast.makeText(NotepadHome.this, "Your data has been Saved to " + calFormat + "." + "txt in your downloads->Notepad folder", Toast.LENGTH_SHORT).show();

            } else {
                File notesFile = new File(path + "/Notepad", title.getText().toString().replaceAll("\\s", "") + "." + "txt");
                notesFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(notesFile, false);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
                outputStreamWriter.append(notes.getText().toString());
                outputStreamWriter.close();
                fileOutputStream.close();
                Toast.makeText(NotepadHome.this, "Your data has been Saved to " + title.getText().toString().replaceAll("\\s", "") + "." + "txt in your downloads->Notepad folder", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(NotepadHome.this, e.toString() + " please notify us about the issue. ", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Menu functionality
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notepad_home, menu);
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
            case R.id.add:
                isFabOpen();
                if (title.getText().toString().isEmpty()) {
                    if (notes.getText().toString().isEmpty()) {
                        Toast.makeText(NotepadHome.this, "This is a new note!", Toast.LENGTH_SHORT).show();
                    } else {
                        calendar = Calendar.getInstance();
                        String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "   " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                        sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        importantEnabled = 0;
                        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                        title.setText("");
                        notes.setText("");
                        importantIsOffed();
                    }
                } else {
                    sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                    importantEnabled = 0;
                    sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                    Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                    title.setText("");
                    notes.setText("");
                    importantIsOffed();
                }
                break;
            case R.id.notepadList:
                if (title.getText().toString().isEmpty()) {
                    if (notes.getText().toString().isEmpty()) {
                        if (importantEnabled == 0) {
                            Intent listIntent = new Intent(this, NotepadListHome.class);
                            startActivity(listIntent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        } else {
                            importantEnabled = 0;
                            sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                            Intent listIntent = new Intent(this, NotepadListHome.class);
                            startActivity(listIntent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        }
                    } else {
                        calendar = Calendar.getInstance();
                        String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "   " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                        sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        importantEnabled = 0;
                        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        Intent listIntent = new Intent(this, NotepadListHome.class);
                        startActivity(listIntent);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                    importantEnabled = 0;
                    sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                    Intent listIntent = new Intent(this, NotepadListHome.class);
                    startActivity(listIntent);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On Back Pressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
     * Fab Animations
     */
    public void animateFAB() {
        if (isFabOpen) {
            mainFab.startAnimation(rotate_backward);
            saveFab.startAnimation(fab_close);
            shareFab.startAnimation(fab_close);
            deleteFab.startAnimation(fab_close);
            saveFab.setClickable(false);
            shareFab.setClickable(false);
            deleteFab.setClickable(false);
            isFabOpen = false;
        } else {
            mainFab.startAnimation(rotate_forward);
            saveFab.startAnimation(fab_open);
            shareFab.startAnimation(fab_open);
            deleteFab.startAnimation(fab_open);
            saveFab.setClickable(true);
            shareFab.setClickable(true);
            deleteFab.setClickable(true);
            isFabOpen = true;
        }
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
     * Important button is oned
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void importantIsOned() {
        importantOffButton.setVisibility(View.INVISIBLE);
        importantOnButton.setVisibility(View.VISIBLE);
        importantOnButton.setClickable(true);
        importantOffButton.setClickable(false);
        title.setTranslationZ(-7);
        title.setBackgroundColor(getResources().getColor(R.color.Red));
        title.setHintTextColor(getResources().getColor(R.color.White));
        title.setTextColor(getResources().getColor(R.color.White));
    }

    /**
     * Important button is offed
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void importantIsOffed() {
        importantOffButton.setVisibility(View.VISIBLE);
        importantOnButton.setVisibility(View.INVISIBLE);
        importantOffButton.setClickable(true);
        importantOnButton.setClickable(false);
        title.setTranslationZ(0);
        title.setBackgroundColor(getResources().getColor(R.color.White));
        title.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
        title.setTextColor(getResources().getColor(R.color.Black));
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
                    if (title.getText().toString().isEmpty()) {
                        if (notes.getText().toString().isEmpty()) {
                            if (importantEnabled == 0) {
                                Intent listIntent = new Intent(this, NotepadListHome.class);
                                startActivity(listIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            } else {
                                importantEnabled = 0;
                                sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                                Intent listIntent = new Intent(this, NotepadListHome.class);
                                startActivity(listIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        } else {
                            calendar = Calendar.getInstance();
                            String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "   " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                            sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                            importantEnabled = 0;
                            sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                            Intent listIntent = new Intent(this, NotepadListHome.class);
                            startActivity(listIntent);
                            finish();
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        importantEnabled = 0;
                        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        Intent listIntent = new Intent(this, NotepadListHome.class);
                        startActivity(listIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                    }
                }
                if (x1 > x2) {
                    if (title.getText().toString().isEmpty()) {
                        if (notes.getText().toString().isEmpty()) {
                            if (importantEnabled == 0) {
                                Intent listIntent = new Intent(this, NotepadListHome.class);
                                startActivity(listIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            } else {
                                importantEnabled = 0;
                                sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                                Intent listIntent = new Intent(this, NotepadListHome.class);
                                startActivity(listIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            }
                        } else {
                            calendar = Calendar.getInstance();
                            String calFormat = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR) + "   " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
                            sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                            importantEnabled = 0;
                            sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + calFormat + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                            Intent listIntent = new Intent(this, NotepadListHome.class);
                            startActivity(listIntent);
                            finish();
                            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        sqLiteDatabase.execSQL("INSERT or replace INTO NotesMetaData VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        importantEnabled = 0;
                        sqLiteDatabase.execSQL("INSERT or replace INTO TitleNotes VALUES('" + title.getText().toString().replace("'", "''") + "'" + "," + "'" + notes.getText().toString().replace("'", "''") + "'" + "," + "'" + importantEnabled + "'" + ");");
                        Intent listIntent = new Intent(this, NotepadListHome.class);
                        startActivity(listIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                        Toast.makeText(NotepadHome.this, "Notes saved", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
        return false;
    }
}
