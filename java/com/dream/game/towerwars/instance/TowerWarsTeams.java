package com.dream.game.towerwars.instance;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.towerwars.holder.TowerWarsPlayersSpot;

import java.util.ArrayList;
import java.util.List;

public class TowerWarsTeams
{
    private final List<L2PcInstance> _teamA = new ArrayList<>();
    private final List<L2PcInstance> _teamB = new ArrayList<>();

    private final TowerWarsPlayersSpot _spotA;
    private final TowerWarsPlayersSpot _spotB;

    public TowerWarsTeams(TowerWarsPlayersSpot spotA, TowerWarsPlayersSpot spotB)
    {
        _spotA = spotA;
        _spotB = spotB;
    }

    public void registerPlayer(L2PcInstance player)
    {
        if (_teamA.size() < 1)
            _teamA.add(player);
        else
            _teamB.add(player);
    }

    public void startMatch()
    {
        teleportTeams();
    }

    private void teleportTeams()
    {
        for (L2PcInstance player : _teamA)
            player.teleToLocation(_spotA.getX(), _spotA.getY(), _spotA.getZ());

        for (L2PcInstance player : _teamB)
            player.teleToLocation(_spotB.getX(), _spotB.getY(), _spotB.getZ());
    }
}
