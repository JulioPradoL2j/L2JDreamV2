package com.dream.game.model.zone;

import com.dream.Config;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.clanhallsiege.DevastatedCastleSiege;
import com.dream.game.manager.clanhallsiege.FortressOfDeadSiege;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.network.SystemMessageId;

public class L2SiegeZone extends EntityZone
{
	@Override
	public void onDieInside(L2Character character)
	{
		if (_entity instanceof Fort)
		{
			Fort fort = (Fort) _entity;
			if (fort.getSiege().getIsInProgress())
				if (character instanceof L2PcInstance && ((L2PcInstance) character).getClan() != null)
				{
					int lvl = 1;
					for (L2Effect effect : character.getAllEffects())
						if (effect != null && effect.getSkill().getId() == 5660)
						{
							lvl = lvl + effect.getLevel();
							if (lvl > 5)
							{
								lvl = 5;
							}
							break;
						}
					L2Clan clan;
					L2Skill skill;
					if (fort.getOwnerClan() == ((L2PcInstance) character).getClan())
					{
						skill = SkillTable.getInstance().getInfo(5660, lvl);
						if (skill != null)
						{
							skill.getEffects(character, character);
						}
					}
					else
					{
						for (L2SiegeClan siegeclan : fort.getSiege().getAttackerClans())
						{
							if (siegeclan == null)
							{
								continue;
							}

							clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
							if (((L2PcInstance) character).getClan() == clan)
							{
								skill = SkillTable.getInstance().getInfo(5660, lvl);
								if (skill != null)
								{
									skill.getEffects(character, character);
								}
								break;
							}
						}
					}
				}
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_castleId == 64 && FortressOfDeadSiege.getInstance().getIsInProgress() || _castleId == 34 && DevastatedCastleSiege.getInstance().getIsInProgress() || _entity instanceof Castle && ((Castle) _entity).getSiege().getIsInProgress() || _entity instanceof Fort && ((Fort) _entity).getSiege().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, true);
			character.setInsideZone(this, FLAG_SIEGE, true);
			character.setInsideZone(this, FLAG_NOSUMMON, true);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);

				if (((L2PcInstance) character).getClan() != null && _entity instanceof Fort && (((Fort) _entity).getSiege().checkIsAttacker(((L2PcInstance) character).getClan()) || ((Fort) _entity).getSiege().checkIsDefender(((L2PcInstance) character).getClan())))
					if (((L2PcInstance) character).getClan() != null && _entity instanceof Castle && (((Castle) _entity).getSiege().checkIsAttacker(((L2PcInstance) character).getClan()) || ((Castle) _entity).getSiege().checkIsDefender(((L2PcInstance) character).getClan())))
						if (!Config.ALT_FLYING_WYVERN_IN_SIEGE)
						{
							character.setInsideZone(this, FLAG_NOLANDING, true);
							if (((L2PcInstance) character).getMountType() == 2)
							{
								character.teleToLocation(TeleportWhereType.Town);
								character.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
								((L2PcInstance) character).enteredNoLanding();
							}
						}
			}
		}
		super.onEnter(character);
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_entity instanceof Castle && ((Castle) _entity).getSiege().getIsInProgress() || _entity instanceof Fort && ((Fort) _entity).getSiege().getIsInProgress() || _castleId == 34 && DevastatedCastleSiege.getInstance().getIsInProgress() || _castleId == 64 && FortressOfDeadSiege.getInstance().getIsInProgress())
		{
			character.setInsideZone(this, FLAG_PVP, false);
			character.setInsideZone(this, FLAG_SIEGE, false);
			character.setInsideZone(this, FLAG_NOSUMMON, false);
			if (character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);

				if (((L2PcInstance) character).getPvpFlag() == 0)
				{
					character.startPvPFlag();
				}
				if (!Config.ALT_FLYING_WYVERN_IN_SIEGE)
				{
					character.setInsideZone(this, FLAG_NOLANDING, false);
					if (((L2PcInstance) character).getMountType() == 2)
					{
						((L2PcInstance) character).exitedNoLanding();
					}
				}
			}
		}
		if (character instanceof L2SiegeSummonInstance)
		{
			((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
		}
		if (character instanceof L2PcInstance)
		{
			L2PcInstance activeChar = (L2PcInstance) character;
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(Config.FORTSIEGE_COMBAT_FLAG_ID);
			if (item != null)
			{
				Fort fort = FortManager.getInstance().getFort(activeChar);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(activeChar);
				}
				else
				{
					int slot = item.getItem().getBodyPart();
					activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
					activeChar.destroyItem("CombatFlag", item, null, true);
				}
			}
		}
		super.onExit(character);
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	@Override
	protected void register()
	{
		if (_castleId > 0)
		{
			_entity = CastleManager.getInstance().getCastleById(_castleId);
			if (_entity != null)
			{
				((Castle) _entity).getSiege();
				_entity.registerSiegeZone(this);
			}
			else if (_castleId == 34)
			{
				DevastatedCastleSiege.getInstance().registerSiegeZone(this);
			}
			else if (_castleId == 64)
			{
				FortressOfDeadSiege.getInstance().registerSiegeZone(this);
			}
			else
			{
				_log.warn("Invalid castleId: " + _castleId);
			}
		}
		else if (_fortId > 0)
		{
			_entity = FortManager.getInstance().getFortById(_fortId);
			if (_entity != null)
			{
				((Fort) _entity).getSiege();
				_entity.registerSiegeZone(this);
			}
			else
			{
				_log.warn("Invalid fortId: " + _fortId);
			}
		}
	}

	public void updateSiegeStatus()
	{
		if (_castleId == 64 && FortressOfDeadSiege.getInstance().getIsInProgress() || _castleId == 34 && DevastatedCastleSiege.getInstance().getIsInProgress() || _entity instanceof Castle && ((Castle) _entity).getSiege().getIsInProgress() || _entity instanceof Fort && ((Fort) _entity).getSiege().getIsInProgress())
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
					character.setInsideZone(this, FLAG_SIEGE, false);
					character.setInsideZone(this, FLAG_NOSUMMON, false);

					if (character instanceof L2PcInstance)
					{
						character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					}
					if (character instanceof L2SiegeSummonInstance)
					{
						((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
					}
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}