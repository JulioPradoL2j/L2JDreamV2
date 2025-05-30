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

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.PetDataTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.geodata.GeoData;
import com.dream.game.handler.IItemHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.idfactory.IdFactory;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.manager.ItemsOnGroundManager;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2PetData;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.stat.PetStat;
import com.dream.game.model.actor.status.PetStatus;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.model.itemcontainer.PetInventory;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.MoveToPawn;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.PetInventoryUpdate;
import com.dream.game.network.serverpackets.PetItemList;
import com.dream.game.network.serverpackets.PetStatusShow;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.StopMove;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.ValidateLocation;
import com.dream.game.skills.Stats;
import com.dream.game.taskmanager.DecayTaskManager;
import com.dream.game.templates.chars.L2NpcTemplate;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.tools.random.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

public class L2PetInstance extends L2Summon
{
	class FeedTask implements Runnable
	{
		private L2ItemInstance ownerFood;

		private int getFeedConsume()
		{
			if (isInCombat())
				return getPetData().getPetFeedBattle();
			return getPetData().getPetFeedNormal();
		}

		@Override
		public void run()
		{
			try
			{
				if (getOwner() == null || getOwner().getPet() == null || getOwner().getPet().getObjectId() != getObjectId())
				{
					stopFeed();
					return;
				}
				else if (getCurrentFed() > getFeedConsume())
				{
					setCurrentFed(getCurrentFed() - getFeedConsume());
				}
				else
				{
					setCurrentFed(0);
				}

				int[] foodIds = PetDataTable.getFoodItemId(getTemplate().getNpcId());
				if (foodIds[0] == 0)
					return;

				L2ItemInstance food = null;
				ownerFood = null;
				food = getInventory().getItemByItemId(foodIds[0]);
				if (PetDataTable.isStrider(getNpcId()))
					if (getInventory().getItemByItemId(foodIds[1]) != null)
					{
						food = getInventory().getItemByItemId(foodIds[1]);
					}
				if (food == null)
				{
					ownerFood = getOwner().getInventory().getItemByItemId(foodIds[0]);
				}

				if (isRunning() && isHungry())
				{
					setWalking();
				}
				else if (!isHungry() && !isRunning())
				{
					setRunning();
				}
				if ((food != null || ownerFood != null) && isHungry())
				{
					IItemHandler handler = null;
					if (food != null)
					{
						handler = ItemHandler.getInstance().getItemHandler(food.getItemId());
						if (handler != null)
						{
							getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food.getItemId()));
							handler.useItem(L2PetInstance.this, food);
						}
					}
					else
					{
						handler = ItemHandler.getInstance().getItemHandler(ownerFood.getItemId());
						if (handler != null)
						{
							getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(ownerFood.getItemId()));
							handler.useItem(L2PetInstance.this, ownerFood);
						}
					}
				}
				else
				{
					if (getCurrentFed() == 0)
					{
						getOwner().sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY);
						if (Rnd.get(100) < 30)
						{
							stopFeed();
							getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
							_log.info("Hungry pet deleted for player :" + getOwner().getName() + " Control Item Id :" + getControlItemId());
							deleteMe(getOwner());
						}
					}
					else if (getCurrentFed() < 0.11 * getPetData().getPetMaxFeed())
					{
						getOwner().sendMessage(Message.getMessage(getOwner(), Message.MessageId.MSG_PET_IS_TO_HUNGRY));
						if (Rnd.get(100) < 3)
						{
							stopFeed();
							getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
							_log.info("Hungry pet deleted for player :" + getOwner().getName() + " Control Item Id :" + getControlItemId());
							deleteMe(getOwner());
						}
					}
					broadcastStatusUpdate();
				}
			}
			catch (Exception e)
			{
				_log.error("Pet [ObjectId: " + getObjectId() + "] a feed task error has occurred", e);
			}
		}
	}

	public final static Logger _log = Logger.getLogger(L2PetInstance.class.getName());

	
	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		Connection con = null;
		try
		{
			L2PetInstance pet;
			if (template.getType().compareToIgnoreCase("L2BabyPet") == 0)
			{
				pet = new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			}
			else if (template.getType().compareToIgnoreCase("L2HelperPet") == 0)
			{
				pet = new L2HelperPetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			}
			else
			{
				pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			}

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed, weapon, armor, jewel FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			ResultSet rset = statement.executeQuery();
			if (!rset.next())
			{
				rset.close();
				statement.close();
				return pet;
			}

			pet.setName(rset.getString("name"));

			if (template.getNpcId() == 16043 || template.getNpcId() == 16044 || template.getNpcId() == 16045 || template.getNpcId() == 16046)
			{
				pet.getStat().setLevel((byte) owner.getLevel());
			}
			else
			{
				pet.getStat().setLevel(rset.getByte("level"));
			}
			pet.getStat().setExp(rset.getLong("exp"));
			pet.getStat().setSp(rset.getInt("sp"));

			if (rset.getDouble("curHp") < 0.5)
			{
				pet.setIsDead(true);
				pet.getStatus().stopHpMpRegeneration();
			}

			int curFed = rset.getInt("fed");

			pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
			pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());

			pet.setWeapon(rset.getInt("weapon"));
			pet.setArmor(rset.getInt("armor"));
			pet.setJewel(rset.getInt("jewel"));

			if (curFed == 0)
			{
				int foodId[] = PetDataTable.getFoodItemId(pet.getTemplate().getNpcId());
				if (foodId[0] != 0)
				{
					L2ItemInstance food = pet.getOwner().getInventory().getItemByItemId(foodId[0]);

					if (food != null && pet.getOwner().destroyItem("Feed", food.getObjectId(), 1, null, false))
					{
						curFed = pet.getCurrentFed() + 100;
					}
					else
					{
						pet.getOwner().sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
						rset.close();
						statement.close();
						return null;
					}
				}
			}

			pet.setCurrentFed(curFed);

			rset.close();
			statement.close();

			pet._respawned = true;

			return pet;
		}
		catch (SQLException e)
		{
			_log.error("Failed to restore pet data", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return null;
	}

	public synchronized static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		if (L2World.getInstance().getPet(owner.getObjectId()) != null)
			return null;

		L2PetInstance pet = restore(control, template, owner);
		if (pet != null)
		{
			L2World.getInstance().addPet(owner.getObjectId(), pet);
		}

		return pet;
	}

	private int _curFed;
	public PetInventory _inventory;

	public final int _controlItemId;
	public boolean _respawned;
	public boolean _mountable;
	private Future<?> _feedTask;

	private int _weapon;

	private int _armor;

	private int _jewel;

	private int _curWeightPenalty = 0;

	private L2PetData _data;

	private long _expBeforeDeath = 0;

	private int _maxload;

	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		super(objectId, template, owner);
		getStat();

		_controlItemId = control.getObjectId();

		if (template.getNpcId() == 12564 || template.getNpcId() == 16043 || template.getNpcId() == 16044 || template.getNpcId() == 16045 || template.getNpcId() == 16046)
		{
			getStat().setLevel((byte) getOwner().getLevel());
		}
		else
		{
			getStat().setLevel(template.getLevel());
		}

		_inventory = new PetInventory(this);
		int npcId = template.getNpcId();
		_mountable = PetDataTable.isMountable(npcId);
		_maxload = getPetData().getPetMaxLoad();
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (getNpcId() == 12564)
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.SINEATER_XP_RATE), addToSp);
		}
		else
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.PET_XP_RATE), addToSp);
		}
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		refreshOverloaded();

		super.broadcastFullInfoImpl();
	}

	private void deathPenalty()
	{

		int lvl = getStat().getLevel();
		double percentLost = -0.07 * lvl + 6.5;

		long lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);

		_expBeforeDeath = getStat().getExp();

		getStat().addExp(-lostExp);
	}

	@Override
	public void deleteMe(L2PcInstance owner)
	{
		getOwner().removeReviving();
		getOwner().sendPacket(SystemMessageId.YOUR_PETS_CORPSE_HAS_DECAYED);
		super.deleteMe(owner);
		destroyControlItem(owner);
	}

	
	public void destroyControlItem(L2PcInstance owner)
	{
		L2World.getInstance().removePet(owner.getObjectId());

		try
		{
			L2ItemInstance removedItem = owner.getInventory().destroyItem("PetDestroy", getControlItemId(), 1, getOwner(), this);
			owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(removedItem));

			InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			owner.sendPacket(iu);

			StatusUpdate su = new StatusUpdate(owner);
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);

			owner.broadcastUserInfo();

			L2World world = L2World.getInstance();
			world.removeObject(removedItem);
		}
		catch (Exception e)
		{
			_log.error("Error while destroying control item: ", e);
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, getControlItemId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("Failed to delete Pet [ObjectId: " + getObjectId() + "]", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		_inventory.updateInventory(item);
		if (sendMessage)
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addNumber(count));
		}

		return true;
	}

	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);
		if (item == null)
		{
			if (sendMessage)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		_inventory.updateInventory(item);

		if (sendMessage)
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addNumber(count));
		}

		return true;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer, true))
			return false;

		getOwner().sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS);

		stopFeed();

		getStatus().stopHpMpRegeneration();
		DecayTaskManager.getInstance().addDecayTask(this);
		if (isRespawned())
		{
			deathPenalty();
		}
		return true;
	}

	@Override
	protected void doPickupItem(L2Object object)
	{
		boolean follow = getFollowStatus();
		if (isDead())
			return;

		getAI().setIntention(CtrlIntention.IDLE);
		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));

		if (!(object instanceof L2ItemInstance))
		{
			_log.warn("Trying to pickup wrong target." + object);
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2ItemInstance target = (L2ItemInstance) object;

		if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target));
			return;
		}

		long weight = ItemTable.getInstance().getTemplate(target.getItemId()).getWeight() * target.getCount();

		if (weight > Integer.MAX_VALUE || weight < 0 || !getInventory().validateWeight((int) weight))
		{
			sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
			return;
		}

		synchronized (target)
		{
			if (!target.isVisible())
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!_inventory.validateCapacity(target))
			{
				getOwner().sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			if (!_inventory.validateWeight(target, target.getCount()))
			{
				getOwner().sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}

			if (target.getOwnerId() != 0 && target.getOwnerId() != getOwner().getObjectId() && !getOwner().isInLooterParty(target.getOwnerId()))
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);

				if (target.getItemId() == 57)
				{
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(target.getCount()));
				}
				else if (target.getCount() > 1)
				{
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target).addNumber(target.getCount()));
				}
				else
				{
					getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target));
				}

				return;
			}
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getOwner().getObjectId() || getOwner().isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}

			target.pickupMe(this);

			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
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

			ItemTable.destroyItem("Consume", target, getOwner(), null);

			broadcastStatusUpdate();
		}
		else
		{
			if (target.getItemId() == 57)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addNumber(target.getCount()));
			}
			else if (target.getEnchantLevel() > 0)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(target.getEnchantLevel()).addString(target.getName()));
			}
			else if (target.getCount() > 1)
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addNumber(target.getCount()).addString(target.getName()));
			}
			else
			{
				getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addString(target.getName()));
			}

			getInventory().addItem("Pickup", target, getOwner(), this);
			getOwner().sendPacket(new PetItemList(this));
		}

		getAI().setIntention(CtrlIntention.IDLE);

		if (follow)
		{
			followOwner();
		}
	}

	@Override
	public void doRevive()
	{
		getOwner().removeReviving();

		super.doRevive();
		super.stopDecay();
		startFeed();
		if (!isHungry())
		{
			setRunning();
		}
		getAI().setIntention(CtrlIntention.ACTIVE, null);
	}

	@Override
	public void doRevive(double revivePower)
	{
		restoreExp(revivePower);
		doRevive();
	}

	public void dropItemHere(L2ItemInstance dropit)
	{
		dropItemHere(dropit, false);
	}

	public void dropItemHere(L2ItemInstance dropit, boolean protect)
	{
		dropit = getInventory().dropItem("PetDrop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);

		if (dropit != null)
		{
			if (protect)
			{
				dropit.getDropProtection().protect(getOwner());
			}
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}

	@Override
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		for (L2ItemInstance item : getInventory().getItems())
			if (item.getLocation() == L2ItemInstance.ItemLocation.PET_EQUIP && item.getItem().getBodyPart() == L2Item.SLOT_R_HAND)
				return item;

		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null)
			return null;

		return (L2Weapon) weapon.getItem();
	}

	@Override
	public final int getArmor()
	{
		return _armor;
	}

	public L2ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlItemId);
	}

	@Override
	public int getControlItemId()
	{
		return _controlItemId;
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	@Override
	public int getCurrentFed()
	{
		return _curFed;
	}

	@Override
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}

	@Override
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}

	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getLevel() + 1);
	}

	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getLevel());
	}

	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public int getInventoryLimit()
	{
		return Config.ALT_INVENTORY_MAXIMUM_PET;
	}

	public final int getJewel()
	{
		return _jewel;
	}

	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}

	@Override
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	@Override
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}

	@Override
	public int getMaxFed()
	{
		return getStat().getMaxFeed();
	}

	@Override
	public final int getMaxLoad()
	{
		return _maxload;
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	@Override
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	@Override
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}

	@Override
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public final L2PetData getPetData()
	{
		if (_data == null)
		{
			_data = PetDataTable.getInstance().getPetData(getTemplate().getNpcId(), getStat().getLevel());
		}

		return _data;
	}

	@Override
	public int getPetSpeed()
	{
		return getPetData().getPetSpeed();
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

	@Override
	public int getSkillLevel(int skillId)
	{
		synchronized (_skills)
		{
			if (_skills == null || _skills.get(skillId) == null)
				return -1;
		}
		int lvl = getLevel();

		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}

	@Override
	public PetStat getStat()
	{
		if (_stat == null)
		{
			_stat = new PetStat(this);
		}

		return (PetStat) _stat;
	}

	@Override
	public final PetStatus getStatus()
	{
		if (_status == null)
		{
			_status = new PetStatus(this);
		}

		return (PetStatus) _status;
	}

	@Override
	public int getSummonType()
	{
		return 2;
	}

	@Override
	public final int getWeapon()
	{
		return _weapon;
	}

	@Override
	public void giveAllToOwner()
	{
		try
		{
			Inventory petInventory = getInventory();
			for (L2ItemInstance giveit : petInventory.getItems())
				if (giveit.getItem().getWeight() * giveit.getCount() + getOwner().getInventory().getTotalWeight() < getOwner().getMaxLoad())
				{
					giveItemToOwner(giveit);
				}
				else
				{
					dropItemHere(giveit);
				}
		}
		catch (Exception e)
		{
			_log.error("Give all items error ", e);
		}
	}

	public void giveItemToOwner(L2ItemInstance item)
	{
		try
		{
			getInventory().transferItem("PetTransfer", item.getObjectId(), item.getCount(), getOwner().getInventory(), getOwner(), this);
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			ItemList PlayerUI = new ItemList(getOwner(), false);
			petiu.addRemovedItem(item);
			getOwner().sendPacket(petiu);
			getOwner().sendPacket(PlayerUI);
		}
		catch (Exception e)
		{
			_log.error("Error while giving item to owner: ", e);
		}
	}

	@Override
	public boolean isHungry()
	{
		return getCurrentFed() < 0.55 * getPetData().getPetMaxFeed();
	}

	@Override
	public boolean isMountable()
	{
		return _mountable;
	}

	public boolean isRespawned()
	{
		return _respawned;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		boolean isOwner = player.getObjectId() == getOwner().getObjectId();
		player.sendPacket(new ValidateLocation(this));
		if (isOwner && player != getOwner())
		{
			updateRefOwner(player);
		}
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));

			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (isAutoAttackable(player) && !isOwner)
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
		else if (!isInsideRadius(player, 150, false, false))
		{
			if (Config.GEODATA)
			{
				if (GeoData.getInstance().canSeeTarget(player, this))
				{
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
					player.onActionRequest();
				}
			}
			else
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
				player.onActionRequest();
			}
		}
		else if (isOwner)
		{
			player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
			player.sendPacket(new PetStatusShow(this));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			int weightproc = getCurrentLoad() * 1000 / maxLoad;
			weightproc = (int) calcStat(Stats.WEIGHT_LIMIT, weightproc, this, null);
			int newWeightPenalty;
			if (weightproc < 500 || getOwner().getDietMode())
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

			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0)
				{
					addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() >= maxLoad);
				}
				else
				{
					super.removeSkill(getKnownSkill(4270));
					setIsOverloaded(false);
				}
			}
		}
	}

	protected void restoreExp(double restorePercent)
	{
		if (_expBeforeDeath > 0)
		{
			getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}

	public final void setArmor(int id)
	{
		_armor = id;
	}

	public void setCurrentFed(int num)
	{
		_curFed = num > getMaxFed() ? getMaxFed() : num;
	}

	public final void setJewel(int id)
	{
		_jewel = id;
	}

	public final void setMaxLoad(int maxLoad)
	{
		_maxload = maxLoad;
	}

	public final void setPetData(L2PetData value)
	{
		_data = value;
	}

	public final void setWeapon(int id)
	{
		_weapon = id;
	}

	public synchronized void startFeed()
	{
		stopFeed();
		if (!isDead() && getOwner().getPet() == this)
		{
			_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
		}
	}

	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}

	
	@Override
	public void store()
	{
		if (getControlItemId() == 0)
			return;

		String req;
		if (!isRespawned())
		{
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,weapon,armor,jewel,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
		}
		else
		{
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,weapon=?,armor=?,jewel=? WHERE item_obj_id = ?";
		}
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setLong(5, getStat().getExp());
			statement.setInt(6, getStat().getSp());
			statement.setInt(7, getCurrentFed());
			statement.setInt(8, getWeapon());
			statement.setInt(9, getArmor());
			statement.setInt(10, getJewel());
			statement.setInt(11, getControlItemId());
			statement.executeUpdate();
			statement.close();

			_respawned = true;
		}
		catch (SQLException e)
		{
			_log.error("Failed to store Pet [ObjectId: " + getObjectId() + "] data", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		L2ItemInstance itemInst = getControlItem();
		if (itemInst != null && itemInst.getEnchantLevel() != getStat().getLevel())
		{
			itemInst.setEnchantLevel(getStat().getLevel());
		}
	}

	public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance oldItem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);

		if (newItem == null)
			return null;

		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if (oldItem.getCount() > 0 && oldItem != newItem)
		{
			petIU.addModifiedItem(oldItem);
		}
		else
		{
			petIU.addRemovedItem(oldItem);
		}
		getOwner().sendPacket(petIU);

		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			InventoryUpdate playerUI = new InventoryUpdate();
			if (newItem.getCount() > count)
			{
				playerUI.addModifiedItem(newItem);
			}
			else
			{
				playerUI.addNewItem(newItem);
			}
			targetPlayer.sendPacket(playerUI);

			StatusUpdate playerSU = new StatusUpdate(targetPlayer);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			petIU = new PetInventoryUpdate();
			if (newItem.getCount() > count)
			{
				petIU.addRemovedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		getInventory().refreshWeight();
		return newItem;
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		stopFeed();
		super.unSummon(owner);
		if (getInventory() != null)
		{
			getInventory().deleteMe();
		}
		L2World.getInstance().removePet(owner.getObjectId());
	}

	public void updateRefOwner(L2PcInstance owner)
	{
		int oldOwnerId = getOwner().getObjectId();

		setOwner(owner);
		L2World.getInstance().removePet(oldOwnerId);
		L2World.getInstance().addPet(oldOwnerId, this);
	}
}