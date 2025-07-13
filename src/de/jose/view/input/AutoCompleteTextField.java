package de.jose.view.input;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
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
public class AutoCompleteTextField extends JTextArea
{
    public interface Completer {
        List<String> findTexts(String prefix, int limit);
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
        // setup styled JTextArea and Styled Dcoument
        //  single-line
        this.doc = (StyledDocument) super.getDocument();
        this.prefixStyle = this.doc.addStyle("prefix", null);
        this.suffixStyle = this.doc.addStyle("suffix",null);
        StyleConstants.setForeground(suffixStyle, Color.gray);
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
    //  length of suggestion prefix (optional)
    private int suffxiLen = 0;

    private Completer completer;
    private ShowOn showSuggest, showComplete;
    private KeyStroke completerKey;
    private StyledDocument doc;
    private Style prefixStyle, suffixStyle;
    //
    //  Methods
    //
}
