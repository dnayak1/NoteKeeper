package dhiraj.com.realm;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by dhira on 24-04-2017.
 */

public class Note extends RealmObject {
    private String noteName;
    private int noteStatus,notePriority;
    private long noteId;
    Date noteUpdateDate;

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public int getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(int noteStatus) {
        this.noteStatus = noteStatus;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public int getNotePriority() {
        return notePriority;
    }

    public void setNotePriority(int notePriority) {
        this.notePriority = notePriority;
    }

    public Date getNoteUpdateDate() {
        return noteUpdateDate;
    }

    public void setNoteUpdateDate(Date noteUpdateDate) {
        this.noteUpdateDate = noteUpdateDate;
    }
}
