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

package de.jose.view;

import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import de.jose.*;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.chess.Move;
import de.jose.image.Surface;
import de.jose.jo3d.*;
import de.jose.profile.UserProfile;
import de.jose.util.file.FileUtil;

import javax.media.j3d.*;
import javax.media.j3d.Locale;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BoardView3D
		extends BoardView
		implements MouseListener, ICallbackListener, ComponentListener, MouseWheelListener
{

//	public static final boolean MAP_DEMO = true;
//	TextureMapCanvas tmCanvas;

    /** model file for clock    */
    public static final String CLOCK_MODEL_FILE = "clock.j3ds";
    /** emissive color for hilited squares  */
    public static final Color3f HILITE_COLOR = new Color3f(0.5f,0.5f,1.0f);

/**	scaling factor applied to the scene	*/
	public static double scaleFactor = 0.00075;

	/**	the J3D universe	 */
	private SimpleUniverse universe;
	/**	the Canvas	 */
	protected JoCanvas3D canvas;
	/**	the J3D view	 */
	private View view;
	/**	orbit behavior (controls camera angle)	 */
	private OrbitBehavior orbit;
	/**	the top Branch Group */
	private BranchGroup scene;
    /** the clock */
    private Clock3d clock;
	/**	this transform scales the scene to fit	 */
	private TransformGroup parent;
	/**	pickig behavior */
	private PickBehavior pick;
    /** pool of PieceAnimators (pool) */
    private PieceAnimator[] animators;
	/**	clip to the inner board (use for shadows)	*/
	private GlobalClip boardClip;
	/**	box that surrounds the reflection geometry	*/
//	private ClipSwitch backBox;
	/**	the coordinates (shown or not)	 */
	private Switch coordinateGroup;
	/**	the frame (may be replaced) */
	private BranchGroup frameGroup;
	/**	the background */
	private Background backgroundNode;
	/**	the squares */
	private Group squareGroup;
	/** hitn arrows */
	private BranchGroup hintGroup;
    /** currently hilited square    */
    private int currentHilite = -1;
	/**	name of model file */
	private String modelFile;
	/**	error sound	*/
//	private javax.media.j3d.Sound errorSound;

	/**	bounds for scheduling */
	private Bounds worldBounds;

	/**	appearances */
	/**	squares */
	private Appearance appLight,appDark, appLightHi,appDarkHi;
	/**	frame */
	private Appearance appFrame;

	/**	directional lights
	 * */
	private AmbientLight			ambientLight;
	private DirectionalLight[]		directionalLight = new DirectionalLight[4];


	/**	max. number of frames per second	 */
	private static double FPS = 2500.0;
	/**	used to compute random offsets for textures */
	private static Random rand = new Random();

	/**	references to PieceGroups	 */
	private PieceGroup[] pieceGroups;
	/**	unused PieceGroups (weak references)	 */
	private Stack[] unusedWhiteGroups;
	private Stack[] unusedBlackGroups;

	/**	the PieceGroup that is currently dragged	 */
	private PieceGroup dragGroup;

	public BoardView3D(IBoardAdapter board)
	{
		super(board);
		addComponentListener(this);
		setDoubleBuffered(Version.useDoubleBuffer());
		setLayout(new BorderLayout());
	}


	/**	called once at startup
	 */
	public void init() throws Exception
	{
		init(SimpleUniverse.getPreferredConfiguration());
	}

    public void setClipDistance(double front, double back)
    {
        view.setFrontClipDistance(front);
        view.setBackClipDistance(back);
    }

	public void init(GraphicsConfiguration gc) throws Exception
	{
		//	Canvas3D will deal with buffering
		pieceGroups = new PieceGroup[OUTER_BOARD_SIZE];
		unusedWhiteGroups = new Stack[KING+1];
		unusedBlackGroups = new Stack[KING+1];
		for (int p=PAWN; p<=KING; p++)
		{
			unusedWhiteGroups[p] = new Stack();
			unusedBlackGroups[p] = new Stack();
		}

		appLight = Util3D.createAppearance(PolygonAttributes.CULL_BACK, 128.0f, 0f);
		appDark  = Util3D.createAppearance(PolygonAttributes.CULL_BACK, 128.0f, 0f);
        appLightHi = Util3D.createAppearance(PolygonAttributes.CULL_BACK, 128.0f, 0f);
        appDarkHi = Util3D.createAppearance(PolygonAttributes.CULL_BACK, 128.0f, 0f);

		//	squares are supposed to be visible from both sides: turn off face-culling

		Vector4f tplane = new Vector4f(+1.0f, 1.0f,-1.0f, 0.0f);
		Transform3D scale = Util3D.getScale(0.005,0.010,1.0);

		appFrame = Util3D.createAppearance(null, tplane, scale);
		//	turn the T projection plane so that stretching is minimal
		//	and repetitions look less obvious

		UserProfile prf = AbstractApplication.theUserProfile;

		boolean runContinuous = prf.getBoolean("board.3d.flyby");
		if (canvas==null) {
			if (gc == null) {
				GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();

				/* We need to set this to force choosing a pixel format
				   that support the canvas.
				*/
				template.setRedSize(4);
				template.setGreenSize(4);
				template.setBlueSize(4);
				template.setDepthSize(4);
				
				template.setSceneAntialiasing(GraphicsConfigTemplate3D.UNNECESSARY);//GraphicsConfigTemplate3D.PREFERRED);
				template.setStereo(GraphicsConfigTemplate3D.UNNECESSARY);

				if (true/*TODO Version.OpenGL*/)
					template.setDoubleBuffer(GraphicsConfigTemplate3D.PREFERRED);       //  prevents flicker
				else
					template.setDoubleBuffer(GraphicsConfigTemplate3D.UNNECESSARY);     //  not necessary on DirectX

				gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
			        							getDefaultScreenDevice().getBestConfiguration(template);
//				gc = SimpleUniverse.getPreferredConfiguration();
			}

	       	canvas = new JoCanvas3D(gc, 0.9f, !runContinuous);
			canvas.setFrameRecord(AbstractApplication.theAbstractApplication.showFrameRate);
			canvas.addMouseListener(this);
		}
		/*	DON'T ...	*/
		//canvas.setDoubleBufferEnable(false);
		//c.getGraphicsContext3D().setBufferOverride(true);
		//c.getGraphicsContext3D().setFrontBufferRendering(true);

		add(BorderLayout.CENTER, canvas);

		worldBounds =
            new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 0.1);

		MouseQueue mouseQueue = new MouseQueue(canvas);
		orbit = new OrbitBehavior(true, mouseQueue);
	    orbit.setSchedulingBounds(worldBounds);
		orbit.addListener(this);
        addMouseWheelListener(this);

		// Create a simple scene and attach it to the virtual universe
		modelFile = prf.getString("board.3d.model","fab100.j3df");

		//	camera position
		orbit.setDistance(prf.getDouble("board.3d.camera.distance"));
		orbit.setLatitude(prf.getDouble("board.3d.camera.latitude"));
		orbit.setLongitude(prf.getDouble("board.3d.camera.longitude"));
		orbit.cropDistance(0.1, 400);

		// Create the root of the branch graph
		scene = new BranchGroup();
		createSceneGraph(canvas,mouseQueue);

		orbit.addListener(boardClip);

		if (universe==null)
			universe = new SimpleUniverse(canvas);
		//u.setJ3DThreadPriority(Thread.MIN_PRIORITY);

		view = canvas.getView();
		synch(false);

		// add mouse behaviors to the ViewingPlatform
		ViewingPlatform viewingPlatform = universe.getViewingPlatform();

		// This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
 //      viewingPlatform.setNominalViewingTransform();

		//	setting accurate clipping distances increases z-buffer accuracy
		//	the ratio should be between 100 and 1000
        setClipDistance(JoCanvas3D.FRONT_CLIP_DISTANCE,JoCanvas3D.BACK_CLIP_DISTANCE);
		//	back/front ratio = 600

		//	transparent objects are not supposed to overlap: no need to sort
		view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_NONE);

		viewingPlatform.setViewPlatformBehavior(orbit);

        //  set up Audio Device
//        AudioDevice audioDev = universe.getViewer().createAudioDevice();

		updateProfile(prf);
//	provisional
//		parent.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

		scene.compile();
		universe.addBranchGraph(scene);

		orbit.apply();

		Point3d eye = new Point3d();
		orbit.getEyePoint(eye);
		boardClip.updateAll(eye);

		if (!runContinuous) {
			view.stopView();
			view.setMinimumFrameCycleTime((int)(1000.0/FPS));
			/*	the view is NOT running continuously; frames are only rendered ...
				1. in response to an update event (see CapturingCanvas3D.paint())
				2. during user interaction (see mousePressed())
				3. during animation
			*/
		}

//		updateProfile(prf);
		//	computes the LOD parameters suitable for the current window size
		recalcSize();
	}

	public void closeScene()
	{
		removeAll();
		view.stopView();

		Enumeration locales = universe.getAllLocales();
		while (locales.hasMoreElements()) {
			Locale locale = (Locale)locales.nextElement();
			Enumeration graphs = locale.getAllBranchGraphs();
			while (graphs.hasMoreElements())
				locale.removeBranchGraph((BranchGroup)graphs.nextElement());
		}

		ambientLight = null;
		animators = null;
		appDark = null;
		appFrame = null;
		appLight = null;
		backgroundNode = null;
//		boardClip.removeAllChildren();
		boardClip = null;
		clock = null;
		coordinateGroup = null;
		Arrays.fill(directionalLight,null);
		dragGroup = null;
		frameGroup = null;
		orbit = null;
		parent = null;
		pick = null;
		pieceGroups = null;
		scene = null;
		squareGroup = null;
		hintGroup = null;
		unusedBlackGroups = null;
		unusedWhiteGroups = null;
		view = null;
		worldBounds = null;
	}

	/**
	 * release all resources
	 */
	public void close()
	{
		removeAll();

		if (view!=null) view.stopView();
		view = null;
		canvas = null;
        if (universe!=null) universe.removeAllLocales();
		universe = null;
	}

	public void setDefaultOrbit(boolean flipped)
	{
		orbit.setLatitude(3*Math.PI/8);
		if (flipped)
			orbit.setLongitude(Math.PI);
		else
			orbit.setLongitude(0);
		orbit.setDistance(2.0);
		orbit.apply();
	}

    public void setMinimumFrameCycleTime(long millis)
    {
        view.setMinimumFrameCycleTime(millis);
    }

	/**
	 * repaint() does not work as expected; it would not update a stopped view
	 */
	public void refresh(boolean stopAnimation)
	{
		/**	unfortunately, repaint() will not force repainting the canvas
		 *	while it is stopped
		 */
		synch(true);
		canvas.paint(getGraphics());
	}

	/**
	 * makes a screen shot and stores it on disk
	 */
	public void capture(File target)
	{
		canvas.capture(target);
		refresh(true);
	}

	public void captureImage(MessageListener callbackTarget, boolean transparent)
	{
		//  transparent background is not supported, obvisouly
		canvas.capture(callbackTarget);
		refresh(true);
	}

	public void paintComponent(Graphics g)
	{
		synch(false);
		super.paintComponent(g);
	}

	public void mouseEntered(MouseEvent evt)		{ }

	public void mouseExited(MouseEvent evt)			{ }

	public void mouseClicked(MouseEvent evt)		{ }

	public void mousePressed(MouseEvent evt)		{
		if (! ContextMenu.isTrigger(evt))
		{
			synch(true);
			canvas.startView(true);
			//	this is meant to kick off rendering immediately
		}
	}

	public void mouseReleased(MouseEvent evt)		{
        canvas.stopView(false);
        //  this will stop rendering (with a delay of abt. 2 frames)
	}

	public void activate(boolean active)
	{
		if (!active)
			canvas.doStopView(true);
	}


	/**	store persistent data in User Profile
	 */
	public void storeProfile(UserProfile prf)
	{
		//	store camera angle
		if (orbit!=null) {
			prf.set("board.3d.camera.distance", new Double(orbit.getDistance()));
			prf.set("board.3d.camera.latitude", new Double(orbit.getLatitude()));
			prf.set("board.3d.camera.longitude", new Double(orbit.getLongitude()));
		}
	}

    public void showClock(boolean on)
    {
        if (clock!=null)
	    	clock.show(on);
	    else if (on)
	    	throw new IllegalStateException("where is the clock ?");
    }

	public void componentHidden(ComponentEvent e)
	{
		canvas.doStopView(true);
	}

	public void componentShown(ComponentEvent e)
	{	}

	public void componentMoved(ComponentEvent e)
	{	}

	public void componentResized(ComponentEvent e)
	{
		recalcSize();
	}

	/**	called after the view is resized
	 *	handled by J3D automatically
	 *
	 * 	the LOD thresholds are adapted
	 */
	public void recalcSize()
	{
//		System.err.println("graphics memory: "+ FileUtil.formatFileSize(canvas.availableGraphicsMemory()));
		Point2d screenSize = getCanvasSize();
		for (int i=0; i < pieceGroups.length; i++)
			if (pieceGroups[i] != null)
				pieceGroups[i].adaptLOD(screenSize);
	}

	public void updateOne(Graphics2D g, int file, int row)
	{
	}

	/**	we don't use buffering
	 * 	Java3D draws directly into the graphics memory
	 */
	public Graphics2D getBufferGraphics()
	{
		return null;
	}


	public synchronized void synch(boolean redraw)
	{
		//	first, recycle as many PieceGroups as possible
		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				int piece = board.pieceAt(square);
				PieceGroup pg = pieceGroups[square];

				if (pg != null && pg.getPiece() != piece)
					set(null,0,square);
			}

		super.synch(redraw);

		//	detach remaining, recycled PieceGroups (these pieces have actually been removed)
		for (int p=PAWN; p<=KING; p++)
		{
			Iterator i = unusedWhiteGroups[p].iterator();
			while (i.hasNext())
			{
				PieceGroup pg = (PieceGroup)i.next();
				if (pg.isLive())
					parent.removeChild(pg);
			}

			i = unusedBlackGroups[p].iterator();
			while (i.hasNext())
			{
				PieceGroup pg = (PieceGroup)i.next();
				if (pg.isLive())
					parent.removeChild(pg);
			}
		}

		//	update pick-ability
/*		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				PieceGroup pg = pieceGroups[square];

				if (pg != null)
					pg.setPickable(panel.canMove(square));
			}
*/	}

	private boolean needsReset(Appearance app, Surface srf, boolean anisotropic)
	{
		return needsReset(app,srf,-1f,anisotropic);
	}

	private boolean needsReset(Appearance app, Surface srf, float transparency, boolean anisotropic)
	{
		if (srf.useTexture())
		{
			Texture2D tex = TextureCache3D.getTexture(srf.texture);
			if (app.getTexture()==null || app.getTexture() != tex)
				return true;	//	texture changes are critical

            if (anisotropic != (tex.getAnisotropicFilterMode()==Texture.ANISOTROPIC_SINGLE_VALUE))
            	return true;
				//	anisotropic changes are critical, cause they can't be performed on a live node
		}
		else
		{
			Color3f c3f = new Color3f(srf.color);
			//	color changes are uncritical, right ?
		}

		if (transparency >= 0f) {
			TransparencyAttributes ta = app.getTransparencyAttributes();
			if (ta==null || ta.getTransparency() != transparency)
				return true;
		}

		return false;
	}

	private void updateAppearance(Appearance app, Surface srf, boolean anisotropic)
		throws FileNotFoundException
	{
		updateAppearance(app, srf, -1f, anisotropic, ColoringAttributes.SHADE_GOURAUD);
	}

	private void updateAppearance(Appearance app, Surface srf, boolean anisotropic, int shadeModel)
		throws FileNotFoundException
	{
		updateAppearance(app, srf, -1f, anisotropic, shadeModel);
	}


	private void updateAppearance(Appearance app, Surface srf, float transparency, boolean anisotropic)
		throws FileNotFoundException
	{
		updateAppearance(app,srf,transparency,anisotropic, ColoringAttributes.SHADE_GOURAUD);
	}

	private void updateAppearance(Appearance app, Surface srf, float transparency, boolean anisotropic,
	                              int shadeModel)
		throws FileNotFoundException
	{
		int oldTexSize = 256;
		int newTexSize = 256;

		if (app.getTexture() != null)
			oldTexSize = app.getTexture().getHeight();

//		Util3D.setIgnoreVertexColors(app,true);
		Util3D.setShadeModel(app,shadeModel);

		if (srf.useTexture())
		{
			Texture2D tex = TextureCache3D.getTexture(srf.texture);
			app.getMaterial().setDiffuseColor(Util3D.white3f);
			app.getMaterial().setLightingEnable(true);
			app.setTexture(tex);
			newTexSize = tex.getHeight();

            if (anisotropic != (tex.getAnisotropicFilterMode()==Texture.ANISOTROPIC_SINGLE_VALUE))
            {
                //  must not change the anisotropy mode on a live object
                //  got to create a new copy
                tex = (Texture2D)tex.cloneNodeComponent(false);
                tex.setAnisotropicFilterMode(anisotropic ? Texture.ANISOTROPIC_NONE:Texture.ANISOTROPIC_SINGLE_VALUE);
                tex.setAnisotropicFilterDegree(4.0f);   //  which value is appropriate ??
            }
		}
		else
		{
			Color3f c3f = new Color3f(srf.color);
			app.getMaterial().setDiffuseColor(c3f);
			app.getMaterial().setLightingEnable(true);
			app.setTexture(null);
			//	gradient paint is not supported in 3D
		}

		if (transparency >= 0f)
			Util3D.setTransparency(app,transparency);

		/*	adjust the texture transform so that texture coordinates always fit to 256
			that means: textures of different sizes can be applied but the level of detail
			stays the same
		*/
		if (oldTexSize != newTexSize)
		{
			double scale = (double)oldTexSize/newTexSize;
			TextureAttributes attr = app.getTextureAttributes();
			if (attr==null) {
				app.setTextureAttributes(attr = new TextureAttributes());
				attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
				attr.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);
			}

			Transform3D tf = new Transform3D();
			attr.getTextureTransform(tf);
			Util3D.scale(tf, scale,scale,1.0);
			attr.setTextureTransform(tf);
		}
	}

	private void updateAppearance(PieceGroup pg, Surface srf, boolean shadow,
	                              boolean reflection, boolean anisotropic)
		throws FileNotFoundException
	{
		updateAppearance(pg,srf,shadow,reflection,anisotropic, ColoringAttributes.SHADE_GOURAUD);
	}

	private void updateAppearance(PieceGroup pg, Surface srf, boolean shadow,
	                              boolean reflection, boolean anisotropic, int shadeModel)
		throws FileNotFoundException
	{
		Appearance app = pg.getAppearance();
		updateAppearance(app, srf, anisotropic, shadeModel);

		app = pg.getReflectionAppearance();
		if (app!=null) updateAppearance(app, srf, anisotropic);	//	ColoringAttributes.SHADE_FLAT);

		//	update reflection decal, too
		pg.enableShadow(shadow);
		pg.enableReflection(reflection);
	}


	/**
	 * get the available graphic card memory
	 */
	public int getGraphicsCardMemory()
	{
		return canvas.getGraphicsConfiguration().getDevice().getAvailableAcceleratedMemory();
	}

	/**
	 *
	 * @return true if a change in the profile requires the scene to be rebuilt;
	 * 	fals if the change can be safely do in the live graph
	 */
	public boolean needsReset(UserProfile prf)
	{
		//	new model ?
		String model = prf.getString("board.3d.model");
		if (!model.equals(modelFile))
		{
			/**	new geometry
			 *	we could do this on the live scene graph but it's not quite reliable
			 */
			return true;
		}

		//	lights
		Color3f col1 = new Color3f((Color)prf.get("board.3d.light.ambient"));
		Color3f col2 = new Color3f();

        if (ambientLight!=null) {
	        ambientLight.getColor(col2);
	        /**	new ambient light
	         * 	this can be safely done in the live scene graph
	         */
//			if (!col1.equals(col2))
//	        	return true;
        }

		col1 = new Color3f((Color)prf.get("board.3d.light.directional"));
		for (int i=0; i<directionalLight.length; i++) {
            if (directionalLight[i] != null) {
	            directionalLight[i].getColor(col2);
//				if (!col1.equals(col2))
//	        		return true;
            }
		}


        boolean anisotropic = Util3D.hasAnisotropicFiltering(canvas) && prf.getBoolean("board.3d.anisotropic");
//        System.out.println("anisotropic filtering = "+anisotropic);

		/**	this can be done on the live graph, right ?	*/

		//	frame
		if (needsReset(appFrame, (Surface)prf.get("board.3d.surface.frame"), anisotropic))
			return true;

		//	pieces
		boolean shadow = prf.getBoolean("board.3d.shadow");
		boolean reflection = prf.getBoolean("board.3d.reflection");

		if (needsReset(appLight, (Surface)prf.get("board.surface.light"), reflection ? 0.25f:0f, anisotropic))
			return true;
		if (needsReset(appDark, (Surface)prf.get("board.surface.dark"), reflection ? 0.25f:0f, anisotropic))
			return true;

		for (int i=0; i<pieceGroups.length; i++)
			if (pieceGroups[i] != null)
			{
				if (pieceGroups[i].isWhite()) {
					if (needsReset(pieceGroups[i].getAppearance(), (Surface)prf.get("board.surface.white"), anisotropic))
						return true;
				}
				else {
					if (needsReset(pieceGroups[i].getAppearance(), (Surface)prf.get("board.surface.black"), anisotropic))
						return true;
				}
			}

		for (int i=PAWN; i < unusedWhiteGroups.length; i++)
			for (int j=0; j < unusedWhiteGroups[i].size(); i++)
			{
				PieceGroup pg = (PieceGroup)unusedWhiteGroups[i].get(j);
				if (needsReset(pg.getAppearance(), (Surface)prf.get("board.surface.white"), anisotropic))
					return true;
			}

		for (int i=PAWN; i < unusedBlackGroups.length; i++)
			for (int j=0; j < unusedBlackGroups[i].size(); i++)
			{
				PieceGroup pg = (PieceGroup)unusedBlackGroups[i].get(j);
				if (needsReset(pg.getAppearance(), (Surface)prf.get("board.surface.white"), anisotropic))
					return true;
			}

//		if (backBox!=null)
//			backBox.setEnable(0,false);
//			backBox.setEnable(0,reflection);

		//	background
		Surface srf = (Surface)prf.get("board.surface.background");
		if (srf.useTexture())
		{
			ImageComponent2D i2d = TextureCache3D.getImage(srf.texture);
			if (backgroundNode.getImage() != i2d)
				return true;
		}
		else
		{
			col1 = new Color3f(srf.color);
			if (backgroundNode.getImage()!=null)
				return true;
		}

		return false;
	}

	/**
	 * called when the user edits his preferences
	 *
	 * experience shows that updates to geometry or textures are a bit unreliable
	 * they may produce rendering errors - so it is better to rebuilt the scene completely from scratch
	 *
	 * @param prf
	 */
	public void updateProfile(UserProfile prf)
		throws Exception
	{
		//	new model ?
		String model = prf.getString("board.3d.model","fab100.j3df");
		if (!model.equals(modelFile))
		{
			//	rebuild scene graph
			//	remove all but squares and clip
            //  TODO setup from scratch !
			Enumeration children = parent.getAllChildren();
			while (children.hasMoreElements())
			{
				Node child = (Node)children.nextElement();
				if (child instanceof PieceGroup || child == frameGroup)
					parent.removeChild(child);
			}

			boardClip.clearClips();

            Jo3DFileReader reader = get3dFileReader(modelFile = model);
			setUpScene(parent, reader, pick);

			for (int i=0; i<Constants.OUTER_BOARD_SIZE; i++)
				pieceGroups[i] = null;
		}

		//	pieces
		boolean shadow = prf.getBoolean("board.3d.shadow");
		boolean reflection = prf.getBoolean("board.3d.reflection");

		//	lights
		Color col = (Color)prf.get("board.3d.light.ambient");
        if (ambientLight !=null)
		    ambientLight.setColor(new Color3f(col));

		col = (Color)prf.get("board.3d.light.directional");
		Color3f c3f = new Color3f(col);
		for (int i=0; i<directionalLight.length; i++)
            if (directionalLight[i] != null) {
			    directionalLight[i].setColor(c3f);
	            if (i >= directionalLight.length/2)
		            directionalLight[i].setEnable(reflection);		//	enable/disable reflection lighting
            }

		int knightAngle = prf.getInt("board.3d.knight.angle",0);
		rotateKnights(knightAngle);

        boolean anisotropic = Util3D.hasAnisotropicFiltering(canvas) && prf.getBoolean("board.3d.anisotropic");
//        System.out.println("anisotropic filtering = "+anisotropic);

		//	frame
		updateAppearance(appFrame, (Surface)prf.get("board.3d.surface.frame"), anisotropic);
//		updateAppearance(appCoords, (Surface)prf.get("board.surface.coords"), anisotropic);

		updateAppearance(appLight, (Surface)prf.get("board.surface.light"), reflection ? 0.25f:0f, anisotropic);
		updateAppearance(appDark, (Surface)prf.get("board.surface.dark"), reflection ? 0.25f:0f, anisotropic);

        updateAppearance(appLightHi, (Surface)prf.get("board.surface.light"), reflection ? 0.25f:0f, anisotropic);
        updateAppearance(appDarkHi, (Surface)prf.get("board.surface.dark"), reflection ? 0.25f:0f, anisotropic);

        appLightHi.getMaterial().setDiffuseColor(HILITE_COLOR);
        appDarkHi.getMaterial().setDiffuseColor(HILITE_COLOR);

		for (int i=0; i<pieceGroups.length; i++)
			if (pieceGroups[i] != null)
			{
				if (pieceGroups[i].isWhite())
					updateAppearance(pieceGroups[i], (Surface)prf.get("board.surface.white"), shadow, reflection, anisotropic);
				else
					updateAppearance(pieceGroups[i], (Surface)prf.get("board.surface.black"), shadow, reflection, anisotropic);
			}

		for (int i=PAWN; i < unusedWhiteGroups.length; i++)
			for (int j=0; j < unusedWhiteGroups[i].size(); i++)
			{
				PieceGroup pg = (PieceGroup)unusedWhiteGroups[i].get(j);
				updateAppearance(pg, (Surface)prf.get("board.surface.white"), shadow, reflection, anisotropic);
			}

		for (int i=PAWN; i < unusedBlackGroups.length; i++)
			for (int j=0; j < unusedBlackGroups[i].size(); i++)
			{
				PieceGroup pg = (PieceGroup)unusedBlackGroups[i].get(j);
				updateAppearance(pg, (Surface)prf.get("board.surface.white"), shadow, reflection, anisotropic);
			}

//		if (backBox!=null)
//			backBox.setEnable(0,false);
//			backBox.setEnable(0,reflection);

		//	background
		Surface srf = (Surface)prf.get("board.surface.background");
		if (srf.useTexture())
		{
			ImageComponent2D i2d = TextureCache3D.getImage(srf.texture);
			backgroundNode.setImage(i2d);
		}
		else
		{
			backgroundNode.setColor(new Color3f(srf.color));
			backgroundNode.setImage(null);
			//	gradient paint is not supported in 3D
		}

        //  showClock ?
        showClock(prf.getBoolean("board.3d.clock"));
		//	show coordinates ?
		showCoords(prf.getBoolean("board.coords"));
		//	flip board ?
		flip(prf.getBoolean("board.flip"));
        //  hilite squares ?
        hiliteSquares = prf.getBoolean("board.hilite.squares");


        //  FSAA
        boolean fsaa = Util3D.hasFullScreenAntiAliasing(canvas) && prf.getBoolean("board.3d.fsaa");
        view.setSceneAntialiasingEnable(fsaa);
//        System.out.println("FSAA = "+fsaa);

		boardClip.updateAll();

		showAnimationHints = prf.getBoolean("board.animation.hints");
	}

	public void doFlip(boolean on)
	{
		if (on)
			orbit.setLongitude(Math.PI);	//	black view angle
		else
			orbit.setLongitude(0);			//	white view angle
		orbit.apply();
	}

	public void doShowCoords(boolean on)
	{
		if (on)
			coordinateGroup.setWhichChild(Switch.CHILD_ALL);
		else
			coordinateGroup.setWhichChild(Switch.CHILD_NONE);
	}


	/**	set a piece at a given square
	 */
	public void set(Graphics2D ignore_me, int piece, int square)
	{
		PieceGroup pg = pieceGroups[square];

		if (pg==null)
		{
			if (piece > 0)
				addPieceGroup(piece,square);
		}
		else if (pg.getPiece() != piece)
		{
			//	put the old one into the recycling list
			removePieceGroup(square);
			if (piece==EMPTY)
				parent.removeChild(pg);
			else if (piece > 0)
				addPieceGroup(piece,square);
			//	TODO remove captured pieces gracefully (with an animation ;-)
		}

		pieces[square] = piece;
	}

	private void addPieceGroup(int piece, int square)
	{
		PieceGroup pg = pieceGroups[square] = newPieceGroup(piece);
		if (!pg.isLive()) {
			parent.addChild(pg);
        }
		pg.moveTo(square);
	}

	private PieceGroup removePieceGroup(int square)
	{
		PieceGroup pg = pieceGroups[square];
		int pc = pg.getPiece();
		if (EngUtil.isWhite(pc))
			unusedWhiteGroups[EngUtil.uncolored(pc)].push(pg);
		else
			unusedBlackGroups[EngUtil.uncolored(pc)].push(pg);

		pieceGroups[square] = null;
		return pg;
	}

	private PieceGroup newPieceGroup(int pc)
	{
		//	first examine the recycling list
		Stack recycle;
		if (EngUtil.isWhite(pc))
			recycle = unusedWhiteGroups[EngUtil.uncolored(pc)];
		else
			recycle = unusedBlackGroups[EngUtil.uncolored(pc)];

		PieceGroup pg = null;
		if (! recycle.isEmpty())
		{
			pg = (PieceGroup)recycle.pop();
			pg.adaptLOD(getCanvasSize());
		}
		else try {
			//	create a new one
			pg = new PieceGroup(pc,this, boardClip, worldBounds, getCanvasSize(), orbit);
			//	and set its appearance
			UserProfile prf = AbstractApplication.theUserProfile;
			boolean shadow = prf.getBoolean("board.3d.shadow");
			boolean reflection = prf.getBoolean("board.3d.reflection");
            boolean anisotropic = Util3D.hasAnisotropicFiltering(canvas) && prf.getBoolean("board.3d.anisotropic");
			if (pg.isWhite())
				updateAppearance(pg, (Surface)prf.get("board.surface.white"), shadow, reflection, anisotropic);
			else
				updateAppearance(pg, (Surface)prf.get("board.surface.black"), shadow, reflection, anisotropic);
//			if (EngUtil.uncolored(pc)!=KNIGHT)
//				pg.compile();   //  don't compile Knights because their static transform might be modified (rotated) ?
		} catch (Exception ex) {
			//	can't help it ;-(
			Application.error(ex);
			pg = null;
		}

		if (pg!=null && EngUtil.uncolored(pc)==KNIGHT) {
		    int angle = AbstractApplication.theUserProfile.getInt("board.3d.knight.angle",0);
		    pg.rotate(angle*Math.PI/180.0);
		}
		return pg;
	}

	public void rotateKnights (int angle)
	{
		double dangle = angle*Math.PI/180.0;
		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				PieceGroup pg = pieceGroups[EngUtil.square(file,row)];
				if (pg!=null && EngUtil.uncolored(pg.getPiece())==KNIGHT)
					pg.rotate(dangle);
			}
	}

	/**	move a piece
	 */
	public void move(int startSquare, int piece, int endSquare,
					 float duration, int frameRate)
	{
		move(pieceGroups[startSquare], PieceGroup.squareCenter(endSquare), calcMillis(duration),frameRate);

		set(EMPTY, startSquare);
		set(piece, endSquare);
	}

	/**	make a complete move (incl. promotion and castling)
	 */
	public void move(Move mv, float time)
	{
		long millis = calcMillis(time);
		if (showAnimationHints) showHint(mv, millis, ANIM_HINT_COLOR);
		finishMove(mv, millis);
	}

	protected void doShowHint(Hint hnt)
	{
		Point2d p1 = PieceGroup.squareCenter(hnt.from);
		Point2d p2 = PieceGroup.squareCenter(hnt.to);

		float length = (float)Math.abs(p1.distance(p2));
		float width = PieceGroup.squareSize/6;
		float tip = 2*width;

		float[] coords = BoardView2D.createArrowFloatCoordinates(length,width,tip);

		Arrow3D arrow = new Arrow3D(p1,p2, coords,PieceGroup.squareSize/4, hnt.color);
		hnt.implData = arrow;
		hintGroup.addChild(arrow);
		canvas.paint(getGraphics());
	}

	protected void doHideHint(Hint hnt)
	{
		Arrow3D arrow = (Arrow3D)hnt.implData;
		hintGroup.removeChild(arrow);
		canvas.paint(getGraphics());
	}

	protected void doHideAllHints(int count)
	{
		hintGroup.removeAllChildren();
		canvas.paint(getGraphics());
	}

	/**
	 * @return an unused PieceAnimator from the pool
	 */
	protected PieceAnimator getAnimator()
	{
		for (int i=0; i<animators.length; i++)
			if (! animators[i].isActive())
				return animators[i];
		return null;
	}

	private void move(PieceGroup pg, Point2d location, long millis, int frameRate)
	{
		/**	set up an animation behavior
		 */
        canvas.startView(false);    //  may be called from Behavior method
        PieceAnimator animator = getAnimator();
		if (animator==null)
		{
			System.err.println("run out of animators ("+animators.length+")");
			pg.moveTo(location.x, location.y, pg.getZ());
			canvas.stopView(false);
		}
		else {
			animator.setTarget(pg);
			animator.setEndPoint(location.x, location.y, pg.getZ());
			animator.setDuration(millis);
			animator.start();
//			animator.setCapture(capture);
			//  animator will call back when finished; then the view will be stopped
		}
	}

	/**	called when the user selects an entry from the promotion popup
	 */
	public void promotionPopup(int piece)
	{
		if (piece <= 0)
			mouseEnd(mouseMove.from, 0);				//	cancel move
		else
			mouseEnd(mouseMove.to, piece);			//	make move
	}

	/**	@return an icon for display in the promotion popup
	 */
	public ImageIcon getPopupIcon(int piece)
	{
		UserProfile prf = AbstractApplication.theUserProfile;
		String font = prf.getString("font.diagram");
		Surface whiteSrf = (Surface)prf.get("board.surface.white");
		Surface blackSrf = (Surface)prf.get("board.surface.black");

		return new ImageIcon(BoardView2D.getPieceImage(font, 64, piece, whiteSrf, blackSrf, null,false));
	}


	/**	callback from PickBehavior
	 */
	public void behaviorCallback(Object behavior, int actionCode, Object params)
	{
		switch (actionCode)
		{
		case PickBehavior.PICK_PIECE:
			mouseStart((PieceGroup)params);
			armCollision((PieceGroup)params,true);
			break;

		case PickBehavior.DROP_PIECE:
			if (dragGroup != null)
				mouseEnd(dragGroup.getLocation2d(), 0);
			//  else: ???
			armCollision((PieceGroup)params,false);
            if (hiliteSquares)
                hiliteSquare(-1);
			break;

        case PickBehavior.DRAG_PIECE:
            if (hiliteSquares) {
                Point p = dragGroup.getLocation2d();
                int square = PieceGroup.findSquare(p.x,p.y);

                Move mv = new Move(mouseStartSquare,square);
                if (board.isLegal(mv))
                    hiliteSquare(square);
                else
                    hiliteSquare(-1);
            }
            break;

        case ICallbackListener.DEACTIVATE:
            if (behavior instanceof PieceAnimator) {
                canvas.stopView(false);
            }
            break;
		}
	}

	/**
	 * called when the mouse is pressed
	 */
	public void mouseStart(PieceGroup pg)
	{
		dragGroup = pg;
		Point p = pg.getLocation2d();
		mouseStartPoint = p;
		mouseStartSquare = PieceGroup.findSquare(p.x,p.y);
	}



	/**
	 * called when the mouse is released
	 */
	private void mouseEnd(Point p, int promoPiece)
	{
		int destSquare = PieceGroup.findSquare(p.x,p.y);
		mouseEnd(destSquare, promoPiece);
	}

	private void mouseEnd(int destSquare, int promoPiece)
	{
		if (destSquare != 0 &&
			destSquare == mouseStartSquare)
		{	//	piece dropped (touch-move rule not enforced!)
			move(dragGroup, PieceGroup.squareCenter(mouseStartSquare), calcMillis(0.1f),0);
		}
		else {
			//	end dragging
			mouseMove = new Move(mouseStartSquare,destSquare);
			mouseMove.setPromotionPiece(promoPiece);
			if (board.isLegal(mouseMove)) {
				/*	legal move	*/
				finishMove(mouseMove,calcMillis(0.1f));
				board.userMove(mouseMove);
			}
			else if (couldBePromotion(mouseMove)) {
				/*	show popup	*/
				Point p = getScreenLocation(destSquare);
				showPromotionPopup(board.movesNext(), p);
				/*	when the user selects an item, mouseEnd will be called again	*/
				return;
			}
			else {
				/*	illegal move */
/*				if (errorSound != null)
                    errorSound.setEnable(true);
				else
*/				de.jose.Sound.play("sound.error");
				move(dragGroup, PieceGroup.squareCenter(mouseMove.from), calcMillis(0.5f),0);
			}
		}

		mouseStartSquare = 0;
		mouseMove = null;
		dragGroup = null;
	}


	/**
	 * calculate the location of a square <b>in screen coordinates</b>
	 */
	public Point getScreenLocation(int square)
	{
		Point3d local = new Point3d();
		local.x = (EngUtil.fileOf(square)-FILE_A-4)*PieceGroup.squareSize;
		local.y = (EngUtil.rowOf(square)-ROW_1-3)*PieceGroup.squareSize;
		local.z = 0;

		return Util3D.localToAWT(canvas, coordinateGroup, local);
	}

	private void armCollision(PieceGroup pg, boolean on)
	{
		for (int i=0; i<pieceGroups.length; i++)
			if (pieceGroups[i] != null && pieceGroups[i] != pg)
				pieceGroups[i].getShape().setCollidable(true);
	}

    protected void hiliteSquare(int square)
    {
        if ((square < A1) && (currentHilite < A1) || (square == currentHilite)) {
            //  nothing to do: fine
            return;
        }

        if (currentHilite >= A1)
            setHilite(currentHilite,false);
        currentHilite=square;
        if (currentHilite >= A1)
            setHilite(currentHilite,true);
    }

    private void setHilite(int square, boolean on)
    {
        int file = EngUtil.fileOf(square)-FILE_A;
        int row = EngUtil.rowOf(square)-ROW_1;
        int index = row*8+file;

        Shape3D shape = (Shape3D)squareGroup.getChild(index);
        boolean light = ((file+row+8)%2)==1;

        Appearance app = on ? (light ? appLightHi:appDarkHi) : (light ? appLight:appDark);
        shape.setAppearance(app);
    }

    protected void finishMove(Move mv, long millis)
    {
        if (pieceGroups[mv.from]==null)
            throw new IllegalStateException(mv.toString()+" origin square must not be empty");

        switch (mv.castlingMask()) {    //  FRC
        case WHITE_KINGS_CASTLING:
            finishCastlingMove(mv, G1, F1, millis, calcMillis(0.4f));
            break;
        case WHITE_QUEENS_CASTLING:
            finishCastlingMove(mv, C1, D1, millis, calcMillis(0.6f));
            break;
        case BLACK_KINGS_CASTLING:
            finishCastlingMove(mv, G8, F8, millis, calcMillis(04.f));
            break;
        case BLACK_QUEENS_CASTLING:
            finishCastlingMove(mv, C8, D8, millis, calcMillis(0.6f));
            break;

        default:
            move(pieceGroups[mv.from], PieceGroup.squareCenter (mv.to), millis, 0);

            if (mv.isEnPassant())
                set(EMPTY, mv.getEnPassantSquare());
            else if (mv.isCapture())
                set(EMPTY, mv.to);

            pieceGroups[mv.to] = pieceGroups[mv.from];
            pieceGroups[mv.from] = null;

            set(EMPTY,mv.from);

            if (mv.isPromotion ())
                set(mv.getPromotionPiece(), mv.to);
            else
                set(mv.moving.piece(), mv.to);  //  is this necessary ?
            break;
        }
    }

    private void finishCastlingMove(Move mv, int king_dest, int rook_dest,
                                    long king_millis, long rook_millis)
    {
        PieceGroup kgroup = pieceGroups[ mv.from];
        PieceGroup rgroup = pieceGroups[mv.to];

        move(kgroup, PieceGroup.squareCenter(king_dest), king_millis, 0);
        move(rgroup, PieceGroup.squareCenter(rook_dest), rook_millis,0);

        pieceGroups[mv.from] = null;
        pieceGroups[mv.to] = null;

        pieceGroups[king_dest] = kgroup;
        pieceGroups[rook_dest] = rgroup;
    }


	private Jo3DFileReader get3dFileReader(String fileName)
		throws IOException
	{
        /** first, look in jar file    */
        URL url = AbstractApplication.theAbstractApplication.getResource("3d/"+fileName);
        if (url==null)
            return null;
        if ("file".equalsIgnoreCase(url.getProtocol())) {
			File file = FileUtil.getFile(url);
            return new Jo3DFileReader(file);
		}
        else
            return new Jo3DFileReader(url);
	}

    public GlobalClip getGlobalClip()
    {
        return boardClip;
    }

	/**
	 * @return the location of the camera in scene coordinates
	 */
	public Point3f getEyePoint()
	{
		Point3f p = new Point3f(0,0,0);		//	the initial eye point
		orbit.apply(p);
		//	p is now in world coordinates
		p.x /= scaleFactor;
		p.y /= scaleFactor;
		p.z /= scaleFactor;
		System.out.println(p);
		return p;
	}

	public final JoCanvas3D getCanvas()			{ return canvas; }

	public Point2d getCanvasSize()
	{
		return new Point2d(canvas.getWidth(), canvas.getHeight());
	}

	public TransformGroup getViewTransformGroup()
	{
		return universe.getViewingPlatform().getMultiTransformGroup().getTransformGroup(0);
	}

	public BranchGroup addBehavior(Behavior behav)
	{
		Bounds bounds = new BoundingSphere(new Point3d(0,0,0), Double.MAX_VALUE);
		behav.setSchedulingBounds(bounds);
		behav.setEnable(true);
		return addObject(behav);
	}

    public BranchGroup addObject(Node node)
	{
        BranchGroup bg = new BranchGroup();
        bg.addChild(node);
        parent.addChild(bg);
        return bg;
    }

    public BranchGroup addObjects(Node node1, Node node2)
	{
        BranchGroup bg = new BranchGroup();
        bg.addChild(node1);
        bg.addChild(node2);
        parent.addChild(bg);
        return bg;
    }

	/**	set up the scene graph (incl. lights and pich behaviors)
	 */
    private void createSceneGraph(JoCanvas3D canvas, MouseQueue q)
		throws Exception
	{
		//	Set up picking
		BoundingSphere bounds = new BoundingSphere(/*new Point3d(0.0,0.0,0.0), 1000.0f*/);

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		parent = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(scaleFactor);
		parent.setTransform(t3d);
		parent.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		parent.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		parent.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		parent.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

		scene.addChild(parent);

        //  pick behavior for picking pieces
		pick = new PickBehavior(canvas,scene,parent,q, PickTool.BOUNDS);
		/*	picking geometry is more accurate but also more expensive
			(consider that a knight's geometry contains more than 15000 triangles)
		*/
		pick.setSchedulingBounds(bounds);
		pick.addListener(this);
		scene.addChild(pick);

        //  animation behaviors for animating pieces
		//	(up to 4 are working simultaneously)
        animators = new PieceAnimator[4];
		for (int i=0; i<animators.length; i++)
		{ 
			animators[i] = new PieceAnimator();
			animators[i].setSchedulingBounds(bounds);
			animators[i].addListener(this);
			scene.addChild(animators[i]);
		}

		//	load shapes from disk file
        Jo3DFileReader reader = get3dFileReader(modelFile);

		setUpScene(parent, reader, pick);
        //   add clock
		UserProfile prf = AbstractApplication.theUserProfile;
		boolean createClock = ! prf.getBoolean("board.3d.flyby") || prf.getBoolean("board.3d.clock");
		if (createClock) {
            Jo3DFileReader clockReader = get3dFileReader(CLOCK_MODEL_FILE);
            if (clockReader==null) throw new IOException("couldn't read "+CLOCK_MODEL_FILE);
        	clock = new Clock3d(canvas, AbstractApplication.theAbstractApplication.theClock, clockReader, pick);
        	parent.addChild(clock);
		}

		//	add squares
		float sz = PieceGroup.squareSize;
		squareGroup = new Group();
        squareGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        for (int row = -4; row < 4; row++)
    		for (int file = -4; file < 4; file++)
			{
				Shape3D square = createSquare(sz, file,row, 40.0f,
							(((file+row+8)%2)==1) ? appLight:appDark);
				squareGroup.addChild(square);
				Util3D.setPickCapabilities(square);
				pick.addZoomHandle(square);	// picking a square will trigger zooming
			}
		parent.addChild(squareGroup);

		//	add coordinates
		coordinateGroup = createCoordinateGroup();
		coordinateGroup.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
		parent.addChild(coordinateGroup);

		parent.setCapability(Group.ALLOW_LOCAL_TO_VWORLD_READ);
		boardClip = new GlobalClip(69);
		scene.addChild(boardClip);

		//  empty hint group
		hintGroup = new BranchGroup();
		hintGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		hintGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		parent.addChild(hintGroup);

		//	in reflection mode, the squares become semi-transparent
		//	and the reflected geometry becomes visible
		//	but we don't want to see the background shining through:
//		backBox = new ClipSwitch(boardClip);
//		backBox.addChild(createBackPlane(0));
//		backBox.addChild(createBackPlane(1));
//		backBox.addChild(createBackPlane(2));
//		backBox.addChild(createBackPlane(3));
//		backBox.addChild(createBackPlane(4));
//		parent.addChild(backBox);

		// Set up the background
        backgroundNode = new Background();
		backgroundNode.setImageScaleMode(Background.SCALE_REPEAT);
		backgroundNode.setCapability(Background.ALLOW_COLOR_WRITE);
		backgroundNode.setCapability(Background.ALLOW_IMAGE_WRITE);
		backgroundNode.setApplicationBounds(bounds);
        scene.addChild(backgroundNode);

		//	set up lighting bounds
		Bounds ambientLightSphere = new BoundingSphere(new Point3d(0,0,0), 5000);

//		BoundingLeaf ambientLightBounds = new BoundingLeaf(ambientLightSphere);
		/**
		 * Lighting is extremely buggy on Linux (OpenGL)
		 * most of the times, lights don't work at all ;-((
		 */

		double n = 5000;
		Bounds phantomLightBox = new BoundingBox(
				new Point3d(-n, -n, -n),
				new Point3d(+n, +n, 0));

//		BoundingLeaf phantomLightBounds = new BoundingLeaf(phantomLightBox);

		Bounds lightBox = new BoundingBox(
				new Point3d(-n, -n, 0),
				new Point3d(+n, +n, +n));

//		BoundingLeaf lightBounds = new BoundingLeaf(lightBox);
//		parent.addChild(ambientLightBounds);
//		parent.addChild(phantomLightBounds);
//		parent.addChild(lightBounds);

        // Set up the ambient light
        ambientLight = new AmbientLight(new Color3f(0.1f,0.1f,0.1f));
//        ambientLight.setInfluencingBoundingLeaf(ambientLightBounds);
		ambientLight.setInfluencingBounds(ambientLightSphere);
  	    ambientLight.setCapability(Light.ALLOW_COLOR_WRITE);
        scene.addChild(ambientLight);

        // Set up the directional lights
		Vector3f[] dirv = {
			new Vector3f(-0.80f, +1.00f, -0.80f),
			new Vector3f(+0.80f, -1.00f, -0.80f),
		};

		scene.addChild(directionalLight[0] = newDirectionalLight(dirv[0], lightBox));
		scene.addChild(directionalLight[1] = newDirectionalLight(dirv[1], lightBox));

		scene.addChild(directionalLight[2] = newDirectionalLight(Util3D.mirrorZ(dirv[0]), phantomLightBox));
		scene.addChild(directionalLight[3] = newDirectionalLight(Util3D.mirrorZ(dirv[1]), phantomLightBox));

		//	add sound
//		String url = "file://"+de.jose.Sound.getPath(de.jose.Sound.ERROR);
//		System.out.println(url);
/*			try {
				MediaContainer soundContainer;
				URL url = de.jose.Sound.getURL("sound.error");
				InputStream soundStream = url.openStream();

				if (soundStream != null) {
					soundContainer = new MediaContainer(soundStream);

					errorSound = new BackgroundSound();
					errorSound.setCapability(PointSound.ALLOW_ENABLE_WRITE);
					errorSound.setInitialGain(3.0f);
					errorSound.setSoundData(soundContainer);
					errorSound.setLoop(0);
					errorSound.setContinuousEnable(false);
					parent.addChild(errorSound);
				}
			} catch (IOException ioex) {
				//  don't mind
				errorSound = null;
			}
 */  }

	public void enablePicking(boolean on)
	{
		pick.setEnable(on);
	}

	private DirectionalLight newDirectionalLight(Vector3f dir)
	{
		DirectionalLight light = new DirectionalLight(Util3D.white3f, dir);
		light.setCapability(Light.ALLOW_COLOR_WRITE);
		light.setCapability(Light.ALLOW_STATE_WRITE);
		light.setEnable(true);
		light.setColor(Util3D.white3f);
		return light;
	}

	private DirectionalLight newDirectionalLight(Vector3f dir, BoundingLeaf bounds)
	{
		DirectionalLight light = newDirectionalLight(dir);
		light.setInfluencingBoundingLeaf(bounds);
		return light;
	}

	private DirectionalLight newDirectionalLight(Vector3f dir, Bounds bounds)
	{
		DirectionalLight light = newDirectionalLight(dir);
		light.setInfluencingBounds(bounds);
		return light;
	}

	/**	set up the 3D scene
	 */
	private void setUpScene(Group parent, Jo3DFileReader loader, PickBehavior pick)
		throws Exception
	{
		//	the board frame (first shape in file)
		Shape3D frame = loader.getShape("FRAME",0);
		frame.setAppearance(appFrame);
		frame.setCollidable(false);
		Util3D.setPickCapabilities(frame);

		//	the piece shapes
		//	how many levels of detail ?
		for (int p=PAWN; p<=KING; p++)
			for (int level=0; level < PieceGroup.MAX_LOD; level++)
			{
				Shape3D shape = loader.getShape(String.valueOf(EngUtil.pieceCharacter(p)), level);
				if (shape!=null)
					PieceGroup.addShape(p, level, shape, loader.getLODThreshhold(shape));
			}

		//	wrap frame into BranchGroup so that it can be replaced at runtime
		frameGroup = new BranchGroup();
		frameGroup.setCapability(BranchGroup.ALLOW_DETACH);
		frameGroup.setCollidable(false);

		frame.setCapability(Geometry.ALLOW_INTERSECT);	//	necessary ?
		frame.setPickable(false);

		for (double angle = 0; angle < 360; angle += 90)
		{
			Transform3D tf = new Transform3D();
			Util3D.rotZ(tf, angle/180*Math.PI);
			TransformGroup tg = new TransformGroup(tf);

			Shape3D shape = (Shape3D)frame.cloneTree(false);
			shape.setCollidable(false);
			tg.addChild(shape);
			pick.addOrbitHandle(shape);	// picking the frame will trigger orbiting
			frameGroup.addChild(tg);
		}

		parent.addChild(frameGroup);
	}

	/**
	 *	creates the coordinate letters; there are two solutions
	 * 	Text3D	craetes geometry (complex)
	 * 	Text2D	creates transparent textures (better)
	 *
	 */
	private Switch createCoordinateGroup()
	{
		Switch group = new Switch();
		group.setCapability(Switch.ALLOW_SWITCH_WRITE);
        group.setWhichChild(Switch.CHILD_NONE);

		for (int row = -4; row < 4; row++)
		{
			//	left border
			char c = (char)('1'+row+4);
			float x = -4*PieceGroup.squareSize-50;
			float y = (row+0.5f)*PieceGroup.squareSize-45;
			group.addChild(decalText(c,x,y,false));

			//	right border
			x = -x;
			y = (row+0.5f)*PieceGroup.squareSize+45;
			group.addChild(decalText(c,x,y,true));
		}

		for (int file = -4; file < 4; file++)
		{
			//	bottom border
			char c = (char)('A'+file+4);
			float x = (file+0.5f)*PieceGroup.squareSize-15;
			float y = -4*PieceGroup.squareSize-75;
			group.addChild(decalText(c,x,y,false));

			//	top border
			x = (file+0.5f)*PieceGroup.squareSize+15;
			y = -y;
			group.addChild(decalText(c,x,y,true));
		}

 		return group;
	}

	private TransformGroup decalText(char c, float x, float y, boolean rotate)
	{
		String s = String.valueOf(c);

		//	bottom border
		Transform3D tf = new Transform3D();
		Util3D.translate(tf, x,y,0f);
		if (rotate) Util3D.rotZ(tf, Math.PI);
		tf.setScale(256);	//	compensates for Text2D's scale factor

		Shape3D shape = new Text2D(s, new Color3f(Color.white), "SansSerif", 60, Font.PLAIN);
		shape.setCapability(Geometry.ALLOW_INTERSECT);		//	needed for picking
//			shape.setAppearance(appCoords);
		shape.setCollidable(false);

		//	offset the z-buffer
		PolygonAttributes polyattr = shape.getAppearance().getPolygonAttributes();
		if (polyattr==null) polyattr = new PolygonAttributes();
		polyattr.setPolygonOffset(-10f);
		shape.getAppearance().setPolygonAttributes(polyattr);

		TransformGroup tg = new TransformGroup(tf);
		tg.addChild(shape);
		return tg;
	}

	private static Shape3D createSquare(float size, float file, float row,
							 float height, Appearance app)
	{
		float ax = file*size;
		float ay = row*size;

		float as = (float)Math.IEEEremainder(rand.nextDouble(),1.0);
		float at = (float)Math.IEEEremainder(rand.nextDouble(),1.0);

		//	texture coordinates are shifted randomly (so that no two squares look exactly the same)
		Plane shp = new Plane(ax,ay,0.1f, ax+size,ay+size,0.1f,
				as,at, as+1.0f, at+1.0f, Plane.XY_PLANE);
		shp.setAppearance(app);
		shp.setPickable(false);
		shp.setCollidable(false);
        shp.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		return shp;
	}
/*
	private static Group createBackBox()
	{
		Appearance app = new Appearance();
		Material mat = new Material();
		mat.setEmissiveColor(white3f);
		app.setMaterial(mat);
		Util3D.setCullFace(app,PolygonAttributes.CULL_FRONT);

		Point3d a = new Point3d(-8*PieceGroup.squareSize,-8*PieceGroup.squareSize,-1000);
		Point3d b = new Point3d(+8*PieceGroup.squareSize,+8*PieceGroup.squareSize,    0);
		Box box = new Box(a,b);
		box.setAppearance(app);
		box.setPickable(false);
		box.setCollidable(false);
		box.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);

		LocalBoundingBox lbb = new LocalBoundingBox(box, a, b);
		box.setBoundsAutoCompute(false);
		box.setBounds(lbb);

		Group grp = new Group();
		grp.addChild(box);
		grp.setCapability(Shape3D.ALLOW_BOUNDS_READ);
		grp.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);
		grp.setBoundsAutoCompute(false);
		grp.setBounds(lbb);

		return grp;
	}
*/
	private static Group createBackPlane(int i)
	{
		double sz = 4.0*PieceGroup.squareSize;

		Point3d a,b;
		Plane p;

		switch (i) {
		case 0:
			a = new Point3d(-sz,-sz,-sz);
			b = new Point3d(+sz,+sz,-sz);
			p = new Plane(a,b, Plane.XY_PLANE);
			break;
		case 1:
			a = new Point3d(+sz,-sz,-sz);
			b = new Point3d(-sz,-sz,0.0);
			p = new Plane(a,b, Plane.XZ_PLANE);
			break;
		case 2:
			a = new Point3d(-sz,-sz,-sz);
			b = new Point3d(-sz,+sz,0.0);
			p = new Plane(a,b, Plane.YZ_PLANE);
			break;
		case 3:
			a = new Point3d(-sz,+sz,-sz);
			b = new Point3d(+sz,+sz,0.0);
			p = new Plane(a,b, Plane.XZ_PLANE);
			break;
		case 4:
			a = new Point3d(+sz,+sz,-sz);
			b = new Point3d(+sz,-sz,0.0);
			p = new Plane(a,b, Plane.YZ_PLANE);
			break;
		default:
			throw new IllegalArgumentException();
		}

		Appearance app = new Appearance();
		Material mat = new Material();
		PolygonAttributes pol = new PolygonAttributes();

		mat.setEmissiveColor(Util3D.white3f);
//		pol.setPolygonOffset(+3000000);
		pol.setPolygonOffsetFactor(+3000000);

		app.setMaterial(mat);
		app.setPolygonAttributes(pol);
		p.setPickable(false);
		p.setCollidable(false);
		p.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);
		p.setAppearance(app);

		LocalBoundingBox lbb = new LocalBoundingBox(p, a,b);
		p.setBoundsAutoCompute(false);
		p.setBounds(lbb);

		Group grp = new Group();
		grp.addChild(p);
		grp.setCapability(Group.ALLOW_BOUNDS_READ);
		grp.setCapability(Group.ALLOW_LOCAL_TO_VWORLD_READ);
		grp.setBoundsAutoCompute(false);
		grp.setBounds(lbb);

		return grp;
	}

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        synch(true);
        canvas.startView(true);
        //	this is meant to kick off the rendering immediately

        orbit.zoomWheel(e);

        canvas.stopView(false);
    }

	public void refresh()
	{
		synch(true);
		canvas.startView(true);
		canvas.stopView(false);
	}

}
