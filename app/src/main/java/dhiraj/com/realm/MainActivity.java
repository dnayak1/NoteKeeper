package dhiraj.com.realm;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements GridRecyclerAdapter.IGridListener {
    EditText editTextNotesTitle;
    Spinner spinnerPriority;
    Button buttonAdd;
    RecyclerView recyclerViewNotesList;
    String noteName,priority;
    int priorityValue;
    private Realm realm;
    private OrderedRealmCollection<Note> notes, pendingNotes,completedNotes,sortedByPriorityNotes,sortedByTimeNotes;
    GridRecyclerAdapter gridRecyclerAdapter;
    LinearLayoutManager layoutManager;
    String statusTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Note Keeper");
        editTextNotesTitle= (EditText) findViewById(R.id.editTextNoteTitle);
        spinnerPriority= (Spinner) findViewById(R.id.spinner);
        buttonAdd= (Button) findViewById(R.id.buttonAdd);
        recyclerViewNotesList= (RecyclerView) findViewById(R.id.recyclerViewNotedDetail);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.priority,android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
        Realm.init(getApplicationContext());
        realm=Realm.getDefaultInstance();
        /*realm.close();
        Realm.deleteRealm(realm.getConfiguration());*/
//        notes=realm.where(Note.class).findAll().sort("noteStatus");
        notes=realm.where(Note.class).findAll().sort("noteStatus",Sort.ASCENDING,"notePriority",Sort.ASCENDING);
        gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,notes,MainActivity.this);
        recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
        layoutManager=new LinearLayoutManager(this);
        recyclerViewNotesList.setLayoutManager(layoutManager);
        gridRecyclerAdapter.notifyDataSetChanged();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });
    }

    public void addNote(){
        noteName=editTextNotesTitle.getText().toString().trim();
        priority=spinnerPriority.getSelectedItem().toString();
        final int id=createId();
        if(noteName!=null && !noteName.isEmpty() && !priority.equals("priority")){
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Note note=realm.createObject(Note.class);
                    note.setNoteName(noteName);
                    if(priority.equals("Low")){
                        priorityValue=2;
                    }
                    else if (priority.equals("Medium")){
                        priorityValue=1;
                    }
                    else if(priority.equals("High")){
                        priorityValue=0;
                    }
                    note.setNotePriority(priorityValue);
                    note.setNoteStatus(0);
                    note.setNoteUpdateDate(new Date(System.currentTimeMillis()));
                    note.setNoteId(id);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Note Successfully Added", Toast.LENGTH_SHORT).show();
                    gridRecyclerAdapter.notifyDataSetChanged();
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(MainActivity.this, "Invalid data", Toast.LENGTH_SHORT).show();
        }
    }

    public int createId(){
        if(realm.where(Note.class).max("noteId")!=null){
            return realm.where(Note.class).max("noteId").intValue() +1;
        }
        else {
            return 1;
        }
    }

    @Override
    public void deleteNote(final Note note) {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Do you really want to delete the task");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Note deleteNote=realm.where(Note.class).equalTo("noteId",note.getNoteId()).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        deleteNote.deleteFromRealm();
                        gridRecyclerAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public void changeStatus(final Note note) {
        if(note.getNoteStatus()==1){
            statusTitle="Do you really want to mark it as pending";
        }
        else if(note.getNoteStatus()==0){
            statusTitle="Do you really want to mark it as completed";
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(statusTitle);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Note updateNote=realm.where(Note.class).equalTo("noteId",note.getNoteId()).findFirst();
                if(updateNote.getNoteStatus()==1){
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            updateNote.setNoteStatus(0);
                            updateNote.setNoteUpdateDate(new Date(System.currentTimeMillis()));
                            gridRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else{
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            updateNote.setNoteStatus(1);
                            updateNote.setNoteUpdateDate(new Date(System.currentTimeMillis()));
                            gridRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,notes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
            }
        });
        builder.create().show();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.showCompletedMenu:
                completedNotes=realm.where(Note.class).equalTo("noteStatus",1).findAll().sort("noteUpdateDate",Sort.DESCENDING);
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,completedNotes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
                return true;
            case R.id.showPendingMenu:
                pendingNotes=realm.where(Note.class).equalTo("noteStatus",0).findAll().sort("noteUpdateDate",Sort.DESCENDING);
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,pendingNotes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
                return true;
            case R.id.showAllNotesMenu:
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,notes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
                return true;
            case R.id.sortByTimeMenu:
                sortedByTimeNotes=realm.where(Note.class).findAll().sort("noteUpdateDate", Sort.DESCENDING);
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,sortedByTimeNotes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
                return true;
            case R.id.sortByPriorityMenu:
                gridRecyclerAdapter=new GridRecyclerAdapter(MainActivity.this,notes,MainActivity.this);
                recyclerViewNotesList.setAdapter(gridRecyclerAdapter);
                layoutManager=new LinearLayoutManager(MainActivity.this);
                recyclerViewNotesList.setLayoutManager(layoutManager);
                gridRecyclerAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
