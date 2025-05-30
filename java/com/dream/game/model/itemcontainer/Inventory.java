package com.dream.game.model.itemcontainer;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.ArmorSetsTable;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.model.L2ArmorSet;
import com.dream.game.model.L2Object;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2ItemInstance.ItemLocation;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.SkillCoolTime;
import com.dream.game.skills.l2skills.L2SkillSummon;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2EtcItem;
import com.dream.game.templates.item.L2EtcItemType;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.templates.skills.L2SkillType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public abstract class Inventory extends ItemContainer
{
	final class AmmunationListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (slot != PAPERDOLL_RHAND)
				return;
			if (Config.ASSERT)
			{
				assert item == getPaperdollItem(PAPERDOLL_RHAND);
			}

			switch (item.getItemType().hashCode())
			{
				case 5:
				{
					L2ItemInstance arrow = findArrowForBow(item.getItem());
					if (arrow != null)
					{
						setPaperdollItem(PAPERDOLL_LHAND, arrow);
					}
					break;
				}
			}

		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (slot != PAPERDOLL_RHAND)
				return;
			if (Config.ASSERT)
			{
				assert null == getPaperdollItem(PAPERDOLL_RHAND);
			}

			switch (item.getItemType().hashCode())
			{
				case 5:
				case 13:
				case 16:
					if (getPaperdollItem(PAPERDOLL_LHAND) != null)
					{
						setPaperdollItem(PAPERDOLL_LHAND, null);
					}
					break;
			}
		}
	}

	final class ArmorSetListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
				return;

			L2PcInstance player = (L2PcInstance) getOwner();

			L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
			if (chestItem == null)
				return;

			L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
			if (armorSet == null)
				return;

			if (armorSet.containItem(slot, item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
					if (skill != null)
					{
						player.addSkill(skill, false);
						player.sendSkillList();
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
					}
					if (armorSet.containShield(player))
					{
						L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
						if (skills != null)
						{
							player.addSkill(skills, false);
							player.sendSkillList();
						}
						else
						{
							_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
						}
					}
					if (armorSet.isEnchanted6(player))
					{
						int skillId = armorSet.getEnchant6skillId();
						if (skillId > 0)
						{
							L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
							if (skille != null)
							{
								player.addSkill(skille, false);
								player.sendSkillList();
							}
							else
							{
								_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
							}
						}
					}
				}
			}
			else if (armorSet.containShield(item.getItemId()))
				if (armorSet.containAll(player))
				{
					L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
					if (skills != null)
					{
						player.addSkill(skills, false);
						player.sendSkillList();
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
					}
				}
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
				return;

			L2PcInstance player = (L2PcInstance) getOwner();

			boolean remove = false;
			int removeSkillId1 = 0;
			int removeSkillId2 = 0;
			int removeSkillId3 = 0;
			if (slot == PAPERDOLL_CHEST)
			{
				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
				if (armorSet == null)
					return;

				remove = true;
				removeSkillId1 = armorSet.getSkillId();
				removeSkillId2 = armorSet.getShieldSkillId();
				removeSkillId3 = armorSet.getEnchant6skillId();
			}
			else
			{
				L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
					return;

				L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
				if (armorSet == null)
					return;

				if (armorSet.containItem(slot, item.getItemId())) // removed
				// part of
				// set
				{
					remove = true;
					removeSkillId1 = armorSet.getSkillId();
					removeSkillId2 = armorSet.getShieldSkillId();
					removeSkillId3 = armorSet.getEnchant6skillId();
				}
				else if (armorSet.containShield(item.getItemId())) // removed
				// shield
				{
					remove = true;
					removeSkillId2 = armorSet.getShieldSkillId();
				}
			}

			if (remove)
			{
				if (removeSkillId1 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
					}
				}
				if (removeSkillId2 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
					}
				}
				if (removeSkillId3 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
					}
				}
				player.checkItemRestriction();
				player.sendSkillList();
			}
		}
	}

	public static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<L2ItemInstance> _changed;

		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_changed = new ArrayList<>();
			_inventory.addPaperdollListener(this);
		}

		public L2ItemInstance[] getChangedItems()
		{
			return _changed.toArray(new L2ItemInstance[_changed.size()]);
		}

		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}

		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}
	}

	final class FormalWearListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
				return;

			L2PcInstance owner = (L2PcInstance) getOwner();

			if (item.getItemId() == 6408)
			{
				owner.setIsWearingFormalWear(true);
			}
			else if (!owner.isWearingFormalWear())
				return;
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
				return;

			L2PcInstance owner = (L2PcInstance) getOwner();

			if (item.getItemId() == 6408)
			{
				owner.setIsWearingFormalWear(false);
			}
		}
	}

	final class ItemSkillsListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			L2PcInstance player;

			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
				return;

			L2Skill[] itemSkills = null;
			L2Skill[] enchant4Skills = null;

			L2Item it = item.getItem();

			if (it instanceof L2Weapon)
			{
				if (item.isAugmented())
				{
					item.getAugmentation().applyBonus(player);
				}

				if (item.getItemType() == L2WeaponType.POLE)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(3599, 1);
					if (skill != null)
					{
						player.addSkill(skill);
					}
				}

				itemSkills = ((L2Weapon) it).getSkills();

				if (item.getEnchantLevel() >= 4)
				{
					enchant4Skills = ((L2Weapon) it).getEnchant4Skills();
				}
			}
			else if (it instanceof L2Armor)
			{
				if (item.isAugmented())
				{
					item.getAugmentation().applyBonus(player);
				}

				itemSkills = ((L2Armor) it).getSkills();

			}

			boolean updateTimeStamp = false;

			if (itemSkills != null)
			{
				for (L2Skill itemSkill : itemSkills)
				{
					player.addSkill(itemSkill, false);
					if (itemSkill.isActive())
					{
						itemSkill.markAsItemSkill();
						if (!player.getReuseTimeStamps().containsKey(itemSkill.getId()))
						{
							int equipDelay = itemSkill.getEquipDelay();
							if (equipDelay > 0)
							{
								player.addTimeStamp(itemSkill.getId(), equipDelay);
								player.disableSkill(itemSkill.getId(), equipDelay);
								updateTimeStamp = true;
							}
						}
					}
				}
			}
			if (enchant4Skills != null)
			{
				for (L2Skill itemSkill : enchant4Skills)
				{
					player.addSkill(itemSkill, false);
					if (itemSkill.isActive())
						if (!player.getReuseTimeStamps().containsKey(itemSkill.getId()))
						{
							int equipDelay = itemSkill.getEquipDelay();
							if (equipDelay > 0)
							{
								player.addTimeStamp(itemSkill.getId(), equipDelay);
								player.disableSkill(itemSkill.getId(), equipDelay);
								updateTimeStamp = true;
							}
						}
				}
			}

			if (itemSkills != null || enchant4Skills != null)
			{
				player.sendSkillList();
			}
			if (updateTimeStamp)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			L2PcInstance player;

			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
				return;

			L2Skill[] itemSkills = null;
			L2Skill[] enchant4Skills = null;

			L2Item it = item.getItem();

			if (it instanceof L2Weapon)
			{
				if (item.isAugmented())
				{
					item.getAugmentation().removeBonus(player);
				}

				if (item.getItemType() == L2WeaponType.POLE)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(3599, 1);
					if (skill != null)
					{
						player.removeSkill(skill);
					}
				}

				itemSkills = ((L2Weapon) it).getSkills();
				enchant4Skills = ((L2Weapon) it).getEnchant4Skills();
			}
			else if (it instanceof L2Armor)
			{
				if (item.isAugmented())
				{
					item.getAugmentation().removeBonus(player);
				}

				itemSkills = ((L2Armor) it).getSkills();

			}

			if (itemSkills != null)
			{
				for (L2Skill itemSkill : itemSkills)
				{
					if (itemSkill.getSkillType() == L2SkillType.SUMMON && player.getPet() != null && ((L2SkillSummon) itemSkill).getNpcId() == player.getPet().getTemplate().getIdTemplate())
					{
						player.getPet().unSummon(player);
					}
					player.removeSkill(itemSkill, false, true);
				}
			}
			if (enchant4Skills != null)
			{
				for (L2Skill itemSkill : enchant4Skills)
				{
					player.removeSkill(itemSkill, false, true);
				}
			}

			if (itemSkills != null || enchant4Skills != null)
			{
				player.sendSkillList();
			}
		}
	}

	public interface PaperdollListener
	{
		public void notifyEquiped(int slot, L2ItemInstance inst);

		public void notifyUnequiped(int slot, L2ItemInstance inst);
	}

	public static enum PaperdollSlots
	{
		PAPERDOLL_UNDER,
		PAPERDOLL_REAR,
		PAPERDOLL_LEAR,
		PAPERDOLL_LREAR,
		PAPERDOLL_NECK,
		PAPERDOLL_LFINGER,
		PAPERDOLL_RFINGER,
		PAPERDOLL_LRFINGER,
		PAPERDOLL_HEAD,
		PAPERDOLL_RHAND,
		PAPERDOLL_LHAND,
		PAPERDOLL_GLOVES,
		PAPERDOLL_CHEST,
		PAPERDOLL_LEGS,
		PAPERDOLL_FEET,
		PAPERDOLL_BACK,
		PAPERDOLL_LRHAND,
		PAPERDOLL_FULLARMOR,
		PAPERDOLL_HAIR,
		PAPERDOLL_ALLDRESS,
		PAPERDOLL_FACE,
		PAPERDOLL_HAIR2,
		PAPERDOLL_HAIRALL

	}

	final class StatsListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
				return;
			getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
		}

		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (slot == PAPERDOLL_LRHAND)
				return;
			getOwner().removeStatsOwner(item);
		}
	}

	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_REAR = 1;
	public static final int PAPERDOLL_LEAR = 2;
	public static final int PAPERDOLL_LREAR = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_LFINGER = 5;
	public static final int PAPERDOLL_RFINGER = 6;
	public static final int PAPERDOLL_LRFINGER = 7;
	public static final int PAPERDOLL_HEAD = 8;
	public static final int PAPERDOLL_RHAND = 9;
	public static final int PAPERDOLL_LHAND = 10;
	public static final int PAPERDOLL_GLOVES = 11;
	public static final int PAPERDOLL_CHEST = 12;
	public static final int PAPERDOLL_LEGS = 13;
	public static final int PAPERDOLL_FEET = 14;
	public static final int PAPERDOLL_BACK = 15;
	public static final int PAPERDOLL_LRHAND = 16;
	public static final int PAPERDOLL_FULLARMOR = 17;
	public static final int PAPERDOLL_HAIR = 18;
	public static final int PAPERDOLL_ALLDRESS = 19;
	public static final int PAPERDOLL_FACE = 20;
	public static final int PAPERDOLL_HAIR2 = 20;
	public static final int PAPERDOLL_HAIRALL = 21;

	public static final int PAPERDOLL_TOTALSLOTS = 22;

	public static final double MAX_ARMOR_WEIGHT = 12000;

	public static int getPaperdollIndex(int slot)
	{
		switch (slot)
		{
			case L2Item.SLOT_UNDERWEAR:
				return PAPERDOLL_UNDER;
			case L2Item.SLOT_R_EAR:
				return PAPERDOLL_REAR;
			case L2Item.SLOT_L_EAR:
				return PAPERDOLL_LEAR;
			case L2Item.SLOT_NECK:
				return PAPERDOLL_NECK;
			case L2Item.SLOT_R_FINGER:
				return PAPERDOLL_RFINGER;
			case L2Item.SLOT_L_FINGER:
				return PAPERDOLL_LFINGER;
			case L2Item.SLOT_HEAD:
				return PAPERDOLL_HEAD;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				return PAPERDOLL_RHAND;
			case L2Item.SLOT_L_HAND:
				return PAPERDOLL_LHAND;
			case L2Item.SLOT_GLOVES:
				return PAPERDOLL_GLOVES;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_ALLDRESS:
				return PAPERDOLL_CHEST;
			case L2Item.SLOT_LEGS:
				return PAPERDOLL_LEGS;
			case L2Item.SLOT_FEET:
				return PAPERDOLL_FEET;
			case L2Item.SLOT_BACK:
				return PAPERDOLL_BACK;
			case L2Item.SLOT_FACE:
			case L2Item.SLOT_HAIRALL:
				return PAPERDOLL_FACE;
			case L2Item.SLOT_HAIR:
				return PAPERDOLL_HAIR;
		}
		return -1;
	}

	private final L2ItemInstance[] _paperdoll;

	private final List<PaperdollListener> _paperdollListeners;

	protected int _totalWeight;

	private int _wearedMask;

	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();
		addPaperdollListener(new AmmunationListener());
		addPaperdollListener(new StatsListener());
		addPaperdollListener(new ItemSkillsListener());
		addPaperdollListener(new ArmorSetListener());
		addPaperdollListener(new FormalWearListener());
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			equipItem(item);
		}
	}

	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		if (Config.ASSERT)
		{
			assert !_paperdollListeners.contains(listener);
		}
		_paperdollListeners.add(listener);
	}

	public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);

		if (item == null)
			return null;

		if (item.getCount() > count)
		{
			item.changeCount(process, -count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);

			item = ItemTable.createItem(process, item.getItemId(), count, actor, reference);

			refreshWeight();
			return item;
		}

		return dropItem(process, item, actor, reference);
	}

	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		if (!_items.contains(item))
			return null;

		if (item == null)
			return null;

		synchronized (item)
		{
			if (!_items.contains(item))
				return null;

			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			refreshWeight();
		}
		return item;
	}

	public void equipItem(L2ItemInstance item)
	{
		if (getOwner() instanceof L2PcInstance && ((L2PcInstance) getOwner()).getPrivateStoreType() != 0)
			return;

		int targetSlot = item.getItem().getBodyPart();

		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				if (setPaperdollItem(PAPERDOLL_LHAND, null) != null)
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}

				setPaperdollItem(PAPERDOLL_RHAND, item);
				setPaperdollItem(PAPERDOLL_LRHAND, item);

				if (item.getItem().getItemType() == L2WeaponType.BOW)
				{
					L2ItemInstance arrow = findArrowForBow(item.getItem());
					if (arrow != null)
					{
						setPaperdollItem(PAPERDOLL_LHAND, arrow);
					}
				}
				break;
			}
			case L2Item.SLOT_L_HAND:
			{
				if (!(item.getItem() instanceof L2EtcItem) || item.getItem().getItemType() != L2EtcItemType.ARROW)
				{
					L2ItemInstance old1 = setPaperdollItem(PAPERDOLL_LRHAND, null);

					if (old1 != null)
					{
						setPaperdollItem(PAPERDOLL_RHAND, null);
					}
				}

				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			}
			case L2Item.SLOT_R_HAND:
			{
				if (_paperdoll[PAPERDOLL_LRHAND] != null)
				{
					setPaperdollItem(PAPERDOLL_LRHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}

				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR | L2Item.SLOT_LR_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if (_paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, null);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				break;
			}
			case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER | L2Item.SLOT_LR_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, null);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_LEGS:
			{
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				{
					setPaperdollItem(PAPERDOLL_CHEST, null);
				}

				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			case L2Item.SLOT_HAIR:
				L2ItemInstance slot = getPaperdollItem(PAPERDOLL_HAIRALL);
				if (slot != null)
				{
					setPaperdollItem(PAPERDOLL_HAIRALL, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
				}
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_FACE:
				L2ItemInstance slot2 = getPaperdollItem(PAPERDOLL_HAIRALL);
				if (slot2 != null)
				{
					setPaperdollItem(PAPERDOLL_HAIRALL, null);
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				setPaperdollItem(PAPERDOLL_FACE, item);
				break;
			case L2Item.SLOT_HAIRALL:
				L2ItemInstance slot3 = getPaperdollItem(PAPERDOLL_HAIR);
				if (slot3 != null)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_FACE, null);
				}
				setPaperdollItem(PAPERDOLL_HAIRALL, item);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			default:
				_log.warn("unknown body slot:" + targetSlot + " for item ID:" + item.getItemId());
				break;
		}
	}

	public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
	{
		Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();

	}

	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		int arrowsId = 0;

		switch (bow.getCrystalType())
		{
			default:
			case L2Item.CRYSTAL_NONE:
				arrowsId = 17;
				break;
			case L2Item.CRYSTAL_D:
				arrowsId = 1341;
				break;
			case L2Item.CRYSTAL_C:
				arrowsId = 1342;
				break;
			case L2Item.CRYSTAL_B:
				arrowsId = 1343;
				break;
			case L2Item.CRYSTAL_A:
				arrowsId = 1344;
				break;
			case L2Item.CRYSTAL_S:
				arrowsId = 1345;
				break;
			case L2Item.CRYSTAL_R:
				arrowsId = 1345;
				break;
			case L2Item.CRYSTAL_S80:
				arrowsId = 1345;
				break;
			case L2Item.CRYSTAL_S84:
				arrowsId = 1345;
				break;
		}
		return getItemByItemId(arrowsId);
	}

	protected abstract ItemLocation getEquipLocation();

	public int getPaperdollAugmentationId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null && item.getAugmentation() != null)
			return item.getAugmentation().getAugmentationId();

		return 0;
	}

	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
	{
		switch (slot)
		{
			case 0x01:
				return _paperdoll[0];
			case 0x02:
				return _paperdoll[1];
			case 0x04:
				return _paperdoll[2];
			case 0x06:
				return _paperdoll[3];
			case 0x08:
				return _paperdoll[4];
			case 0x10:
				return _paperdoll[5];
			case 0x20:
				return _paperdoll[6];
			case 0x30:
				return _paperdoll[7];
			case 0x040:
				return _paperdoll[8];
			case 0x080:
				return _paperdoll[9];
			case 0x0100:
				return _paperdoll[10];
			case 0x0200:
				return _paperdoll[11];
			case 0x0400:
				return _paperdoll[12];
			case 0x0800:
				return _paperdoll[13];
			case 0x1000:
				return _paperdoll[14];
			case 0x2000:
				return _paperdoll[15];
			case 0x4000:
				return _paperdoll[16];
			case 0x8000:
				return _paperdoll[17];
			case 0x010000:
				return _paperdoll[18];
			case 0x020000:
				return _paperdoll[19];
			case 0x040000:
				return _paperdoll[20];
			case 0x080000:
				return _paperdoll[21];
			case 0x100000:
				return _paperdoll[22];
			case 0x200000:
				return _paperdoll[23];
			case 0x400000:
				return _paperdoll[24];
			case 0x10000000:
				return _paperdoll[30];
		}
		return null;
	}

	public int getPaperdollItemDisplayId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getItemDisplayId();
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_HAIRALL];
			if (item != null)
				return item.getItemDisplayId();
		}
		return 0;
	}

	public int getPaperdollItemId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getItemId();
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_HAIRALL];
			if (item != null)
				return item.getItemId();
		}
		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getObjectId();
		else if (slot == PAPERDOLL_HAIR)
		{

			item = _paperdoll[PAPERDOLL_HAIRALL];
			if (item != null)
				return item.getObjectId();
		}
		return 0;
	}

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	public int getWearedMask()
	{
		return _wearedMask;
	}

	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}

	@Override
	protected void refreshWeight()
	{
		int weight = 0;
		for (L2ItemInstance item : _items)
			if (item != null && item.getItem() != null)
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		_totalWeight = weight;
	}

	public void reloadEquippedItems()
	{
		L2ItemInstance item;
		int slot;

		for (L2ItemInstance element : _paperdoll)
		{
			item = element;
			if (item == null)
			{
				continue;
			}
			slot = item.getLocationSlot();

			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}
				listener.notifyUnequiped(slot, item);
				listener.notifyEquiped(slot, item);
			}
		}
	}

	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		for (int i = 0; i < _paperdoll.length; i++)
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}

		return super.removeItem(item);
	}

	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}

	
	@Override
	public void restore()
	{
		_items.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=? OR loc=?) ");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());

			ResultSet inv = statement.executeQuery();

			L2ItemInstance item;
			while (inv.next())
			{
				item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);
				if (item == null)
				{
					continue;
				}

				L2World.getInstance().storeObject(item);
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
				{
					addItem("Restore", item, null, getOwner());
				}
				else
				{
					addItem(item);
				}
			}

			inv.close();
			statement.close();
			refreshWeight();
			restoreEquipedItemsPassiveSkill();
			restoreArmorSetPassiveSkill();
		}
		catch (Exception e)
		{
			_log.warn("Could not restore inventory : " + e);
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void restoreArmorSetPassiveSkill()
	{
		if (!(getOwner() instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) getOwner();

		L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
		if (chestItem == null)
			return;

		L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
		if (armorSet == null)
			return;

		if (armorSet.containAll(player))
		{
			L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
			if (skill != null)
			{
				((L2PcInstance) getOwner()).addSkill(skill, false);
			}
			else
			{
				_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
			}

			if (armorSet.containShield(player))
			{
				L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
				if (skills != null)
				{
					player.addSkill(skills, false);
				}
				else
				{
					_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
				}
			}
			if (armorSet.isEnchanted6(player))
			{
				int skillId = armorSet.getEnchant6skillId();
				if (skillId > 0)
				{
					L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
					if (skille != null)
					{
						player.addSkill(skille, false);
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
					}
				}
			}
		}
	}

	public void restoreEquipedItemsPassiveSkill()
	{
		if (!(getOwner() instanceof L2PcInstance))
			return;
		for (int i = 0; i < 19; i++)
		{
			if (getPaperdollItem(i) == null)
			{
				continue;
			}
			_paperdollListeners.get(2).notifyEquiped(i, getPaperdollItem(i));
		}
	}

	public synchronized L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		L2ItemInstance old = _paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				old.setLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);

				int mask = 0;
				for (int i = 0; i < PAPERDOLL_LRHAND; i++)
				{
					L2ItemInstance pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				_wearedMask = mask;

				for (PaperdollListener temp : _paperdollListeners)
				{
					try
					{
						temp.notifyUnequiped(slot, old);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener temp : _paperdollListeners)
				{
					temp.notifyEquiped(slot, item);
				}
			}
		}
		return old;
	}

	private void unEquipItemInBodySlot(int slot)
	{
		int pdollSlot = -1;

		switch (slot)
		{
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_FACE:
				pdollSlot = PAPERDOLL_FACE;
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_FACE, null);
				pdollSlot = PAPERDOLL_HAIRALL;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_ALLDRESS:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_BACK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case L2Item.SLOT_LR_HAND:
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);
				pdollSlot = PAPERDOLL_RHAND;
				pdollSlot = PAPERDOLL_LRHAND;
				break;
		}

		if (pdollSlot >= 0)
		{
			setPaperdollItem(pdollSlot, null);
		}
	}

	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInBodySlot(slot);
			if (getOwner() instanceof L2PcInstance)
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public L2ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}

	public L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInSlot(slot);
			if (getOwner() instanceof L2PcInstance)
			{
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public abstract void updateInventory(L2ItemInstance item);

}