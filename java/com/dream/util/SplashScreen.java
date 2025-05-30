package com.dream.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class SplashScreen extends JWindow
{
	private static final long serialVersionUID = 3135071544352172471L;
	
	public SplashScreen(String imagePath, JFrame frame)
	{
		JLabel splashLabel = new JLabel(new ImageIcon(imagePath));
		getContentPane().add(splashLabel, BorderLayout.CENTER);
		pack();
		
		// Posicionar no canto inferior direito, acima do relógio do Windows
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = getSize();
		int x = screenSize.width - windowSize.width - 10;
		int y = screenSize.height - windowSize.height - 50;
		setLocation(x, y);
		
		setAlwaysOnTop(true);
		setVisible(true);
		
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				setVisible(false);
				if (frame != null)
				{
					frame.setVisible(true);
					frame.toFront();
					frame.setState(Frame.ICONIFIED);
					frame.setState(Frame.NORMAL);
				}
				dispose();
			}
		}, 1000 * 2);
	}
}
