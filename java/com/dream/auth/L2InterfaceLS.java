package com.dream.auth;

import com.dream.auth.manager.BanManager;
import com.dream.auth.manager.GameServerManager;
import com.dream.auth.model.GameServerInfo;
import com.dream.auth.network.gameserverpackets.ServerStatus;
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
import java.util.Collection;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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

public class L2InterfaceLS
{
	JTextArea txtrConsole;
	JCheckBoxMenuItem chckbxmntmEnabled;
	JCheckBoxMenuItem chckbxmntmDisabled;
	JCheckBoxMenuItem chckbxmntmGmOnly;
	
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
	
	public L2InterfaceLS()
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
		
		final JMenu mnActions = new JMenu("Auth");
		mnActions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnActions);
		
		final JMenuItem mntmShutdown = new JMenuItem("Shutdown");
		mntmShutdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmShutdown.addActionListener(arg0 -> {
			if (JOptionPane.showOptionDialog(null, "Shutdown LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
			{
				System.exit(1);
			}
		});
		mnActions.add(mntmShutdown);
		
	
		
		final JMenu mnReload = new JMenu("Reload");
		mnReload.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnReload);
		
		final JMenuItem mntmBannedIps = new JMenuItem("Banned IPs");
		mntmBannedIps.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mntmBannedIps.addActionListener(arg0 -> {
			
			BanManager.getInstance().load();
			
		});
		mnReload.add(mntmBannedIps);
		
		final JMenu mnStatus = new JMenu("Status");
		mnStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		menuBar.add(mnStatus);
		
		chckbxmntmEnabled = new JCheckBoxMenuItem("Enabled");
		chckbxmntmEnabled.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmEnabled.addActionListener(arg0 -> {
			chckbxmntmEnabled.setSelected(true);
			chckbxmntmDisabled.setSelected(false);
			chckbxmntmGmOnly.setSelected(false);
			
			L2AuthServer.getInstance().getGameServerListener();
			
			Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
			for (GameServerInfo gsi : serverList)
			{
				
				gsi.setStatus(ServerStatus.STATUS_NORMAL);
				
			}
			
			
		});
		chckbxmntmEnabled.setSelected(true);
		mnStatus.add(chckbxmntmEnabled);
		
		chckbxmntmDisabled = new JCheckBoxMenuItem("Disabled");
		chckbxmntmDisabled.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmDisabled.addActionListener(arg0 -> {
			chckbxmntmEnabled.setSelected(false);
			chckbxmntmDisabled.setSelected(true);
			chckbxmntmGmOnly.setSelected(false);
			
			L2AuthServer.getInstance().getGameServerListener();
			
			Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
			for (GameServerInfo gsi : serverList)
			{
				
				gsi.setStatus(ServerStatus.STATUS_DOWN);
				
			}
			
			
		});
		mnStatus.add(chckbxmntmDisabled);
		
		chckbxmntmGmOnly = new JCheckBoxMenuItem("GM only");
		chckbxmntmGmOnly.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		chckbxmntmGmOnly.addActionListener(arg0 -> {
			chckbxmntmEnabled.setSelected(false);
			chckbxmntmDisabled.setSelected(false);
			chckbxmntmGmOnly.setSelected(true);
			L2AuthServer.getInstance().getGameServerListener();
			
			Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
			for (GameServerInfo gsi : serverList)
			{
				
				gsi.setStatus(ServerStatus.STATUS_GM_ONLY);
				
			}
			
			
		});
		mnStatus.add(chckbxmntmGmOnly);
		
		// Set Panels.
		
		final JScrollPane scrollPanel = new JScrollPane(txtrConsole);
		scrollPanel.setBounds(0, 0, 300, 160);
		final JLayeredPane layeredPanel = new JLayeredPane();
		layeredPanel.add(scrollPanel, 0, 0);
		
		// Set frame.
		final JFrame frame = new JFrame("Dream - Auth");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent ev)
			{
				if (JOptionPane.showOptionDialog(null, "Shutdown LoginServer?", "Select an option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, shutdownOptions, shutdownOptions[1]) == 0)
				{
					System.exit(1);
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
