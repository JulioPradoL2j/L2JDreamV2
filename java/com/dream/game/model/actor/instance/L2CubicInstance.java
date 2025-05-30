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
package com.dream.game.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.ICubicSkillHandler;
import com.dream.game.handler.ISkillHandler;
import com.dream.game.handler.SkillHandler;
import com.dream.game.manager.DuelManager;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.skills.l2skills.L2SkillDrain;
import com.dream.game.taskmanager.AttackStanceTaskManager;
import com.dream.tools.random.Rnd;

public class L2CubicInstance
{
	private class Action implements Runnable
	{
		private final int _chance;

		public Action(int chance)
		{
			_chance = chance;
		}

		@Override
		public void run()
		{
			try
			{
				if (_owner.isDead() || _owner.isOnline() == 0)
				{
					stopAction();
					_owner.delCubic(_id);
					_owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner))
					if (_owner.getPet() != null)
					{
						if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner.getPet()))
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				boolean UseCubicCure = false;
				L2Skill skill = null;

				if (_id >= SMART_CUBIC_EVATEMPLAR && _id <= SMART_CUBIC_SPECTRALMASTER)
				{
					L2Effect[] effects = _owner.getAllEffects();

					for (L2Effect e : effects)
						if (e.getSkill().isDebuff() && e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515)
						{
							UseCubicCure = true;
							e.exit();
						}
				}

				if (UseCubicCure)
				{
					_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, SKILL_CUBIC_CURE, 1, 0, 0, false));
				}
				else if (Rnd.get(1, 100) < _chance)
				{
					skill = _skills.get(Rnd.get(_skills.size()));
					if (skill != null)
					{
						if (skill.getId() == SKILL_CUBIC_HEAL)
						{
							setCubicTargetForHeal();
						}
						else
						{
							getCubicTarget(skill);
							if (!isInCubicRange(_owner, _target))
							{
								_target = null;
							}
						}
						if (_target != null && !_target.isDead())
						{
							ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
							if (handler instanceof ICubicSkillHandler)
							{
								((ICubicSkillHandler) handler).useCubicSkill(L2CubicInstance.this, skill, _target);
							}
							else if (skill instanceof L2SkillDrain)
							{
								((L2SkillDrain) skill).useCubicSkill(L2CubicInstance.this, _target);
							}
							else
							{
								handler.useSkill(_owner, skill, _target);
							}

							_owner.broadcastPacket(new MagicSkillUse(_owner, _target, skill.getId(), skill.getLevel(), 0, 0, skill.isPositive()));
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.fatal("Action:", e);
			}
		}
	}

	private class Disappear implements Runnable
	{
		Disappear()
		{

		}

		@Override
		public void run()
		{
			stopAction();
			_owner.delCubic(_id);
			_owner.broadcastUserInfo();
		}
	}

	public class Heal implements Runnable
	{
		@Override
		public void run()
		{
			if (_owner.isDead() && _owner.isOnline() == 0)
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			if (Rnd.get(100) > 60)
				return;
			try
			{
				L2Skill skill = null;
				for (L2Skill sk : _skills)
					if (sk.getId() == SKILL_CUBIC_HEAL)
					{
						skill = sk;
						break;
					}

				if (skill != null)
				{
					setCubicTargetForHeal();
					L2Character target = _target;
					if (target != null && !target.isDead())
						if (target.getMaxHp() - target.getStatus().getCurrentHp() > skill.getPower())
						{
							SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(_owner, skill, target);

							_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0, skill.isPositive()));
						}
				}
			}
			catch (Exception e)
			{
				_log.fatal("Run", e);
			}
		}
	}

	protected static Logger _log = Logger.getLogger(L2CubicInstance.class.getName());
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;

	public static final int SMART_CUBIC_ARCANALORD = 12;

	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;

	public static final int MAX_MAGIC_RANGE = 900;
	public static final int SKILL_CUBIC_HEAL = 4051;

	public static final int SKILL_CUBIC_CURE = 5579;
	protected L2PcInstance _owner;
	protected L2Character _target;
	protected int _id;
	protected int _matk;

	protected int _activationtime;

	protected int _activationchance;
	protected boolean _active;

	protected List<L2Skill> _skills = new ArrayList<>();

	private Future<?> _disappearTask;

	private Future<?> _actionTask;

	public L2CubicInstance(L2PcInstance owner, int id, int level, int mAtk, int activationtime, int activationchance, int totallifetime)
	{
		_owner = owner;
		_id = id;
		_matk = mAtk;
		_activationtime = activationtime * 1000;
		_activationchance = activationchance;
		_active = false;

		switch (_id)
		{
			case STORM_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4053, level));
				_skills.add(SkillTable.getInstance().getInfo(4054, level));
				_skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case ATTRACT_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(5115, level));
				_skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				_skills.add(SkillTable.getInstance().getInfo(4051, 7));
				_skills.add(SkillTable.getInstance().getInfo(4165, 9));
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(4166, 9));
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(4052, 6));
				break;
			case SMART_CUBIC_EVATEMPLAR:
				_skills.add(SkillTable.getInstance().getInfo(4053, 8));
				_skills.add(SkillTable.getInstance().getInfo(4165, 9));
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8));
				_skills.add(SkillTable.getInstance().getInfo(5115, 4));
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), totallifetime); // Disappear in 20 mins
	}

	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}

	public void doAction()
	{
		if (_active)
			return;
		_active = true;

		switch (_id)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(_activationchance), 0, _activationtime);
				break;
			case LIFE_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(), 0, _activationtime);
				break;
		}
	}

	public void getCubicTarget(L2Skill skill)
	{
		try
		{
			_target = null;
			L2Object ownerTarget = _owner.getTarget();
			if (ownerTarget == null)
				return;

			if (_owner._event != null && _owner._event.isRunning())
			{
				L2PcInstance target = ownerTarget.getActingPlayer();
				if (target != null && _owner._event.canBeSkillTarget(_owner, target, skill))
				{
					_target = (L2Character) ownerTarget;
				}
				return;
			}
			if (_owner.isInDuel())
			{
				L2PcInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				L2PcInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();

				if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					L2Party partyA = PlayerA.getParty();
					L2Party partyB = PlayerB.getParty();
					L2Party partyEnemy = null;

					if (partyA != null)
					{
						if (partyA.getPartyMembers().contains(_owner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else if (PlayerA == _owner)
					{
						if (partyB != null)
						{
							partyEnemy = partyB;
						}
						else
						{
							_target = PlayerB;
						}
					}
					else
					{
						_target = PlayerA;
					}
					if (_target == PlayerA || _target == PlayerB)
						if (_target == ownerTarget)
							return;
					if (partyEnemy != null)
					{
						if (partyEnemy.getPartyMembers().contains(ownerTarget))
						{
							_target = (L2Character) ownerTarget;
						}
						return;
					}
				}
				if (PlayerA != _owner && ownerTarget == PlayerA)
				{
					_target = PlayerA;
					return;
				}

				if (PlayerB != _owner && ownerTarget == PlayerB)
				{
					_target = PlayerB;
					return;
				}
				_target = null;
				return;
			}
			if (_owner.isInOlympiadMode())
			{
				if (_owner.isOlympiadStart())
				{
					L2PcInstance[] players = Olympiad.getPlayers(_owner.getOlympiadGameId());
					if (players != null)
						if (_owner.getOlympiadSide() == 1)
						{
							if (ownerTarget == players[1])
							{
								_target = players[1];
							}
							else if (players[1].getPet() != null && ownerTarget == players[1].getPet())
							{
								_target = players[1].getPet();
							}
						}
						else if (ownerTarget == players[0])
						{
							_target = players[0];
						}
						else if (players[0].getPet() != null && ownerTarget == players[0].getPet())
						{
							_target = players[0].getPet();
						}
				}
				return;
			}

			if (ownerTarget instanceof L2Character && ownerTarget != _owner.getPet() && ownerTarget != _owner)
			{
				if (ownerTarget instanceof L2Attackable)
				{
					if (((L2Attackable) ownerTarget).getAggroListRP().get(_owner) != null && !((L2Attackable) ownerTarget).isDead())
					{
						_target = (L2Character) ownerTarget;
						return;
					}
					if (_owner.getPet() != null)
						if (((L2Attackable) ownerTarget).getAggroListRP().get(_owner.getPet()) != null && !((L2Attackable) ownerTarget).isDead())
						{
							_target = (L2Character) ownerTarget;
							return;
						}
				}

				L2PcInstance enemy = null;

				if (_owner.getPvpFlag() > 0 && !_owner.isInsideZone(L2Zone.FLAG_PEACE) || _owner.isInsideZone(L2Zone.FLAG_PVP))
				{
					if (ownerTarget instanceof L2Character && !((L2Character) ownerTarget).isDead())
					{
						enemy = ownerTarget.getActingPlayer();
					}

					if (enemy != null)
					{
						boolean targetIt = true;
						if (_owner.getParty() != null)
							if (_owner.getParty().getPartyMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if (_owner.getParty().getCommandChannel() != null)
								if (_owner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
						if (_owner.getClan() != null && !_owner.isInsideZone(L2Zone.FLAG_PVP))
						{
							if (_owner.getClan().isMember(enemy.getObjectId()))
							{
								targetIt = false;
							}
							if (_owner.getAllyId() > 0 && enemy.getAllyId() > 0)
								if (_owner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
						}
						if (enemy.getPvpFlag() == 0 && !enemy.isInsideZone(L2Zone.FLAG_PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(L2Zone.FLAG_PEACE))
						{
							targetIt = false;
						}
						if (_owner.getSiegeState() > 0 && _owner.getSiegeState() == enemy.getSiegeState())
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}

						if (targetIt)
						{
							_target = enemy;
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("GetCubicTarget:", e);
		}
	}

	public int getId()
	{
		return _id;
	}

	public int getMAtk()
	{
		return _matk;
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return _owner.getTemplate().getBaseMCritRate();
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public boolean isInCubicRange(L2Character owner, L2Character target)
	{
		if (owner == null || target == null)
			return false;

		int x, y, z;
		int range = MAX_MAGIC_RANGE;

		x = owner.getX() - target.getX();
		y = owner.getY() - target.getY();
		z = owner.getZ() - target.getZ();

		return x * x + y * y + z * z <= range * range;
	}

	public void setCubicTargetForHeal()
	{
		L2Character target = null;
		double percentleft = 100.0;
		L2Party party = _owner.getParty();

		if (_owner.isInDuel())
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}

		if (party != null && !_owner.isInOlympiadMode())
		{
			List<L2PcInstance> partyList = party.getPartyMembers();
			for (L2Character partyMember : partyList)
			{
				if (!partyMember.isDead())
					if (isInCubicRange(_owner, partyMember))
						if (partyMember.getStatus().getCurrentHp() < partyMember.getMaxHp())
							if (percentleft > partyMember.getStatus().getCurrentHp() / partyMember.getMaxHp())
							{
								percentleft = partyMember.getStatus().getCurrentHp() / partyMember.getMaxHp();
								target = partyMember;
							}
				if (partyMember.getPet() != null)
				{
					if (partyMember.getPet().isDead())
					{
						continue;
					}

					if (!isInCubicRange(_owner, partyMember.getPet()))
					{
						continue;
					}

					if (partyMember.getPet().getStatus().getCurrentHp() < partyMember.getPet().getMaxHp())
						if (percentleft > partyMember.getPet().getStatus().getCurrentHp() / partyMember.getPet().getMaxHp())
						{
							target = partyMember.getPet();
						}
				}
			}
		}
		else
		{
			if (_owner.getStatus().getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = _owner.getStatus().getCurrentHp() / _owner.getMaxHp();
				target = _owner;
			}
			if (_owner.getPet() != null)
				if (!_owner.getPet().isDead() && _owner.getPet().getStatus().getCurrentHp() < _owner.getPet().getMaxHp() && percentleft > _owner.getPet().getStatus().getCurrentHp() / _owner.getPet().getMaxHp() && isInCubicRange(_owner, _owner.getPet()))
				{
					target = _owner.getPet();
					percentleft = _owner.getPet().getStatus().getCurrentHp() / _owner.getPet().getMaxHp();
				}
		}
		_target = target;
	}

	public void stopAction()
	{
		_target = null;
		_active = false;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
	}
}