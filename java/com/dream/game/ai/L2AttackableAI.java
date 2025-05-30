package com.dream.game.ai;

import static com.dream.game.ai.CtrlIntention.ACTIVE;
import static com.dream.game.ai.CtrlIntention.ATTACK;
import static com.dream.game.ai.CtrlIntention.IDLE;
import static com.dream.game.ai.CtrlIntention.INTERACT;

import java.util.Collection;
import java.util.List;

import com.dream.Config;
import com.dream.game.GameTimeController;
import com.dream.game.geodata.GeoData;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.DimensionalRiftManager.DimensionalRiftRoom;
import com.dream.game.model.L2Boss;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2FestivalMonsterInstance;
import com.dream.game.model.actor.instance.L2FriendlyMobInstance;
import com.dream.game.model.actor.instance.L2GuardInstance;
import com.dream.game.model.actor.instance.L2MinionInstance;
import com.dream.game.model.actor.instance.L2MonsterInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RiftInvaderInstance;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.taskmanager.AbstractIterativePeriodicTaskManager;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	private static final class AttackableAiTaskManager extends AbstractIterativePeriodicTaskManager<L2AttackableAI>
	{
		private static final AttackableAiTaskManager _instance = new AttackableAiTaskManager();

		public static AttackableAiTaskManager getInstance()
		{
			return _instance;
		}

		private AttackableAiTaskManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(L2AttackableAI task)
		{
			task.run();
		}

		@Override
		protected String getCalledMethodName()
		{
			return "run()";
		}
	}

	private static final int RANDOM_WALK_RATE = 30;
	private static final int MAX_ATTACK_TIMEOUT = 300;

	private final SelfAnalysis _selfAnalysis = new SelfAnalysis();
	private final TargetAnalysis _mostHatedAnalysis = new TargetAnalysis();
	private final TargetAnalysis _secondMostHatedAnalysis = new TargetAnalysis();

	private int _attackTimeout;
	private int _globalAggro;
	private volatile boolean _thinking;
	private L2MonsterInstance leader;

	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);

		_selfAnalysis.init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10;
	}

	protected boolean autoAttackCondition(L2Character target)
	{
		if (target == null || !(_actor instanceof L2Attackable))
			return false;

		if (target instanceof L2NpcInstance || target instanceof L2DoorInstance)
			return false;

		L2Attackable me = (L2Attackable) _actor;

		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || Math.abs(_actor.getZ() - target.getZ()) > 300)
			return false;

		if (_selfAnalysis.cannotMoveOnLand && !target.isInWater())
			return false;

		if (target instanceof L2Playable)
		{
			L2PcInstance player = target.getActingPlayer();
			if (Config.ALT_MOB_NOAGRO > 0 && player != null)
				if (player.getLevel() >= me.getLevel() + Config.ALT_MOB_NOAGRO)
					return false;

			if (!(me instanceof L2Boss) && ((L2Playable) target).isSilentMoving() && me.getFactionId() != "exfact")
				return false;
		}

		if (target instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) target;

			if (player.isGM() && player.isInvul())
				return false;

			if (me.getFactionId() == "varka" && player.isAlliedWithVarka())
				return false;
			else if (me.getFactionId() == "ketra" && player.isAlliedWithKetra())
				return false;

			if (player.isInFunEvent())
				return false;

			if (player.isRecentFakeDeath())
				return false;
		}

		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();

			if (owner != null)
			{
				if (owner.isGM() && owner.isInvul())
					return false;

				if (me.getFactionId() == "varka" && owner.isAlliedWithVarka())
					return false;
				if (me.getFactionId() == "ketra" && owner.isAlliedWithKetra())
					return false;
			}
		}

		if (me instanceof L2GuardInstance)
		{
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return GeoData.getInstance().canSeeTarget(me, target);

			if (target instanceof L2MonsterInstance)
				return ((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target);

			return false;
		}

		else if (me instanceof L2FriendlyMobInstance)
		{
			if (target instanceof L2Npc)
				return false;

			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return GeoData.getInstance().canSeeTarget(me, target);
			return false;
		}
		else
		{
			if (target instanceof L2Npc)
				return false;

			if (!Config.ALT_MOB_AGGRO_IN_PEACEZONE && target.isInsideZone(L2Zone.FLAG_PEACE))
				return false;

			if (me.isChampion() && Config.CHAMPION_PASSIVE)
				return false;

			return me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target);
		}
	}

	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == IDLE || intention == ACTIVE)
		{
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable) _actor;
				if (!npc.getKnownList().getKnownPlayers().isEmpty())
				{
					intention = ACTIVE;
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

		startAITask();
	}

	private void notifyFaction(L2Character attacker)
	{
		try
		{
			String faction_id = ((L2Npc) _actor).getFactionId();
			Collection<L2Object> objectsCollect = _actor.getKnownList().getKnownObjects().values();
			for (L2Object obj : objectsCollect)
				if (obj instanceof L2Npc)
				{

					L2Npc npc = (L2Npc) obj;
					boolean sevenSignFaction = false;
					if (!faction_id.equals(npc.getFactionId()) && !sevenSignFaction)
					{
						continue;
					}
					if (_actor.isInsideRadius(npc, npc.getFactionRange() + npc.getTemplate().getCollisionRadius() * 2, true, false) && npc.getAI() != null)
						if (Math.abs(attacker.getZ() - npc.getZ()) < 600 && !npc.isInCombat() && GeoData.getInstance().canSeeTarget(_actor, npc))
						{
							if (attacker instanceof L2PcInstance && attacker.isInParty() && attacker.getParty().isInDimensionalRift())
							{
								byte riftType = attacker.getParty().getDimensionalRift().getType();
								byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

								if (_actor instanceof L2RiftInvaderInstance)
								{
									DimensionalRiftRoom room = DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom);
									if (room != null && !room.checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
									{
										continue;
									}
								}
							}
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1);

							if (attacker instanceof L2PcInstance || attacker instanceof L2Summon)
							{
								L2PcInstance player = attacker instanceof L2PcInstance ? (L2PcInstance) attacker : ((L2Summon) attacker).getOwner();
								if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null)
								{
									for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL))
									{
										quest.notifyFactionCall(npc, (L2Npc) _actor, player, attacker instanceof L2Summon);
									}
								}
							}
						}
				}
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		L2Attackable me = (L2Attackable) _actor;

		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);
			if (getIntention() != CtrlIntention.ATTACK)
			{
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}

				setIntention(CtrlIntention.ATTACK, target);
			}
		}
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		notifyFaction(attacker);
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}

		if (!_actor.isCoreAIDisabled())
		{
			((L2Attackable) _actor).addDamageHate(attacker, 0, 1);
		}

		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}

		if (getIntention() != ATTACK && !_actor.isCoreAIDisabled())
		{
			setIntention(CtrlIntention.ATTACK, attacker);
		}
		else if (((L2Attackable) _actor).getMostHated() != getAttackTarget() && !_actor.isCoreAIDisabled())
		{
			setIntention(CtrlIntention.ATTACK, attacker);
		}
		else if (getIntention() != INTERACT && _actor.isCoreAIDisabled())
		{
			setIntention(CtrlIntention.INTERACT, attacker);
		}

		super.onEvtAttacked(attacker);
		if (_actor instanceof L2MonsterInstance)
		{
			leader = (L2MonsterInstance) _actor;
			if (_actor instanceof L2MinionInstance)
			{
				L2MinionInstance minion = (L2MinionInstance) _actor;
				leader = minion.getLeader();
				if (leader != null && !leader.isInCombat())
				{
					leader.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
				}
			}
			if (leader.hasMinions())
			{
				leader.callMinionsToAssist(attacker);
			}

		}
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
			else if (getIntention() == IDLE)
			{
				thinkIdle();
			}
		}
		finally
		{
			_thinking = false;
		}
	}

	@Override
	protected void onIntentionActive()
	{
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		if (_selfAnalysis.lastBuffTick + 100 < GameTimeController.getGameTicks())
		{
			for (L2Skill sk : _selfAnalysis.buffSkills)
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN)
					{
						continue;
					}

					L2Object OldTarget = _actor.getTarget();

					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_selfAnalysis.lastBuffTick = GameTimeController.getGameTicks();
					_actor.setTarget(OldTarget);
				}
		}
		super.onIntentionAttack(target);
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}

	public void startAITask()
	{
		AttackableAiTaskManager.getInstance().startTask(this);
	}

	@Override
	public void stopAITask()
	{
		AttackableAiTaskManager.getInstance().stopTask(this);
		_accessor.detachAI();
	}

	protected void thinkActive()
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
			for (L2Object obj : npc.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Character))
				{
					continue;
				}
				L2Character target = (L2Character) obj;

				if (_actor instanceof L2FestivalMonsterInstance && obj instanceof L2PcInstance)
				{
					L2PcInstance targetPlayer = (L2PcInstance) obj;
					if (!targetPlayer.isFestivalParticipant())
					{
						continue;
					}
				}

				if (autoAttackCondition(target))
					if (npc.getHating(target) == 0)
					{
						npc.addDamageHate(target, 0, 1);
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

					setIntention(CtrlIntention.ATTACK, hated);

					L2MinionInstance minion;
					L2MonsterInstance boss;
					List<L2MinionInstance> minions;

					if (_actor instanceof L2MonsterInstance)
					{
						boss = (L2MonsterInstance) _actor;
						if (boss.hasMinions())
						{
							minions = boss.getSpawnedMinions();
							if (minions != null)
							{
								for (L2MinionInstance m : minions)
								{
									if (m == null)
									{
										continue;
									}

									if (!m.isRunning())
									{
										m.setRunning();
									}
									m.getAI().startFollow(_actor);
								}
							}
						}
					}
					else if (_actor instanceof L2MinionInstance)
					{
						minion = (L2MinionInstance) _actor;
						boss = minion.getLeader();
						if (!boss.isRunning())
						{
							boss.setRunning();
						}
						boss.getAI().startFollow(_actor);
						minions = boss.getSpawnedMinions();
						for (L2MinionInstance m : minions)
							if (!(m.getObjectId() == _actor.getObjectId()))
							{
								if (!m.isRunning())
								{
									m.setRunning();
								}
								m.getAI().startFollow(_actor);
							}

					}
				}
				return;
			}
		}

		if (_actor instanceof L2FestivalMonsterInstance)
			return;

		if (!npc.canReturnToSpawnPoint())
			return;

		if (_actor instanceof L2MinionInstance)
		{
			L2MinionInstance minion = (L2MinionInstance) _actor;

			if (minion.getLeader() == null)
				return;

			int offset;

			if (_actor.isRaid())
			{
				offset = 500;
			}
			else
			{
				offset = 200;
			}

			if (minion.getLeader().isRunning())
			{
				_actor.setRunning();
			}
			else
			{
				_actor.setWalking();
			}

			if (_actor.getPlanDistanceSq(minion.getLeader()) > offset * offset)
			{
				int x1, y1, z1;
				x1 = minion.getLeader().getX() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				y1 = minion.getLeader().getY() + Rnd.nextInt((offset - 30) * 2) - (offset - 30);
				z1 = minion.getLeader().getZ();

				moveTo(x1, y1, z1);
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				for (L2Skill sk : _selfAnalysis.buffSkills)
					if (_actor.getFirstEffect(sk.getId()) == null)
					{
						if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF && Rnd.nextInt(2) != 0)
						{
							continue;
						}
						if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						if (_actor.isSkillDisabled(sk.getId()))
						{
							continue;
						}

						L2Object OldTarget = _actor.getTarget();

						_actor.setTarget(_actor);
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
			}
		}
		else if (npc.getSpawn() != null && Rnd.nextInt(RANDOM_WALK_RATE) == 0 && !(_actor.isRaid() || _actor instanceof L2MinionInstance || _actor instanceof L2ChestInstance || _actor instanceof L2GuardInstance))
		{
			for (L2Skill sk : _selfAnalysis.buffSkills)
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					if (sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF && Rnd.nextInt(2) != 0)
					{
						continue;
					}
					if (_actor.getStatus().getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}

					L2Object OldTarget = _actor.getTarget();

					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					return;
				}

			if (_actor instanceof L2Boss || _actor instanceof L2MinionInstance || _actor instanceof L2ChestInstance || _actor instanceof L2GuardInstance)
				return;

			int range = Config.MAX_DRIFT_RANGE;
			int x1 = npc.getSpawn().getLocx();
			int y1 = npc.getSpawn().getLocy();
			int z1 = npc.getSpawn().getLocz();

			boolean turnBackToStartLocation = false;

			if (Math.sqrt(_actor.getPlanDistanceSq(x1, y1)) > range * 2)
			{
				turnBackToStartLocation = true;
				if (!_actor.isDead())
				{
					_actor.teleToLocation(x1, y1, z1);
				}
			}
			else
			{
				x1 += Rnd.nextInt(range);
				y1 += Rnd.nextInt(range);
				z1 = npc.getZ();
				moveTo(x1, y1, z1);
			}

			if (_actor instanceof L2MonsterInstance)
			{
				L2MonsterInstance boss = (L2MonsterInstance) _actor;
				if (boss.hasMinions())
				{
					boss.callMinions(turnBackToStartLocation);
				}
			}
		}

		_actor.returnHome();
	}

	protected void thinkAttack()
	{
		if (_attackTimeout < GameTimeController.getGameTicks())
			if (_actor.isRunning())
			{
				_actor.setWalking();
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}

		L2Character originalAttackTarget = getAttackTarget();

		if (originalAttackTarget == null || originalAttackTarget.isAlikeDead() || _attackTimeout < GameTimeController.getGameTicks())
		{
			if (originalAttackTarget != null)
			{
				((L2Attackable) _actor).stopHating(originalAttackTarget);
			}

			setIntention(ACTIVE);

			_actor.setWalking();
			return;
		}

		if (((L2Npc) _actor).getFactionId() != null)
		{
			String faction_id = ((L2Npc) _actor).getFactionId();

			Collection<L2Object> objectsCollect = _actor.getKnownList().getKnownObjects().values();

			if (objectsCollect == null)
				return;

			try
			{
				for (L2Object obj : objectsCollect)
					if (obj instanceof L2Npc)
					{
						L2Npc npc = (L2Npc) obj;

						boolean sevenSignFaction = false;

						if (faction_id != npc.getFactionId() && !sevenSignFaction)
						{
							continue;
						}
						if (_actor.isInsideRadius(npc, npc.getFactionRange() + npc.getTemplate().getCollisionRadius(), true, false) && npc.getAI() != null)
							if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && npc.getStatus().getCurrentHp() < npc.getMaxHp() * 0.6 && _actor.getStatus().getCurrentHp() > _actor.getMaxHp() / 2 && _actor.getStatus().getCurrentMp() > _actor.getMaxMp() / 2)
								if (npc.isDead() && _actor instanceof L2MinionInstance)
								{
									if (((L2MinionInstance) _actor).getLeader() == npc)
									{
										for (L2Skill sk : _selfAnalysis.resurrectSkills)
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
											if (10 >= Rnd.get(100))
											{
												continue;
											}
											if (!GeoData.getInstance().canSeeTarget(_actor, npc))
											{
												break;
											}

											L2Object OldTarget = _actor.getTarget();

											_actor.setTarget(npc);
											DecayTaskManager.getInstance().cancelDecayTask(npc);
											DecayTaskManager.getInstance().addDecayTask(npc);
											clientStopMoving(null);
											_accessor.doCast(sk);
											_actor.setTarget(OldTarget);
											return;
										}
									}
								}
								else if (npc.isInCombat())
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

										int chance = 4;
										if (_actor instanceof L2MinionInstance)
											if (((L2MinionInstance) _actor).getLeader() == npc)
											{
												chance = 6;
											}
											else
											{
												chance = 3;
											}
										if (npc instanceof L2Boss)
										{
											chance = 6;
										}
										if (chance >= Rnd.get(100))
										{
											continue;
										}
										if (!GeoData.getInstance().canSeeTarget(_actor, npc))
										{
											break;
										}

										L2Object OldTarget = _actor.getTarget();
										_actor.setTarget(npc);
										clientStopMoving(null);
										_accessor.doCast(sk);
										_actor.setTarget(OldTarget);
										return;
									}
								}
					}
			}
			catch (Exception e)
			{
			}
		}

		if (_actor.isAttackingDisabled())
			return;

		L2Character[] hated = ((L2Attackable) _actor).get2MostHated();
		if (_actor.isConfused())
			if (hated != null)
			{
				hated[0] = originalAttackTarget;
			}
			else
			{
				hated = new L2Character[]
				{
					originalAttackTarget,
					null
				};
			}

		if (hated == null || hated[0] == null)
		{
			setIntention(ACTIVE);
			return;
		}
		if (hated[0] != originalAttackTarget)
		{
			setAttackTarget(hated[0]);
		}
		_mostHatedAnalysis.update(hated[0]);
		_secondMostHatedAnalysis.update(hated[1]);
		_actor.setTarget(_mostHatedAnalysis.character);
		double dist2 = _actor.getPlanDistanceSq(_mostHatedAnalysis.character.getX(), _mostHatedAnalysis.character.getY());
		int combinedCollision = _actor.getTemplate().getCollisionRadius() + _mostHatedAnalysis.character.getTemplate().getCollisionRadius();
		int range = _actor.getPhysicalAttackRange() + combinedCollision;

		if (!_actor.isMuted() && _attackTimeout - 160 < GameTimeController.getGameTicks() && _secondMostHatedAnalysis.character != null)
			if (Util.checkIfInRange(900, _actor, hated[1], true))
			{
				((L2Attackable) _actor).reduceHate(hated[0], 2 * (((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1])));
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		if (_actor.isRooted() && _secondMostHatedAnalysis.character != null)
			if (_selfAnalysis.isMage && dist2 > _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange && _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < _selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange)
			{
				((L2Attackable) _actor).reduceHate(hated[0], 1 + ((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1]));
			}
			else if (dist2 > range * range && _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < range * range)
			{
				((L2Attackable) _actor).reduceHate(hated[0], 1 + ((L2Attackable) _actor).getHating(hated[0]) - ((L2Attackable) _actor).getHating(hated[1]));
			}

		if (dist2 < 10000 + combinedCollision * combinedCollision && !_selfAnalysis.isFighter && !_selfAnalysis.isBalanced && (_selfAnalysis.hasLongRangeSkills || _selfAnalysis.isArcher || _selfAnalysis.isHealer) && (_mostHatedAnalysis.isBalanced || _mostHatedAnalysis.isFighter) && (_mostHatedAnalysis.character.isRooted() || _mostHatedAnalysis.isSlower) && (Config.PATHFINDING ? 20 : 12) >= Rnd.get(100))
		{
			int posX = _actor.getX();
			int posY = _actor.getY();
			int posZ = _actor.getZ();
			double distance = Math.sqrt(dist2);

			int signx = -1;
			int signy = -1;
			if (_actor.getX() > _mostHatedAnalysis.character.getX())
			{
				signx = 1;
			}
			if (_actor.getY() > _mostHatedAnalysis.character.getY())
			{
				signy = 1;
			}
			posX += Math.round((float) (signx * (range / 2 + Rnd.get(range)) - distance));
			posY += Math.round((float) (signy * (range / 2 + Rnd.get(range)) - distance));
			setIntention(CtrlIntention.MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
			return;
		}

		if (dist2 > 96100 + combinedCollision * combinedCollision && _selfAnalysis.hasLongRangeSkills && !GeoData.getInstance().canSeeTarget(_actor, _mostHatedAnalysis.character))
			if (!(_selfAnalysis.isMage && _actor.isMuted()))
			{
				moveToPawn(_mostHatedAnalysis.character, 300);
				return;
			}

		if (_mostHatedAnalysis.character.isMoving())
		{
			range += 50;
		}
		if (dist2 > range * range)
		{
			if (!_actor.isMuted() && (_selfAnalysis.hasLongRangeSkills || !_selfAnalysis.healSkills.isEmpty()))
			{
				if (!_mostHatedAnalysis.isCanceled)
				{
					for (L2Skill sk : _selfAnalysis.cancelSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= 8)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_mostHatedAnalysis.isCanceled = true;
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
				{
					for (L2Skill sk : _selfAnalysis.debuffSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						int chance = 8;
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
						{
							chance = 3;
						}
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
						{
							chance = 12;
						}
						if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
						{
							chance = 10;
						}
						if (_selfAnalysis.isHealer)
						{
							chance = 12;
						}
						if (_mostHatedAnalysis.isMagicResistant)
						{
							chance /= 2;
						}

						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isMuted())
				{
					int chance = 8;
					if (!(_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
					{
						chance = 3;
					}
					for (L2Skill sk : _selfAnalysis.muteSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
				{
					double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (L2Skill sk : _selfAnalysis.muteSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || secondHatedDist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= 2)
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isSleeping())
				{
					for (L2Skill sk : _selfAnalysis.sleepSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 1))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isSleeping())
				{
					double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (L2Skill sk : _selfAnalysis.sleepSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || secondHatedDist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 3))
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isRooted())
				{
					for (L2Skill sk : _selfAnalysis.rootSkills)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_mostHatedAnalysis.isSlower ? 3 : 8))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (!_mostHatedAnalysis.character.isAttackingDisabled())
				{
					for (L2Skill sk : _selfAnalysis.generalDisablers)
					{
						int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
						{
							continue;
						}
						if (Rnd.nextInt(100) <= (_selfAnalysis.isFighter && _actor.isRooted() ? 15 : 7))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				if (_actor.getStatus().getCurrentHp() < _actor.getMaxHp() * 0.4)
				{
					for (L2Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk))
						{
							continue;
						}
						int chance = 7;
						if (_mostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						if (_secondMostHatedAnalysis.character == null || _secondMostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						if (Rnd.nextInt(100) <= chance)
						{
							_actor.setTarget(_actor);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}

				int castingChance = 5;
				if (_selfAnalysis.isMage || _selfAnalysis.isHealer)
				{
					castingChance = 50;
				}
				if (_selfAnalysis.isBalanced)
					if (!_mostHatedAnalysis.isFighter)
					{
						castingChance = 15;
					}
					else
					{
						castingChance = 25;
					}
				if (_selfAnalysis.isFighter)
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 3;
					}
					else
					{
						castingChance = 7;
					}
					if (_actor.isRooted())
					{
						castingChance = 20;
					}
				}
				for (L2Skill sk : _selfAnalysis.generalSkills)
				{
					int castRange = sk.getCastRange() + combinedCollision;
					if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
					{
						continue;
					}

					if (Rnd.nextInt(100) <= castingChance)
					{
						clientStopMoving(null);
						_accessor.doCast(sk);
						_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
						return;
					}
				}
			}

			if (_selfAnalysis.isMage && !_actor.isMuted())
				if (_actor.getMaxMp() / 3 < _actor.getStatus().getCurrentMp())
				{
					range = _selfAnalysis.maxCastRange;
					if (dist2 < range * range)
						return;
				}
			if (_selfAnalysis.isHealer)
				return;

			if (_mostHatedAnalysis.character.isMoving())
			{
				range -= 100;
			}
			if (range < 5)
			{
				range = 5;
			}
			moveToPawn(_mostHatedAnalysis.character, range);
			return;
		}

		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();

		if (!_mostHatedAnalysis.isCanceled)
		{
			for (L2Skill sk : _selfAnalysis.cancelSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 8)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_mostHatedAnalysis.isCanceled = true;
					return;
				}
			}
		}
		if (_selfAnalysis.lastDebuffTick + 60 < GameTimeController.getGameTicks())
		{
			for (L2Skill sk : _selfAnalysis.debuffSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				int chance = 5;
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
				{
					chance = 3;
				}
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
				{
					chance = 3;
				}
				if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
				{
					chance = 4;
				}
				if (_selfAnalysis.isHealer)
				{
					chance = 12;
				}
				if (_mostHatedAnalysis.isMagicResistant)
				{
					chance /= 2;
				}
				if (sk.getCastRange() < 200)
				{
					chance += 3;
				}
				if (Rnd.nextInt(100) <= chance)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isMuted() && (_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
		{
			for (L2Skill sk : _selfAnalysis.muteSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 7)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
		{
			double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (L2Skill sk : _selfAnalysis.muteSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || secondHatedDist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 3)
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isSleeping() && _selfAnalysis.isHealer)
		{
			for (L2Skill sk : _selfAnalysis.sleepSkills)
			{
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= 10)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
					return;
				}
			}
		}
		if (_secondMostHatedAnalysis.character != null && !_secondMostHatedAnalysis.character.isSleeping())
		{
			double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (L2Skill sk : _selfAnalysis.sleepSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || secondHatedDist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 4))
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isRooted() && _mostHatedAnalysis.isFighter && !_selfAnalysis.isFighter)
		{
			for (L2Skill sk : _selfAnalysis.rootSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= (_selfAnalysis.isHealer ? 10 : 4))
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (!_mostHatedAnalysis.character.isAttackingDisabled())
		{
			for (L2Skill sk : _selfAnalysis.generalDisablers)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
				{
					continue;
				}
				if (Rnd.nextInt(100) <= (sk.getCastRange() < 200 ? 10 : 7))
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		if (_actor.getStatus().getCurrentHp() < _actor.getMaxHp() * (_selfAnalysis.isHealer ? 0.7 : 0.4))
		{
			for (L2Skill sk : _selfAnalysis.healSkills)
			{
				if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
				{
					continue;
				}
				if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk))
				{
					continue;
				}
				int chance = _selfAnalysis.isHealer ? 15 : 7;
				if (_mostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				if (_secondMostHatedAnalysis.character == null || _secondMostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				if (Rnd.nextInt(100) <= chance)
				{
					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		for (L2Skill sk : _selfAnalysis.generalSkills)
		{
			if (_actor.isMuted() && sk.isMagic() || _actor.isPhysicalMuted() && !sk.isMagic())
			{
				continue;
			}
			int castRange = sk.getCastRange() + combinedCollision;
			if (_actor.isSkillDisabled(sk.getId()) || _actor.getStatus().getCurrentMp() < _actor.getStat().getMpConsume(sk) || dist2 > castRange * castRange)
			{
				continue;
			}

			int castingChance = 5;
			if (_selfAnalysis.isMage || _selfAnalysis.isHealer)
				if (sk.getCastRange() < 200)
				{
					castingChance = 35;
				}
				else
				{
					castingChance = 25;
				}
			if (_selfAnalysis.isBalanced)
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else if (_mostHatedAnalysis.isMage)
				{
					castingChance = 2;
				}
				else
				{
					castingChance = 5;
				}
			if (_selfAnalysis.isFighter)
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else if (_mostHatedAnalysis.isMage)
				{
					castingChance = 1;
				}
				else
				{
					castingChance = 3;
				}

			if (Rnd.nextInt(100) <= castingChance)
			{
				clientStopMoving(null);
				_accessor.doCast(sk);
				return;
			}
		}

		if (!_selfAnalysis.isHealer)
		{
			clientStopMoving(null);
			_accessor.doAttack(_mostHatedAnalysis.character);
		}
	}

	protected void thinkIdle()
	{

	}
}