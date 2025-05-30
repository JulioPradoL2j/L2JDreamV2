package com.dream.game;

import com.dream.util.InterfaceLimit;
import com.dream.util.SplashScreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class L2InterfaceGS
{
	JTextArea txtrConsole;
	static final String[] shutdownOptions =
	{
		"Shutdown",
		"Cancel"
	};
	
	static final String[] restartOptions =
	{
		"Restart",
		"Cancel"
	};
	static final String[] abortOptions =
	{
		"Abort",
		"Cancel"
	};
	
	static final String[] requestOptions =
	{
		"Ok",
		"Cancel"
	};
	
	public L2InterfaceGS()
	{
		try
		{
			// Base escura para todo o sistema
			UIManager.put("control", new Color(40, 40, 40));
			UIManager.put("info", new Color(60, 63, 65));
			UIManager.put("nimbusBase", new Color(30, 30, 30));
			UIManager.put("nimbusBlueGrey", new Color(70, 73, 75));
			UIManager.put("nimbusLightBackground", new Color(30, 30, 30));
			UIManager.put("text", new Color(220, 220, 220));
			
			UIManager.put("nimbusSelectionBackground", new Color(60, 120, 200));
			UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
			UIManager.put("nimbusFocus", new Color(100, 150, 255));
			
			UIManager.put("nimbusDisabledText", new Color(100, 100, 100));
			
			// Cores de alerta (opcional)
			UIManager.put("nimbusRed", new Color(150, 60, 60));
			UIManager.put("nimbusOrange", new Color(200, 120, 60));
			UIManager.put("nimbusGreen", new Color(100, 160, 100));
			UIManager.put("nimbusAlertYellow", new Color(255, 210, 60));
			UIManager.put("nimbusInfoBlue", new Color(80, 140, 255));
			
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Initialize console.
		txtrConsole = new JTextArea();
		txtrConsole.setEditable(false);
		txtrConsole.setLineWrap(true);
		txtrConsole.setWrapStyleWord(true);
		txtrConsole.setDropMode(DropMode.INSERT);
		txtrConsole.setFont(new Font("Monospaced", Font.PLAIN, 13));
		txtrConsole.getDocument().addDocumentListener(new InterfaceLimit(500));
		
		final JMenuBar menuBar = new JMenuBar();
		menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		
		final JMenu mnActions = new JMenu("World");
		mnActions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnActions);
		
		final JMenuItem mntmShutdowna = new JMenuItem("Shutdown");
		mntmShutdowna.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmShutdowna.addActionListener(arg0 -> {
			if (JOptionPane.showOptionDialog(null, "Shutdown GameServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
			{
				final Object answer = JOptionPane.showInputDialog(null, "Shutdown delay in seconds", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "600");
				if (answer != null)
				{
					final String input = ((String) answer).trim();
					if (isDigit(input))
					{
						final int delay = Integer.parseInt(input);
						if (delay > 0)
						{
							
							Shutdown.getInstance().startShutdown("GM", delay, Shutdown.ShutdownModeType.SHUTDOWN);
							
						}
					}
				}
			}
		});
		mnActions.add(mntmShutdowna);
		
		final JMenuItem mntmRestart = new JMenuItem("Restart");
		mntmRestart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		
		mntmRestart.addActionListener(arg0 -> {
			if (JOptionPane.showOptionDialog(null, "Restart GameServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, restartOptions, restartOptions[1]) == 0)
			{
				final Object answer = JOptionPane.showInputDialog(null, "Restart delay in seconds", "Input", JOptionPane.INFORMATION_MESSAGE, null, null, "600");
				if (answer != null)
				{
					final String input = ((String) answer).trim();
					if (isDigit(input))
					{
						final int delay = Integer.parseInt(input);
						if (delay > 0)
						{
							
							Shutdown.getInstance().startShutdown("GM", delay, Shutdown.ShutdownModeType.RESTART);
							
						}
					}
				}
			}
		});
		mnActions.add(mntmRestart);
		
		// Set Panels.
		
		final JScrollPane scrollPanel = new JScrollPane(txtrConsole);
		scrollPanel.setBounds(0, 0, 300, 160);
		final JLayeredPane layeredPanel = new JLayeredPane();
		layeredPanel.add(scrollPanel, 0, 0);
		
		// Set frame.
		final JFrame frame = new JFrame("Dream - World");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				if (JOptionPane.showOptionDialog(null, "Shutdown World Server?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
				{
					
					Shutdown.getInstance().startShutdown("GM", 1, Shutdown.ShutdownModeType.SHUTDOWN);
					
				}
			}
		});
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent ev)
			{
				scrollPanel.setSize(frame.getContentPane().getSize());
				
			}
		});
		
		// Set icons.
		final List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "imgs" + File.separator + "16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "imgs" + File.separator + "32x32.png").getImage());
		
		frame.setJMenuBar(menuBar);
		frame.setIconImages(icons);
		frame.add(layeredPanel, BorderLayout.CENTER);
		frame.getContentPane().setPreferredSize(new Dimension(680, 360));
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		redirectSystemStreams();
		
		new SplashScreen(".." + File.separator + "imgs" + File.separator + "KnupGuard.png", frame);
		
	}
	
	private void redirectSystemStreams()
	{
		final OutputStream out = new OutputStream()
		{
			@Override
			public void write(int b)
			{
				updateTextArea(String.valueOf((char) b));
			}
			
			@Override
			public void write(byte[] b, int off, int len)
			{
				updateTextArea(new String(b, off, len));
			}
			
			@Override
			public void write(byte[] b)
			{
				write(b, 0, b.length);
			}
		};
		
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
	private void updateTextArea(String text)
	{
		SwingUtilities.invokeLater(() -> {
			txtrConsole.append(text);
			txtrConsole.setCaretPosition(txtrConsole.getText().length());
		});
	}
	
	public static boolean isDigit(String text)
	{
		if ((text == null) || text.isEmpty())
		{
			return false;
		}
		for (char c : text.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return false;
			}
		}
		return true;
	}
}
