package de.jose.view.input;

import de.jose.Application;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
public class AutoCompleteTextField extends JTextPane implements DocumentListener, CaretListener
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
        //  todo single-line, don't expand
        this.doc = (DefaultStyledDocument) super.getStyledDocument();
        this.prefixStyle = this.doc.addStyle("prefix", null);
        this.suffixStyle = this.doc.addStyle("suffix",null);
        StyleConstants.setForeground(suffixStyle, Color.gray);
        doc.addDocumentListener(this);
        super.addCaretListener(this);

        getInputMap().put(completerKey, "completerTyped");
        getActionMap().put("completerTyped", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { onCompleterKey(); }
            @Override
            public boolean accept(Object sender) { return true; }
        });

        doc.setDocumentFilter(new ACDocumentFilter());
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
    private List<String> suffixes = new ArrayList<String>();
    private boolean wasAbbreviated = false;

    private Completer completer;
    private ShowOn showSuggest, showComplete;
    private KeyStroke completerKey;
    private DefaultStyledDocument doc;

    private Style prefixStyle, suffixStyle;
    private boolean blockListeners=false;

    //
    //  Methods
    //

    private void applySuggestion(String suggestion) {
        prefixLen += suggestion.length();
        doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);
        getCaret().setDot(prefixLen);
        suffixes.clear();
        updateCompletions(); // ?
    }

    private void showCompletionPopup() {
        //  todo
    }

    private void updateCompletions() {
        Application.theExecutorService.submit(AutoCompleteTextField.this::doUpdateCompletions);
    }

    private void doUpdateCompletions()
    {
        String query = getText();
        List<String> completions = null;
        try {

            completions = completer.getCompletions(query, 0);
            if (completions==null) return;  //  query was aborted by Completer

            computeSuffixes(completions, query);

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        System.err.println(query+": "+completions.size()+" completions; "+suffixes.size()+" suffixes");
        if (suffixes.size()==1)
            System.err.println("suggestion: '"+suffixes.get(0)+"'");

        SwingUtilities.invokeLater(AutoCompleteTextField.this::updateDocument);
    }

    private void computeSuffixes(List<String> completions, String query)
    {
        //  find suffixes length
        int suffLen = 0;
        this.suffixes = new ArrayList<>();
        this.wasAbbreviated = false;
        int i,j;

        outer_loop:
        for(i=0; i<completions.size(); i++)
        {
            String cmpi = completions.get(i);
            int pxi = completer.prefixLength(query,cmpi);
            suffLen = cmpi.length() - pxi;
            if (suffLen==0) continue;   //  skip this one

            assert(suffLen > 0);
            for(j = i+1; j < completions.size(); j++) {
                String cmpj = completions.get(j);
                int pxj = completer.prefixLength(query,cmpj);

                if (cmpi.charAt(pxi) != cmpj.charAt(pxj))  {
                    //  ends one run
                    suffixes.add( cmpi.substring(pxi, pxi+suffLen) );
                    i=j-1;
                    continue outer_loop;
                }

                for (int k=1; k < suffLen; k++) {
                    if (cmpi.charAt(pxi+k) != cmpj.charAt(pxj+k)) {
                        suffLen = k;
                        wasAbbreviated = true;//  shorten suffix
                        break;
                    }
                }
                assert(suffLen > 0);
            }
            //  eof
            assert(j==completions.size());
            suffixes.add( cmpi.substring(pxi, pxi+suffLen) );
            break;
        }
    }

    private void updateDocument() {
        Caret caret = super.getCaret();
        int oldDot = caret.getDot();
        int oldMark = caret.getMark();

        //  text operations should not mess up the caret. it is restored at the end
        //  @see also AWTUtil.setTextSafe()
        caret.setDot(0);
        setCaret(null);

        try {
            blockListeners=true;
            doc.remove(prefixLen, doc.getLength() - prefixLen);
            doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);

            if (suffixes.size()==1)
                doc.insertString(prefixLen, suffixes.get(0), suffixStyle);
            if (suffixes.size() >= 2 || wasAbbreviated)
                doc.insertString(doc.getLength(), "...", suffixStyle);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        } finally {
            super.setCaret(caret);
            caret.setDot(Math.min(prefixLen,oldMark));
            caret.moveDot(Math.min(prefixLen,oldDot));
            blockListeners=false;
        }
    }

    //
    //  Implemented Interfaces
    //

    private void onCompleterKey() {
        int dot = super.getCaret().getDot();
        if (inSuggestion(dot)) {
            String suggestion = suffixes.get(0);    //  note: **do** evaluate before lambda
            SwingUtilities.invokeLater(() -> applySuggestion(suggestion));
        }
        else if (suffixes.size() >= 2)
            SwingUtilities.invokeLater(AutoCompleteTextField.this::showCompletionPopup);
    }

    private boolean inSuggestion(int pos) {
        return (suffixes.size()==1)
                && (pos >= prefixLen)
                && (pos < prefixLen+suffixes.get(0).length());
    }


    @Override
    public void caretUpdate(CaretEvent e) {
        if (blockListeners) return;

        if (e.getDot() <= prefixLen) {
            //  click in editable section. ok.
        }
        else {
            onCompleterKey();
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

        if (e.getOffset() <= prefixLen) {
            int chunk = Math.min(e.getLength(),prefixLen-e.getOffset());
            prefixLen -= chunk;
        }
        updateCompletions();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (blockListeners) return;

        updateCompletions();
    }

    class ACDocumentFilter extends DocumentFilter
    {
        //  gobble tabs and newlines
        private String filterString(String input) {
            input = input.replace("\t","");   //  can not insert tabs
            input = input.replace("\n","");
            input = input.replace("\r","");
            return input;//  or newlines
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            fb.insertString(offset, filterString(string), attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            fb.remove(offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            fb.replace(offset, length, filterString(text), attrs);
        }
    }

}
