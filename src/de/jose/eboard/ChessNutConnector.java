package de.jose.eboard;

import de.jose.view.IBoardAdapter;
import com.chessnut.EasyLink;

import javax.swing.*;

public class ChessNutConnector extends EBoardConnector implements EasyLink.IRealTimeCallback
{
    public ChessNutConnector(IBoardAdapter anAppBoard) {
        super(anAppBoard);
    }

    public boolean doConnect()
    {
        if (!EasyLink.AVAILABLE)
            return false;
        if (EasyLink.connect()==0)
            return false;

        EasyLink.setRealtimeCallback(this);
        EasyLink.switchRealTimeMode();
        EasyLink.led(EasyLink.NO_LEDS);
        return true;
    }

    public void doDisconnect()
    {
        EasyLink.disconnect();
    }


    @Override
    public void realTimeCallback(String fen) {
        //  called from E-Board
        if (super.setExplodedFen(fen,board[0].fen) > 0)
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    synchFromBoard();
                }
            });
        //  invokeLater avoids threading issues with GUI thread
    }

    public void doShowLeds(String leds)
    {
        EasyLink.led(leds);
    }
}
