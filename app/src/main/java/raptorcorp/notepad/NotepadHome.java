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

    private static EditText notes,title;
    private static FloatingActionButton mainFab,saveFab,shareFab,deleteFab;
    private static Boolean isFabOpen = false;
    private static Animation fab_open, fab_close, rotate_forward, rotate_backward;
    private static Intent shareIntent;
    private static ImageView importantOffButton,importantOnButton;
    private static String notesData,titleData;
    private static boolean importantEnabled = false;
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
        if(importantEnabled) {
            authenticateApp();
        }
         /**Requesting external storage permission.*/
        int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check == PackageManager.PERMISSION_GRANTED) {
        } else {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(NotepadHome.this, PERMISSIONS, PERMISSION_ALL);
        }

         /**Database Creation Start*/
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
    }

    private void authenticateApp() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Create an intent to open device screen lock screen to authenticate
            //Pass the Screen Lock screen Title and Description
            Intent i = keyguardManager.createConfirmDeviceCredentialIntent(getResources().getString(R.string.unlock), getResources().getString(R.string.confirm_pattern));
            try {
                //Start activity for result
                startActivityForResult(i, LOCK_REQUEST_CODE);
            } catch (Exception e) {

                //If some exception occurs means Screen lock is not set up please set screen lock
                //Open Security screen directly to enable patter lock
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                try {

                    //Start activity for result
                    startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE);
                } catch (Exception ex) {

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

                } else {
                }
                break;
            case SECURITY_SETTING_REQUEST_CODE:
                //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    Toast.makeText(this, getResources().getString(R.string.device_is_secure), Toast.LENGTH_SHORT).show();
                    authenticateApp();
                } else {
                    //If screen lock is not enabled just update text
                }

                break;
        }
    }

    /**
     * method to return whether device has screen lock enabled or not
     **/
    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        //this method only work whose api level is greater than or equal to Jelly_Bean (16)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && keyguardManager.isKeyguardSecure();

        //You can also use keyguardManager.isDeviceSecure(); but it requires API Level 23

    }

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
        if(importantEnabled == false){
            title.setText(null);
            notes.setText(null);
        } else {
            Toast.makeText(NotepadHome.this,"Important is enabled, disable it if u wish to delete the file" , Toast.LENGTH_LONG).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void importantOffOnClick(View view){
        importantOffButton.setVisibility(View.INVISIBLE);
        importantOnButton.setVisibility(View.VISIBLE);
        importantOnButton.setClickable(true);
        importantOffButton.setClickable(false);
        title.setTranslationZ(-7);
        title.setTextColor(getResources().getColor(R.color.colorAccent));
        importantEnabled =true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void importantOnOnClick(View view){
        importantOffButton.setVisibility(View.VISIBLE);
        importantOnButton.setVisibility(View.INVISIBLE);
        importantOffButton.setClickable(true);
        importantOnButton.setClickable(false);
        title.setTranslationZ(0);
        title.setTextColor(getResources().getColor(R.color.Black));
        importantEnabled = false;
    }

    public void onMainFabClick(View view){
        animateFAB();
    }

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
