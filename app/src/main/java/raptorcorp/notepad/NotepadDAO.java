package raptorcorp.notepad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Samuel Nitin Vangara on 01/22/2018.
 */

public class NotepadDAO {

    private SQLiteDatabase sqLiteDatabase;

    /** DROP TitleNotes */
    public void DropTitleNotes() {
        sqLiteDatabase.execSQL("DROP TABLE TitleNotes");
    }
    /** DROP NotesMetaData*/
    public void DropNotesMetaData() {
        sqLiteDatabase.execSQL("DROP TABLE NotesMetaData");
    }


    /**Database Creation. DbName:Notepad.db,TableName:TitleNotes;NotesMetaData*/
    public void DbAndTableCreation(Context context){
        sqLiteDatabase = context.openOrCreateDatabase("Notepad.db", Context.MODE_PRIVATE, null);
        sqLiteDatabase.setVersion(1);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS TitleNotes(title VARCHAR , notes VARCHAR , importantEnabled VARCHAR);");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS NotesMetaData(title VARCHAR UNIQUE, notes VARCHAR , importantEnabled VARCHAR);");
    }

    /**Select title from TitleNotes */
    public Cursor SelectTitleFromTitleNotes(){
        return sqLiteDatabase.rawQuery("Select title from TitleNotes;", null);
    }

    /**Select notes from TitleNotes */
    public Cursor SelectNotesFromTitleNotes(){
        return sqLiteDatabase.rawQuery("Select notes from TitleNotes;", null);
    }

    /**Select importantEnabled from TitleNotes */
    public Cursor SelectImportantEnabledFromTitleNotes(){
        return sqLiteDatabase.rawQuery("Select importantEnabled from TitleNotes;", null);
    }

    /**Select title from NotesMetaData*/
    public Cursor SelectTitleFromNotesMetaData(){
        return sqLiteDatabase.rawQuery("Select title from NotesMetaData", null);
    }

    /**Select notes from NotesMetaData*/
    public Cursor SelectNotesFromNotesMetaData(){
        return sqLiteDatabase.rawQuery("Select notes from NotesMetaData", null);
    }

    /**Select importantEnabled from NotesMetaData*/
    public Cursor SelectImportantEnabledFromNotesMetaData(){
        return sqLiteDatabase.rawQuery("Select importantEnabled from NotesMetaData", null);
    }

    /**Insert into NotesMetaData*/
    public void InsertIntoNotesMetaData(String title, String notes, int importantEnabled){
        sqLiteDatabase.execSQL("INSERT OR REPLACE INTO NotesMetaData VALUES('" + title + "','" + notes + "','" + importantEnabled + "');");
    }

    /**Insert into TitleNotes*/
    public void InsertIntoTitleNotes(String title, String notes, int importantEnabled){
        sqLiteDatabase.execSQL("INSERT OR REPLACE INTO TitleNotes VALUES('" + title + "','" + notes + "','" + importantEnabled + "');");
    }

    /**Delete from TitleNotes*/
    public void DeleteFromTitleNotes(String title, String notes){
        sqLiteDatabase.execSQL("DELETE FROM TitleNotes WHERE title='" + title + "' AND notes='" + notes + "';");
    }

    /**Delete from NotesMetaData*/
    public void DeleteFromNotesMetaData(String title, String notes){
        sqLiteDatabase.execSQL("DELETE FROM NotesMetaData WHERE title='" + title + "' AND notes='" + notes + "';");
    }

    /**Delete from TitleNotes based on Title*/
    public void DeleteFromTitleNotesBasedOnTitle(String title){
        sqLiteDatabase.execSQL("DELETE FROM TitleNotes WHERE title='" + title + "';");
    }

    /**Delete from NotesMetaData based on Title*/
    public void DeleteFromNotesMetaDataBasedOnTitle(String title){
        sqLiteDatabase.execSQL("DELETE FROM NotesMetaData WHERE title='" + title + "';");
    }

    /**Select all form NotesMetaData*/
    public Cursor SelectAllFromNotesMetaData() {
      return sqLiteDatabase.rawQuery("Select title from NotesMetaData", null);
    }

    /**Select Title for NotesMetaData based on retrieve Value*/
    public Cursor SelectTitleFromNotesMetaDataBasedOnRetrieve(String retrieve) {
      return sqLiteDatabase.rawQuery("Select title from NotesMetaData where title=" + "'" + retrieve + "'", null);
    }

    /**Select Notes for NotesMetaData based on retrieve Value*/
    public Cursor SelectNotesFromNotesMetaDataBasedOnRetrieve(String retrieve) {
        return sqLiteDatabase.rawQuery("Select notes from NotesMetaData where title=" + "'" + retrieve + "'", null);
    }

    /**Select Important Enabled for NotesMetaData based on retrieve Value*/
    public Cursor SelectImportantEnabledFromNotesMetaDataBasedOnRetrieve(String retrieve) {
        return sqLiteDatabase.rawQuery("Select importantEnabled from NotesMetaData where title=" + "'" + retrieve + "'", null);
    }

    /**Select Title for NotesMetaData based on retrieve Value*/
    public Cursor SelectTitleFromTitleNotesBasedOnRetrieve(String retrieve) {
        return sqLiteDatabase.rawQuery("Select title from TitleNotes where title=" + "'" + retrieve + "'", null);
    }

    /**Select Notes for NotesMetaData based on retrieve Value*/
    public Cursor SelectNotesFromTitleNotesBasedOnRetrieve(String retrieve) {
        return sqLiteDatabase.rawQuery("Select notes from TitleNotes where title=" + "'" + retrieve + "'", null);
    }

    /**Select Important Enabled for NotesMetaData based on retrieve Value*/
    public Cursor SelectImportantEnabledTitleNotesBasedOnRetrieve(String retrieve) {
        return sqLiteDatabase.rawQuery("Select importantEnabled from TitleNotes where title=" + "'" + retrieve + "'", null);
    }

    /**Select Notes for NotesMetaData based on retrieve Value*/
    public Cursor SelectimportantEnabledFromNotesMetaData() {
        return sqLiteDatabase.rawQuery("Select importantEnabled from NotesMetaData", null);
    }
}
