package com.endercrypt.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.endercrypt.gui.keyboard.Keyboard;

public class AwtWindow
{
	private JFrame jFrame;
	private JPanel jPanel;
	private Keyboard keyboard;

	public AwtWindow(String title, Dimension windowSize, Consumer<Graphics2D> drawListener)
	{
		this(title, windowSize, drawListener, false);
	}

	public AwtWindow(String title, Dimension windowSize, Consumer<Graphics2D> listener, boolean undecorated)
	{
		jPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			{
				setPreferredSize(windowSize);
			}

			@Override
			protected void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				listener.accept(g2d);
			}
		};

		jFrame = new JFrame(title);
		jFrame.setUndecorated(undecorated);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.add(jPanel);
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);

		keyboard = new Keyboard(jFrame);
	}

	public Dimension screenSize()
	{
		return jFrame.getSize();
	}

	public JPanel getJPanel()
	{
		return jPanel;
	}

	public JFrame getJFrame()
	{
		return jFrame;
	}

	public void show()
	{
		jFrame.setVisible(true);
	}

	public void dispose()
	{
		jFrame.dispose();
	}

	public Keyboard getKeyboard()
	{
		return keyboard;
	}

	public void repaint()
	{
		jFrame.repaint();
	}
}
