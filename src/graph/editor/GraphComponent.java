package graph.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class GraphComponent extends JComponent implements MouseInputListener, KeyListener {

	private static final long serialVersionUID = 1L;
	private List<RectangularShape> shapes = new ArrayList<RectangularShape>();
	private List<Color> colors = new ArrayList<>();
	private RectangularShape currentShape = null;
	private int dx = 0;
	private int dy = 0;
	private static final Color[] colorList = new Color[] { Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW };

	private RectangularShape shapeSample = new Ellipse2D.Double(0, 0, 10, 10);

	List<Edge> edges = new ArrayList<Edge>();
	Edge currentEdge = null;
	private RectangularShape currentJointPoint = null;

	public GraphComponent() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		Graphics2D g2 = (Graphics2D) g;
		for (int i = 0; i < shapes.size(); i++) {
			RectangularShape s = shapes.get(i);
			g.setColor(colors.get(i));
			g2.fill(s);
		}
		for (Edge e : edges) {
			g.setColor(getForeground());
			e.draw(g2);
		}
	}

	private RectangularShape getShape(int x, int y) {
		for (int i = shapes.size() - 1; i >= 0; i--) {
			RectangularShape s = shapes.get(i);
			if (s.contains(x, y)) {
				dx = (int) (x - s.getCenterX());
				dy = (int) (y - s.getCenterY());
				System.out.println(dx);
				System.out.println(dy);
				return s;
			}
		}
		return null;
	}

	private RectangularShape getJointPoint(int x, int y) {
		for (Edge e : edges) {
			RectangularShape jp = e.getJointPoint(x, y);
			if (jp != null)
				return jp;
		}
		return null;
	}

	private static final double EDGE_EPSILON = 2.0;

	private Edge getEdge(int x, int y) {
		for (Edge e : edges)
			if (e.contains(x, y, EDGE_EPSILON))
				return e;
		return null;
	}

	public void setShapeType(RectangularShape sample) {
		shapeSample = sample;
	}

	private void removeShape(RectangularShape v) {
		List<Edge> toRemove = new ArrayList<Edge>();
		for (Edge e : edges) {
			if (e.rs1 == v || e.rs2 == v)
				toRemove.add(e);
		}
		for (Edge e : toRemove)
			removeEdge(e);
		shapes.remove(v);
	}

	private void removeEdge(Edge e) {
		edges.remove(e);
	}

	private RectangularShape createShape(int x, int y) {
		RectangularShape rs = newShape(x, y);
		shapes.add(rs);
		Random r = new Random();
		colors.add(colorList[r.nextInt(9)]);
		return rs;
	}

	private void moveShape(RectangularShape rs, int x, int y) {
		rs.setFrameFromCenter(x, y, x + rs.getHeight() / 2, y + rs.getWidth() / 2);
	}

	private RectangularShape newShape(int x, int y) {
		RectangularShape rs = (RectangularShape) shapeSample.clone();
		moveShape(rs, x, y);
		Random r = new Random();
		colors.add(colorList[r.nextInt(9)]);
		return rs;
	}

	private Edge startEdge(RectangularShape rs) {
		RectangularShape rs2 = newShape(0, 0);
		rs2.setFrameFromCenter((int) rs.getCenterX(), (int) rs.getCenterY(), (int) rs.getCenterX(),
				(int) rs.getCenterY());
		Edge e = new Edge(rs, rs2);
		edges.add(e);
		return e;
	}

	private void endEdge(Edge e, int x, int y) {
		RectangularShape rs = getShape(x, y);
		if (rs == null) {
			e.rs2.setFrameFromCenter(x, y, x + shapeSample.getHeight() / 2, y + shapeSample.getWidth() / 2);
			shapes.add(e.rs2);
		} else
			e.rs2 = rs;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseClicked");
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			int x = arg0.getX();
			int y = arg0.getY();
			RectangularShape rs = getShape(x, y);
			if (rs != null) {
				removeShape(rs);
				repaint();
				return;
			}
			for (Edge edge : edges) {
				RectangularShape jp = edge.getJointPoint(x, y);
				if (jp != null) {
					edge.removeJointPoint(jp);
					repaint();
					return;
				}
			}
			Edge edge = getEdge(x, y);
			if (edge != null) {
				removeEdge(edge);
				repaint();
				return;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseEntered");
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseExited");
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		requestFocusInWindow();
		if ((arg0.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
			return;
		int x = arg0.getX();
		int y = arg0.getY();
		RectangularShape rs = getShape(x, y);
		if (rs == null) {
			currentJointPoint = getJointPoint(x, y);
		}
		if (rs == null && currentJointPoint == null)
			rs = createShape(x, y);
		if (rs != null && arg0.isAltDown())
			currentEdge = startEdge(rs);
		else
			currentShape = rs;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseReleased");
		if (currentEdge != null) {
			endEdge(currentEdge, arg0.getX(), arg0.getY());
			currentEdge = null;
			repaint();
		}
		currentShape = null;
		currentJointPoint = null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseDragged");
		if (currentShape != null) {
			moveShape(currentShape, arg0.getX() - dx, arg0.getY() - dy);
			repaint();
		} else if (currentEdge != null) {
			moveShape(currentEdge.rs2, arg0.getX(), arg0.getY());
			repaint();
		} else if (currentJointPoint != null) {
			moveShape(currentJointPoint, arg0.getX(), arg0.getY());
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseMoved");
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getKeyCode() == ' ' && currentEdge != null) {
			currentEdge.addJointPoint();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
