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
package com.dream.game.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.dream.game.ai.CtrlEvent;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2EffectPointInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.MagicSkillLaunched;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.l2skills.L2SkillSignetCasttime;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.tools.geometry.Point3D;

public final class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;

	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}

	@Override
	public boolean onActionTime()
	{
		if (getCount() >= getTotalCount() - 2)
			return true;
		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		boolean ss = false;
		boolean bss = false;

		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			switch (weaponInst.getChargedSpiritshot())
			{
				case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					bss = true;
					break;
				case L2ItemInstance.CHARGED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ss = true;
					break;
			}
		}

		List<L2Character> targets = new ArrayList<>();

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster)
			{
				continue;
			}

			if (cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if (cha.isAlikeDead())
				{
					continue;
				}

				if (mpConsume > caster.getStatus().getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}

				caster.reduceCurrentMp(mpConsume);

				if (cha instanceof L2Playable)
					if (!(cha instanceof L2Summon && ((L2Summon) cha).getOwner() == caster))
					{
						caster.updatePvPStatus(cha);
					}

				targets.add(cha);
			}
		}

		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), getSkill().isPositive(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), ss, bss, mcrit);

				target.broadcastStatusUpdate();

				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}

	@Override
	public boolean onStart()
	{
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
		{
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		}
		else
			return false;

		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		L2World.getInstance().storeObject(effectPoint);

		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();

		if (getEffector() instanceof L2PcInstance && getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance) getEffector()).getCurrentSkillWorldPosition();

			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);

		_actor = effectPoint;

		return true;
	}
}
