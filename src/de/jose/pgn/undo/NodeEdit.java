package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public abstract class NodeEdit implements UndoableEdit
{
    protected String name;
    protected Node node,prev,next;

    public NodeEdit(String name, Node node) {
        this.name = name;
        this.node = node;
        this.prev = node.previous();
        this.next = node.next();
    }

    String getRelText(int pos1, int pos2) {
        try {
            return node.getDocument().getText(node.getStartOffset()+pos1,pos2-pos1);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
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
    public String getPresentationName() { return UndoHistory.presentationName(name); }
    @Override
    public String getUndoPresentationName() { return UndoHistory.undoPresentationName(name); }
    @Override
    public String getRedoPresentationName() { return UndoHistory.redoPresentationName(name); }
}

