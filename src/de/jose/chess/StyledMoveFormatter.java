package de.jose.chess;

import de.jose.Application;
import de.jose.profile.FontEncoding;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class StyledMoveFormatter extends StringMoveFormatter
{
    private StyledDocument doc;
    private int at;
    private Style textStyle;
    private Style figStyle;
    private FontEncoding enc;

    public Style getTextStyle() {
        return textStyle;
    }

    public Style getFigStyle() {
        return figStyle;
    }

    public void setTextStyle(Style textStyle) {
        this.textStyle = textStyle;
    }

    public void setTextLanguage() {
        String language = JoStyleContext.getFigurineLanguage(textStyle);
        setLanguage(language);
    }

    public void setFigStyle(Style figStyle) {
        this.figStyle = figStyle;
        if (figStyle != null) {
            String fontName = JoFontConstants.getFontFamily(figStyle);
            this.enc = FontEncoding.getEncoding(fontName);
            figStyle.addAttribute("figurine",true);
        }
    }

    public void setDocument(StyledDocument doc, int at) {
        this.doc = doc;
        this.at = at;
    }

    public int position() {
        return at;
    }

    @Override
    public void figurine(int pc, boolean promotion) {
        if (figStyle == null)
            buf.append(pieceChars[EngUtil.uncolored(pc)]);
        else try {

            flush();

            String ptxt = enc.getFigurine(pc);
            //ptxt = FontCapture.checkPrintable(ptxt, family);
            //ptxt +="p\u0087o";
            doc.insertString(at, ptxt, figStyle);
            at += ptxt.length();

        } catch (BadLocationException blex) {
            Application.error(blex);
            throw new RuntimeException(blex.getMessage());
        }
    }

    private static boolean equals(String query, CharSequence str, int i)
    {
        if (i+query.length() > str.length()) return false;
        for(int j=0; j < query.length(); j++)
            if (str.charAt(i+j) != query.charAt(j)) return false;
        return true;
    }

    private static int pieceFromChars(String[] pcs, CharSequence str, int start)
    {
        //  attention: multi-char encodings, and prefixes.
        //  e.g. in Russian, there is "K" and "Kp". Find the **longest** match.
        int pc=-1;
        int match=0;
        for(int i=0; i < pcs.length; i++)
            if (pcs[i]!=null && equals(pcs[i],str,start) && (pcs[i].length() > match)) {
                pc = (i - 1 + PAWN);
                match = pcs[i].length();
            }
        return pc;
    }

    /**
     * plain text from formatted text
     * @param doc
     * @return
     */
    public String reformat(StyledDocument doc)
    {
        String text;
        try {
            text = doc.getText(0,doc.getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        StringBuffer buf = new StringBuffer(text);
        boolean comment=false;
        int i=0; //  i = index into doc
        int j=0; // j = index into buf
        while(j < buf.length())
        {
            char c = buf.charAt(j);
            if (c=='{') comment=true;
            if (!comment && doc.getCharacterElement(i).getAttributes().containsAttribute("figurine",true)) {
                int pc = enc.pieceFromFigurine(c);  //  todo multi-char piece encodings!
                if (pc>0) {
                    String pcs = pieceChars[pc];
                    buf.replace(j,j+1,pcs);
                    i++;
                    j += pcs.length();
                    continue;
                }
            }
            if (c=='}') comment=false;
            i++; j++;
        }
        return buf.toString();
    }

    /**
     * create styled text from plain text
     * @param str
     */
    public void reformatFrom(CharSequence str)
    {
        boolean comment=false;
        int i=0;
        while(i < str.length())
        {
            char c = str.charAt(i);
            if (c=='{') comment=true;

            if (!comment && Character.isUpperCase(c)) {
                int pc = pieceFromChars(pieceChars,str,i);   //  attention: multi-char piece encodings!
                if (pc >= PAWN) {
                    figurine(pc, false);
                    i += pieceChars[pc-PAWN+1].length();
                    continue;
                }
            }

            buf.append(c);
            if (c=='}') comment=false;
            i++;
        }
        flush();
    }

    @Override
    public String flush() {
        String result = super.flush();
        if (result != null) try {
            doc.insertString(at, result, textStyle);
            at += result.length();
        } catch (BadLocationException blex) {
            Application.error(blex);
            throw new RuntimeException(blex.getMessage());
        }
        return result;
    }
}
