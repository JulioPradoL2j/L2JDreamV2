package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ACTIVE;
import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.IDLE;

import com.dream.game.GameTimeController;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeGuardInstance;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class L2SiegeGuardAI extends L2CharacterAI implements Runnable
{
	private static final class SiegeGuardAiTaskManager extends AbstractIterativePeriodicTaskManager<L2SiegeGuardAI>
	{
		private static final SiegeGuardAiTaskManager _instance = new SiegeGuardAiTaskManager();

		public static SiegeGuardAiTaskManager getInstance()
		{
			return _instance;
		}

		private SiegeGuardAiTaskManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(L2SiegeGuardAI task)
		{
			task.run();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "run()";
		}
	}

	private static final int MAX_ATTACK_TIMEOUT = 300;
	private final SelfAnalysis _selfAnalysis = new SelfAnalysis();
	private int _attackTimeout;
	private int _globalAggro;
	private volatile boolean _thinking;
	private final int _attackRange;

	public L2SiegeGuardAI(L2Character.AIAccessor accessor)
	{
		super(accessor);

		_selfAnalysis.init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10;

		_attackRange = _actor.getPhysicalAttackRange() < 100 ? 100 : _actor.getPhysicalAttackRange();
	}

	private void attackPrepare()
	{
		L2Skill[] skills = null;
		double dist_2 = 0;
		int range = 0;
		L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance) _actor;
		L2Character attackTarget = getAttackTarget();

		if (attackTarget != null)
		{
			_actor.setTarget(attackTarget);
			skills = _actor.getAllSkills();
			dist_2 = _actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY());
			range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + attackTarget.getTemplate().getCollisionRadius();
			if (attackTarget.isMoving())
			{
				range += 50;
			}
		}
		else
		{
			_actor.setTarget(null);
			setIntention(IDLE, null, null);
			return;
		}

		if (attackTarget instanceof L2PcInstance && sGuard.getCastle().getSiege().checkIsDefender(((L2PcInstance) attackTarget).getClan()))
		{
			sGuard.stopHating(attackTarget);
			_actor.setTarget(null);
			setIntention(IDLE, null, null);
			return;
		}

		if (!GeoData.getInstance().canSeeTarget(_actor, attackTarget))
		{
			sGuard.stopHating(attackTarget);
			_actor.setTarget(null);
			setIntention(IDLE, null, null);
			return;
		}

		if (!_actor.isMuted() && dist_2 > range * range)
		{
			for (L2Skill sk : skills)
			{
				int castRange = sk.getCastRange();

				if (dist_2 <= castRange * castRange && castRange > 70 && !_actor.isSkillDisabled(sk.getId()) && _actor.getStatus().getCurrentMp() >= _actor.getStat().getMpConsume(sk) && !sk.isPassive())
				{

					L2Object OldTarget = _actor.getTarget();
					if (sk.getSkillType() == L2SkillType.BUFF || sk.getSkillType() == L2SkillType.HEAL)
					{
						boolean useSkillSelf = true;
						if (sk.getSkillType() == L2SkillType.HEAL && _actor.getStatus().getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
						{
							useSkillSelf = false;
							break;
						}
						if (sk.getSkillType() == L2SkillType.BUFF)
						{
							L2Effect[] effects = _actor.getAllEffects();
							for (int i = 0; effects != null && i < effects.length; i++)
							{
								L2Effect effect = effects[i];
								if (effect.getSkill() == sk)
								{
									useSkillSelf = false;
									break;
								}
							}
						}
						if (useSkillSelf)
						{
							_actor.setTarget(_actor);
						}
					}

					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					return;
				}
			}

			if (!_actor.isAttackingNow() && _actor.getRunSpeed() == 0 && _actor.getKnownList().knowsObject(attackTarget))
			{
				_actor.getKnownList().removeKnownObject(attackTarget);
				_actor.setTarget(null);
				setIntention(IDLE, null, null);
			}
			else
			{
				double dx = _actor.getX() - attackTarget.getX();
				double dy = _actor.getY() - attackTarget.getY();
				double dz = _actor.getZ() - attackTarget.getZ();
				double homeX = attackTarget.getX() - sGuard.getSpawn().getLocx();
				double homeY = attackTarget.getY() - sGuard.getSpawn().getLocy();

				if (dx * dx + dy * dy > 10000 && homeX * homeX + homeY * homeY > 3240000 && _actor.getKnownList().knowsObject(attackTarget))
				{
					_actor.getKnownList().removeKnownObject(attackTarget);
					_actor.setTarget(null);
					setIntention(IDLE, null, null);
				}
				else if (dz * dz < 28900)
				{
					if (_selfAnalysis.isHealer)
						return;
					if (_selfAnalysis.isMage)
					{
						range = _selfAnalysis.maxCastRange - 50;
					}
					if (_attackTarget.isMoving())
					{
						moveToPawn(attackTarget, range - 70);
					}
					else
					{
						moveToPawn(attackTarget, range);
					}
				}
			}
		}
		else if (_actor.isMuted() && dist_2 > range * range && !_selfAnalysis.isHealer)
		{
			double dz = _actor.getZ() - attackTarget.getZ();
			if (dz * dz < 170 * 170)
			{
				if (_selfAnalysis.isMage)
				{
					range = _selfAnalysis.maxCastRange - 50;
				}
				if (_attackTarget.isMoving())
				{
					moveToPawn(attackTarget, range - 70);
				}
				else
				{
					moveToPawn(attackTarget, range);
				}
			}
		}
		else if (dist_2 <= range * range)
		{
			L2Character hated = null;
			if (_actor.isConfused())
			{
				hated = attackTarget;
			}
			else
			{
				hated = ((L2Attackable) _actor).getMostHated();
			}

			if (hated == null)
			{
				setIntention(ACTIVE, null, null);
				return;
			}
			if (hated != attackTarget)
			{
				attackTarget = hated;
			}

			_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

			if (!_actor.isMuted() && Rnd.nextInt(100) <= 5)
			{
				for (L2Skill sk : skills)
				{
					int castRange = sk.getCastRange();

					if (castRange * castRange >= dist_2 && !sk.isPassive() && _actor.getStatus().getCurrentMp() >= _actor.getStat().getMpConsume(sk) && !_actor.isSkillDisabled(sk.getId()))
					{
						L2Object OldTarget = _actor.getTarget();
						if (sk.getSkillType() == L2SkillType.BUFF || sk.getSkillType() == L2SkillType.HEAL)
						{
							boolean useSkillSelf = true;
							if (sk.getSkillType() == L2SkillType.HEAL && _actor.getStatus().getCurrentHp() > (int) (_actor.getMaxHp() / 1.5))
							{
								useSkillSelf = false;
								break;
							}
							if (sk.getSkillType() == L2SkillType.BUFF)
							{
								L2Effect[] effects = _actor.getAllEffects();
								for (int i = 0; effects != null && i < effects.length; i++)
								{
									L2Effect effect = effects[i];
									if (effect.getSkill() == sk)
									{
										useSkillSelf = false;
										break;
									}
								}
							}
							if (useSkillSelf)
							{
								_actor.setTarget(_actor);
							}
						}

						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
			if (!_selfAnalysis.isHealer)
			{
				_accessor.doAttack(attackTarget);
			}
		}
	}

	private boolean autoAttackCondition(L2Character target)
	{
		if (target == null || target instanceof L2SiegeGuardInstance || target instanceof L2NpcInstance || target instanceof L2DoorInstance)
			return false;

		if (target.isInvul())
		{
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isGM())
				return false;
			if (target instanceof L2Summon && ((L2Summon) target).getOwner().isGM())
				return false;
		}

		if (target.isAlikeDead())
			return false;

		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (_actor.isInsideRadius(owner, 1000, true, false))
			{
				target = owner;
			}
		}

		if (target instanceof L2Playable)
			if (((L2Playable) target).isSilentMoving() && !_actor.isInsideRadius(target, 250, false, false))
				return false;
		return _actor.isAutoAttackable(target) && GeoData.getInstance().canSeeTarget(_actor, target);
	}

	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == IDLE)
		{
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) _actor;

				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = ACTIVE;
				}
				else
				{
					intention = IDLE;
				}
			}

			if (intention == IDLE)
			{
				super.changeIntention(IDLE, null, null);

				stopAITask();

				return;
			}
		}

		super.changeIntention(intention, arg0, arg1);

		SiegeGuardAiTaskManager.getInstance().startTask(this);
	}

	private final void factionNotifyAndSupport()
	{
		final L2Character target = getAttackTarget();

		if (((L2Npc) _actor).getFactionId() == null || target == null || target.isInvul())
			return;

		final String faction_id = ((L2Npc) _actor).getFactionId();

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(1000))
		{
			if (cha == null)
			{
				continue;
			}

			if (!(cha instanceof L2Npc))
			{
				if (_selfAnalysis.hasHealOrResurrect && cha instanceof L2PcInstance && ((L2Npc) _actor).getCastle().getSiege().checkIsDefender(((L2PcInstance) cha).getClan()))
					if (!_actor.isAttackingDisabled() && cha.getStatus().getCurrentHp() < cha.getMaxHp() * 0.6 && _actor.getStatus().getCurrentHp() > _actor.getMaxHp() / 2 && _actor.getStatus().getCurrentMp() > _actor.getMaxMp() / 2 && cha.isInCombat())
					{
						for (L2Skill sk : _selfAnalysis.healSkills)
						{
							if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
							{
								continue;
							}
							if (_actor.isSkillDisabled(sk.getId()))
							{
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange(), _actor, cha, true))
							{
								continue;
							}
							if (5 >= Rnd.get(100))
							{
								continue;
							}
							if (!GeoData.getInstance().canSeeTarget(_actor, cha))
							{
								break;
							}

							L2Object OldTarget = _actor.getTarget();
							_actor.setTarget(cha);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(OldTarget);
							return;
						}
					}
				continue;
			}

			final L2Npc npc = (L2Npc) cha;

			if (!faction_id.equals(npc.getFactionId()))
			{
				continue;
			}

			if (npc.getAI() != null)
			{
				if (!npc.isDead() && Math.abs(target.getZ() - npc.getZ()) < 600 && (npc.getAI()._intention == CtrlIntention.IDLE || npc.getAI()._intention == CtrlIntention.ACTIVE) && target.isInsideRadius(npc, 1500, true, false) && GeoData.getInstance().canSeeTarget(npc, target))
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					return;
				}
				if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && npc.getStatus().getCurrentHp() < npc.getMaxHp() * 0.6 && _actor.getStatus().getCurrentHp() > _actor.getMaxHp() / 2 && _actor.getStatus().getCurrentMp() > _actor.getMaxMp() / 2 && npc.isInCombat())
				{
					for (L2Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (_actor.isSkillDisabled(sk.getId()))
						{
							continue;
						}
						if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
						{
							continue;
						}
						if (4 >= Rnd.get(100))
						{
							continue;
						}
						if (!GeoData.getInstance().canSeeTarget(_actor, npc))
						{
							break;
						}
						L2Object OldTarget = _actor.getTarget();
						clientStopMoving(null);
						_actor.setTarget(npc);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
		}
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		if (_actor == null)
			return;
		L2Attackable me = (L2Attackable) _actor;

		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);

			aggro = me.getHating(target);

			if (aggro <= 0)
			{
				if (me.getMostHated() == null)
				{
					_globalAggro = -25;
					me.clearAggroList();
					setIntention(IDLE, null, null);
				}
				return;
			}

			if (getIntention() != CtrlIntention.ATTACK)
			{
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}

				L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance) _actor;
				double homeX = target.getX() - sGuard.getSpawn().getLocx();
				double homeY = target.getY() - sGuard.getSpawn().getLocy();

				if (homeX * homeX + homeY * homeY < 3240000)
				{
					setIntention(CtrlIntention.ATTACK, target, null);
				}
			}
		}
		else
		{
			if (aggro >= 0)
				return;

			L2Character mostHated = me.getMostHated();
			if (mostHated == null)
			{
				_globalAggro = -25;
				return;
			}
			for (L2Character aggroed : me.getAggroListRP().keySet())
			{
				me.addDamageHate(aggroed, 0, aggro);
			}

			aggro = me.getHating(mostHated);
			if (aggro <= 0)
			{
				_globalAggro = -25;
				me.clearAggroList();
				setIntention(IDLE, null, null);
			}
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}

		((L2Attackable) _actor).addDamageHate(attacker, 0, 1);

		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}

		if (getIntention() != ATTACK)
		{
			setIntention(CtrlIntention.ATTACK, attacker, null);
		}

		super.onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
			return;

		_thinking = true;

		try
		{
			if (getIntention() == ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		super.onIntentionAttack(target);
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	@Override
	public void stopAITask()
	{
		SiegeGuardAiTaskManager.getInstance().stopTask(this);
		_accessor.detachAI();
	}

	private void thinkActive()
	{
		L2Attackable npc = (L2Attackable) _actor;

		if (_globalAggro != 0)
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}

		if (_globalAggro >= 0)
		{
			Iterable<L2Character> chars = npc.getKnownList().getKnownCharactersInRadius(_attackRange);
			if (chars != null)
			{
				for (L2Character target : chars)
				{
					if (target == null)
					{
						continue;
					}
					if (autoAttackCondition(target))
					{
						int hating = npc.getHating(target);

						if (hating == 0)
						{
							npc.addDamageHate(target, 0, 1);
						}
					}
				}
			}

			L2Character hated;
			if (_actor.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getMostHated();
			}

			if (hated != null)
			{
				int aggro = npc.getHating(hated);

				if (aggro + _globalAggro > 0)
				{
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}

					setIntention(CtrlIntention.ATTACK, hated, null);
				}

				return;
			}

		}
		((L2SiegeGuardInstance) _actor).returnHome();
	}

	private void thinkAttack()
	{
		if (_attackTimeout < GameTimeController.getGameTicks())
			if (_actor.isRunning())
			{
				_actor.setWalking();

				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}

		L2Character attackTarget = getAttackTarget();
		if (attackTarget == null || _attackTarget.isAlikeDead() || _attackTimeout < GameTimeController.getGameTicks())
		{
			if (attackTarget != null)
			{
				L2Attackable npc = (L2Attackable) _actor;
				npc.stopHating(attackTarget);
			}

			_attackTimeout = Integer.MAX_VALUE;
			setAttackTarget(null);

			setIntention(ACTIVE, null, null);

			_actor.setWalking();
			return;
		}

		factionNotifyAndSupport();
		attackPrepare();
	}
}