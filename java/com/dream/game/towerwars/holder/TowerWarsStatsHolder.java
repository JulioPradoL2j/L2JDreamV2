package com.dream.game.towerwars.holder;

public class TowerWarsStatsHolder
{
	private boolean _popup;
	private int _respawnTimeTower;
	private int _respawnTimeInhibitor;
	public boolean isPopup(){return _popup;}
	public void setPopup(boolean val){_popup = val;}
	public int isRespawnTowerTime(){return _respawnTimeTower;}
	public void setRespawnTowerTime(int val){_respawnTimeTower = val;}
	public int isRespawnInhibitorTime(){return _respawnTimeInhibitor;}
	public void setRespawnInhibitorTime(int val){_respawnTimeInhibitor = val;}
}
