package de.jose.view;

import de.jose.AbstractApplication;
import de.jose.Application;
import de.jose.chess.Board;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.pgn.DiagramNode;
import de.jose.profile.FontEncoding;
import de.jose.util.FontUtil;
import de.jose.util.StringUtil;
import de.jose.view.input.JoBigLabel;
import de.jose.view.input.JoStyledLabel;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.text.Style;
import java.awt.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class PopupBoardWindow
    extends JToolTip
{
    public PopupBoardWindow(EnginePanel enginePanel, int idx)
    {
        //setModal(false);
        this.engpanel = enginePanel;
        this.pvidx = idx;

        JoStyleContext styles = Application.theUserProfile.styles;
        Style style = styles.getStyle("body.inline");
        String diaFont = JoFontConstants.getFontFamily(style);
        this.enc = FontEncoding.getEncoding(diaFont);
        this.font = FontUtil.newFont(diaFont, Font.PLAIN, FONT_SIZE);

        updateUI();
    }

    public void updateUI() {
        setUI(PopupBoardWindow.PopupBoardWindowUI.createUI(this));
    }


    static class PopupBoardWindowUI extends BasicToolTipUI
    {
        static PopupBoardWindow.PopupBoardWindowUI sharedInstance = null;

        public static ComponentUI createUI(JComponent c)
        {
            if (sharedInstance==null) {
                sharedInstance = new PopupBoardWindow.PopupBoardWindowUI();
            }
            return sharedInstance;
        }


        @Override
        public void paint(Graphics g, JComponent c)
        {
            PopupBoardWindow pbw = (PopupBoardWindow)c;
            //label.setText("Helllo World");
            //pbw.insertDiagramText(label);
            int ply = pbw.trackMouse();
            String fen = pbw.replay(ply);
            boolean flip = AbstractApplication.theUserProfile.getBoolean("board.flip");
            if (flip)
                fen = StringUtil.reverse(fen);

            String[] old_border = pbw.enc.setBorder(null);
            String text = DiagramNode.toString(fen, pbw.enc);
            pbw.enc.setBorder(old_border);

            Graphics2D g2d = (Graphics2D)g;
            g2d.setFont(pbw.font);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0,0, c.getWidth(), c.getHeight());

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.BLACK);
            //g.drawString("Hello World", 20, 20);
            //g.drawString(fen, 20, 40);
            StringTokenizer tok = new StringTokenizer(text,"\n");
            for(int line=0; tok.hasMoreTokens(); line++)
                g2d.drawString(tok.nextToken(), 0,(line+1)*FONT_SIZE);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            PopupBoardWindow pbw = (PopupBoardWindow)c;
            return new Dimension(8*FONT_SIZE,8*FONT_SIZE);
        }
    }

    protected String replay(int ply)
    {
        if (engpanel.plugin!=null)
            pos.setup(engpanel.plugin.applPosition);
        else
            pos.setup(Application.theApplication.theGame.getPosition());

        ArrayList<Move> moves = null;
        if (engpanel.analysis!=null)
            moves = engpanel.analysis.moves[pvidx];
        else if (engpanel.bookmoves!=null)
            moves = engpanel.bookmoves.moves[pvidx];

        if (moves==null || moves.isEmpty())
            return "?";

        for(int i=0; i<moves.size() && i<ply; i++)
            pos.doMove(moves.get(i));
        //  now we are at then end of line
        return pos.toString(Board.SIMPLE_FEN);
    }

    protected int trackMouse()
    {
        JoStyledLabel pv = engpanel.getPvLabel(pvidx,false,false);
        assert(pv!=null);
        Point mouse = pv.mouseLocation;
        int ply = engpanel.findPly(pv,mouse);
        return ply;
    }


    protected EnginePanel engpanel;
    protected int pvidx;

    protected static Position pos = new Position();
    protected static int FONT_SIZE = 32;

    protected Font font;
    protected FontEncoding enc;
}
