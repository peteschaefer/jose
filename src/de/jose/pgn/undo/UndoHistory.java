package de.jose.pgn.undo;

import de.jose.Language;
import de.jose.pgn.Game;

import javax.swing.undo.UndoableEdit;
import java.io.Serializable;
import java.util.LinkedList;

public class UndoHistory implements UndoableEdit
{
    protected Game game;
    //  todo integrate with swing.UndoManager ?
    protected LinkedList<NodeEditGroup> hist;

    protected int current;

    public UndoHistory(Game game) {
        this.game = game;
        this.hist = new LinkedList<>();
        this.current = -1;
    }

    public boolean isEmpty() { return hist.isEmpty(); }

    public NodeEditGroup getCurrent() {
        return (current<0) ? null : hist.get(current);
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
    public void die() {

    }

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

    public NodeEditGroup open(String name, boolean force)
    {
        if (getCurrent()!=null && getCurrent().getName().equals(name) && !force) {
            // append to current
        }
        else {
            //  create new
            truncate();
            NodeEditGroup e = new NodeEditGroup(game, name);
            hist.add(e);
            current++;
        }
        return getCurrent();
    }

    protected void truncate() {
        while(hist.size() > (current+1)) hist.removeLast();
    }

}
