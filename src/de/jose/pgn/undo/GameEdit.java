package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public abstract class GameEdit implements UndoableEdit
{
    protected Node node;

    public GameEdit(Node node) {
        this.node = node;
    }

    @Override
    public boolean isSignificant() { return false; }
    @Override
    public boolean canUndo() { return true; }
    @Override
    public boolean canRedo() { return true; }

    @Override
    public void undo() throws CannotUndoException {
        /* implement! */
    }


    @Override
    public void redo() throws CannotRedoException {
        /* implement! */
    }


    @Override
    public void die() { /*what's that?*/ }

    @Override
    public boolean addEdit(UndoableEdit undoableEdit) { return false; }
    @Override
    public boolean replaceEdit(UndoableEdit undoableEdit) { return false; }

    @Override
    public String getPresentationName() { return ""; }
    @Override
    public String getUndoPresentationName() { return ""; }
    @Override
    public String getRedoPresentationName() { return ""; }
}

