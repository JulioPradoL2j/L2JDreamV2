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

import com.dream.Message;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.model.L2PetData;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.base.Experience;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;

public class PetStat extends SummonStat
{
	public PetStat(L2PetInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addExp(long value)
	{
		if (!super.addExp(value))
			return false;

		getActiveChar().broadcastFullInfo();

		return true;
	}

	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;

		getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
		getActiveChar().broadcastFullInfo();

		return true;
	}

	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > Experience.MAX_LEVEL - 1)
			return false;

		boolean levelIncreased = super.addLevel(value);

		if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel()))
		{
			setExp(Experience.LEVEL[getLevel()]);
		}

		if (levelIncreased)
		{
			getActiveChar().getOwner().sendMessage(Message.getMessage(getActiveChar().getOwner(), Message.MessageId.MSG_ACTION_PET_LEVEL_UP));
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
		}

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().broadcastPacket(su);

		getActiveChar().broadcastFullInfo();

		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}

		return levelIncreased;
	}

	@Override
	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null);
	}

	@Override
	public L2PetInstance getActiveChar()
	{
		return (L2PetInstance) _activeChar;
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null);
	}

	@Override
	public int getEvasionRate(L2Character target)
	{
		return (int) calcStat(Stats.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null);
	}

	@Override
	public final long getExpForLevel(int level)
	{
		L2PetData data = PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level);
		if (data != null)
			return data.getPetMaxExp();

		_log.warn("Pet NPC ID " + getActiveChar().getNpcId() + ", level " + level + " is missing data from pets_stats table!");
		return 5000000L * level;
	}

	public final int getFeedBattle()
	{
		return getActiveChar().getPetData().getPetFeedBattle();
	}

	public final int getFeedNormal()
	{
		return getActiveChar().getPetData().getPetFeedNormal();
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = getActiveChar().getPetData().getPetMAtk();

		if (skill != null)
		{
			attack += skill.getPower();
		}
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	@Override
	public int getMAtkSpd()
	{
		if (getActiveChar() == null)
			return 1;
		int val = (int) calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null);
		if (!getActiveChar().isRunning())
		{
			val = val / 2;
		}

		return val;
	}

	public final int getMaxFeed()
	{
		return getActiveChar().getPetData().getPetMaxFeed();
	}

	@Override
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null);
	}

	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null);
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		double defence = getActiveChar().getPetData().getPetMDef();
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
			return 1;
		float val = getRunSpeed() * 1f / getActiveChar().getPetData().getPetSpeed();
		if (!getActiveChar().isRunning())
		{
			val = val / 2;
		}

		return val;
	}

	@Override
	public int getPAtk(L2Character target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null);
	}

	@Override
	public int getPAtkSpd()
	{
		int val = (int) calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null);
		if (!getActiveChar().isRunning())
		{
			val = val / 2;
		}

		return val;
	}

	@Override
	public int getPDef(L2Character target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null);
	}

	public int getRegenHp()
	{
		return (int) calcStat(Stats.REGENERATE_HP_RATE, getActiveChar().getPetData().getPetRegenHP(), null, null);
	}

	public int getRegenMp()
	{
		return (int) calcStat(Stats.REGENERATE_MP_RATE, getActiveChar().getPetData().getPetRegenMP(), null, null);
	}

	@Override
	public int getRunSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, getActiveChar().getPetData().getPetSpeed(), null, null);
	}

	@Override
	public int getWalkSpeed()
	{
		return getRunSpeed() / 2;
	}

	@Override
	public void setLevel(byte value)
	{
		getActiveChar().stopFeed();
		super.setLevel(value);

		getActiveChar().setPetData(PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().getNpcId(), getLevel()));
		getActiveChar().startFeed();

		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}
	}
}