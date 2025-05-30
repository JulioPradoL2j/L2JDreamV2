package com.dream.game.access;

import java.util.ArrayList;
import java.util.List;

public class GmPlayer
{
	private String name;
	private final int ojId;
	private boolean root = false;
	private boolean norm = false;
	private boolean FixedRes = false;
	private boolean AltG = false;
	private boolean PeaceAtk = false;
	private boolean CheckIP = false;
	private boolean isTemp = false;
	private String[] SecureIP;
	private final List<String> commands;

	public GmPlayer(int id)
	{
		ojId = id;
		commands = new ArrayList<>();
	}

	public boolean allowAltG()
	{
		return AltG;
	}

	public boolean allowFixRes()
	{
		return FixedRes;
	}

	public boolean allowPeaceAtk()
	{
		return PeaceAtk;
	}

	public boolean checkIp()
	{
		return CheckIP;
	}

	public List<String> getCommands()
	{
		return commands;
	}

	public boolean getIsTemp()
	{
		return isTemp;
	}

	public String getName()
	{
		return name;
	}

	public int getObjId()
	{
		return ojId;
	}

	public boolean isGm()
	{
		return norm;
	}

	public boolean isRoot()
	{
		return root;
	}

	public void putCommand(String val)
	{
		commands.add(val);
	}

	public void seAltG(boolean val)
	{
		AltG = val;
	}

	public String[] secureIp()
	{
		return SecureIP;
	}

	public void seFixRes(boolean val)
	{
		FixedRes = val;
	}

	public void setCheckIp(boolean val)
	{
		CheckIP = val;
	}

	public void setGm(boolean val)
	{
		norm = val;
	}

	public void setIP(String[] val)
	{
		SecureIP = val;
	}

	public void setIsTemp(boolean val)
	{
		isTemp = val;
	}

	public void setName(String val)
	{
		name = val;
	}

	public void setPeaceAtk(boolean val)
	{
		PeaceAtk = val;
	}

	public void setRoot(boolean val)
	{
		root = val;
		if (val)
		{
			AltG = true;
			FixedRes = true;
			norm = true;
			PeaceAtk = true;
		}
	}
}