package com.myapp.noteapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.myapp.noteapp.model.Note;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NotesDB";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Notes table
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_NOTE_ID = "id";
    private static final String COLUMN_NOTE_TITLE = "title";
    private static final String COLUMN_NOTE_CONTENT = "content";
    private static final String COLUMN_NOTE_TIMESTAMP = "timestamp";
    private static final String COLUMN_NOTE_PINNED = "pinned";
    private static final String COLUMN_NOTE_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";

        // Create notes table
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_TITLE + " TEXT,"
                + COLUMN_NOTE_CONTENT + " TEXT,"
                + COLUMN_NOTE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_NOTE_PINNED + " INTEGER DEFAULT 0,"
                + COLUMN_NOTE_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_NOTE_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String hashedPassword = hashPassword(password);
        String[] selectionArgs = {username, hashedPassword};
        
        // Add debug logging
        System.out.println("Attempting login for username: " + username);
        System.out.println("Hashed password: " + hashedPassword);
        
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);
        
        int count = cursor.getCount();
        cursor.close();
        
        // Add result logging
        System.out.println("Login result count: " + count);
        return count > 0;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Change to throw runtime exception instead of returning null
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public long getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, null);
        
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public List<Note> getNotesByUser(long userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_NOTES + 
                       " WHERE " + COLUMN_NOTE_USER_ID + " = ?" +
                       " ORDER BY " + COLUMN_NOTE_PINNED + " DESC, " +
                       COLUMN_NOTE_TIMESTAMP + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_PINNED)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_USER_ID))
                );
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    public boolean addNote(String title, String content, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);
        values.put(COLUMN_NOTE_USER_ID, userId);
        values.put(COLUMN_NOTE_PINNED, 0);
        
        long result = db.insert(TABLE_NOTES, null, values);
        return result != -1;
    }

    public boolean updateNote(long noteId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);
        
        int result = db.update(TABLE_NOTES, values,
                COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        return result > 0;
    }

    public boolean deleteNote(long noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTES,
                COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        return result > 0;
    }

    public boolean toggleNotePin(long noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First, get current pin status
        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_NOTE_PINNED},
                COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)},
                null, null, null);
        
        if (cursor.moveToFirst()) {
            int currentPinStatus = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOTE_PINNED, currentPinStatus == 0 ? 1 : 0);
            
            int result = db.update(TABLE_NOTES, values,
                    COLUMN_NOTE_ID + " = ?",
                    new String[]{String.valueOf(noteId)});
            cursor.close();
            return result > 0;
        }
        cursor.close();
        return false;
    }

    public Note getNote(long noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null,
                COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)},
                null, null, null);

        Note note = null;
        if (cursor.moveToFirst()) {
            note = new Note(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TIMESTAMP)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_PINNED)) == 1,
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_USER_ID))
            );
        }
        cursor.close();
        return note;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_EMAIL + "=?", new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
} 