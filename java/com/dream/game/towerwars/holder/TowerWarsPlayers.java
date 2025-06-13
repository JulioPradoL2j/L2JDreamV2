package com.dream.game.towerwars.holder;

import com.dream.data.xml.StatSet;

public class TowerWarsPlayers
{
	private final String _name;
	private final TowerWarsPlayersSpot _players;
	public TowerWarsPlayers(StatSet set, TowerWarsPlayersSpot players){_name = set.getString("name");_players = players;}
	public String getName() { return _name; }
	public TowerWarsPlayersSpot getPlayers() { return _players; }
}