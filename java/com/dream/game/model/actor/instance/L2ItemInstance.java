package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.manager.ItemsOnGroundManager;
import com.dream.game.manager.MercTicketManager;
import com.dream.game.model.DropProtection;
import com.dream.game.model.L2Augmentation;
import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.quest.QuestState;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.DeleteObject;
import com.dream.game.network.serverpackets.GetItem;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.funcs.Func;
import com.dream.game.skills.funcs.FuncOwner;
import com.dream.game.taskmanager.SQLQueue;
import com.dream.game.templates.item.AbstractL2ItemType;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2Equip;
import com.dream.game.templates.item.L2EtcItem;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.sql.SQLQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public final class L2ItemInstance extends L2Object implements FuncOwner
{
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT,
		NPC
	}

	public class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance _shadowItem;

		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}

		@Override
		public void run()
		{
			try
			{
				if (_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	private static enum UpdateMode
	{
		INSERT,
		UPDATE,
		REMOVE,
		NONE
	}

	protected static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());

	private static final Logger _logItems = Logger.getLogger("item");
	private static final int MANA_CONSUMPTION_RATE = 60000;
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SOULSHOT = 2;
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int MODIFIED = 2;
	public static final int REMOVED = 3;

	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;

		int objectId, item_id, count, loc_data, enchant_level, custom_type1, custom_type2, manaLeft;
		String data;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt("object_id");
			item_id = rs.getInt("item_id");
			count = rs.getInt("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			loc_data = rs.getInt("loc_data");
			enchant_level = rs.getInt("enchant_level");
			custom_type1 = rs.getInt("custom_type1");
			custom_type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			data = rs.getString("data");
		}
		catch (Exception e)
		{
			_log.fatal("Could not restore an item owned by " + ownerId + " from DB:" + e.getMessage(), e);
			return null;
		}
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if (item == null)
		{
			_log.fatal("Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant_level;
		inst._type1 = custom_type1;
		inst._type2 = custom_type2;
		inst._loc = loc;
		inst._locData = loc_data;
		inst._existsInDb = true;
		inst._storedInDb = true;
		inst._itemData = data;

		inst._mana = manaLeft;

		if (inst._mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
		{
			inst.decreaseMana(false);
		}

		if (inst._mana == 0)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				inst.removeFromDb(con);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			return null;
		}
		else if (inst._mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
		{
			inst.scheduleConsumeManaTask();
		}

		if (inst.isEquipable())
		{
			inst.restoreAttributes();
		}

		return inst;
	}

	private ScheduledFuture<?> itemLootShedule = null;
	private int _chargedSoulshot = CHARGED_NONE;
	private int _chargedSpiritshot = CHARGED_NONE;
	private boolean _chargedFishtshot = false;
	private boolean _consumingMana = false;
	private boolean _decrease = false;
	private L2Augmentation _augmentation = null;
	private int _mana = -1;
	private int _lastChange = 2;
	private int _ownerId;
	private int _count;
	private int _initCount;
	private int _time;
	private int _type1;
	private int _type2;
	private long _dropTime;
	private int _locData;
	private int _enchantLevel;
	private int _priceSell;
	private int _priceBuy;
	private final int _itemId;
	private final int _itemDisplayId;
	private final L2Item _item;
	private ItemLocation _loc;
	private boolean _wear;

	private boolean _protected;

	private boolean _existsInDb;
	private boolean _storedInDb;
	private final DropProtection _dropProtection = new DropProtection();
	public int engraver;
	private int _engraverId;

	private int _rewardId;
	private long _rewardTime;
	private String _itemData = "";

	private String _process = "";

	private int _creator;

	private long _creationTime;

	private final SQLQuery _removeAqugmentationQueue = new SQLQuery()
	{
		
		@Override
		public void execute(Connection con)
		{
			try
			{
				PreparedStatement statement = null;
				statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");

				statement.setInt(1, getObjectId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{

			}
		}

	};


	private final SQLQuery UPDATE_DATABASE_QUERY = new SQLQuery()
	{
		@Override
		public void execute(Connection con)
		{
			switch (getUpdateMode(true))
			{
				case INSERT:
					insertIntoDb(con);
					break;
				case UPDATE:
					updateInDb(con);
					break;
				case REMOVE:
					removeFromDb(con);
					break;
			}
		}
	};

	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		getKnownList();
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		super.setName(_item.getName());
		_itemDisplayId = _item.getItemDisplayId();
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
	}

	public L2ItemInstance(int objectId, int itemId, int itemDisplayId)
	{
		super(objectId);
		getKnownList();
		_itemId = itemId;
		_itemDisplayId = itemDisplayId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
	}

	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		getKnownList();
		_itemId = item.getItemId();
		_itemDisplayId = item.getItemDisplayId();
		_item = item;
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
	}

	public void bind()
	{
		if (isBounded())
			return;
		L2PcInstance pc = L2World.getInstance().getPlayer(getOwnerId());
		if (pc != null)
		{
			if (!_itemData.isEmpty())
			{
				_itemData += ";";
			}
			_itemData += "b";
		}
	}

	public boolean canPickup(L2PcInstance player)
	{
		if (_ownerId != 0)
			return false;
		if (_rewardId != 0 && _rewardId != player.getObjectId())
		{
			if (player.getParty() != null)
			{
				for (L2PcInstance pc : player.getParty().getPartyMembers())
					if (_rewardId == pc.getObjectId())
					{
						_rewardId = 0;
						return true;
					}
			}
			if (System.currentTimeMillis() - _rewardTime < 15000)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return false;
			}
		}
		_rewardId = 0;
		return true;

	}

	public void changeCount(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if (count == 0)
			return;

		if (count > 0 && getCount() > Integer.MAX_VALUE - count)
		{
			setCount(Integer.MAX_VALUE);
		}
		else
		{
			setCount(getCount() + count);
		}

		if (getCount() < 0)
		{
			setCount(0);
		}

		_storedInDb = false;

		if (Config.LOG_ITEMS && process != null && !Config.IGNORE_LOG.contains(process.toUpperCase()))
		{
			List<Object> param = new ArrayList<>();
			param.add("CHANGE:" + process);
			param.add(this);
			param.add(creator);
			param.add(reference);
			_logItems.info(param);
		}
	}

	public void changeCountWithoutTrace(int count, L2PcInstance creator, L2Object reference)
	{
		changeCount(null, count, creator, reference);
	}

	public void clearEngravement()
	{
		if (_engraverId == _ownerId)
		{
			_engraverId = 0;
		}
	}

	public void decreaseMana(boolean resetConsumingMana)
	{
		if (!isShadowItem())
			return;

		if (_mana > 0)
		{
			_mana--;
		}

		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}

		L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
		if (player != null && !player.isDead())
		{
			switch (_mana)
			{
				case 10:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addString(getItemName()));
					break;
				case 5:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addString(getItemName()));
					break;
				case 1:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addString(getItemName()));
					break;
			}

			if (_mana == 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addString(getItemName()));

				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance element : unequiped)
					{
						player.checkSSMatch(null, element);
						iu.addModifiedItem(element);
					}
					player.sendPacket(iu);
					player.sendPacket(new ItemList(player, true));
					player.broadcastUserInfo();
				}

				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);

					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);

					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}

				L2World.getInstance().removeObject(this);
			}
			else
			{
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}

	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		if (Config.ASSERT)
		{
			assert getPosition().getWorldRegion() == null;
		}

		if (Config.GEODATA && dropper != null)
		{
			Location dropDest = GeoData.getInstance().moveCheck(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z);
			x = dropDest.getX();
			y = dropDest.getY();
			z = dropDest.getZ();
		}

		synchronized (this)
		{
			getPosition().setXYZ(x, y, z);
		}

		getPosition().getWorldRegion().addVisibleObject(this);
		setOwnerId(0);
		setDropTime(System.currentTimeMillis());

		L2World.getInstance().addVisibleObject(this, dropper);
		updateDatabase();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}
	}

	public void engrave()
	{
		_engraverId = _ownerId;
	}

	public L2Armor getArmorItem()
	{
		if (_item instanceof L2Armor)
			return (L2Armor) _item;

		return null;
	}

	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	public int getCount()
	{
		return _count;
	}

	public boolean getCountDecrease()
	{
		return _decrease;
	}

	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public String getData()
	{
		return _itemData;
	}

	public final DropProtection getDropProtection()
	{
		return _dropProtection;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public int getEngraver()
	{
		return _engraverId;
	}

	public int getEquipSlot()
	{
		return _locData;
	}

	public L2EtcItem getEtcItem()
	{
		if (_item instanceof L2EtcItem)
			return (L2EtcItem) _item;

		return null;
	}

	@Override
	public final String getFuncOwnerName()
	{
		return getItem().getFuncOwnerName();
	}

	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return getItem().getFuncOwnerSkill();
	}

	public int getInitCount()
	{
		return _initCount;
	}

	public L2Item getItem()
	{
		return _item;
	}

	public int getItemDisplayId()
	{
		return _itemDisplayId;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}

	public String getItemName()
	{
		return _item.getName();
	}

	public AbstractL2ItemType getItemType()
	{
		return _item.getItemType();
	}

	public int getLastChange()
	{
		return _lastChange;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	public int getLocationSlot()
	{
		if (Config.ASSERT)
		{
			assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT || _loc == ItemLocation.INVENTORY;
		}

		return _locData;
	}

	public int getMana()
	{
		return _mana;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public int getPDef()
	{
		if (_item instanceof L2Armor)
			return ((L2Armor) _item).getPDef();
		return 0;
	}

	public int getPriceToBuy()
	{
		return isConsumable() ? (int) (_priceBuy * Config.RATE_CONSUMABLE_COST) : _priceBuy;
	}

	public int getPriceToSell()
	{
		return isConsumable() ? (int) (_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell;
	}

	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	public Func[] getStatFuncs(L2Character player)
	{
		if (getItem() instanceof L2Equip)
			return getItem().getStatFuncs(this, player);
		return L2Equip.EMPTY_FUNC_SET;
	}

	public int getTime()
	{
		return _time;
	}

	public UpdateMode getUpdateMode(boolean force)
	{
		if (_wear)
			return UpdateMode.NONE;

		boolean shouldBeInDb = true;
		shouldBeInDb &= _ownerId != 0;
		shouldBeInDb &= _loc != ItemLocation.VOID;
		shouldBeInDb &= _count != 0 || _loc == ItemLocation.LEASE;

		if (_existsInDb)
		{
			if (!shouldBeInDb)
				return UpdateMode.REMOVE;
			else if (!Config.LAZY_ITEMS_UPDATE || force)
				return UpdateMode.UPDATE;
		}
		else if (shouldBeInDb && _loc != ItemLocation.NPC)
			return UpdateMode.INSERT;

		return UpdateMode.NONE;
	}

	public L2Weapon getWeaponItem()
	{
		if (_item instanceof L2Weapon)
			return (L2Weapon) _item;

		return null;
	}

	
	public void insertIntoDb(Connection con)
	{
		try
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,process,creator_id,first_owner_id,creation_time,data) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, getMana());
			statement.setString(11, _process);
			statement.setInt(12, _creator);
			statement.setInt(13, _ownerId);
			statement.setLong(14, _creationTime);
			statement.setString(15, _itemData);

			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not insert item " + this);
			if (_ownerId != 0)
			{
				L2Character owner = L2World.getInstance().findCharacter(_ownerId);
				if (owner != null)
				{
					owner.getInventory().destroyItem("fail", this, null, null);
				}
			}
		}
	}

	public boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	public boolean isAugmented()
	{
		return _augmentation != null;
	}

	public boolean isAvailable(L2PcInstance player, boolean allowAdena)
	{
		return !isEquipped() && getItem().getType2() != 3 && (getItem().getType2() != 4 || getItem().getType1() != 1) && (player.getPet() == null || getObjectId() != player.getPet().getControlItemId()) && player.getActiveEnchantItem() != this && (allowAdena || getItemId() != 57) && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && (!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null || player.getLastSimultaneousSkillCast().getItemConsumeId() != getItemId()) && isTradeable();
	}

	public boolean isBounded()
	{
		if (_itemData != null)
			return _itemData.contains("b");
		return false;
	}

	public boolean isBracelet()
	{
		return _itemId >= 9589 && _itemId <= 9592 || _itemId >= 10209 && _itemId <= 10210;
	}

	public boolean isConsumable()
	{
		return _item.isConsumable();
	}

	public boolean isCrystalScroll()
	{
		return _itemId >= 957 && _itemId <= 958 || _itemId >= 953 && _itemId <= 954 || _itemId >= 949 && _itemId <= 950 || _itemId >= 731 && _itemId <= 732 || _itemId >= 961 && _itemId <= 962;
	}

	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}

	public boolean isDropable()
	{
		return !isAugmented() && _item.isDropable();
	}

	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem);
	}

	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	public boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	public boolean isOlyRestrictedItem()
	{
		if (Config.ALT_OLY_ENCHANT_LIMIT > 0)
			if (getEnchantLevel() > Config.ALT_OLY_ENCHANT_LIMIT)
				return true;
		return Config.LIST_OLY_RESTRICTED_ITEMS.contains(_itemId);
	}

	public boolean isProtected()
	{
		return _protected;
	}

	public boolean isSellable()
	{
		return !isAugmented() && _item.isSellable();
	}

	public boolean isShadowItem()
	{
		return _mana >= 0;
	}

	public boolean isStackable()
	{
		return _item.isStackable();
	}

	public boolean isTradeable()
	{
		return !isAugmented() && _item.isTradeable() && !isBounded();
	}

	public boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	public boolean isWear()
	{
		return _wear;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		int _castleId = MercTicketManager.getTicketCastleId(_itemId);

		if (_castleId > 0)
		{
			boolean privMatch = (player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES;
			boolean castleMatch = player.getClan() != null && player.getClan().getHasCastle() == _castleId;
			if (privMatch && castleMatch)
			{
				if (player.isInParty())
				{
					player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_YOU_CANT_PICKUP_MERCH_IN_PARTY));
					player.getAI().setIntention(CtrlIntention.IDLE);
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.PICK_UP, this);
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				player.getAI().setIntention(CtrlIntention.IDLE);
			}
			player.setTarget(this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (getOwnerId() == 0)
		{
			player.getAI().setIntention(CtrlIntention.PICK_UP, this);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public final void pickupMe(L2Character player)
	{
		_rewardId = 0;
		L2WorldRegion oldregion = getPosition().getWorldRegion();
		resetOwnerTimer();
		player.broadcastPacket(new GetItem(this, player.getObjectId()));
		player.broadcastPacket(new DeleteObject(this));
		getPosition().clearWorldRegion();

		ItemsOnGroundManager.getInstance().removeObject(this);

		final int itemId = getItemId();

		if (MercTicketManager.getTicketCastleId(itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
		}
		else if (itemId == 6353)
		{
			L2PcInstance pc = player.getActingPlayer();
			if (pc != null)
			{
				QuestState qs = pc.getQuestState("255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE" + itemId + "", null, pc);
				}
			}
		}

		L2World.getInstance().removeVisibleObject(this, oldregion);
		setOwnerId(player.getObjectId());
	}

	
	public void removeAugmentation()
	{
		if (_augmentation != null)
		{
			try
			{

				Connection con = L2DatabaseFactory.getInstance().getConnection();
				_removeAqugmentationQueue.execute(con);
				con.close();
				_augmentation = null;
			}
			catch (SQLException e)
			{
			}
		}
	}

	
	public void removeFromDb(Connection con)
	{
		_augmentation = null;
		if (!_existsInDb)
			return;
		try
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not delete item " + getObjectId(), e);
		}
	}

	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}

	
	public void restoreAttributes()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT augAttributes,augSkillId,augSkillLevel FROM item_attributes WHERE itemId=?");
			statement.setInt(1, getObjectId());
			ResultSet rs = statement.executeQuery();
			rs = statement.executeQuery();
			if (rs.next())
			{
				int aug_attributes = rs.getInt(1);
				int aug_skillId = rs.getInt(2);
				int aug_skillLevel = rs.getInt(3);
				if (aug_attributes != -1 && aug_skillId != -1 && aug_skillLevel != -1)
				{
					_augmentation = new L2Augmentation(aug_attributes, aug_skillId, aug_skillLevel);
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not restore augmentation and elemental data for item " + getObjectId() + " from DB: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void restoreInitCount()
	{
		if (_decrease)
		{
			setCount(_initCount);
		}
	}

	private void scheduleConsumeManaTask()
	{
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}

	public boolean setAugmentation(L2Augmentation augmentation)
	{
		if (_augmentation != null)
			return false;
		_augmentation = augmentation;
		updateItemAttributes();
		return true;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	public void setChargedSoulshot(int type, boolean isSkill)
	{
		_chargedSoulshot = type;
		L2PcInstance owner = L2World.getInstance().getPlayer(getOwnerId());
		L2PetInstance petowner = L2World.getInstance().getPet(getOwnerId());
		if (owner != null)
			if (type == CHARGED_NONE && isEquipped() && owner.getAutoSoulShot().size() > 0)
				if (isSkill)
				{
					owner.rechargeAutoSoulShot(true, false, false, true);
				}
				else
				{
					owner.rechargeAutoSoulShot(true, false, false, false);
				}
		if (petowner != null)
			if (type == L2ItemInstance.CHARGED_NONE)
			{
				petowner.getOwner().rechargeAutoSoulShot(true, true, true, false);
			}
	}

	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
		L2PcInstance owner = L2World.getInstance().getPlayer(getOwnerId());
		L2PetInstance petowner = L2World.getInstance().getPet(getOwnerId());
		if (owner != null)
			if (type == CHARGED_NONE && isEquipped() && owner.getAutoSoulShot().size() > 0)
			{
				owner.rechargeAutoSoulShot(true, true, false, true);
			}
		if (petowner != null)
			if (type == L2ItemInstance.CHARGED_NONE)
			{
				petowner.getOwner().rechargeAutoSoulShot(true, true, true, false);
			}
	}

	public void setCount(int count)
	{
		if (getCount() == count)
			return;

		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}

	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}

	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}

	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}

	public void setData(String val)
	{
		_itemData = val;
		_storedInDb = false;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}

	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}

	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _locData)
			return;
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
	}

	public void setMana(int mana)
	{
		_mana = mana;
	}

	protected void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
			return;

		_ownerId = owner_id;
		_storedInDb = false;
	}

	public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference)
	{

		_process = process;
		if (_ownerId == 0)
		{
			_creationTime = System.currentTimeMillis() / 1000;
			if (reference != null)
			{
				if (reference instanceof L2Npc)
				{
					_creator = ((L2Npc) reference).getNpcId();
				}
				else
				{
					_creator = reference.getObjectId();
				}
			}
			else
			{
				_creator = creator == null ? 0 : creator.getObjectId();
			}
		}
		setOwnerId(owner_id);

		if (Config.LOG_ITEMS && !Config.IGNORE_LOG.contains(process.toUpperCase()))
		{
			List<Object> param = new ArrayList<>();
			param.add("CHANGE:" + process);
			param.add(this);
			param.add(creator);
			param.add(reference);
			_logItems.info(param);
		}
	}

	public void setPriceToBuy(int price)
	{
		_priceBuy = price;
		_storedInDb = false;
	}

	public void setPriceToSell(int price)
	{
		_priceSell = price;
		_storedInDb = false;
	}

	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}

	public void setRewardId(int rewarder)
	{
		_rewardId = rewarder;
		_rewardTime = System.currentTimeMillis();

	}

	public void setTime(int time)
	{
		_time = time > 0 ? time : 0;
	}

	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}

	@Override
	public String toString()
	{
		StringBuffer output = new StringBuffer();
		output.append("item " + getObjectId() + ": ");
		if (getEnchantLevel() > 0)
		{
			output.append("+" + getEnchantLevel() + " ");
		}
		output.append(getItem().getName());
		output.append("(" + getCount() + ")");
		output.append(" owner: " + getOwnerId() + ", last process: " + _process);
		if (_creator != 0)
		{
			output.append(", creator " + _creator);
		}
		return output.toString();
	}

	public void updateDatabase()
	{
		updateDatabase(false);
	}

	public void updateDatabase(boolean force)
	{
		if (getUpdateMode(force) != UpdateMode.NONE)
		{
			SQLQueue.getInstance().add(UPDATE_DATABASE_QUERY);
		}
	}

	
	public void updateInDb(Connection con)
	{
		if (_storedInDb)
			return;

		try
		{
			PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,data=? WHERE object_id = ?");
			statement.setInt(1, _ownerId);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, getCustomType1());
			statement.setInt(7, getCustomType2());
			statement.setInt(8, getMana());
			statement.setString(9, _itemData);
			statement.setInt(10, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not update item " + getObjectId(), e);
		}
	}

	
	public void updateItemAttributes()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?,?,?)");
			statement.setInt(1, getObjectId());
			if (_augmentation == null)
			{
				statement.setInt(2, -1);
				statement.setInt(3, -1);
				statement.setInt(4, -1);
			}
			else
			{
				statement.setInt(2, _augmentation.getAttributes());
				if (_augmentation.getSkill() == null)
				{
					statement.setInt(3, 0);
					statement.setInt(4, 0);
				}
				else
				{
					statement.setInt(3, _augmentation.getSkill().getId());
					statement.setInt(4, _augmentation.getSkill().getLevel());
				}
			}
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Could not remove elemental enchant for item: " + getObjectId() + " from DB:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

}