/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.plugin;

import de.jose.chess.StringMoveFormatter;


public class CraftyPlugin
		extends XBoardPlugin
{
	/**	PV from endgame table bases are split across several lines
	 * 	collect them
	 * */
	StringBuffer tbLine = new StringBuffer();


	public int getParseCapabilities()
	{
		return 	AnalysisRecord.DEPTH +
				AnalysisRecord.EVAL +
				AnalysisRecord.ELAPSED_TIME +
				AnalysisRecord.NODE_COUNT +
		        AnalysisRecord.NODES_PER_SECOND +
		        AnalysisRecord.INFO +
				1;  //  we can show exactly 1 PV
	}

	public void parseAnalysis(String input, AnalysisRecord rec)
	{
/* Crafty sample output:
	 7    -78      86 165614  4. ... Bg7 5. Bb2 O-O 6. cxd5 Qxd5 7. e4 Qd6
	 7    -78      93 183550  4. ... Bg7 5. Bb2 O-O 6. cxd5 Qxd5 7. e4 Qd6
	 8    -58     135 339844  4. ... Bg7 5. Ra2 O-O 6. c5 Nc6 7. Nf3 b6 8. d4 bxc5 9. dxc5
	 8    -58     149 386995  4. ... Bg7 5. Ra2 O-O 6. c5 Nc6 7. Nf3 b6 8. d4 bxc5 9. dxc5
	 9    -69     301 958365  4. ... Bg7 5. Bb2 dxc4 6. dxc4 O-O 7. Qxd8 Rxd8 8. Be5 Na6 9. Nd2
	 9    -69     321 1037685  4. ... Bg7 5. Bb2 dxc4 6. dxc4 O-O 7. Qxd8 Rxd8 8. Be5 Na6 9. Nd2
	 7    -25     112 245246  5. ... Qxd5 6. Nc3 Qc5 7. Bd2 Ng4 8. Nh3 Nc6
	 7    -25     138 328658  5. ... Qxd5 6. Nc3 Qc5 7. Bd2 Ng4 8. Nh3 Nc6
	 8     31     198 589781  5. ... Qxd5 6. e4 Qd4 7. Ra2 Bg4 8. Nf3 Qd6 9. Be3
	 8     15     231 668689  5. ... Nxd5 6. Bb2 Nf6 7. e4 Bg4 8. f3 Bd7 9. Nd2
	 8     15     285 869764  5. ... Nxd5 6. Bb2 Nf6 7. e4 Bg4 8. f3 Bd7 9. Nd2
	 9      0     346 1164365  5. ... Nxd5 6. e4 Bg7 7. Ra2 Nc3 8. Nxc3 Bxc3+ 9. Bd2 Bxd2+ 10. Rxd2 Bd7
	 9      0     526 1936069  5. ... Nxd5 6. e4 Bg7 7. Ra2 Nc3 8. Nxc3 Bxc3+ 9. Bd2 Bxd2+ 10. Rxd2 Bd7
	10     30     614 2259455  5. ... Nxd5 6. e4 Bg7 7. Ra2 Nc3 8. Nxc3 Bxc3+ 9. Bd2 Bxd2+ 10. Rxd2 Bd7 11. Nf3
	10     30    1166 4443401  5. ... Nxd5 6. e4 Bg7 7. Ra2 Nc3 8. Nxc3 Bxc3+ 9. Bd2 Bxd2+ 10. Rxd2 Bd7 11. Nf3
0	123       10

	 7     19      93    198316  2. cxd5 Nf6 3. Nf3 Bg4 4. Qa4+ Nbd7 5. Nc3 e6 6. dxe6 Bxe6
tab  depth score*100
	               time*100
				         nodes    variation

*/
		if (input==null || input.length()==0) {
			rec.clear();
			return;
		}

		rec.modified = 0;
		rec.ply = enginePosition.gamePly();
		char[] chars = input.toCharArray();

		if (chars[0]=='\t') {
			rec.depth = AnalysisRecord.parseInt(chars,1,2);
			rec.modified |= AnalysisRecord.DEPTH;

			rec.selectiveDepth = AnalysisRecord.UNKNOWN;
			int eval = AnalysisRecord.parseInt(chars,3,7);
			if (eval > 32000) {
				//	engine mates in ... plies
				int plies = 32768-eval;
				rec.eval[0] = AnalysisRecord.WHITE_MATES+plies;
			}
			else if (eval < -32000) {
				int plies = -32768-eval;
				rec.eval[0] = AnalysisRecord.BLACK_MATES-plies;
			}
			else
				rec.eval[0] = eval;

			rec.eval[0] = adjustPointOfView(rec.eval[0]);

			/**
			 * Crafty reports
			 */

			rec.modified |= AnalysisRecord.EVAL;

			rec.elapsedTime = AnalysisRecord.parseLong(chars,10,8)*10;
			//  Crafty return time in 1/100 of a second; we return milliseconds
			rec.modified |= AnalysisRecord.ELAPSED_TIME;
			rec.nodes = AnalysisRecord.parseLong(chars,18,10);
			rec.nodesPerSecond = Math.round(((double)rec.nodes*1000)/((double)rec.elapsedTime));
			rec.modified |= AnalysisRecord.NODE_COUNT | AnalysisRecord.NODES_PER_SECOND;

			StringBuffer line = rec.getLine(0);
			line.setLength(0);
			if (27 < chars.length) {
				StringMoveFormatter.replaceDefaultPieceChars(chars,27,chars.length-27);
				/*	replace english figurines by local	*/
				line.append(chars,27,chars.length-27);
			}
			rec.setPvModified(0);
		}
		else {
			//	uses endgame table bases
			rec.depth = AnalysisRecord.ENDGAME_TABLE;
			rec.selectiveDepth = AnalysisRecord.UNKNOWN;
			rec.eval[0] = AnalysisRecord.UNKNOWN;
			rec.elapsedTime = AnalysisRecord.UNKNOWN;
			rec.nodes = AnalysisRecord.UNKNOWN;

			/**	lines are split */
			if (input.startsWith("1.")) tbLine.setLength(0);
			tbLine.append(input);

			StringBuffer line = rec.getLine(0);
			line.setLength(0);
			line.append(tbLine);

			rec.modified |= AnalysisRecord.DEPTH;
			rec.setPvModified(0);  //  first PV modified
		}
	}

	public void disableBook()
	{
		//  TODO
	}

	public boolean isBookEnabled()
	{
		//  TODO
		return super.isBookEnabled();
	}

	/**
	 * output from Crafty that should be ignored (not appear in the Engine Panel)
	 * @param s
	 * @return
	 */
	protected boolean filterOutput(String s)
	{
		if (s.startsWith("setboard"))
			return false;		//	ignore setboard echo
		if (s.startsWith("Analyze Mode"))
			return false;		//	ignore message when entering analyze mode
		if (s.startsWith("learning"))
			return false;
		if (s.endsWith("time control"))
			return false;
		return super.filterOutput(s);
	}
}
