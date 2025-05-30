/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.gsregistering;

import com.dream.AuthConfig;
import com.dream.L2AuthDatabaseFactory;
import com.dream.auth.manager.GameServerManager;
import com.dream.tools.network.Util;
import com.dream.tools.util.HexUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.util.Map;

import org.apache.log4j.xml.DOMConfigurator;

public class GameServerRegister
{
	private static GameServerManager gsServerManager;

	public static void cleanRegisteredGameServersFromDB()
	{
		GameServerManager.getInstance().deleteAllServer();
	}

	public static void main(String[] args) throws Throwable
	{
		DOMConfigurator.configure("./config/log4j.xml");
		AuthConfig.load();
		L2AuthDatabaseFactory.getInstance();
		gsServerManager = GameServerManager.getInstance();

		GameServerRegister gsRegister = new GameServerRegister();

		gsRegister.displayMenu();
	}

	private String _choice;

	private boolean _choiceOk;

	private void displayMenu() throws IOException
	{
		Util.printSection("");
		System.out.println("Welcome to Dream Server Register");
		System.out.println("Enter The id of the server you want to register");
		System.out.println("-- Type 'help' to get a list of ids.");
		System.out.println("-- Type 'clean' to unregister all currently registered servers from this Auth.");
		LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
		GameServerManager gameServerTable = GameServerManager.getInstance();
		while (!_choiceOk)
		{
			System.out.println("Your choice:");
			_choice = _in.readLine();

			if (_choice.equalsIgnoreCase("help"))
			{
				for (Map.Entry<Integer, String> entry : gameServerTable.getServerNames().entrySet())
				{
					System.out.println("Server ID: " + entry.getKey() + "\t- " + entry.getValue() + " - In Use: " + (gameServerTable.hasRegisteredGameServerOnId(entry.getKey()) ? "YES" : "NO"));
				}
				System.out.println("You can also see 'servername.xml'.");
			}
			else if (_choice.equalsIgnoreCase("clean"))
			{
				System.out.print("This is going to UNREGISTER ALL servers from this AuthServer. Are you sure? (y/n) ");
				_choice = _in.readLine();
				if (_choice.equals("y"))
				{
					GameServerRegister.cleanRegisteredGameServersFromDB();
					gsServerManager.getRegisteredGameServers().clear();
				}
				else
				{
					System.out.println("ABORTED");
				}
			}
			else
			{
				registerServer();
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void registerServer()
	{
		try
		{
			int id = new Integer(_choice).intValue();
			String name = gsServerManager.getServerNameById(id);
			if (name == null)
			{
				System.out.println("No name for id: " + id);
				return;
			}
			if (id < 0)
			{
				System.out.println("ID must be positive number");
			}
			else if (!gsServerManager.hasRegisteredGameServerOnId(id))
			{
				byte[] hex = HexUtil.generateHex(16);

				gsServerManager.registerServerOnDB(hex, id, "");
				HexUtil.saveHexid(id, new BigInteger(hex).toString(16), "hexid(server " + id + ").txt");
				System.out.println("Server Registered hexid saved to 'hexid(server " + id + ").txt'");
				System.out.println("Put this file in the config folder of your game and rename it to 'hexid.txt'");
				_choiceOk = true;
				return;
			}
			else
			{
				System.out.println("This ID isn't available.");
			}
		}
		catch (NumberFormatException nfe)
		{
			System.out.println("Type a number or 'help'.");
		}
	}

}