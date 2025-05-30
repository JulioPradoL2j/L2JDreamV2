package com.dream.game.model.zone;

import com.dream.game.manager.ClanHallManager;
import com.dream.game.manager.clanhallsiege.BanditStrongholdSiege;
import com.dream.game.manager.clanhallsiege.WildBeastFarmSiege;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.ClanHall;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.AgitDecoInfo;

public class L2ClanhallZone extends L2DefaultZone
{
	protected ClanHall _clanhall;

	@Override
	protected void onEnter(L2Character character)
	{
		if (_clanhall.getId() == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, true);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		if (_clanhall.getId() == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, true);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_CLANHALL, true);

			if (_clanhall.getOwnerId() > 0)
			{
				AgitDecoInfo deco = new AgitDecoInfo(_clanhall);
				character.sendPacket(deco);
			}
		}
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_clanhall.getId() == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, false);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		if (_clanhall.getId() == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, false);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_CLANHALL, false);
		}
		super.onExit(character);
	}

	@Override
	protected void register()
	{
		_clanhall = ClanHallManager.getInstance().getClanHallById(_clanhallId);
		_clanhall.registerZone(this);
	}

	public void updateSiegeStatus()
	{
		if (_clanhall.getId() == 35 && BanditStrongholdSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (Exception e)
				{
				}
			}
		}
		else if (_clanhall.getId() == 63 && WildBeastFarmSiege.getInstance().getIsInProgress())
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (Exception e)
				{
				}
			}
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				try
				{
					character.setInsideZone(this, FLAG_PVP, false);

					if (character instanceof L2PcInstance)
					{
						character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					}
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}