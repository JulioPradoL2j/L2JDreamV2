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
import com.dream.game.GameTimeController;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2SummonAI;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Attackable.AggroInfo;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2SiegeSummonInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.actor.knownlist.SummonKnownList;
import com.dream.game.model.actor.stat.SummonStat;
import com.dream.game.model.base.Experience;
import com.dream.game.model.itemcontainer.PetInventory;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcInfo;
import com.dream.game.network.serverpackets.PartySpelled;
import com.dream.game.network.serverpackets.PetDelete;
import com.dream.game.network.serverpackets.PetInfo;
import com.dream.game.network.serverpackets.PetStatusShow;
import com.dream.game.network.serverpackets.PetStatusUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Weapon;

public abstract class L2Summon extends L2Playable
{
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}

		public L2Summon getSummon()
		{
			return L2Summon.this;
		}

		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}
	}

	public static final int SIEGE_GOLEM_ID = 14737;
	public static final int HOG_CANNON_ID = 14768;

	public static final int SWOOP_CANNON_ID = 14839;
	private L2PcInstance _owner;
	private int _attackRange = 36;
	private boolean _follow = true;

	private boolean _previousFollowStatus = true;
	private int _chargedSoulShot;

	private int _chargedSpiritShot;
	private final int _soulShotsPerHit = 1;

	private final int _spiritShotsPerHit = 1;

	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_event = owner._event;
		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new L2Summon.AIAccessor());

		getPosition().setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);

		broadcastFullInfo();
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastFullInfoImpl(1);
	}

	public void broadcastFullInfoImpl(int val)
	{
		if (getOwner() == null)
			return;

		getOwner().sendPacket(new PetInfo(this, val));
		getOwner().sendPacket(new PetStatusUpdate(this));

		broadcastPacket(new NpcInfo(this, val));

		updateEffectIcons();
	}

	@Override
	public final void broadcastStatusUpdateImpl()
	{
		getOwner().sendPacket(new PetStatusUpdate(this));

		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
		broadcastPacket(su);
	}

	public final boolean checkStartAttacking()
	{
		return isStunned() || isSleeping() || isImmobileUntilAttacked() || isParalyzed() || isPetrified() || isFallsdown() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}

	public void deleteMe(L2PcInstance owner)
	{
		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		if (getInventory() != null)
		{
			getInventory().destroyAllItems("pet deleted", getOwner(), this);
		}

		stopAllEffects();
		getStatus().stopHpMpRegeneration();
		L2WorldRegion oldRegion = getWorldRegion();
		decayMe();
		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}

		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
		setTarget(null);
	}

	@Override
	protected void doAttack(L2Character target)
	{
		if (getOwner() != null && getOwner() == target && !getOwner().isBetrayed())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!target.isAttackable())
			if (!(this instanceof L2SiegeSummonInstance))
			{
				getAI().setIntention(CtrlIntention.ACTIVE);
				return;
			}
		super.doAttack(target);
	}

	@Override
	public void doCast(L2Skill skill)
	{
		if (PetDataTable.isImprovedBaby(getNpcId()))
		{
			super.doCast(skill);
			return;
		}

		if (!_owner.checkPvpSkill(getTarget(), skill))
		{
			_owner.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			_owner.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			int petLevel = getLevel();
			int skillLevel = petLevel / 10;
			if (petLevel >= 70)
			{
				skillLevel += (petLevel - 65) / 10;
			}

			if (skillLevel < 1)
			{
				skillLevel = 1;
			}

			L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
			if (skillToCast != null)
			{
				super.doCast(skillToCast);
			}
			else
			{
				super.doCast(skill);
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		L2PcInstance owner = getOwner();

		if (owner != null)
		{
			for (L2Character TgMob : getKnownList().getKnownCharacters())
				if (TgMob instanceof L2Attackable)
				{
					if (TgMob.isDead())
					{
						continue;
					}

					AggroInfo info = ((L2Attackable) TgMob).getAggroListRP().get(this);
					if (info != null)
					{
						((L2Attackable) TgMob).addDamageHate(owner, info._damage, info._hate);
					}
				}
			if (killer instanceof L2PcInstance)
			{
				L2PcInstance pk = killer.getActingPlayer();
				pk.onKillUpdatePvPKarma(this);
			}

		}

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	public boolean doDie(L2Character killer, boolean decayed)
	{
		if (!super.doDie(killer))
			return false;
		if (!decayed)
		{
			DecayTaskManager.getInstance().addDecayTask(this);
		}
		if (killer instanceof L2PcInstance)
		{
			L2PcInstance pk = killer.getActingPlayer();
			System.out.println("Update karma1");
			pk.onKillUpdatePvPKarma(this);
		}
		return true;
	}

	protected void doPickupItem(L2Object object)
	{
	}

	public void followOwner()
	{
		setFollowStatus(true);
	}

	@Override
	public final L2PcInstance getActingPlayer()
	{
		return getOwner();
	}

	@Override
	public final L2Summon getActingSummon()
	{
		return this;
	}

	public L2Weapon getActiveWeapon()
	{
		return null;
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2SummonAI(new L2Summon.AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	public int getArmor()
	{
		return 0;
	}

	public int getAttackRange()
	{
		return _attackRange;
	}

	public int getChargedSoulShot()
	{
		return _chargedSoulShot;
	}

	public int getChargedSpiritShot()
	{
		return _chargedSpiritShot;
	}

	public int getControlItemId()
	{
		return 0;
	}

	public abstract int getCurrentFed();

	public int getCurrentLoad()
	{
		return 0;
	}

	public long getExpForNextLevel()
	{
		if (getLevel() >= Experience.LEVEL.length - 1)
			return 0;
		return Experience.LEVEL[getLevel() + 1];
	}

	public long getExpForThisLevel()
	{
		if (getLevel() >= Experience.LEVEL.length)
			return 0;
		return Experience.LEVEL[getLevel()];
	}

	public boolean getFollowStatus()
	{
		return _follow;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	public final int getKarma()
	{
		return getOwner() != null ? getOwner().getKarma() : 0;
	}

	@Override
	public final SummonKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new SummonKnownList(this);
		}

		return (SummonKnownList) _knownList;
	}

	public abstract int getMaxFed();

	public int getMaxLoad()
	{
		return 0;
	}

	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	public final L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public L2Party getParty()
	{
		if (_owner == null)
			return null;

		return _owner.getParty();
	}

	public int getPetSpeed()
	{
		return getTemplate().getBaseRunSpd();
	}

	public byte getPvpFlag()
	{
		return getOwner() != null ? getOwner().getPvpFlag() : 0;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	public final int getSoulShotsPerHit()
	{
		return _soulShotsPerHit;
	}

	public final int getSpiritShotsPerHit()
	{
		return _spiritShotsPerHit;
	}

	@Override
	public SummonStat getStat()
	{
		if (_stat == null)
		{
			_stat = new SummonStat(this);
		}

		return (SummonStat) _stat;
	}

	public abstract int getSummonType();

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	public int getWeapon()
	{
		return 0;
	}

	public void giveAllToOwner()
	{
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (_owner == null)
			return false;
		return _owner.isAutoAttackable(attacker);
	}

	public boolean isHungry()
	{
		return false;
	}

	@Override
	public boolean isInCombat()
	{
		return getOwner() != null ? getOwner().isInCombat() : false;
	}

	@Override
	public boolean isInParty()
	{
		if (_owner == null)
			return false;

		return _owner.getParty() != null;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || getOwner().getProtection() > GameTimeController.getGameTicks();
	}

	public boolean isMountable()
	{
		return false;
	}

	@Override
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isBetrayed();
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		L2PcInstance attOwner = getActingPlayer();
		L2PcInstance trgOwner = target.getActingPlayer();
		if (miss)
		{
			attOwner.sendAvoidMessage(this);
			target.sendAvoidMessage(this);
			return;
		}

		if (pcrit)
		{
			attOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
		}

		if (mcrit)
		{
			attOwner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
		}

		if (trgOwner != null && attOwner != trgOwner)
			if (attOwner.isInOlympiadMode() && target instanceof L2PcInstance && trgOwner.isInOlympiadMode() && trgOwner.getOlympiadGameId() == attOwner.getOlympiadGameId())
				if (this instanceof L2SummonInstance || Config.ALT_OLY_INCLUDE_SUMMON_DAMAGE)
				{
					Olympiad.notifyCompetitorDamage(attOwner, damage, attOwner.getOlympiadGameId());
				}

		SystemMessage sm = null;
		if (target.isInvul() && !(target instanceof L2Npc))
		{
			sm = SystemMessageId.ATTACK_WAS_BLOCKED.getSystemMessage();
		}
		else if (this instanceof L2SummonInstance)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1).addNumber(damage);
			sm.addNumber(damage);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE).addNumber(damage);
			sm.addNumber(damage);
		}
		if (sm != null)
		{
			attOwner.sendPacket(sm);
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == _owner && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (player.getTarget() == this)
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.GEODATA)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.FOLLOW, this);
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
				}
			}

	}

	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setFollowStatus(true);
		setShowSummonAnimation(false);
		broadcastFullInfoImpl(0);
		getOwner().broadcastRelationChanged();
	}


	public void reduceCurrentHp(int damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);

		if (isDOT)
			return;

		if (this instanceof L2SummonInstance)
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1).addNumber(damage).addCharName(attacker));
		}
		else
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1).addNumber(damage).addCharName(attacker));
		}

	}

	public void setAttackRange(int range)
	{
		if (range < 36)
		{
			range = 36;
		}
		_attackRange = range;
	}

	public void setChargedSoulShot(int shotType)
	{
		_chargedSoulShot = shotType;
		if (getOwner() != null)
			if (shotType == L2ItemInstance.CHARGED_NONE && getOwner().getAutoSoulShot().size() > 0)
			{
				getOwner().rechargeAutoSoulShot(true, false, true, false);
			}
	}

	public void setChargedSpiritShot(int shotType)
	{
		_chargedSpiritShot = shotType;
		if (getOwner() != null)
			if (shotType == L2ItemInstance.CHARGED_NONE && getOwner().getAutoSoulShot().size() > 0)
			{
				getOwner().rechargeAutoSoulShot(false, true, true, true);
			}
	}

	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
		{
			getAI().setIntention(CtrlIntention.FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.IDLE, null);
		}
	}

	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);

		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			setFollowStatus(_previousFollowStatus);
		}
	}

	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	public void store()
	{
	}

	public void unSummon(L2PcInstance owner)
	{
		if (isVisible())
		{
			stopAllEffects();

			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));

			store();

			giveAllToOwner();

			stopAllEffects();
			getStatus().stopHpMpRegeneration();
			L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}

			getKnownList().removeAllKnownObjects();
			owner.setPet(null);
			setTarget(null);
		}
	}

	@Override
	public void updateAbnormalEffect()
	{
		for (L2PcInstance player : getKnownList().getKnownType(L2PcInstance.class))
		{
			player.sendPacket(new NpcInfo(this, 1));
		}
	}

	@Override
	public final void updateEffectIconsImpl()
	{
		final EffectInfoPacketList list = new EffectInfoPacketList(this);

		final L2Party party = getParty();

		if (party != null)
		{
			party.broadcastToPartyMembers(new PartySpelled(list));
		}
		else
		{
			getOwner().sendPacket(new PartySpelled(list));
		}
	}

	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null || isDead())
			return;

		if (skill.isPassive())
			return;

		if (isCastingNow())
			return;

		L2Character target = null;

		switch (skill.getTargetType())
		{

			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_SELF:
				target = this;
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}

		if (target == null)
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			}
			return;
		}

		if (isSkillDisabled(skill.getId()))
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1).addString("The skill cannot be used"));
			}
			return;
		}

		if (skill.getItemConsume() > 0 && getOwner().getInventory() != null)
		{
			L2ItemInstance requiredItems = getOwner().getInventory().getItemByItemId(skill.getItemConsumeId());

			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}

		if (getStatus().getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			}
			return;
		}

		if (getStatus().getCurrentHp() <= skill.getHpConsume())
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			}
			return;
		}

		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target) && getOwner() != null && !getOwner().allowPeaceAttack())
			{
				if (!isInFunEvent() || !target.isInFunEvent())
				{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return;
				}
			}

			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
					return;
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && !getOwner().allowPeaceAttack())
					return;

				if (!target.isAutoAttackable(this) && !forceUse)
				{
					switch (skill.getTargetType())
					{
						case TARGET_AURA:
						case TARGET_FRONT_AURA:
						case TARGET_BEHIND_AURA:
						case TARGET_CLAN:
						case TARGET_ALLY:
						case TARGET_PARTY:
						case TARGET_SELF:
							break;
						default:
							return;
					}
				}
			}
		}

		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		getAI().setIntention(CtrlIntention.CAST, skill, target);
	}
}