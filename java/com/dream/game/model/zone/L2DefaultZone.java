package com.dream.game.model.zone;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ShowMiniMap;

public class L2DefaultZone extends L2Zone
{
	public final static int REASON_OK = 0;
	public final static int REASON_MULTIPLE_INSTANCE = 1;

	public final static int REASON_INSTANCE_FULL = 2;

	public final static int REASON_SMALL_GROUP = 3;

	@Override
	public void onDieInside(L2Character character)
	{
		if (_exitOnDeath)
		{
			onExit(character);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_onEnterMsg != null && character instanceof L2PcInstance)
		{
			character.sendPacket(_onEnterMsg);
		}

		if (_abnormal > 0)
		{
			character.startAbnormalEffect(_abnormal);
		}

		if (_removeAll)
		{
			character.stopAllEffects();
		}

		if (_applyEnter != null)
			if (!character.isDead())
			{
				for (L2Skill sk : _applyEnter)
				{
					SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel()).getEffects(character, character);
					sk.getEffects(character, character);
				}
			}
		if (_removeEnter != null)
		{
			for (L2Skill sk : _removeEnter)
			{
				character.stopSkillEffects(sk.getId());
			}
		}

		if (_funcTemplates != null)
		{
			character.addStatFuncs(getStatFuncs(character));
		}

		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(this, FLAG_NOSUMMON, true);
			character.setInsideZone(this, FLAG_PVP, true);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		else if (_pvp == PvpSettings.PEACE)
			if (Config.ZONE_TOWN != 2)
			{
				character.setInsideZone(this, FLAG_PEACE, true);
			}

		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_NOLANDING, true);
			if (((L2PcInstance) character).getMountType() == 2)
			{
				character.teleToLocation(TeleportWhereType.Town);
				character.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
				((L2PcInstance) character).enteredNoLanding();
			}
		}

		if (_noStrider && character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_NOSTRIDER, true);
			if (((L2PcInstance) character).getMountType() == 1)
			{
				// ((L2PcInstance) character).dismount();
				character.sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				((L2PcInstance) character).enteredNoLanding();
			}
		}
		if (_noEscape)
		{
			character.setInsideZone(this, FLAG_NOESCAPE, true);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(this, FLAG_NOSTORE, true);
		}
		if (_noSummon)
		{
			character.setInsideZone(this, FLAG_NOSUMMON, true);
		}
		if (_noMiniMap && character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendMessage(Message.getMessage((L2PcInstance) character, Message.MessageId.MSG_MAP_NOT_ALLOWED));
			character.setInsideZone(this, FLAG_NOMAP, true);
			if (((L2PcInstance) character).isMiniMapOpen())
			{
				((L2PcInstance) character).setMiniMapOpen(false);
				character.sendPacket(new ShowMiniMap());
			}
		}
		if (_noChat)
		{
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage(Message.getMessage((L2PcInstance) character, Message.MessageId.MSG_NO_CHAT));
			}
			character.setInsideZone(this, FLAG_NOCHAT, true);
		}
		if (_chaotic)
		{
			if (character instanceof L2PcInstance)
			{
				character.stopPvPFlag();
				((L2PcInstance) character).setPvpFlag(1);
				((L2PcInstance) character).broadcastUserInfo();

				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
		if (_noRestart)
		{
			if (character instanceof L2PcInstance)
			{
				character.setInsideZone(this, FLAG_NORESTART, true);
			}
		}
		if (_allowaio)
		{
			if (character instanceof L2PcInstance)
			{
				character.setInsideZone(this, FLAG_ALLOWAIO, true);
			}
		}
		if (_trade)
		{
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage(Message.getMessage((L2PcInstance) character, Message.MessageId.MSG_ENTER_TRADE_ZONE));
			}
			character.setInsideZone(this, FLAG_TRADE, true);
		}
		if (_Queen)
		{
			character.setInsideZone(this, FLAG_QUEEN, true);
		}
		if (_Baium && character.isGrandBoss())
		{
			character.setInsideZone(this, FLAG_BAIUM, true);
		}
		if (_Zaken && character.isGrandBoss())
		{
			character.setInsideZone(this, FLAG_ZAKEN, true);
		}
		if (_artefactCast)
		{
			character.setInsideZone(this, FLAG_ARTEFACTCAST, true);
		}
		character.broadcastFullInfo();
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_onExitMsg != null && character instanceof L2PcInstance)
		{
			character.sendPacket(_onExitMsg);
		}

		if (_abnormal > 0)
		{
			character.stopAbnormalEffect(_abnormal);
		}

		if (_applyExit != null)
		{
			for (L2Skill sk : _applyExit)
			{
				sk.getEffects(character, character);
			}
		}
		if (_removeExit != null)
		{
			for (L2Skill sk : _removeExit)
			{
				character.stopSkillEffects(sk.getId());
			}
		}
		if (_funcTemplates != null)
		{
			character.removeStatsOwner(this);
		}

		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(this, FLAG_NOSUMMON, false);
			character.setInsideZone(this, FLAG_PVP, false);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			character.setPreventedFromReceivingBuffs(false);
			if (character instanceof L2PcInstance && character.isPreventedFromReceivingBuffs())
			{
				((L2PcInstance) character).sendMessage("Block buff is off");
			}
			character.setInsideZone(this, FLAG_PEACE, false);
		}

		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_NOLANDING, false);
			if (((L2PcInstance) character).getMountType() == 2)
			{
				((L2PcInstance) character).exitedNoLanding();
			}
		}
		if (_noStrider && character instanceof L2PcInstance)
		{
			character.setInsideZone(this, FLAG_NOSTRIDER, false);
			if (((L2PcInstance) character).getMountType() == 1)
			{
				((L2PcInstance) character).exitedNoLanding();
			}
		}
		if (_noEscape)
		{
			character.setInsideZone(this, FLAG_NOESCAPE, false);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(this, FLAG_NOSTORE, false);
		}
		if (_noSummon)
		{
			character.setInsideZone(this, FLAG_NOSUMMON, false);
		}
		if (_noMiniMap && character instanceof L2PcInstance)
		{
			if (character.isInsideZone(FLAG_NOMAP) && character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage(Message.getMessage((L2PcInstance) character, Message.MessageId.MSG_MAP_ON));
			}
			character.setInsideZone(this, FLAG_NOMAP, false);
		}
		if (_noChat)
		{
			character.setInsideZone(this, FLAG_NOCHAT, false);
		}
		if (_trade)
		{
			character.setInsideZone(this, FLAG_TRADE, false);
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage(Message.getMessage((L2PcInstance) character, Message.MessageId.MSG_EXIT_TRADE_ZONE));
			}
		}
		if (_chaotic)
		{
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).setPvpFlag(0);
				((L2PcInstance) character).updatePvPStatus();
				((L2PcInstance) character).broadcastUserInfo();

				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}
		}
		if (_noRestart)
		{
			if (character instanceof L2PcInstance)
			{
				character.setInsideZone(this, FLAG_NORESTART, false);
			}
		}
		if (_allowaio)
		{
			if (character instanceof L2PcInstance)
			{
				character.setInsideZone(this, FLAG_ALLOWAIO, false);
			}
		}
		if (_Queen)
		{
			character.setInsideZone(this, FLAG_QUEEN, false);
		}
		if (_Baium && character.isGrandBoss())
		{
			character.setInsideZone(this, FLAG_BAIUM, false);
		}
		if (_Zaken && character.isGrandBoss())
		{
			character.setInsideZone(this, FLAG_ZAKEN, false);
		}
		if (_artefactCast)
		{
			character.setInsideZone(this, FLAG_ARTEFACTCAST, false);
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		if (_exitOnDeath)
		{
			onEnter(character);
		}
	}
}