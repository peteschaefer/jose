/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */


package de.jose.view.style;

import de.jose.Application;
import de.jose.Language;
import de.jose.Util;
import de.jose.profile.UserProfile;
import de.jose.view.colorchooser.JoColorButton;
import de.jose.view.input.JoBigLabel;
import de.jose.view.input.MoveFormatList;
import de.jose.view.input.LanguageList;
import de.jose.view.input.JoButtonGroup;
import de.jose.view.JoLineBorder;
import de.jose.window.JoDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.event.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A panel for editing text styles.
 *
 * @author Peter Sch�fer
 */
public class StyleChooser
		extends JPanel
		implements ListSelectionListener, TreeSelectionListener, ChangeListener, ItemListener
{
	/**	the style context to be edited	*/
	protected JoStyleContext styles;

	protected JPanel editPanel;

	/**	currently selecte Style	*/
	protected Style currentStyle;

	/** set if at least one style is modified */
	protected boolean dirty;

	/**	this list shows all available styles in a list	*/
	protected JTree styleTree;
	/** maps styles names to TreeNodes
	 * Map<String,TreeNode>
	 */
	protected Map styleNodeMap;

	/**
	 * don't display font samples larger than that:
	 */
	private static final int MIN_SAMPLE_SIZE  = 7;
	private static final int MAX_SAMPLE_SIZE = 22;

	/**	this popup show available fonts	*/
	protected FontList fontList;
	protected JSpinner fontSize;
    protected JToggleButton fontBold, fontItalic;
    protected JoColorButton fontColor;
    protected FontPreview fontPreview,fontPreviewScreen;
	protected JLabel fontPreviewExplain;
	protected MoveFormatList moveFormat;
	protected LanguageList moveLanguage;
	protected JRadioButton useFontTrue,useFontFalse;
	protected JoButtonGroup useFont;

	/** models for FontList */
	protected DefaultComboBoxModel textFontModel, figFontModel, inlineFontModel, diagFontModel, symbolFontModel;

	public StyleChooser()
	{
		createLayout();
		startListen();
	}

	private void createLayout()
	{
		setLayout(new BorderLayout());

		JPanel leftPanel = createLeftPanel();
		JPanel rightPanel = createRightPanel();

		boolean dark = Application.theApplication.isDarkLookAndFeel();
		updateUI(dark);

		add(leftPanel,BorderLayout.CENTER);
		add(rightPanel,BorderLayout.EAST);
	}

	private JComponent createStyleTree()
	{
		styleTree = new JTree() {
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};
		CellRenderer rend = new CellRenderer(new Dimension(160, 20));
		styleTree.setCellRenderer(rend);
		styleTree.setDragEnabled(false);
		styleTree.setEditable(false);
		styleTree.setVisibleRowCount(12);
		styleTree.setRootVisible(true);
		styleTree.setShowsRootHandles(false);
		styleTree.setBackground(Color.white);

		JScrollPane scroll = new JScrollPane(styleTree,
		            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBackground(Color.white);
		scroll.getViewport().setBackground(Color.white);

		CompoundBorder border2 = new CompoundBorder(
				new BevelBorder(BevelBorder.LOWERED),
				new EmptyBorder(8,8,4,4)
		);
		scroll.setBorder(border2);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.white);
		panel.add(scroll,BorderLayout.CENTER);
		return panel;
	}

	private JPanel createOptionPanel()
	{
		JPanel notationPanel = JoDialog.newGridPane();
		notationPanel.setBorder(
				//new CompoundBorder(
					new BevelBorder(BevelBorder.LOWERED));
				//	new TitledBorder(Language.get("dialog.option.doc.move.format"))));

		//  notation
		moveFormat = new MoveFormatList();
		GridBagConstraints labelOne = (GridBagConstraints) JoDialog.LABEL_ONE.clone();
		labelOne.ipadx=60;
		notationPanel.add(JoDialog.newLabel("move.format"), JoDialog.gridConstraint(labelOne,0,0,1));
		notationPanel.add(moveFormat, JoDialog.gridConstraint(JoDialog.ELEMENT_TWO,1,0,3));

		//  figurine font
		notationPanel.add(useFontTrue=JoDialog.newRadioButton("figurine.usefont.true"),
		                    JoDialog.gridConstraint(JoDialog.ELEMENT_ROW,0,1,4));
		//  plain text figurines
		moveLanguage = new LanguageList(Language.getList("fig.langs"));
		moveLanguage.setName("figurine.language");

		notationPanel.add(useFontFalse=JoDialog.newRadioButton("figurine.usefont.false"),
		                    JoDialog.gridConstraint(JoDialog.LABEL_ONE_LEFT,0,2,1));
		notationPanel.add(moveLanguage, JoDialog.gridConstraint(JoDialog.ELEMENT_TWO,1,2,3));

		useFont = new JoButtonGroup("figurine.usefont");
		useFont.add(useFontTrue);
		useFont.add(useFontFalse);

		//optionPanel.add(notationPanel, JoDialog.gridConstraint(JoDialog.ELEMENT_ROW,0,0,4));
/*
		if (true) {
			antiAliasing = JoDialog.newCheckBox("doc.panel.antialias");
			optionPanel.add(antiAliasing, JoDialog.gridConstraint(JoDialog.ELEMENT_ROW,0,1,4));
		}
 */
		return notationPanel;
	}

	private JPanel createLeftPanel()
	{
		JComponent styleScroller = createStyleTree();
		fontPreview = createPreview("font.sample",Color.white);
		fontPreviewScreen = createPreview("font.sample.screen",UIManager.getColor("background"));
		fontPreviewExplain = new JLabel(Language.get("dialog.option.dark.explain"));
		fontPreviewExplain.setBorder(
				new CompoundBorder(
						new BevelBorder(BevelBorder.LOWERED),
						new EmptyBorder(8,8,8,8)));

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(styleScroller, BorderLayout.CENTER);

		JPanel previewPanel = new JPanel();
		previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
		previewPanel.add(fontPreview);
		previewPanel.add(fontPreviewExplain);
		previewPanel.add(fontPreviewScreen);
		leftPanel.add(previewPanel, BorderLayout.SOUTH);

		return leftPanel;
	}

	public void updateUI(boolean dark)
	{
		fontPreviewExplain.setVisible(dark);
		fontPreviewScreen.setVisible(dark);

		Color selBgColor = Color.lightGray;UIManager.getColor("Table.selectionBackground");
		((CellRenderer)styleTree.getCellRenderer()).setBackgroundSelectionColor(selBgColor);
		fontList.setSelectionBackground(selBgColor);
	}

	public void forPrintOnly(boolean print)
	{
		fontPreviewExplain.setVisible(!print);
		fontPreviewScreen.setVisible(!print);
	}

	private JPanel createRightPanel()
	{
		JPanel editPanel = createEditPanel();
		JPanel optionPane = createOptionPanel();

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(editPanel,BorderLayout.CENTER);
		rightPanel.add(optionPane,BorderLayout.SOUTH);
		return rightPanel;
	}

	private JPanel createEditPanel()
	{
		editPanel = JoDialog.newGridPane();

		fontList = new FontList(new Dimension(180,20));
//		fontList.setPreferredSize(new Dimension(180,20));
		fontList.setVisibleRowCount(9);

        fontSize = new JSpinner();
		((SpinnerNumberModel)fontSize.getModel()).setMinimum(1);
		fontSize.setPreferredSize(new Dimension(64,24));

        editPanel.add(JoDialog.newLabel("font.name"), JoDialog.LABEL_ONE_NOPAD);
		GridBagConstraints constr = JoDialog.gridConstraint(JoDialog.ELEMENT_TWO, 1, 0, 1, GridBagConstraints.BOTH);
		constr.weighty = 20.0;
		editPanel.add(new JScrollPane(fontList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				constr);

        editPanel.add(JoDialog.newLabel("font.size"), JoDialog.LABEL_ONE_NOPAD);
        editPanel.add(fontSize, JoDialog.gridConstraint(JoDialog.ELEMENT_TWO_SMALL,1,1,1));

		Box box1 = Box.createHorizontalBox();
        fontBold = JoDialog.newToggleButton("font.bold");
		fontBold.setFont(fontBold.getFont().deriveFont(Font.BOLD));
        box1.add(fontBold);

        fontItalic = JoDialog.newToggleButton("font.italic");
		fontItalic.setFont(fontItalic.getFont().deriveFont(Font.ITALIC));
        box1.add(fontItalic);

        fontColor = new JoColorButton("font.color");
        box1.add(fontColor);
		editPanel.add(box1, JoDialog.gridConstraint(JoDialog.ELEMENT_TWO,1,2,1));

		editPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
//		add(editPanel, BorderLayout.EAST);
		return editPanel;
	}

	private FontPreview createPreview(String text, Color bgColor)
	{
		FontPreview preview = new FontPreview(Language.get(text),null);
		preview.setPreferredSize(new Dimension(40,72));
		preview.setBackground(bgColor);
		preview.setBorder(new CompoundBorder(
		                //new BevelBorder(BevelBorder.RAISED, Color.lightGray,Color.darkGray),
						new BevelBorder(BevelBorder.LOWERED),
		                new EmptyBorder(2,12,2,2)));

//		fontPreview.setBorder(
//		        new JoLineBorder(JoLineBorder.ALL, Color.lightGray, 8, 10,16,10,10));

		return preview;
	}

	protected void startListen()
	{
		//	setup listeners
		styleTree.addTreeSelectionListener(this);
		fontList.addListSelectionListener(this);

		fontSize.addChangeListener(this);
		fontBold.addItemListener(this);
		fontItalic.addItemListener(this);
		fontColor.addItemListener(this);

		useFontTrue.addChangeListener(this);
		useFontFalse.addChangeListener(this);
		moveLanguage.addItemListener(this);
	}

	protected void stopListen()
	{
		//	setup listeners
		styleTree.removeTreeSelectionListener(this);
		fontList.removeListSelectionListener(this);

		fontSize.removeChangeListener(this);
		fontBold.removeChangeListener(this);
		fontItalic.removeChangeListener(this);
		fontColor.removeChangeListener(this);

		useFontTrue.removeChangeListener(this);
		useFontFalse.removeChangeListener(this);
		moveLanguage.removeItemListener(this);
	}

	public boolean isDirty()		 { return dirty; }

	public void apply(JoStyleContext theStyles, boolean editOriginal)
	{
		if (!editOriginal)
			theStyles.copyFrom(styles);
		dirty = false;
	}

	protected DefaultMutableTreeNode createTree(Style parent, Map nodeMap)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(parent);
		node.setUserObject(parent.getName());
		nodeMap.put(parent.getName(),node);

		java.util.List children = JoStyleContext.getChildren(parent);
		if (children!=null) {
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Style child = (Style)i.next();
				node.add(createTree(child,nodeMap));
			}
		}
		return node;
	}

	public boolean revert(JoStyleContext theStyles, int moveFormat, boolean editOriginal)
	{
		if (editOriginal)
        	styles = theStyles;
		else  {
			if (styles==null) styles = new JoStyleContext();
			styles.copyFrom(theStyles);
		}

		Style newSelect = null;
		if (currentStyle!=null)
			newSelect = styles.getStyle(currentStyle.getName());

		Style rootStyle = styles.getStyle("base");

		boolean firstTime = (styleNodeMap==null);
		if (firstTime) {
			//  initialize styleTree
			styleNodeMap = new HashMap();

			TreeNode rootNode = createTree(rootStyle,styleNodeMap);
			((DefaultTreeModel)styleTree.getModel()).setRoot(rootNode);
		}

		if (newSelect!=null)
			selectStyle(newSelect);
		else
			selectStyle(rootStyle);

		boolean figurineFont = Util.toboolean(rootStyle.getAttribute("figurine.usefont"));
		useFontTrue.setSelected(figurineFont);
		useFontFalse.setSelected(!figurineFont);

		moveLanguage.setValue(styles.getFigurineLanguage());

		if (moveFormat>=0) setMoveFormat(moveFormat);

		repaint();
		updateViews();
		dirty = false;
		return firstTime;
	}

	public void expand(String styleName)
	{
		TreePath path = findPath(styleName);
		styleTree.expandPath(path);
	}

	protected TreePath findPath(Style style)
	{
		return findPath(style.getName());
	}

	protected TreePath findPath(String styleName)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)styleNodeMap.get(styleName);
		if (node!=null) {
			TreeNode[] path = node.getPath();
			return new TreePath(path);
		}
		else
			return null;
	}

	public void selectStyle(Style style)
	{
//		if (style==currentStyle) return;
		currentStyle = style;
		updateViews();
		TreePath path = findPath(style);
		styleTree.setSelectionPath(path);
	}

	public void selectStyle(String styleName)
	{
		Style style = styles.getStyle(styleName);
		if (style!=null) selectStyle(style);
	}

	public void setFigurineFont(boolean useFigurines)
	{
		set("base","figurine.usefont", Util.toBoolean(useFigurines));
	}

	public void setFigurineLanguage(String lang)
	{
		set("base","figurine.language",lang);
	}

	public int getMoveFormat()
	{
		return moveFormat.getFormat();
	}

	public void setMoveFormat(int format)
	{
		moveFormat.setFormat(format);
	}

	protected void updateViews()
	{
		try {
			stopListen();

			if (JoFontConstants.isFigurine(currentStyle)) {
				if (figFontModel==null) figFontModel = FontList.createFigurineFontModel(16,true);
				if (fontList.getModel()!=figFontModel)
					fontList.setModel(figFontModel);
			}
			else if (JoFontConstants.isDiagram(currentStyle)) {
				if (diagFontModel==null) diagFontModel = FontList.createDiagramFontModel(16,true);
				if (fontList.getModel()!=diagFontModel)
					fontList.setModel(diagFontModel);
			}
			else if (JoFontConstants.isInline(currentStyle)) {
				if (inlineFontModel==null) inlineFontModel = FontList.createInlineFontModel(16,true);
				if (fontList.getModel()!=inlineFontModel)
					fontList.setModel(inlineFontModel);
			}
			else if (JoFontConstants.isSymbol(currentStyle)) {
				if (symbolFontModel==null) symbolFontModel = FontList.createSymbolFontModel(16,true);
				if (fontList.getModel()!=symbolFontModel)
					fontList.setModel(symbolFontModel);
			}
			else {
				if (textFontModel==null) textFontModel = FontList.createTextFontModel(16,true);
				if (fontList.getModel()!=textFontModel)
					fontList.setModel(textFontModel);
			}

			fontList.setSelectedFont(JoFontConstants.getFontFamily(currentStyle));

			fontSize.setValue(JoFontConstants.getFontSize(currentStyle));
			fontBold.setSelected(JoFontConstants.isBold(currentStyle));
			fontItalic.setSelected(JoFontConstants.isItalic(currentStyle));
			fontColor.setColor(JoFontConstants.getForeground(currentStyle));

			fontPreview.setAntiAliasing(true);
			fontPreview.setStyle(currentStyle,styles.getFontScale());

			fontPreviewScreen.setAntiAliasing(true);
			fontPreviewScreen.setStyle(currentStyle,styles.getFontScale());

		} finally {
			startListen();
		}
	}

	public void styleUpdated(Style style)
	{
		/**	invalidate font sample	*/
		styleTree.repaint();
		fontPreview.repaint();
		if (fontPreviewScreen.isVisible())
			fontPreviewScreen.repaint();
	}
	protected boolean updateAttribute(Style style, Object key, Object value)
	{
		return updateAttribute(style,key,value,null);
	}

	protected boolean updateAttribute(Style style, Object key, Object value, Object defaultValue)
	{
		Object thisValue = style.getAttribute(key);
		if (Util.equals(thisValue,value))
			return (thisValue!=null);	//	nothing changed

		style.removeAttribute(key);
		Object parentValue = style.getAttribute(key);
		if (parentValue==null)
			parentValue = defaultValue;

		boolean isdefined = ! Util.equals(parentValue,value);
		if (isdefined)
			style.addAttribute(key,value);
		//	else: keep parent value ! */ ;

		styleUpdated(style);
		dirty = true;
		return isdefined;
	}

	public boolean set(String styleName, Object key, Object value)
	{
		Style style = styles.getStyle(styleName);
		if (style!=null)
			return updateAttribute(style,key,value);
		else
			throw new IllegalArgumentException("Style "+styleName+" not found");
	}

	public void stateChanged(ChangeEvent e)
	{
		//  TODO use CSS.Attributes
		if (e.getSource()==fontSize) {
			Number size = (Number)fontSize.getValue();
			updateAttribute(currentStyle, StyleConstants.FontSize, size);
			JoFontConstants.removeFontScaleFactor(currentStyle);
		}
		if (e.getSource()==useFontTrue) {
			boolean useFont = useFontTrue.isSelected();
			if (useFont!=styles.useFigurineFont()) {
				styles.setFigurineFont(useFont);
				dirty = true;
			}
		}
		if (e.getSource()==useFontFalse) {
			boolean useFont = !useFontFalse.isSelected();
			if (useFont!=styles.useFigurineFont()) {
				styles.setFigurineFont(useFont);
				dirty = true;
			}
		}

	}

	public void valueChanged(TreeSelectionEvent e)
	{
		if (e.getSource()==styleTree) {
			/**	update panel	*/
			TreePath path = styleTree.getSelectionPath();
			if (path!=null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				String styleName = (String)node.getUserObject();
				if (styleName!=null) selectStyle(styleName);
			}
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource()==fontList)
		{
			String family = fontList.getSelectedFont();
			updateAttribute(currentStyle, StyleConstants.FontFamily, family);
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange()!=ItemEvent.SELECTED
			&& e.getStateChange()!=ItemEvent.DESELECTED) return;

		if (e.getSource()==moveLanguage)
		{
			String language = moveLanguage.getSelectedLanguage();
			if (!language.equals(styles.getFigurineLanguage())) {
				styles.setFigurineLanguage(language);
				dirty = true;
			}
		}
		if (e.getSource()==fontBold) {
			boolean isBold = fontBold.isSelected();
			updateAttribute(currentStyle, StyleConstants.Bold, Boolean.valueOf(isBold));
		}
		if (e.getSource()==fontItalic) {
			boolean isItalic = fontItalic.isSelected();
			updateAttribute(currentStyle, StyleConstants.Italic, Boolean.valueOf(isItalic));
		}
		if (e.getSource()==fontColor) {
			Color color = fontColor.getColor();
			updateAttribute(currentStyle, StyleConstants.Foreground, color, Color.black);
		}
	}

	class CellRenderer
			extends DefaultTreeCellRenderer
			implements TreeCellRenderer
	{
		protected Style style;
		protected FontSample sample;

		 public CellRenderer(Dimension preferredSize)
		 {
			 setOpaque(true);
			 setPreferredSize(preferredSize);
			 setBorder(new EmptyBorder(0,0,0,0));
			 setIcon(null);
			 setDisabledIcon(null);
			 setClosedIcon(null);
			 setOpenIcon(null);
			 setLeafIcon(null);
//			 setIconTextGap(0);
//			 setHorizontalTextPosition(JLabel.LEFT);
			 sample = new FontSample(7,22);
		 }

		public void paint(Graphics g) {
			super.paint(g);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
		                                              boolean selected, boolean expanded,
		                                              boolean leaf, int row, boolean hasFocus)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			String styleName = (String)node.getUserObject();
			if (styleName!=null && styles!=null)
				style = styles.getStyle(styleName);
			else
				style = null;

			return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}

		protected void paintComponent(Graphics g)
		{
			if (style!=null) {
				Rectangle bounds = getBounds();
				String name = Language.get(style.getName());
				boolean dark = !getBackground().equals(Color.white);
				sample.setStyle(style, styles.getFontScale(), name, 0, dark); // JoStyleContext.getNestLevel(style));
				sample.paint(g, bounds, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			}
			else
				g.drawString("?",0,20);
		}

	}


}
