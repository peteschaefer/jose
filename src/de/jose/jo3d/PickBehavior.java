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

import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * this behaviour lets the user pick a shape and drag it over the board
 * if the user picks a point on the board, it will instead trigger an OrbitBehavior
 * 
 */
public class PickBehavior
		extends CallbackBehavior
{
	/**	the picking tool	 */
	protected PickCanvas pickCanvas;
	/**	the currently dragged Piece Group	 */
	protected PieceGroup draggedGroup;
	/**	the shapes we are currently colliding with (maps Nodes to TransparenceAttributes)
	 * 	currently (Java3D 1.3) this is pretty useless because ONLY ONE collision can be detected  :-(
	 * */
	protected HashMap collisionNodes;
	/**	transforms VWorld coordinates to local	 */
	protected Transform3D vwToLocal;
	/**	point where dragging started	 */
	protected Point3d dragStartPoint;
	/**	point where piece started	 */
	protected Point3d pieceStartPoint;
	/**	root group	 */
	protected TransformGroup root;
	/**	orbit handle (picking this node will trigger orbiting	 */
	protected Set orbitHandles;
	/**	zoom handle (picking this node will trigger zooming	 */
	protected Set zoomHandles;
	/**	parent AWT component	 */
	protected Component awtComponent;
	/**	queues Mouse drag events	 */
    protected MouseQueue mouseQueue;
	
	/**	wakeup condition: mouse pressed	 */
	protected WakeupCondition startWakeup;
	/**	drag conditon: frame or mouse released	 */
	protected WakeupCondition dragWakeup;
	/**	aux variables	 */
	protected Point3d pray;
	protected Vector3d vray;

	/**	return value from pickUp	 */
	public static final int PICK_PIECE			= 3;
	public static final int PICK_ZOOM_HANDLE	= 4;
	public static final int PICK_ORBIT_HANDLE	= 5;
    public static final int DRAG_PIECE			= 6;
	public static final int DROP_PIECE			= 7;
	/**	transparent appearance for collided objects */
	private static TransparencyAttributes attTransparent;

	public PickBehavior(Canvas3D c3d, BranchGroup branchGroup, TransformGroup rootGroup,
						MouseQueue q, int mode)
	{
		pickCanvas = new PickCanvas(c3d,branchGroup);
		pickCanvas.setMode(mode);
		pickCanvas.setTolerance(0.0f);
		awtComponent = c3d;
		collisionNodes = new HashMap();

		mouseQueue = q;
		root = rootGroup;
		
		pieceStartPoint = new Point3d();
		pray = new Point3d();
		vray = new Vector3d();
		
		orbitHandles = new HashSet();
		zoomHandles = new HashSet();

		attTransparent = new TransparencyAttributes();
		attTransparent.setTransparencyMode(TransparencyAttributes.FASTEST);
		attTransparent.setTransparency(0.7f);
	}
	
	public void addOrbitHandle(Node nd)		{ 
		orbitHandles.add(nd); 
		nd.setPickable(true);
	}

	public void addZoomHandle(Node nd)		{
		zoomHandles.add(nd); 
		nd.setPickable(true);
	}
	
	/**	implements Behavior	 */
	public void initialize()
	{

		startWakeup = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);

		wakeupOn(startWakeup);
	}

	private WakeupCondition dragCondition(PieceGroup pg)
	{
		WakeupCriterion[] criteria = {
			new WakeupOnElapsedFrames (0,false),
			new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED),
			new WakeupOnCollisionEntry(pg.getShape(), WakeupOnCollisionEntry.USE_BOUNDS),
			new WakeupOnCollisionExit(pg.getShape(), WakeupOnCollisionEntry.USE_BOUNDS),
//			new WakeupOnCollisionMovement(pg.getShape(), WakeupOnCollisionEntry.USE_BOUNDS),
		};

		return new WakeupOr(criteria);
	}

	/**	implements Behavior	 */
	public void processStimulus(Enumeration criteria)
	{
		WakeupCondition wakeupNext = startWakeup;
		
		while (criteria.hasMoreElements())
		{
			WakeupCriterion wake = (WakeupCriterion)criteria.nextElement();
			if (wake instanceof WakeupOnElapsedFrames)
			{
				//	keep on dragging
				MouseEvent mevt = mouseQueue.lastEvent(MouseEvent.MOUSE_DRAGGED);
				if (mevt!=null && isDragging())
					drag(mevt);
				wakeupNext = dragWakeup;
			}
			else if (wake instanceof WakeupOnAWTEvent)
			{
				AWTEvent[] evts = ((WakeupOnAWTEvent)wake).getAWTEvent();
				
				for (int i=0; i<evts.length; i++)
					switch (evts[i].getID()) {
					case MouseEvent.MOUSE_PRESSED:
						MouseEvent mevt = (MouseEvent)evts[i];
						if ((mevt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
							switch (pickUp(mevt))
							{
							case PICK_PIECE:
								mouseQueue.clear(MouseEvent.MOUSE_DRAGGED);
								wakeupNext = dragWakeup = dragCondition(draggedGroup);		//	start dragging
								break;
							case PICK_ORBIT_HANDLE:
								postId(OrbitBehavior.ORBIT_EVENT);	//	trigger orbiting
								break;
							case PICK_ZOOM_HANDLE:
								postId(OrbitBehavior.ZOOM_EVENT);	//	trigger zooming
								break;
							}
						break;
						
					case MouseEvent.MOUSE_RELEASED:
						//	end dragging
						collisionEnd();
						drop((MouseEvent)evts[i]);
						//	release all collisions
						wakeupNext = startWakeup;
						break;
					}
			}
			else if (wake instanceof WakeupOnCollisionEntry)
			{
				SceneGraphPath scp = ((WakeupOnCollisionEntry)wake).getTriggeringPath();
				Shape3D nd = (Shape3D)scp.getObject();
//				System.out.println("enter "+nd);
				collisionStart(nd);
				wakeupNext = dragWakeup;
			}
			else if (wake instanceof WakeupOnCollisionMovement)
			{
				SceneGraphPath scp = ((WakeupOnCollisionMovement)wake).getTriggeringPath();
				Shape3D nd = (Shape3D)scp.getObject();
//				System.out.println("move "+nd);
				collisionStart(nd);
				wakeupNext = dragWakeup;
			}
			else if (wake instanceof WakeupOnCollisionExit)
			{
				SceneGraphPath scp = ((WakeupOnCollisionExit)wake).getTriggeringPath();
				Shape3D nd = (Shape3D)scp.getObject();
//				System.out.println("exit "+nd);
				collisionEnd(nd);
				wakeupNext = dragWakeup;
			}
		}
		
		if (wakeupNext==startWakeup)
				notifyListeners(ICallbackListener.DEACTIVATE, null);
		wakeupOn(wakeupNext);
	}
		
	public boolean isDragging()
	{
		return draggedGroup != null;
	}

	private int countValidPicks()
	{
		int result = 0;
		PickResult[] all = pickCanvas.pickAll();
		for (int i=0; i<all.length; i++)
		{
			Shape3D shape = (Shape3D)all[i].getNode(PickResult.SHAPE3D);
			PieceGroup piece = PieceGroup.getPieceGroup(shape);
			if (piece != null) result++;
		}
		return result;
	}

	/**
	 * start dragging a piece
	 */
	protected int pickUp(MouseEvent evt)
	{
		notifyListeners(ICallbackListener.ACTIVATE, evt);
		//	pick a piece
		pickCanvas.setShapeLocation(evt);

		int hits = 1;
		PickResult closest = pickCanvas.pickClosest();
		if (closest==null)
			return 0;

		Shape3D shape = (Shape3D)closest.getNode(PickResult.SHAPE3D);
		PieceGroup piece = PieceGroup.getPieceGroup(shape);
		if (piece==null) {
			if (zoomHandles.contains(shape))
				return PICK_ZOOM_HANDLE;
			else if (orbitHandles.contains(shape))
				return PICK_ORBIT_HANDLE;
			else
				return 0;
		}

		if (pickCanvas.getMode() == PickTool.BOUNDS)
			hits = countValidPicks();

		vwToLocal = piece.getVWorldToLocal();
		Point3d s = null;	//	z coordinate of the picked point
		if (pickCanvas.getMode() == PickTool.BOUNDS && hits > 1)
		{
			//	ambigous pick by bounds - disambiguate
			System.out.println("ambiguous pick");
			pickCanvas.setMode(PickTool.GEOMETRY);
			pickCanvas.setShapeLocation(evt);
			closest = pickCanvas.pickClosest();
			shape = (Shape3D)closest.getNode(PickResult.SHAPE3D);
			piece = PieceGroup.getPieceGroup(shape);

			closest.setFirstIntersectOnly(true);
			PickIntersection isect = closest.getIntersection(0);

			s = isect.getPointCoordinates();
			pickCanvas.setMode(PickTool.BOUNDS);
		}
		else if (pickCanvas.getMode() == PickTool.GEOMETRY)
		{
			//	ordinary pick by geometry
			closest.setFirstIntersectOnly(true);
			PickIntersection isect = closest.getIntersection(0);

			s = isect.getPointCoordinates();
		}
		else
		{
			//	bounds picking is much faster than geometry picking but it might be ambiguous
			s = getPickPoint(evt, piece.getCurrentBounds());
		}

		if (s==null) return 0;	//	missed

		draggedGroup = piece;
		piece.location().get(pieceStartPoint);

		dragStartPoint = getPickPoint(evt, s.z);
		awtComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		notifyListeners(PICK_PIECE, piece);
		return PICK_PIECE;
	}
	
	protected Point3d getPickPoint(MouseEvent evt, double z)
	{
		//	project the pick ray on the z plane
		pickCanvas.setShapeLocation(evt);
		PickRay ray = (PickRay)pickCanvas.getPickShape();
		
		ray.get(pray,vray);
		
		//	translate into local coordinates
		vwToLocal.transform(pray);
		vwToLocal.transform(vray);
		
		//	intersect it with the z plane
		Point3d q = Util3D.intersectRayZ(pray,vray,z);
		return q;
	}

	protected Point3d getPickPoint(MouseEvent evt, Bounds box)
	{
		//	project the pick ray on to a box
		pickCanvas.setShapeLocation(evt);
		PickRay ray = (PickRay)pickCanvas.getPickShape();

		ray.get(pray,vray);

		//	translate into local coordinates
		vwToLocal.transform(pray);
		vwToLocal.transform(vray);

		//	intersect it with the box
		Point3d q1 = new Point3d();
		Point3d q2 = new Point3d();

		if (Util3D.intersectRay(pray,vray,box, q1,q2) >= 1)
			return q1;
		else
			return null;	//	shouldn't happen
	}

	protected void drag(MouseEvent evt)
	{
		//	get intersection with z plane
		Point3d p = getPickPoint(evt, dragStartPoint.z);
		if (p==null) return;
			
		p.sub(dragStartPoint);
		p.add(pieceStartPoint);
		p.z = 0.0;
			

		if (!p.equals(draggedGroup.location()))
		{
//			long age = System.currentTimeMillis()-evt.getWhen();
//			System.out.println(age);
            notifyListeners(DRAG_PIECE,draggedGroup);
			draggedGroup.moveTo(p);
		}
	}
	
	protected void drop(MouseEvent evt)
	{
		if (isDragging())
			drag(evt);
		
		awtComponent.setCursor(Cursor.getDefaultCursor());
		notifyListeners(DROP_PIECE, draggedGroup, true);
		//  delayed invocation avoids deadlock with JComponent.getTreeLock() !!
		draggedGroup = null;
	}

	protected void collisionStart(Shape3D nd)
	{
		if (collisionNodes.containsKey(nd)) return;
		Appearance app = nd.getAppearance();
		collisionNodes.put(nd, app.getTransparencyAttributes());
		//	make it transparent
		app.setTransparencyAttributes(attTransparent);
	}

	protected void collisionEnd(Shape3D nd)
	{
		if (! collisionNodes.containsKey(nd)) return;
		Appearance app = nd.getAppearance();
		TransparencyAttributes oldta = (TransparencyAttributes)collisionNodes.get(nd);
		app.setTransparencyAttributes(oldta);
		collisionNodes.remove(nd);
	}

	protected void collisionEnd()
	{
		Object[] nodes = collisionNodes.keySet().toArray();
		//	make a copy to avoid comodifications
		for (int i=0; i<nodes.length; i++)
			collisionEnd((Shape3D)nodes[i]);
	}
}
