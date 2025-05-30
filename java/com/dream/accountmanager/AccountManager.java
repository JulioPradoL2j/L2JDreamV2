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
package com.dream.accountmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import com.dream.AuthConfig;
import com.dream.L2AuthDatabaseFactory;
import com.dream.auth.manager.AuthManager;
import com.dream.auth.model.Account;
import com.dream.auth.services.AccountModificationException;
import com.dream.tools.network.Util;

public class AccountManager
{
	private static String _uname = "";

	private static String _pass = "";
	private static String _level = "";
	private static String _mode = "";

	public static void main(String[] args) throws Throwable
	{
		DOMConfigurator.configure("./config/log4j.xml");
		AuthConfig.load();
		L2AuthDatabaseFactory.getInstance();
		AuthManager.load();
		AccountManager accountManager = new AccountManager();

		boolean bContinue = true;
		while (bContinue)
		{
			bContinue = accountManager.displayMenu();
		}
	}

	private LineNumberReader _in = null;

	private AuthManager _loginManager = null;

	public AccountManager()
	{
		_in = new LineNumberReader(new InputStreamReader(System.in));
		_loginManager = AuthManager.getInstance();
	}

	private void displayChoices() throws IOException
	{
		_mode = "";
		_uname = "";
		_pass = "";
		_level = "";
		Util.printSection("");
		System.out.println("Please choose an option:");
		System.out.println("");
		System.out.println("1 - Create new account or update existing one (change pass and access level).");
		System.out.println("2 - Change access level.");
		System.out.println("3 - Delete existing account.");
		System.out.println("4 - List accounts & access levels.");
		System.out.println("5 - Exit.");
		while (!(_mode.equals("1") || _mode.equals("2") || _mode.equals("3") || _mode.equals("4") || _mode.equals("5")))
		{
			System.out.print("Your choice: ");
			_mode = _in.readLine();
		}

		if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
		{
			if (_mode.equals("1") || _mode.equals("2") || _mode.equals("3"))
			{
				while (_uname.length() == 0)
				{
					System.out.print("Username: ");
					_uname = _in.readLine();
				}
			}

			if (_mode.equals("1"))
			{
				while (_pass.length() == 0)
				{
					System.out.print("Password: ");
					_pass = _in.readLine();
				}
			}

			if (_mode.equals("1") || _mode.equals("2"))
			{
				while (_level.length() == 0)
				{
					System.out.print("Access level: ");
					_level = _in.readLine();
				}
			}
		}
	}

	private boolean displayMenu() throws IOException
	{
		boolean bResponse = true;

		displayChoices();

		if (_mode.equals("1"))
		{
			try
			{
				_loginManager.addOrUpdateAccount(_uname, _pass, Integer.parseInt(_level));
			}
			catch (AccountModificationException e)
			{
				System.out.println("Unable to complete operation.");
				e.printStackTrace();
				bResponse = false;
			}
		}
		else if (_mode.equals("2"))
		{
			try
			{
				_loginManager.changeAccountLevel(_uname, Integer.parseInt(_level));
			}
			catch (AccountModificationException e)
			{
				System.out.println("Unable to complete operation.");
				e.printStackTrace();
				bResponse = false;
			}
		}
		else if (_mode.equals("3"))
		{
			System.out.print("Do you really want to delete this account ? Y/N : ");
			String yesno = _in.readLine();
			if (yesno.equals("Y"))
			{
				try
				{
					_loginManager.deleteAccount(_uname);
				}
				catch (AccountModificationException e)
				{
					System.out.println("Unable to complete operation.");
					e.printStackTrace();
					bResponse = false;
				}
			}

		}
		else if (_mode.equals("4"))
		{
			List<Account> list = _loginManager.getAccountsInfo();
			for (Account account : list)
			{
				System.out.println(account.getLogin() + " -> " + account.getAccessLevel());
			}
			System.out.println("Number of accounts: " + list.size() + ".");

		}
		else if (_mode.equals("5"))
		{
			bResponse = false;
		}

		return bResponse;
	}
}