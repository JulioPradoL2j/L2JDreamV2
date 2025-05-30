package com.dream.game.network.serverpackets;

import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.SiegeManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;

public class Die extends L2GameServerPacket
{
	private final int _charObjId;
	private final boolean _fallDown;
	private boolean _sweepable;
	private boolean _inFunEvent = false;
	private final L2Character _activeChar;
	private int _showVillage = 0;
	private int _showClanhall = 0;
	private int _showCastle = 0;
	private int _showFlag;
	private int _fixedres = 0;

	public Die(L2Character cha)
	{
		_activeChar = cha;
		L2Clan clan = null;
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;
			clan = player.getClan();

			if (player.isInArenaEvent())
			{
				_inFunEvent = true;
			}
			if (player.isInFunEvent())
			{
				_inFunEvent = true;
			}
			if (player._event != null)
			{
				_inFunEvent = !player._event.canTeleportOnDie(player);
			}
			_fixedres = player.isGM() ? 1 : 0;
		}
		_charObjId = cha.getObjectId();
		_fallDown = cha.mustFallDownOnDeath();
		if (cha instanceof L2Attackable)
		{
			_sweepable = ((L2Attackable) cha).isSweepActive();
		}
		if (clan != null)
		{
			_showClanhall = clan.getHasHideout() <= 0 ? 0 : 1;
			_showCastle = clan.getHasCastle() <= 0 ? 0 : 1;

			L2SiegeClan siegeClan = null;
			boolean isInDefense = false;
			Siege siege = SiegeManager.getSiege(_activeChar);
			if (siege != null && siege.getIsInProgress())
			{
				siegeClan = siege.getAttackerClan(clan);
				if (siegeClan == null && siege.checkIsDefender(clan))
				{
					isInDefense = true;
				}
			}
			else
			{
				FortSiege fsiege = FortSiegeManager.getSiege(_activeChar);
				if (fsiege != null && fsiege.getIsInProgress())
				{
					siegeClan = fsiege.getAttackerClan(clan);
					if (siegeClan == null && fsiege.checkIsDefender(clan))
					{
						isInDefense = true;
					}
				}
			}
			_showFlag = siegeClan == null || isInDefense || siegeClan.getFlag().size() <= 0 ? 0 : 1;
			if (BanditStrongholdSiege.getInstance().getIsInProgress())
				if (BanditStrongholdSiege.getInstance().isPlayerRegister(clan, _activeChar.getName()))
				{
					_showFlag = 1;
				}
			if (WildBeastFarmSiege.getInstance().getIsInProgress())
				if (WildBeastFarmSiege.getInstance().isPlayerRegister(clan, _activeChar.getName()))
				{
					_showFlag = 1;
				}
		}
		_showVillage = 1;
	}

	@Override
	protected final void writeImpl()
	{
		if (!_fallDown)
			return;

		writeC(0x6);
		writeD(_charObjId);
		writeD(_inFunEvent ? 0x00 : _showVillage);
		writeD(_inFunEvent ? 0x00 : _showClanhall);
		writeD(_inFunEvent ? 0x00 : _showCastle);
		writeD(_showFlag);
		writeD(_sweepable ? 0x01 : 0x00);
		writeD(_inFunEvent ? 0x00 : _fixedres);
	}

}