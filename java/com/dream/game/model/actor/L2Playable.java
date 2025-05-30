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
package com.dream.game.model.actor;

import com.dream.Config;
import com.dream.game.model.L2Effect;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.knownlist.PlayableKnownList;
import com.dream.game.model.actor.stat.PcStat;
import com.dream.game.model.actor.stat.PlayableStat;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.chars.L2CharTemplate;
import com.dream.game.templates.skills.L2EffectType;

public abstract class L2Playable extends L2Character
{
	public static final L2Playable[] EMPTY_ARRAY = new L2Playable[0];

	private boolean _isNoblesseBlessed = false;
	private boolean _getCharmOfLuck = false;
	private boolean _isPhoenixBlessed = false;
	private boolean _isSilentMoving = false;
	private boolean _protectionBlessing = false;

	private L2PcInstance player;

	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
	}

	@Override
	public final void addEffect(L2Effect newEffect)
	{
		super.addEffect(newEffect);

		updateEffectIcons();
	}

	public boolean checkIfPvP(L2Character target)
	{
		if (target == null)
			return false;
		if (target == this)
			return false;
		if (!(target instanceof L2Playable))
			return false;

		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		else if (this instanceof L2Summon)
		{
			player = ((L2Summon) this).getOwner();
		}

		if (player == null)
			return false;
		if (player.getKarma() != 0)
			return false;

		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}

		if (targetPlayer == null)
			return false;
		if (targetPlayer == this)
			return false;

		return targetPlayer.getKarma() == 0;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (killer != null && killer.getActingPlayer() != null && this instanceof L2PcInstance)
		{
			killer.getActingPlayer().onKillUpdatePvPKarma(this);
		}

		return true;
	}

	public final boolean getCharmOfLuck()
	{
		return _getCharmOfLuck;
	}

	@Override
	public PlayableKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new PlayableKnownList(this);
		}

		return (PlayableKnownList) _knownList;
	}

	public final boolean getProtectionBlessing()
	{
		return _protectionBlessing;
	}

	@Override
	public PlayableStat getStat()
	{
		if (_stat == null)
		{
			_stat = new PlayableStat(this);
		}

		return (PcStat) _stat;
	}

	@Override
	public boolean isAttackable()
	{
		return true;
	}

	public boolean isGM()
	{
		player = null;
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}

		return player.isGM();
	}

	public final boolean isNoblesseBlessed()
	{
		getStat().calcStat(Stats.NOBLE_BLESS, 1, this, null);
		return _isNoblesseBlessed;
	}

	public final boolean isPhoenixBlessed()
	{
		return _isPhoenixBlessed;
	}

	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}

	@Override
	public final void removeEffect(L2Effect effect)
	{
		super.removeEffect(effect);

		updateEffectIcons();
	}

	@Override
	public final void sendAvoidMessage(L2Character attacker)
	{
		/* SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK); sm.addCharName(this); sm.addCharName(attacker); getActingPlayer().sendPacket(sm); */
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		L2PcInstance attOwner = getActingPlayer();
		L2PcInstance trgOwner = target.getActingPlayer();
		L2Effect Tpain = target.getFirstEffect(1262);

		if (miss)
		{
			target.sendAvoidMessage(this);
			return;
		}

		if (pcrit)
		{
			attOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
		}

		if (mcrit)
		{
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
		}

		if (trgOwner != null && attOwner != trgOwner)
			if (attOwner.isInOlympiadMode() && target instanceof L2PcInstance && trgOwner.isInOlympiadMode() && trgOwner.getOlympiadGameId() == attOwner.getOlympiadGameId())
				if (this instanceof L2PcInstance || Config.ALT_OLY_INCLUDE_SUMMON_DAMAGE)
				{
					Olympiad.notifyCompetitorDamage(attOwner, damage, attOwner.getOlympiadGameId());
				}

		SystemMessage sm = null;
		if (target.isInvul() && !(target instanceof L2Npc))
		{
			sm = SystemMessageId.ATTACK_WAS_BLOCKED.getSystemMessage();
		}
		if (this instanceof L2PcInstance)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage);
			sm.addNumber(damage);
		}
		if (Tpain != null && this instanceof L2PcInstance)
			if (target.getPet() == null || target.getPet().isDead())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage);
			}
			else if (target.getPet().getNpcId() != 12077 & target.getPet().getNpcId() != 12311 & target.getPet().getNpcId() != 12312 & target.getPet().getNpcId() != 12313 & target.getPet().getNpcId() != 12526 & target.getPet().getNpcId() != 12527 & target.getPet().getNpcId() != 12528 & target.getPet().getNpcId() != 12564 & target.getPet().getNpcId() != 12621 & target.getPet().getNpcId() != 12780 & target.getPet().getNpcId() != 12781 & target.getPet().getNpcId() != 12782 & target.getPet() != null & !target.getPet().isDead())
			{
					if (Tpain.getSkill().getLevel() == 1)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber((damage / 10) * 9).addNumber(damage / 10);
					}
					if (Tpain.getSkill().getLevel() == 2)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber((damage / 10) * 8).addNumber(damage / 5);
					}
					if (Tpain.getSkill().getLevel() == 3)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber((damage / 10) * 7).addNumber((damage / 10) * 3);
					}
					if (Tpain.getSkill().getLevel() == 4)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber((damage / 10) * 6).addNumber((damage / 10) * 4);
					}
					if (Tpain.getSkill().getLevel() == 5)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(damage / 2).addNumber(damage / 2);
					}
				}

		if (sm != null)
		{
			attOwner.sendPacket(sm);
		}
	}

	public final void setCharmOfLuck(boolean value)
	{
		_getCharmOfLuck = value;
	}

	public final void setIsNoblesseBlessed(boolean value)
	{
		_isNoblesseBlessed = value;
	}

	public final void setIsPhoenixBlessed(boolean value)
	{
		_isPhoenixBlessed = value;
	}

	public final void setProtectionBlessing(boolean value)
	{
		_protectionBlessing = value;
	}

	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}

	public final void startCharmOfLuck()
	{
		setCharmOfLuck(true);
		updateAbnormalEffect();
	}

	public final void startNoblesseBlessing()
	{
		setIsNoblesseBlessed(true);
		updateAbnormalEffect();
	}

	public final void startPhoenixBlessing()
	{
		setIsPhoenixBlessed(true);
		updateAbnormalEffect();
	}

	public void startProtectionBlessing()
	{
		setProtectionBlessing(true);
		updateAbnormalEffect();
	}

	public final void stopCharmOfLuck(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.CHARM_OF_LUCK);
		}
		else
		{
			removeEffect(effect);
		}

		setCharmOfLuck(false);
		updateAbnormalEffect();
	}

	public final void stopNoblesseBlessing(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setIsNoblesseBlessed(false);
		updateAbnormalEffect();
	}

	public final void stopPhoenixBlessing(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PHOENIX_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setIsPhoenixBlessed(false);
		updateAbnormalEffect();
	}

	public void stopProtectionBlessing(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PROTECTION_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		setProtectionBlessing(false);
		updateAbnormalEffect();
	}

	public final void updateEffectIcons()
	{
		updateEffectIconsImpl();
	}

	public abstract void updateEffectIconsImpl();
}