package com.dream.game.scripting;

public abstract class ScriptManager<S extends ManagedScript>
{
	public abstract Iterable<S> getAllManagedScripts();

	public abstract String getScriptManagerName();

	public boolean reload(S ms)
	{
		return ms.reload();
	}

	public void setActive(S ms, boolean status)
	{
		ms.setActive(status);
	}

	public boolean unload(S ms)
	{
		return ms.unload();
	}
}