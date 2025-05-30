package com.dream.game.model;

import java.util.List;

import com.dream.Config;
import com.dream.game.datatables.xml.AugmentationData;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.SkillCoolTime;
import com.dream.game.skills.Stats;
import com.dream.game.skills.funcs.FuncAdd;
import com.dream.game.skills.funcs.FuncOwner;

public final class L2Augmentation
{
	public final class AugmentationStatBoni implements FuncOwner
	{
		private final Stats _stats[];
		private final float _values[];
		private boolean _active;

		public AugmentationStatBoni(int augmentationId)
		{
			_active = false;
			List<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);

			_stats = new Stats[as.size()];
			_values = new float[as.size()];

			int i = 0;
			for (AugmentationData.AugStat aStat : as)
			{
				_stats[i] = aStat.getStat();
				_values[i] = aStat.getValue();
				i++;
			}
		}

		public void applyBonus(L2PcInstance player)
		{
			if (_active)
				return;

			for (int i = 0; i < _stats.length; i++)
			{
				player.addStatFunc(new FuncAdd(_stats[i], 0x40, this, _values[i], null));
			}

			_active = true;
		}

		@Override
		public String getFuncOwnerName()
		{
			return null;
		}

		@Override
		public L2Skill getFuncOwnerSkill()
		{
			return null;
		}

		public void removeBonus(L2PcInstance player)
		{
			if (!_active)
				return;

			player.removeStatsOwner(this);

			_active = false;
		}
	}

	private int _effectsId = 0;
	private AugmentationStatBoni _boni = null;

	private L2Skill _skill = null;

	public L2Augmentation(int effects, int skill, int skillLevel)
	{
		this(effects, SkillTable.getInstance().getInfo(skill, skillLevel));
	}

	public L2Augmentation(int effects, L2Skill skill)
	{
		_effectsId = effects;
		_boni = new AugmentationStatBoni(_effectsId);
		_skill = skill;
	}

	public void applyBonus(L2PcInstance player)
	{
		_boni.applyBonus(player);

		boolean updateTimeStamp = false;
		if (_skill != null)
		{
			player.addSkill(_skill);
			if (_skill.isActive())
				if (!player.getReuseTimeStamps().containsKey(_skill.getId()))
				{
					int equipDelay = _skill.getEquipDelay();
					if (equipDelay > 0)
					{
						player.addTimeStamp(_skill.getId(), equipDelay);
						player.disableSkill(_skill.getId(), equipDelay);
						updateTimeStamp = true;
					}
				}
			player.sendSkillList();
			if (updateTimeStamp)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
		}
	}

	public int getAttributes()
	{
		return _effectsId;
	}

	public int getAugmentationId()
	{
		return _effectsId;
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);
		if (_skill != null)
		{
			if (_skill.isPassive())
			{
				player.removeSkill(_skill, false, true);
			}
			else if (!Config.CANCEL_AUGUMENTATION_EFFECT)
			{
				player.removeSkill(_skill, false, false);
			}
			else
			{
				player.removeSkill(_skill, false, true);
			}
			player.sendSkillList();
		}
	}
}