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

package de.jose.jo3d;

import de.jose.chess.Constants;
import de.jose.chess.EngUtil;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

/**
 * a piece group models a 3D piece it contains
 * - a transform group to locate it on the board
 * - a shape3d that defines the geometry and apperance
 * - a behavior for animation and mouse picking
 *
 * note that we have to extend BranchGroup
 * because only BranchGroups can be added or removed
 * from live scene graphs
 *
 * @author Peter Schäfer
 */

public class PieceGroup
		extends BranchGroup
		implements Constants, ICallbackListener
{
	//-------------------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------------------

	/**	size of a square (in the virtual universe)	 */
	public static final float squareSize = 274.0f;
	/**	center of a square 	 */
	public static final float squareCenter = squareSize/2;
	/**	max. levels of detail	*/
	public static final int MAX_LOD	= 6;

	/**	reflection is the first element in the decalSwitch group	*/
	private static final int REFLECTION	= 0;
	/**	shados is the second element in the decalSwitch group	*/
	private static final int SHADOW	= 1;

	//-------------------------------------------------------------------------------------------
	//	Static Members
	//-------------------------------------------------------------------------------------------

	/**	factory cache	 */
	protected static Shape3D[][] shapeFactory = new Shape3D[KING+1][MAX_LOD];
	protected static float[][] threshholdFactory = new float[KING+1][MAX_LOD];

	/**	used for randomly offsetting textures */
	protected static Random random = new Random();

	//-------------------------------------------------------------------------------------------
	//	Members
	//-------------------------------------------------------------------------------------------

	/**	the piece	 */
	protected int piece;

	/**	the current location */
	protected Vector3d location;
	/**	dynamic transform group (editable) */
	protected TransformGroup dynamicGroup;
	/**	lod switch node	*/
	protected CallbackSwitch switchGroup;
	/**	distance LOD behavior (if necessary) */
	protected DistanceLOD lod;
	/**	lod distances (relative to a screen of size 1000x1000)	 */
	protected float[] lodDistance;
	/**	static transfrom group (not editable) */
	protected TransformGroup staticGroup;
	/**	current geometric shape	 */
	protected Shape3D shape;
	/**	switch node that contains shadow and reflection */
	protected ClipSwitch decalSwitch;
	/**	static transform	 */
	protected Transform3D staticTransform;
	/**	aux variable	 */
	protected Transform3D aux;


	//-------------------------------------------------------------------------------------------
	//	Static Methods
	//-------------------------------------------------------------------------------------------

	public static void addShape(int pc, int lod, Shape3D shape, float lod_threshhold)
	{
		pc = EngUtil.uncolored(pc);
		shapeFactory[pc][lod] = shape;
		threshholdFactory[pc][lod] = lod_threshhold;

		//	when cloning the node, geometry is referenced, not copied
		Enumeration geoms = shape.getAllGeometries();
		while (geoms.hasMoreElements())
		{
			Geometry geo = (Geometry)geoms.nextElement();
			geo.setDuplicateOnCloneTree(false);
		}

		Util3D.setPickCapabilities(shape);
	}

	/**	maps Shape3D to PieceGroup
	 */
	protected static Hashtable shapeMap = new Hashtable();

	//-------------------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------------------

	/**
	 *
	 * BranchGroup (logical unit)
	 *  +--> TransformGroup (dynamic, use to move the piece around)
	 *     +--> LOD behavior
	 *     +--> SwitchNode (LOD)
	 *	      +--> TransformGroup (static, transform to 0,0)
	 *	         +--> Shape3D (geometry)
	 *	            +--> Appearance (color or texture)
	 *     +--> SwitchNode (decals)
	 *        +--> Decal (shadow)
	 *
	 * @param pc a piece conastant + color (as defined by de.jose.Constants)
	 * @param ignored ?
	 * @param shadowClip clipping region for shadows (the board)
	 * @param behaviorBounds bounds for LOD behavior (should include the whole scene)
	 * @param screenSize size of actual window (in pixels); used to adjust LODs
	 */
	public PieceGroup(int pc, Component observer, GlobalClip clip, Bounds behaviorBounds,
					  Point2d screenSize, OrbitBehavior orbit)
		throws Exception
	{
		super();

		dynamicGroup = new TransformGroup();
		addChild(dynamicGroup);

		piece = pc;

		location = new Vector3d();
		aux = new Transform3D();

		//	create Switch
		switchGroup = new CallbackSwitch();
		//	the switch is attached to a Distance LOD; we would like to be informed about switches

		int upc = EngUtil.uncolored(pc);
		//	for all available LODs: creat shape and static transform
		int maxLOD = 0;
		for (int level=0; level <= MAX_LOD && shapeFactory[upc][level] != null; level++)
		{
			//	create static transform
			maxLOD = level;
			staticGroup = new TransformGroup();
			staticTransform = getStaticTransform(pc,level,0.0);
			staticGroup.setTransform(staticTransform);
			staticGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			staticGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			staticGroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
			staticGroup.setCapability(ENABLE_PICK_REPORTING);
//			staticGroup.setCapability(ALLOW_LOCAL_TO_VWORLD_READ);

			//	get shape
			shape = (Shape3D)shapeFactory[upc][level].cloneTree();
			shape.setAppearance(createAppearance(maxLOD));
			shape.setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
			shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
			shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			shape.setCapability(Shape3D.ALLOW_COLLIDABLE_WRITE);
			shape.setCapability(Shape3D.ALLOW_BOUNDS_READ);
			staticGroup.addChild(shape);
			shapeMap.put(shape,this);

			shape.setCollisionBounds(shape.getBounds());

			switchGroup.addChild(staticGroup);
		}

		dynamicGroup.addChild(switchGroup);
		switchGroup.addListener(this);

		if (maxLOD > 0) {
			//	setup LOD behavior
			lodDistance = new float[maxLOD];

			for (int level=0; level < maxLOD; level++)
				lodDistance[level] = threshholdFactory[upc][level+1];

			lod = new DistanceLOD(lodDistance);
			lod.setSchedulingBounds(behaviorBounds);
			lod.setPosition(new Point3f(0,0,0));
			lod.addSwitch(switchGroup);
			dynamicGroup.addChild(lod);
			adaptLOD(screenSize);
		}

		switchGroup.setWhichChild(0);	//	will eventually trigger callback

		Point3d p1 = new Point3d();
		Point3d p2 = new Point3d();
		BoundingBox box = (BoundingBox)shape.getBounds();
		box.getLower(p1);
		box.getUpper(p2);

		double footSize = Math.min(Math.abs(p1.x-p2.x), Math.abs(p1.y-p2.y));
		double headHeight = Math.abs(p1.z-p2.z);

		decalSwitch = new ClipSwitch(clip);
		decalSwitch.addChild(createReflection(maxLOD));
		decalSwitch.addChild(createShadow(footSize,headHeight,observer));
		dynamicGroup.addChild(decalSwitch);

		//	create editable transform
		dynamicGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		dynamicGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		setCapability(ENABLE_PICK_REPORTING);
		setCapability(ALLOW_LOCAL_TO_VWORLD_READ);
		setCapability(ALLOW_DETACH);
	}

	private Decal createShadow(double footSize, double headHeight, Component observer)
	{
		//	create shadow & reflection
		Decal shadow = new Decal(-10f, EngUtil.isWhite(piece));
		// white pieces' textures are mirrored
		shadow.setTexture(String.valueOf(EngUtil.lowerPieceCharacter(piece))+"1.png",observer);

		shadow.rotate(Math.PI/4);
		shadow.scale(footSize, headHeight*1.0);

		shadow.setCollidable(false);
		shadow.setPickable(false);

		return shadow;
	}

	private TransformGroup createReflection(int level)
	{
		Transform3D tf = new Transform3D(Util3D.mirrorZ);
		tf.mul(staticTransform);

		TransformGroup tg = new TransformGroup(tf);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		tg.setCapability(TransformGroup.ALLOW_BOUNDS_READ);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

		int upc = EngUtil.uncolored(piece);
		Shape3D shp = shapeFactory[upc][level];
		shp = (Shape3D)shp.cloneTree();

		//	least detailed LOD is good enough for reflection
		Appearance app = (Appearance)getAppearance().cloneNodeComponent(false);
		Util3D.setCullFace(app, PolygonAttributes.CULL_FRONT);
		shp.setAppearance(app);
		shp.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		shp.setCapability(Shape3D.ALLOW_BOUNDS_READ);
		shp.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);

		LocalBoundingBox lbb = new LocalBoundingBox(shp, (BoundingBox)shp.getBounds());
		shp.setBoundsAutoCompute(false);
		shp.setBounds(lbb);

		tg.setBoundsAutoCompute(false);
		tg.setBounds(lbb);
		tg.addChild(shp);

		shp.setCollidable(false);
		shp.setPickable(false);
		return tg;
	}

	/**
	 * call back from switch node
	 */
	public void behaviorCallback(Object source, int actionCode, Object params)
	{
		if (actionCode==CallbackSwitch.SWITCH_ON)
		{
			//	select static transform
			staticGroup = (TransformGroup)switchGroup.currentChild();
			staticGroup.getTransform(staticTransform);

			//	get shape
			shape = (Shape3D)staticGroup.getChild(0);
		}
	}


	//-------------------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------------------

	public final int getPiece()						{ return piece; }

	public final boolean isWhite()					{ return EngUtil.isWhite(piece); }

	public final Shape3D getShape()					{ return shape; }

	public final Appearance getAppearance()			{ return shape.getAppearance(); }

	public final TransformGroup getStaticGroup()	{ return staticGroup; }

	public final Vector3d location()	{ return location; }

	public final Point getLocation2d()	{ return new Point((int)Math.floor(location.x), (int)Math.floor(location.y)); }

	public final double getX()			{ return location.x; }
	public final double getY()			{ return location.y; }
	public final double getZ()			{ return location.z; }

	public final Switch	getSwitch()					{ return switchGroup; }

	public final Decal getShadow()					{ return (Decal)decalSwitch.getChild(SHADOW); }

	public final TransformGroup getReflection()		{ return (TransformGroup)decalSwitch.getChild(REFLECTION); }

	public void enableShadow(boolean on)			{ decalSwitch.setEnable(SHADOW, on);	}

	public void enableReflection(boolean on)		{ decalSwitch.setEnable(REFLECTION, on);	}

	public Appearance getReflectionAppearance()
	{
		TransformGroup tg = getReflection();
		Shape3D shape = (Shape3D)tg.getChild(0);
		return shape.getAppearance();
	}

	/**
	 * @return the current level of detail
	 */
	public int getLOD()						{ return switchGroup.getWhichChild(); }

	/**
	 * @return the max. available LOD
	 */
	public int getMaxLOD()
	{
		if (lodDistance!=null)
			return lodDistance.length-1;
		else
			return 0;
	}

	/**
	 * adapt the lod threshholds to the current screen size
	 * (note that the LOD depends on the distance from the viewer AND the window size)
	 * @param screenSize
	 */
	public void adaptLOD(Point2d screenSize)
	{
		if (lod!=null)
		{
			double scale = Math.min(screenSize.x,screenSize.y)/1000;
			for (int level = 0; level < lodDistance.length; level++)
				lod.setDistance(level, lodDistance[level]*scale);
		}
	}

	/**
	 * get the bounds of the shape, translated to the current location
	 */
	public Bounds getCurrentBounds()
	{
		Bounds bounds = (Bounds)getShape().getBounds().clone();
		bounds.transform(staticTransform);
		dynamicGroup.getTransform(aux);
		bounds.transform(aux);
		return bounds;
	}

    public static final Point2d squareCenter(int square)
	{
        return squareCenter(EngUtil.fileOf(square), EngUtil.rowOf(square));
    }

    public static final Point2d squareCenter(int file, int row)
    {
		return new Point2d ((file-FILE_E)*squareSize + squareCenter,
							(row-ROW_5)*squareSize + squareCenter);
	}

	/**
	 *	@param x
     *  @param y a point on the screen
	 *	@return the square index (or 0, if off the board)
	 * */

	public static final int findSquare(int x, int y)
	{
        if (x < -4*squareSize || x >= 5*squareSize)
            return 0;
        if (y < -4*squareSize || y >= 5*squareSize)
            return 0;

		int file = (int)((x+4*squareSize) / squareSize) + FILE_A;
		int row = (int)((y+4*squareSize) / squareSize) + ROW_1;

		return EngUtil.square(file,row);
	}

	public void moveTo(double x, double y, double z)
	{
		location.set(x, y, z);
		aux.setTranslation(location);
		dynamicGroup.setTransform(aux);
		decalSwitch.updateAll();
	}

	public void moveTo(Point3d p)
	{
		location.set(p);
		aux.setTranslation(location);
		dynamicGroup.setTransform(aux);
		decalSwitch.updateAll();
	}


	public final void moveTo(int file, int row)
	{
		moveTo(squareSize*(file-FILE_E)+squareCenter,
			   squareSize*(row-ROW_5)+squareCenter, location.z);
	}

	public final void moveTo(int square)
	{
		moveTo(EngUtil.fileOf(square), EngUtil.rowOf(square));
	}

	public final void move(double x, double y, double z)
	{
		moveTo(location.x+x, location.y+y, location.z+z);
	}

	public final void move(Tuple3d p)
	{
		moveTo(location.x+p.x,location.y+p.y,location.z+p.z);
	}

	private static final Appearance createAppearance(int lod)
	{
		//	textures are shifted and rotated randomly so that no two pieces look exactly the same
		double dx = Math.IEEEremainder(random.nextDouble(), 1.0);
		double dy = Math.IEEEremainder(random.nextDouble(), 1.0);
		double da = Math.IEEEremainder(random.nextDouble(), Math.PI/4);

		Transform3D tf = Util3D.getScale(0.005,0.005,1.0);
//		Util3D.rotZ(tf, dy-Math.PI/8);
		Util3D.translate(tf, 2000*dx,2000*dy,0.0);

		//	set the T plane so that the texture is projected from the front
		//	(cause that's the direction we are looking from)
		Vector4f splane = new Vector4f(+1.0f, 0.0f, 0.0f, 0.0f);
		Vector4f tplane = new Vector4f( 0.0f, 0.0f,+1.0f, 0.0f);

		Appearance app = Util3D.createAppearance(splane,tplane, tf);
//		app.getMaterial().setShininess(128.0f);	//	very shiny
		app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		//	transparency is set when colliding

		//	for debugging: different LODs are colored
/*		switch (lod)
		{
		case 0: app.getMaterial().setEmissiveColor(new Color3f(1f,0f,0f)); break;
		case 1: app.getMaterial().setEmissiveColor(new Color3f(0f,1f,0f)); break;
		case 2: app.getMaterial().setEmissiveColor(new Color3f(0f,0f,1f)); break;
		case 3: app.getMaterial().setEmissiveColor(new Color3f(1f,1f,0f)); break;
		}
*/
//		app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.9f));
		return app;
	}


	public final Transform3D getStaticTransform(int piece, int lod, double angle)
	{
		Transform3D tf = new Transform3D();
		if (EngUtil.isWhite(piece))
			tf.rotZ(Math.PI+angle);
		else
			tf.rotZ(angle);
		return tf;
	}

	/**
	 * TODO doesn't work. why ?
	 * @param angle
	 */
	public void rotate(double angle)
	{
		staticTransform = getStaticTransform(getPiece(),0,angle);
		staticGroup.setTransform(staticTransform);

		TransformGroup refl = getReflection();
		if (refl!=null) {
			Transform3D tf = new Transform3D(Util3D.mirrorZ);
			tf.mul(staticTransform);
			refl.setTransform(tf);
		}
	}

	/**
	 * get the owning PiecGroup from a shape
	 */
	public static PieceGroup getPieceGroup(Shape3D shape)
	{
		return (PieceGroup)shapeMap.get(shape);
	}

	public Transform3D getVWorldToLocal()
	{
		Transform3D result = new Transform3D();
		getLocalToVworld(result);
		result.invert();
		return result;
	}

	/**	pickability refers always to the Shape
	 */
	public void setPickable(boolean pick)
	{
		shape.setPickable(pick);
	}
}
