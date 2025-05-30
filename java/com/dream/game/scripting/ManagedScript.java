package com.dream.game.scripting;

import java.io.File;

public abstract class ManagedScript
{
	private final String _scriptFile;
	private long _lastLoadTime;
	private boolean _isActive;

	public ManagedScript()
	{
		_scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadingScript();
		setLastLoadTime(System.currentTimeMillis());
	}

	protected long getLastLoadTime()
	{
		return _lastLoadTime;
	}

	public String getScriptFile()
	{
		return _scriptFile;
	}

	public abstract ScriptManager<?> getScriptManager();

	public abstract String getScriptName();

	public boolean isActive()
	{
		return _isActive;
	}

	public boolean reload()
	{
		try
		{
			if (getScriptFile() != null)
			{
				L2ScriptEngineManager.getInstance().executeScript(new File(getScriptFile()));
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public void setActive(boolean status)
	{
		_isActive = status;
	}

	protected void setLastLoadTime(long lastLoadTime)
	{
		_lastLoadTime = lastLoadTime;
	}

	public abstract boolean unload();
}