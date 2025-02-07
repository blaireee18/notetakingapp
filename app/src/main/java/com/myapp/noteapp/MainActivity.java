package com.myapp.noteapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.myapp.noteapp.adapter.NoteAdapter;
import com.myapp.noteapp.data.DatabaseHelper;
import com.myapp.noteapp.model.Note;
import com.myapp.noteapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initialize SessionManager and get userId
            sessionManager = new SessionManager(this);
            if (!sessionManager.isLoggedIn()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            userId = sessionManager.getUserId();

            // Setup Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Notes");
            }

            // Initialize DatabaseHelper
            databaseHelper = new DatabaseHelper(this);

            // Setup RecyclerView
            recyclerView = findViewById(R.id.notesRecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            noteAdapter = new NoteAdapter(this);
            recyclerView.setAdapter(noteAdapter);

            // Setup FAB
            com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fab = findViewById(R.id.addNoteFab);
            fab.setOnClickListener(view -> {
                try {
                    Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error creating new note: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                }
            });

            // Load notes
            loadNotes();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
        }
    }

    private void loadNotes() {
        try {
            if (databaseHelper != null && userId != -1) {
                List<Note> notes = databaseHelper.getNotesByUser(userId);
                if (noteAdapter != null) {
                    noteAdapter.setNotes(notes != null ? notes : new ArrayList<>());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading notes: " + e.getMessage(), 
                         Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, AddEditNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    @Override
    public void onNoteLongClick(Note note, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.note_menu);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_delete) {
                databaseHelper.deleteNote(note.getId());
                loadNotes();
                return true;
            } else if (itemId == R.id.action_toggle_pin) {
                databaseHelper.toggleNotePin(note.getId());
                loadNotes();
                return true;
            }
            return false;
        });
        popup.show();
    }
}