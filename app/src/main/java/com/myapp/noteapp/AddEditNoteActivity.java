package com.myapp.noteapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.myapp.noteapp.data.DatabaseHelper;
import com.myapp.noteapp.model.Note;
import com.myapp.noteapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditNoteActivity extends AppCompatActivity {
    private TextInputEditText titleInput, contentInput;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long noteId = -1;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views and helpers
        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Check if editing existing note
        noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            currentNote = databaseHelper.getNote(noteId);
            if (currentNote != null) {
                titleInput.setText(currentNote.getTitle());
                contentInput.setText(currentNote.getContent());
                getSupportActionBar().setTitle("Edit Note");
            }
        } else {
            getSupportActionBar().setTitle("New Note");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_save) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        boolean success;
        if (noteId == -1) {
            // Adding new note
            success = databaseHelper.addNote(title, content, sessionManager.getUserId());
        } else {
            // Updating existing note
            success = databaseHelper.updateNote(noteId, title, content);
        }

        if (success) {
            finish();
        } else {
            Toast.makeText(this, "Error saving note", Toast.LENGTH_SHORT).show();
        }
    }
} 