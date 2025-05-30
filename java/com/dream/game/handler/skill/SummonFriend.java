package com.dream.game.handler.skill;

import com.dream.Config;
import com.dream.game.datatables.xml.MapRegionTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ConfirmDlg;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class SummonFriend implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SUMMON_FRIEND
	};

	public static boolean checkSummonerStatus(L2PcInstance summonerChar)
	{
		if (summonerChar == null)
			return false;

		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		if (summonerChar.isFlying())
			return false;
		if (summonerChar.inObserverMode())

			return false;
		if (summonerChar.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		return true;
	}

	public static boolean checkTargetStatus(L2PcInstance targetChar, L2PcInstance summonerChar)
	{
		if (targetChar == null)
			return false;
		if (targetChar.isAlikeDead())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addPcName(targetChar));
			return false;
		}
		if (targetChar.isInStoreMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addPcName(targetChar));
			return false;
		}
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addPcName(targetChar));
			return false;
		}
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		if (targetChar.inObserverMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if (targetChar.isFestivalParticipant())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if (summonerChar.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addString(targetChar.getName()));
			return false;
		}

		if (ObjectRestrictions.getInstance().checkRestriction(targetChar, AvailableRestriction.PlayerSummonFriend))
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if (summonerChar._event != null && summonerChar._event.isRunning() || targetChar._event != null && targetChar._event.isRunning())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if (summonerChar.isIn7sDungeon())
		{
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(targetChar);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			}
			else if (targetCabal == SevenSigns.CABAL_NULL)
			{
				summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
				return false;
			}
		}

		return true;
	}

	public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
	{
		if (!Config.ALLOW_AIO_TELEPORT)
		{
			if (targetChar.isAio() && !targetChar.isInsideZone(L2Zone.FLAG_ALLOWAIO))
			{
				targetChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
		if (targetChar == null || summonerChar == null || summonSkill == null)
			return;

		if (!checkSummonerStatus(summonerChar))
			return;
		if (!checkTargetStatus(targetChar, summonerChar))
			return;

		int itemConsumeId = summonSkill.getTargetConsumeId();
		int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(itemConsumeId));
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemConsumeId));
		}
		int x = summonerChar.getX();
		int y = summonerChar.getY();
		int n = 0;
		for (;;)
		{
			x += Rnd.get((int) summonerChar.getTemplate().getdCollisionRadius() * -2, (int) summonerChar.getTemplate().getdCollisionRadius() * 2);
			y += Rnd.get((int) summonerChar.getTemplate().getdCollisionRadius() * -2, (int) summonerChar.getTemplate().getdCollisionRadius() * 2);
			if (++n > 5 || GeoData.getInstance().canMoveFromToTarget(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), x, y, summonerChar.getZ()))
			{
				break;
			}
		}
		targetChar.teleToLocation(x, y, summonerChar.getZ(), false);

	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance activePlayer = (L2PcInstance) activeChar;
		if (!checkSummonerStatus(activePlayer))
			return;

		for (L2Character element : targets)
		{
			if (!(element instanceof L2PcInstance))
			{
				continue;
			}

			if (activeChar == element)
			{
				continue;
			}

			L2PcInstance target = (L2PcInstance) element;

			if (!checkTargetStatus(target, activePlayer))
			{
				continue;
			}

			if (!Util.checkIfInRange(0, activeChar, target, false))
			{
				if (!target.teleportRequest(activePlayer, skill))
				{
					activePlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addString(target.getName()));
					continue;
				}

				if (skill.getId() == 1403)
				{
					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
					confirm.addCharName(activeChar);
					confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
					confirm.addTime(30000);
					confirm.addRequesterId(activePlayer.getObjectId());
					target.sendPacket(confirm);
				}
				else
				{
					teleToTarget(target, activePlayer, skill);
					target.teleportRequest(null, null);
				}
			}
		}
	}
}