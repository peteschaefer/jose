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

package de.jose.window;

import de.jose.profile.UserProfile;
import de.jose.Language;
import de.jose.view.input.JoStyledLabel;

import javax.swing.*;

/**
 * FRCWarningDialog
 *
 * @author Peter Schäfer
 */
public class FRCWarningDialog
{
	private static boolean shownInSession = false;

	public static void reset()
	{
		shownInSession = false;
	}

	public static boolean showWarning(UserProfile profile)
	{
		if (shownInSession) return false;
		if (! profile.getBoolean("show.frc.warning",true)) return false;

		String title = Language.get("warning.engine");
		String message = "<html>"+Language.get("warning.engine.no.frc")+"<br><br>";

		JCheckBox dontShowAgain;
		Box box = Box.createVerticalBox();
		box.add(new JoStyledLabel(message));
		box.add(dontShowAgain = new JCheckBox(Language.get("warning.engine.off")));

		JOptionPane.showMessageDialog(JoFrame.theActiveFrame,
				box, title, JOptionPane.WARNING_MESSAGE);

		profile.set("show.frc.warning", !dontShowAgain.isSelected());

		return (shownInSession=true);
	}

}
