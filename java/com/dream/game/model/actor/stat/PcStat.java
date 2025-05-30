/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dream.game.model.actor.stat;

import com.dream.Config;
import com.dream.game.handler.IVoicedCommandHandler;
import com.dream.game.handler.VoicedCommandHandler;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;

public class PcStat extends PlayableStat
{
	private int _oldMaxHp, _oldMaxMp, _oldMaxCp;

	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();

		if (!super.addExp(value))
			return false;

		if (!activeChar.isCursedWeaponEquipped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(L2Zone.FLAG_PVP)))
		{
			int karmaLost = activeChar.calculateKarmaLost((int) value);
			if (karmaLost > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost);
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(activeChar.getKarma()));
			}
			int karmaLost2 = activeChar.calculateKarmaLost((int) value);
			if (activeChar.getActingSummon() != null | karmaLost2 > 0)
			{
				activeChar.setKarma(activeChar.getKarma() - karmaLost2);
				activeChar.getActingSummon().broadcastFullInfoImpl();
			}
		}
		activeChar.sendPacket(new UserInfo(activeChar));
		return true;
	}

	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		float ratioTakenByPet = 0;
		L2PcInstance activeChar = getActiveChar();

		float baseRates[] = new float[2];
		baseRates[0] = addToExp / Config.RATE_XP;
		baseRates[1] = addToSp / Config.RATE_SP;

		if (activeChar.getPet() instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			ratioTakenByPet = pet.getPetData().getOwnerExpTaken();

			if (ratioTakenByPet > 0 && !pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * ratioTakenByPet), (int) (addToSp * ratioTakenByPet));
			}

			if (ratioTakenByPet > 1)
			{
				ratioTakenByPet = 1;
			}
			addToExp = (long) (addToExp * (1 - ratioTakenByPet));
			addToSp = (int) (addToSp * (1 - ratioTakenByPet));
		}


		if (!super.addExpAndSp(addToExp, addToSp))
			return false;

		if (addToExp == 0 && addToSp > 0)
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp));
		else if (addToExp > 0 && addToSp == 0)
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp));
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp));

		return true;

	}

	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > Experience.MAX_LEVEL - 1)
			return false;

		boolean levelIncreased = super.addLevel(value);

		L2PcInstance activeChar = getActiveChar();

		if (levelIncreased)
		{
			QuestState qs = activeChar.getQuestState("255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("CE40", null, activeChar);
			}

			activeChar.getStatus().setCurrentCp(getMaxCp());
			activeChar.broadcastPacket(new SocialAction(activeChar, 15));
			activeChar.sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
		}

		activeChar.rewardSkills();

		if (activeChar.getClan() != null)
		{
			activeChar.getClan().updateClanMember(activeChar);
			activeChar.getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(activeChar));
		}
		if (activeChar.isInParty())
		{
			activeChar.getParty().recalculatePartyLevel(); // Recalculate the
			// party level
		}

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		activeChar.sendPacket(su);

		activeChar.refreshOverloaded();
		activeChar.refreshExpertisePenalty();
		activeChar.sendPacket(new UserInfo(activeChar));

		if (Config.CLASS_MASTER_POPUP && !activeChar.isAio())
		{
			if (activeChar.isAio())
			{
			}
			IVoicedCommandHandler handler = VoicedCommandHandler.getInstance().getVoicedCommandHandler("classmaster");
			if (handler != null)
			{
				int classLevel = activeChar.getClassLevel();
				if (classLevel == 0 && getLevel() >= 20)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
				else if (classLevel == 1 && getLevel() >= 40)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
				else if (classLevel == 2 && getLevel() >= 76)
				{
					handler.useVoicedCommand("classmaster", activeChar, "");
				}
			}
		}
		activeChar.intemediateStore();
		return levelIncreased;
	}

	@Override
	public boolean addSp(int value)
	{
		if (!super.addSp(value))
			return false;
		L2PcInstance activeChar = getActiveChar();
		activeChar.sendPacket(new UserInfo(activeChar));
		return true;
	}

	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) _activeChar;
	}

	@Override
	public final long getExp()
	{
		L2PcInstance activeChar = getActiveChar();

		if (activeChar.isSubClassActive())
		{
			try
			{
				return activeChar.getSubclassByIndex(activeChar.getClassIndex()).getExp();
			}
			catch (NullPointerException npe)
			{

			}
		}
		return super.getExp();

	}

	@Override
	public final long getExpForLevel(int level)
	{
		return Experience.LEVEL[level];
	}

	@Override
	public final byte getLevel()
	{
		L2PcInstance activeChar = getActiveChar();

		if (activeChar.isSubClassActive())
		{
			try
			{
				return activeChar.getSubclassByIndex(activeChar.getClassIndex()).getLevel();
			}
			catch (NullPointerException npe)
			{

			}
		}
		return super.getLevel();
	}

	@Override
	public final int getMaxCp()
	{
		L2PcInstance activeChar = getActiveChar();

		int val = super.getMaxCp();

		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;

			if (activeChar.getStatus() == null)
				return val;
			if (activeChar.getStatus().getCurrentCp() != val)
			{
				activeChar.getStatus().setCurrentCp(activeChar.getStatus().getCurrentCp());
			}
		}

		return val;
	}

	@Override
	public final int getMaxHp()
	{
		L2PcInstance activeChar = getActiveChar();

		int val = super.getMaxHp();
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			if (activeChar.getStatus() == null)
				return val;

			if (activeChar.getStatus().getCurrentHp() != val)
			{
				activeChar.getStatus().setCurrentHp(activeChar.getStatus().getCurrentHp());
			}
		}

		return val;
	}

	@Override
	public final int getMaxMp()
	{
		L2PcInstance activeChar = getActiveChar();

		int val = super.getMaxMp();

		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			if (activeChar.getStatus() == null)
				return val;

			if (activeChar.getStatus().getCurrentMp() != val)
			{
				activeChar.getStatus().setCurrentMp(activeChar.getStatus().getCurrentMp());
			}
		}

		return val;
	}

	@Override
	public final int getSp()
	{
		L2PcInstance activeChar = getActiveChar();

		if (activeChar.isSubClassActive())
			return activeChar.getSubclassByIndex(activeChar.getClassIndex()).getSp();
		return super.getSp();
	}

	@Override
	public boolean removeExpAndSp(long removeExp, int removeSp)
	{
		return removeExpAndSp(removeExp, removeSp, true);
	}

	public boolean removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		final int oldLevel = getLevel();

		if (!super.removeExpAndSp(removeExp, removeSp))
			return false;

		// Send messages.
		if (sendMessage)
		{
			if (removeExp > 0)
				getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) removeExp));

			if (removeSp > 0)
				getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(removeSp));

			if (getLevel() < oldLevel)
				getActiveChar().broadcastStatusUpdate();
		}
		return true;
	}

	@Override
	public final void setExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();

		if (activeChar.isSubClassActive())
		{
			activeChar.getSubclassByIndex(activeChar.getClassIndex()).setExp(value);
		}
		else
		{
			super.setExp(value);
		}
	}

	@Override
	public final void setLevel(byte value)
	{
		L2PcInstance activeChar = getActiveChar();

		if (value > Experience.MAX_LEVEL - 1)
		{
			value = (byte) (Experience.MAX_LEVEL - 1);
		}

		if (activeChar.isSubClassActive())
		{
			activeChar.getSubclassByIndex(activeChar.getClassIndex()).setLevel(value);
		}
		else
		{
			super.setLevel(value);
		}
		activeChar.sendSkillList();
	}

	@Override
	public final void setSp(int value)
	{
		L2PcInstance activeChar = getActiveChar();

		if (activeChar.isSubClassActive())
		{
			activeChar.getSubclassByIndex(activeChar.getClassIndex()).setSp(value);
		}
		else
		{
			super.setSp(value);
		}
	}

}
