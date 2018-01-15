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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class NotepadHome extends AppCompatActivity {

    private EditText notes,title;
    private FloatingActionButton mainFab,saveFab,shareFab,deleteFab;
    private static Boolean isFabOpen = false;
    private static Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private Intent shareIntent;
    private ImageView importantOffButton,importantOnButton;
    private static String notesData,titleData;
    int importantEnabled;
    SQLiteDatabase sqLiteDatabase;
    private static File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final int LOCK_REQUEST_CODE = 221;
    private static final int SECURITY_SETTING_REQUEST_CODE = 233;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad_home);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
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

         /**Database Creation.*/
        sqLiteDatabase = openOrCreateDatabase("Notepad.db", Context.MODE_PRIVATE, null);
        sqLiteDatabase.setVersion(1);
        sqLiteDatabase.setLocale(Locale.getDefault());
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS TitleNotes(title VARCHAR , notes VARCHAR , importantEnabled VARCHAR);");
        Cursor titleCursor = sqLiteDatabase.rawQuery("Select title from TitleNotes", null);
        Cursor notesCursor = sqLiteDatabase.rawQuery("Select notes from TitleNotes", null);
        Cursor importantEnabledCursor = sqLiteDatabase.rawQuery("Select importantEnabled from TitleNotes", null);
        if (importantEnabledCursor.moveToFirst()) {
            do{
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
                titleData = title.getText().toString();
                notesData = notes.getText().toString();
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
                titleData = title.getText().toString();
                notesData = notes.getText().toString();
                sqLiteDatabase.execSQL("INSERT INTO TitleNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "'" + "," + "'" + importantEnabled + "'" +");");
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        /** Important Enabled. */
        if(importantEnabled == 1)
        {
            authenticateApp();
            importantOffOnClick(findViewById(R.id.importantOff));
        }
        else {
            importantOffOnClick(findViewById(R.id.importantOn));
        }
    }

    /** Important Off button functionality*/
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void importantOffOnClick(View view){
        importantOffButton.setVisibility(View.INVISIBLE);
        importantOnButton.setVisibility(View.VISIBLE);
        importantOnButton.setClickable(true);
        importantOffButton.setClickable(false);
        title.setTranslationZ(-7);
        title.setBackgroundColor(getResources().getColor(R.color.Red));
        title.setHintTextColor(getResources().getColor(R.color.White));
        title.setTextColor(getResources().getColor(R.color.White));
        importantEnabled =1;
        sqLiteDatabase.execSQL("INSERT INTO TitleNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "'" + "," + "'" + importantEnabled + "'" +");");
        Log.i("off",Integer.toString(importantEnabled));
    }

    /** Important On button functionality*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void importantOnOnClick(View view){
        importantOffButton.setVisibility(View.VISIBLE);
        importantOnButton.setVisibility(View.INVISIBLE);
        importantOffButton.setClickable(true);
        importantOnButton.setClickable(false);
        title.setTranslationZ(0);
        title.setBackgroundColor(getResources().getColor(R.color.White));
        title.setHintTextColor(getResources().getColor(R.color.colorPrimaryDark));
        title.setTextColor(getResources().getColor(R.color.Black));
        importantEnabled = 0;
        sqLiteDatabase.execSQL("INSERT INTO TitleNotes VALUES('" + titleData + "'" + "," + "'" + notesData + "'" + "," + "'" + importantEnabled + "'" +");");
        Log.i("on",Integer.toString(importantEnabled));
    }

    /** Fingerprint authentication. */
    private void authenticateApp() {
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
                } catch (Exception ex) {}
            }
        }
    }

    /** method to return the result of the authentication. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, getResources().getString(R.string.unlock_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.unlock_failed), Toast.LENGTH_SHORT).show();
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
        }
    }

    /** method to return whether device has screen lock enabled or not. */
    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure();
    }

    /** Delete Fab button functionality*/
    public void onDeleteFabClick(View view){
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
        if(importantEnabled==0){
            title.setText(null);
            notes.setText(null);
        } else {
            Toast.makeText(NotepadHome.this,"Important is enabled, disable it if u wish to delete the file" , Toast.LENGTH_LONG).show();
        }

    }

    /** Main Fab button functionality*/
    public void onMainFabClick(View view){
        animateFAB();
    }

    /** Share Fab button functionality*/
    public void onShareFabClick(View view){
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
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Title: "+title.getText().toString()+"\n"+"----------------"+"\n"+"Notes: "+notes.getText().toString());
        startActivity(Intent.createChooser(shareIntent,"Share With: "));
    }

    /** Save Fab button functionality*/
    public void onSaveFabClick(View view){
        try {
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
            File notesFile = new File(path, title.getText().toString().replaceAll("\\s", "")+"."+"txt");
            Log.i("test",title.getText().toString().replaceAll("\\s", "")+"."+"txt");
            FileOutputStream fileOutputStream = new FileOutputStream(notesFile, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.append("Title: "+title.getText().toString());
            outputStreamWriter.append("\n\r");
            outputStreamWriter.append("*************************");
            outputStreamWriter.append("\n\r");
            outputStreamWriter.append("Notes: "+notes.getText().toString());
            outputStreamWriter.append("\n\r");
            outputStreamWriter.append("*************************");
            outputStreamWriter.close();
            fileOutputStream.close();
            Toast.makeText(NotepadHome.this, "Your data has been Saved to "+ title.getText().toString().replaceAll("\\s", "")+"."+"txt in your downloads folder", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(NotepadHome.this, e.toString() + " please notify us about the issue. " , Toast.LENGTH_LONG).show();
        }

    }

    /** Menu functionality*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_notepad_home,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId())
        {
            case R.id.contactUs:
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
                Intent contactUsIntent = new Intent(this, ContactusActivity.class);
                startActivity(contactUsIntent);
                break;
        }
        return true;
    }

    /** Fab Animations */
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
}
