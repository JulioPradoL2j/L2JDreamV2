package com.dream.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.dream.Config;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.effects.EffectCharmOfCourage;
import com.dream.game.skills.effects.EffectTemplate;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.util.LinkedBunch;

public class CharEffectList
{
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	private List<L2Effect> _buffs;
	private List<L2Effect> _debuffs;
	protected Map<String, List<L2Effect>> _stackedEffects;
	private final L2Character _owner;

	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}

	public void addEffect(L2Effect newEffect)
	{
		if (newEffect == null)
			return;

		synchronized (this)
		{
			if (_buffs == null)
			{
				_buffs = new CopyOnWriteArrayList<>();
			}
			if (_debuffs == null)
			{
				_debuffs = new CopyOnWriteArrayList<>();
			}
			if (_stackedEffects == null)
			{
				_stackedEffects = new HashMap<>();
			}
		}

		List<L2Effect> effectList = newEffect.getSkill().isDebuff() ? _debuffs : _buffs;
		L2Effect tempEffect = null;
		boolean stopNewEffect = false;

		synchronized (effectList)
		{
			// Check for same effects
			for (L2Effect e : effectList)
				if (e != null && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getStackOrder() == newEffect.getStackOrder())
				{
					if (!newEffect.getSkill().isDebuff())
					{
						tempEffect = e; // exit this
						break;
					}

					// Started scheduled timer needs to be canceled.
					stopNewEffect = true;
					break;
				}
		}

		if (tempEffect != null)
		{
			synchronized (this)
			{
				L2Skill skill = newEffect.getSkill();
				if (skill != null)
					if (skill.bestowed() || skill.bestowTriggered() || skill.isChance())
					{
						newEffect.stopEffectTask();
						return;
					}
				tempEffect.exit(newEffect.getEffector());
			}
		}

		if ((newEffect.getEffectType() == L2EffectType.NOBLESSE_BLESSING) && _owner.isInFunEvent())
		{
			newEffect.stopEffectTask();
		}

		// if max buffs, no herb effects are used, even if they would replace
		// one old
		if (stopNewEffect || getBuffCount() >= _owner.getMaxBuffCount() && newEffect.isHerbEffect())
		{
			newEffect.stopEffectTask();
			return;
		}

		// Remove first buff when buff list is full
		L2Skill tempSkill = newEffect.getSkill();
		if (!_stackedEffects.containsKey(newEffect.getStackType()) && !tempSkill.isDebuff() && !tempSkill.bestowed() && !(tempSkill.getId() > 4360 && tempSkill.getId() < 4367))
		{
			removeFirstBuff(tempSkill);
		}

		synchronized (effectList)
		{
			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isToggle() && !newEffect.getSkill().isDebuff())
			{
				int pos = 0;
				for (L2Effect e : effectList)
					if (e != null)
					{
						int skillid = e.getSkill().getId();
						if (!e.getSkill().isToggle() && !(skillid > 4360 && skillid < 4367))
						{
							pos++;
						}
					}
					else
					{
						break;
					}
				effectList.add(pos, newEffect);
			}
			else
			{
				effectList.add(newEffect);
			}
		}

		List<L2Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());
		if (stackQueue == null)
		{
			stackQueue = new ArrayList<>();
		}

		L2Effect[] allEffects = getAllEffects();

		tempEffect = null;
		if (!stackQueue.isEmpty())
		{
			for (L2Effect e : allEffects)
				if (e == stackQueue.get(0))
				{
					tempEffect = e;
					break;
				}
		}

		stackQueue = effectQueueInsert(newEffect, stackQueue);

		if (stackQueue == null)
			return;

		_stackedEffects.put(newEffect.getStackType(), stackQueue);

		L2Effect tempEffect2 = null;
		for (L2Effect e : allEffects)
			if (e == stackQueue.get(0))
			{
				tempEffect2 = e;
				break;
			}

		if (tempEffect != tempEffect2)
		{
			if (tempEffect != null)
			{
				_owner.removeStatsOwner(tempEffect);

				tempEffect.setInUse(false);
			}
			if (tempEffect2 != null)
			{
				tempEffect2.setInUse(true);

				_owner.addStatFuncs(tempEffect2.getStatFuncs());
			}
		}
	}

	private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
	{
		List<L2Effect> effectList = newStackedEffect.getSkill().isDebuff() ? _debuffs : _buffs;

		if (_buffs == null && _debuffs == null)
			return null;

		Iterator<L2Effect> queueIterator = stackQueue.iterator();

		int i = 0;
		while (queueIterator.hasNext())
		{
			L2Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
			{
				i++;
			}
			else
			{
				break;
			}
		}

		stackQueue.add(i, newStackedEffect);

		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && stackQueue.size() > 1)
		{
			synchronized (effectList)
			{
				for (L2Effect e : effectList)
					if (e == stackQueue.get(1))
					{
						effectList.remove(e);
						break;
					}
			}
			stackQueue.remove(1);
		}
		return stackQueue;
	}

	public final L2Effect[] getAllEffects()
	{
		if ((_buffs == null || _buffs.isEmpty()) && (_debuffs == null || _debuffs.isEmpty()))
			return EMPTY_EFFECTS;
		LinkedBunch<L2Effect> temp = new LinkedBunch<>();

		for (L2Effect eff : _buffs)
		{
			try
			{
				if (eff != null)
				{
					temp.add(eff);
				}
			}
			catch (NullPointerException npe)
			{
				continue;
			}
		}

		for (L2Effect eff : _debuffs)
		{
			try
			{
				if (eff != null)
				{
					temp.add(eff);
				}
			}
			catch (NullPointerException npe)
			{
				continue;
			}
		}
		return temp.moveToArray(new L2Effect[temp.size()]);
	}

	public int getBuffCount()
	{
		if (_buffs == null)
			return 0;
		int buffCount = 0;
		synchronized (_buffs)
		{
			for (L2Effect e : _buffs)
				if (e.isBuff())
				{
					buffCount++;
				}
		}
		return buffCount;
	}

	public int getDanceCount(boolean dances, boolean songs)
	{
		if (_buffs == null)
			return 0;
		int danceCount = 0;
		synchronized (_buffs)
		{
			for (L2Effect e : _buffs)
				if (e != null && (e.getSkill().isDance() && dances || e.getSkill().isSong() && songs) && e.getInUse())
				{
					danceCount++;
				}
		}
		return danceCount;
	}

	public final L2Effect getFirstEffect(int skillId)
	{
		L2Effect[] effects = getAllEffects();

		L2Effect eventNotInUse = null;
		for (L2Effect e : effects)
			if (e.getSkill().getId() == skillId)
			{
				if (e.getInUse())
					return e;
				eventNotInUse = e;
			}
		return eventNotInUse;
	}

	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		L2Effect[] effects = getAllEffects();

		L2Effect eventNotInUse = null;
		for (L2Effect e : effects)
			if (e.getEffectType() == tp)
			{
				if (e.getInUse())
					return e;
				eventNotInUse = e;
			}
		return eventNotInUse;
	}

	public final L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect[] effects = getAllEffects();

		L2Effect eventNotInUse = null;
		for (L2Effect e : effects)
			if (e.getSkill() == skill)
			{
				if (e.getInUse())
					return e;
				eventNotInUse = e;
			}
		return eventNotInUse;
	}

	public boolean isPossible(EffectTemplate newEffect, L2Skill skill)
	{

		if (_stackedEffects == null)
			return true;
		synchronized (_stackedEffects)
		{
			if (!_stackedEffects.containsKey(newEffect.stackType))
				return true;
			List<L2Effect> eff = _stackedEffects.get(newEffect.stackType);
			if (eff == null || eff.isEmpty())
				return true;
			return eff.get(0).getSkill().getEffectLevel() <= skill.getEffectLevel();
		}

	}

	public final void removeEffect(L2Effect effect)
	{
		if (effect == null || _buffs == null && _debuffs == null)
			return;

		List<L2Effect> effectList = effect.getSkill().isDebuff() ? _debuffs : _buffs;

		synchronized (effectList)
		{
			if (_stackedEffects == null)
				return;

			// Get the list of all stacked effects corresponding to the stack
			// type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(effect.getStackType());

			if (stackQueue == null || stackQueue.size() < 1)
				return;

			// Get the identifier of the first stacked effect of the stack group
			// selected
			L2Effect frontEffect = stackQueue.get(0);

			// Remove the effect from the stack group
			boolean removed = stackQueue.remove(effect);

			if (removed)
			{
				// Check if the first stacked effect was the effect to remove
				if (frontEffect == effect)
				{
					// Remove all its Func objects from the L2Character
					// calculator set
					_owner.removeStatsOwner(effect);

					// Check if there's another effect in the Stack Group
					if (!stackQueue.isEmpty())
					{
						// Add its list of Funcs to the Calculator set of the
						// L2Character
						for (L2Effect e : effectList)
							if (e == stackQueue.get(0))
							{
								// Add its list of Funcs to the Calculator set
								// of the L2Character
								_owner.addStatFuncs(e.getStatFuncs());
								// Set the effect to In Use
								e.setInUse(true);
								break;
							}
					}
				}
				if (stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getStackType());
				}
				else
				{
					_stackedEffects.put(effect.getStackType(), stackQueue);
				}
			}

			for (L2Effect e : effectList)
				if (e == effect)
				{
					effectList.remove(e);
					if (_owner instanceof L2PcInstance && effect.getShowIcon())
						if (effect.getSkill().isToggle())
						{
							_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED).addSkillName(effect));
						}
						else
						{
							_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(effect));
						}
					break;
				}
		}
	}

	private void removeFirstBuff(L2Skill checkSkill)
	{
		if (getBuffCount() >= _owner.getMaxBuffCount())
		{
			if (checkSkill.getSkillType() != L2SkillType.BUFF && checkSkill.getSkillType() != L2SkillType.REFLECT && checkSkill.getSkillType() != L2SkillType.HEAL_PERCENT && checkSkill.getSkillType() != L2SkillType.MANAHEAL_PERCENT)
				return;
		}
		else
			return;

		L2Effect[] effects = getAllEffects();
		L2Effect removeMe = null;

		for (L2Effect e : effects)
		{
			if (e == null)
			{
				continue;
			}

			if (e.getSkill().bestowed())
			{
				continue;
			}

			switch (e.getSkill().getSkillType())
			{
				case BUFF:
				case DEBUFF:
				case REFLECT:
				case HEAL_PERCENT:
				case MANAHEAL_PERCENT:
					break;
				default:
					continue;
			}

			// don't remove charm of courage
			if (e.getEffectType() == L2EffectType.CHARMOFCOURAGE)
			{
				continue;
			}

			if (e.getSkill().getId() == checkSkill.getId())
			{
				removeMe = e;
				break;
			}
			else if (removeMe == null)
			{
				removeMe = e;
			}
		}
		if (removeMe != null)
		{
			removeMe.exit(removeMe.getEffector());
		}
	}

	public final void stopAllEffects()
	{
		L2Effect[] effects = getAllEffects();

		for (L2Effect e : effects)
			if (e != null && e.getSkill().getId() != 5660)
			{
				e.exit(null);
			}
	}

	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();

		// Exit them
		for (L2Effect e : effects)
			if (e != null)
			{
				if (e instanceof EffectCharmOfCourage)
				{
					continue;
				}
				if (Config.LEAVE_BUFFS_ONDIE)
				{
				e.exit(e.getEffector());
				}
			}
	}

	public final void stopEffects(L2EffectType type)
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();

		// Go through all active skills effects
		for (L2Effect e : effects)
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type)
			{
				e.exit(e.getEffector());
			}
	}

	public final void stopSkillEffects(int skillId)
	{
		// Get all skills effects on the L2Character
		L2Effect[] effects = getAllEffects();

		for (L2Effect e : effects)
			if (e.getSkill().getId() == skillId)
			{
				e.exit(e.getEffector());
			}
	}
}