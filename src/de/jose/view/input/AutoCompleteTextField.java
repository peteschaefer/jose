package de.jose.view.input;

import de.jose.view.JoLineBorder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Similar to JTextField but with Autocomplete/suggestion
 *
 *  * completion: multiple completions are available and shown in a drop-down menu
 *  * suggestion: one completion is available and is shown as greyed-out suffix
 *
 *  * drop down menu can be shown
 *      * on demand, if auto-complete key is pressed (Tab, or something else)
 *      * as user types (more calls to completer, of course)
 */
public class AutoCompleteTextField extends JTextPane implements DocumentListener, CaretListener, KeyListener
{
    public interface Completer {
        List<String> getCompletions(String query, int limit);

        default int prefixLength(String query, String result) {
            return query.length();
            //  todo re-implemented for wildcard queries
        }
    }

    public enum ShowOn {
        NEVER, ONKEY, ASYOUTYPE
    };

    public static KeyStroke DEFAULT_COMPLETE_KEY = KeyStroke.getKeyStroke('\t');

    public AutoCompleteTextField(Completer completer,
                          ShowOn showSuggest, ShowOn showComplete,
                          KeyStroke completerKey)
    {
        this.completer = completer;
        this.showSuggest = showSuggest;
        this.showComplete = showComplete;
        this.completerKey = completerKey;
      //  Border border = (Border) UIManager.get("TextPane.border");
        super.setBorder(new LineBorder(Color.red));
        // setup styled JTextArea and Styled Dcoument
        //  single-line
        this.doc = super.getStyledDocument();
        this.prefixStyle = this.doc.addStyle("prefix", null);
        this.suffixStyle = this.doc.addStyle("suffix",null);
        StyleConstants.setForeground(suffixStyle, Color.gray);
        doc.addDocumentListener(this);
        super.addKeyListener(this);
        super.addCaretListener(this);
    }
    public AutoCompleteTextField(Completer completer) {
        this(completer, ShowOn.ASYOUTYPE, ShowOn.ONKEY, DEFAULT_COMPLETE_KEY);
    }

    public void setText(String text) {
        // todo
        super.setText(text);
    }

    public String getText() {
        try {
            return doc.getText(0,prefixLen);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    //
    //  Members
    //
    //  length of user-typed prefix
    private int prefixLen = 0;
    private List<String> completions = new ArrayList<String>();
    private String suggestion;

    private Completer completer;
    private ShowOn showSuggest, showComplete;
    private KeyStroke completerKey;
    private StyledDocument doc;

    private Style prefixStyle, suffixStyle;
    private boolean blockListeners=false;

    //
    //  Methods
    //

    private void applySuggestion() {
        prefixLen += suggestion.length();
        doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);
        suggestion = "";
    }

    private void showCompletionPopup() {
        //  todo
    }

    private void updateCompletions() {
        String query = getText();
        this.completions = completer.getCompletions(query, 0);
        //  find suggestions length
        int suggLen = 0;
        suggestion = "";

        if (!completions.isEmpty()) {
            String res0 = completions.get(0);
            int px0 = completer.prefixLength(query,res0);
            suggLen = res0.length() - px0;

            for(int i=1; suggLen > 0 && i < completions.size(); i++) {
                String resi = completions.get(i);
                int pxi = completer.prefixLength(query,resi);
                for (int j=0; j < suggLen; j++) {
                    if (resi.charAt(pxi+j) != res0.charAt(px0+j)) {
                        suggLen = j;
                        break;
                    }
                }
            }

            if (suggLen > 0)
                suggestion = res0.substring(px0, suggLen);
        }

        SwingUtilities.invokeLater(AutoCompleteTextField.this::updateDocument);
    }

    private void updateDocument() {
        Caret caret = super.getCaret();
        try {
            blockListeners=true;
            doc.remove(prefixLen, doc.getLength() - prefixLen);
            doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);

            if (!suggestion.isEmpty())
                doc.insertString(prefixLen, suggestion, suffixStyle);
            if (completions.size() >= 2)
                doc.insertString(doc.getLength(), "...", suffixStyle);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        } finally {
            super.setCaret(caret);
            blockListeners=false;
        }
    }

    //
    //  Implemented Interfaces
    //
    @Override
    public void keyTyped(KeyEvent e) {
        if (blockListeners) return;
        
        if (e.getKeyCode()==completerKey.getKeyCode()
                && e.getModifiersEx()==completerKey.getModifiers())
        {
            if (super.getCaret().getDot() < prefixLen+suggestion.length())
                applySuggestion();
            else
                showCompletionPopup();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { }
    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (blockListeners) return;

        if (e.getDot() <= prefixLen) {
            //  click in editable section. ok.
        }
        else if (e.getDot() < prefixLen+suggestion.length()) {
            //  click in suggestion
            applySuggestion();
        }
        else {
            //  click in "..."
            showCompletionPopup();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (blockListeners) return;

        if (e.getOffset() <= prefixLen)
            prefixLen += e.getLength();
        updateCompletions();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (blockListeners) return;

        if (e.getOffset() <= prefixLen)
            prefixLen -= e.getLength();
        updateCompletions();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (blockListeners) return;

        updateCompletions();
    }

}
