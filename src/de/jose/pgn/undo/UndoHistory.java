package de.jose.pgn.undo;

import de.jose.Language;
import de.jose.pgn.Game;
import de.jose.pgn.Node;

import javax.swing.undo.UndoableEdit;
import java.util.LinkedList;

public class UndoHistory implements UndoableEdit
{
    protected Game game;
    //  todo integrate with swing.UndoManager ?
    protected LinkedList<UndoableEdit> hist;
    protected boolean recording;

    protected int current;

    public UndoHistory(Game game) {
        this.game = game;
        this.hist = new LinkedList<>();
        this.current = -1;
        this.recording = false;
    }

    public boolean isEmpty() { return hist.isEmpty(); }

    public NodeEdit getCurrent() {
        return (current<0) ? null : (NodeEdit) hist.get(current);
    }

    @Override
    public boolean canUndo() {
        return current >= 0;
    }

    @Override
    public boolean canRedo() {
        return (current+1) < hist.size();
    }

    @Override
    public void die() { }

    @Override
    public boolean addEdit(UndoableEdit undoableEdit) {
        return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit undoableEdit) {
        return false;
    }

    @Override
    public boolean isSignificant() {
        return true;
    }

    public InsertText pushInsert(Node node, int pos1, String text)
    {
        UndoableEdit cur = getCurrent();
        InsertText ins;
        if (cur instanceof InsertText) {
            ins = (InsertText)cur;
            if (ins.conflateWith(node, pos1, text))
                return ins;
        }
        //else
        ins = new InsertText(node,pos1,text);
        push(ins);
        return ins;
    }

    public DeleteText pushDelete(Node node, int pos1, int pos2)
    {
        UndoableEdit cur = getCurrent();
        DeleteText del;
        if (cur instanceof DeleteText) {
            del = (DeleteText)cur;
            if (del.conflateWith(node, pos1, pos2))
                return del;
        }
        //else
        del = new DeleteText(node,pos1,pos2);
        push(del);
        return del;
    }

    public UndoableEdit push(UndoableEdit edit) {
        truncate();
        hist.add(edit);
        current++;
        return edit;
    }


    public boolean isRecording() { return recording; }

    public CompoundEdit getRecorder() {
        if (isRecording())
            return (CompoundEdit)getCurrent();
        else
            return null;
    }

    public CompoundEdit startRecording(String name) {
        recording = true;
        CompoundEdit comp = new CompoundEdit(name);
        push(comp);
        return comp;
    }

    public void stopRecording() {
        recording=false;
    }

    @Override
    public String getPresentationName() {
        if (getCurrent() == null)
            return Language.get("undo.cant");
        else
            return getCurrent().getPresentationName();
    }

    @Override
    public String getUndoPresentationName() {
        if (getCurrent() == null)
            return Language.get("undo.cant.undo");
        else
            return getCurrent().getUndoPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        if (getCurrent() == null)
            return Language.get("undo.cant.redo");
        else
            return getCurrent().getRedoPresentationName();
    }

    @Override
    public void undo() {
        hist.get(current--).undo();
    }

    @Override
    public void redo() {
        hist.get(++current).redo();
    }

    protected void truncate() {
        while(hist.size() > (current+1)) hist.removeLast();
    }

    public static String presentationName(String edit) {
        return Language.get("undo."+edit);
    }
    public static String undoPresentationName(String edit) {
        return Language.get("undo.undo")+" "+presentationName(edit);
    }
    public static String redoPresentationName(String edit) {
        return Language.get("undo.redo")+" "+presentationName(edit);
    }
}
