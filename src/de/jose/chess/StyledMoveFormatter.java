package de.jose.chess;

import de.jose.Application;
import de.jose.image.FontCapture;
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

    private static int pieceFromChar(String[] pcs, char c)
    {
        for(int i=0; i < pcs.length; i++)
            if (pcs[i]!=null && pcs[i].charAt(0)==c)
                return (i-1+PAWN);
        return -1;
    }

    public String reformatTo(StyledDocument doc)
    {
        String text;
        try {
            text = doc.getText(0,doc.getLength());
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        StringBuffer buf = new StringBuffer(text);
        boolean comment=false;
        for (int i=0; i < doc.getLength(); i++)
        {
            char c = buf.charAt(i);
            if (c=='{') comment=true;
            if (!comment && doc.getCharacterElement(i).getAttributes().containsAttribute("figurine",true)) {
                int pc = enc.pieceFromFigurine(c);
                if (pc>0) {
                    c = pieceChars[pc].charAt(0);
                    buf.setCharAt(i,c);
                }
            }
            if (c=='}') comment=false;
        }
        return buf.toString();
    }

    public void reformatFrom(CharSequence str)
    {
        boolean comment=false;
        for (int i=0; i<str.length(); i++)
        {
            char c = str.charAt(i);
            if (c=='{') comment=true;

            if (!comment && Character.isUpperCase(c)) {
                int pc = pieceFromChar(pieceChars,c);
                if (pc >= PAWN) {
                    figurine(pc, false);
                    continue;
                }
            }

            buf.append(c);
            if (c=='}') comment=false;
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
