package com.fathzer.jchess.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.fathzer.games.GameBuilder;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;

public class ChessBoardPanel extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	public static final String SELECTION = "Selection";
	public static final String TARGET = "Target";
    private static final String SPRITES_PATH = "/ChessPieces.png"; 
    private static final int SPRITE_SIZE = 170; 

    private static final BufferedImage chessPiecesImage;
	
	private final transient com.fathzer.jchess.Dimension dimension;
    private final transient SpriteMover sprites;
	private transient GameBuilder<Board<Move>> rules;
	private int selected;
	private boolean reverted;
    private transient Board<Move> board;
    private int[] destinations;
    private int lastFrom;
    private int lastTo;
    private com.fathzer.games.Color invertedColor=null;
    private int squareSize=64;
    private int offsetX = 0;
    private int offsetY = 0;
    
	private transient List<Move> moveList;
	private int[] targets;
	private boolean manualMoves = true;
	private boolean showPossibleMoves = true;
	private boolean touchMove = true;
	
    static {
    	try {
    		chessPiecesImage = ImageIO.read(ChessBoardPanel.class.getResourceAsStream(SPRITES_PATH));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    }
    
    public ChessBoardPanel() {
    	this(new com.fathzer.jchess.Dimension(8,8));
    }
    public ChessBoardPanel(com.fathzer.jchess.Dimension dimension) {
        this.setBackground(Color.BLACK);
        this.reverted = false;
        this.addMouseListener(this);
        this.sprites = new SpriteMover(this, e -> this.selected>=0 && inBoard(e));
        this.addMouseMotionListener(this.sprites);
        this.dimension = dimension;
        this.selected = -1;
        this.destinations = new int[0];
        this.lastFrom = -1;
        this.lastTo = -1;
    }
    
    public void setBoard(Board<Move> board) {
    	this.board = board;
    	setSelected(-1);
    	this.setLastMove(-1, -1);
    	this.setDestinations(new int[0]);
    	this.updatePossibleMoves();
    	this.repaint();
    }
    
    public void setChessRules(GameBuilder<Board<Move>> rules) {
    	this.rules = rules;
    	this.updatePossibleMoves();
    	this.repaint();
    }
    
    public void setShowPossibleMoves(boolean display) {
    	this.showPossibleMoves = display;
    	this.repaint();
    }
    
    public void setTouchMove(boolean touchMove) {
    	this.touchMove = touchMove;
    }
    
    public void setManualMoveEnabled(boolean enabled) {
    	if (this.manualMoves!=enabled) {
        	this.manualMoves = enabled;
        	if (!enabled) {
        		this.setSelected(-1);
        		this.setDestinations(new int[0]);
        		this.repaint();
        	}
    	}
    }
    
    private void updatePossibleMoves() {
    	this.targets = new int[0];
    	if (board!=null && rules!=null) {
    		this.moveList = board.getMoves();
    	}
    }
    
    public void setUpsideDownColor(com.fathzer.games.Color color) {
    	this.invertedColor = color;
    }
    
    public void setReverted(boolean reverted) {
    	this.reverted = reverted;
    	this.repaint();
    }
    
    public void setDestinations(int[] dest) {
    	this.destinations = dest;
    }
    
    public void setSelected(int selection) {
    	this.selected = selection;
    	if (this.selected>=0) {
    		final Rectangle spriteBounds = getPieceBounds(board.getPiece(selection));
    		final Image sprite = chessPiecesImage.getSubimage(spriteBounds.x, spriteBounds.y, spriteBounds.width, spriteBounds.height)
    			.getScaledInstance(squareSize, squareSize, Image.SCALE_SMOOTH);
    		this.sprites.setSprite(sprite);
    	} else {
    		this.sprites.setSprite(null);
    	}
    }
    
    public void setLastMove(int lastFrom, int lastTo) {
    	this.lastFrom = lastFrom;
    	this.lastTo = lastTo;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board!=null) {
	        squareSize = Math.min(this.getHeight()/board.getDimension().getHeight(),this.getWidth()/board.getDimension().getWidth());
	        offsetX = (this.getWidth()-squareSize*board.getDimension().getWidth())/2;
	        offsetY = (this.getHeight()-squareSize*board.getDimension().getHeight())/2;
	        ((Graphics2D)g).setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	        drawBoard(g);
	        drawSelection(g);
	        if (this.showPossibleMoves) {
	        	drawPossibleDestinations(g);
	        }
	        drawPieces(g);
	        drawLastMove(g);
	        drawGhost(g);
        }
    }
	protected void drawPieces(Graphics g) {
		for (int r=0;r<dimension.getHeight();r++) {
        	final int row = reverted ? dimension.getHeight() - r - 1 : r;
			for (int c=0;c<dimension.getWidth();c++) {
	        	final int index = board.getCoordinatesSystem().getIndex(r, c);
				Piece p = board.getPiece(index);
	        	if (p!=null && (index!=selected || this.sprites.getSprite()==null)) {
	            	final int col = reverted ? dimension.getWidth() - c - 1 : c;
	            	drawPiece(g, p, offsetX+col*squareSize, offsetY+row*squareSize);
	        	}
			}
        }
	}
	protected void drawPiece(Graphics g, Piece p, int x, int y) {
		final Rectangle bounds = getPieceBounds(p);
        if (bounds!=null) {
            g.drawImage(chessPiecesImage, x, y, x+squareSize, y+squareSize, bounds.x, bounds.y, bounds.x+bounds.width, bounds.y+bounds.height, this);
        }
	}
	private Rectangle getPieceBounds(Piece p) {
    	int j=-1;
    	int k=-1;
        switch (p) {
            case WHITE_PAWN: j=5; k=0;
                break;
            case BLACK_PAWN: j=5; k=1;
                break;
            case WHITE_ROOK: j=4; k=0;
                break;
            case BLACK_ROOK: j=4; k=1;
                break;
            case WHITE_KNIGHT: j=3; k=0;
                break;
            case BLACK_KNIGHT: j=3; k=1;
                break;
            case WHITE_BISHOP: j=2; k=0;
                break;
            case BLACK_BISHOP: j=2; k=1;
                break;
            case WHITE_QUEEN: j=1; k=0;
                break;
            case BLACK_QUEEN: j=1; k=1;
                break;
            case WHITE_KING: j=0; k=0;
                break;
            case BLACK_KING: j=0; k=1;
                break;
            case BORDER:break;
        }
        if (p.getColor().equals(this.invertedColor)) {
        	k = k+2;
        }
        return (j<0 || k<0) ? null : new Rectangle(j*SPRITE_SIZE, k*SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);
	}
	
	protected void drawGhost(Graphics g) {
		this.sprites.draw(g);
	}
	protected void drawBoard(Graphics g) {
		final Color white = new Color(255,200,100);
        final Color black = new Color(150,50,30);
        for (int row=0;row<dimension.getHeight();row++) {
            for (int col=0;col<dimension.getWidth();col++) {
        		g.setColor((row+col)%2==0 ? white : black);
                g.fillRect(offsetX+col*squareSize, offsetY+row*squareSize, squareSize, squareSize);
            }
        }
	}
	
	protected void drawSelection(Graphics g) {
		if (selected>=0) {
			int cell = revertIfNeeded(selected);
    		g.setColor(new Color(255,255,255,128));
    		((Graphics2D)g).setStroke(new BasicStroke(5));
    		int col = board.getCoordinatesSystem().getColumn(cell);
    		int row = board.getCoordinatesSystem().getRow(cell);
    		g.drawOval(offsetX+col*squareSize+3, offsetY+row*squareSize+3, squareSize-6, squareSize-6);
		}
	}
	
	protected void drawPossibleDestinations(Graphics g) {
		if (selected<0) {
			return;
		}
		g.setColor(new Color(255,255,255,128));
		Arrays.stream(destinations).map(this::revertIfNeeded).forEach( cell -> {
    		int col = board.getCoordinatesSystem().getColumn(cell);
    		int row = board.getCoordinatesSystem().getRow(cell);
    		g.fillOval(offsetX+col*squareSize+squareSize/4, offsetY+row*squareSize+squareSize/4, squareSize/2, squareSize/2);
    	});
	}
	
	protected void drawLastMove(Graphics g) {
		if (this.lastFrom>=0) {
    		g.setColor(new Color(0,255,0,96));
    		((Graphics2D)g).setStroke(new BasicStroke(8));
    		final int from = revertIfNeeded(lastFrom);
    		final int to = revertIfNeeded(lastTo);
			final int rowFrom = board.getCoordinatesSystem().getRow(from);
			final int colFrom = board.getCoordinatesSystem().getColumn(from); 
			final int rowTo = board.getCoordinatesSystem().getRow(to); 
			final int colTo = board.getCoordinatesSystem().getColumn(to);
			g.drawLine(offsetX+colFrom*squareSize+squareSize/2 , offsetY+rowFrom*squareSize+squareSize/2, offsetX+colTo*squareSize+squareSize/2, offsetY+rowTo*squareSize+squareSize/2);
		}
	}
	
	private boolean inBoard(MouseEvent e) {
		return e.getX()>=offsetX && e.getX()<dimension.getWidth()*squareSize+offsetX &&
			e.getY()>=offsetY && e.getY()<dimension.getHeight()*squareSize+offsetY;
	}
	private int toPosition(MouseEvent e) {
		int col = (e.getX()-offsetX)/squareSize;
		int row = (e.getY()-offsetY)/squareSize;
		if (reverted) {
			col = board.getDimension().getWidth() - col - 1;
			row = board.getDimension().getHeight() - row - 1;
		}
		return board.getCoordinatesSystem().getIndex(row, col);
	}
	private int revertIfNeeded(int pos) {
		if (reverted) {
			final CoordinatesSystem cs = board.getCoordinatesSystem();
			final int row = dimension.getHeight() - cs.getRow(pos) - 1;
			final int col = dimension.getWidth() - cs.getColumn(pos) - 1;
			pos = cs.getIndex(row, col);
		}
		return pos;
	}
    @Override
    public void mousePressed(MouseEvent e) {
        if (manualMoves && inBoard(e)) {
        	int oldSelected = selected;
        	final int position = toPosition(e);
    		// If a selection is already made, try move and exit if move is valid
        	if (selected>=0 && doMove(e)) {
        		return;
        	}
        	if (selected>0 && touchMove) {
        		// If touch move rule applies, refuse to change the selection
        		return;
        	}
            final Piece piece = board.getPiece(position);
            if (Piece.BORDER.equals(piece)) {
            	System.out.println("What's the fuck");
            }
			if (piece!=null && piece.getColor().equals(board.getActiveColor())) {
            	// The player clicked one of his pieces
                this.targets = getMoves().filter(m->m.getFrom()==position).mapToInt(Move::getTo).distinct().toArray();
                if (this.targets.length>0) {
	                setSelected(position);
	                sprites.setGhost(e.getPoint());
	                setDestinations(targets);
                } else {
                	setSelected(-1);
                }
            } else {
            	setSelected(-1);
            }
            if (oldSelected!=selected) {
            	firePropertyChange(SELECTION, oldSelected, selected);
            }
            this.repaint();
        }
    }
    @Override
	public void mouseReleased(MouseEvent e) {
    	if (this.sprites.getSprite()!=null) {
    		this.sprites.setSprite(null);
    		this.repaint();
    	}
		if (manualMoves && inBoard(e) && e.getButton() == MouseEvent.BUTTON1 && toPosition(e)!=selected) {
			doMove(e);
		}
	}

	private boolean doMove(MouseEvent e) {
		return doMove(toPosition(e));
	}
	
	public boolean doMove(Move move) {
		final boolean legal = getMoves().anyMatch(m -> m.getFrom()==move.getFrom() && m.getTo()==move.getTo());
		if (legal) {
			board.makeMove(move);
			moveList = board.getMoves();
	        setDestinations(new int[0]);
	        setLastMove(move.getFrom(), move.getTo());
	        setSelected(-1);
			targets = new int[0];
			this.repaint();
	        firePropertyChange(TARGET, null, move);
		}
		return legal;
	}
	
	private boolean doMove(int destination) {
		if (Arrays.stream(targets).anyMatch(i -> i == destination)) {
			// Legal move
			final List<Move> moves = getMoves().filter(m -> m.getFrom()==selected && m.getTo()==destination).collect(Collectors.toList());
			final Move move;
			if (moves.isEmpty()) {
				move = null;
			} else if (moves.size()>1) {
				// Promotion
				final PieceKind[] kinds = moves.stream().map(m->m.getPromotion().getKind()).toArray(PieceKind[]::new);
				final PieceKind choice = getPromotion(kinds);
				move = moves.stream().filter(m -> m.getPromotion().getKind().equals(choice)).findAny().get();
			} else {
				move = moves.get(0);
			}
			if (move!=null) {
				return doMove(move);
			}
		}
		return false;
	}
	
	protected PieceKind getPromotion(PieceKind[] kinds) {
		return (PieceKind) JOptionPane.showInputDialog(this, "Select the promotion", "Promotion", JOptionPane.QUESTION_MESSAGE, null, kinds, PieceKind.QUEEN);
	}
	
	private Stream<Move> getMoves() {
		return moveList.stream();
	}
	
	public Status getStatus() {
		return board.getStatus();
	}
	
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(dimension.getWidth()*squareSize,dimension.getHeight()*squareSize);
	}
}