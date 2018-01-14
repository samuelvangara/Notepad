package raptorcorp.notepad;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class ContactusActivity extends AppCompatActivity {

    private static ImageButton ContactUsSendButton;
    private static EditText FeedBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);
        FeedBack = findViewById(R.id.feedBack);
        ContactUsSendButton = findViewById(R.id.ContactUsSendButton);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void ContactUsSendButtonOnClick(View view){
        ContactUsSendButton.setTranslationZ(-12);
        ContactUsSendButton.setTranslationZ(0);
            Toast.makeText(ContactusActivity.this, "Thank you for your feedback.", Toast.LENGTH_SHORT).show();
            Intent getBackToNotepadHome = new Intent(this,NotepadHome.class);
            startActivity(getBackToNotepadHome);
    }
}
