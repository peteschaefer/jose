/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @author Peter Schäfer
 */

public class CommandTimer
        extends Timer
{
    private String command;

    public CommandTimer(String command, int delay, ActionListener listener)
    {
        super(delay, listener);
        this.command = command;
    }

    protected void fireActionPerformed(ActionEvent e)
    {
        e = new ActionEvent(e.getSource(),e.getID(), command, e.getWhen(),e.getModifiers());
        super.fireActionPerformed(e);
    }
}
