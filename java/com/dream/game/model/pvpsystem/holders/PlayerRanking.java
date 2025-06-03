package com.dream.game.model.pvpsystem.holders;

public class PlayerRanking
{
	private final int charId;
	private int pvpPoints;
	private int pkPoints;
	private String currentRank;
	private int currentTier;
	private long lastPvpTime;
	private int classId;
	
	public PlayerRanking(int charId, int pvpPoints, int pkPoints, String currentRank, int currentTier, long lastPvpTime, int classId)
	{
		this.charId = charId;
		this.pvpPoints = pvpPoints;
		this.pkPoints = pkPoints;
		this.currentRank = currentRank;
		this.currentTier = currentTier;
		this.lastPvpTime = lastPvpTime;
		this.classId = classId;
	}
	
	public int getCharId()
	{
		return charId;
	}
	
	public int getPvpPoints()
	{
		return pvpPoints;
	}
	
	public void setPvpPoints(int pvpPoints)
	{
		this.pvpPoints = pvpPoints;
	}
	
	public int getPkPoints()
	{
		return pkPoints;
	}
	
	public void setPkPoints(int pkPoints)
	{
		this.pkPoints = pkPoints;
	}
	
	public String getCurrentRank()
	{
		return currentRank;
	}
	
	public void setCurrentRank(String currentRank)
	{
		this.currentRank = currentRank;
	}
	
	public int getCurrentTier()
	{
		return currentTier;
	}
	
	public void setCurrentTier(int currentTier)
	{
		this.currentTier = currentTier;
	}
	
	public long getLastPvpTime()
	{
		return lastPvpTime;
	}
	
	public void setLastPvpTime(long lastPvpTime)
	{
		this.lastPvpTime = lastPvpTime;
	}
	
	public int getClassId()
	{
		return classId;
	}
	
	public void setClassId(int classId)
	{
		this.classId = classId;
	}
}