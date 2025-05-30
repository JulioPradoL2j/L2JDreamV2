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

import static com.dream.game.ai.CtrlIntention.ACTIVE;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.dream.Config;
import com.dream.Message;
import com.dream.Message.MessageId;
import com.dream.game.Shutdown;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.cache.HtmCache;
import com.dream.game.datatables.MobGroupTable;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.sql.SpawnTable;
import com.dream.game.datatables.xml.DoorTable;
import com.dream.game.datatables.xml.HelperBuffTable;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.FishermanManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.QuestManager;
import com.dream.game.manager.TownManager;
import com.dream.game.manager.clanhallsiege.RainbowSpringSiege;
import com.dream.game.manager.games.Lottery;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2DropCategory;
import com.dream.game.model.L2DropData;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Spawn;
import com.dream.game.model.actor.instance.L2AuctioneerInstance;
import com.dream.game.model.actor.instance.L2ChestInstance;
import com.dream.game.model.actor.instance.L2ClanHallManagerInstance;
import com.dream.game.model.actor.instance.L2ControllableMobInstance;
import com.dream.game.model.actor.instance.L2DoormenInstance;
import com.dream.game.model.actor.instance.L2FestivalGuideInstance;
import com.dream.game.model.actor.instance.L2FishermanInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.actor.instance.L2NpcInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2TeleporterInstance;
import com.dream.game.model.actor.instance.L2WarehouseInstance;
import com.dream.game.model.actor.knownlist.NpcKnownList;
import com.dream.game.model.actor.stat.NpcStat;
import com.dream.game.model.actor.status.NpcStatus;
import com.dream.game.model.entity.Town;
import com.dream.game.model.entity.events.BigSquash;
import com.dream.game.model.entity.events.Cristmas;
import com.dream.game.model.entity.events.EventMedals;
import com.dream.game.model.entity.events.L2day;
import com.dream.game.model.entity.events.StarlightFestival;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.itemcontainer.NpcInventory;
import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ExQuestInfo;
import com.dream.game.network.serverpackets.ExShowVariationCancelWindow;
import com.dream.game.network.serverpackets.ExShowVariationMakeWindow;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MoveToPawn;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.NpcInfo;
import com.dream.game.network.serverpackets.ServerObjectInfo;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.Stats;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.L2HelperBuff;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.tools.random.Rnd;

public class L2Npc extends L2Character
{
	protected class DespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!isDecayed())
			{
				deleteMe();
			}
		}
	}

	public class DestroyTemporalNPC implements Runnable
	{
		private final L2Spawn _oldSpawn;

		public DestroyTemporalNPC(L2Spawn spawn)
		{
			_oldSpawn = spawn;
		}

		@Override
		public void run()
		{
			try
			{
				_oldSpawn.getLastSpawn().deleteMe();
				_oldSpawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(_oldSpawn, false);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public class DestroyTemporalSummon implements Runnable
	{
		L2Summon _summon;
		L2PcInstance _player;

		public DestroyTemporalSummon(L2Summon summon, L2PcInstance player)
		{
			_summon = summon;
			_player = player;
		}

		@Override
		public void run()
		{
			_summon.unSummon(_player);
		}
	}

	protected class RandomAnimationTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (this != _rAniTask)
					return;
				if (isMob())
				{
					if (getAI().getIntention() != ACTIVE)
						return;
				}
				else if (!isInActiveRegion())
					return;

				if (!(isDead() || isStunned() || isSleeping() || isParalyzed()))
				{
					onRandomAnimation(null);
				}

				startRandomAnimationTimer();
			}
			catch (Exception e)
			{

			}
		}
	}

	public static final int INTERACTION_DISTANCE = 170;

	private static boolean cwCheck(L2PcInstance player)
	{
		return Config.CURSED_WEAPON_NPC_INTERACT || !player.isCursedWeaponEquipped();
	}

	private L2Spawn _spawn;

	private NpcInventory _inventory = null;

	private boolean _isBusy = false;

	private String _busyMessage = "";
	volatile boolean _isDecayed = false;
	private boolean _isSpoil = false;
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	private boolean _isInTown = false;
	private int _isSpoiledBy = 0;
	protected RandomAnimationTask _rAniTask = null;
	private int _currentLHandId;

	private int _currentRHandId;

	private int _currentCollisionHeight;

	private int _currentCollisionRadius;

	private boolean _notAgro = false;

	private boolean _notFaction = false;

	private boolean _isMagicBottle = false;

	public boolean _isJailMob = false;

	public L2Npc(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		super.initCharStatusUpdateValues();

		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();

		if (template == null)
		{
			_log.fatal("No template for Npc. Please check your datapack is setup correctly.");
			return;
		}

		setName(template.getName());
		setTitle(template.getTitle());

		if ((template.getSS() > 0 || template.getBSS() > 0) && template.getSSRate() > 0)
		{
			_inventory = new NpcInventory(this);
		}
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new NpcInfo(this));
	}

	public boolean canInteract(L2PcInstance player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			return false;

		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;

		// Can't interact sitted.
		if (player.isSitting())
			return false;

		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;

		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
			return false;

		return true;
	}

	protected boolean canTarget(L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_NPC_ITERACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_ACTION_NOT_ALLOWED_DURING_SHUTDOWN));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return true;
	}

	@SuppressWarnings("null")
	public void deleteMe()
	{
		L2WorldRegion oldRegion = getWorldRegion();
		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}

			for (L2Character character : getKnownList().getKnownCharacters())
			{
				if (character != null && character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				{
					character.abortCast();
				}

				if (character instanceof L2PcInstance && character.getAI().getAttackTarget() == this && character.getAI().getIntention() != CtrlIntention.MOVE_TO)
				{
					character.getAI().setIntention(CtrlIntention.IDLE, null);
				}
				if (character instanceof L2Summon && character.getAI().getAttackTarget() == this && character.getAI().getIntention() != CtrlIntention.MOVE_TO)
				{
					character.getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
				if (character.getTarget() == this)
				{
					character.setTarget(null);
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal("deleteMe()", e);
		}

		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			_log.fatal("Failed decayMe().", e);
		}

		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}

		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.fatal("Failed removing cleaning knownlist.", e);
		}

		L2World.getInstance().removeObject(this);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		_currentLHandId = getTemplate().getLhand();
		_currentRHandId = getTemplate().getRhand();
		_currentCollisionHeight = getTemplate().getCollisionHeight();
		_currentCollisionRadius = getTemplate().getCollisionRadius();
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	public void endDecayTask()
	{
		if (!isDecayed())
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		int weaponId = getTemplate().getRhand();

		if (weaponId < 1)
			return null;

		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().getRhand());

		if (!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	public int getAggroRange()
	{
		if (getNotAgro())
			return 0;
		return getTemplate().getAggroRange();
	}

	public final String getBusyMessage()
	{
		return _busyMessage;
	}

	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			Town town = TownManager.getInstance().getTown(this);
			_isInTown = town != null;

			if (!_isInTown)
			{
				_castleIndex = CastleManager.getInstance().getClosestCastle(this).getCastleId();
			}
			else if (town != null && town.getCastle() != null)
			{
				_castleIndex = town.getCastle().getCastleId();
			}
			else
			{
				_castleIndex = CastleManager.getInstance().getClosestCastle(this).getCastleId();
			}
		}

		return CastleManager.getInstance().getCastleById(_castleIndex);
	}

	public int getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
			return 10000;

		if (object instanceof L2NpcInstance || !(object instanceof L2Character))
			return 0;

		if (object instanceof L2Playable)
			return 1500;

		return 500;
	}

	public int getExpReward()
	{
		double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);

		return (int) (getTemplate().getRewardExp() * rateXp * Config.RATE_XP);
	}

	public final String getFactionId()
	{
		return getTemplate().getFactionId();
	}

	public int getFactionRange()
	{
		if (getNotFaction())
			return 0;
		return getTemplate().getFactionRange();
	}

	public final Fort getFort()
	{
		if (_fortIndex < 0)
		{
			Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null)
			{
				_fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}
			if (_fortIndex < 0)
			{
				_fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		if (_fortIndex < 0)
			return null;
		return FortManager.getInstance().getForts().get(_fortIndex);
	}

	public String getHtmlPath(int npcId, int val)
	{
		String filename;

		if (val == 0)
		{
			filename = "data/html/default/" + npcId + ".htm";
		}
		else
		{
			filename = "data/html/default/" + npcId + "-" + val + ".htm";
		}

		if (HtmCache.getInstance().isLoadable(filename))
			return filename;

		if (Config.DEVELOPER)
		{
			_log.warn("NPC: Using default dialog for " + getNpcId());
		}

		return "data/html/npcdefault.htm";
	}

	@Override
	public NpcInventory getInventory()
	{
		return _inventory;
	}

	public final boolean getIsInTown()
	{
		if (_castleIndex < 0)
		{
			getCastle();
		}
		return _isInTown;
	}

	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}

	@Override
	public NpcKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new NpcKnownList(this);
		}

		return (NpcKnownList) _knownList;
	}

	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}

	public boolean getNotAgro()
	{
		return _notAgro;
	}

	public boolean getNotFaction()
	{
		return _notFaction;
	}

	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	public int getRightHandItem()
	{
		return _currentRHandId;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		int weaponId = getTemplate().getLhand();

		if (weaponId < 1)
			return null;

		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().getLhand());

		if (!(item instanceof L2Weapon))
			return null;

		return (L2Weapon) item;
	}

	public L2Spawn getSpawn()
	{
		return _spawn;
	}

	public int getSpReward()
	{
		double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);

		return (int) (getTemplate().getRewardSp() * rateSp * Config.RATE_SP);
	}

	@Override
	public NpcStat getStat()
	{
		if (_stat == null)
		{
			_stat = new NpcStat(this);
		}

		return (NpcStat) _stat;
	}

	@Override
	public NpcStatus getStatus()
	{
		if (_status == null)
		{
			_status = new NpcStatus(this);
		}

		return (NpcStatus) _status;
	}

	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	public void giveBlessingSupport(L2PcInstance player)
	{
		if (player == null)
			return;

		int player_level = player.getLevel();
		setTarget(player);

		if (player_level > 39 || player.getClassId().level() >= 2)
		{
			String content = "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer</font>.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		doCast(SkillTable.getInstance().getInfo(5182, 1));
	}

	public boolean hasRandomAnimation()
	{
		return Config.MAX_NPC_ANIMATION > 0 && getNpcId() != 29045 && getNpcId() != 31074;
	}

	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}

	public boolean isAggressive()
	{
		return false;
	}

	@Override
	public boolean isAttackable()
	{
		return Config.ALT_ATTACKABLE_NPCS;
	}

	public final boolean isBusy()
	{
		return _isBusy;
	}

	public boolean isDecayed()
	{
		return _isDecayed;
	}

	public boolean isLethalImmune()
	{
		int npcId = getNpcId();
		if (npcId == 22398 || npcId == 22399 || npcId == 35062)
			return true;

		return false;
	}

	public boolean isMagicBottle()
	{
		return _isMagicBottle;
	}

	public boolean isMob()
	{
		return false;
	}

	public boolean isSpoil()
	{
		return _isSpoil;
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	public boolean isUsingShot(boolean physical)
	{
		if (_inventory == null)
			return false;
		if (physical && _inventory.sshotInUse)
			return true;

		return !physical && _inventory.bshotInUse;
	}

	public void makeCPRecovery(L2PcInstance player)
	{
		if (getNpcId() != 31225 && getNpcId() != 31226)
			return;

		if (!cwCheck(player))
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_GO_AWAY));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int neededmoney = 100;
		if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true))
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			setTarget(player);
			doCast(skill);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void makeSupportMagic(L2PcInstance player)
	{

		if (player == null || player.isCursedWeaponEquipped())
			return;

		int player_level = player.getLevel();
		int lowestLevel = 0;
		int highestLevel = 0;

		setTarget(player);

		if (player.isMageClass())
		{
			lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
			highestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
		}
		else
		{
			lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
			highestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
		}

		if (player_level > highestLevel)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
			npcReply.setHtml("<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + highestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>");
			npcReply.replace("%objectId%", getObjectId());
			player.sendPacket(npcReply);
			return;
		}

		if (player_level < lowestLevel)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
			npcReply.setHtml("<html><body>Newbie Guide:<br>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>");
			npcReply.replace("%objectId%", getObjectId());
			player.sendPacket(npcReply);
			return;
		}

		L2Skill skill = null;
		for (L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
		{
			if (helperBuffItem.isMagicClassBuff() == player.isMageClass())
			{
				if (player_level >= helperBuffItem.getLowerLevel() && player_level <= helperBuffItem.getUpperLevel())
				{
					skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
					if (skill.getSkillType() == L2SkillType.SUMMON)
					{
						player.doCast(skill);
					}
					else
					{
						doCast(skill);
					}
				}
			}
		}
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		try
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
				if (isAutoAttackable(player))
				{
					player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

					StatusUpdate su = new StatusUpdate(this);
					su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
					player.sendPacket(su);
					su = null;
				}
				else
				{
					player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				}

				player.sendPacket(new ValidateLocation(this));
			}
			else
			{
				player.sendPacket(new ValidateLocation(this));
				if (isAutoAttackable(player) && !isAlikeDead())
				{
					if (Math.abs(player.getZ() - getZ()) < 400)
					{
						player.getAI().setIntention(CtrlIntention.ATTACK, this);
					}
					else
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
				else if (!isAutoAttackable(player))
				{
					if (!canInteract(player))
					{
						player.getAI().setIntention(CtrlIntention.INTERACT, this);
					}
					else
					{
						if (player.isSitting() || player.isDead() || player.isFakeDeath() || player.getActiveTradeList() != null)
							return;

						broadcastPacket(new SocialAction(this, Rnd.get(8)));
						player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
						player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

						onRandomAnimation(player);
						if (_event != null)
							if (_event.onNPCTalk(this, player))
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
						Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
						if (qlsa != null && qlsa.length > 0)
						{
							player.setLastQuestNpcObject(getObjectId());
						}
						Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
						if (qlst != null && qlst.length == 1)
						{
							qlst[0].notifyFirstTalk(this, player);
						}
						else
						{
							showChatWindow(player, 0);
						}
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error: L2NpcInstance--> onAction(){" + e.toString() + "}\n\n", e);
			e.printStackTrace();
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

			if (isAutoAttackable(player))
			{
				StatusUpdate su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder html1 = new StringBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center><br><br>");
			String className = getClass().getName().substring(49);
			html1.append("<br>");
			html1.append("Instance Type: " + className + "<br1>Faction: " + getFactionId() + "<br1>Location ID: " + (getSpawn() != null ? getSpawn().getLocation() : 0) + "<br1>");

			if (this instanceof L2ControllableMobInstance)
			{
				html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) this).getGroupId() + "<br>");
			}
			else
			{
				html1.append("Respawn Time: " + (getSpawn() != null ? getSpawn().getRespawnDelay() / 1000 + "  Seconds<br>" : "?  Seconds<br>"));
			}
			html1.append("Intention: " + getAI().getIntention() + "<br1>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().getNpcId() + "</td></tr>");
			html1.append("<tr><td>Castle</td><td>" + getCastle().getCastleId() + "</td><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");
			html1.append("<tr><td>Level</td><td>" + getLevel() + "</td><td>Aggro</td><td>" + (this instanceof L2Attackable ? getAggroRange() : 0) + "</td></tr>");
			html1.append("</table><br>");

			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + getStatus().getCurrentHp() + "</td><td>Current MP</td><td>" + getStatus().getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().getRace() + "</td><td></td><td></td></tr>");
			html1.append("</table><br>");

			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getStat().getSTR() + "</td><td>DEX</td><td>" + getStat().getDEX() + "</td><td>CON</td><td>" + getStat().getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getStat().getWIT() + "</td><td>MEN</td><td>" + getStat().getMEN() + "</td></tr>");
			html1.append("</table>");

			html1.append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().getNpcId() + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"><br1></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><br1></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().getNpcId() + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
			html1.append("<tr><td><button value=\"Show Skillist\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().getNpcId() + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td><td></td></tr>");
			html1.append("</table></center><br>");
			html1.append("</body></html>");
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else if (Config.ALT_GAME_VIEWNPC && !(this instanceof L2ChestInstance))
		{
			player.setTarget(this);	
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

			if (isAutoAttackable(player))
			{
				StatusUpdate su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder html1 = new StringBuilder("<html><title>Description Of The NPC</title><body>");
			html1.append("<br><center><font color=\"LEVEL\">[Fighting qualities]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + (int) getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().getRace() + "</td><td></td><td></td></tr>");
			html1.append("</table>");

			html1.append("<br><center><font color=\"LEVEL\">[Basic quality]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getStat().getSTR() + "</td><td>DEX</td><td>" + getStat().getDEX() + "</td><td>CON</td><td>" + getStat().getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getStat().getWIT() + "</td><td>MEN</td><td>" + getStat().getMEN() + "</td></tr>");
			html1.append("</table>");

			if (Config.ALT_GAME_SHOWPC_DROP)
			{
				html1.append("<br><center><font color=\"LEVEL\">[Drop list]</font></center>");
				html1.append("<br>Mark chance: [0-30%] <font color=\"ffcc33\">[30-60%]</font> <font color=\"ff9900\">[60%+]</font>");
				html1.append("<table border=0 width=\"100%\">");
				if (getTemplate().getDropData() != null)
				{
					for (L2DropCategory cat : getTemplate().getDropData())
					{
						for (L2DropData drop : cat.getAllDrops())
						{
							String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();

							if (drop.getChance() >= 600000)
							{
								html1.append("<tr><td><font color=\"ff9900\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
							}
							else if (drop.getChance() >= 300000)
							{
								html1.append("<tr><td><font color=\"ffcc33\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
							}
							else
							{
								html1.append("<tr><td>" + name + "</td><td>" + (drop.isQuestDrop() ? "Quest" : cat.isSweep() ? "Sweep" : "Drop") + "</td></tr>");
							}
						}
					}
				}
				html1.append("</table>");
			}
			html1.append("</body></html>");
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{
			if (!canTarget(player))
				return;

			try
			{
				if (this != player.getTarget())
				{
					player.setTarget(this);
					if (isAutoAttackable(player))
					{
						player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
						StatusUpdate su = new StatusUpdate(this);
						su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
						su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
						player.sendPacket(su);
					}
					else
					{
						player.sendPacket(new MyTargetSelected(getObjectId(), 0));
					}
					player.sendPacket(new ValidateLocation(this));
				}
				else
				{
					player.sendPacket(new ValidateLocation(this));
					if (isAutoAttackable(player) && !isAlikeDead())
					{
						if (Math.abs(player.getZ() - getZ()) < 400 && player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false))
						{
							player.getAI().setIntention(CtrlIntention.ATTACK, this);
						}
						else
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
					else if (!isAutoAttackable(player))
					{
						if (!canInteract(player))
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
						else
						{
							onRandomAnimation(player);

							Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
							if (qlsa != null && qlsa.length > 0)
							{
								player.setLastQuestNpcObject(getObjectId());
							}
							Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
							if (qlst != null && qlst.length == 1)
							{
								qlst[0].notifyFirstTalk(this, player);
							}
							else
							{
								showChatWindow(player, 0);
							}

							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
					else
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
			}
			catch (Exception e)
			{
				_log.error("Error: L2NpcInstance--> onAction(){" + e.toString() + "}\n\n", e);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		{
			if (isBusy() && getBusyMessage().length() > 0)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/npcbusy.htm");
				html.replace("%busymessage%", getBusyMessage());
				html.replace("%npcname%", String.valueOf(getName()));
				html.replace("%playername%", player.getName());
				player.sendPacket(html);
			}
			else if (Config.ALLOW_WYVERN_UPGRADER && command.startsWith("upgrade") && player.getClan() != null && player.getClan().getHasCastle() != 0)
			{
				String type = command.substring(8);

				if (type.equalsIgnoreCase("wyvern"))
				{
					L2NpcTemplate wind = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_WIND_ID);
					L2NpcTemplate star = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_STAR_ID);
					L2NpcTemplate twilight = NpcTable.getInstance().getTemplate(PetDataTable.STRIDER_TWILIGHT_ID);

					L2Summon summon = player.getPet();
					L2NpcTemplate myPet = summon.getTemplate();

					if ((myPet.equals(wind) || myPet.equals(star) || myPet.equals(twilight)) && player.getAdena() >= 20000000 && player.getInventory().getItemByObjectId(summon.getControlItemId()) != null)
					{
						int exchangeItem = PetDataTable.WYVERN_ID;
						if (!player.reduceAdena("PetUpdate", 20000000, this, true))
							return;
						player.getInventory().destroyItem("PetUpdate", summon.getControlItemId(), 1, player, this);

						try
						{
							int level = summon.getLevel();
							int chance = (level - 54) * 10;

							if (Rnd.nextInt(100) < chance)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(new DestroyTemporalSummon(summon, player), 6000);
								player.addItem("PetUpdate", exchangeItem, 1, player, true, true);

								NpcHtmlMessage adminReply = new NpcHtmlMessage(getObjectId());
								StringBuilder replyMSG = new StringBuilder("<html><body>");
								replyMSG.append("Congratulations, the evolution suceeded.");
								replyMSG.append("</body></html>");
								adminReply.setHtml(replyMSG.toString());
								player.sendPacket(adminReply);
							}
							else
							{
								summon.reduceCurrentHp(summon.getStatus().getCurrentHp(), player);
							}

							player.sendPacket(new ItemList(player, true));
						}
						catch (Exception e)
						{
							_log.error(e.getMessage(), e);
						}
					}
					else
					{
						NpcHtmlMessage adminReply = new NpcHtmlMessage(getObjectId());
						StringBuilder replyMSG = new StringBuilder("<html><body>");

						replyMSG.append("You will need 20.000.000 and have the pet summoned for the ceremony ...");
						replyMSG.append("</body></html>");

						adminReply.setHtml(replyMSG.toString());
						player.sendPacket(adminReply);
					}
				}
			}
			else if (command.equalsIgnoreCase("TerritoryStatus"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				{
					if (getCastle().getOwnerId() > 0)
					{
						html.setFile("data/html/territorystatus.htm");
						L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
						html.replace("%clanname%", clan.getName());
						html.replace("%clanleadername%", clan.getLeaderName());
					}
					else
					{
						html.setFile("data/html/territorynoclan.htm");
					}
				}
				html.replace("%castlename%", getCastle().getName());
				html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				{
					if (getCastle().getCastleId() > 6)
					{
						html.replace("%territory%", "The Kingdom Of Elmar");
					}
					else
					{
						html.replace("%territory%", "The Kingdom Of Aden");
					}
				}
				player.sendPacket(html);
			}
			else if (command.startsWith("Quest"))
			{
				String quest = "";
				try
				{
					quest = command.substring(5).trim();
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}

				if (quest.length() == 0)
				{
					showQuestWindow(player);
				}
				else
				{
					showQuestWindow(player, quest);
				}
			}
			else if (command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}
				showChatWindow(player, val);
			}
			else if (command.startsWith("Link"))
			{
				String path = "";
				try
				{
					path = command.substring(5).trim();
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				if (path.equalsIgnoreCase(""))
					return;
				if (path.indexOf("..") != -1)
					return;
				String filename = "data/html/" + path;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", String.valueOf(getName()));
				player.sendPacket(html);
				if (Config.SHOW_HTML_CHAT && player.isGM())
				{
					player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_None, "HTML", filename));
				}
			}
			else if (command.startsWith("Loto"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}

				if (val == 0)
				{
					for (int i = 0; i < 5; i++)
					{
						player.setLoto(i, 0);
					}
				}
				showLotoWindow(player, val);
			}
			else if (command.startsWith("CPRecovery"))
			{
				makeCPRecovery(player);
			}
			else if (command.startsWith("SupportMagic"))
			{
				makeSupportMagic(player);
			}
			else if (command.startsWith("multisell"))
			{
				L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(9).trim()), player, false, getCastle().getTaxRate());
			}
			else if (command.startsWith("exc_multisell"))
			{
				L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(13).trim()), player, true, getCastle().getTaxRate());
			}
			else if (command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				switch (cmdChoice)
				{
					case 1:
						player.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
						player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
						break;
					case 2:
						player.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
						player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
						break;
				}
			}
			else if (command.startsWith("EnterRift"))
			{
				try
				{
					Byte b1 = Byte.parseByte(command.substring(10));
					DimensionalRiftManager.getInstance().start(player, b1, this);
				}
				catch (Exception e)
				{
				}
			}
			else if (command.startsWith("ChangeRiftRoom"))
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().manualTeleport(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().handleCheat(player, this);
				}
			}
			else if (command.startsWith("ExitRift"))
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().manualExitRift(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().handleCheat(player, this);
				}
			}
			else if (command.startsWith("remove_dp"))
			{
				int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
				int[] pen_clear_price =
				{
					3600,
					8640,
					25200,
					50400,
					86400,
					144000,
					144000
				};
				switch (cmdChoice)
				{
					case 1:
						String filename = "data/html/default/30981-1.htm";
						NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
						html.setFile(filename);
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%dp_price%", String.valueOf(pen_clear_price[player.getExpertiseIndex()]));
						player.sendPacket(html);
						break;
					case 2:
						NpcHtmlMessage Reply = new NpcHtmlMessage(getObjectId());
						StringBuilder replyMSG = new StringBuilder("<html><body>Black Judge:<br>");

						if (player.getDeathPenaltyBuffLevel() > 0)
						{
							if (player.getAdena() >= pen_clear_price[player.getExpertiseIndex()])
							{
								if (!player.reduceAdena("DeathPenality", pen_clear_price[player.getExpertiseIndex()], this, true))
									return;
								player.setDeathPenaltyBuffLevel(player.getDeathPenaltyBuffLevel() - 1);
								player.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
								player.sendEtcStatusUpdate();
								return;
							}

							replyMSG.append("The wound you have received from death's touch is too deep to be healed for the money you have to give me. Find more money if you wish death's mark to be fully removed from you.");
						}
						else
						{
							replyMSG.append("You have no more death wounds that require healing.<br>");
							replyMSG.append("Go forth and fight, both for this world and your own glory.");
						}

						replyMSG.append("</body></html>");
						Reply.setHtml(replyMSG.toString());
						player.sendPacket(Reply);
						break;
				}
			}
			else if (command.equals("questlist"))
			{
				player.sendPacket(ExQuestInfo.STATIC_PACKET);
			}
			else if (command.equalsIgnoreCase("exchange"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/merchant/exchange.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (command.startsWith("open_gate"))
			{
				final DoorTable _doorTable = DoorTable.getInstance();
				int doorId;

				StringTokenizer st = new StringTokenizer(command.substring(10), ", ");

				while (st.hasMoreTokens())
				{
					doorId = Integer.parseInt(st.nextToken());

					if (_doorTable.getDoor(doorId) != null)
					{
						_doorTable.getDoor(doorId).openMe();
						_doorTable.getDoor(doorId).onOpen();
					}
					else
					{
						_log.warn("Door Id does not exist.(" + doorId + ")");
					}
				}
				return;
			}
			else if (command.startsWith("close_gate"))
			{
				final DoorTable _doorTable = DoorTable.getInstance();
				int doorId;

				StringTokenizer st = new StringTokenizer(command.substring(10), ", ");

				while (st.hasMoreTokens())
				{
					doorId = Integer.parseInt(st.nextToken());

					if (_doorTable.getDoor(doorId) != null)
					{
						_doorTable.getDoor(doorId).closeMe();
						_doorTable.getDoor(doorId).onClose();
					}
					else
					{
						_log.warn("Door Id does not exist.(" + doorId + ")");
					}
				}
				return;
			}
			else if (command.startsWith("GiveBlessing"))
			{
				if (player == null)
					return;

				if (player.getLevel() > 39 || player.getClassId().level() >= 2)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/default/BlessingOfProtection-no.htm");
					player.sendPacket(html);
				}
				else
				{
					setTarget(player);

					L2Skill skill = SkillTable.getInstance().getInfo(5182, 1);
					if (skill != null)
					{
						doCast(skill);
					}
				}
			}
			else if (command.startsWith("fisherman_info"))
			{
				NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
				htm.setHtml(FishermanManager.getInstance().showHtm(player.getObjectId()));
				player.sendPacket(htm);
			}
			else if (command.startsWith("event"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(6));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}
				if (val == 0)
					return;
				if (getNpcId() == 31228)
				{
					EventMedals.exchangeItem(player, val);
				}
				if (getNpcId() == 31229)
				{
					EventMedals.exchangeItem(player, val);
				}
				if (getNpcId() == 31230)
				{
					EventMedals.exchangeItem(player, val);
				}
				if (getNpcId() == 31864)
				{
					Cristmas.exchangeItem(player, val);
				}
				if (getNpcId() == 32130)
				{
					L2day.exchangeItem(player, val);
				}
				if (getNpcId() == 31255)
				{
					BigSquash.exchangeItem(player, val);
				}
				if (getNpcId() == 31855)
				{
					StarlightFestival.exchangeItem(player, val);
				}
				if (getNpcId() == 35596)
				{
					RainbowSpringSiege.getInstance().exchangeItem(player, val);
				}
			}
			else if (command.startsWith("HotSpringsArena"))
				if (!RainbowSpringSiege.getInstance().enterOnArena(player))
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/default/35603-no.htm");
					player.sendPacket(html);
				}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onDecay()
	{
		if (isDecayed())
			return;
		setDecayed(true);

		setChampion(false);

		super.onDecay();

		if (_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
	}

	public void onRandomAnimation(L2PcInstance player)
	{
		broadcastPacket(new SocialAction(this, Rnd.get(2, 3)));
	}

	@Override
	public void onSpawn()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
		if (_inventory != null)
		{
			_inventory.reset();
		}

		setDecayed(false);
		super.onSpawn();
		revalidateZone(true);
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN))
			{
				quest.notifySpawn(this);
			}
		}
	}

	public boolean rechargeAutoSoulShot(boolean physical, boolean magic)
	{
		if (getTemplate().getSSRate() == 0)
			return false;

		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
			return false;
		if (magic)
		{
			if (getTemplate().getSSRate() < Rnd.get(100))
			{
				_inventory.bshotInUse = false;
				return false;
			}
			if (null != _inventory.destroyItemByItemId("Consume", 3947, weaponItem.getSpiritShotCount(), null, null))
			{
				_inventory.bshotInUse = true;
				broadcastPacket(new MagicSkillUse(this, this, 2061, 1, 0, 0, false), 600); // no grade
				return true;
			}

			_inventory.bshotInUse = false;
		}
		if (physical)
		{
			if (getTemplate().getSSRate() < Rnd.get(100))
			{
				_inventory.sshotInUse = false;
				return false;
			}

			if (null != _inventory.destroyItemByItemId("Consume", 1835, weaponItem.getSoulShotCount(), null, null))
			{
				_inventory.sshotInUse = true;
				broadcastPacket(new MagicSkillUse(this, this, 2039, 1, 0, 0, false), 600); // no grade
				return true;
			}

			_inventory.sshotInUse = false;
		}
		return false;
	}

	public L2Npc scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(this.new DespawnTask(), delay);
		return this;
	}

	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	public void setCollisionHeight(int height)
	{
		_currentCollisionHeight = height;
	}

	public void setCollisionRadius(int radius)
	{
		_currentCollisionRadius = radius;
	}

	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}

	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setLRHandId(int newLWeaponId, int newRWeaponId)
	{
		_currentRHandId = newRWeaponId;
		_currentLHandId = newLWeaponId;
		updateAbnormalEffect();
	}

	public void setMagicBottle(boolean result)
	{
		_isMagicBottle = result;
	}

	public void setNotAgro(boolean par)
	{
		_notAgro = par;
	}

	public void setNotFaction(boolean par)
	{
		_notFaction = par;
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}

	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}

	public void showChatWindow(L2PcInstance player)
	{
		showChatWindow(player, 0);
	}

	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player.isSitting() || player.isDead() || player.isFakeDeath() || player.getActiveTradeList() != null)
			return;

		if (!cwCheck(player) && !(player.getTarget() instanceof L2ClanHallManagerInstance || player.getTarget() instanceof L2DoormenInstance))
		{
			player.setTarget(player);
			return;
		}

		if (!Config.ALLOW_AIO_SPEAK_NPC && player.isAio() && (!(this instanceof L2MerchantInstance)))
		{
			if (this instanceof L2TeleporterInstance)
			{
				String filename = "data/html/mods/aiogatekeeper.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			return;
		}

		if (player.getKarma() > 0)
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2MerchantInstance)
			{
				if (showPkDenyChatWindow(player, "merchant"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof L2TeleporterInstance)
			{
				if (showPkDenyChatWindow(player, "teleporter"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof L2WarehouseInstance)
			{
				if (showPkDenyChatWindow(player, "warehouse"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2FishermanInstance)
				if (showPkDenyChatWindow(player, "fisherman"))
					return;

		if (this instanceof L2AuctioneerInstance && val == 0)
			return;

		int npcId = getTemplate().getNpcId();

		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		boolean isCompResultsPeriod = SevenSigns.getInstance().isCompResultsPeriod();
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();

		switch (npcId)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082:
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DAWN:
						if (isCompResultsPeriod)
						{
							filename += "dawn_priest_5.htm";
						}
						else if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dawn_priest_2c.htm";
								}
								else
								{
									filename += "dawn_priest_2a.htm";
								}
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DUSK:
						filename += "dawn_priest_3a.htm";
						break;
					default:
						if (isCompResultsPeriod)
						{
							filename += "dawn_priest_5.htm";
						}
						else if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								filename += "dawn_priest_4.htm";
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1a.htm";
						}
						break;
				}
				break;
			case 31085:
			case 31086:
			case 31087:
			case 31088:
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DUSK:
						if (isCompResultsPeriod)
						{
							filename += "dusk_priest_5.htm";
						}
						else if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dusk_priest_2c.htm";
								}
								else
								{
									filename += "dusk_priest_2a.htm";
								}
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DAWN:
						filename += "dusk_priest_3a.htm";
						break;
					default:
						if (isCompResultsPeriod)
						{
							filename += "dusk_priest_5.htm";
						}
						else if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								filename += "dusk_priest_4.htm";
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1a.htm";
						}
						break;
				}
				break;
			case 31111:
				if (playerCabal == sealAvariceOwner && playerCabal == compWinner)
				{
					switch (sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				}
				else
				{
					filename += "spirit_null.htm";
				}
				break;
			case 31112:
				filename += "spirit_exit.htm";
				break;
			case 31127:
			case 31128:
			case 31129:
			case 31130:
			case 31131:
				filename += "festival/dawn_guide.htm";
				break;
			case 31137:
			case 31138:
			case 31139:
			case 31140:
			case 31141:
				filename += "festival/dusk_guide.htm";
				break;
			case 31092:
				filename += "blkmrkt_1.htm";
				break;
			case 31113:
				if (Config.ALT_STRICT_SEVENSIGNS)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
					}
				}
				filename += "mammmerch_1.htm";
				break;
			case 31126:
				if (Config.ALT_STRICT_SEVENSIGNS)
				{
					switch (compWinner)
					{
						case SevenSigns.CABAL_DAWN:
							if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
						case SevenSigns.CABAL_DUSK:
							if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
							{
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
							break;
					}
				}
				filename += "mammblack_1.htm";
				break;
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136:
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
				filename += "festival/festival_witch.htm";
				break;
			case 31688:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, val);
				}
				break;
			case 31690:
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isHero() || player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				}
				else
				{
					filename = getHtmlPath(npcId, val);
				}
				break;
			default:
				if (npcId >= 31865 && npcId <= 31918)
				{
					filename += "rift/GuardianOfBorder.htm";
					break;
				}
				if (npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254)
					return;
				filename = getHtmlPath(npcId, val);
				break;
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);

		if (this instanceof L2MerchantInstance)
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
			{
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
			}

		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", String.valueOf(getName()));
		if (this instanceof L2FestivalGuideInstance)
		{
			html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		}
		player.sendPacket(html);

		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (Config.SHOW_HTML_CHAT && player.isGM())
		{
			player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_None, "HTML", filename));
		}
	}

	public void showChatWindow(L2PcInstance player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);

		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (Config.SHOW_HTML_CHAT && player.isGM())
		{
			player.sendPacket(new CreatureSay(0, SystemChatChannelId.Chat_None, "HTML", filename));
		}
	}

	public void showLotoWindow(L2PcInstance player, int val)
	{
		int npcId = getTemplate().getNpcId();
		String filename;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

		if (val == 0) // 0 - first buy lottery ticket window
		{
			filename = (getHtmlPath(npcId, 1));
			html.setFile(filename);
		}
		else if (val >= 1 && val <= 21) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			filename = (getHtmlPath(npcId, 5));
			html.setFile(filename);

			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == val)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}

			// if not rearched limit 5 and not unseted value
			if (count < 5 && found == 0 && val <= 20)
				for (int i = 0; i < 5; i++)
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}

			// setting pusshed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
						button = "0" + button;
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}

			if (count == 5)
			{
				String search = "0\">Return";
				String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (val == 22) // 22 - selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			int price = Config.ALT_LOTTERY_TICKET_PRICE;
			int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;

			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
					return;

				if (player.getLoto(i) < 17)
					enchant += Math.pow(2, player.getLoto(i) - 1);
				else
					type2 += Math.pow(2, player.getLoto(i) - 17);
			}

			if (!player.reduceAdena("Loto", price, this, true))
				return;

			Lottery.getInstance().increasePrize(price);

			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.addItem("Loto", item, player, true);

			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 23) // 23 - current lottery jackpot
		{
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = (getHtmlPath(npcId, 4));
			html.setFile(filename);

			int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (L2ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
					continue;
				if (item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					int[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch (check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}

			if (message.isEmpty())
				message += "There is no winning lottery ticket...<br>";

			html.replace("%result%", message);
		}
		else if (val > 24) // >24 - check lottery ticket by item object id
		{
			int lotonumber = Lottery.getInstance().getId();
			L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if (item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
				return;
			int[] check = Lottery.getInstance().checkTicket(item);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(4442));

			int adena = check[1];
			if (adena > 0)
				player.addAdena("Loto", adena, this, true);
			player.destroyItem("Loto", item, this, false);
			return;
		}
		html.replace("%objectId%", getObjectId());
		html.replace("%race%", Lottery.getInstance().getId());
		html.replace("%adena%", Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", Config.ALT_LOTTERY_5_NUMBER_RATE * 100);
		html.replace("%prize4%", Config.ALT_LOTTERY_4_NUMBER_RATE * 100);
		html.replace("%prize3%", Config.ALT_LOTTERY_3_NUMBER_RATE * 100);
		html.replace("%prize2%", Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private boolean showPkDenyChatWindow(L2PcInstance player, String type)
	{
		String html = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-pk.htm");

		if (html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}

		return false;
	}

	public void showQuestChooseWindow(L2PcInstance player, Quest[] quests)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		for (Quest q : quests)
		{
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\"> [").append(q.getDescr());

			QuestState qs = player.getQuestState(q.getScriptName());
			if (qs != null)
				if (qs.getState() == State.STARTED && qs.getInt("cond") > 0)
				{
					sb.append(" (In progress)");
				}
				else if (qs.getState() == State.COMPLETED)
				{
					sb.append(" (Completed)");
				}
			sb.append("]</a><br>");
		}

		sb.append("</body></html>");

		insertObjectIdAndShowChatWindow(player, sb.toString());
	}

	public void showQuestWindow(L2PcInstance player)
	{
		List<Quest> options = new ArrayList<>();

		QuestState[] awaits = player.getQuestsForTalk(getTemplate().getNpcId());
		Quest[] starts = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

		if (awaits != null)
		{
			for (QuestState x : awaits)
				if (!options.contains(x.getQuest()))
					if (x.getQuest().getQuestIntId() > 0 && x.getQuest().getQuestIntId() < 1000)
					{
						options.add(x.getQuest());
					}
		}

		if (starts != null)
		{
			for (Quest x : starts)
				if (!options.contains(x))
					if (x.getQuestIntId() > 0 && x.getQuestIntId() < 1000)
					{
						options.add(x);
					}
		}

		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}

	public void showQuestWindow(L2PcInstance player, String questId)
	{
		String content = null;

		Quest q = null;
		q = QuestManager.getInstance().getQuest(questId);

		QuestState qs = player.getQuestState(questId);

		if (q == null)
		{
			content = Message.getMessage(player, MessageId.MSG_NO_QUEST);
		}
		else
		{
			if (q.getQuestIntId() >= 1 && q.getQuestIntId() < 1000 && (player.getWeightPenalty() >= 3 || player.getInventoryLimit() * 0.8 <= player.getInventory().getSize()))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}

			if (qs == null)
			{
				if (q.getQuestIntId() >= 1 && q.getQuestIntId() < 1000)
				{
					Quest[] questList = player.getAllActiveQuests();
					if (questList.length >= 25)
					{
						player.sendPacket(SystemMessageId.TOO_MANY_QUESTS);
						return;
					}
				}
				Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);

				if (qlst != null && qlst.length > 0)
				{
					for (Quest temp : qlst)
						if (temp == q)
						{
							qs = q.newQuestState(player);
							break;
						}
				}
			}
		}

		if (qs != null)
		{
			if (!qs.getQuest().notifyTalk(this, qs))
				return;

			questId = qs.getQuest().getName();
			String stateId = State.getStateName(qs.getState());
			String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(path);

			if (_log.isDebugEnabled() || Config.DEBUG)
				if (content != null)
				{
					_log.debug("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					_log.debug("File not exists for quest " + questId + " html path: " + path);
				}
		}

		if (content != null)
		{
			insertObjectIdAndShowChatWindow(player, content);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void startRandomAnimationTimer()
	{
		if (!hasRandomAnimation())
			return;

		int minWait = isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		int maxWait = isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;

		int interval = Rnd.get(minWait, maxWait) * 1000;

		_rAniTask = new RandomAnimationTask();
		ThreadPoolManager.getInstance().scheduleGeneral(_rAniTask, interval);
	}

	@Override
	public String toString()
	{
		return getTemplate().getName();
	}

	@Override
	public void updateAbnormalEffect()
	{
		// Send NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2Npc
		for (L2PcInstance player : getKnownList().getKnownType(L2PcInstance.class))
		{
			if (getRunSpeed() == 0)
			{
				player.sendPacket(new ServerObjectInfo(this));
			}
			else
			{
				player.sendPacket(new NpcInfo(this));
			}
		}
	}
}