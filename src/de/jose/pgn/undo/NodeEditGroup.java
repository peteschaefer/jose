package de.jose.pgn.undo;

import de.jose.Language;
import de.jose.pgn.Game;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.LinkedList;

public class NodeEditGroup implements UndoableEdit
{
    protected Game game;
    protected String name;
    protected LinkedList<UndoableEdit> ops;

    public NodeEditGroup(Game game, String name) {
        this.game = game;
        this.name = name;
        ops = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean addEdit(UndoableEdit undoableEdit) {
        ops.add(undoableEdit);
        return true;
    }

    @Override
    public boolean canUndo() { return true; }
    @Override
    public boolean canRedo() { return true; }

    @Override
    public void undo() throws CannotUndoException {
        for(int i = ops.size() - 1; i >= 0; i--)
            ops.get(i).undo();
    }

    @Override
    public void redo() throws CannotRedoException {
        for(int i = 0; i < ops.size(); i++)
            ops.get(i).redo();
    }


    @Override
    public void die() {
        //what's that?
    }

    @Override
    public boolean replaceEdit(UndoableEdit undoableEdit) {
        //what's that?
        return false;
    }

    @Override
    public boolean isSignificant() { return true; }

    @Override
    public String getPresentationName() {
        return Language.get(name);
    }

    @Override
    public String getUndoPresentationName() {
        return Language.get("undo.undo")+" "+getPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        return Language.get("undo.redo")+" "+getPresentationName();
    }
}
