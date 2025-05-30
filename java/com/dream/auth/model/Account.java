package com.dream.auth.model;

public class Account implements java.io.Serializable
{
	private static final long serialVersionUID = 6410734961032035356L;
	private String login;
	private String password;
	private long lastactive;
	private Integer accessLevel;
	private Integer lastServerId = 0;
	private String lastIp;

	public Account()
	{

	}

	public Account(String _login)
	{
		login = _login;
	}

	public Account(String _login, String _password, int _accessLevel)
	{
		this(_login, _password, _accessLevel, 0, -1, "");
	}

	public Account(String _login, String _password, long _lastactive, Integer _accessLevel, Integer _lastServerId, String _lastIp)
	{
		login = _login;
		password = _password;
		lastactive = _lastactive;
		accessLevel = _accessLevel;
		lastServerId = _lastServerId;
		lastIp = _lastIp;
	}

	public Integer getAccessLevel()
	{
		return accessLevel;
	}

	public long getLastactive()
	{
		return lastactive;
	}

	public String getLastIp()
	{
		return lastIp;
	}

	public Integer getLastServerId()
	{
		return lastServerId;
	}

	public String getLogin()
	{
		return login;
	}

	public String getPassword()
	{
		return password;
	}

	public void setAccessLevel(Integer _accessLevel)
	{
		accessLevel = _accessLevel;
	}

	public void setLastactive(long _lastactive)
	{
		lastactive = _lastactive;
	}

	public void setLastIp(String _lastIp)
	{
		lastIp = _lastIp;
	}

	public void setLastServerId(Integer _lastServerId)
	{
		lastServerId = _lastServerId;
	}

	public void setLogin(String _login)
	{
		login = _login;
	}

	public void setPassword(String _password)
	{
		password = _password;
	}
}