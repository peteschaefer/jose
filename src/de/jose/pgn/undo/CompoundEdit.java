package de.jose.pgn.undo;

import de.jose.Language;
import de.jose.pgn.Game;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.LinkedList;

public class CompoundEdit extends NodeEdit
{
    protected LinkedList<NodeEdit> ops;

    public CompoundEdit(String name) {
        super(name,null);
        ops = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public boolean addEdit(UndoableEdit edit) {
        ops.add((NodeEdit)edit);
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

}
