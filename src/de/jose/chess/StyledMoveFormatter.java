package de.jose.chess;

import de.jose.Application;
import de.jose.image.FontCapture;
import de.jose.profile.FontEncoding;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import static de.jose.util.ListUtil.indexOf;

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
            String family = JoFontConstants.getFontFamily(figStyle);
            ptxt = FontCapture.checkPrintable(ptxt, family);

            doc.insertString(at, ptxt, figStyle);
            at += ptxt.length();

        } catch (BadLocationException blex) {
            Application.error(blex);
            throw new RuntimeException(blex.getMessage());
        }
    }

    public void reformatFrom(CharSequence str)
    {
        boolean comment=false;
        for (int i=0; i<str.length(); i++)
        {
            char c = str.charAt(i);
            if (c=='{') comment=true;

            if (!comment && Character.isUpperCase(c)) {
                int pc = indexOf(pieceChars,c); //  todo
                if (pc >= 0) {
                    figurine(PAWN + pc, false);
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
