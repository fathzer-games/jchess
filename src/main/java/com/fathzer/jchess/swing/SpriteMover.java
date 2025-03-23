package com.fathzer.jchess.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.function.Predicate;

import javax.swing.JComponent;

import lombok.Getter;

public class SpriteMover implements MouseMotionListener {
	private final JComponent component;
	private final Predicate<MouseEvent> eventValidator;
	@Getter
	private Image sprite;
	private int width;
	private int height;
	private Rectangle last;
	private Point ghost; 
	
	public SpriteMover (JComponent component, Predicate<MouseEvent> eventValidator) {
		this.component = component;
		this.eventValidator = eventValidator;
	}
	
	public void setSprite(Image sprite) {
		this.sprite = sprite;
		if (sprite!=null) {
			this.width = sprite.getWidth(null);
			this.height = sprite.getHeight(null);
		}
	}
	
	public void setGhost(Point point) {
    	this.ghost = point;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
    	if (sprite!=null && eventValidator.test(e)) {
	    	setGhost(e.getPoint());
	    	if (this.last!=null) {
	    		component.repaint(this.last);
				component.repaint(ghost.x-width/2, ghost.y-height/2, width, height);
	    	} else {
	    		component.repaint();
	    	}
    	}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Nothing to do when mouse is moved
	}
	
	public void draw(Graphics g) {
		if (ghost==null || sprite==null) {
			last = null;
		} else {
			this.last = new Rectangle(ghost.x - width/2, ghost.y - height/2, width, height);
            g.drawImage(sprite, this.last.x, this.last.y, component);
		}
	}
}
