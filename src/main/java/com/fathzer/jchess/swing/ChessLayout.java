package com.fathzer.jchess.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.function.Function;

class ChessLayout implements LayoutManager {
	static final String NORTH = "NORTH";
	static final String CENTER = "CENTER";
	static final String SOUTH = "SOUTH";
	
	private Component north;
	private Component center;
	private Component south;
	
	public ChessLayout() {
		super();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		if (NORTH.equals(name)) {
			north = comp;
		} else if (SOUTH.equals(name)) {
			south = comp;
		} else {
			center = comp;
		}
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if (comp.equals(north)) {
			north = null;
		}
		if (comp.equals(center)) {
			center = null;
		}
		if (comp.equals(south)) {
			south = null;
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return getSize(Component::getPreferredSize);
	}

	private Dimension getSize(Function<Component, Dimension> sizer) {
		final int width = sizer.apply(center).width;
		int height = width;
		if (north!=null) {
			height += sizer.apply(north).height;
		}
		if (south!=null) {
			height += sizer.apply(south).height;
		}
		return new Dimension(width, height);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return getSize(Component::getMinimumSize);
	}

	@Override
	public void layoutContainer(Container parent) {
		final Insets insets = parent.getInsets();
		final int width = parent.getWidth() - insets.right - insets.left;
		final int height = parent.getHeight() - insets.bottom - insets.top;
		final int northHeight = north!=null ? north.getPreferredSize().height : 0;
		final int southHeight = south!=null ? south.getPreferredSize().height : 0;
		final int squareSize = Math.min(height - northHeight - southHeight, width);
		final int centerTop = insets.top + height/2 - squareSize/2;
		final int centerLeft = insets.left + width/2 - squareSize/2;
		center.setBounds(centerLeft, centerTop, squareSize, squareSize);
		if (north!=null) {
			north.setBounds(centerLeft, centerTop-northHeight, squareSize, northHeight);
		}
		if (south!=null) {
			south.setBounds(centerLeft, centerTop+squareSize, squareSize, southHeight);
		}
	}
}
