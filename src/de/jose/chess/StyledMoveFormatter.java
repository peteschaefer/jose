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

    public void setTextStyle(Style textStyle) {
        this.textStyle = textStyle;
        if (figStyle==null) {
            String language = JoStyleContext.getFigurineLanguage(textStyle);
            setLanguage(language);
        }
    }

    public void setFigStyle(Style figStyle) {
        this.figStyle = figStyle;
        if (figStyle != null) {
            String fontName = JoFontConstants.getFontFamily(figStyle);
            this.enc = FontEncoding.getEncoding(fontName);
        }
        if (figStyle==null) {
            String language = JoStyleContext.getFigurineLanguage(textStyle);
            setLanguage(language);
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
