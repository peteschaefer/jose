package de.jose.view.input;

import de.jose.Application;
import de.jose.Language;
import de.jose.db.JoConnection;
import de.jose.util.CharUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;
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
        ArrayList<String> getCompletions(String query, int limit);

        default boolean canComplete(String query) {
            return !query.isEmpty();
        }

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

        setMinimumSize(new Dimension(100,22));
        //  todo why is this necessary ? height collapses *sometimes* with long text ?


        this.doc = (DefaultStyledDocument) text.getStyledDocument();
        this.prefixStyle = this.doc.addStyle("prefix", null);
        this.suffixStyle = this.doc.addStyle("suffix",null);
        StyleConstants.setForeground(suffixStyle, new Color(148,148,148));
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
        //  todo focused border width is too narrow. No way to fix it :(
    }

    public AutoCompleteTextField(Completer completer) {
        this(completer, DEFAULT_COMPLETE_KEY);
    }

    public void setText(String text) {
        if (text==null) text="";
        this.text.setText(text);
        prefixLen = text.length();
        updateDocument();
    }

    public String getText() {
        assert(prefixLen >= 0);
        assert(prefixLen <= doc.getLength());
        try {
            return doc.getText(0, Math.max(0,Math.min(prefixLen,doc.getLength())));
        } catch (BadLocationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        updateDocument();
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        updateDocument();
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
    private int prefixLen = 0;
    private ArrayList<String> suffixes = new ArrayList<String>();
    private boolean wasAbbreviated = false;

    private Completer completer;
    private KeyStroke completerKey;
    private DefaultStyledDocument doc;
    private List<TextListener> listeners = new ArrayList<TextListener>();

    private Style prefixStyle, suffixStyle;
    private boolean blockListeners=false;

    private JScrollPane scroller;
    private ACTextPane text;
    private ACPopupMenu popupMenu = new ACPopupMenu();

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
            Action action = new AbstractAction(s) {
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
        if (completer.canComplete(getText())) {
            Application.theExecutorService.submit(() -> doUpdateCompletions(continuePopping));
        } else {
            suffixes.clear();
            wasAbbreviated=false;
            updateDocument();
        }
    }

    private void doUpdateCompletions(boolean continuePopping)
    {
        String query = getText();
        ArrayList<String> completions = null;
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

        if (continuePopping && suffixes.size() >= 2) {
            SwingUtilities.invokeLater(() -> {
                updateDocument();   //  w/out triggering updateCompletions !
                showCompletionPopup();
            });
        }
        else {
            SwingUtilities.invokeLater(AutoCompleteTextField.this::updateDocument);
        }
    }

    private static int nextLetter(CharSequence s, int k) {
        while(k < s.length() && !Character.isLetterOrDigit(s.charAt(k)))
            k++;
        return k;
    }

    static class SuffixCompare {
        int compare;        //  comparison result < 0, == 0, > 0
        int len;            //  match length
        int letters;        //  # matched letters

        public SuffixCompare(int compare, int len, int letters) {
            this.compare = compare;
            this.len = len;
            this.letters = letters;
        }
    }

    private static SuffixCompare compareSuffixSequences(CharSequence s1, CharSequence s2)
    {
        int k1 = 0, k2 = 0;
        int commonLetters = 0;

        while (k1 < s1.length() && k2 < s2.length()) {
            char c1 = s1.charAt(k1);
            char c2 = s2.charAt(k2);

            boolean punct1 = !Character.isLetterOrDigit(c1);
            boolean punct2 = !Character.isLetterOrDigit(c2);

            if (punct1 && !punct2)
                return new SuffixCompare(-1,k1,commonLetters);
            if (!punct1 && punct2)
                return new SuffixCompare(+1,k1,commonLetters);

            if (punct1 && punct2) {
                k1 = nextLetter(s1, k1+1);
                k2 = nextLetter(s2, k2+1);
                //  treat sequences of punctation as one entity
                continue;
            }

            assert(!punct1);
            assert(!punct2);
            assert(Character.isLetterOrDigit(c1));
            assert(Character.isLetterOrDigit(c2));

            c1 = CharUtil.stripDiacritic(c1);
            c2 = CharUtil.stripDiacritic(c2);

            c1 = CharUtil.toUpperCase(c1);
            c2 = CharUtil.toUpperCase(c2);

            if (c1!=c2)
                return new SuffixCompare(c1-c2,k1,commonLetters);

            //  else
            commonLetters++;
            k1++;
            k2++;
        }

        if (k1 < s1.length())
            return new SuffixCompare(+1,k1,commonLetters);
        if (k2 < s2.length())
            return new SuffixCompare(-1,k1,commonLetters);
        //  else
        return new SuffixCompare(0,s1.length(),commonLetters);
    }

    private CharSequence commonSuffix(CharSequence s1, CharSequence s2)
    {
        SuffixCompare cmp = compareSuffixSequences(s1, s2);
        if (cmp.letters ==0)
            return null;
        else
            return s1.subSequence(0,cmp.len);
    }


    private static int compareSuffixes(CharSequence s1, CharSequence s2) {
        SuffixCompare cmp = compareSuffixSequences(s1, s2);
        return cmp.compare;
    }


    private void truncatePrefixes(List<String> completions, String query)
    {
        for(int i=0; i < completions.size(); ) {
            String s = completions.get(i);
            int pi = completer.prefixLength(query,s);   //  might differ with wildcards
            if (pi > 0) {
                s = s.substring(pi, s.length());
                completions.set(i, s);
                i++;
            }
            else {
                //  note that the GlobX matcher used by DBFieldCompleter
                //  is stricter than the SQL query w.r.t. punctuation
                /*  an example:
                    user input = "Kasparov, G"
                    SQL like pattern = "Kaspoarov%G"    (note that LIKE can not detect punctuation)
                    matches, among others, "Kasparov, Sergey".
                    But that's not what we wanted.

                    Globx pattern = "Kasparov.G"    is strict about punctuation and rejects ", Sergey".
                    Does this make sense?
                 */
                completions.remove(i);
            }
        }
    }

    private void findSuffixRuns(ArrayList<String> completions)
    {
        CharSequence current;
        this.suffixes = new ArrayList<>();
        this.wasAbbreviated = false;
        int i,j;

        outer_loop:
        for(i=0; i<completions.size(); i++)
        {
            current = completions.get(i);
            if (current.length()==0) continue;   //  skip this one

            assert(current.length() > 0);
            for(j = i+1; j < completions.size(); j++) {
                CharSequence next = completions.get(j);
                if (next.length()==0) continue; //  skip this one

                CharSequence common = commonSuffix(current, next);
                if (common==null || common.length()==0) {
                    //  end this run
                    suffixes.add(current.toString());
                    i=j-1;
                    continue outer_loop;
                }
                //  else
                if (common.length() < current.length()) wasAbbreviated=true;
                current = common;
            }
            //  eof
            assert(j==completions.size());
            suffixes.add( current.toString() );
            break;
        }
    }

    private void computeSuffixes(ArrayList<String> completions, String query)
    {
        truncatePrefixes(completions, query);
        completions.sort(AutoCompleteTextField::compareSuffixes);
        findSuffixRuns(completions);
        removeDuplicates(suffixes);
    }

    private static void removeDuplicates(ArrayList<String> strs) {
        //  now that punctuation has been skipped, sort *again*
        //  and remove duplicates
        for(int i=1; i < strs.size(); ) {
            String s0 = strs.get(i-1);
            String s1 = strs.get(i);
            if (compareSuffixes(s0,s1)==0)
                strs.remove(i);
            else
                i++;
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

            if (prefixLen==0) {
                if (getName()!=null || getToolTipText()!=null) {
                    String tip = Language.getTip((getToolTipText()!=null) ? getToolTipText():getName());
                    //if (tip==null && getName()!=null) tip = Language.getTip(getName());
                    if (tip==null && getName()!=null) tip = Language.get(getName());
                    doc.insertString(0, tip, suffixStyle);
                }
            }
            else if (text.hasFocus()) {
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

    class ACPopupMenu extends JPopupMenu
    {
        @Override
        public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager manager) {
            super.processKeyEvent(e, path, manager);
            if (e.isConsumed()) {
                //  ok
            }
            else switch (e.getID()) {
                case KEY_PRESSED:
                case KEY_RELEASED:
                    break;
                case KEY_TYPED:
                    //  forward to text field
                    this.setVisible(false);
                    AutoCompleteTextField.this.text.grabFocus();
                    AutoCompleteTextField.this.text.postKeyEvent(e);
                    break;
            }
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

        protected void postKeyEvent(KeyEvent e) {
            super.processKeyEvent(e);
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
