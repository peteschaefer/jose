package de.jose.view.input;

import com.formdev.flatlaf.ui.FlatTextBorder;
import de.jose.Application;
import de.jose.db.JoConnection;
import de.jose.util.CharUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;

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
public class AutoCompleteTextField extends JComponent implements CaretListener, FocusListener
{

    /**
     * Interface that completion providers must implement
     * @see de.jose.db.io.DBFieldCompleter which retrieves completions from the database.
     *  Also demontrates how to handle concurrent queries: only the last query survives, previous queries are aborted.
     */
    public interface Completer {
        /**
         * return a list of completions, matching a prefix.
         * May be called from a background thread, and concurrently.
         *
         * Make sure that the implementation is thread safe!
         * Queries may be aborted, if a new query comes in.
         * Only the last query has to return valid results.
         *
         * @param query the query prefix. may contain wildcards
         * @param limit max. number of results
         * @return a list of completions, sorted alphabetically.
         *  Null if the query was interrupted by a subsequent query.
         */
        List<String> getCompletions(String query, int limit);

        /**
         * @param query string (may contain wildcards)
         * @param result completion string (one of the list returned by getCompletions)
         * @return length of prefix. Usually, this is just query.getLength(),
         *               but differs if there were wildcards in the query string.*
         */
        default int prefixLength(String query, String result) {
            return query.length();
            //  todo re-implemented for wildcard queries
        }
    }

    public interface TextListener {
        void textChanged(String text);
    }

    public static KeyStroke DEFAULT_COMPLETE_KEY = KeyStroke.getKeyStroke('\t');

    public AutoCompleteTextField(Completer completer, KeyStroke completerKey)
    {
        this.text = new ACTextPane();
        this.scroller = new JScrollPane(text,VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setLayout(new GridLayout(1,1));
        add(scroller);

        //  scroller has a nicer focus border ?!
        //text.setBorder(scroller.getBorder());

        this.completer = completer;
        this.completerKey = completerKey;

        FlatTextBorder border = (FlatTextBorder) UIManager.get("TextField.border");
//        text.setBorder(border);
//        scroller.setBorder(null);
        setMinimumSize(new Dimension(100,22));
        //  todo why is this necessary ? height collapses *sometimes* with long text ?


        this.doc = (DefaultStyledDocument) text.getStyledDocument();
        this.prefixStyle = this.doc.addStyle("prefix", null);
        this.suffixStyle = this.doc.addStyle("suffix",null);
        StyleConstants.setForeground(suffixStyle, Color.gray);
        //doc.addDocumentListener(this);
        text.addCaretListener(this);
        text.addFocusListener(this);

        /**
         * Focus traversal keys are
         *  Tab, Ctrl-Tab for normal components
         *  but only Ctrl-Tab for multi-line text.
         *  'text' is a multi-linte text edit that should behave like a single-line,
         *  i.e. Tab should
         *  (1) trigger auto completion
         *  (2) trigger focus traversal
         */

        text.getInputMap().put(completerKey, "completerTyped");
        text.getActionMap().put("completerTyped", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (! onCompleterKey()) {
                    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                    focusManager.focusNextComponent();
                }
            }
            @Override
            public boolean accept(Object sender) { return true; }
        });

        doc.setDocumentFilter(new ACDocumentFilter());

        //  todo hide popup on: ESC, focus loss
    }

    public AutoCompleteTextField(Completer completer) {
        this(completer, DEFAULT_COMPLETE_KEY);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public String getText() {
        assert(prefixLen <= doc.getLength());
        try {
            return doc.getText(0,prefixLen);
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Document getDocument() {
        return doc;
    }

    public void addTextListener(TextListener listener) {
        listeners.add(listener);
    }

    @Override
    public void setMinimumSize(Dimension minimumSize) {
        super.setMinimumSize(minimumSize);
        scroller.setMinimumSize(minimumSize);
        text.setMinimumSize(minimumSize);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        scroller.setPreferredSize(preferredSize);
        text.setPreferredSize(preferredSize);
    }

    //
    //  Members
    //
    //  length of user-typed prefix
    private int minPrefixLen = 1;   //  don't autocomplete on an empty string (it works, but is not intuitive)
    private int prefixLen = 0;
    private List<String> suffixes = new ArrayList<String>();
    private boolean wasAbbreviated = false;

    private Completer completer;
    private KeyStroke completerKey;
    private DefaultStyledDocument doc;
    private List<TextListener> listeners = new ArrayList<TextListener>();

    private Style prefixStyle, suffixStyle;
    private boolean blockListeners=false;

    private JScrollPane scroller;
    private ACTextPane text;
    private JPopupMenu popupMenu = new JPopupMenu();

    //
    //  Methods
    //

    //  append a suggestion that is already displayed (greyed out)
    private void applySuggestion(String suggestion) {
        prefixLen += suggestion.length();
        doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);
        text.getCaret().setDot(prefixLen);
        suffixes.clear();
        updateCompletions();
    }

    //  append a suggestion that is not yet displayed (from popup menu)
    private void appendSuggestion(String suggestion) {
        try {
            blockListeners=true;
            doc.insertString(prefixLen,suggestion,prefixStyle);     //  would not trigger updateCompletion !
            prefixLen += suggestion.length();
            text.getCaret().setDot(prefixLen);
            suffixes.clear();
            updateCompletions(true);    //  todo then continue, if there are more completions
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        } finally {
            blockListeners=false;
        }
    }

    private static char findMnemo(String s) {
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c))
                return Character.toUpperCase(c);
        }
        if (s.length() > 0) {
            char c = s.charAt(0);
            return Character.toUpperCase(c);
        }
        return '\0';
    }

    private void showCompletionPopup() {
        String t = getText();
        popupMenu.removeAll();

        for(String s : suffixes) {
            //popupMenu.add(t+s);
            Action action = new AbstractAction(s) {                 @Override
                public void actionPerformed(ActionEvent e) {
                    appendSuggestion(s);
                }
            };
            action.putValue(Action.NAME, t+"-"+s);
            if (s.length()>=1) {
                char mnemo = findMnemo(s);
                if (mnemo!=0) {
                    action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(mnemo));
                    action.putValue(Action.MNEMONIC_KEY, (int) mnemo);
                }
            }
            popupMenu.add(action);
        }
        //  align below Caret
        Point p = text.getLocation();
        popupMenu.show(this, p.x, p.y+text.getHeight());
    }

    private void hideCompletionPopup() {
        if (popupMenu.isVisible() && !blockListeners)
            popupMenu.setVisible(false);
    }

    private void updateCompletions() {
        //hideCompletionPopup();
        if (text.hasFocus())
            updateCompletions(false);
    }

    private void updateCompletions(boolean continuePopping)
    {
        if (prefixLen >= minPrefixLen) {
            Application.theExecutorService.submit(() -> doUpdateCompletions(continuePopping));
        } else {
            suffixes.clear();
            updateDocument();
        }
    }

    private void doUpdateCompletions(boolean continuePopping)
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
        System.err.println(JoConnection.getPool().size()+" pooled connections");

        if (continuePopping) {
            SwingUtilities.invokeLater(() -> {
                updateDocument();   //  todo w/out triggering updateCompletions !?
                showCompletionPopup();
            });
        }
        else {
            SwingUtilities.invokeLater(AutoCompleteTextField.this::updateDocument);
        }
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

                if (CharUtil.toUpperCase(cmpi,pxi) != CharUtil.toUpperCase(cmpj,pxj))
                {
                    //  ends one run
                    //  todo single-char completions are rather pointless; but must not get lost, either?
                    // if (suffLen >= 2)
                    suffixes.add( cmpi.substring(pxi, pxi+suffLen) );
                    i=j-1;
                    continue outer_loop;
                }

                for (int k=1; k < suffLen; k++) {
                    if (CharUtil.toUpperCase(cmpi,pxi+k) != CharUtil.toUpperCase(cmpj,pxj+k))
                    {
                        //  skip punctation, too
                        while(k > 0 && !Character.isLetterOrDigit(cmpj.charAt(pxj+k-1)))
                            k--;
                        if (k==0) {
                            //  ends one run
                            suffixes.add( cmpi.substring(pxi, pxi+suffLen) );
                            i=j-1;
                            continue outer_loop;
                        }

                        suffLen = k;
                        wasAbbreviated = true;//  shorten suffix
                        break;
                        /** todo we would like to know for each suffix whether it is an abbreviation
                         *  s.t. we can indicate it in the menu like "suf..."
                         */
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
        Caret caret = text.getCaret();
        int oldDot = caret.getDot();
        int oldMark = caret.getMark();

        //  text operations should not mess up the caret. it is restored at the end
        //  @see also AWTUtil.setTextSafe()
        caret.setDot(0);
        text.setCaret(null);

        boolean wasBlockListeners = blockListeners;
        blockListeners=true;
        try {
            doc.remove(prefixLen, doc.getLength() - prefixLen);
            doc.setCharacterAttributes(0, prefixLen, prefixStyle, true);

            if (text.hasFocus()) {
                if (suffixes.size() == 1)
                    doc.insertString(prefixLen, suffixes.get(0), suffixStyle);
                if (suffixes.size() >= 2 || wasAbbreviated)
                if (suffixes.size() >= 2 || wasAbbreviated)
                    doc.insertString(doc.getLength(), "...", suffixStyle);
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        } finally {
            text.setCaret(caret);
            caret.setDot(Math.min(prefixLen,oldMark));
            caret.moveDot(Math.min(prefixLen,oldDot));
            blockListeners=wasBlockListeners;
        }
    }

    //
    //  Implemented Interfaces
    //

    private boolean onCompleterKey() {
        if (blockListeners) return true;

        int dot = text.getCaret().getDot();
        if (inSuggestion(dot)) {
            String suggestion = suffixes.get(0);    //  note: **do** evaluate before lambda
            SwingUtilities.invokeLater(() -> applySuggestion(suggestion));
            return true;
        }
        else if (suffixes.size() >= 2) {
            SwingUtilities.invokeLater(AutoCompleteTextField.this::showCompletionPopup);
            return true;
        }
        else {
            return false;
        }
    }

    private boolean inSuggestion(int pos) {
        return (suffixes.size()==1)
                && (pos >= prefixLen)
                && (pos < prefixLen+suffixes.get(0).length());
    }

    //  implements CaretListener
    @Override
    public void caretUpdate(CaretEvent e) {
        if (blockListeners) return;

        if (e.getDot() > prefixLen) {
            onCompleterKey();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        SwingUtilities.invokeLater(AutoCompleteTextField.this::updateCompletions);
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (e.getOppositeComponent()==popupMenu) {
            //  popup menu gained focus. no worry
        }
        else {
            SwingUtilities.invokeLater(AutoCompleteTextField.this::updateDocument);
        }
    }

    class ACTextPane extends JTextPane
    {
        //  hack to avoid line breaking ?!
        @Override
        public boolean getScrollableTracksViewportWidth() {
            // Only track viewport width when the viewport is wider than the preferred width
            return getUI().getPreferredSize(this).width <= getParent().getSize().width;
        }
        @Override
        public Dimension getPreferredSize() {
            return getUI().getPreferredSize(this);
        }
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

        private void onUpdate(int offset, int length) {
            if (length==0) return;
            if (offset <= prefixLen)
                prefixLen += length;    //  length may be negative!
            assert(prefixLen <= doc.getLength());
            for(TextListener list : listeners)
                list.textChanged(getText());
            updateCompletions();
        }

        private void onRemove(int offset, int length) {
            if (length==0) return;
            if (offset <= prefixLen) {
                int chunk = Math.min(length,prefixLen-offset);
                prefixLen -= chunk;
            }
            assert(prefixLen <= doc.getLength());
            for(TextListener list : listeners)
                list.textChanged(getText());
            updateCompletions();
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
            boolean wasBlockListeners = blockListeners;
            blockListeners=true;
            text = filterString(text);
            fb.insertString(offset, text, attr);

            if (!wasBlockListeners)
                onUpdate(offset,text.length());

            blockListeners=wasBlockListeners;
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            boolean wasBlockListeners = blockListeners;
            blockListeners=true;
            fb.remove(offset, length);

            if (!wasBlockListeners)
                onRemove(offset,length);

            blockListeners=wasBlockListeners;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            boolean wasBlockListeners = blockListeners;
            blockListeners=true;
            text = filterString(text);
            fb.replace(offset, length, text, attrs);

            if (!wasBlockListeners) {
                if (text.isEmpty())
                    onRemove(offset, length);
                else
                    onUpdate(offset, text.length()-length);
            }

            blockListeners=wasBlockListeners;
        }
    }

}
