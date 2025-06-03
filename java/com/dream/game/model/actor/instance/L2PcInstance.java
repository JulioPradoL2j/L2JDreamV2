package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.Announcements;
import com.dream.game.GameTimeController;
import com.dream.game.Shutdown;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.ai.L2PlayerAI;
import com.dream.game.ai.L2SummonAI;
import com.dream.game.cache.HtmCache;
import com.dream.game.communitybbs.BB.Forum;
import com.dream.game.communitybbs.Manager.ForumsBBSManager;
import com.dream.game.datatables.GmListTable;
import com.dream.game.datatables.HeroSkillTable;
import com.dream.game.datatables.NobleSkillTable;
import com.dream.game.datatables.sql.CharNameTable;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.sql.NpcTable;
import com.dream.game.datatables.xml.CharTemplateTable;
import com.dream.game.datatables.xml.FishTable;
import com.dream.game.datatables.xml.HennaTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.datatables.xml.SkillTreeTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.handler.IItemHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.handler.admin.AdminEditChar;
import com.dream.game.handler.skill.SummonFriend;
import com.dream.game.handler.skill.TakeCastle;
import com.dream.game.handler.skill.TakeFort;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.BuffShopManager;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.CoupleManager;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.DimensionalRiftManager;
import com.dream.game.manager.DuelManager;
import com.dream.game.manager.FortManager;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.manager.ItemsAutoDestroy;
import com.dream.game.manager.OfflineManager;
import com.dream.game.manager.PartyRoomManager;
import com.dream.game.manager.QuestManager;
import com.dream.game.manager.RecipeController;
import com.dream.game.manager.SiegeManager;
import com.dream.game.model.BlockList;
import com.dream.game.model.CursedWeapon;
import com.dream.game.model.FishData;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2ClanMember;
import com.dream.game.model.L2Effect;
import com.dream.game.model.L2Fishing;
import com.dream.game.model.L2FriendList;
import com.dream.game.model.L2Macro;
import com.dream.game.model.L2ManufactureList;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Party;
import com.dream.game.model.L2PartyRoom;
import com.dream.game.model.L2PetData;
import com.dream.game.model.L2Radar;
import com.dream.game.model.L2RecipeList;
import com.dream.game.model.L2Request;
import com.dream.game.model.L2ShortCut;
import com.dream.game.model.L2SiegeClan;
import com.dream.game.model.L2Skill;
import com.dream.game.model.L2Skill.SkillTargetType;
import com.dream.game.model.L2SkillLearn;
import com.dream.game.model.MacroList;
import com.dream.game.model.ShortCuts;
import com.dream.game.model.TradeList;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Decoy;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.appearance.PcAppearance;
import com.dream.game.model.actor.knownlist.PcKnownList;
import com.dream.game.model.actor.reference.ClearableReference;
import com.dream.game.model.actor.reference.ImmutableReference;
import com.dream.game.model.actor.stat.PcStat;
import com.dream.game.model.actor.status.PcStatus;
import com.dream.game.model.base.ClassId;
import com.dream.game.model.base.ClassLevel;
import com.dream.game.model.base.Experience;
import com.dream.game.model.base.PlayerClass;
import com.dream.game.model.base.Race;
import com.dream.game.model.base.SubClass;
import com.dream.game.model.entity.Duel;
import com.dream.game.model.entity.events.GameEvent;
import com.dream.game.model.entity.events.archievements.AchievementsManager;
import com.dream.game.model.entity.sevensigns.SevenSigns;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.model.entity.siege.Castle;
import com.dream.game.model.entity.siege.Fort;
import com.dream.game.model.entity.siege.FortSiege;
import com.dream.game.model.entity.siege.Siege;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.itemcontainer.ItemContainer;
import com.dream.game.model.itemcontainer.PcFreight;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.model.itemcontainer.PcWarehouse;
import com.dream.game.model.itemcontainer.PetInventory;
import com.dream.game.model.olympiad.Olympiad;
import com.dream.game.model.quest.Quest;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.quest.State;
import com.dream.game.model.restriction.AvailableRestriction;
import com.dream.game.model.restriction.ObjectRestrictions;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.model.zone.L2TradeZone;
import com.dream.game.model.zone.L2Zone;
import com.dream.game.network.AuthServerThread;
import com.dream.game.network.Disconnection;
import com.dream.game.network.L2GameClient;
import com.dream.game.network.SystemChatChannelId;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.clientpackets.RequestActionUse;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.CameraMode;
import com.dream.game.network.serverpackets.ChangeWaitType;
import com.dream.game.network.serverpackets.CharInfo;
import com.dream.game.network.serverpackets.ConfirmDlg;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.dream.game.network.serverpackets.EnchantResult;
import com.dream.game.network.serverpackets.EtcStatusUpdate;
import com.dream.game.network.serverpackets.ExAutoSoulShot;
import com.dream.game.network.serverpackets.ExDuelUpdateUserInfo;
import com.dream.game.network.serverpackets.ExFishingEnd;
import com.dream.game.network.serverpackets.ExFishingStart;
import com.dream.game.network.serverpackets.ExOlympiadMode;
import com.dream.game.network.serverpackets.ExOlympiadSpelledInfo;
import com.dream.game.network.serverpackets.ExOlympiadUserInfo;
import com.dream.game.network.serverpackets.ExPutEnchantTargetItemResult;
import com.dream.game.network.serverpackets.ExSetCompassZoneCode;
import com.dream.game.network.serverpackets.ExShowScreenMessage;
import com.dream.game.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.dream.game.network.serverpackets.ExStorageMaxCount;
import com.dream.game.network.serverpackets.FriendList;
import com.dream.game.network.serverpackets.GameGuardQuery;
import com.dream.game.network.serverpackets.HennaInfo;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.LeaveWorld;
import com.dream.game.network.serverpackets.MagicEffectIcons;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ObservationMode;
import com.dream.game.network.serverpackets.ObservationReturn;
import com.dream.game.network.serverpackets.PartySmallWindowUpdate;
import com.dream.game.network.serverpackets.PartySpelled;
import com.dream.game.network.serverpackets.PetInventoryUpdate;
import com.dream.game.network.serverpackets.PlaySound;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.PledgeShowMemberListDelete;
import com.dream.game.network.serverpackets.PledgeShowMemberListUpdate;
import com.dream.game.network.serverpackets.PledgeSkillList;
import com.dream.game.network.serverpackets.PrivateStoreListBuy;
import com.dream.game.network.serverpackets.PrivateStoreListSell;
import com.dream.game.network.serverpackets.PrivateStoreManageListBuff;
import com.dream.game.network.serverpackets.PrivateStoreManageListBuy;
import com.dream.game.network.serverpackets.PrivateStoreManageListSell;
import com.dream.game.network.serverpackets.QuestList;
import com.dream.game.network.serverpackets.RecipeShopSellList;
import com.dream.game.network.serverpackets.RelationChanged;
import com.dream.game.network.serverpackets.Ride;
import com.dream.game.network.serverpackets.SendTradeDone;
import com.dream.game.network.serverpackets.SetSummonRemainTime;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.network.serverpackets.ShortBuffStatusUpdate;
import com.dream.game.network.serverpackets.ShortCutInit;
import com.dream.game.network.serverpackets.SkillCoolTime;
import com.dream.game.network.serverpackets.SkillList;
import com.dream.game.network.serverpackets.Snoop;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.SpecialCamera;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.TargetSelected;
import com.dream.game.network.serverpackets.TargetUnselected;
import com.dream.game.network.serverpackets.TitleUpdate;
import com.dream.game.network.serverpackets.TradePressOtherOk;
import com.dream.game.network.serverpackets.TradePressOwnOk;
import com.dream.game.network.serverpackets.TradeStart;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.BaseStats;
import com.dream.game.skills.Env;
import com.dream.game.skills.Formulas;
import com.dream.game.skills.Stats;
import com.dream.game.skills.conditions.ConditionGameTime;
import com.dream.game.skills.conditions.ConditionPlayerHp;
import com.dream.game.skills.effects.EffectFusion;
import com.dream.game.skills.funcs.Func;
import com.dream.game.skills.l2skills.L2SkillSummon;
import com.dream.game.taskmanager.AttackStanceTaskManager;
import com.dream.game.taskmanager.SQLQueue;
import com.dream.game.templates.chars.L2PcTemplate;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2ArmorType;
import com.dream.game.templates.item.L2EtcItem;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Henna;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2EffectType;
import com.dream.game.templates.skills.L2SkillType;
import com.dream.game.util.Broadcast;
import com.dream.game.util.DBFactory;
import com.dream.game.util.DatabaseUtils;
import com.dream.game.util.FiltredPreparedStatement;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.PcAction;
import com.dream.game.util.ThreadConnection;
import com.dream.game.util.Util;
import com.dream.game.util.mysql;
import com.dream.tools.geometry.Point3D;
import com.dream.tools.random.Rnd;
import com.dream.util.LinkedBunch;
import com.dream.util.SingletonList;
import com.dream.util.SingletonMap;
import com.dream.util.StatsSet;
import com.dream.util.Strings;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;
import javolution.util.FastMap;

public class L2PcInstance extends L2Playable
{
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
			
		}
		
		@Override
		public void doAttack(L2Character target)
		{
			_inWorld = true;
			super.doAttack(target);
			
			getPlayer().setRecentFakeDeath(false);
			L2Effect silentMove = getPlayer().getFirstEffect(L2EffectType.SILENT_MOVE);
			if (silentMove != null)
			{
				silentMove.exit();
			}
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			_inWorld = true;
			super.doCast(skill);
			
			getPlayer().setRecentFakeDeath(false);
			if (skill == null)
				return;
			if (!skill.isOffensive() && skill.getId() != 3261 && skill.getId() != 3260 && skill.getId() != 3262)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (getPlayer().getSecondRefusal())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (getPlayer().isSilentMoving() && skill.getId() != 51 && skill.getId() != 511)
			{
				L2Effect silentMove = getPlayer().getFirstEffect(L2EffectType.SILENT_MOVE);
				if (silentMove != null)
				{
					silentMove.exit();
				}
			}
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					for (L2CubicInstance cubic : getCubics().values())
						if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
						{
							cubic.doAction();
						}
				}
					break;
			}
		}
		
		public void doInteract(L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}
		
		public void doPickupItem(L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					setIsParalyzed(false);
				}
			}, 550);
			setIsParalyzed(true);
		}
		
		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}
	}
	
	public class ChargeTask implements Runnable
	{
		@Override
		public void run()
		{
			clearCharges();
		}
	}
	
	public final class ConditionGameTimeListener extends ConditionListener
	{
		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;
			
			if (newValue)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_APPLIES);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS);
			}
			
			if (f.funcOwner.getFuncOwnerSkill() != null)
			{
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			}
			else
			{
				sm.addString(f.funcOwner.getFuncOwnerName());
			}
			
			sendPacket(sm);
			
			broadcastUserInfo();
		}
		
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionGameTime)
			{
				super.onFuncAddition(f);
			}
		}
		
		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.GAME_TIME)
			{
				super.refresh(dependency);
			}
		}
	}
	
	private abstract class ConditionListener
	{
		private final Map<Func, Boolean> _values = new SingletonMap<Func, Boolean>().setShared();
		private final Env _env;
		
		protected ConditionListener()
		{
			_env = new Env();
			_env.player = L2PcInstance.this;
		}
		
		protected void onChange(Func f, boolean newValue)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", f.funcOwner.getFuncOwnerName() + (newValue ? " on." : " off."));
		}
		
		protected void onFuncAddition(Func f)
		{
			final boolean newValue = f.isAllowed(_env);
			
			_values.put(f, newValue);
			
			if (newValue)
			{
				onChange(f, true);
			}
		}
		
		protected void onFuncRemoval(Func f)
		{
			_values.remove(f);
		}
		
		protected void refresh(ConditionListenerDependency dependency)
		{
			for (Entry<Func, Boolean> entry : _values.entrySet())
			{
				boolean newValue = entry.getKey().isAllowed(_env);
				boolean oldValue = entry.setValue(newValue);
				
				if (newValue != oldValue)
				{
					onChange(entry.getKey(), newValue);
				}
			}
		}
	}
	
	public enum ConditionListenerDependency
	{
		CURRENT_HP,
		PLAYER_HP,
		GAME_TIME;
	}
	
	public final class ConditionPlayerHpListener extends ConditionListener
	{
		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;
			
			if (newValue)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_APPLIES);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_DISAPPEARS);
			}
			
			if (f.funcOwner.getFuncOwnerSkill() != null)
			{
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			}
			else
			{
				sm.addString(f.funcOwner.getFuncOwnerName());
			}
			
			sendPacket(sm);
			
			broadcastUserInfo();
		}
		
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionPlayerHp)
			{
				super.onFuncAddition(f);
			}
		}
		
		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.PLAYER_HP)
			{
				super.refresh(dependency);
			}
		}
	}
	
	public class dismount implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				dismount();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!isMounted())
				{
					stopFeed();
					return;
				}
				
				if (getCurrentFeed() > getFeedConsume())
				{
					setCurrentFeed(getCurrentFeed() - getFeedConsume());
				}
				else
				{
					setCurrentFeed(0);
					stopFeed();
					dismount();
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED));
				}
				
				int[] foodIds = PetDataTable.getFoodItemId(getMountNpcId());
				if (foodIds[0] == 0)
					return;
				L2ItemInstance food = null;
				food = getInventory().getItemByItemId(foodIds[0]);
				
				if (PetDataTable.isStrider(getMountNpcId()))
					if (getInventory().getItemByItemId(foodIds[1]) != null)
					{
						food = getInventory().getItemByItemId(foodIds[1]);
					}
				if (food != null && isHungry())
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(food.getItemId());
					if (handler != null)
					{
						handler.useItem(L2PcInstance.this, food);
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food.getItemId()));
					}
				}
			}
			catch (Exception e)
			{
				_log.fatal("Mounted Pet [NpcId: " + getMountNpcId() + "] a feed task error has occurred", e);
			}
		}
	}
	
	class InventoryEnable implements Runnable
	{
		@Override
		public void run()
		{
			_inventoryDisable = false;
		}
	}
	
	private class JailTask implements Runnable
	{
		L2PcInstance _player;
		
		protected JailTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.setInJail(false, 0);
		}
	}
	
	class LookingForFishTask implements Runnable
	{
		boolean _isNoob, _isUpperGrade;
		int _fishType, _fishGutsCheck, _gutsCheckTime;
		long _endTaskTime;
		
		protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}
			
			if (!GameTimeController.isNowNight() && _lure.isNightLure())
				return;
			
			int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}
	
	public class ProtectSitDownStandUp implements Runnable
	{
		@Override
		public void run()
		{
			_protectedSitStand = false;
		}
	}
	
	class RentPetTask implements Runnable
	{
		@Override
		public void run()
		{
			stopRentPet();
		}
	}
	
	public final class ShortBuffTask implements Runnable
	{
		@Override
		public void run()
		{
			sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
		}
	}
	
	public class SkillDat
	{
		private final L2Skill _skill;
		private final boolean _ctrlPressed;
		private final boolean _shiftPressed;
		
		protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
		{
			_skill = skill;
			_ctrlPressed = ctrlPressed;
			_shiftPressed = shiftPressed;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		public int getSkillId()
		{
			return getSkill() != null ? getSkill().getId() : -1;
		}
		
		public boolean isCtrlPressed()
		{
			return _ctrlPressed;
		}
		
		public boolean isShiftPressed()
		{
			return _shiftPressed;
		}
	}
	
	public static class TimeStamp
	{
		private final int skill;
		private final long reuse;
		private final long stamp;
		
		public TimeStamp(int _skill, long _reuse)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = System.currentTimeMillis() + reuse;
		}
		
		public TimeStamp(int _skill, long _reuse, long _systime)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = _systime;
		}
		
		public long getRemaining()
		{
			return Math.max(stamp - System.currentTimeMillis(), 0);
		}
		
		public long getReuse()
		{
			return reuse;
		}
		
		public int getSkill()
		{
			return skill;
		}
		
		public long getStamp()
		{
			return stamp;
		}
		
		public boolean hasNotPassed()
		{
			return System.currentTimeMillis() < stamp;
		}
	}
	
	class WarnUserTakeBreak implements Runnable
	{
		@Override
		public void run()
		{
			if (isOnline() == 1)
			{
				sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME);
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}
	
	class SkillRewardTime implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Skill skill : getAllSkills())
				if (isOnline() == 1 && getSkillLevel(Config.REWARDSKILL_TIME_SKILL_ID) < Config.REWARDSKILL_TIME_SKILL_MAXLVL)
				{
					if (skill.getId() == Config.REWARDSKILL_TIME_SKILL_ID && getSkillLevel(Config.REWARDSKILL_TIME_SKILL_ID) >= 1 && getSkillLevel(Config.REWARDSKILL_TIME_SKILL_ID) < Config.REWARDSKILL_TIME_SKILL_MAXLVL)
					{
						addSkill(SkillTable.getInstance().getInfo(Config.REWARDSKILL_TIME_SKILL_ID, skill.getLevel() + 1), true);
						String skillname = skill.getName();
						sendSkillList();
						sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Your skill " + skillname + " has been increased by one more level.");
					}
				}
				else
				{
					stopSkillRewardTime();
				}
		}
	}
	
	class WaterTask implements Runnable
	{
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;
			
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			reduceCurrentHp(reduceHp, L2PcInstance.this, false, false, null);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int) reduceHp));
		}
	}
	
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id, skill_level FROM character_skills WHERE charId = ? AND class_index = ?";
	
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (charId, skill_id, skill_level, skill_name, class_index) VALUES (?, ?, ?, ?, ?)";
	
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level = ? WHERE skill_id = ? AND charId = ? AND class_index = ?";
	
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id = ? AND charId = ? AND class_index = ?";
	
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE charId = ? AND class_index = ?";
	
	private static final String ADD_SKILL_SAVE = "REPLACE INTO character_skills_save (charId, skill_id, skill_level, effect_count, effect_cur_time, reuse_delay, systime, restore_type, class_index, buff_index) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id, skill_level, effect_count, effect_cur_time, reuse_delay, systime FROM character_skills_save WHERE charId = ? AND class_index = ? AND restore_type = ? ORDER BY buff_index ASC";
	
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE charId = ? AND class_index = ?";
	
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level = ?, maxHp = ?, curHp = ?, maxCp = ?, curCp = ?, maxMp = ?, curMp = ?, face = ?, hairStyle = ?, hairColor = ?, heading = ?, x = ?, y = ?, z = ?, exp = ?, expBeforeDeath = ?, sp = ?, karma = ?, pvpkills = ?, pkkills = ?, rec_have = ?, rec_left = ?, clanid = ?, race = ?, classid = ?, deletetime = ?, title = ?, online = ?, isin7sdungeon = ?, clan_privs = ?, wantspeace = ?, base_class = ?, onlinetime = ?, in_jail = ?, jail_timer = ?, newbie = ?, nobless = ?, pledge_rank = ?, subpledge = ?, last_recom_date = ?, lvl_joined_academy = ?, apprentice = ?, sponsor = ?, varka_ketra_ally = ?, clan_join_expiry_time = ?, clan_create_expiry_time = ?, char_name = ?, death_penalty_level = ?, pccaffe_points = ?, isBanned = ?, vip = ?, vip_end = ?, aio = ?, aio_end = ?, arena_wins = ?, arena_defeats = ?, haswhacc = ?, whaccid = ?, whaccpwd = ?, buffshop_slots = ?  WHERE charId = ?";
	
	private static final String RESTORE_CHARACTER = "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, rec_have, rec_left, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, pledge_rank, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time, clan_create_expiry_time, death_penalty_level, pccaffe_points, isBanned, vip, vip_end, aio, aio_end, arena_wins, arena_defeats, haswhacc, whaccid, whaccpwd, buffshop_slots FROM characters WHERE charId = ?";
	
	private static final String CREATE_CHARACTER = "INSERT INTO characters (account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, online, isin7sdungeon, clan_privs, wantspeace, base_class, newbie, nobless, pledge_rank, last_recom_date, vip, vip_end, aio, aio_end, arena_wins, arena_defeats, haswhacc, whaccid, whaccpwd, buffshop_slots) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String RESTORE_HEROSERVICE = "SELECT enddate FROM character_herolist WHERE charId = ?";
	
	private static final String UPDATE_HEROSERVICE = "UPDATE character_herolist SET enddate = ? WHERE charId = ?";
	
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id, exp, sp, level, class_index FROM character_subclasses WHERE charId = ? ORDER BY class_index ASC";
	
	private static final String ADD_CHAR_SUBCLASS = "REPLACE INTO character_subclasses (charId, class_id, exp, sp, level, class_index) VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp = ?, sp = ?, level = ?, class_id = ? WHERE charId = ? AND class_index = ?";
	
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE charId = ? AND class_index = ?";
	
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot, symbol_id FROM character_hennas WHERE charId = ? AND class_index = ?";
	
	private static final String ADD_CHAR_HENNA = "REPLACE INTO character_hennas (charId, symbol_id, slot, class_index) VALUES (?, ?, ?, ?)";
	
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE charId = ? AND slot = ? AND class_index = ?";
	
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE charId = ? AND class_index = ?";
	
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE charId = ? AND class_index = ?";
	
	private static final String RESTORE_CHAR_RECOMS = "SELECT charId, target_id FROM character_recommends WHERE charId = ?";
	private static final String ADD_CHAR_RECOM = "REPLACE INTO character_recommends (charId, target_id) VALUES (?, ?)";
	private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE charId = ?";
	private static final String CREATE_CHAR_DATA = "INSERT INTO character_data values (?, ?, ?)";
	private static final String STORE_CHAR_DATA = "UPDATE character_data set valueData = ? where charId = ? and valueName = ?";
	
	private static final String LOAD_CHAR_DATA = "SELECT valueName, valueData from character_data where charId = ?";
	
	public static final int REQUEST_TIMEOUT = 15;
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0),
		SkillTreeTable.getInstance().getExpertiseLevel(1),
		SkillTreeTable.getInstance().getExpertiseLevel(2),
		SkillTreeTable.getInstance().getExpertiseLevel(3),
		SkillTreeTable.getInstance().getExpertiseLevel(4),
		SkillTreeTable.getInstance().getExpertiseLevel(5)
	};
	private static final int[] COMMON_CRAFT_LEVELS =
	{
		5,
		20,
		28,
		36,
		43,
		49,
		55,
		62
	};
	private static final byte ONLINE_STATE_LOADED = 0;
	private static final byte ONLINE_STATE_ONLINE = 1;
	private static final byte ONLINE_STATE_DELETED = 2;
	private static final int[] CHARGE_SKILLS =
	{
		570,
		8,
		50
	};
	private static final String ACUMULATE_SKILLS_FOR_CHAR_SUB = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? ORDER BY skill_id , skill_level ASC";
	
	private static QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}
	
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex)
	{
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);
		
		player.setName(name);
		
		player.setBaseClass(player.getClassId());
		
		player.setNewbie(1);
		
		boolean ok = player.createDb();
		
		if (!ok)
			return null;
		
		return player;
	}
	
	private static void createHSdb(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_herolist (charId,enddate) values(?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert char data: ", e);
			return;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public static void disconnectIfOnline(int objectId)
	{
		L2PcInstance onlinePlayer = L2World.getInstance().findPlayer(objectId);
		
		if (onlinePlayer == null)
			return;
		
		onlinePlayer.store(true);
		if (onlinePlayer.isOfflineTrade())
		{
			OfflineManager.getInstance().removeTrader(onlinePlayer);
		}
		
		new Disconnection(onlinePlayer).defaultSequence(true);
	}
	
	private static void HStimeOver(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_HEROSERVICE);
			statement.setLong(1, 0);
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("HeroService: Could not increase data");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public static L2PcInstance load(int objectId)
	{
		disconnectIfOnline(objectId);
		
		L2PcInstance player = null;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();
			
			double currentHp = 1, currentMp = 1, currentCp = 1;
			if (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex") != 0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
				
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				player._baseExp = rset.getLong("exp");
				player._baseSP = rset.getInt("sp");
				player._baseLevel = rset.getByte("level");
				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				
				player.getStat().setLevel((byte) player._baseLevel);
				player.getStat().setSp(rset.getInt("sp"));
				player.setWantsPeace(rset.getInt("wantspeace") == 1);
				player.setHeading(rset.getInt("heading"));
				player.setKarma(rset.getInt("karma"));
				player._pccaffe = rset.getInt("pccaffe_points");
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setPrivateBuffShopLimit(rset.getInt("buffshop_slots"));
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}
				player.setVip(rset.getInt("vip") == 1 ? true : false);
				player.setVipEndTime(rset.getLong("vip_end"));
				player.setAio(rset.getInt("aio") == 1 ? true : false);
				player.setAioEndTime(rset.getLong("aio_end"));
				
				player.setArenaWins(rset.getInt("arena_wins"));
				player.setArenaDefeats(rset.getInt("arena_defeats"));
				
				player.setHasWarehouseAccount(rset.getInt("haswhacc") == 1);
				player.setWarehouseAccountId(rset.getString("whaccid"));
				player.setWarehouseAccountPwd(rset.getString("whaccpwd"));
				
				int clanId = rset.getInt("clanid");
				
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie"));
				player.setNoble(rset.getInt("nobless") == 1);
				
				String title = rset.getString("title");
				if (title == null)
				{
					player.setTitle(" ");
				}
				else
				{
					player.setTitle(title);
				}
				
				player.setIsBanned(rset.getBoolean("isBanned"));
				
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());
				
				currentHp = rset.getDouble("curHp");
				currentMp = rset.getDouble("curMp");
				currentCp = rset.getDouble("curCp");
				
				player.setLastRecomUpdate(rset.getLong("last_recom_date"));
				player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));
				
				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
				}
				
				if (restoreSubClassData(player))
					if (activeClassId != player.getBaseClass())
					{
						for (SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
							{
								player._classIndex = subClass.getClassIndex();
							}
					}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					player.setClassId(player.getBaseClass());
					_log.warn("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player._activeClass = activeClassId;
				}
				
				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setInJail(rset.getInt("in_jail") == 1);
				player.setJailTimer(rset.getLong("jail_timer"));
				if (player.isInJail())
				{
					player.setJailTimer(rset.getLong("jail_timer"));
				}
				else
				{
					player.setJailTimer(0);
				}
				
				CursedWeaponsManager.getInstance().onEnter(player);
				
				player.setNoble(rset.getBoolean("nobless"));
				player.setPledgeType(rset.getInt("subpledge"));
				player.setPledgeRank(rset.getInt("pledge_rank"));
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPledgeRank() == 0)
						{
							player.setPledgeRank(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPledgeRank()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPledgeRank(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.getPosition().setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
				PreparedStatement stmt = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
				{
					Integer charId = chars.getInt("charId");
					String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}
				chars.close();
				stmt.close();
			}
			
			rset.close();
			statement.close();
			
			if (player == null)
				return null;
			
			player.restoreCharData(con);
			player.rewardSkills();
			player.loadSetting(con);
			
			if (Config.STORE_SKILL_COOLTIME)
			{
				player.restoreEffects();
			}
			
			if (player.getAllEffects() != null)
			{
				for (L2Effect e : player.getAllEffects())
					if (e.getEffectType() == L2EffectType.HEAL_OVER_TIME)
					{
						player.stopEffects(L2EffectType.HEAL_OVER_TIME);
						player.removeEffect(e);
					}
					else if (e.getEffectType() == L2EffectType.COMBAT_POINT_HEAL_OVER_TIME)
					{
						player.stopEffects(L2EffectType.COMBAT_POINT_HEAL_OVER_TIME);
						player.removeEffect(e);
					}
			}
			
			player.getStatus().setCurrentCp(currentCp);
			player.getStatus().setCurrentHp(currentHp);
			player.getStatus().setCurrentMp(currentMp);
			
			if (currentHp < 0.5)
			{
				player.setIsDead(true);
				player.getStatus().stopHpMpRegeneration();
			}
			
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
			{
				player.getPet().setOwner(player);
			}
			
			player.refreshOverloaded();
			player.refreshExpertisePenalty();
			player.setUptime(System.currentTimeMillis());
			player.loadVariables();
		}
		catch (Exception e)
		{
			_log.error("Failed loading character.", e);
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return player;
	}
	
	private static boolean restoreSubClassData(L2PcInstance player)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());
			
			ResultSet rset = statement.executeQuery();
			int i = 1;
			while (rset.next())
			{
				SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));
				
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
				i++;
				if (i > Config.MAX_SUBCLASS)
				{
					break;
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore classes for " + player.getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return true;
	}
	
	public void loadVariables()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement offline = null;
		ResultSet rs = null;
		try
		{
			con = DBFactory.getInstance().getConnection();
			offline = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			offline.setInt(1, getObjectId());
			rs = offline.executeQuery();
			while (rs.next())
			{
				final String name = rs.getString("name");
				final String value = Strings.stripSlashes(rs.getString("value"));
				user_variables.put(name, value);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, offline, rs);
		}
	}
	
	public void setVar(final String name, final String value)
	{
		user_variables.put(name, value);
		mysql.set("REPLACE INTO character_variables  (obj_id, type, name, value, expire_time) VALUES (" + _objectId + ",'user-var','" + Strings.addSlashes(name) + "','" + Strings.addSlashes(value) + "',-1)");
	}
	
	public int _bbsMultisell = 0;
	public boolean _inWorld = false;
	public boolean _inSepulture = false;
	private boolean _showTraders = true;
	
	private boolean _showBuffAnimation = true;
	
	private boolean _GmStatus = false;
	
	private boolean _AllowFixRes = false;
	
	private boolean _AllowAltG = false;
	
	private boolean _AllowPeaceAtk = false;
	private boolean _isBanned = false;
	private int _dmKills = 0;
	public int[] _seeds = new int[3];
	public int _lastSkill = 0;
	private final StatsSet _dynaicData = new StatsSet();
	protected L2GameClient _client;
	private final PcAppearance _appearance;
	protected boolean _protectedSitStand = false;
	private long _expBeforeDeath;
	private int _karma;
	
	private int _pvpKills;
	private int _pkKills;
	private byte _pvpFlag;
	private byte _siegeState = 0;
	private int _lastCompassZone;
	private boolean _isIn7sDungeon = false;
	
	private int _pledgeType = 0;
	private int _pledgeRank;
	private int _lvlJoinedAcademy = 0;
	private int _curWeightPenalty = 0;
	private long _deleteTimer;
	private PcInventory _inventory;
	private PcWarehouse _warehouse;
	private PcFreight _freight;
	private boolean _waitTypeSitting;
	private boolean _relax;
	private boolean _inBoat;
	private int _questNpcObject = 0;
	private int _newbie;
	private final Map<String, QuestState> _quests = new SingletonMap<>();
	private ShortCuts _shortCuts;
	private MacroList _macroses;
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private L2ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;
	private List<L2PcInstance> _snoopListener;
	private List<L2PcInstance> _snoopedPlayer;
	
	private long _offlineShopStart = 0L;
	
	private List<L2ItemInstance> _skillsForBuffShop;
	private final Map<Integer, int[]> _buffShopSellList = new HashMap<>();
	
	private boolean _isBuffShop = false;
	
	private boolean _isBizy = false;
	
	private int _privateBuffShopLimit = Config.DEFAULT_BUFFSHOP_SLOTS;
	
	private int _privatestore;
	private ClassId _skillLearningClassId;
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	private boolean _isRidingStrider = false;
	
	private boolean _isRidingRedStrider = false;
	
	private L2Summon _summon = null;
	private L2Decoy _decoy = null;
	private L2TamedBeastInstance _tamedBeast = null;
	
	private L2Radar _radar;
	private boolean _lookingForParty;
	
	private boolean _partyMatchingAllLevels;
	private int _partyMatchingRegion;
	private L2PartyRoom _partyRoom;
	
	private L2Party _party;
	private int _clanId;
	private L2Clan _clan;
	
	private int _apprentice = 0;
	private int _sponsor = 0;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private long _onlineTime;
	private long _onlineBeginTime;
	
	private boolean _messageRefusal = false;
	
	private boolean _dietMode = false;
	
	private boolean _tradeRefusal = false;
	
	private boolean _exchangeRefusal = false;
	public L2PcInstance _activeRequester;
	
	public long _requestExpireTime = 0;
	
	private L2Request _request;
	private L2ItemInstance _arrowItem;
	protected long TOGGLE_USE = 0;
	private long _protectEndTime = 0;
	private long _recentFakeDeathEndTime = 0;
	
	private L2Weapon _fistsWeaponItem;
	
	private long _uptime;
	
	private final String _accountName;
	
	private final Map<Integer, String> _chars = new SingletonMap<>();
	
	private final Map<Integer, L2RecipeList> _dwarvenRecipeBook = new HashMap<>();
	private final Map<Integer, L2RecipeList> _commonRecipeBook = new HashMap<>();
	
	private int _mountType;
	private int _mountNpcId;
	private int _mountLevel;
	private int _expertiseIndex;
	
	private int _itemExpertiseIndex;
	
	private int _expertisePenalty = 0;
	
	private boolean _isEnchanting = false;
	private L2ItemInstance _activeEnchantItem = null;
	private byte _isOnline = ONLINE_STATE_LOADED;
	
	protected boolean _inventoryDisable = false;
	protected Map<Integer, L2CubicInstance> _cubics = new SingletonMap<Integer, L2CubicInstance>().setShared();
	private L2NpcInstance _lastFolkNpc = null;
	protected final Map<Integer, Integer> _activeSoulShots = new SingletonMap<Integer, Integer>().setShared();
	private int _clanPrivileges = 0;
	private int _pledgeClass = 0;
	private int _obsX;
	
	private int _obsY;
	private int _obsZ;
	private boolean _observerMode = false;
	private int _observMode = 0;
	
	private int _olyDamage = 0;
	
	public int _telemode = 0;
	private final int _loto[] = new int[5];
	private final int _race[] = new int[2];
	private BlockList _blockList;
	
	private L2FriendList _friendList;
	private boolean _fishing = false;
	private int _fishx = 0;
	private int _fishy = 0;
	private int _fishz = 0;
	
	private boolean _wantsPeace;
	private int _deathPenaltyBuffLevel = 0;
	
	private boolean _charmOfCourage = false;
	private boolean _canUseCharmOfCourageRes = true;
	private boolean _canUseCharmOfCourageItem = true;
	private boolean _hero = false;
	
	private boolean _noble = false;
	
	private boolean _inOlympiadMode = false;
	
	private boolean _olympiadStart = false;
	
	private int _olympiadGameId = -1;
	
	private int _olympiadSide = -1;
	
	private int _olympiadOpponentId = 0;
	
	// Tournament
	public int duelist_cont = 0, dreadnought_cont = 0, tanker_cont = 0, dagger_cont = 0, archer_cont = 0, bs_cont = 0, archmage_cont = 0, soultaker_cont = 0, mysticMuse_cont = 0, stormScreamer_cont = 0, titan_cont = 0, grandKhauatari_cont = 0, dominator_cont = 0, doomcryer_cont = 0;
	
	private int _duelState = Duel.DUELSTATE_NODUEL;
	
	private boolean _isInDuel = false;
	
	private int _duelId = 0;
	
	private int _noDuelReason = 0;
	private int _alliedVarkaKetra = 0;
	private Map<Integer, SubClass> _subClasses = new LinkedHashMap<>();
	
	protected int _baseClass;
	protected int _activeClass;
	public boolean verSkin;
	
	protected int _classIndex = 0;
	
	private int _controlItemId;
	
	private L2PetData _data;
	private int _curFeed;
	
	protected Future<?> _mountFeedTask;
	private ScheduledFuture<?> _dismountTask;
	private long _lastAccess;
	private int _boatId;
	private ScheduledFuture<?> _taskRentPet;
	private ScheduledFuture<?> _taskWater;
	private L2BoatInstance _boat;
	private Point3D _inBoatPosition;
	private L2Fishing _fishCombat;
	
	private Point3D _lastServerPosition;
	
	private Point3D _lastPartyPosition;
	
	private int _recomHave;
	private int _recomLeft;
	
	private long _lastRecomUpdate;
	
	private final List<Integer> _recomChars = new SingletonList<>();
	
	private boolean _inCrystallize;
	
	private boolean _inCraftMode;
	private boolean _isSummoning;
	
	private Forum _forumMail;
	
	private Forum _forumMemo;
	private SkillDat _currentSkill;
	private SkillDat _currentPetSkill;
	
	private SkillDat _queuedSkill;
	
	private int _mountObjectID = 0;
	
	private boolean _inJail = false;
	
	private long _jailTimer = 0;
	
	private boolean _maried = false;
	
	private int _partnerId = 0;
	
	private int _coupleId = 0;
	
	private boolean _engagerequest = false;
	
	private int _engageid = 0;
	
	private boolean _maryrequest = false;
	
	private boolean _maryaccepted = false;
	
	private int _clientRevision = 0;
	
	public boolean _buffBlocked;
	
	private boolean _IsWearingFormalWear = false;
	
	private L2StaticObjectInstance _objectSittingOn;
	
	private int _charges = 0;
	
	private ScheduledFuture<?> _chargeTask = null;
	
	private Point3D _currentSkillWorldPosition;
	
	private boolean _miniMapOpen;
	
	private final List<String> _userMessages = new ArrayList<>();
	
	private final List<String> _messageQueue = new ArrayList<>();
	
	private final int _queueDepth = 5 + Rnd.get(3);
	
	private final StatsSet _characterData = new StatsSet();
	
	private int _pccaffe;
	
	public int _lastUseItem;
	
	private ScheduledFuture<?> _shortBuffTask = null;
	
	private boolean _sitWhenArrived;
	
	public boolean _clanLeader = false;
	
	public long _lastStore = 0;
	
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	
	private ScheduledFuture<?> _taskSkillRewardTime;
	
	protected ScheduledFuture<?> _onCounterTask = null;
	protected int _OnTimer = 5 * 60;
	
	public ScheduledFuture<?> _taskforfish;
	
	private int _baseLevel;
	
	private long _baseExp;
	
	private int _baseSP;
	
	private FishData _fish;
	
	public L2ItemInstance _lure = null;
	
	private long _skillQueueProtectionTime = 0;
	
	private ScheduledFuture<?> _jailTask;
	
	private int _cursedWeaponEquippedId = 0;
	
	private boolean _combatFlagEquipped = false;
	
	private boolean _reviveRequested = false;
	
	private double _revivePower = 0;
	
	private boolean _revivePetRequested = false;
	
	private double _revivePetPower = 0;
	
	private double _cpUpdateIncCheck = .0;
	
	private double _cpUpdateDecCheck = .0;
	
	private double _cpUpdateInterval = .0;
	
	private double _mpUpdateIncCheck = .0;
	
	private double _mpUpdateDecCheck = .0;
	
	private double _mpUpdateInterval = .0;
	
	private boolean _canFeed;
	
	private final Map<Integer, TimeStamp> _reuseTimeStamps = new SingletonMap<Integer, TimeStamp>().setShared();
	
	private ImmutableReference<L2PcInstance> _immutableReference;
	
	private ClearableReference<L2PcInstance> _clearableReference;
	
	private L2PcInstance _summonRequestTarget;
	
	private L2Skill _summonRequestSkill = null;
	
	private L2DoorInstance _gatesRequestTarget = null;
	
	private ConditionListener[] _conditionListeners;
	
	private long[] _floodCount = null;
	
	private boolean _trading = false;
	
	private StatsSet _fakeAccData = null;
	
	private long _lastPetCheck;
	
	private final String[] LAST_BBS_PAGE = new String[2];
	
	private final Map<Integer, Long> confirmDlgRequests = new ConcurrentHashMap<>();
	
	private L2PetInstance targetPet;
	
	private boolean _isOfflineTrade = false;
	
	private long _endOfflineTime = 0;
	
	private boolean _isPartyInvProt = false;
	private static L2ItemInstance item;
	
	public boolean hasWarehouseAccount = false;
	public String warehouseAccountId, warehouseAccountPwd;
	
	private boolean _isVip = false;
	private long _vip_endTime = 0;
	
	private boolean _isAio = false;
	private long _aio_endTime = 0;
	
	private int _arenaWins;
	private int _arenaDefeats;
	
	private boolean _inArenaEvent = false;
	
	private int _allowed;
	private FastMap<String, String> user_variables = new FastMap<>();
	
	// Multisell
	/** The _current multi sell id. */
	private int _currentMultiSellId = -1;
	
	private final List<String> _validBypass = new ArrayList<>();
	
	private final List<String> _validBypass2 = new ArrayList<>();
	
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		getStat();
		getStatus();
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;
		
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
		
		getInventory().restore();
		getWarehouse();
		getFreight();
		
	}
	
	public void academyCheck(int Id)
	{
		if ((getPledgeType() == -1 || getLvlJoinedAcademy() != 0) && _clan != null && PlayerClass.values()[Id].getLevel() == ClassLevel.Third && !isSubClassActive())
		{
			if (getLvlJoinedAcademy() <= 16)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 400, true);
				_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.MEMBER_GRADUATED_EARNED_S1_REPU).addNumber(400));
			}
			else if (getLvlJoinedAcademy() >= 39)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 170, true);
				_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.MEMBER_GRADUATED_EARNED_S1_REPU).addNumber(170));
			}
			else
			{
				_clan.setReputationScore(_clan.getReputationScore() + 400 - (getLvlJoinedAcademy() - 16) * 10, true);
				_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.MEMBER_GRADUATED_EARNED_S1_REPU).addNumber(400 - (getLvlJoinedAcademy() - 16) * 10));
			}
			if (_clan == null)
				return;
			setLvlJoinedAcademy(0);
			_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
			_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(getName()));
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
			
			_clan.removeClanMember(getObjectId(), 0);
			_clan = null;
			
			sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			getInventory().addItem("Gift", 8181, 1, this, null);
		}
	}
	
	public void addAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > 0)
		{
			if (_inventory.getAdena() == Integer.MAX_VALUE)
			{
				sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				return;
			}
			else if (_inventory.getAdena() >= Integer.MAX_VALUE - count)
			{
				count = Integer.MAX_VALUE - _inventory.getAdena();
				_inventory.addAdena(process, count, this, reference);
			}
			else if (_inventory.getAdena() < Integer.MAX_VALUE - count)
			{
				_inventory.addAdena(process, count, this, reference);
			}
			if (sendMessage)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));
			}
			
			getInventory().updateInventory(getInventory().getAdenaInstance());
		}
	}
	
	public void addAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(count));
		}
		
		if (count > 0)
		{
			getInventory().addAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
		}
	}
	
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.put(itemId, itemId);
	}
	
	public synchronized void addBypass(String bypass)
	{
		if (bypass == null)
			return;
		
		_validBypass.add(bypass);
	}
	
	public synchronized void addBypass2(String bypass)
	{
		if (bypass == null)
			return;
		
		_validBypass2.add(bypass);
	}
	
	public void addConfirmDlgRequestTime(int requestId, int time)
	{
		confirmDlgRequests.put(requestId, System.currentTimeMillis() + time + 2000);
	}
	
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifeTime)
	{
		L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, totalLifeTime);
		
		_cubics.put(id, cubic);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (canGainExp())
		{
			getStat().addExpAndSp(addToExp, addToSp);
		}
	}
	
	public boolean addHenna(L2HennaInstance henna)
	{
		for (int i = 0; i < 3; i++)
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				recalcHennaStats();
				
				Connection con = null;
				
				broadcastFullInfo();
				
				try
				{
					broadcastFullInfo();
					con = L2DatabaseFactory.getInstance().getConnection(con);
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Failed saving character henna.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
					broadcastFullInfo();
				}
				broadcastFullInfo();
				sendPacket(new HennaInfo(this));
				sendPacket(new UserInfo(this));
				
				return true;
			}
		
		return false;
	}
	
	public boolean addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		return addItem(process, itemId, count, reference, sendMessage, true) != null;
	}
	
	public L2ItemInstance addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (count > 0)
		{
			L2ItemInstance newItem = getInventory().addItem(process, itemId, count, this, reference);
			
			if (sendMessage)
			{
				sendMessageForNewItem(newItem, count, process);
			}
			
			processAddItem(UpdateIL, newItem);
			return newItem;
		}
		return null;
	}
	
	public boolean addItem(String process, int[] itemsId, int[] counts, L2Object reference, boolean sendMessage)
	{
		if (itemsId.length == 0 || itemsId.length != counts.length)
			return false;
		for (int i = 0; i < itemsId.length; i++)
			if (addItem(process, itemsId[i], counts[i], reference, sendMessage, true) == null)
				return false;
		return true;
	}
	
	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		addItem(process, item, reference, sendMessage, true);
	}
	
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (item.getCount() > 0)
		{
			if (sendMessage)
				if (item.getCount() > 1)
				{
					if (item.isStackable() && !Config.MULTIPLE_ITEM_DROP)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item.getItemId()));
					}
					else
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(item.getItemId()).addNumber(item.getCount()));
					}
				}
				
				else if (item.getEnchantLevel() > 0)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item.getItemId()));
				}
			
			L2ItemInstance newitem = getInventory().addItem(process, item, this, reference);
			
			processAddItem(UpdateIL, newitem);
			return newitem;
		}
		return null;
	}
	
	public void addMessage(String msg)
	{
		_userMessages.add(msg);
	}
	
	public void addOlyDamage(int dmg)
	{
		_olyDamage = _olyDamage + dmg;
	}
	
	public void addPacket(String packetClass)
	{
		synchronized (_messageQueue)
		{
			if (_messageQueue.size() >= _queueDepth)
			{
				_messageQueue.remove(0);
			}
			_messageQueue.add(packetClass);
		}
	}
	
	private void addSkill(Connection con, L2Skill newSkill, int classIndex) throws SQLException
	{
		PreparedStatement statement;
		statement = con.prepareStatement(ADD_NEW_SKILL);
		statement.setInt(1, getObjectId());
		statement.setInt(2, newSkill.getId());
		statement.setInt(3, newSkill.getLevel());
		statement.setString(4, newSkill.getName());
		statement.setInt(5, classIndex);
		try
		{
			statement.execute();
		}
		finally
		{
			statement.close();
		}
	}
	
	@Override
	public L2Skill addSkill(L2Skill newSkill)
	{
		return addSkill(newSkill, false);
	}
	
	public L2Skill addSkill(L2Skill newSkill, boolean save)
	{
		L2Skill oldSkill = super.addSkill(newSkill);
		
		if (save)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		
		return oldSkill;
	}
	
	public void addSnooped(L2PcInstance pci)
	{
		if (_snoopedPlayer == null)
		{
			_snoopedPlayer = new SingletonList<>();
		}
		
		if (!_snoopedPlayer.contains(pci))
		{
			_snoopedPlayer.add(pci);
			sendPacket(new Snoop(pci.getObjectId(), pci.getName(), 0, "", "*** Starting Snoop for " + pci.getName() + " ***"));
		}
	}
	
	public void addSnooper(L2PcInstance pci)
	{
		if (_snoopListener == null)
		{
			_snoopListener = new SingletonList<>();
		}
		
		if (!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}
	
	public boolean addSubClass(int classId, int classIndex)
	{
		if (getTotalSubClasses() == Config.MAX_SUBCLASS || classIndex == 0)
			return false;
		
		if (getSubClasses().containsKey(classIndex))
			return false;
		
		store(true);
		SubClass newClass = new SubClass();
		newClass.setClassId(classId);
		newClass.setClassIndex(classIndex);
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, newClass.getSp());
			statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("WARNING: Could not add character sub class for " + getName() + ": " + e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		getSubClasses().put(newClass.getClassIndex(), newClass);
		
		ClassId subTemplate = ClassId.values()[classId];
		Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
		
		if (skillTree == null)
			return true;
		
		final Map<Integer, L2Skill> prevSkillList = new LinkedHashMap<>();
		
		for (L2SkillLearn skillInfo : skillTree)
			if (skillInfo.getMinLevel() <= 40)
			{
				L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
				L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());
				
				if (prevSkill != null && prevSkill.getLevel() >= newSkill.getLevel())
				{
					continue;
				}
				
				if (newSkill != null)
				{
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
				else
				{
					_log.info("L2PcInstance: Skill " + skillInfo.getId() + " not found for character " + getName() + " (" + getClassId() + ")");
				}
			}
		return true;
	}
	
	@Override
	public void addTimeStamp(int s, int r)
	{
		_reuseTimeStamps.put(s, new TimeStamp(s, r));
	}
	
	public void addTimeStamp(TimeStamp ts)
	{
		_reuseTimeStamps.put(ts.getSkill(), ts);
	}
	
	public boolean allowAltG()
	{
		if (_GmStatus && _AllowAltG)
			return true;
		return false;
	}
	
	public boolean allowFixedRes()
	{
		if (_GmStatus && _AllowFixRes)
			return true;
		return false;
	}
	
	public boolean allowPeaceAttack()
	{
		if (_GmStatus && _AllowPeaceAtk)
			return true;
		return false;
	}
	
	public boolean banChar()
	{
		try
		{
			setIsBanned(true);
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_YOU_ARE_BANNED));
			if (isOfflineTrade())
			{
				setOfflineTrade(false);
				standUp();
			}
			new Disconnection(this).defaultSequence(false);
		}
		catch (Exception e)
		{
			_log.info("Could't ban player: " + getName() + ". Error: ", e);
			return false;
		}
		return true;
	}
	
	public void broadcastClassIcon()
	{
		if (isInParty())
		{
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		}
		
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		
		if (_inWorld)
		{
			sendPacket(new UserInfo(this));
		}
		
		Broadcast.toKnownPlayers(this, new CharInfo(this));
	}
	
	public void broadcastKarma()
	{
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		broadcastUserInfo();
	}
	
	public void broadcastRelationChanged()
	{
		broadcastRelationChangedImpl();
	}
	
	public void broadcastRelationChangedImpl()
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			RelationChanged.sendRelationChanged(this, player);
		}
	}
	
	public void broadcastSnoop(int type, String name, String _text)
	{
		if (_snoopListener == null)
			return;
		
		for (L2PcInstance pci : _snoopListener)
		{
			pci.sendPacket(new Snoop(getObjectId(), getName(), type, name, _text));
		}
	}
	
	@Override
	public final void broadcastStatusUpdateImpl()
	{
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getStatus().getCurrentCp());
		broadcastPacket(su);
		
		if (isInParty() && (needHpUpdate(352) || needMpUpdate(352) || needCpUpdate(352)))
		{
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
		
		if (isInOlympiadMode())
		{
			for (L2PcInstance player : getKnownList().getKnownPlayers().values())
				if (player.getOlympiadGameId() == getOlympiadGameId() && player.isOlympiadStart())
				{
					player.sendPacket(new ExOlympiadUserInfo(this, 1));
				}
			if (isOlympiadStart())
			{
				final List<L2PcInstance> spectators = Olympiad.getSpectators(getOlympiadGameId());
				
				if (spectators != null && !spectators.isEmpty())
				{
					final ExOlympiadUserInfo eoui = new ExOlympiadUserInfo(this);
					
					for (L2PcInstance spectator : spectators)
					{
						spectator.sendPacket(eoui);
					}
				}
			}
		}
		if (isInDuel())
		{
			DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
		}
	}
	
	public final void broadcastTitleInfo()
	{
		sendPacket(new UserInfo(this));
		broadcastPacket(new TitleUpdate(this));
	}
	
	public final void broadcastUserInfo()
	{
		sendPacket(new UserInfo(this));
		Broadcast.toKnownPlayers(this, new CharInfo(this));
		broadcastFullInfo();
	}
	
	public void calcExpertiseLevel()
	{
		int lvl = getLevel();
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setExpertiseIndex(i);
			}
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setItemExpertiseIndex(i);
			}
		}
	}
	
	public void calculateDeathPenaltyBuffLevel(L2Character killer)
	{
		if ((getKarma() > 0 || Rnd.get(1, 100) <= Config.DEATH_PENALTY_CHANCE) && !(killer instanceof L2PcInstance) && !isGM() && !(getCharmOfLuck() && !isPhoenixBlessed() && (killer instanceof L2GrandBossInstance || killer instanceof L2RaidBossInstance)) && !(isInsideZone(L2Zone.FLAG_PVP) || isInsideZone(L2Zone.FLAG_SIEGE)))
		{
			increaseDeathPenaltyBuffLevel();
		}
	}
	
	public int calculateKarmaLost(long exp)
	{
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
		{
			karmaLost = Integer.MAX_VALUE;
		}
		else
		{
			karmaLost = (int) expGained;
		}
		
		if (karmaLost < Config.KARMA_LOST_BASE)
		{
			karmaLost = Config.KARMA_LOST_BASE;
		}
		if (karmaLost > getKarma())
		{
			karmaLost = getKarma();
		}
		
		return karmaLost;
	}
	
	public boolean canBeTargetedByAtSiege(L2PcInstance player)
	{
		Siege siege = SiegeManager.getSiege(this);
		if (siege != null && siege.getIsInProgress())
		{
			L2Clan selfClan = getClan();
			L2Clan oppClan = player.getClan();
			if (selfClan != null && oppClan != null)
			{
				boolean self = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}
				
				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}
				
				boolean opp = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
				
				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());
					
					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}
				
				return self && opp;
			}
			
			return false;
		}
		
		return true;
	}
	
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;
		setTrading(false);
		setActiveEnchantItem(null);
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null)
		{
			partner.onTradeCancel(this);
		}
		onTradeCancel(this);
	}
	
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE.getId();
			return false;
		}
		if (isDead() || isAlikeDead() || getStatus().getCurrentHp() < getStat().getMaxHp() / 2 || getStatus().getCurrentMp() < getStat().getMaxMp() / 2)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT.getId();
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL.getId();
			return false;
		}
		if (isInOlympiadMode() || Olympiad.isRegistered(this) || getOlympiadGameId() != -1)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD.getId();
			return false;
		}
		if (isCursedWeaponEquipped())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE.getId();
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE.getId();
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER.getId();
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING.getId();
			return false;
		}
		if (isInsideZone(L2Zone.FLAG_PVP) || isInsideZone(L2Zone.FLAG_PEACE) || SiegeManager.checkIfInZone(this))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA.getId();
			return false;
		}
		return true;
	}
	
	public boolean canGainExp()
	{
		try
		{
			return _characterData.getBool("CanGainExp");
		}
		catch (IllegalArgumentException e)
		{
			canGainExp(true);
			return true;
		}
	}
	
	public void canGainExp(boolean b)
	{
		_characterData.set("CanGainExp", b);
		
	}
	
	protected boolean canInteract(L2PcInstance player)
	{
		if (!isInsideRadius(player, 50, false, false))
			return false;
		
		return true;
	}
	
	public boolean canLogout()
	{
		if (!isGM())
		{
			if (isInsideZone(L2Zone.FLAG_NOESCAPE))
			{
				sendPacket(SystemMessageId.NO_LOGOUT_HERE);
				return false;
			}
			if (isInsideZone(L2Zone.FLAG_NORESTART))
			{
				sendPacket(SystemMessageId.NO_LOGOUT_HERE);
				return false;
			}
			if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this))
			{
				sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
				return false;
			}
		}
		
		getInventory().updateDatabase();
		getWarehouse().updateDatabase();
		
		if (isFlying())
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		
		L2Summon summon = getPet();
		
		if (summon != null && summon instanceof L2PetInstance && !summon.isBetrayed() && summon.isAttackingNow())
		{
			sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
			return false;
		}
		if (isInFunEvent())
		{
			if (!_event.canLogout(this))
			{
				sendMessage(Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
				return false;
			}
		}
		if (isInArenaEvent())
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		if (isInOlympiadMode() || Olympiad.isRegistered(this) || getOlympiadGameId() != -1)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		if (isFestivalParticipant())
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		
		if (getPrivateStoreType() != 0)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		
		if (getActiveEnchantItem() != null)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_EXIT));
			return false;
		}
		
		if (isInStoreMode() && Config.ALLOW_OFFLINE_TRADE || isInCraftMode() && Config.ALLOW_OFFLINE_TRADE_CRAFT)
			return false;
		
		getInventory().updateDatabase();
		getWarehouse().updateDatabase();
		
		return true;
	}
	
	public boolean canOpenPrivateStore()
	{
		return !isAlikeDead() && !isInOlympiadMode() && !isMounted();
	}
	
	public boolean canRecom(L2PcInstance target)
	{
		return !_recomChars.contains(target.getObjectId());
	}
	
	public boolean canRegisterToEvents()
	{
		if (isInOlympiadMode() || Olympiad.isRegistered(this))
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_BAD_CONDITIONS));
			return false;
		}
		if (isInJail() || isInsideZone(L2Zone.FLAG_JAIL))
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_BAD_CONDITIONS));
			return false;
		}
		if (getKarma() > 0)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_BAD_CONDITIONS));
			return false;
		}
		if (_event != null)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_BAD_CONDITIONS));
			return false;
		}
		
		return true;
	}
	
	public boolean canSee(L2Character cha)
	{
		if (cha instanceof L2Decoy)
			return true;
		final L2PcInstance player = cha.getActingPlayer();
		
		if (player != null)
		{
			if (player.inObserverMode())
				return false;
			if (isGM())
				return true;
			if (player.getAppearance().isInvisible())
				return false;
		}
		return true;
	}
	
	public void changeName(String name)
	{
		String oldName = getName();
		super.setName(name);
		CharNameTable.getInstance().update(getObjectId(), getName(), oldName);
	}
	
	@SuppressWarnings("null")
	public void checkAllowedSkills()
	{
		if (isGM())
			return;
		
		Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(getClassId());
		
		int Count = 0;
		
		skill_loop:
		for (L2Skill skill : getAllSkills())
		{
			if (skill == null)
			{
				continue skill_loop;
			}
			int skillid = skill.getId();
			
			for (L2SkillLearn sk1 : skillTree)
				if (sk1.getId() == skillid)
				{
					continue skill_loop;
				}
			if (isNoble() && NobleSkillTable.isNobleSkill(skillid))
			{
				continue skill_loop;
			}
			if (isHero() && HeroSkillTable.isHeroSkill(skillid))
			{
				continue skill_loop;
			}
			if (isCursedWeaponEquipped() && skillid == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).getSkillId())
			{
				continue skill_loop;
			}
			if (getClan() != null && skillid >= 370 && skillid <= 391)
			{
				continue skill_loop;
			}
			if (getClan() != null && getClan().getLeaderId() == getObjectId() && (skillid == 246 || skillid == 247))
			{
				continue skill_loop;
			}
			if (skillid >= 1312 && skillid <= 1322)
			{
				continue skill_loop;
			}
			if (skillid >= 1368 && skillid <= 1373)
			{
				continue skill_loop;
			}
			if (skillid >= 3000 && skillid < 7000)
			{
				continue skill_loop;
			}
			if (skillid >= 8193 && skillid < 8233)
			{
				continue skill_loop;
			}
			if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
			{
				continue skill_loop;
			}
			
			if (skill != null)
			{
				removeSkill(skill);
				Count++;
			}
		}
		
		if (Count > 0)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", String.format(Message.getMessage(this, Message.MessageId.MSG_SKILL_REMOVED_ADMIN_INFORMED), Count));
		}
	}
	
	@Override
	protected boolean checkAndEquipArrows()
	{
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (_arrowItem != null)
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				getInventory().updateInventory(_arrowItem);
			}
		}
		else
		{
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
	public boolean checkCanLand()
	{
		if (isInsideZone(L2Zone.FLAG_NOLANDING))
			return false;
		
		return !(SiegeManager.checkIfInZone(this) && !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan().getLeader().getPlayerInstance()));
	}
	
	public void checkInventory()
	{
		for (L2ItemInstance item : getInventory().getItems())
			if (item.isEquipped() && item.getItem() instanceof L2Armor)
			{
				getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
	}
	
	public L2ItemInstance checkItemManipulation(int objectId, int count, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if (count < 0 || count > 1 && !item.isStackable())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
			return null;
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return null;
		
		if (item.isWear())
			return null;
		
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
			return null;
		
		return item;
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if (equippedItem != null && !equippedItem.getItem().checkCondition(this, this, false))
			{
				getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
				{
					continue;
				}
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(equippedItem.getEnchantLevel()).addItemName(equippedItem);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(equippedItem);
				}
				sendPacket(sm);
			}
		}
	}
	
	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return getLastServerPosition().equals(x, y, z);
	}
	
	public boolean checkPacket(String[] packetClass)
	{
		if (packetClass == null)
			return true;
		synchronized (_messageQueue)
		{
			for (String s : packetClass)
			{
				String[] packets = s.split(",");
				boolean isOk = true;
				for (String pkt : packets)
					if (!_messageQueue.contains(pkt))
					{
						isOk = false;
						break;
					}
				if (isOk)
					return true;
			}
		}
		return false;
	}
	
	public boolean checkPvpSkill(L2Object obj, L2Skill skill)
	{
		return checkPvpSkill(obj, skill, false);
	}
	
	public boolean checkPvpSkill(L2Object obj, L2Skill skill, boolean srcIsSummon)
	{
		if (obj != this && obj instanceof L2PcInstance && !(isInDuel() && ((L2PcInstance) obj).getDuelId() == getDuelId()) && !isInsideZone(L2Zone.FLAG_PVP) && !((L2PcInstance) obj).isInsideZone(L2Zone.FLAG_PVP))
		{
			L2PcInstance target = (L2PcInstance) obj;
			if (skill.isPvpSkill())
			{
				if (getClan() != null && target.getClan() != null)
					if (getClan().isAtWarWith(target.getClan().getClanId()) && target.getClan().isAtWarWith(getClan().getClanId()))
						return true;
				if (target.getPvpFlag() == 0 && target.getKarma() == 0)
					return false;
			}
			else if (getCurrentSkill() != null && !getCurrentSkill().isCtrlPressed() && skill.isOffensive() && !srcIsSummon || getCurrentPetSkill() != null && !getCurrentPetSkill().isCtrlPressed() && skill.isOffensive() && srcIsSummon)
			{
				if (getClan() != null && target.getClan() != null)
					if (getClan().isAtWarWith(target.getClan().getClanId()) && target.getClan().isAtWarWith(getClan().getClanId()))
						return true;
				if (target.getPvpFlag() == 0 && target.getKarma() == 0)
					return false;
			}
		}
		return true;
	}
	
	public void checkRecom(int recsHave, int recsLeft)
	{
		Calendar check = Calendar.getInstance();
		if (_lastRecomUpdate == 0)
		{
			restartRecom();
		}
		else
		{
			_recomHave = recsHave;
			_recomLeft = recsLeft;
		}
		check.setTimeInMillis(_lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		check.set(Calendar.HOUR_OF_DAY, 13);
		check.set(Calendar.MINUTE, 0);
		check.set(Calendar.SECOND, 0);
		check.set(Calendar.MILLISECOND, 0);
		
		Calendar min = Calendar.getInstance();
		
		if (getStat().getLevel() < 10)
			return;
		
		while (!check.after(min))
		{
			check = restartRecom();
		}
	}
	
	public void checkSSMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
	{
		if (unequipped == null)
			return;
		
		if (unequipped.getItem().getType2() == L2Item.TYPE2_WEAPON && (equipped == null ? true : equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType()))
		{
			for (L2ItemInstance ss : getInventory().getItems())
			{
				int _itemId = ss.getItemId();
				
				if ((_itemId >= 2509 && _itemId <= 2514 || _itemId >= 3947 && _itemId <= 3952 || _itemId <= 1804 && _itemId >= 1808 || _itemId == 5789 || _itemId == 5790 || _itemId == 1835) && ss.getItem().getCrystalType() == unequipped.getItem().getCrystalType())
				{
					sendPacket(new ExAutoSoulShot(_itemId, 0));
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(ss.getItemName()));
				}
			}
		}
	}
	
	public void checkSummon()
	{
		if (!isMounted() && getPet() != null && !getPet().isOutOfControl() && !getPet().isDead() && System.currentTimeMillis() - _lastPetCheck > 5000)
		{
			if (!Util.checkIfInRange(2000, this, getPet(), true))
			{
				getPet().setFollowStatus(false);
				getPet().teleToLocation(getX(), getY(), getZ(), false);
				getPet().setFollowStatus(true);
				getPet().broadcastFullInfo();
			}
			_lastPetCheck = System.currentTimeMillis();
		}
	}
	
	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (isDead() || isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		L2SkillType sklType = skill.getSkillType();
		
		if (isFishing() && sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING)
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isSitting() && !skill.isPotion())
		{
			if (skill.isToggle())
			{
				// Get effects of the skill
				L2Effect effect = getFirstEffect(skill.getId());
				if (effect != null)
				{
					effect.exit();
					
					// Send ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isToggle())
		{
			if (skill.getId() == 60 && isMounted())
				return false;
			
			L2Effect effect = getFirstEffect(skill);
			
			if (TOGGLE_USE != 0 && TOGGLE_USE + 400 > System.currentTimeMillis())
			{
				TOGGLE_USE = 0;
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			TOGGLE_USE = System.currentTimeMillis();
			
			if (effect != null)
			{
				effect.exit();
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (isFakeDeath())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (skill.getId())
		{
			case 13:
			case 299:
			case 448:
				if (!SiegeManager.checkIfOkToSummon(this, false) && !FortSiegeManager.checkIfOkToSummon(this, false) || SevenSigns.getInstance().checkSummonConditions(this))
					return false;
		}
		
		L2Object target = null;
		SkillTargetType sklTargetType = skill.getTargetType();
		Point3D worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (sklTargetType)
		{
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CORPSE_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			case TARGET_PET:
			case TARGET_SUMMON:
				target = getPet();
				break;
			default:
				target = getTarget();
				break;
		}
		
		if (target == null)
		{
			sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isOffensive() && target instanceof L2DoorInstance)
		{
			L2DoorInstance door = (L2DoorInstance) target;
			boolean isCastleDoor = door.getCastle() != null && door.getCastle().getSiege().getIsInProgress();
			boolean isFortDoor = door.getFort() != null && door.getFort().getSiege().getIsInProgress() && !door.getIsCommanderDoor();
			if (!isCastleDoor && !isFortDoor && !(door.isUnlockable() && skill.getSkillType() == L2SkillType.UNLOCK))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				return false;
			}
		}
		
		SkillDat skilldat = getCurrentSkill();
		if (skilldat != null && skill.getSkillType() == L2SkillType.HEAL && !skilldat.isCtrlPressed() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 1 && this != target)
			if (getClanId() == 0 || ((L2PcInstance) target).getClanId() == 0 || getClanId() != ((L2PcInstance) target).getClanId())
				if (getAllyId() == 0 || ((L2PcInstance) target).getAllyId() == 0 || getAllyId() != ((L2PcInstance) target).getAllyId())
					if (getParty() == null || ((L2PcInstance) target).getParty() == null || !getParty().equals(((L2PcInstance) target).getParty()))
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				
		if (isInDuel())
			if (!(target instanceof L2PcInstance && ((L2PcInstance) target).getDuelId() == getDuelId() || target instanceof L2Summon && ((L2Summon) target).getOwner().getDuelId() == getDuelId()))
			{
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		
		if (isSkillDisabled(skill.getId()))
		{
			TimeStamp ts = _reuseTimeStamps == null ? null : _reuseTimeStamps.get(skill.getId());
			if (ts != null)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
			}
			return false;
		}
		
		if (_charges < skill.getNeededCharges())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return false;
		}
		
		if (skill.getGiveCharges() > 0 && _charges >= skill.getMaxCharges() && !skill.getContinueAfterMax())
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!skill.checkCondition(this, target))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isFishing() && sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING)
		{
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (isFlying() && !isGM())
			if (skill.getId() != 327 && skill.getId() != 4289 && !skill.isPotion())
			{
				sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "You cannot use skills while riding a wyvern.");
				return false;
			}
		
		if (sklType == L2SkillType.SUMMON && skill instanceof L2SkillSummon && !skill.isCubic())
			if (getPet() != null || isMounted())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
				return false;
			}
		
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target) && !allowPeaceAttack())
			{
				if (!skill.useAlways())
				{
					sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				forceUse = false;
			}
			
			if (isInOlympiadMode() && !isOlympiadStart() && sklTargetType != SkillTargetType.TARGET_AURA)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!target.isAttackable() && !allowPeaceAttack())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!target.isAutoAttackable(this) && !forceUse && !isInDuel())
			{
				switch (sklTargetType)
				{
					case TARGET_AURA:
					case TARGET_FRONT_AURA:
					case TARGET_BEHIND_AURA:
					case TARGET_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
					case TARGET_GROUND:
						break;
					default:
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					
				}
			}
			
			if (dontMove)
				if (sklTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
					{
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
				{
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		if (skill.isPvpSkill() && target instanceof L2Playable && sklTargetType == SkillTargetType.TARGET_ONE && !isInOlympiadMode())
		{
			boolean srcInPvP = isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_SIEGE);
			boolean targetInPvP = ((L2Playable) target).isInsideZone(L2Zone.FLAG_PVP) && !((L2Playable) target).isInsideZone(L2Zone.FLAG_SIEGE);
			boolean stop = false;
			if (target instanceof L2PcInstance)
			{
				if (getParty() != null && ((L2PcInstance) target).getParty() != null && getParty().getPartyLeaderOID() == ((L2PcInstance) target).getParty().getPartyLeaderOID())
				{
					stop = true;
				}
				if (!srcInPvP && !targetInPvP)
				{
					if (getClanId() != 0 && getClanId() == ((L2PcInstance) target).getClanId())
					{
						stop = true;
					}
					if (getAllyId() != 0 && getAllyId() == ((L2PcInstance) target).getAllyId())
					{
						stop = true;
					}
				}
			}
			else if (target instanceof L2Summon)
			{
				L2PcInstance trg = ((L2Summon) target).getOwner();
				if (trg == this)
				{
					stop = true;
				}
				if (getParty() != null && trg.getParty() != null && getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
				{
					stop = true;
				}
				if (!srcInPvP && !targetInPvP)
				{
					if (getClanId() != 0 && getClanId() == trg.getClanId())
					{
						stop = true;
					}
					if (getAllyId() != 0 && getAllyId() == trg.getAllyId())
					{
						stop = true;
					}
				}
			}
			if (stop)
			{
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (skill.getSkillType() == L2SkillType.INSTANT_JUMP)
		{
			if (isRooted())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill.getId()));
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			if (isInsideZone(L2Zone.FLAG_PEACE))
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (!skill.isOffensive() && target instanceof L2MonsterInstance && !forceUse && !skill.isNeutral())
		{
			switch (sklTargetType)
			{
				case TARGET_PET:
				case TARGET_SUMMON:
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_CLAN:
				case TARGET_SELF:
				case TARGET_PARTY:
				case TARGET_ALLY:
				case TARGET_CORPSE_MOB:
				case TARGET_AREA_CORPSE_MOB:
				case TARGET_GROUND:
					break;
				default:
					switch (sklType)
					{
						case BEAST_FEED:
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
						case GARDEN_KEY_UNLOCK:
						case MAKE_KILLABLE:
							break;
						default:
							sendPacket(ActionFailed.STATIC_PACKET);
							return false;
					}
			}
		}
		
		if (sklType == L2SkillType.SPOIL)
			if (!(target instanceof L2MonsterInstance) && !(target instanceof L2ChestInstance))
			{
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		
		if (sklType == L2SkillType.SWEEP && target instanceof L2Attackable)
		{
			int spoilerId = ((L2Attackable) target).getIsSpoiledBy();
			
			if (((L2Attackable) target).isDead())
			{
				if (!((L2Attackable) target).isSpoil())
				{
					sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				
				if (getObjectId() != spoilerId && !isInLooterParty(spoilerId))
				{
					sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if (sklType == L2SkillType.DRAIN_SOUL)
			if (!(target instanceof L2MonsterInstance))
			{
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
				break;
			default:
				if (!checkPvpSkill(target, skill) && !allowPeaceAttack())
				{
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		if (sklTargetType == SkillTargetType.TARGET_HOLY && !TakeCastle.checkIfOkToCastSealOfRule(this, false) || sklTargetType == SkillTargetType.TARGET_FLAGPOLE && !TakeFort.checkIfOkToCastFlagDisplay(this, false, skill, getTarget()) || sklType == L2SkillType.SIEGEFLAG && !SiegeManager.checkIfOkToPlaceFlag(this, false) && !FortSiegeManager.checkIfOkToPlaceFlag(this, false) || sklType == L2SkillType.STRSIEGEASSAULT && !SiegeManager.checkIfOkToUseStriderSiegeAssault(this, false) && !FortSiegeManager.checkIfOkToUseStriderSiegeAssault(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}
		
		if (skill.getCastRange() > 0)
			if (sklTargetType == SkillTargetType.TARGET_GROUND)
			{
				if (!GeoData.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (!GeoData.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		return true;
	}
	
	public void checkWaterState()
	{
		if (isInsideZone(L2Zone.FLAG_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}
	
	public void clearActiveTradeList(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
	}
	
	public synchronized void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	public void clearCharges()
	{
		_charges = 0;
		stopChargeTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	protected final void clearPetData()
	{
		_data = null;
	}
	
	public void clearWarehouse()
	{
		if (_warehouse != null)
		{
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}
	
	public void closeClient()
	{
		if (getClient() != null)
		{
			getClient().closeNow();
		}
	}
	
	private boolean createDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(CREATE_CHARACTER);
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getStatus().getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getStatus().getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getStatus().getCurrentMp());
			statement.setInt(11, getAppearance().getFace());
			statement.setInt(12, getAppearance().getHairStyle());
			statement.setInt(13, getAppearance().getHairColor());
			statement.setInt(14, getAppearance().getSex() ? 1 : 0);
			statement.setLong(15, getExp());
			statement.setInt(16, getSp());
			statement.setInt(17, getKarma());
			statement.setInt(18, getPvpKills());
			statement.setInt(19, getPkKills());
			statement.setInt(20, getClanId());
			statement.setInt(21, getRace().ordinal());
			statement.setInt(22, getClassId().getId());
			statement.setLong(23, getDeleteTimer());
			statement.setInt(24, hasDwarvenCraft() ? 1 : 0);
			statement.setString(25, getTitle());
			statement.setInt(26, isOnline());
			statement.setInt(27, isIn7sDungeon() ? 1 : 0);
			statement.setInt(28, getClanPrivileges());
			statement.setInt(29, wantsPeace() ? 1 : 0);
			statement.setInt(30, getBaseClass());
			statement.setInt(31, getNewbie());
			statement.setInt(32, isNoble() ? 1 : 0);
			statement.setLong(33, 0);
			statement.setLong(34, System.currentTimeMillis());
			statement.setInt(35, isVip() ? 1 : 0);
			statement.setLong(36, 0);
			statement.setInt(37, isAio() ? 1 : 0);
			statement.setLong(38, 0);
			statement.setInt(39, getArenaWins());
			statement.setInt(40, getArenaDefeats());
			statement.setInt(41, hasWarehouseAccount() ? 1 : 0);
			statement.setString(42, getWarehouseAccountId());
			statement.setString(43, getWarehouseAccountPwd());
			statement.setInt(44, getPrivateBuffShopLimit());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert char data: ", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return true;
	}
	
	public void deathPenalty(boolean atWar, boolean killedByPc)
	{
		deathPenalty(atWar, killedByPc, getCharmOfCourage());
	}
	
	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean charmOfCourage)
	{
		
		final int lvl = getLevel();
		
		byte level = (byte) getLevel();
		
		int clan_luck = getSkillLevel(L2Skill.SKILL_CLAN_LUCK);
		
		double clan_luck_modificator = 1.0;
		
		if (!killed_by_pc)
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.8;
					break;
				case 2:
					clan_luck_modificator = 0.8;
					break;
				case 1:
					clan_luck_modificator = 0.88;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		else
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.5;
					break;
				case 2:
					clan_luck_modificator = 0.5;
					break;
				case 1:
					clan_luck_modificator = 0.5;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		
		double percentLost = clan_luck_modificator;
		
		switch (level)
		{
			case 80:
			case 79:
			case 78:
				percentLost = 1.5 * clan_luck_modificator;
				break;
			case 77:
				percentLost = 2.0 * clan_luck_modificator;
				break;
			case 76:
				percentLost = 2.5 * clan_luck_modificator;
				break;
			default:
				if (level < 40)
				{
					percentLost = 7.0 * clan_luck_modificator;
				}
				else if (level >= 40 && level <= 75)
				{
					percentLost = 4.0 * clan_luck_modificator;
				}
				break;
		}
		
		if (getKarma() > 0)
		{
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		}
		
		if (isFestivalParticipant() || atwar)
		{
			percentLost /= 4.0;
		}
		
		long lostExp = 0;
		
		if (_event == null || _event.canLostExpOnDie())
		{
			if (lvl < Experience.MAX_LEVEL)
			{
				lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
			}
			else
			{
				lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);
			}
			
			if (killed_by_pc)
			{
				lostExp = (long) calcStat(Stats.LOST_EXP_PVP, lostExp, null, null);
			}
			else
			{
				lostExp = (long) calcStat(Stats.LOST_EXP, lostExp, null, null);
			}
		}
		setExpBeforeDeath(getExp());
		
		if (charmOfCourage && getSiegeState() > 0 && isInsideZone(L2Zone.FLAG_SIEGE))
			return;
		
		if (killed_by_pc && (isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_SIEGE) || isInsideZone(L2Zone.FLAG_SIEGE) && getSiegeState() > 0))
			return;
		getStat().addExp(-lostExp);
	}
	
	public void decreaseCharges(int count)
	{
		if (count < 0)
			return;
		if (_charges - count >= 0)
		{
			_charges -= count;
		}
		else
		{
			_charges = 0;
		}
		if (_charges == 0)
		{
			stopChargeTask();
		}
		else
		{
			restartChargeTask();
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
		{
			_recomLeft--;
		}
	}
	
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}
	
	public void deleteMacro(int id)
	{
		getMacroses().deleteMacro(id);
	}
	
	public void deleteMe()
	{
		if (getOnlineState() == ONLINE_STATE_DELETED)
			return;
		
		ObjectRestrictions.getInstance().pauseTasks(getObjectId());
		
		abortCast();
		abortAttack();
		try
		{
			if (isFlying())
			{
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			}
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			L2ItemInstance flag = getInventory().getItemByItemId(Config.FORTSIEGE_COMBAT_FLAG_ID);
			if (flag != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this);
				}
				else
				{
					int slot = flag.getItem().getBodyPart();
					getInventory().unEquipItemInBodySlotAndRecord(slot);
					destroyItem("CombatFlag", flag, null, true);
				}
			}
		}
		catch (Exception e)
		{
			
		}
		
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
				if (getPet() != null)
				{
					getPet().broadcastFullInfoImpl(0);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
		
		if (getActiveRequester() != null)
		{
			getActiveRequester().onTradeCancel(this);
			onTradeCancel(getActiveRequester());
			
			cancelActiveTrade();
			
			setActiveRequester(null);
		}
		
		if (inObserverMode())
		{
			getPosition().setXYZ(_obsX, _obsY, _obsZ);
		}
		
		if (isOfflineTrade())
		{
			try
			{
				stopWarnUserTakeBreak();
				stopSkillRewardTime();
				// stopAutoSaveTask();
				stopWaterTask();
				stopFeed();
				clearPetData();
				storePetFood(_mountNpcId);
				stopChargeTask();
				stopPvPFlag();
				stopJailTask();
			}
			catch (Exception e)
			{
				
			}
			return;
		}
		try
		{
			setOnlineStatus(false);
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			stopAllTimers();
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			if (isInOlympiadMode())
			{
				Olympiad.unRegisterNoble(this);
			}
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			RecipeController.getInstance().requestMakeItemAbort(this);
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			setTarget(null);
		}
		catch (Exception e)
		{
			
		}
		
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().onExit(this);
		}
		
		if (_objectSittingOn != null)
		{
			_objectSittingOn.setBusyStatus(null);
		}
		_objectSittingOn = null;
		
		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}
			
			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				{
					character.abortCast();
				}
		}
		catch (Exception e)
		{
			
		}
		
		storeEffect();
		stopAllEffects();
		
		L2WorldRegion oldRegion = getWorldRegion();
		
		if (isVisible())
		{
			try
			{
				decayMe();
			}
			catch (Exception e)
			{
				
			}
		}
		
		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}
		
		if (isInParty())
		{
			try
			{
				leaveParty();
				if (isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
					if (getParty() != null)
					{
						getParty().broadcastToPartyMembers(SystemMessage.sendString(getName() + " has been removed from the upcoming festival."));
					}
			}
			catch (Exception e)
			{
				
			}
		}
		else
		{
			L2PartyRoom room = getPartyRoom();
			if (room != null)
			{
				room.removeMember(this, false);
			}
		}
		PartyRoomManager.getInstance().removeFromWaitingList(this);
		
		if (Olympiad.isRegistered(this) || getOlympiadGameId() != -1)
		{
			Olympiad.removeDisconnectedCompetitor(this);
		}
		
		if (getClanId() != 0 && getClan() != null)
		{
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}
			}
			catch (Exception e)
			{
				
			}
		}
		
		if (getActiveRequester() != null)
		{
			setActiveRequester(null);
		}
		
		if (isGM())
		{
			try
			{
				GmListTable.getInstance().deleteGm(this);
			}
			catch (Exception e)
			{
				
			}
		}
		
		try
		{
			getInventory().deleteMe();
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			clearWarehouse();
		}
		catch (Exception e)
		{
			
		}
		try
		{
			getFreight().deleteMe();
		}
		catch (Exception e)
		{
			
		}
		
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			
		}
		
		if (getClanId() > 0)
		{
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
		}
		
		if (_snoopedPlayer != null)
		{
			for (L2PcInstance player : _snoopedPlayer)
			{
				player.removeSnooper(this);
			}
			_snoopedPlayer.clear();
			_snoopedPlayer = null;
		}
		
		if (_snoopListener != null)
		{
			broadcastSnoop(0, "", "*** Player " + getName() + " logged off ***");
			for (L2PcInstance player : _snoopListener)
			{
				player.removeSnooped(this);
			}
			_snoopListener.clear();
			_snoopListener = null;
		}
		
		for (String friendName : L2FriendList.getFriendListNames(this))
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
			}
		}
		
		if (_chanceSkills != null)
		{
			_chanceSkills.setOwner(null);
			_chanceSkills = null;
		}
		
		L2World.getInstance().removeObject(this);
		
		try
		{
			setIsTeleporting(false);
			L2World.getInstance().removeFromAllPlayers(this);
		}
		catch (Throwable t)
		{
			
		}
		SQLQueue.getInstance().run();
		
	}
	
	public void deleteShortCut(int slot, int page)
	{
		getShortCuts().deleteShortCut(slot, page);
	}
	
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		return destroyItem(process, item, count, reference, sendMessage);
	}
	
	public boolean destroyItem(String process, L2ItemInstance item, int count, L2Object reference, boolean sendMessage)
	{
		item = getInventory().destroyItem(process, item, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		if (sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addNumber(count));
		}
		
		return true;
	}
	
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemId);
		if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		getInventory().updateInventory(item);
		
		if (sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addNumber(count));
		}
		return true;
	}
	
	public boolean destroyItemWithoutTrace(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		return destroyItem(null, item, count, reference, sendMessage);
	}
	
	public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
	{
		
		for (L2ItemInstance item : getInventory().getItems())
			if (item.isWear())
			{
				if (item.isEquipped())
				{
					getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
				}
				
				if (getInventory().destroyItem(process, item, this, reference) == null)
				{
					_log.warn("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}
				
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item.getItemId()));
			}
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		sendPacket(new ItemList(getInventory().getItems(), true));
		
		broadcastUserInfo();
		
		sendPacket(SystemMessageId.NO_LONGER_TRYING_ON);
	}
	
	public boolean disarmWeapons()
	{
		if (isCursedWeaponEquipped())
			return false;
		
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		if (wpn != null)
		{
			if (wpn.isWear())
				return false;
			
			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			refreshExpertisePenalty();
			broadcastUserInfo();
			
			if (unequipped.length > 0)
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0].getItemId()));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0].getItemId()));
				}
		}
		
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
				return false;
			
			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			refreshExpertisePenalty();
			broadcastUserInfo();
			
			if (unequipped.length > 0)
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0].getItemId()));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0].getItemId()));
				}
		}
		return true;
	}
	
	public boolean dismount()
	{
		sendPacket(new SetupGauge(3, 0, 0));
		int petId = _mountNpcId;
		if (setMount(0, 0, 0))
		{
			stopFeed();
			clearPetData();
			
			if (isFlying())
			{
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			}
			
			broadcastPacket(new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0));
			
			setMountObjectID(0);
			storePetFood(petId);
			
			broadcastUserInfo();
			return true;
		}
		return false;
	}
	
	public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
	{
		if (isInParty())
		{
			getParty().distributeItem(this, item, false, target);
		}
		else if (item.getItemId() == 57)
		{
			addAdena("Loot", item.getCount(), target, true);
		}
		else
		{
			addItem("Loot", item.getItemId(), item.getCount(), target, true, false);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (isMounted())
		{
			stopFeed();
		}
		synchronized (this)
		{
			if (isFakeDeath())
			{
				stopFakeDeath(null);
			}
			_charges = 0;
			sendPacket(new EtcStatusUpdate(this));
		}
		if (getActiveEnchantItem() != null)
		{
			sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
			setActiveEnchantItem(null);
		}
		
		setExpBeforeDeath(0);
		
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
		}
		if (isCombatFlagEquipped())
		{
			FortSiegeManager.getInstance().dropCombatFlag(this);
		}
		
		if (killer != null)
		{
			onPvPPkKill(killer);
		}
		
		_charges = 0;
		
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}
		
		if (_fusionSkill != null)
		{
			abortCast();
		}
		
		for (L2Character character : getKnownList().getKnownCharacters())
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
			{
				character.abortCast();
			}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().memberDead(this);
		}
		
		calculateDeathPenaltyBuffLevel(killer);
		updateEffectIcons();
		stopRentPet();
		stopWaterTask();
		if (Config.QUAKE_SYSTEM_RESETONDIE)
		{
			spreeKills = 0;
		}
		
		// leave war legend aura if enabled
		heroConsecutiveKillCount = 0;
		if (Config.WAR_LEGEND_AURA && !_hero && isPVPHero)
		{
			setHeroAura(false);
			this.sendMessage("You leaved War Legend State");
		}
		
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void doInteract(L2Character target)
	{
		if (getPrivateStoreType() == 0)
		{
			setIsBuffShop(false);
		}
		
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
			{
				sendPacket(new PrivateStoreListSell(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreListBuy(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
		}
		else if (target != null)
		{
			target.onAction(this);
		}
	}
	
	public boolean doOffline()
	{
		synchronized (this)
		{
			if (isOfflineTrade())
				return false;
			
			setOfflineTrade(true);
			setEndOfflineTime(false, 0);
			leaveParty();
			
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_OFFLINE_MODE_ON));
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					sendPacket(LeaveWorld.STATIC_PACKET);
					deleteMe();
					getClient().setActiveChar(null);
					_client.stopGuardTask();
					
					updateOnlineStatus();
				}
			}, 5000);
			return true;
		}
	}
	
	public boolean doOfflineBuff()
	{
		synchronized (this)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_OFFLINE_MODE_ON_BUFF));
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (isOfflineTrade())
						return;
					
					setOfflineTrade(true);
					setEndOfflineTime(false, 0);
					
					leaveParty();
					
					sendPacket(LeaveWorld.STATIC_PACKET);
					deleteMe();
					getClient().setActiveChar(null);
					updateOnlineStatus();
					BuffShopManager.onShutDown();
				}
			}, 600000);
			return true;
		}
	}
	
	protected void doPickupItem(L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
			return;
		
		getAI().setIntention(CtrlIntention.IDLE);
		
		if (!(object instanceof L2ItemInstance))
		{
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target));
				return;
			}
			
			if ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (isInvul() && !isGM())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (getActiveTradeList() != null)
			{
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (getAdena() >= 2147483647 && target.getItemId() == 57)
			{
				sendPacket(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
				sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(target.getCount()));
				}
				else if (target.getCount() > 1)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target.getItemId()).addNumber(target.getCount()));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
				}
				return;
			}
			if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()) && isCursedWeaponEquipped())
			{
				ItemTable.destroyItem("Pickup CW", target, this, null);
				CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId());
				cw.increaseKills(cw.getStageKills());
				return;
			}
			
			if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
				if (!FortSiegeManager.getInstance().checkIfCanPickup(this))
					return;
				
			target.pickupMe(this);
		}
		
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
			if (handler == null)
			{
				_log.warn("No item handler registered for item ID " + target.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, target);
			}
		}
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
				if (target.getEnchantLevel() > 0)
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addPcName(this).addNumber(target.getEnchantLevel()).addItemName(target);
					broadcastPacket(msg, 1400);
				}
				else
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addPcName(this).addItemName(target);
					broadcastPacket(msg, 1400);
				}
			
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.destroyItem("Pickup", target, this, null);
			}
			else
			{
				addItem("Pickup", target, null, true);
				
				final L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (weapon != null)
				{
					final L2EtcItem etcItem = target.getEtcItem();
					if (etcItem != null)
					{
						final L2EtcItemType itemType = etcItem.getItemType();
						if (weapon.getItemType() == L2WeaponType.BOW && itemType == L2EtcItemType.ARROW)
						{
							checkAndEquipArrows();
						}
					}
				}
			}
		}
		
		target = null;
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
		_reviveRequested = false;
		_revivePower = 0;
		
		if (isMounted())
		{
			startFeed(_mountNpcId);
		}
		
		if (isInParty() && getParty().isInDimensionalRift())
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
			{
				getParty().getDimensionalRift().memberRessurected(this);
			}
		
		if (_event != null && _event.isRunning())
		{
			_event.onRevive(this);
		}
		else
		{
			if (Config.RESPAWN_RESTORE_CP > 0)
			{
				getStatus().setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
			}
			if (Config.RESPAWN_RESTORE_HP > 0)
			{
				getStatus().setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
			}
			if (Config.RESPAWN_RESTORE_MP > 0)
			{
				getStatus().setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}
		}
		
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		restoreExp(revivePower);
		doRevive();
	}
	
	public L2ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		L2ItemInstance olditem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance item = getInventory().dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		item.setOwnerId(0);
		if (Config.DESTROY_PLAYER_INVENTORY_DROP)
		{
			if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				if (Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB)
					if (item.isEquipable())
					{
						if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
						{
							ItemsAutoDestroy.getInstance().addItem(item);
						}
					}
					else
					{
						ItemsAutoDestroy.getInstance().addItem(item);
					}
			item.setProtected(false);
		}
		else
		{
			item.setDropTime(0);
		}
		
		// retail drop protection
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		getInventory().updateInventory(olditem);
		
		if (sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item.getItemId()));
		}
		return item;
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return dropItem(process, item, reference, sendMessage, false);
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		item = getInventory().dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ());
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
			if (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
			{
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}
		
		// retail drop protection
		if (protectItem)
		{
			item.getDropProtection().protect(this);
		}
		
		getInventory().updateInventory(item);
		
		if (sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item.getItemId()));
		}
		
		return true;
	}
	
	public void enableAutoLoot(boolean var)
	{
		if (!Config.ALLOW_AUTO_LOOT)
		{
			_characterData.set("autoloot", false);
		}
		else
		{
			_characterData.set("autoloot", var);
		}
	}
	
	public void endFishing(boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		broadcastUserInfo();
		if (_fishCombat == null)
		{
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		
		_lure = null;
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}
	
	public void engageAnswer(int answer)
	{
		if (!_engagerequest)
			return;
		else if (_engageid == 0)
			return;
		else
		{
			L2Object obj = getKnownList().getKnownObject(_engageid);
			setEngageRequest(false, 0);
			if (obj instanceof L2PcInstance)
			{
				L2PcInstance ptarget = (L2PcInstance) obj;
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_REQUEST_OK));
				}
				else
				{
					sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_REQUEST_CANCELED));
				}
			}
			else
			{
				sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_TARGET_NOT_FOUND));
			}
		}
	}
	
	public void enteredNoLanding()
	{
		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new L2PcInstance.dismount(), 1000);
	}
	
	public void enterMovieMode()
	{
		setTarget(null);
		stopMove(null);
		setIsInvul(true);
		setIsImmobilized(true);
		sendPacket(new CameraMode(1));
	}
	
	public void enterObserverMode(int x, int y, int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();
		
		setTarget(null);
		stopMove(null);
		setIsParalyzed(true);
		setIsInvul(true);
		getAppearance().setInvisible();
		sendPacket(new ObservationMode(x, y, z));
		teleToLocation(x, y, z, false);
		
		_observerMode = true;
		setObserveMode(1);
		updateInvisibilityStatus();
	}
	
	public void enterOlympiadObserverMode(int x, int y, int z, int id, boolean storeCoords)
	{
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		if (!getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			getCubics().clear();
		}
		if (getParty() != null)
		{
			getParty().removePartyMember(this);
		}
		
		_olympiadGameId = id;
		if (isSitting())
		{
			standUp();
		}
		if (storeCoords)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		teleToLocation(x, y, z, false);
		sendPacket(new ExOlympiadMode(3));
		_observerMode = true;
		setObserveMode(2);
		updateInvisibilityStatus();
	}
	
	public void exitedNoLanding()
	{
		if (_dismountTask != null)
		{
			_dismountTask.cancel(false);
			_dismountTask = null;
		}
	}
	
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if (classId >= 0x00 && classId <= 0x09)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x0a && classId <= 0x11)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x12 && classId <= 0x18)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x19 && classId <= 0x1e)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x1f && classId <= 0x25)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x26 && classId <= 0x2b)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x2c && classId <= 0x30)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x31 && classId <= 0x34)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if (classId >= 0x35 && classId <= 0x39)
		{
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}
		
		return weaponItem;
	}
	
	@Override
	public void finishMovement()
	{
		super.finishMovement();
		if (_sitWhenArrived)
		{
			sitDown(true);
		}
		_sitWhenArrived = false;
	}
	
	public void gatesAnswer(int answer, int type)
	{
		if (_gatesRequestTarget == null)
			return;
		if (answer == 1 && getTarget() == _gatesRequestTarget && type == 1)
		{
			_gatesRequestTarget.openMe();
		}
		else if (answer == 1 && getTarget() == _gatesRequestTarget && type == 0)
		{
			_gatesRequestTarget.closeMe();
		}
		
		_gatesRequestTarget = null;
	}
	
	public void gatesRequest(L2DoorInstance door)
	{
		_gatesRequestTarget = door;
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	public StatsSet getAccountData()
	{
		if (getClient() != null)
			return getClient().getAccountData();
		if (_fakeAccData == null)
		{
			_fakeAccData = new StatsSet();
		}
		return _fakeAccData;
	}
	
	public String getAccountName()
	{
		if (getClient() == null)
			return "disconnected";
		return getClient().getAccountName();
	}
	
	@Override
	public final L2PcInstance getActingPlayer()
	{
		return this;
	}
	
	@Override
	public final L2Summon getActingSummon()
	{
		return getPet();
	}
	
	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
			return null;
		
		return (L2Armor) armor.getItem();
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	public L2Armor getActiveLegsArmorItem()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		
		if (legs == null)
			return null;
		
		return (L2Armor) legs.getItem();
	}
	
	public L2PcInstance getActiveRequester()
	{
		return _activeRequester;
	}
	
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		return (L2Weapon) weapon.getItem();
	}
	
	public int getAdena()
	{
		return getInventory().getAdena();
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
					_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}
	
	public Quest[] getAllActiveQuests()
	{
		LinkedBunch<Quest> quests = new LinkedBunch<>();
		
		for (String qname : _quests.keySet())
		{
			QuestState qs = _quests.get(qname);
			if (qs == null || qs.getQuest() == null)
			{
				_quests.remove(qname);
				continue;
			}
			int questId = qs.getQuest().getQuestIntId();
			if (questId > 999 || questId < 1)
			{
				continue;
			}
			
			if (!qs.isStarted() && !Config.DEVELOPER)
			{
				continue;
			}
			
			quests.add(qs.getQuest());
		}
		
		return quests.moveToArray(new Quest[quests.size()]);
	}
	
	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return getShortCuts().getAllShortCuts();
	}
	
	public int getAllyCrestId()
	{
		if (getClanId() == 0)
			return 0;
		if (getClan().getAllyId() == 0)
			return 0;
		return getClan().getAllyCrestId();
	}
	
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}
	
	public int getAncientAdena()
	{
		return getInventory().getAncientAdena();
	}
	
	public int getAnimationTimer()
	{
		return Math.max(1000, 5000 - getRunSpeed() * 20);
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	public int getApprentice()
	{
		return _apprentice;
	}
	
	public Map<Integer, Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public boolean verSkins()
	{
		if (getVar("verskins") != null)
		{
			verSkin = true;
		}
		else if (getVar("verskins") == null)
		{
			verSkin = false;
		}
		return verSkin;
	}
	
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass);
	}
	
	public BlockList getBlockList()
	{
		if (_blockList == null)
		{
			_blockList = new BlockList(this);
		}
		return _blockList;
	}
	
	public L2BoatInstance getBoat()
	{
		return _boat;
	}
	
	public int getBoatId()
	{
		return _boatId;
	}
	
	public TradeList getBuyList()
	{
		if (_buyList == null)
		{
			_buyList = new TradeList(this);
		}
		return _buyList;
	}
	
	public boolean getCanUseCharmOfCourageItem()
	{
		return _canUseCharmOfCourageItem;
	}
	
	public boolean getCanUseCharmOfCourageRes()
	{
		return _canUseCharmOfCourageRes;
	}
	
	public StatsSet getCharacterData()
	{
		return _characterData;
	}
	
	public int getCharges()
	{
		return _charges;
	}
	
	public L2Skill getChargeSkill()
	{
		for (int id : L2PcInstance.CHARGE_SKILLS)
		{
			L2Skill skill = getKnownSkill(id);
			if (skill != null && skill.getMaxCharges() > 0)
				return skill;
		}
		return null;
	}
	
	public int getCharId()
	{
		return getObjectId();
	}
	
	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}
	
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
			return _clan.getCrestId();
		return 0;
	}
	
	public int getClanCrestLargeId()
	{
		if (_clan != null && _clan.hasCrestLarge())
			return _clan.getCrestLargeId();
		return 0;
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public int getClassLevel()
	{
		int level = 0;
		ClassId parent = getClassId().getParent();
		while (parent != null)
		{
			level++;
			parent = parent.getParent();
		}
		return level;
	}
	
	public ClearableReference<L2PcInstance> getClearableReference()
	{
		if (_clearableReference == null)
		{
			_clearableReference = new ClearableReference<>(this);
		}
		
		return _clearableReference;
	}
	
	public L2GameClient getClient()
	{
		return _client;
	}
	
	public int getClientRevision()
	{
		return _clientRevision;
	}
	
	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	public L2RecipeList[] getCommonRecipeBook()
	{
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}
	
	public int getCommonRecipeLimit()
	{
		int recclim = Config.COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	private ConditionListener[] getConditionListeners()
	{
		if (_conditionListeners == null)
		{
			_conditionListeners = new ConditionListener[]
			{
				new ConditionPlayerHpListener(),
				new ConditionGameTimeListener()
			};
		}
		return _conditionListeners;
	}
	
	public Long getConfirmDlgRequestTime(int requestId)
	{
		return confirmDlgRequests.get(requestId);
	}
	
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}
	
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}
	
	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}
	
	public int getCurrentFeed()
	{
		return _curFeed;
	}
	
	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}
	
	public SkillDat getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	public SkillDat getCurrentSkill()
	{
		return _currentSkill;
	}
	
	public Point3D getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}
	
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	public int getDmKills()
	{
		return _dmKills;
	}
	
	public int getDuelId()
	{
		return _duelId;
	}
	
	public int getDuelState()
	{
		return _duelState;
	}
	
	public int getDwarfRecipeLimit()
	{
		int recdlim = Config.DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	public L2RecipeList[] getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}
	
	public StatsSet getDynamicData()
	{
		return _dynaicData;
	}
	
	public int getEnchantEffect(boolean self)
	{
		L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null)
			return 0;
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	public long getEndOfflineTime()
	{
		return _endOfflineTime;
	}
	
	public int getEngageId()
	{
		return _engageid;
	}
	
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	public long getExp()
	{
		return getStat().getExp();
	}
	
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	public int getExpertisePenalty()
	{
		return _expertisePenalty;
	}
	
	protected int getFeedConsume()
	{
		if (isAttackingNow())
			return getPetData(_mountNpcId).getPetFeedBattle();
		return getPetData(_mountNpcId).getPetFeedNormal();
	}
	
	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}
	
	public int getFishx()
	{
		return _fishx;
	}
	
	public int getFishy()
	{
		return _fishy;
	}
	
	public int getFishz()
	{
		return _fishz;
	}
	
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	public final long getFloodCount(Protected action)
	{
		if (_floodCount == null)
		{
			initFloodCount();
		}
		if (action.ordinal() > _floodCount.length)
			return 0;
		return _floodCount[action.ordinal()];
	}
	
	public PcFreight getFreight()
	{
		if (_freight == null)
		{
			_freight = new PcFreight(this);
			_freight.restore();
		}
		return _freight;
	}
	
	public int getFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}
	
	public L2FriendList getFriendList()
	{
		if (_friendList == null)
		{
			_friendList = new L2FriendList(this);
		}
		return _friendList;
	}
	
	public L2HennaInstance getHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return null;
		
		return _henna[slot - 1];
	}
	
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getClassId().level() == 1)
		{
			totalSlots = 2;
		}
		else
		{
			totalSlots = 3;
		}
		
		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
			{
				totalSlots--;
			}
		
		if (totalSlots <= 0)
			return 0;
		
		return totalSlots;
	}
	
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	public String getHost()
	{
		if (getClient() == null)
			return "disconnected";
		return getClient().getConnection().getInetAddress().getHostAddress();
	}
	
	public ImmutableReference<L2PcInstance> getImmutableReference()
	{
		if (_immutableReference == null)
		{
			_immutableReference = new ImmutableReference<>(this);
		}
		
		return _immutableReference;
	}
	
	public Point3D getInBoatPosition()
	{
		return _inBoatPosition;
	}
	
	@Override
	public PcInventory getInventory()
	{
		if (_inventory == null)
		{
			_inventory = new PcInventory(this);
		}
		return _inventory;
	}
	
	@Override
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.Dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
		
		return ivlim;
	}
	
	public int getItemExpertiseIndex()
	{
		return _itemExpertiseIndex;
	}
	
	public long getJailTimer()
	{
		return _jailTimer;
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	@Override
	public final PcKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new PcKnownList(this);
		}
		
		return (PcKnownList) _knownList;
	}
	
	public String getLang()
	{
		try
		{
			
			return getAccountData().getString("lang");
		}
		catch (IllegalArgumentException e)
		{
			getAccountData().set("lang", "en");
			return "en";
		}
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	public String getVar(final String name)
	{
		return user_variables.get(name);
	}
	
	public L2NpcInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	public String getLastPage()
	{
		return LAST_BBS_PAGE[0];
	}
	
	private Point3D getLastPartyPosition()
	{
		if (_lastPartyPosition == null)
		{
			_lastPartyPosition = new Point3D(0, 0, 0);
		}
		
		return _lastPartyPosition;
	}
	
	public int getLastPartyPositionDistance(int x, int y, int z)
	{
		double dx = x - getLastPartyPosition().getX();
		double dy = y - getLastPartyPosition().getY();
		double dz = z - getLastPartyPosition().getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	public long getLastRecomUpdate()
	{
		return _lastRecomUpdate;
	}
	
	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = x - getLastServerPosition().getX();
		double dy = y - getLastServerPosition().getY();
		double dz = z - getLastServerPosition().getZ();
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public Point3D getLastServerPosition()
	{
		if (_lastServerPosition == null)
		{
			_lastServerPosition = new Point3D(0, 0, 0);
		}
		return _lastServerPosition;
	}
	
	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	@Override
	public double getLevelMod()
	{
		return (89 + getLevel()) / 100.0;
	}
	
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	public L2ItemInstance getLure()
	{
		return _lure;
	}
	
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	public MacroList getMacroses()
	{
		if (_macroses == null)
		{
			_macroses = new MacroList(this);
		}
		return _macroses;
	}
	
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return _forumMail;
	}
	
	protected int getMaxFeed()
	{
		return getPetData(_mountNpcId).getPetMaxFeed();
	}
	
	public int getMaxLoad()
	{
		int con = getCON();
		
		if (con < 1)
			return 31000;
		
		if (con > 59)
			return 176000;
		
		double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
		return (int) calcStat(Stats.MAX_LOAD, baseLoad, this, null);
	}
	
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		
		return _forumMemo;
	}
	
	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}
	
	public int getMountLevel()
	{
		return _mountLevel;
	}
	
	public int getMountNpcId()
	{
		return _mountNpcId;
	}
	
	public int getMountObjectID()
	{
		return _mountObjectID;
	}
	
	public int getMountType()
	{
		return _mountType;
	}
	
	/**
	 * Gets the multi sell id.
	 * @return the multi sell id
	 */
	public final int getMultiSellId()
	{
		return _currentMultiSellId;
	}
	
	public int getNameColor()
	{
		if (isGM())
			return Config.GM_NAME_COLOR;
		if (isOfflineTrade() && Config.ALLOW_OFFLINE_TRADE_COLOR_NAME)
			return Config.OFFLINE_TRADE_COLOR_NAME;
		try
		{
			if (_characterData.getBool("ignorecolors"))
				return getAppearance().getNameColor();
		}
		catch (Exception e)
		{
			
		}
		
		if (Config.PVP_COLOR_SYSTEM && !isGM() && (Config.PVP_COLOR_MODE & Config.PVP_MODE_NAME) != 0)
		{
			int pvpAmmount = getPvpKills();
			if (pvpAmmount >= Config.PVP_AMMOUNT1 && pvpAmmount < Config.PVP_AMMOUNT2)
				return Config.COLOR_FOR_AMMOUNT1;
			else if (pvpAmmount >= Config.PVP_AMMOUNT2 && pvpAmmount < Config.PVP_AMMOUNT3)
				return Config.COLOR_FOR_AMMOUNT2;
			else if (pvpAmmount >= Config.PVP_AMMOUNT3 && pvpAmmount < Config.PVP_AMMOUNT4)
				return Config.COLOR_FOR_AMMOUNT3;
			else if (pvpAmmount >= Config.PVP_AMMOUNT4 && pvpAmmount < Config.PVP_AMMOUNT5)
				return Config.COLOR_FOR_AMMOUNT4;
			else if (pvpAmmount >= Config.PVP_AMMOUNT5)
				return Config.COLOR_FOR_AMMOUNT5;
		}
		
		if (Config.WEDDING_USE_COLOR)
		{
			if (_partnerId == 0)
				return getAppearance().getNameColor();
			L2PcInstance partner = L2World.getInstance().getPlayer(_partnerId);
			if (partner == null)
				return getAppearance().getNameColor();
			if (partner.getAppearance().getSex() != getAppearance().getSex())
				return Config.WEDDING_NORMAL;
			else if (partner.getAppearance().getSex())
				return Config.WEDDING_LESBI;
			else
				return Config.WEDDING_GAY;
		}
		return getAppearance().getNameColor();
		
	}
	
	public int getNameColor(L2PcInstance cha)
	{
		return getNameColor();
	}
	
	public int getNewbie()
	{
		return _newbie;
	}
	
	public SystemMessage getNoDuelReason()
	{
		if (_noDuelReason == 0)
		{
			_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL.getId();
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.getSystemMessageId(_noDuelReason)).addPcName(this);
		_noDuelReason = 0;
		return sm;
	}
	
	public L2StaticObjectInstance getObjectSittingOn()
	{
		return _objectSittingOn;
	}
	
	public int getObservMode()
	{
		return _observMode;
	}
	
	public int getObsX()
	{
		return _obsX;
	}
	
	public int getObsY()
	{
		return _obsY;
	}
	
	public int getObsZ()
	{
		return _obsZ;
	}
	
	public int getOlyDamage()
	{
		return _olyDamage;
	}
	
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	public int getOlympiadOpponentId()
	{
		return _olympiadOpponentId;
	}
	
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	public byte getOnlineState()
	{
		return _isOnline;
	}
	
	public long getOnlineTime()
	{
		long totalOnlineTime = _onlineTime;
		
		if (_onlineBeginTime > 0)
		{
			totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
		}
		
		return totalOnlineTime;
	}
	
	public L2PcInstance getPartner()
	{
		if (_partnerId == 0)
			return null;
		return L2World.getInstance().findPlayer(_partnerId);
	}
	
	public int getPartnerId()
	{
		return _partnerId;
	}
	
	@Override
	public L2Party getParty()
	{
		return _party;
	}
	
	public boolean getVarB(final String name, final boolean defaultVal)
	{
		final String var = user_variables.get(name);
		if (var == null)
			return defaultVal;
		return !(var.equals("0") || var.equalsIgnoreCase("false"));
	}
	
	public void unsetVar(final String name)
	{
		if (name == null)
			return;
		
		if (user_variables.remove(name) != null)
			mysql.set("DELETE FROM `character_variables` WHERE `obj_id`='" + _objectId + "' AND `type`='user-var' AND `name`='" + name + "' LIMIT 1");
	}
	
	public boolean getPartyMatchingLevelRestriction()
	{
		return !_partyMatchingAllLevels;
	}
	
	public int getPartyMatchingRegion()
	{
		return _partyMatchingRegion;
	}
	
	public L2PartyRoom getPartyRoom()
	{
		return _partyRoom;
	}
	
	public int getPcCaffePoints()
	{
		return _pccaffe;
	}
	
	@Override
	public L2Summon getPet()
	{
		return _summon;
	}
	
	protected final L2PetData getPetData(int npcId)
	{
		if (_data == null && getPet() != null)
		{
			_data = PetDataTable.getInstance().getPetData(getPet().getNpcId(), getPet().getLevel());
		}
		else if (_data == null && npcId > 0)
		{
			_data = PetDataTable.getInstance().getPetData(npcId, getLevel());
		}
		
		return _data;
	}
	
	public int getPkKills()
	{
		return _pkKills;
	}
	
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	public int getPledgeRank()
	{
		return _pledgeRank;
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.Dwarf)
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
		
		return pblim;
	}
	
	public int getPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.Dwarf)
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
		}
		else
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
		
		return pslim;
	}
	
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	public long getProtection()
	{
		return _protectEndTime;
	}
	
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	public QuestState[] getQuestsForAttacks(L2Npc npc)
	{
		QuestState[] states = null;
		
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
			if (getQuestState(quest.getName()) != null)
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			
		return states;
	}
	
	public QuestState[] getQuestsForKills(L2Npc npc)
	{
		QuestState[] states = null;
		
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
			if (getQuestState(quest.getName()) != null)
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			
		return states;
	}
	
	public QuestState[] getQuestsForTalk(int npcId)
	{
		QuestState[] states = null;
		
		Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
				if (quest != null)
					if (getQuestState(quest.getName()) != null)
						if (states == null)
						{
							states = new QuestState[]
							{
								getQuestState(quest.getName())
							};
						}
						else
						{
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
						}
		}
		
		return states;
	}
	
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}
	
	public SkillDat getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	public Race getRace()
	{
		if (!isSubClassActive())
			return getTemplate().getRace();
		
		L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass);
		return charTemp.getRace();
	}
	
	public int getRace(int i)
	{
		return _race[i];
	}
	
	public L2Radar getRadar()
	{
		if (_radar == null)
		{
			_radar = new L2Radar(this);
		}
		return _radar;
	}
	
	private int getRandomFishLvl()
	{
		L2Effect[] effects = getAllEffects();
		int skilllvl = getSkillLevel(1315);
		for (L2Effect e : effects)
			if (e.getSkill().getId() == 2274)
			{
				skilllvl = (int) e.getSkill().getPower(this);
			}
		if (skilllvl <= 0)
			return 1;
		int randomlvl;
		int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		
		return randomlvl;
	}
	
	private int getRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0:
				switch (_lure.getItemId())
				{
					case 7807:
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808:
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809:
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486:
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1:
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519:
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522:
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525:
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484:
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2:
				switch (_lure.getItemId())
				{
					case 8506:
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509:
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512:
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485:
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	private int getRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807:
			case 7808:
			case 7809:
			case 8486:
				return 0;
			case 8485:
			case 8506:
			case 8509:
			case 8512:
				return 2;
			default:
				return 1;
		}
	}
	
	public FastMap<String, String> getVars()
	{
		return user_variables;
	}
	
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	public int getRelation(L2PcInstance target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
		{
			result |= RelationChanged.RELATION_PVP_FLAG;
		}
		if (getKarma() > 0)
		{
			result |= RelationChanged.RELATION_HAS_KARMA;
		}
		
		if (isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
			{
				result |= RelationChanged.RELATION_ENEMY;
			}
			else
			{
				result |= RelationChanged.RELATION_ALLY;
			}
			if (getSiegeState() == 1)
			{
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}
		
		if (getClan() != null && target.getClan() != null)
		{
			if (target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && getPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
				{
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		return result;
	}
	
	public L2Request getRequest()
	{
		if (_request == null)
		{
			_request = new L2Request(this);
		}
		return _request;
	}
	
	public Map<Integer, TimeStamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		L2Item item = weapon.getItem();
		
		if (item instanceof L2Weapon)
			return (L2Weapon) item;
		
		return null;
	}
	
	public TradeList getSellList()
	{
		if (_sellList == null)
		{
			_sellList = new TradeList(this);
		}
		return _sellList;
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		return getShortCuts().getShortCut(slot, page);
	}
	
	private ShortCuts getShortCuts()
	{
		if (_shortCuts == null)
		{
			_shortCuts = new ShortCuts(this);
		}
		
		return _shortCuts;
	}
	
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	public long getSkillQueueProtectionTime()
	{
		return _skillQueueProtectionTime;
	}
	
	public int getSp()
	{
		return getStat().getSp();
	}
	
	public int getSponsor()
	{
		return _sponsor;
	}
	
	@Override
	public final PcStat getStat()
	{
		if (_stat == null)
		{
			_stat = new PcStat(this);
		}
		
		return (PcStat) _stat;
	}
	
	@Override
	public final PcStatus getStatus()
	{
		if (_status == null)
		{
			_status = new PcStatus(this);
		}
		
		return (PcStatus) _status;
	}
	
	public SubClass getSubclassByIndex(int classIndex)
	{
		SubClass result = getSubClasses().get(classIndex);
		if (result == null)
			return getSubClasses().get(0);
		return result;
	}
	
	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
		{
			_subClasses = new ConcurrentHashMap<>();
		}
		
		return _subClasses;
	}
	
	public int getSubLevel()
	{
		if (isSubClassActive())
		{
			int lvl = getLevel();
			return lvl;
		}
		return 0;
	}
	
	public int getTeleMode()
	{
		return _telemode;
	}
	
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}
	
	public int getTitleColor()
	{
		if (isGM())
			return Config.GM_TITLE_COLOR;
		try
		{
			if (_characterData.getBool("ignorecolors"))
				return getAppearance().getTitleColor();
		}
		catch (Exception e)
		{
			
		}
		
		if (Config.PVP_COLOR_SYSTEM && !isGM() && (Config.PVP_COLOR_MODE & Config.PVP_MODE_TITLE) != 0)
		{
			int pvpAmmount = getPvpKills();
			if (pvpAmmount >= Config.PVP_AMMOUNT1 && pvpAmmount < Config.PVP_AMMOUNT2)
				return Config.TITLE_COLOR_FOR_AMMOUNT1;
			else if (pvpAmmount >= Config.PVP_AMMOUNT2 && pvpAmmount < Config.PVP_AMMOUNT3)
				return Config.TITLE_COLOR_FOR_AMMOUNT2;
			else if (pvpAmmount >= Config.PVP_AMMOUNT3 && pvpAmmount < Config.PVP_AMMOUNT4)
				return Config.TITLE_COLOR_FOR_AMMOUNT3;
			else if (pvpAmmount >= Config.PVP_AMMOUNT4 && pvpAmmount < Config.PVP_AMMOUNT5)
				return Config.TITLE_COLOR_FOR_AMMOUNT4;
			else if (pvpAmmount >= Config.PVP_AMMOUNT5)
				return Config.TITLE_COLOR_FOR_AMMOUNT5;
		}
		
		return getAppearance().getTitleColor();
	}
	
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}
	
	public boolean getTrading()
	{
		return _trading;
	}
	
	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		return _warehouse;
	}
	
	public int getWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.Dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
		
		return whlim;
	}
	
	public int getWeightPenalty()
	{
		if (_dietMode)
			return 0;
		return _curWeightPenalty;
	}
	
	public void giveAvailableSkills()
	{
		int unLearnable = 0;
		int skillCounter = 0;
		
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		while (skills.length != unLearnable)
		{
			unLearnable = 0;
			for (L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || !sk.getCanLearn(getClassId()) || sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION || sk.getId() == L2Skill.SKILL_LUCKY)
				{
					unLearnable++;
					continue;
				}
				
				if (getSkillLevel(sk.getId()) == -1)
				{
					skillCounter++;
				}
				
				if (sk.isToggle())
				{
					L2Effect toggleEffect = getFirstEffect(sk.getId());
					if (toggleEffect != null)
					{
						toggleEffect.exit();
						sk.getEffects(this, this);
					}
				}
				
				addSkill(sk, true);
			}
			skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		}
		
		if (skillCounter > 0)
		{
			sendMessage("You have learned " + skillCounter + " new skills.");
		}
	}
	
	public void giveRecom(L2PcInstance target)
	{
		if (Config.ALT_RECOMMEND)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);
				statement.setInt(1, getObjectId());
				statement.setInt(2, target.getObjectId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Failed updating character recommendations.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		target.incRecomHave();
		decRecomLeft();
		_recomChars.add(target.getObjectId());
	}
	
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	public boolean hasDwarvenCrystallize()
	{
		return getSkillLevel(L2Skill.SKILL_CRYSTALLIZE) >= 1;
	}
	
	public boolean hasRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			return true;
		return _commonRecipeBook.containsKey(recipeId);
	}
	
	public void increaseCharges(int count, int max)
	{
		if (count <= 0)
			return;
		
		int charges = _charges + count;
		if (_charges < max)
		{
			_charges = Math.min(max, charges);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges));
		}
		else
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		}
		restartChargeTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15)
			return;
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null)
			{
				removeSkill(skill, true);
			}
		}
		
		_deathPenaltyBuffLevel++;
		
		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		sendSkillList();
	}
	
	public void increaseKarma(int targLVL)
	{
		if (_event != null && _event.isRunning())
			return;
		
		int baseKarma = (int) (Config.KARMA_MIN_KARMA * Config.KARMA_RATE);
		int newKarma = baseKarma;
		int karmaLimit = (int) (Config.KARMA_MAX_KARMA * Config.KARMA_RATE);
		
		int pkLVL = getLevel();
		int pkPKCount = getPkKills();
		
		int lvlDiffMulti = 0;
		int pkCountMulti = 0;
		
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		
		if (pkLVL > targLVL)
		{
			lvlDiffMulti = pkLVL / targLVL;
		}
		else
		{
			lvlDiffMulti = 1;
		}
		if (lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}
		
		newKarma = (int) (newKarma * pkCountMulti * lvlDiffMulti * Config.KARMA_RATE);
		
		if (newKarma < baseKarma)
		{
			newKarma = baseKarma;
		}
		if (newKarma > karmaLimit)
		{
			newKarma = karmaLimit;
		}
		
		if (getKarma() > Integer.MAX_VALUE - newKarma)
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		setKarma(getKarma() + newKarma);
	}
	
	private int spreeKills = 0;
	
	public void increasePvpKills(int level)
	{
		if (_event != null && _event.isRunning())
			return;
		
		setPvpKills(getPvpKills() + 1);
		if (Config.PVP_CONGRATULATIONS_MSG)
		{
			sendMessage("SVR Congratulations you won a pvp point !!!");
		}
		
		heroConsecutiveKillCount++;
		
		if (heroConsecutiveKillCount == Config.KILLS_TO_GET_WAR_LEGEND_AURA && Config.WAR_LEGEND_AURA == true)
		{
			setHeroAura(true);
			Announcements.getInstance().announceToAll(getName() + " becames War Legend with " + Config.KILLS_TO_GET_WAR_LEGEND_AURA + " PvP!!");
			sendPacket(new UserInfo(this));
		}
		
		if (Config.ALLOW_QUAKE_SYSTEM)
		{
			spreeKills++;
			switch (spreeKills)
			{
				case 1:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " First Blood !!!"));
					break;
				case 2:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Is Dominating !!!"));
					break;
				case 4:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Rampage !!!"));
					break;
				case 8:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Killing Spree !!!"));
					break;
				case 16:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Mo-Mo-Monster Kill !!!"));
					break;
				case 24:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Unstoppable !!!"));
					break;
				case 32:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Ultra Kill !!!"));
					break;
				case 48:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " God Like !!!"));
					break;
				case 64:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Wicked Sick !!!"));
					break;
				case 96:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Ludicrous !!!"));
					break;
				case 128:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " Holy S H I T !!!"));
					break;
				case 129:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " OMFG !!!"));
					break;
				case 130:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " ROFL !!!"));
					break;
				case 132:
					Broadcast.toAllOnlinePlayers(new CreatureSay(0, SystemChatChannelId.Chat_Market, "SVR", getName() + " FLAWLESS VICTORY !!!"));
					spreeKills = 0;
					break;
			}
		}
		
		if (Config.ALLOW_PVP_REWARD)
		{
			for (int[] item : Config.PVP_REWARD_ITEM)
				addItem("", item[0], item[1], this, true);
			sendMessage("You will be rewarded for pvp kill!");
		}
		
		sendPacket(new UserInfo(this));
		
		broadcastUserInfo();
	}
	
	protected void incRecomHave()
	{
		if (_recomHave < 255)
		{
			_recomHave++;
		}
	}
	
	public final void initFloodCount()
	{
		_floodCount = new long[Protected.values().length];
		for (int i = 0; i < _floodCount.length; i++)
		{
			_floodCount[i] = 0;
		}
	}
	
	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	public boolean inPrivateMode()
	{
		if (_privatestore > 0)
			return true;
		
		return false;
	}
	
	public synchronized void intemediateStore()
	{
		if (System.currentTimeMillis() > _lastStore + 300000)
			return;
		_lastStore = System.currentTimeMillis();
		store(false);
	}
	
	public boolean inTradeZone()
	{
		if (Config.CHECK_ZONE_ON_PVT && !isInsideZone(L2Zone.FLAG_TRADE))
		{
			sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		return true;
	}
	
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	public boolean isAlliedWithKetra()
	{
		return _alliedVarkaKetra > 0;
	}
	
	public boolean isAlliedWithVarka()
	{
		return _alliedVarkaKetra < 0;
	}
	
	@Override
	public final boolean isAllSkillsDisabled()
	{
		return super.isAllSkillsDisabled() || _protectedSitStand;
	}
	
	@Override
	public final boolean isAttackingDisabled()
	{
		return super.isAttackingDisabled() || _protectedSitStand;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null || attacker == this || attacker == getPet())
			return false;
		if (attacker instanceof L2MonsterInstance)
			return true;
		if (getParty() != null && getParty().getPartyMembers().contains(attacker) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
			return false;
		if (isCursedWeaponEquipped())
			return true;
		if (attacker._event != null && attacker._event.canAttack(attacker, this))
			return true;
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInOlympiadMode())
			return isInOlympiadMode() && isOlympiadStart() && ((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId();
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getObjectId()) && !(getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId()))
			return false;
		if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == ((L2PcInstance) attacker).getDuelId())
			return true;
		if (attacker instanceof L2Playable && isInsideZone(L2Zone.FLAG_PEACE))
			return false;
		if (attacker instanceof L2Playable && isInsideZone(L2Zone.FLAG_CHAOTIC))
			return true;
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;
		
		if (attacker instanceof L2PcInstance || attacker instanceof L2Summon)
		{
			L2PcInstance attackTarget = attacker.getActingPlayer();
			
			if (attackTarget == null)
				return false;
			
			if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == attackTarget.getDuelId())
				return true;
			if (isInsideZone(L2Zone.FLAG_PVP) && attacker.isInsideZone(L2Zone.FLAG_PVP))
				return true;
			if (isInsideZone(L2Zone.FLAG_CHAOTIC) && attacker.isInsideZone(L2Zone.FLAG_CHAOTIC))
				return true;
			if (attackTarget.isCursedWeaponEquipped())
				return true;
			
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					if (siege.checkIsDefender(attackTarget.getClan()) && siege.checkIsDefender(getClan()))
						return false;
					if (siege.checkIsAttacker(attackTarget.getClan()) && siege.checkIsAttacker(getClan()))
						return false;
				}
				if (getClan().isAtWarWith(attackTarget.getClanId()) && !wantsPeace() && !attackTarget.wantsPeace() && !isAcademyMember())
					return true;
			}
			if (attacker._event != null && attacker._event.canAttack(attacker, this))
				return true;
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getSiege(this);
				return siege != null && siege.checkIsAttacker(getClan());
			}
		}
		else if (attacker instanceof L2FortSiegeGuardInstance)
			if (getClan() != null)
			{
				FortSiege siege = FortSiegeManager.getSiege(this);
				return siege != null && siege.checkIsAttacker(getClan());
			}
		return false;
	}
	
	public boolean isAutoLootEnabled()
	{
		try
		{
			return _characterData.getBool("autoloot");
		}
		catch (IllegalArgumentException e)
		{
			_characterData.set("autoloot", Config.AUTO_LOOT);
			return Config.AUTO_LOOT;
		}
	}
	
	public boolean isBanned()
	{
		return _isBanned;
	}
	
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = getClan();
		if (clan != null && clan.getLeader().getPlayerInstance() == this)
		{
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if (castle != null && castle == CastleManager.getInstance().getCastleById(castleId))
				return true;
		}
		return false;
	}
	
	public boolean isChaotic()
	{
		if (getKarma() > 0)
			return true;
		return false;
	}
	
	public boolean isChatBanned()
	{
		if (ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerChat))
			return true;
		if (ObjectRestrictions.getInstance().checkGlobalRestriction(AvailableRestriction.GlobalPlayerChat))
			return true;
		return false;
	}
	
	public boolean isClanLeader()
	{
		if (getClan() == null)
			return false;
		return getObjectId() == getClan().getLeaderId();
	}
	
	public boolean isCombatFlagEquipped()
	{
		return _combatFlagEquipped;
	}
	
	public boolean isCummonerClass()
	{
		return getClassId().isSummoner();
	}
	
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}
	
	public boolean isEnchanting()
	{
		return _isEnchanting;
	}
	
	public boolean isEngageRequest()
	{
		return _engagerequest;
	}
	
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public GameEvent isFightingInEvent()
	{
		return _event;
	}
	
	public boolean isFishing()
	{
		return _fishing;
	}
	
	@Override
	public boolean isGM()
	{
		return _GmStatus;
	}
	
	public boolean isHero()
	{
		return _hero;
	}
	
	protected boolean isHungry()
	{
		return _canFeed ? getCurrentFeed() < 0.55 * getPetData(getMountNpcId()).getPetMaxFeed() : false;
	}
	
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	public boolean isInBoat()
	{
		return _inBoat;
	}
	
	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}
	
	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}
	
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	public boolean isInDuel()
	{
		return _isInDuel;
	}
	
	@Override
	public boolean isInFunEvent()
	{
		return _event != null && _event.isRunning();
	}
	
	public boolean isInJail()
	{
		return _inJail;
	}
	
	public boolean isInLooterParty(int LooterId)
	{
		L2PcInstance looter = (L2PcInstance) L2World.getInstance().findObject(LooterId);
		
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
			return getParty().getCommandChannel().getMembers().contains(looter);
		
		if (isInParty() && looter != null)
			return getParty().getPartyMembers().contains(looter);
		
		return false;
	}
	
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}
	
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	public boolean isInStoreMode()
	{
		return getPrivateStoreType() > 0;
	}
	
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || _protectEndTime > GameTimeController.getGameTicks();
	}
	
	public boolean isLookingForParty()
	{
		return _lookingForParty;
	}
	
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	public boolean isMaried()
	{
		return _maried;
	}
	
	public boolean isMary()
	{
		return _maryrequest;
	}
	
	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}
	
	public boolean isMiniMapOpen()
	{
		return _miniMapOpen;
	}
	
	public boolean getVarB(final String name)
	{
		final String var = user_variables.get(name);
		return !(var == null || var.equals("0") || var.equalsIgnoreCase("false"));
	}
	
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	public boolean isNoble()
	{
		return _noble;
	}
	
	public boolean isNormalCraftMode()
	{
		try
		{
			return _characterData.getBool("normalCraft");
		}
		catch (IllegalArgumentException e)
		{
			_characterData.set("normalCraft", Config.ALT_GAME_CREATION);
			return Config.ALT_GAME_CREATION;
		}
	}
	
	public boolean isOfflineTrade()
	{
		return _isOfflineTrade;
	}
	
	public boolean isOlympiadStart()
	{
		return _olympiadStart;
	}
	
	public int isOnline()
	{
		if (isOfflineTrade())
			return 2;
		
		return getClient() == null ? 0 : 1;
	}
	
	public boolean isOnVehicle()
	{
		return false;
	}
	
	public boolean isPartyInvProt()
	{
		
		return _isPartyInvProt;
	}
	
	public boolean isPetReviveRequested()
	{
		return _revivePetRequested;
	}
	
	public boolean isProcessingRequest()
	{
		return _requestExpireTime > System.currentTimeMillis() || _activeRequester != null;
	}
	
	public boolean isProcessingTransaction()
	{
		return _requestExpireTime > System.currentTimeMillis() || _activeTradeList != null;
	}
	
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
	}
	
	public boolean isRentedPet()
	{
		return _taskRentPet != null;
	}
	
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > System.currentTimeMillis());
	}
	
	public boolean isReviveRequested()
	{
		return _reviveRequested;
	}
	
	@Override
	public boolean isRiding()
	{
		return _mountType == 1;
	}
	
	public final boolean isRidingRedStrider()
	{
		return _isRidingRedStrider;
	}
	
	public final boolean isRidingStrider()
	{
		return _isRidingStrider;
	}
	
	public boolean isShowSkillChance()
	{
		if (!Config.SHOW_SKILL_SUCCESS_CHANCE)
			return false;
		try
		{
			return _characterData.getBool("skillChance");
		}
		catch (IllegalArgumentException e)
		{
			_characterData.set("skillChance", Config.SHOW_SKILL_SUCCESS_CHANCE);
			return Config.SHOW_SKILL_SUCCESS_CHANCE;
		}
	}
	
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}
	
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	public boolean isSummoning()
	{
		return _isSummoning;
	}
	
	@Override
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		
		if (weaponItem == null)
			return false;
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
			return true;
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
			return true;
		else if (weaponItem.getItemId() == 248)
			return true;
		else
			return weaponItem.getItemId() == 252;
	}
	
	public boolean isWearingFormalWear()
	{
		return _IsWearingFormalWear;
	}
	
	public boolean isWearingHeavyArmor()
	{
		if (getChestArmorInstance() != null && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.HEAVY && armor.getItemType() == L2ArmorType.HEAVY)
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();
			
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.HEAVY)
				return true;
		}
		
		return false;
	}
	
	public boolean isWearingLightArmor()
	{
		if (getChestArmorInstance() != null && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.LIGHT && armor.getItemType() == L2ArmorType.LIGHT)
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();
			
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.LIGHT)
				return true;
		}
		
		return false;
	}
	
	public boolean isWearingMagicArmor()
	{
		if (getChestArmorInstance() != null && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.MAGIC && armor.getItemType() == L2ArmorType.MAGIC)
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();
			
			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.MAGIC)
				return true;
		}
		
		return false;
	}
	
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			_party = party;
			if (!party.addPartyMember(this))
			{
				_party = null;
			}
		}
	}
	
	public void leaveMovieMode()
	{
		if (!isGM())
		{
			setIsInvul(false);
		}
		setIsImmobilized(false);
		sendPacket(new CameraMode(0));
	}
	
	public void leaveObserverMode()
	{
		setTarget(null);
		getPosition().setXYZ(_obsX, _obsY, _obsZ);
		setIsParalyzed(false);
		
		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.IDLE);
		}
		
		teleToLocation(_obsX, _obsY, _obsZ);
		sendPacket(new ObservationReturn(this));
		_observerMode = false;
		setObserveMode(0);
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0));
		teleToLocation(_obsX, _obsY, _obsZ);
		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.IDLE);
		}
		Olympiad.removeSpectator(_olympiadGameId, this);
		_olympiadGameId = -1;
		_observerMode = false;
		setObserveMode(0);
		broadcastUserInfo();
	}
	
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this, false);
			_party = null;
		}
	}
	
	public void loadSetting(Connection con) throws SQLException
	{
		PreparedStatement stm = con.prepareStatement(LOAD_CHAR_DATA);
		stm.setInt(1, getObjectId());
		ResultSet rs = stm.executeQuery();
		while (rs.next())
		{
			_characterData.set(rs.getString(1), rs.getString(2));
		}
		
		rs.close();
		stm.close();
		try
		{
			int nameColor = _characterData.getInteger("nameColor");
			getAppearance().setNameColor(nameColor);
		}
		catch (IllegalArgumentException e)
		{
			_characterData.set("nameColor", getAppearance().getNameColor());
		}
		try
		{
			int titleColor = _characterData.getInteger("titleColor");
			getAppearance().setTitleColor(titleColor);
		}
		catch (IllegalArgumentException e)
		{
			_characterData.set("titleColor", getAppearance().getTitleColor());
		}
		
	}
	
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			
			statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_CHAR_SKILLS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e);
			
			getSubClasses().remove(classIndex);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		try
		{
			L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getSubClasses().get(classIndex).getClassDefinition());
			for (L2SkillLearn sk : skills)
			{
				removeSkill(sk.getId());
			}
			for (L2Effect e : getAllEffects())
			{
				e.exit();
			}
		}
		catch (Exception e)
		{
			
		}
		getSubClasses().remove(classIndex);
		
		return addSubClass(newClassId, classIndex);
	}
	
	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if (!disarmWeapons())
			return false;
		
		for (L2Effect e : getAllEffects())
			if (e != null && e.getSkill().isToggle() && e.getSkill().getId() != 5399)
			{
				e.exit();
			}
		
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, npcId);
		if (setMount(npcId, getLevel(), mount.getMountType()))
		{
			clearPetData();
			setMountObjectID(controlItemObjId);
			broadcastPacket(mount);
			broadcastUserInfo();
			if (useFood)
			{
				startFeed(npcId);
			}
			return true;
		}
		return false;
	}
	
	public boolean mount(L2Summon pet)
	{
		if (!isInsideRadius(pet, 80, true, false))
		{
			sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
			return false;
		}
		
		if (!GeoData.getInstance().canSeeTarget(this, pet))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			return false;
		}
		if (getActiveEnchantItem() != null)
		{
			sendPacket(new EnchantResult(0));
			setActiveEnchantItem(null);
		}
		if (!disarmWeapons())
			return false;
		
		for (L2Effect e : getAllEffects())
			if (e != null && e.getSkill().isToggle() && e.getSkill().getId() != 5399)
			{
				e.exit();
			}
		
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		setMountObjectID(pet.getControlItemId());
		clearPetData();
		startFeed(pet.getNpcId());
		broadcastPacket(mount);
		broadcastUserInfo();
		pet.unSummon(this);
		return true;
	}
	
	public boolean mountPlayer(L2Summon pet)
	{
		if (pet != null && pet.isMountable() && !isMounted() && !isBetrayed() && !pet.isOutOfControl())
		{
			if (_event != null && !_event.canDoAction(this, RequestActionUse.ACTION_MOUNT))
				return false;
			
			if (isParalyzed() || isPetrified())
				return false;
			
			else if (isDead())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			else if (getActiveEnchantItem() != null)
			{
				sendPacket(new EnchantResult(0));
				setActiveEnchantItem(null);
			}
			else if (pet.isDead())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else if (pet.isInCombat() || pet.isRooted() || pet.isParalyzed() || pet.isPetrified())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;
			}
			if (getActiveTradeList() != null)
			{
				cancelActiveTrade();
				return false;
			}
			if (isProcessingTransaction())
			{
				setActiveEnchantItem(null);
				return false;
			}
			else if (isInCombat())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if (isSitting() || isInWater())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if (isFishing())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			else if (isInDuel())
				return false;
			else if (isCursedWeaponEquipped())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (getInventory().getItemByItemId(Config.FORTSIEGE_COMBAT_FLAG_ID) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (isCastingNow())
				return false;
			else if (pet.isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if (!Util.checkIfInRange(200, this, pet, true))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
				return false;
			}
			else if (!pet.isDead() && !isMounted())
			{
				mount(pet);
			}
		}
		else if (isRentedPet())
		{
			stopRentPet();
		}
		else if (isMounted())
			if (isInCombat() && getMountType() != 1)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (getMountType() == 1 && isInsideZone(L2Zone.FLAG_NOSTRIDER))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_DISMOUNT_HERE));
				return false;
			}
			else if (getMountType() == 2 && isInsideZone(L2Zone.FLAG_NOLANDING))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_DISMOUNT_HERE));
				return false;
			}
			else if (isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT));
				return false;
			}
			else if (ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerUnmount))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
				return false;
			}
			else
			{
				dismount();
				broadcastStatusUpdate();
				broadcastUserInfo();
			}
		return true;
	}
	
	@Override
	public boolean moveToLocation(int x, int y, int z, int offset)
	{
		_sitWhenArrived = false;
		return super.moveToLocation(x, y, z, offset);
	}
	
	@Override
	public boolean mustFallDownOnDeath()
	{
		return super.mustFallDownOnDeath() && Config.FALLDOWNONDEATH;
	}
	
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getStatus().getCurrentCp();
		
		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;
		
		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			return true;
		}
		return false;
	}
	
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getStatus().getCurrentMp();
		
		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;
		
		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			return true;
		}
		return false;
	}
	
	public void notWorking(boolean sendhtml)
	{
		if (sendhtml)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/disabled.htm");
			sendPacket(html);
		}
		else
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_FORBIDEN_BY_ADMIN));
		}
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
			return;
		
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_PC_ITERACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_ACTION_NOT_ALLOWED_DURING_SHUTDOWN));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (Config.SIEGE_ONLY_REGISTERED && !canBeTargetedByAtSiege(player))
		{
			player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_TARGET_WHEN_TARGET_IN_SIEGE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((_inOlympiadMode && !player._inOlympiadMode || !_inOlympiadMode && player._inOlympiadMode || _inOlympiadMode && player._inOlympiadMode && _olympiadGameId != player._olympiadGameId) && !player.isGM())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player._event != _event)
			if (player._event != null && !player._event.canInteract(player, this) || _event != null && !_event.canInteract(player, this) && !player.isGM())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
		}
		else
		{
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
			
			if (getPrivateStoreType() != 0)
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
				
				if (canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
				}
			}
			else if (!isGM() && isAutoAttackable(player))
			{
				if (isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && getLevel() < 21)
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (Config.GEODATA)
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
	}
	
	public void onActionRequest()
	{
		setProtection(false);
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (player.isGM())
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				AdminEditChar.sendHtml(player, this, "charinfo_menu.htm");
			}
		}
		else
		{
			if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_PC_ITERACTION && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_ACTION_NOT_ALLOWED_DURING_SHUTDOWN));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (Config.SIEGE_ONLY_REGISTERED && !canBeTargetedByAtSiege(player))
			{
				player.sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_CANT_TARGET_WHEN_TARGET_IN_SIEGE));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if ((_inOlympiadMode && !player._inOlympiadMode || !_inOlympiadMode && player._inOlympiadMode || _inOlympiadMode && player._inOlympiadMode && _olympiadGameId != player._olympiadGameId) && !player.isGM())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (player.isOutOfControl())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			
			if (player.getTarget() != this)
			{
				player.setTarget(this);
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
				
				if (getPrivateStoreType() != 0)
				{
					if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false))
					{
						player.getAI().setIntention(CtrlIntention.INTERACT, this);
					}
				}
				else if (!isGM() && isAutoAttackable(player))
					if (isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && getLevel() < 21)
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false))
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
		}
	}
	
	private void onDieDropItem(L2Character killer)
	{
		if (_event != null && _event.isRunning() || killer == null)
			return;
		
		L2PcInstance pk = killer.getActingPlayer();
		if (pk != null && getKarma() <= 0 && pk.getClan() != null && getClan() != null && pk.getClan().isAtWarWith(getClanId()))
			return;
		
		if ((!isInsideZone(L2Zone.FLAG_PVP) || pk == null) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKillerNpc = killer instanceof L2Npc;
			int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if (getKarma() == 0 || getPkKills() < pkLimit)
				return;
			
			dropPercent = Config.KARMA_RATE_DROP;
			dropEquip = Config.KARMA_RATE_DROP_EQUIP;
			dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
			dropItem = Config.KARMA_RATE_DROP_ITEM;
			dropLimit = Config.KARMA_DROP_LIMIT;
			
			int dropCount = 0;
			while (dropPercent > 0 && Rnd.get(100) < dropPercent && dropCount < dropLimit)
			{
				int itemDropPercent = 0;
				List<Integer> nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				List<Integer> nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS;
				
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					if (!itemDrop.isDropable() || itemDrop.getItemId() == 57 || itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST || nonDroppableList.contains(itemDrop.getItemId()) || nonDroppableListPet.contains(itemDrop.getItemId()) || getPet() != null && getPet().getControlItemId() == itemDrop.getItemId())
					{
						continue;
					}
					
					if (itemDrop.isEquipped())
					{
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getLocationSlot());
					}
					else
					{
						itemDropPercent = dropItem;
					}
					
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true);
						dropCount++;
						break;
					}
					if (dropCount >= Config.KARMA_DROP_LIMIT)
					{
						break;
					}
				}
			}
			if (Config.ALT_PLAYER_CAN_DROP_ADENA && !isKillerNpc && Config.PLAYER_RATE_DROP_ADENA > 0 && 100 >= Config.PLAYER_RATE_DROP_ADENA && !(killer instanceof L2PcInstance && ((L2PcInstance) killer).isGM()))
			{
				L2ItemInstance itemDrop = getInventory().getAdenaInstance();
				int iCount = getInventory().getAdena();
				iCount = iCount / 100 * Config.PLAYER_RATE_DROP_ADENA;
				
				if (itemDrop != null && itemDrop.getItemId() == 57)
				{
					dropItem("DieDrop", itemDrop.getObjectId(), iCount, getPosition().getX() + Rnd.get(50) - 25, getPosition().getY() + Rnd.get(50) - 25, getPosition().getZ() + 20, killer, true, false);
				}
			}
		}
	}
	
	private void onDieUpdateKarma()
	{
		if (getKarma() > 0)
		{
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel();
			karmaLost *= getLevel() / 100.0;
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
			{
				karmaLost = 1;
			}
			setKarma(getKarma() - (int) karmaLost);
			broadcastFullInfo();
		}
	}
	
	public void onDisarm(L2PcInstance target)
	{
		target.getInventory().unEquipItemInBodySlotAndRecord(14);
	}
	
	public void onFuncAddition(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
		{
			listener.onFuncAddition(f);
		}
	}
	
	public void onFuncRemoval(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
		{
			listener.onFuncRemoval(f);
		}
	}
	
	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null)
			return;
		if (!(target instanceof L2Playable))
			return;
		if (_event != null && _event.isRunning())
			return;
		L2PcInstance targetPlayer = target.getActingPlayer();
		
		if (targetPlayer == null)
			return;
		if (targetPlayer == this)
			return;
		if (isInOlympiadMode() && targetPlayer.isInOlympiadMode())
			return;
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}
		
		if (isInDuel() && targetPlayer.isInDuel())
			return;
		
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;
		
		if (checkIfPvP(target) && targetPlayer.getPvpFlag() != 0 || isInsideZone(L2Zone.FLAG_PVP) && targetPlayer.isInsideZone(L2Zone.FLAG_PVP))
		{
			if (target instanceof L2PcInstance)
			{
				increasePvpKills(targetPlayer.getLevel());
			}
		}
		else
		{
			boolean clanWarKill = targetPlayer.getClan() != null && getClan() != null && !isAcademyMember() && !targetPlayer.isAcademyMember() && _clan.isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(_clan.getClanId());
			if (clanWarKill && target instanceof L2PcInstance)
			{
				increasePvpKills(targetPlayer.getLevel());
				return;
			}
			if (clanWarKill && target instanceof L2Summon)
				return;
			if (targetPlayer.getKarma() > 0)
			{
				if (Config.KARMA_AWARD_PK_KILL && target instanceof L2PcInstance)
				{
					increasePvpKills(targetPlayer.getLevel());
				}
			}
			else if (targetPlayer.getPvpFlag() == 0)
			{
				increaseKarma(target.getLevel());
				if (target instanceof L2PcInstance)
				{
					setPkKills(getPkKills() + 1);
					stopPvPFlag();
					if (Config.ALLOW_PK_REWARD)
					{
						for (int[] item : Config.PK_REWARD_ITEM)
							addItem("", item[0], item[1], this, true);
						sendMessage("You will be rewarded for pk kill!");
					}
				}
				sendPacket(new UserInfo(this));
				if (getInventory().getPaperdollItemId(7) >= 7816 && getInventory().getPaperdollItemId(7) <= 7831)
				{
					L2ItemInstance invItem = getInventory().getItemByItemId(getInventory().getPaperdollItemId(7));
					if (invItem.isEquipped())
					{
						L2ItemInstance[] unequiped = getInventory().unEquipItemInSlotAndRecord(invItem.getLocationSlot());
						InventoryUpdate iu = new InventoryUpdate();
						for (L2ItemInstance itm : unequiped)
						{
							iu.addModifiedItem(itm);
						}
						sendPacket(iu);
					}
					refreshExpertisePenalty();
					sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE);
				}
			}
		}
	}
	
	public void onPartnerDisconnect()
	{
		broadcastFullInfo();
	}
	
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (Config.ENABLE_REWARDSKILL_TIME && Config.EFIREX_GAMESHARK == true)
		{
			startSkillRewardTime();
		}
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS && SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(TeleportWhereType.Town);
				setIsIn7sDungeon(false);
			}
		}
		else if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
		{
			teleToLocation(TeleportWhereType.Town);
			setIsIn7sDungeon(false);
		}
		
		updateJailState();
		
		if (_isInvul)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_ENTER_IN_INVUL_MODE));
		}
		if (getAppearance().isInvisible())
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_ENTER_IN_INVIS_MODE));
		}
		if (getMessageRefusal())
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_ENTER_IN_REFUS_MODE));
		}
		
		if (getVar("firstlogin") == null && Config.ENABLE_FIRSTLOGIN_REWARD && Config.EFIREX_GAMESHARK == true)
		{
			setVar("firstlogin", "1");
			for (int[] item : Config.FIRSTLOGIN_REWARD_ITEM)
				addItem("", item[0], item[1], this, true);
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Você recebeu uma recompensa por seu primeiro login!");
		}
		
		if (_onCounterTask == null && getVar("firstrwdbytime") == null && Config.ENABLE_REWARDITEM_BYTIME && Config.EFIREX_GAMESHARK == true)
			_onCounterTask = startCounterReward();
		
		revalidateZone(true);
		
	}
	
	public void onPvPPkKill(L2Character killer)
	{
		boolean clanWarKill = false;
		boolean playerKill = false;
		boolean charmOfCourage = getCharmOfCourage();
		L2PcInstance pk = killer.getActingPlayer();
		
		if (pk != null && pk._event != null && pk._event.isRunning())
		{
			pk._event.onKill(pk, this);
		}
		else if (pk != null && _event != null && _event.isRunning())
		{
			_event.onKill(killer, this);
		}
		else if (pk != null)
		{
			clanWarKill = pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember() && _clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(getClanId());
			playerKill = true;
		}
		
		if (Config.ANNOUNCE_PK_PVP && pk != null && !pk.isGM())
		{
			String msg = "";
			if (getPvpFlag() == 0)
			{
				msg = Config.ANNOUNCE_PK_MSG.replace("$killer", pk.getName()).replace("$target", getName());
				if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
					Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg));
				else
					Announcements.getInstance().announceToAll(msg);
			}
			else if (getPvpFlag() != 0)
			{
				msg = Config.ANNOUNCE_PVP_MSG.replace("$killer", pk.getName()).replace("$target", getName());
				if (Config.ANNOUNCE_PK_PVP_NORMAL_MESSAGE)
					Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.S1).addString(msg));
				else
					Announcements.getInstance().announceToAll(msg);
			}
		}
		
		boolean srcInPvP = isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_SIEGE);
		
		if (!srcInPvP)
		{
			if (pk == null || !pk.isCursedWeaponEquipped())
			{
				onDieDropItem(killer);
				
				if (!srcInPvP)
					if (Config.ALT_GAME_DELEVEL)
					{
						if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
						{
							deathPenalty(clanWarKill, playerKill, charmOfCourage);
						}
					}
					else if (!(isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_PVP)) || pk == null)
					{
						onDieUpdateKarma();
					}
			}
			if (pk != null)
				if (clanWarKill)
				{
					if (getClan().getReputationScore() >= 0)
					{
						pk.getClan().setReputationScore(pk.getClan().getReputationScore() + Config.ALT_REPUTATION_SCORE_PER_KILL, true);
						getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
						pk.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(pk.getClan()));
					}
					if (pk.getClan().getReputationScore() >= Config.ALT_REPUTATION_SCORE_PER_KILL)
					{
						_clan.setReputationScore(_clan.getReputationScore() - Config.ALT_REPUTATION_SCORE_PER_KILL, true);
						getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
						pk.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(pk.getClan()));
						
						_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_KILLED_AND_S2_POINTS_DEDUCTED_FROM_REPUTATION).addString(getName()).addString(Config.ALT_REPUTATION_SCORE_PER_KILL_SM));
					}
				}
		}
	}
	
	@Override
	public void onSpawn()
	{
		getKnownList();
		super.onSpawn();
	}
	
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		getKnownList().updateKnownObjects();
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0 && !isInOlympiadMode())
		{
			setProtection(true);
		}
		
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().decayMe();
			setTrainedBeast(null);
		}
		
		if (getPet() != null)
		{
			getPet().setFollowStatus(false);
			getPet().teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
			((L2SummonAI) getPet().getAI()).setStartFollowController(true);
			getPet().setFollowStatus(true);
			getPet().broadcastFullInfoImpl(0);
		}
	}
	
	public void onTradeCancel(L2PcInstance partner)
	{
		if (_activeTradeList == null)
			return;
		
		_activeTradeList.lock();
		_activeTradeList = null;
		sendPacket(new SendTradeDone(0));
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));
	}
	
	public void onTradeConfirm(L2PcInstance partner)
	{
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
		{
			getInventory().updateDatabase();
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TRADE_SUCCESSFUL));
		}
	}
	
	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		setActiveEnchantItem(null);
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
		sendPacket(new TradeStart(this));
	}
	
	public void onTransactionRequest(L2PcInstance partner)
	{
		setActiveEnchantItem(null);
		_requestExpireTime = System.currentTimeMillis() + REQUEST_TIMEOUT * 1000;
		partner.setActiveRequester(this);
	}
	
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	private void processAddItem(boolean UpdateIL, L2ItemInstance newitem)
	{
		if (newitem == null)
			return;
		if (!isGM() && !getInventory().validateCapacity(0))
		{
			dropItem("InvDrop", newitem, null, true, true);
		}
		else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
		{
			CursedWeaponsManager.getInstance().activate(this, newitem);
		}
		else if (FortSiegeManager.getInstance().isCombat(newitem.getItemId()))
		{
			if (FortSiegeManager.getInstance().activateCombatFlag(this, newitem))
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
				{
					fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1), getName() + " picked up the flag");
				}
			}
		}
		else if (isBuffShop())
		{
			BuffShopManager.updateShopTitle(this);
		}
		else if (newitem.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getItemHandler(newitem.getItemId());
			if (handler == null)
			{
				_log.warn("No item handler registered for item ID " + newitem.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, newitem);
			}
			return;
		}
		
		if (UpdateIL)
		{
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
		}
		
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newitem);
			sendPacket(playerIU);
		}
	}
	
	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
		{
			event = "";
		}
		if (!_quests.containsKey(quest))
			return retval;
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return retval;
		if (qs == null)
		{
			Quest q = QuestManager.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null)
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = getKnownList().getKnownObject(getLastQuestNpcObject());
				if (object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());
					if (states != null)
					{
						for (QuestState state : states)
							if (state.getQuest().getName().equals(qs.getQuest().getName()))
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
								{
									showQuestWindow(quest, State.getStateName(qs.getState()));
								}
								
								retval = qs;
							}
						sendPacket(new QuestList(this));
					}
				}
			}
		
		return retval;
	}
	
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				continue;
			}
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}
		
		if (_hennaINT > 5)
		{
			_hennaINT = 5;
		}
		if (_hennaSTR > 5)
		{
			_hennaSTR = 5;
		}
		if (_hennaMEN > 5)
		{
			_hennaMEN = 5;
		}
		if (_hennaCON > 5)
		{
			_hennaCON = 5;
		}
		if (_hennaWIT > 5)
		{
			_hennaWIT = 5;
		}
		if (_hennaDEX > 5)
		{
			_hennaDEX = 5;
		}
	}
	
	public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon, boolean animation)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if (_activeSoulShots == null || _activeSoulShots.size() == 0)
			return;
		
		Collection<Integer> vals = _activeSoulShots.values();
		
		synchronized (_activeSoulShots)
		{
			for (int itemId : vals)
			{
				item = getInventory().getItemByItemId(itemId);
				
				if (item != null)
				{
					if (magic)
						if (!summon)
						{
							if (itemId >= 2509 && itemId <= 2514 || itemId >= 3947 && itemId <= 3952 || itemId == 5790)
							{
								handler = ItemHandler.getInstance().getItemHandler(itemId);
								if (handler != null)
								{
									handler.useItem(this, item, animation);
								}
							}
						}
						else if (itemId == 6646 || itemId == 6647)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							if (handler != null)
							{
								handler.useItem(this, item, animation);
							}
						}
					
					if (physical)
						if (!summon)
						{
							if (itemId >= 1463 && itemId <= 1467 || itemId == 1835 || itemId == 5789)
							{
								handler = ItemHandler.getInstance().getItemHandler(itemId);
								if (handler != null)
								{
									handler.useItem(this, item, animation);
								}
							}
						}
						else if (itemId == 6645)
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							if (handler != null)
							{
								handler.useItem(this, item, animation);
							}
						}
				}
				else
				{
					removeAutoSoulShot(itemId);
				}
			}
		}
	}
	
	public final void recoverPet()
	{
		targetPet = null;
		targetPet.setCurrentFed(targetPet.getMaxFed());
		targetPet.setCurrentHpMp(targetPet.getMaxHp(), targetPet.getMaxMp());
		targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
	}
	
	public boolean reduceAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = getInventory().getAdenaInstance();
			getInventory().reduceAdena(process, count, this, reference);
			
			getInventory().updateInventory(adenaItem);
			
			if (sendMessage)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED).addNumber(count));
			}
		}
		
		return true;
	}
	
	public boolean reduceAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			
			return false;
		}
		
		if (count > 0)
		{
			getInventory().reduceAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
			if (sendMessage)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(count));
			}
		}
		
		return true;
	}
	
	@Override
	protected void reduceArrowCount(boolean bolts)
	{
		L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (arrows == null)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			sendPacket(new ItemList(this, false));
			return;
		}
		
		if (!Config.CONSUME_ARROWS)
			return;
		
		if (arrows.getCount() > 1)
		{
			synchronized (arrows)
			{
				arrows.changeCountWithoutTrace(-1, this, null);
				arrows.setLastChange(L2ItemInstance.MODIFIED);
				getInventory().refreshWeight();
			}
		}
		else
		{
			getInventory().destroyItem("Consume", arrows, this, null);
			
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			
			sendPacket(new ItemList(this, false));
			return;
		}
		
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
	}
	
	@Override
	public void reduceCurrentHp(double value, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		getStatus().reduceHp(value, attacker, awake, isDOT);
		
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		}
		else
		{
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}
		
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void reduceOlyDamage(int dmg)
	{
		if (_olyDamage - dmg < 0)
		{
			_olyDamage = 0;
		}
		else
		{
			_olyDamage = _olyDamage - dmg;
		}
	}
	
	public void refreshConditionListeners(ConditionListenerDependency dependency)
	{
		for (ConditionListener listener : getConditionListeners())
		{
			listener.refresh(dependency);
		}
	}
	
	public void refreshExpertisePenalty()
	{
		if (Config.GRADE_PENALTY)
		{
			int newPenalty = 0;
			
			for (L2ItemInstance item : getInventory().getItems())
				if (item != null)
					if (item.isEquipped() && item.getItem() != null)
					{
						int crystaltype = item.getItem().getCrystalType();
						
						if (crystaltype > newPenalty)
						{
							newPenalty = crystaltype;
						}
					}
				
			newPenalty = newPenalty - getItemExpertiseIndex();
			if (newPenalty <= 0)
			{
				newPenalty = 0;
			}
			
			if (getExpertisePenalty() != newPenalty || hasSkill(4267) != newPenalty > 0)
			{
				_expertisePenalty = newPenalty;
				
				if (newPenalty > 0)
				{
					super.addSkill(SkillTable.getInstance().getInfo(4267, 1));
					sendSkillList();
				}
				else
				{
					super.removeSkill(getKnownSkill(4267));
					sendSkillList();
					_expertisePenalty = 0;
				}
				
				sendPacket(new EtcStatusUpdate(this));
			}
		}
	}
	
	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		int newWeightPenalty = 0;
		
		if (maxLoad > 0)
		{
			setIsOverloaded(getCurrentLoad() > maxLoad && !_dietMode);
			int weightproc = getCurrentLoad() * 1000 / maxLoad;
			weightproc = (int) calcStat(Stats.WEIGHT_LIMIT, weightproc, this, null);
			
			if (weightproc < 500 || _dietMode)
			{
				newWeightPenalty = 0;
			}
			else if (weightproc < 666)
			{
				newWeightPenalty = 1;
			}
			else if (weightproc < 800)
			{
				newWeightPenalty = 2;
			}
			else if (weightproc < 1000)
			{
				newWeightPenalty = 3;
			}
			else
			{
				newWeightPenalty = 4;
			}
		}
		
		if (_curWeightPenalty != newWeightPenalty)
		{
			_curWeightPenalty = newWeightPenalty;
			if (newWeightPenalty > 0)
			{
				super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
				sendPacket(new UserInfo(this));
				sendSkillList();
			}
			else
			{
				super.removeSkill(getKnownSkill(4270));
				sendPacket(new UserInfo(this));
				sendSkillList();
			}
			
			sendPacket(new EtcStatusUpdate(this));
		}
	}
	
	public void registerCommonRecipeList(L2RecipeList recipe)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	public void registerDwarvenRecipeList(L2RecipeList recipe)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	public void registerMacro(L2Macro macro)
	{
		getMacroses().registerMacro(macro);
	}
	
	public void registerShortCut(L2ShortCut shortcut)
	{
		getShortCuts().registerShortCut(shortcut);
	}
	
	public void regiveTemporarySkills()
	{
		if (isNoble())
		{
			setNoble(true);
		}
		
		if (isHero())
		{
			setHero(true);
		}
		
		if (getClan() != null)
		{
			setPledgeClass(L2ClanMember.getCurrentPledgeClass(this));
			getClan().addSkillEffects(this, false);
			sendPacket(new PledgeSkillList(getClan()));
			if (getClan().getLevel() >= Config.SIEGE_CLAN_MIN_LEVEL && isClanLeader())
			{
				SiegeManager.addSiegeSkills(this);
			}
			if (getClan().getHasCastle() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);
			}
			if (getClan().getHasFort() > 0)
			{
				FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
			}
		}
		getInventory().reloadEquippedItems();
		restoreDeathPenaltyBuffLevel();
	}
	
	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(itemId);
	}
	
	public void removeConfirmDlgRequestTime(int requestId)
	{
		confirmDlgRequests.remove(requestId);
	}
	
	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;
		
		slot--;
		
		if (_henna[slot] == null)
			return false;
		
		L2HennaInstance henna = _henna[slot];
		_henna[slot] = null;
		
		Connection con = null;
		
		broadcastFullInfo();
		
		try
		{
			broadcastFullInfo();
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed removing character henna.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
			broadcastFullInfo();
		}
		
		broadcastFullInfo();
		
		recalcHennaStats();
		
		sendPacket(new HennaInfo(this));
		
		sendPacket(new UserInfo(this));
		
		L2ItemInstance dye = getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);
		getInventory().updateInventory(dye);
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(henna.getItemIdDye()).addNumber(henna.getAmountDyeRequire() / 2));
		
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		return true;
	}
	
	public void removeItemFromShortCut(int objectId)
	{
		getShortCuts().deleteShortCutByObjectId(objectId);
	}
	
	public void removePetReviving()
	{
		_revivePetRequested = false;
		_revivePetPower = 0;
	}
	
	public void removeReviving()
	{
		_reviveRequested = false;
		_revivePower = 0;
	}
	
	@Override
	public L2Skill removeSkill(L2Skill skill)
	{
		L2Skill oldSkill = super.removeSkill(skill);
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
			
			if (oldSkill != null)
			{
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error could not delete skill: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		if (isCursedWeaponEquipped())
			return oldSkill;
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
			if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		
		return oldSkill;
	}
	
	@Override
	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		return store ? removeSkill(skill) : super.removeSkill(skill, true);
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean store, boolean cancelEffect)
	{
		if (store)
			return removeSkill(skill);
		
		return super.removeSkill(skill, cancelEffect);
	}
	
	public void removeSnooped(L2PcInstance snooped)
	{
		if (_snoopedPlayer != null)
		{
			_snoopedPlayer.remove(snooped);
			if (_snoopedPlayer.size() == 0)
			{
				_snoopedPlayer = null;
			}
		}
	}
	
	public void removeSnooper(L2PcInstance pci)
	{
		if (_snoopListener != null)
		{
			_snoopListener.remove(pci);
			if (_snoopListener.size() == 0)
			{
				_snoopListener = null;
			}
		}
	}
	
	@Override
	public void removeTimeStamp(int s)
	{
		_reuseTimeStamps.remove(s);
	}
	
	public void resetSkillTime(boolean ssl)
	{
		for (L2Skill skill : getAllSkills())
			if (skill != null)
				if (skill.isActive())
				{
					enableSkill(skill.getId());
				}
		if (ssl)
		{
			sendSkillList();
		}
		sendPacket(new SkillCoolTime(this));
	}
	
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChargeTask(), 600000);
	}
	
	private Calendar restartRecom()
	{
		if (Config.ALT_RECOMMEND)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS);
				statement.setInt(1, getObjectId());
				
				statement.execute();
				statement.close();
				
				_recomChars.clear();
			}
			catch (Exception e)
			{
				_log.error("Error clearing char recommendations.", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		
		if (getStat().getLevel() < 20)
		{
			_recomLeft = 3;
			_recomHave--;
		}
		else if (getStat().getLevel() < 40)
		{
			_recomLeft = 6;
			_recomHave -= 2;
		}
		else
		{
			_recomLeft = 9;
			_recomHave -= 3;
		}
		if (_recomHave < 0)
		{
			_recomHave = 0;
		}
		
		Calendar check = Calendar.getInstance();
		check.setTimeInMillis(_lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		check.set(Calendar.HOUR_OF_DAY, 13);
		check.set(Calendar.MINUTE, 0);
		check.set(Calendar.SECOND, 0);
		_lastRecomUpdate = check.getTimeInMillis();
		return check;
	}
	
	private void restoreCharData(Connection con)
	{
		restoreSkills(con);
		
		getMacroses().restore(con);
		
		getShortCuts().restore(con);
		
		restoreHenna(con);
		
		if (Config.ALT_RECOMMEND)
		{
			restoreRecom(con);
		}
		
		restoreRecipeBook(con);
		
	}
	
	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		}
	}
	
	public void restoreEffects()
	{
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			ResultSet rset;
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 0);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				
				long remainingTime = systime - System.currentTimeMillis();
				
				if (skillId == -1 || effectCount == -1 || effectCurTime == -1 || reuseDelay < 0)
				{
					continue;
				}
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if (skill != null)
				{
					skill.getEffects(this, this);
				}
				
				if (remainingTime > 10)
				{
					disableSkill(skillId, remainingTime);
					addTimeStamp(new TimeStamp(skillId, reuseDelay, systime));
				}
				
				for (L2Effect effect : getAllEffects())
				{
					if (effect.getSkill().getId() == skillId)
					{
						effect.setCount(effectCount);
						effect.setFirstTime(effectCurTime);
					}
					if (effect.getEffectType() == L2EffectType.CHARMOFCOURAGE)
					{
						setCanUseCharmOfCourageItem(false);
						setCanUseCharmOfCourageRes(false);
					}
				}
			}
			rset.close();
			statement.close();
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 1);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				
				long remainingTime = systime - System.currentTimeMillis();
				
				if (remainingTime < 10)
				{
					continue;
				}
				
				disableSkill(skillId, remainingTime);
				addTimeStamp(new TimeStamp(skillId, reuseDelay, systime));
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore active effect data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}
	
	private void restoreHenna(Connection con)
	{
		
		try
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				
				if (slot < 1 || slot > 3)
				{
					continue;
				}
				
				int symbol_id = rset.getInt("symbol_id");
				
				L2HennaInstance sym = null;
				
				if (symbol_id != 0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
					
					if (tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot - 1] = sym;
						tpl = null;
						sym = null;
					}
				}
				
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed restoing character hennas.", e);
		}
		
		recalcHennaStats();
	}
	
	public void restoreHeroServiceData(L2PcInstance player)
	{
		boolean sucess = false;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_HEROSERVICE);
			statement.setInt(1, player.getObjectId());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				sucess = true;
				long enddate = rset.getLong("enddate");
				
				if (enddate <= System.currentTimeMillis())
				{
					if (enddate != 0)
					{
						PcAction.deleteHeroItems(player);
					}
					HStimeOver(player);
				}
				else
				{
					player.setHero(true);
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("HeroService: Could not restore HeroStatus data for " + player.getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		if (sucess == false)
		{
			createHSdb(player);
		}
	}
	
	public void restoreHPMP()
	{
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}
	
	private void restoreRecipeBook(Connection con)
	{
		
		try
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE charId=?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			L2RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id"));
				
				if (rset.getInt("type") == 1)
				{
					registerDwarvenRecipeList(recipe);
				}
				else
				{
					registerCommonRecipeList(recipe);
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore recipe book data:", e);
		}
	}
	
	private void restoreRecom(Connection con)
	{
		
		try
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_recomChars.add(rset.getInt("target_id"));
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("could not restore recommendations: ", e);
		}
	}
	
	private void restoreSkills(Connection con)
	{
		
		try
		{
			boolean isAcumulative = Config.ACUMULATIVE_SUBCLASS_SKILLS;
			
			PreparedStatement statement = con.prepareStatement(isAcumulative ? ACUMULATE_SKILLS_FOR_CHAR_SUB : RESTORE_SKILLS_FOR_CHAR);
			statement.setInt(1, getObjectId());
			if (!isAcumulative)
			{
				statement.setInt(2, getClassIndex());
			}
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				
				if (id > 9000 && id < 9007)
				{
					continue;
				}
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				
				super.addSkill(skill);
			}
			
			rset.close();
			statement.close();
			
			if (_clan != null)
			{
				_clan.addSkillEffects(this, false);
			}
		}
		catch (Exception e)
		{
			_log.error("Could not restore character skills: ", e);
		}
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
				return;
		}
		
		if (getWorldRegion() == null)
			return;
		getWorldRegion().revalidateZones(this);
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		if (isInsideZone(L2Zone.FLAG_SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				return;
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
		}
		else if (isInsideZone(L2Zone.FLAG_PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE));
		}
		else if (isInsideZone(L2Zone.FLAG_PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
				return;
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
				return;
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				updatePvPStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		}
	}
	
	public void reviveAnswer(int answer)
	{
		if (!(_reviveRequested && isDead() || _revivePetRequested && getPet() != null && getPet().isDead()))
			return;
		if (answer == 0 && isPhoenixBlessed() && isDead() && _reviveRequested)
		{
			stopPhoenixBlessing(null);
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (answer == 1)
			if (_reviveRequested)
			{
				if (_revivePower != 0)
				{
					doRevive(_revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (_revivePetRequested && getPet() != null)
				if (_revivePower != 0)
				{
					getPet().doRevive(_revivePower);
				}
				else
				{
					getPet().doRevive();
				}
		_reviveRequested = false;
		_revivePower = 0;
	}
	
	public void revivePetRequest(L2PcInstance reviver, L2Skill skill)
	{
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
			return;
		}
		
		if (getPet().isDead() && getPet() instanceof L2PetInstance)
		{
			_revivePetRequested = true;
			if (skill != null)
			{
				_revivePetPower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			}
			else
			{
				_revivePetPower = 0;
			}
			
			int restoreExp = (int) Math.round((((L2PetInstance) getPet()).getExpBeforeDeath() - getPet().getStat().getExp()) * _revivePetPower / 100);
			
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1_MAKING_RESSURECTION_REQUEST.getId());
			sendPacket(dlg.addPcName(reviver).addString("" + restoreExp));
		}
	}
	
	public void reviveRequest(L2PcInstance reviver, L2Skill skill)
	{
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
			return;
		}
		if (isDead())
		{
			_reviveRequested = true;
			if (isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else if (skill != null)
			{
				_revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			}
			else
			{
				_revivePower = 0;
			}
			
			int restoreExp = (int) Math.round((getExpBeforeDeath() - getExp()) * _revivePower / 100);
			
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1_MAKING_RESSURECTION_REQUEST.getId());
			sendPacket(dlg.addPcName(reviver).addString("" + restoreExp));
		}
	}
	
	public void rewardSkills()
	{
		int lvl = getLevel();
		
		if (lvl > 9)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
		}
		
		calcExpertiseLevel();
		
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill);
		}
		else
		{
			
		}
		
		if (getSkillLevel(1321) < 1 && getRace() == Race.Dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill);
		}
		
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill);
		}
		
		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < i + 1)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, i + 1);
				addSkill(skill);
			}
		
		if (Config.AUTO_LEARN_SKILLS)
		{
			if (isAio())
				return;
			
			if (isCursedWeaponEquipped())
				return;
			if (Config.AUTO_LEARN_MAX_LEVEL > 0 && getLevel() > Config.AUTO_LEARN_MAX_LEVEL)
				return;
			giveAvailableSkills();
		}
		refreshOverloaded();
		refreshExpertisePenalty();
		sendSkillList();
	}
	
	public void saveSettingInDb(Connection con) throws SQLException
	{
		_characterData.set("nameColor", getAppearance().getNameColor());
		_characterData.set("titleColor", getAppearance().getTitleColor());
		PreparedStatement statement = con.prepareStatement(STORE_CHAR_DATA);
		statement.setInt(2, getObjectId());
		PreparedStatement insert = con.prepareStatement(CREATE_CHAR_DATA);
		insert.setInt(1, getObjectId());
		for (String s : _characterData.getSet().keySet())
		{
			try
			{
				statement.setString(1, _characterData.getString(s));
			}
			catch (IllegalArgumentException e)
			{
				statement.setString(1, "");
			}
			statement.setString(3, s);
			if (statement.executeUpdate() == 0)
			{
				insert.setString(2, s);
				try
				{
					insert.setString(3, _characterData.getString(s));
				}
				catch (IllegalArgumentException e)
				{
					insert.setString(3, "");
				}
				insert.execute();
			}
		}
		insert.close();
		statement.close();
		
	}
	
	public void sendChatMessage(int objectId, SystemChatChannelId messageType, String charName, String text)
	{
		sendPacket(new CreatureSay(objectId, SystemChatChannelId.Chat_None, charName, text));
	}
	
	public void sendEtcStatusUpdate()
	{
		sendEtcStatusUpdateImpl();
	}
	
	public void sendEtcStatusUpdateImpl()
	{
		sendPacket(new EtcStatusUpdate(this));
	}
	
	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	private void sendMessageForNewItem(L2ItemInstance item, int count, String process)
	{
		if (item == null)
			return;
		
		if (count > 1)
		{
			if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(count));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(item).addNumber(count));
			}
		}
		else if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(item));
		}
		else
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
		}
	}
	
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		final L2GameClient client = _client;
		if (client != null)
		{
			client.sendPacket(packet);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId sm)
	{
		sendPacket(sm.getSystemMessage());
	}
	
	public void sendSkillList()
	{
		L2Skill[] array = getAllSkills();
		List<L2Skill> skills = new ArrayList<>(array.length);
		
		for (L2Skill s : array)
		{
			if (s == null)
			{
				continue;
			}
			
			if (s.bestowed())
			{
				continue;
			}
			
			if (s.getSkillType() == L2SkillType.NOTDONE)
			{
				switch (Config.SEND_NOTDONE_SKILLS)
				{
					case 1:
						continue;
					case 2:
						if (!isGM())
						{
							continue;
						}
				}
			}
			
			skills.add(s);
		}
		
		sendPacket(new SkillList(skills));
	}
	
	public void setAccountAccesslevel(int level)
	{
		AuthServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	public synchronized boolean setActiveClass(int classIndex)
	{
		for (L2ItemInstance temp : getInventory().getAugmentedItems())
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().removeBonus(this);
			}
		
		L2ItemInstance circlet = getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIRALL);
		if (circlet != null)
			if ((circlet.getItemId() >= 9397 && circlet.getItemId() <= 9408 || circlet.getItemId() == 10169) && circlet.isEquipped())
			{
				L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(circlet.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance element : unequipped)
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		
		if (_fusionSkill != null)
		{
			abortCast();
		}
		
		for (L2Character character : getKnownList().getKnownCharacters())
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
			{
				character.abortCast();
			}
		
		if (_classIndex == 0)
		{
			_baseLevel = getLevel();
			_baseExp = getExp();
			_baseSP = getSp();
		}
		store();
		
		if (classIndex == 0)
		{
			setClassTemplate(getBaseClass());
		}
		else
		{
			try
			{
				setClassTemplate(getSubClasses().get(classIndex).getClassId());
			}
			catch (Exception e)
			{
				_log.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e);
				return false;
			}
		}
		
		_classIndex = classIndex;
		
		if (isInParty())
			if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
			{
				for (L2PcInstance p : getParty().getPartyMembers())
					if (Math.abs(p.getLevel() - getLevel()) > Config.MAX_PARTY_LEVEL_DIFFERENCE)
					{
						getParty().removePartyMember(this);
						sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_REMOVE_FROM_PARTY_BIG_LVL_DIF));
						break;
					}
			}
			else
			{
				getParty().recalculatePartyLevel();
			}
		
		if (getPet() != null && getPet() instanceof L2SummonInstance)
		{
			getPet().unSummon(this);
		}
		
		if (!getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			getCubics().clear();
		}
		
		abortCast();
		
		for (L2Skill oldSkill : getAllSkills())
		{
			super.removeSkill(oldSkill);
		}
		
		stopAllEffectsExceptThoseThatLastThroughDeath();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			restoreDeathPenaltyBuffLevel();
			restoreSkills(con);
			regiveTemporarySkills();
			rewardSkills();
			restoreEffects();
			updateEffectIcons();
			
			QuestState st = getQuestState("422_RepentYourSins");
			
			if (st != null)
			{
				st.exitQuest(true);
			}
			
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			restoreHenna(con);
			sendPacket(new HennaInfo(this));
			
			if (getStatus().getCurrentHp() > getMaxHp())
			{
				getStatus().setCurrentHp(getMaxHp());
			}
			if (getStatus().getCurrentMp() > getMaxMp())
			{
				getStatus().setCurrentMp(getMaxMp());
			}
			if (getStatus().getCurrentCp() > getMaxCp())
			{
				getStatus().setCurrentCp(getMaxCp());
			}
			getInventory().restoreEquipedItemsPassiveSkill();
			getInventory().restoreArmorSetPassiveSkill();
			getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
			getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
			getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_L_HAND);
			sendPacket(new ItemList(this, false));
			broadcastUserInfo();
			refreshOverloaded();
			refreshExpertisePenalty();
			setExpBeforeDeath(0);
			getShortCuts().restore(con);
			sendPacket(new ShortCutInit(this));
			broadcastPacket(new SocialAction(this, 15));
			sendPacket(new SkillCoolTime(this));
			broadcastClassIcon();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		for (L2ItemInstance item : getInventory().getItems())
			if (item.isEquipped() && item.getItem() instanceof L2Armor)
			{
				getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
		sendPacket(new ItemList(this, false));
		intemediateStore();
		return true;
	}
	
	public void setActiveEnchantItem(L2ItemInstance scroll)
	{
		if (scroll == null)
		{
			setIsEnchanting(false);
		}
		
		_activeEnchantItem = scroll;
	}
	
	public synchronized void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
		if (_activeRequester != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					_activeRequester = null;
					_requestExpireTime = 0;
				}
			}, REQUEST_TIMEOUT * 10000);
		}
	}
	
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}
	
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
		
	}
	
	public void setBoat(L2BoatInstance boat)
	{
		_boat = boat;
	}
	
	public void setBoatId(int boatId)
	{
		_boatId = boatId;
	}
	
	public void setCanUseCharmOfCourageItem(boolean value)
	{
		_canUseCharmOfCourageItem = value;
	}
	
	public void setCanUseCharmOfCourageRes(boolean value)
	{
		_canUseCharmOfCourageRes = value;
		sendEtcStatusUpdate();
	}
	
	public void setCharId(int charId)
	{
		
	}
	
	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;
		sendEtcStatusUpdate();
	}
	
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_pledgeRank = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getObjectId()))
		{
			setClan(null);
			return;
		}
		
		_clanId = clan.getClanId();
	}
	
	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}
	
	public void setClassId(int Id)
	{
		academyCheck(Id);
		if (isSubClassActive())
		{
			getSubClasses().get(_classIndex).setClassId(Id);
		}
		setClassTemplate(Id);
		
		setTarget(this);
		broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1196, 0, false));
		broadcastClassIcon();
		
		if (getClassId().level() == 3)
		{
			sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
		}
		else
		{
			sendPacket(SystemMessageId.CLASS_TRANSFER);
		}
		
		if (isInParty())
		{
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		}
		
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
		
		if (Config.AUTO_LEARN_SKILLS)
		{
			if (isAio())
				return;
			
			rewardSkills();
			sendSkillList();
		}
		intemediateStore();
	}
	
	private void setClassTemplate(int classId)
	{
		_activeClass = classId;
		
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);
		
		if (t == null)
		{
			_log.fatal("Missing template for classId: " + classId);
			throw new Error();
		}
		
		setTemplate(t);
	}
	
	public void setClient(L2GameClient client)
	{
		if ((client == null) && (_client != null))
		{
			_client.stopGuardTask();
			
		}
		_client = client;
		updateOnlineStatus();
	}
	
	public void setClientRevision(int clientrev)
	{
		_clientRevision = clientrev;
	}
	
	public void setCombatFlagEquipped(boolean value)
	{
		_combatFlagEquipped = value;
		sendPacket(new InventoryUpdate());
	}
	
	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}
	
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public void setCurrentFeed(int num)
	{
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		sendPacket(new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(), getMaxFeed() * 10000 / getFeedConsume()));
	}
	
	@Override
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentPetSkill = null;
			return;
		}
		
		_currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentSkill = null;
			return;
		}
		_currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public void setCurrentSkillWorldPosition(Point3D worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}
	
	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}
	
	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}
	
	public void setDmKills(int x)
	{
		_dmKills = x;
	}
	
	public void setDuelState(int mode)
	{
		_duelState = mode;
	}
	
	public void setEndOfflineTime(boolean restore, long endTime)
	{
		if (!restore)
		{
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.add(Calendar.HOUR_OF_DAY, Config.ALLOW_OFFLINE_HOUR);
			
			_endOfflineTime = finishtime.getTimeInMillis();
		}
		else
		{
			_endOfflineTime = endTime;
		}
	}
	
	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	public void setExp(long exp)
	{
		getStat().setExp(exp);
	}
	
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}
	
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}
	
	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
	
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public final boolean setFloodCount(Protected action, long value)
	{
		if (action.ordinal() > _floodCount.length)
			return false;
		_floodCount[action.ordinal()] = value;
		return true;
	}
	
	public void setGmSetting(boolean gm, boolean res, boolean altg, boolean peace)
	{
		_GmStatus = gm;
		_AllowFixRes = res;
		_AllowAltG = altg;
		_AllowPeaceAtk = peace;
	}
	
	public void setHero(boolean hero)
	{
		if (hero && _baseClass == _activeClass)
		{
			for (L2Skill s : HeroSkillTable.getHeroSkills())
			{
				addSkill(s, false);
			}
		}
		else if (hero && Config.ALLOW_HERO_SUBSKILL)
		{
			for (L2Skill s : HeroSkillTable.getHeroSkills())
			{
				addSkill(s, false);
			}
		}
		else
		{
			for (L2Skill s : HeroSkillTable.getHeroSkills())
			{
				super.removeSkill(s);
			}
		}
		_hero = hero;
		sendSkillList();
	}
	
	public void setInBoat(boolean inBoat)
	{
		_inBoat = inBoat;
	}
	
	public void setInBoatPosition(Point3D pt)
	{
		_inBoatPosition = pt;
	}
	
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	public void setInJail(boolean state)
	{
		_inJail = state;
	}
	
	public void setInJail(boolean state, int delayInMinutes)
	{
		_inJail = state;
		stopJailTask();
		
		if (_inJail)
		{
			if (delayInMinutes > 0)
			{
				_jailTimer = delayInMinutes * 60000L;
				
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
				sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", String.format(Message.getMessage(this, Message.MessageId.MSG_YOU_JAILED_FOR_X_MINUTES), delayInMinutes));
			}
			
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
			if (jailInfos != null)
			{
				htmlMsg.setHtml(jailInfos);
			}
			else
			{
				htmlMsg.setHtml("<html><body>You sent to jail by the administration.</body></html>");
			}
			sendPacket(htmlMsg);
			
			teleToLocation(-114356, -249645, -2984, false); // Jail
		}
		else
		{
			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
			if (jailInfos != null)
			{
				htmlMsg.setHtml(jailInfos);
			}
			else
			{
				htmlMsg.setHtml("<html><body>You are released from prison. Examine the server rules!</body></html>");
			}
			sendPacket(htmlMsg);
			
			teleToLocation(17836, 170178, -3507); // Floran
		}
		
	}
	
	@Override
	public void setInsideZone(L2Zone zone, byte zoneType, boolean state)
	{
		super.setInsideZone(zone, zoneType, state);
		if (state && zoneType == L2Zone.FLAG_NOSUMMON && isFlying())
		{
			enteredNoLanding();
		}
		super.setInsideZone(zone, zoneType, state);
		if (state && zoneType == L2Zone.FLAG_NOSTRIDER && isMounted())
		{
			enteredNoLanding();
		}
	}
	
	public void setIsBanned(boolean val)
	{
		_isBanned = val;
	}
	
	@Override
	public void setIsCastingNow(boolean value)
	{
		if (!value)
		{
			_isSummoning = false;
			_currentSkill = null;
		}
		super.setIsCastingNow(value);
	}
	
	public void setIsEnchanting(boolean val)
	{
		_isEnchanting = val;
	}
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		_isIn7sDungeon = isIn7sDungeon;
	}
	
	public void setIsInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}
	
	public void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}
	
	public void setIsOlympiadStart(boolean b)
	{
		_olympiadStart = b;
		if (b)
		{
			setOlyDamage(0);
		}
	}
	
	public void setIsPartyInvProt(boolean value)
	{
		_isPartyInvProt = value;
	}
	
	public final void setIsRidingRedStrider(boolean mode)
	{
		_isRidingRedStrider = mode;
	}
	
	public final void setIsRidingStrider(boolean mode)
	{
		_isRidingStrider = mode;
	}
	
	public void setIsSitting(boolean state)
	{
		_waitTypeSitting = state;
	}
	
	public void setIsWearingFormalWear(boolean value)
	{
		_IsWearingFormalWear = value;
	}
	
	public void setItemExpertiseIndex(int expertiseIndex)
	{
		_itemExpertiseIndex = expertiseIndex;
	}
	
	public void setJailTimer(long time)
	{
		_jailTimer = time;
	}
	
	public void setKarma(int karma)
	{
		if (karma < 0)
		{
			karma = 0;
		}
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects().values())
			{
				if (!(object instanceof L2GuardInstance))
				{
					continue;
				}
				
				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.IDLE)
				{
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.ACTIVE, null);
				}
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			setKarmaFlag(0);
		}
		_karma = karma;
		broadcastKarma();
	}
	
	public void setKarmaFlag(int flag)
	{
		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}
	
	public void setKnowlistMode(boolean showOff)
	{
		getKnownList().updateKnownObjects(true);
		_showTraders = showOff;
	}
	
	public void setLang(String lang)
	{
		getAccountData().set("lang", lang);
	}
	
	public void setLastFolkNPC(L2NpcInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	public void setLastPage(String html)
	{
		LAST_BBS_PAGE[0] = LAST_BBS_PAGE[1];
		LAST_BBS_PAGE[1] = html;
	}
	
	public void setLastPartyPosition(int x, int y, int z)
	{
		getLastPartyPosition().setXYZ(x, y, z);
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	public void setLastRecomUpdate(long date)
	{
		_lastRecomUpdate = date;
	}
	
	public void setLastServerPosition(int x, int y, int z)
	{
		getLastServerPosition().setXYZ(x, y, z);
	}
	
	public void setLookingForParty(boolean matching)
	{
		_lookingForParty = matching;
	}
	
	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	
	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}
	
	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}
	
	public void setMaried(boolean state)
	{
		_maried = state;
	}
	
	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}
	
	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}
	
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}
	
	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendEtcStatusUpdate();
	}
	
	public void setMiniMapOpen(boolean par)
	{
		_miniMapOpen = par;
	}
	
	public boolean setMount(int npcId, int npcLevel, int mountType)
	{
		switch (mountType)
		{
			case 0:
				setIsRidingStrider(false);
				setIsRidingRedStrider(false);
				setIsFlying(false);
				isFalling(false, 0);
				break;
			case 1:
				if (npcId >= 12526 && npcId <= 12528)
				{
					setIsRidingStrider(true);
				}
				else if (npcId >= 16038 && npcId <= 16040)
				{
					setIsRidingRedStrider(true);
				}
				if (isNoble())
				{
					L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
					addSkill(striderAssaultSkill);
				}
				break;
			case 2:
				setIsFlying(true);
				break;
		}
		
		_mountType = mountType;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;
		
		return true;
	}
	
	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}
	
	/**
	 * Sets the multi sell id.
	 * @param listid the new multi sell id
	 */
	public final void setMultiSellId(final int listid)
	{
		_currentMultiSellId = listid;
	}
	
	@Override
	public void setName(String name)
	{
		super.setName(name);
		try
		{
			CharNameTable.getInstance().update(getObjectId(), getName());
		}
		catch (Exception e)
		{
			_log.warn("Error caching char name");
		}
	}
	
	public void setNewbie(int newbieRewards)
	{
		_newbie = newbieRewards;
	}
	
	public void setNoble(boolean val)
	{
		if (val)
		{
			for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills())
			{
				addSkill(s, false);
			}
		}
		else
		{
			for (L2Skill s : NobleSkillTable.getInstance().getNobleSkills())
			{
				super.removeSkill(s);
			}
		}
		_noble = val;
		sendSkillList();
		intemediateStore();
	}
	
	public void setNormalCraft(boolean val)
	{
		_characterData.set("normalCraft", val);
	}
	
	public void setObjectSittingOn(L2StaticObjectInstance id)
	{
		_objectSittingOn = id;
	}
	
	private void setObserveMode(int val)
	{
		
		_observMode = val;
		if (_observMode != 0 && _decoy == null)
		{
			_decoy = new L2Decoy(this);
			_decoy.setTitle("watch");
			_decoy.sitDown();
			_decoy.spawnMe(_obsX, _obsY, _obsZ);
		}
		else if (_decoy != null)
		{
			_decoy.deleteMe(this);
			_decoy = null;
		}
	}
	
	public void setOfflineTrade(boolean mode)
	{
		_isOfflineTrade = mode;
		if (_isOfflineTrade)
		{
			store(true);
		}
	}
	
	public void setOlyDamage(int dmg)
	{
		_olyDamage = dmg;
	}
	
	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}
	
	public void setOlympiadOpponentId(int value)
	{
		_olympiadOpponentId = value;
	}
	
	public void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}
	
	public void setOnlineStatus(boolean isOnline)
	{
		final byte value = isOnline ? ONLINE_STATE_ONLINE : ONLINE_STATE_DELETED;
		
		if (_isOnline != value)
		{
			_isOnline = value;
			
			updateOnlineStatus();
		}
	}
	
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}
	
	public void setParty(L2Party party)
	{
		_party = party;
	}
	
	public void setPartyMatchingRegion(int region)
	{
		_partyMatchingRegion = region;
	}
	
	public void setPartyMatchingShowClass(boolean par)
	{
		
	}
	
	public void setPartyRoom(L2PartyRoom room)
	{
		_partyRoom = room;
	}
	
	public void setPcCaffePoints(int val)
	{
		_pccaffe = val;
	}
	
	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}
	
	public void setPledgeRank(int rank)
	{
		_pledgeRank = rank;
	}
	
	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}
	
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
		if (_privatestore == 0)
		{
			setIsBuffShop(false);
		}
	}
	
	public void setProtection(boolean protect)
	{
		_protectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	public void setPvpFlag(int pvpFlag)
	{
		if (_pvpFlag != (byte) pvpFlag)
		{
			_pvpFlag = (byte) pvpFlag;
			broadcastFullInfo();
		}
	}
	
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}
	
	public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			_queuedSkill = null;
			return;
		}
		_queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	public void setRecomHave(int value)
	{
		if (value > 255)
		{
			_recomHave = 255;
		}
		else if (value < 0)
		{
			_recomHave = 0;
		}
		else
		{
			_recomHave = value;
		}
	}
	
	public void setRelax(boolean val)
	{
		_relax = val;
	}
	
	public void setShowBuffAnim(boolean show)
	{
		_showBuffAnimation = show;
	}
	
	public void setShowSkillChance(boolean val)
	{
		_characterData.set("skillChance", val);
	}
	
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
		broadcastRelationChanged();
	}
	
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	
	public void setSkillQueueProtectionTime(long time)
	{
		_skillQueueProtectionTime = time;
	}
	
	public void setSp(int sp)
	{
		super.getStat().setSp(sp);
	}
	
	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}
	
	public void setSummonning(boolean par)
	{
		_isSummoning = par;
	}
	
	@Override
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null)
		{
			boolean isParty = newTarget instanceof L2PcInstance && isInParty() && getParty().getPartyMembers().contains(newTarget);
			
			if (!isParty && !newTarget.isVisible())
			{
				newTarget = null;
			}
			
			if (newTarget != null && !isParty && Math.abs(newTarget.getZ() - getZ()) > 1000)
			{
				newTarget = null;
			}
		}
		
		if (!isGM())
			if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
			{
				newTarget = null;
			}
			else if (isInParty() && getParty().isInDimensionalRift())
			{
				byte riftType = getParty().getDimensionalRift().getType();
				byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
				
				if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				{
					newTarget = null;
				}
			}
		
		if (newTarget instanceof L2PcInstance)
		{
			sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null && newTarget != null)
			if (oldTarget.equals(newTarget))
				return;
			
		if (newTarget instanceof L2PcInstance)
		{
			sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		
		if (newTarget instanceof L2Character)
		{
			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		if (newTarget == null && getTarget() != null)
		{
			broadcastPacket(new TargetUnselected(this));
		}
		
		super.setTarget(newTarget);
	}
	
	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}
	
	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}
	
	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}
	
	public void setTrading(boolean trading)
	{
		_trading = trading;
	}
	
	public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public void setWantsPeace(boolean wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(), 15000);
		
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	public boolean ShowBuffAnim()
	{
		return _showBuffAnimation;
	}
	
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content != null)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}
		
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public boolean showTraders()
	{
		return _showTraders;
	}
	
	public void showUserMessages()
	{
		if (_userMessages.size() == 0)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_NO_NEW_MESSAGE));
			return;
		}
		String msg = _userMessages.remove(0);
		if (msg != null)
		{
			if (_userMessages.size() > 0)
			{
				msg = msg.replace("</body>", "<br><a action=\"bypass -h voice_readmsg\">Read more (" + _userMessages.size() + " messages)</a></body>");
			}
			NpcHtmlMessage html = new NpcHtmlMessage(5);
			html.setHtml(msg);
			sendPacket(html);
		}
	}
	
	public void sitDown()
	{
		sitDown(true);
	}
	
	public void sitDown(boolean force)
	{
		if ((isCastingNow() || isCastingSimultaneouslyNow()) && !_relax)
		{
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", Message.getMessage(this, Message.MessageId.MSG_NOT_ALLOWED_AT_THE_MOMENT));
			return;
		}
		if (_isMoving)
		{
			_sitWhenArrived = true;
			return;
		}
		if (!(_waitTypeSitting || super.isAttackingDisabled() || isOutOfControl() || isImmobilized() || !force && _protectedSitStand))
		{
			breakAttack();
			_waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			_protectedSitStand = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new ProtectSitDownStandUp(), 2333);
		}
	}
	
	public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}
	
	public void standUp()
	{
		standUp(true);
	}
	
	public void standUp(boolean force)
	{
		if (_event != null && !_event.canDoAction(this, RequestActionUse.ACTION_SIT_STAND))
		{
			sendMessage(Message.getMessage(this, Message.MessageId.MSG_FORBIDEN_BY_ADMIN));
		}
		else if (isBuffShop() || (_waitTypeSitting && !isInStoreMode() && !isAlikeDead() && (!_protectedSitStand || force)))
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2EffectType.RELAXING);
			}
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.IDLE);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			_protectedSitStand = true;
			ThreadPoolManager.getInstance().scheduleGeneral(new ProtectSitDownStandUp(), 2333);
		}
	}
	
	protected synchronized void startFeed(int npcId)
	{
		_canFeed = npcId > 0;
		if (!isMounted())
			return;
		
		if (getPet() != null)
		{
			setCurrentFeed(((L2PetInstance) getPet()).getCurrentFed());
			_controlItemId = getPet().getControlItemId();
			sendPacket(new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(), getMaxFeed() * 10000 / getFeedConsume()));
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
		else if (_canFeed)
		{
			setCurrentFeed(getMaxFeed());
			sendPacket(new SetupGauge(3, getCurrentFeed() * 10000 / getFeedConsume(), getMaxFeed() * 10000 / getFeedConsume()));
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}
	
	public void startFishing(int x, int y, int z, boolean isHotSpring)
	{
		_fishx = x;
		_fishy = y;
		_fishz = z;
		
		stopMove(null);
		setIsImmobilized(true);
		_fishing = true;
		// Starts fishing
		int group = getRandomGroup();
		
		_fish = FishTable.getFish(getRandomFishLvl(), getRandomFishType(group), group);
		if (_fish == null)
		{
			endFishing(false);
			return;
		}
		
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		
		broadcastPacket(new ExFishingStart(this, _fish.getType(), x, y, z, _lure.isNightLure()));
		sendPacket(new PlaySound(1, "SF_P_01", 0, 0, 0, 0, 0));
		startLookingForFishTask();
	}
	
	public void startLookingForFishTask()
	{
		if (!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.33));
				}
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.00));
				}
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513 || lureid == 8548)
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 0.66));
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
		{
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000);
		}
	}
	
	private int clientZ;
	
	/**
	 * Gets the client z.
	 * @return the client z
	 */
	public int getClientZ()
	{
		return clientZ;
	}
	
	public void startTrade(L2PcInstance partner)
	{
		setActiveEnchantItem(null);
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}
	
	public void startSkillRewardTime()
	{
		if (_taskSkillRewardTime == null)
		{
			_taskSkillRewardTime = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SkillRewardTime(), Config.REWARDSKILL_TIME_TASK, Config.REWARDSKILL_TIME_TASK);
		}
	}
	
	private ScheduledFuture<?> startCounterReward()
	{
		return ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CounterReward(), 1000, 1000);
	}
	
	protected void resetCounter()
	{
		if (_onCounterTask != null)
		{
			_onCounterTask.cancel(true);
			_onCounterTask = null;
			for (int[] item : Config.REWARDITEM_BYTIME_ITEM)
				addItem("", item[0], item[1], this, true);
			sendMessage("You received a gift!");
			setVar("firstrwdbytime", "1");
		}
		_OnTimer = Config.REWARDITEM_BYTIME_MIN * 60;
	}
	
	class CounterReward implements Runnable
	{
		
		protected CounterReward()
		{
		}
		
		@Override
		public void run()
		{
			if (_OnTimer < 1)
			{
				resetCounter();
				return;
			}
			
			int minutes = _OnTimer / 60;
			int second = _OnTimer % 60;
			String timing = ((minutes < 10) ? ("0" + minutes) : minutes) + ":" + ((second < 10) ? ("0" + second) : second);
			sendPacket(new ExShowScreenMessage("Next Reward : " + timing, 1100, SMPOS.TOP_RIGHT, false, 1));
			
			_OnTimer--;
		}
	}
	
	public void startWaterTask()
	{
		if (isMounted())
		{
			dismount();
		}
		
		if (!isDead() && _taskWater == null)
		{
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);
			
			sendPacket(new SetupGauge(2, timeinwater));
			broadcastUserInfo();
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}
	
	public void stopAllTimers()
	{
		getStatus().stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopSkillRewardTime();
		stopWaterTask();
		stopFeed();
		clearPetData();
		storePetFood(_mountNpcId);
		stopChargeTask();
		stopPvPFlag();
		stopJailTask();
	}
	
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}
	
	protected synchronized void stopFeed()
	{
		if (_mountFeedTask != null)
		{
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
		}
	}
	
	public void stopJailTask()
	{
		if (_jailTask != null)
		{
			_jailTask.cancel(false);
			_jailTask = null;
		}
	}
	
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			if (getMountType() == 2 && !checkCanLand())
			{
				teleToLocation(TeleportWhereType.Town);
			}
			
			if (dismount())
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
			}
		}
	}
	
	public void stopSkillId(int skillId)
	{
		L2Effect[] effects = getAllEffects();
		for (L2Effect e : effects)
			if (e != null && e.getSkill().getId() == skillId)
			{
				e.exit();
			}
	}
	
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(false);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	public void stopSkillRewardTime()
	{
		if (_taskSkillRewardTime != null)
		{
			_taskSkillRewardTime.cancel(false);
			_taskSkillRewardTime = null;
		}
	}
	
	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);
			
			_taskWater = null;
			
			sendPacket(new SetupGauge(SetupGauge.CYAN, 0));
			
			isFalling(false, 0);
			broadcastUserInfo();
		}
		
	}
	
	public synchronized void store()
	{
		store(false);
	}
	
	public synchronized void store(boolean items)
	{
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			if (_jailTask != null)
			{
				long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
				{
					delay = 0;
				}
				setJailTimer(delay);
			}
			
			storeEffect();
			
			try
			{
				storeCharBase(con);
			}
			catch (Exception e)
			{
				_log.error("L2PcInstance: Error saving character " + getName() + ", bugload possible", e);
				e.printStackTrace();
			}
			try
			{
				storeCharSub(con);
			}
			catch (Exception e)
			{
				_log.error("L2PcInstance: Error saving character " + getName() + " subclasses, not fatal", e);
			}
			try
			{
				storeRecipeBook(con);
			}
			catch (Exception e)
			{
				_log.error("L2PcInstance: Error saving character " + getName() + " recipies, not fatal", e);
			}
			try
			{
				saveSettingInDb(con);
			}
			catch (Exception e)
			{
				_log.error("L2PcInstance: Error saving character " + getName() + " settings, not fatal", e);
			}
			
			if (items)
			{
				getInventory().updateDatabase();
				if (_warehouse != null)
				{
					_warehouse.updateDatabase();
				}
				if (_freight != null)
				{
					_freight.updateDatabase();
				}
				SQLQueue.getInstance().run();
			}
			
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
	}
	
	private void storeCharBase(Connection con) throws SQLException
	{
		long totalOnlineTime = _onlineTime;
		if (_onlineBeginTime > 0)
		{
			totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
		}
		
		PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER);
		statement.setInt(1, _classIndex == 0 ? getLevel() : _baseLevel);
		statement.setInt(2, getMaxHp());
		statement.setDouble(3, getStatus().getCurrentHp());
		statement.setInt(4, getMaxCp());
		statement.setDouble(5, getStatus().getCurrentCp());
		statement.setInt(6, getMaxMp());
		statement.setDouble(7, getStatus().getCurrentMp());
		statement.setInt(8, getAppearance().getFace());
		statement.setInt(9, getAppearance().getHairStyle());
		statement.setInt(10, getAppearance().getHairColor());
		statement.setInt(11, getHeading());
		statement.setInt(12, _observerMode ? _obsX : getX());
		statement.setInt(13, _observerMode ? _obsY : getY());
		statement.setInt(14, _observerMode ? _obsZ : getZ());
		statement.setLong(15, _classIndex == 0 ? getStat().getExp() : _baseExp);
		statement.setLong(16, getExpBeforeDeath());
		statement.setInt(17, _classIndex == 0 ? getStat().getSp() : _baseSP);
		statement.setInt(18, getKarma());
		statement.setInt(19, getPvpKills());
		statement.setInt(20, getPkKills());
		statement.setInt(21, getRecomHave());
		statement.setInt(22, getRecomLeft());
		statement.setInt(23, getClanId());
		statement.setInt(24, getRace().ordinal());
		statement.setInt(25, getClassId().getId());
		statement.setLong(26, getDeleteTimer());
		statement.setString(27, _title != null ? _title : "");
		statement.setInt(28, isOnline());
		statement.setInt(29, isIn7sDungeon() ? 1 : 0);
		statement.setInt(30, getClanPrivileges());
		statement.setInt(31, wantsPeace() ? 1 : 0);
		statement.setInt(32, getBaseClass());
		statement.setLong(33, totalOnlineTime);
		statement.setInt(34, isInJail() ? 1 : 0);
		statement.setLong(35, getJailTimer());
		statement.setInt(36, getNewbie());
		statement.setInt(37, isNoble() ? 1 : 0);
		statement.setLong(38, getPledgeRank());
		statement.setInt(39, getPledgeType());
		statement.setLong(40, getLastRecomUpdate());
		statement.setInt(41, getLvlJoinedAcademy());
		statement.setLong(42, getApprentice());
		statement.setLong(43, getSponsor());
		statement.setInt(44, getAllianceWithVarkaKetra());
		statement.setLong(45, getClanJoinExpiryTime());
		statement.setLong(46, getClanCreateExpiryTime());
		statement.setString(47, getName());
		statement.setLong(48, getDeathPenaltyBuffLevel());
		statement.setLong(49, _pccaffe);
		statement.setInt(50, isBanned() ? 1 : 0);
		statement.setInt(51, isVip() ? 1 : 0);
		statement.setLong(52, getVipEndTime());
		statement.setInt(53, isAio() ? 1 : 0);
		statement.setLong(54, getAioEndTime());
		statement.setInt(55, getArenaWins());
		statement.setInt(56, getArenaDefeats());
		statement.setInt(57, hasWarehouseAccount() ? 1 : 0);
		statement.setString(58, getWarehouseAccountId());
		statement.setString(59, getWarehouseAccountPwd());
		statement.setInt(60, getPrivateBuffShopLimit());
		statement.setInt(61, getObjectId());
		statement.execute();
		statement.close();
	}
	
	private void storeCharSub(Connection con) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
		
		if (getTotalSubClasses() > 0)
		{
			for (SubClass subClass : getSubClasses().values())
			{
				statement.setLong(1, subClass.getExp());
				statement.setInt(2, subClass.getSp());
				statement.setInt(3, subClass.getLevel());
				
				statement.setInt(4, subClass.getClassId());
				statement.setInt(5, getObjectId());
				statement.setInt(6, subClass.getClassIndex());
				
				statement.execute();
			}
		}
		statement.close();
	}
	
	private synchronized void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME)
			return;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			statement.close();
			
			int buff_index = 0;
			
			statement = con.prepareStatement(ADD_SKILL_SAVE);
			
			Set<Integer> storedSkills = new HashSet<>();
			
			for (L2Effect effect : getAllEffects())
				if (effect != null && !effect.isHerbEffect() && effect.getInUse() && !effect.getSkill().isToggle())
				{
					if (effect instanceof EffectFusion)
					{
						continue;
					}
					int skillId = effect.getSkill().getId();
					if (!storedSkills.add(skillId))
					{
						continue;
					}
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					if (_reuseTimeStamps.containsKey(skillId))
					{
						TimeStamp t = _reuseTimeStamps.get(skillId);
						statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
						statement.setLong(7, t.hasNotPassed() ? t.getStamp() : 0);
					}
					else
					{
						statement.setLong(6, 0);
						statement.setLong(7, 0);
					}
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			for (TimeStamp t : _reuseTimeStamps.values())
				if (t.hasNotPassed())
				{
					int skillId = t.getSkill();
					if (!storedSkills.add(skillId))
					{
						continue;
					}
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, -1);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store char effect data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void storePetFood(int petId)
	{
		if (_controlItemId != 0 && petId != 0)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?");
				statement.setInt(1, getCurrentFeed());
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				statement.close();
				_controlItemId = 0;
			}
			catch (Exception e)
			{
				_log.fatal("Failed to store Pet [NpcId: " + petId + "] data", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
	
	private void storeRecipeBook(Connection con) throws SQLException
	{
		if (getCommonRecipeBook().length == 0 && getDwarvenRecipeBook().length == 0)
			return;
		
		PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
		statement.setInt(1, getObjectId());
		statement.execute();
		statement.close();
		
		L2RecipeList[] recipes = getCommonRecipeBook();
		
		for (L2RecipeList element : recipes)
		{
			statement = con.prepareStatement("REPLACE INTO character_recipebook (charId, id, type) values(?,?,0)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, element.getId());
			statement.execute();
			statement.close();
		}
		
		recipes = getDwarvenRecipeBook();
		for (L2RecipeList recipe : recipes)
		{
			statement = con.prepareStatement("REPLACE INTO character_recipebook (charId, id, type) values(?,?,1)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, recipe.getId());
			statement.execute();
			statement.close();
		}
	}
	
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		if (newSkill == null || newSkill.getId() > 369 && newSkill.getId() < 392)
			return;
		
		int classIndex = _classIndex;
		
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			if (oldSkill != null)
			{
				updateSkill(con, newSkill, classIndex);
			}
			else
			{
				try
				{
					addSkill(con, newSkill, classIndex);
				}
				catch (SQLException e)
				{
					if (e.getClass().getSimpleName().equals("MySQLIntegrityConstraintViolationException"))
					{
						updateSkill(con, newSkill, classIndex);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error could not store char skills: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void teleportAnswer(int answer, int requesterId)
	{
		if (_summonRequestTarget == null)
			return;
		if (answer == 1 && _summonRequestTarget.getObjectId() == requesterId)
		{
			SummonFriend.teleToTarget(this, _summonRequestTarget, _summonRequestSkill);
		}
		_summonRequestTarget = null;
		_summonRequestSkill = null;
	}
	
	public boolean teleportRequest(L2PcInstance requester, L2Skill skill)
	{
		if (_summonRequestTarget != null && requester != null)
			return false;
		_summonRequestTarget = requester;
		_summonRequestSkill = skill;
		return true;
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (isPreventedFromReceivingBuffs())
		{
			setPreventedFromReceivingBuffs(false);
			sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", "Block buff is off");
		}
		super.teleToLocation(x, y, z, allowRandomOffset);
		intemediateStore();
	}
	
	public void tempInvetoryDisable()
	{
		_inventoryDisable = true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}
	
	@Override
	public String toString()
	{
		return "player " + getName();
	}
	
	public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
			return null;
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			sendPacket(playerIU);
		}
		
		StatusUpdate playerSU = new StatusUpdate(this);
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			}
			
			playerSU = new StatusUpdate(targetPlayer);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		
		return newItem;
	}
	
	public void tryOpenPrivateBuyStore()
	{
		if (canOpenPrivateStore())
		{
			revalidateZone(true);
			L2TradeZone z = (L2TradeZone) getZone("Trade");
			if (z != null && !z.canBuy())
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY || getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY + 1)
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (Config.CHECK_ZONE_ON_PVT && !isInsideZone(L2Zone.FLAG_TRADE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
				sendPacket(new PrivateStoreManageListBuy(this));
			}
			
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		if (canOpenPrivateStore())
		{
			revalidateZone(true);
			L2TradeZone z = (L2TradeZone) getZone("Trade");
			if (z != null && !z.canSell())
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL + 1 || getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (Config.CHECK_ZONE_ON_PVT && !isInsideZone(L2Zone.FLAG_TRADE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL + 1);
				sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
		{
			_dwarvenRecipeBook.remove(recipeId);
		}
		else if (_commonRecipeBook.containsKey(recipeId))
		{
			_commonRecipeBook.remove(recipeId);
		}
		else
		{
			_log.warn("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	@Override
	public void updateEffectIconsImpl()
	{
		final EffectInfoPacketList list = new EffectInfoPacketList(this);
		
		sendPacket(new MagicEffectIcons(list));
		
		if (isInParty())
		{
			getParty().broadcastToPartyMembers(this, new PartySpelled(list));
		}
		
		if (isInOlympiadMode() && isOlympiadStart())
		{
			final List<L2PcInstance> spectators = Olympiad.getSpectators(getOlympiadGameId());
			
			if (spectators != null && !spectators.isEmpty())
			{
				final ExOlympiadSpelledInfo os = new ExOlympiadSpelledInfo(list);
				
				for (L2PcInstance spectator : spectators)
				{
					spectator.sendPacket(os);
				}
			}
		}
	}
	
	private void updateJailState()
	{
		if (isInJail())
		{
			if (_jailTimer > 0)
			{
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
				sendChatMessage(0, SystemChatChannelId.Chat_None, "SYS", String.format(Message.getMessage(this, Message.MessageId.MSG_YOU_JAIL_StATUS_UPDATED), Math.round(_jailTimer / 60000)));
			}
			
			if (!isInsideZone(L2Zone.FLAG_JAIL))
			{
				teleToLocation(-114356, -249645, -2984, false);
			}
		}
	}
	
	public void updateNameTitleColor()
	{
		broadcastUserInfo();
	}
	
	public void updateOnlineStatus()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
			statement.setInt(1, isOnline());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed updating character online status.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	@Override
	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
			return;
		
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}
	
	public void updatePvPStatus()
	{
		if (_event != null && _event.isRunning())
			return;
		
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;
		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
		{
			startPvPFlag();
		}
	}
	
	public void updatePvPStatus(L2Character target)
	{
		L2PcInstance player_target = target.getActingPlayer();
		
		if (player_target == null)
			return;
		if (_event != null && _event.isRunning())
			return;
		
		if (isInDuel() && player_target.getDuelId() == getDuelId())
			return;
		if ((!isInsideZone(L2Zone.FLAG_PVP) || !player_target.isInsideZone(L2Zone.FLAG_PVP)) && player_target.getKarma() == 0)
		{
			if (checkIfPvP(player_target))
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			}
			else
			{
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			}
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
			}
		}
	}
	
	private void updateSkill(Connection con, L2Skill newSkill, int classIndex) throws SQLException
	{
		PreparedStatement statement;
		statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
		statement.setInt(1, newSkill.getLevel());
		statement.setInt(2, newSkill.getId());
		statement.setInt(3, getObjectId());
		statement.setInt(4, classIndex);
		try
		{
			statement.execute();
		}
		finally
		{
			statement.close();
		}
	}
	
	public synchronized void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		L2ItemInstance[] items = null;
		boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		L2ItemInstance old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		
		if (old == null)
		{
			old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		}
		
		checkSSMatch(item, old);
		int bodyPart = item.getItem().getBodyPart();
		
		if (isEquiped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));
			}
			
			if (bodyPart == L2Item.SLOT_L_EAR || bodyPart == L2Item.SLOT_LR_EAR || bodyPart == L2Item.SLOT_L_FINGER || bodyPart == L2Item.SLOT_LR_FINGER)
			{
				getInventory().setPaperdollItem(item.getLocationSlot(), null);
				sendPacket(new ItemList(this, false));
			}
			
			items = getInventory().unEquipItemInBodySlotAndRecord(bodyPart);
		}
		else
		{
			L2ItemInstance tempItem = getInventory().getPaperdollItemByL2ItemId(bodyPart);
			
			if (tempItem != null && tempItem.isWear())
				return;
			else if (bodyPart == 0x4000)
			{
				tempItem = getInventory().getPaperdollItem(7);
				if (tempItem != null && tempItem.isWear())
					return;
				
				tempItem = getInventory().getPaperdollItem(8);
				if (tempItem != null && tempItem.isWear())
					return;
			}
			else if (bodyPart == 0x8000)
			{
				tempItem = getInventory().getPaperdollItem(10);
				if (tempItem != null && tempItem.isWear())
					return;
				
				tempItem = getInventory().getPaperdollItem(11);
				if (tempItem != null && tempItem.isWear())
					return;
			}
			
			if (item.getEnchantLevel() > 0)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item));
			}
			
			if ((bodyPart & L2Item.SLOT_HEAD) > 0 || (bodyPart & L2Item.SLOT_NECK) > 0 || (bodyPart & L2Item.SLOT_L_EAR) > 0 || (bodyPart & L2Item.SLOT_R_EAR) > 0 || (bodyPart & L2Item.SLOT_L_FINGER) > 0 || (bodyPart & L2Item.SLOT_R_FINGER) > 0)
			{
				sendPacket(new UserInfo(this));
			}
			
			items = getInventory().equipItemAndRecord(item);
			if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
			{
				item.setChargedSoulshot(L2ItemInstance.CHARGED_NONE, true);
				item.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			item.decreaseMana(false);
		}
		
		refreshExpertisePenalty();
		InventoryUpdate iu = new InventoryUpdate();
		iu.addEquipItems(items);
		sendPacket(iu);
		
		if (abortAttack)
		{
			abortAttack();
		}
		
		if (getInventoryLimit() != oldInvLimit)
		{
			sendPacket(new ExStorageMaxCount(this));
		}
		broadcastUserInfo();
	}
	
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null)
			return;
		
		if (skill.isPassive() || skill.isChance() || skill.bestowed())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (isCastingNow())
		{
			SkillDat currentSkill = getCurrentSkill();
			if (currentSkill != null && skill.getId() == currentSkill.getSkillId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		setIsCastingNow(true);
		setCurrentSkill(skill, forceUse, dontMove);
		
		if (getQueuedSkill() != null)
		{
			setQueuedSkill(null, false, false);
		}
		
		if (!checkUseMagicConditions(skill, forceUse, dontMove))
		{
			setIsCastingNow(false);
			return;
		}
		
		L2Character target = null;
		switch (skill.getTargetType())
		{
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}
		getAI().setIntention(CtrlIntention.CAST, skill, target);
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public synchronized boolean validateBypass(String cmd)
	{
		for (String bp : _validBypass)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (bp.equals(cmd))
				return true;
		}
		
		for (String bp : _validBypass2)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (cmd.startsWith(bp))
				return true;
		}
		
		return false;
	}
	
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
			return false;
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return false;
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
			return false;
		
		return !item.isWear();
	}
	
	public boolean wantsPeace()
	{
		return _wantsPeace;
	}
	
	private boolean _secondRefusal = false;
	
	public void setSecondRefusal(boolean mode)
	{
		_secondRefusal = mode;
	}
	
	public boolean getSecondRefusal()
	{
		return _secondRefusal;
	}
	
	public void setWarehouseAccountId(String id)
	{
		warehouseAccountId = id;
	}
	
	public String getWarehouseAccountId()
	{
		return warehouseAccountId;
	}
	
	public void setWarehouseAccountPwd(String pwd)
	{
		warehouseAccountPwd = pwd;
	}
	
	public String getWarehouseAccountPwd()
	{
		return warehouseAccountPwd;
	}
	
	public void setHasWarehouseAccount(boolean i)
	{
		hasWarehouseAccount = i;
	}
	
	public boolean hasWarehouseAccount()
	{
		return hasWarehouseAccount;
	}
	
	public boolean isVip()
	{
		return _isVip;
	}
	
	public void setVip(boolean val)
	{
		_isVip = val;
		
	}
	
	public void setVipEndTime(long val)
	{
		_vip_endTime = val;
	}
	
	public long getVipEndTime()
	{
		return _vip_endTime;
	}
	
	public boolean isAio()
	{
		return _isAio;
	}
	
	public void setAio(boolean val)
	{
		_isAio = val;
		
	}
	
	public void rewardAioSkills()
	{
		L2Skill skill;
		for (Integer skillid : Config.AIO_SKILLS.keySet())
		{
			int skilllvl = Config.AIO_SKILLS.get(skillid);
			skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			if (skill != null)
			{
				addSkill(skill, true);
			}
		}
	}
	
	public void lostAioSkills()
	{
		L2Skill skill;
		for (Integer skillid : Config.AIO_SKILLS.keySet())
		{
			int skilllvl = Config.AIO_SKILLS.get(skillid);
			skill = SkillTable.getInstance().getInfo(skillid, skilllvl);
			removeSkill(skill);
		}
	}
	
	public void setAioEndTime(long val)
	{
		_aio_endTime = val;
	}
	
	public void setEndTime(String process, int val)
	{
		if (val > 0)
		{
			long end_day;
			Calendar calendar = Calendar.getInstance();
			if (val >= 30)
			{
				while (val >= 30)
				{
					if (calendar.get(Calendar.MONTH) == 11)
					{
						calendar.roll(Calendar.YEAR, true);
					}
					calendar.roll(Calendar.MONTH, true);
					val -= 30;
				}
			}
			if ((val < 30) && (val > 0))
			{
				while (val > 0)
				{
					if ((calendar.get(Calendar.DATE) == 28) && (calendar.get(Calendar.MONTH) == 1))
					{
						calendar.roll(Calendar.MONTH, true);
					}
					if (calendar.get(Calendar.DATE) == 30)
					{
						if (calendar.get(Calendar.MONTH) == 11)
						{
							calendar.roll(Calendar.YEAR, true);
						}
						calendar.roll(Calendar.MONTH, true);
						
					}
					calendar.roll(Calendar.DATE, true);
					val--;
				}
			}
			
			end_day = calendar.getTimeInMillis();
			if (process.equals("aio"))
			{
				_aio_endTime = end_day;
			}
			else if (process.equals("vip"))
			{
				_vip_endTime = end_day;
			}
			else
			{
				_log.warn("process " + process + "no Known while try set end date");
				return;
			}
			Date dt = new Date(end_day);
			_log.info("" + process + " End time for player " + getName() + " is " + dt);
		}
		else
		{
			if (process.equals("aio"))
			{
				_aio_endTime = 0;
			}
			else if (process.equals("vip"))
			{
				_vip_endTime = 0;
			}
			else
			{
				System.out.println("process " + process + "no Known while try set end date");
				return;
			}
		}
	}
	
	public long getAioEndTime()
	{
		return _aio_endTime;
	}
	
	@Override
	public boolean isInArenaEvent()
	{
		return _inArenaEvent;
	}
	
	@Override
	public void setInArenaEvent(boolean val)
	{
		_inArenaEvent = val;
	}
	
	public void increaseArenaDefeats()
	{
		_arenaDefeats++;
	}
	
	public void increaseArenaWins()
	{
		_arenaWins++;
	}
	
	private void setArenaDefeats(int val)
	{
		_arenaDefeats = val;
	}
	
	private void setArenaWins(int val)
	{
		_arenaWins = val;
	}
	
	public int getArenaWins()
	{
		return _arenaWins;
	}
	
	public int getArenaDefeats()
	{
		return _arenaDefeats;
	}
	
	public long getOfflineStartTime()
	{
		return _offlineShopStart;
	}
	
	public void setOfflineStartTime(long time)
	{
		_offlineShopStart = time;
	}
	
	public void tryOpenPrivateBuffStore()
	{
		if (canOpenPrivateStore())
		{
			if ((getPrivateStoreType() == 1) || (getPrivateStoreType() == 2) || (getPrivateStoreType() == 8))
			{
				setPrivateStoreType(0);
			}
			if (getPrivateStoreType() == 0)
			{
				if (isSitting())
				{
					standUp();
				}
				setPrivateStoreType(2);
				setIsBuffShop(true);
				
				if (Config.OFFLINE_BUFF_SHOP)
				{
					doOfflineBuff();
				}
				sendPacket(new PrivateStoreManageListBuff(this));
			}
		}
		else
		{
			if (!isInsideZone(L2Zone.FLAG_PEACE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private static L2ItemInstance createBuffShopItem(int itemId)
	{
		item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		if (item != null)
		{
			L2World.getInstance().storeObject(item);
			return item;
		}
		return null;
	}
	
	public List<L2ItemInstance> getSkillsForBuffShop()
	{
		if (_skillsForBuffShop == null)
		{
			_skillsForBuffShop = new ArrayList<>();
			for (L2Skill skill : getAllSkills())
			{
				String key = new StringBuilder().append(skill.getId()).append("-").append(skill.getLevel()).toString();
				if (Config.isValidBuffShopSkill(key))
				{
					int itemId = Config.getBuffShopItem(key);
					if (itemId < 0)
					{
						continue;
					}
					L2ItemInstance item = createBuffShopItem(itemId);
					if (item == null)
					{
						continue;
					}
					_skillsForBuffShop.add(item);
				}
			}
		}
		return _skillsForBuffShop;
	}
	
	public List<L2ItemInstance> getAvailableSkillsForBuffShop()
	{
		List<L2ItemInstance> skills = new ArrayList<>();
		Map<Integer, int[]> sellList = getBuffShopSellList();
		for (L2ItemInstance skill : getSkillsForBuffShop())
		{
			if (!sellList.containsKey(Integer.valueOf(skill.getItemId())))
			{
				skills.add(skill);
			}
		}
		return skills;
	}
	
	public Map<Integer, int[]> getBuffShopSellList()
	{
		return _buffShopSellList;
	}
	
	public void clearBuffShopSellList()
	{
		if (_buffShopSellList != null)
		{
			_buffShopSellList.clear();
		}
	}
	
	public void addItemToBuffShopSellList(int id, int count)
	{
		L2ItemInstance item = createBuffShopItem(id);
		if (item == null)
		{
			_log.info(new StringBuilder().append("[").append(getName()).append("] Failed to add item ").append(id).append(" to buff shop sell list.").toString());
			return;
		}
		
		_buffShopSellList.put(Integer.valueOf(id), new int[]
		{
			item.getObjectId(),
			count
		});
	}
	
	public void addObjectToBuffShopSellList(int id, int count)
	{
		L2Object object = L2World.getInstance().findObject(id);
		if ((object == null) || (!(object instanceof L2ItemInstance)))
		{
			_log.info(new StringBuilder().append("[").append(getName()).append("] Failed to add object ").append(id).append(" to buff shop sell list.").toString());
			return;
		}
		L2ItemInstance item = (L2ItemInstance) object;
		
		_buffShopSellList.put(Integer.valueOf(item.getItemId()), new int[]
		{
			id,
			count
		});
	}
	
	public void setIsBuffShop(boolean is)
	{
		_isBuffShop = is;
		try
		{
			if (_isBuffShop)
			{
				if ((getClient() != null) && (!getClient().isDetached()))
				{
					sendPacket(new ExStorageMaxCount(this));
				}
				BuffShopManager.updateShopTitle(this);
			}
			else
			{
				if ((getClient() != null) && (!getClient().isDetached()))
				{
					sendPacket(new ExStorageMaxCount(this));
				}
				BuffShopManager.restoreEffects(this);
			}
		}
		catch (NullPointerException e)
		{
			_log.info(new StringBuilder().append("L2PcInstance#setIsBuffShop: NPE: ").append(e.getMessage()).toString());
		}
	}
	
	public boolean isBuffShop()
	{
		return _isBuffShop;
	}
	
	public void setIsBizy(boolean is)
	{
		_isBizy = is;
	}
	
	public boolean isBizy()
	{
		return _isBizy;
	}
	
	public int getPrivateBuffShopLimit()
	{
		return _privateBuffShopLimit;
	}
	
	public void setPrivateBuffShopLimit(int limit)
	{
		_privateBuffShopLimit = limit;
	}
	
	private List<Integer> _completedAchievements = new FastList<>();
	
	public boolean readyAchievementsList()
	{
		if (_completedAchievements.isEmpty())
			return false;
		return true;
	}
	
	public void saveAchievemntData()
	{
		
	}
	
	public void getAchievemntData()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			PreparedStatement insertStatement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT * from achievements WHERE owner_id=" + getObjectId());
			
			rs = statement.executeQuery();
			
			String values = "owner_id";
			String in = Integer.toString(getObjectId());
			String questionMarks = in;
			int ilosc = AchievementsManager.getInstance().getAchievementList().size();
			
			if (rs.next())
			{
				_completedAchievements.clear();
				for (int i = 1; i <= ilosc; i++)
				{
					int a = rs.getInt("a" + i);
					
					if (!_completedAchievements.contains(i))
						if (a == 1)
							_completedAchievements.add(i);
				}
			}
			else
			{
				// Player hasnt entry in database, means we have to create it.
				
				for (int i = 1; i <= ilosc; i++)
				{
					values += ", a" + i;
					questionMarks += ", 0";
				}
				
				String s = "INSERT INTO achievements(" + values + ") VALUES (" + questionMarks + ")";
				insertStatement = con.prepareStatement(s);
				
				insertStatement.execute();
				insertStatement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warn("[ACHIEVEMENTS ENGINE GETDATA]" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public void saveAchievementData(int achievementID)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			statement.executeUpdate("UPDATE achievements SET a" + achievementID + "=1 WHERE owner_id=" + getObjectId());
			statement.close();
			
			if (!_completedAchievements.contains(achievementID))
				_completedAchievements.add(achievementID);
		}
		catch (SQLException e)
		{
			_log.warn("[ACHIEVEMENTS SAVE GETDATA]" + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public List<Integer> getCompletedAchievements()
	{
		return _completedAchievements;
	}
	
	public void addItem(String process, int itemId, long countL, L2Object reference, boolean sendMessage)
	{
		int count = 0;
		count = (int) countL;
		if (count != countL)
		{
			count = 1;
		}
		
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && (!isCastingNow() && ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB || ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
				
				sendPacket(new ItemList(this, false));
				
				// Update current load as well
				StatusUpdate su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				su = null;
				
				// If over capacity, drop the item
				if (!isGM() && !_inventory.validateCapacity(item))
				{
					dropItem("InvDrop", item, null, true, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
				
				item = null;
			}
		}
	}
	
	public int getItemCount(final int itemId, final int enchantLevel)
	{
		return _inventory.getInventoryItemCount(itemId, enchantLevel);
	}
	
	private int heroConsecutiveKillCount = 0;
	@SuppressWarnings("unused")
	private boolean isPermaHero = false;
	private boolean isPVPHero = false;
	
	public void reloadPVPHeroAura()
	{
		sendPacket(new UserInfo(this));
	}
	
	public void setHeroAura(boolean heroAura)
	{
		isPVPHero = heroAura;
		return;
	}
	
	public boolean getIsPVPHero()
	{
		return isPVPHero;
	}
	
	public void queryGameGuard()
	{
		getClient().setGameGuardOk(false);
		sendPacket(GameGuardQuery.STATIC_PACKET);
		if (Config.GAMEGUARD_ENFORCE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
		}
	}
	
	class GameGuardCheck implements Runnable
	{
		@Override
		public void run()
		{
			L2GameClient client = getClient();
			if ((client != null) && !client.isAuthedGG() && (isOnline() == 1))
			{
				_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
				new Disconnection(client, L2PcInstance.this).defaultSequence(false);
			}
		}
	}
	
	public void closeNetConnection()
	{
		if (_client != null)
		{
			_client.close(new LeaveWorld());
			setClient(null);
		}
	}
	
	public void setAllowed(int allowed)
	{
		_allowed = allowed;
	}
	
	public int getAllowed()
	{
		return _allowed;
	}
	
	private boolean _isAutoFarm;
	
	public void setAutoFarm(boolean val)
	{
		_isAutoFarm = val;
		
		if(!val)
		{
			L2ShortCut[] shortcuts = getAllShortCuts();

			for (L2ShortCut sc : shortcuts)
			{
				if (sc.getPage() != 0)
					continue;
				
				int slot = sc.getSlot();
				int type = sc.getType();
				
				if (slot >= 0 && slot <= 12 && type == L2ShortCut.TYPE_SKILL)
				{
					L2Skill skill = getKnownSkill(sc.getId());
					
					sendPacket(new ExAutoSoulShot(skill.getId(), 0));
				}
				
				if (sc.getType() == L2ShortCut.TYPE_ACTION && sc.getId() == 2)
				{
					sendPacket(new ExAutoSoulShot(2, 0));
				}
			}
		}
	}
	
	public boolean isAutoFarm()
	{
		return _isAutoFarm;
	}
	
	private final Map<Integer, Integer> _scriptValues = new ConcurrentHashMap<>();
	
	public int getScriptValue(int key)
	{
		return _scriptValues.getOrDefault(key, 0);
	}
	
	public void setScriptValue(int key, int value)
	{
		_scriptValues.put(key, value);
	}
	
	public void clearScriptValues()
	{
		_scriptValues.clear();
	}
	
}