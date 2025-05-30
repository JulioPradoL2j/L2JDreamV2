package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.Shutdown;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.base.Race;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExPutEnchantTargetItemResult;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.network.serverpackets.UserInfo;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;

public class RequestEnchantItem extends L2GameClientPacket
{
	private static final int[] CRYSTAL_SCROLLS =
	{
		731,
		732,
		949,
		950,
		953,
		954,
		957,
		958,
		961,
		962
	};
	
	private static final int[] NORMAL_WEAPON_SCROLLS =
	{
		729,
		947,
		951,
		955,
		959
	};
	
	private static final int[] BLESSED_WEAPON_SCROLLS =
	{
		6569,
		6571,
		6573,
		6575,
		6577
	};
	
	private static final int[] CRYSTAL_WEAPON_SCROLLS =
	{
		731,
		949,
		953,
		957,
		961
	};
	
	private static final int[] NORMAL_ARMOR_SCROLLS =
	{
		730,
		948,
		952,
		956,
		960
	};
	
	private static final int[] BLESSED_ARMOR_SCROLLS =
	{
		6570,
		6572,
		6574,
		6576,
		6578
	};
	
	private static final int[] CRYSTAL_ARMOR_SCROLLS =
	{
		732,
		950,
		954,
		958,
		962
	};
	
	private static final int[] DONATOR_WEAPON_SCROLL =
	{
		9210
	};
	private static final int[] DONATOR_ARMOR_SCROLL =
	{
		9211
	};
	
	private static final int[] DONATOR_SCROLLS =
	{
		9210,
		9211
	};
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || _objectId == 0)
			return;
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		if (activeChar.isOlympiadStart())
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
			
		}
		if (activeChar.isAio())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isFlying() || activeChar.isFishing() || activeChar.isCastingNow() || activeChar.isMounted() || activeChar.getActiveWarehouse() != null || activeChar.isMuted() || activeChar.isParalyzed() || activeChar.inObserverMode() || activeChar.isStunned() || activeChar.isConfused() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isCursedWeaponEquipped())
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isOnline() == 0)
		{
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
			activeChar.setActiveEnchantItem(null);
			return;
		}
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_ENCHANT && Shutdown.getCounterInstance() != null && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			return;
		}
		if (Config.ENCHAT_TIME > 0 && !FloodProtector.tryPerformAction(activeChar, Protected.ENCHANT))
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			activeChar.setActiveEnchantItem(null);
			return;
		}
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
			activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
			return;
		}
		if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
			activeChar.setActiveEnchantItem(null);
			return;
		}
		if (activeChar.getPrivateStoreType() != 0 || activeChar.getTrading())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ENCHANT_WHILE_STORE);
			return;
		}
		if (item == null || scroll == null || activeChar.getInventory().getItemByObjectId(scroll.getObjectId()) != scroll)
		{
			activeChar.setActiveEnchantItem(null);
			return;
		}
		if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
			return;
		if (item.isWear())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant the wedding gift", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		if (item.getItem().getItemType() == L2WeaponType.ROD || item.isShadowItem() || !Config.ENCHANT_HERO_WEAPONS && item.isHeroItem() || item.getItemId() >= 7816 && item.getItemId() <= 7831 || item.getItem().isCommonItem())
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
			activeChar.setActiveEnchantItem(null);
			return;
		}
		try
		{
			int itemType2 = item.getItem().getType2();
			boolean enchantItem = false;
			boolean enchantBreak = true;
			int crystalId = 0;
			
			switch (item.getItem().getCrystalType())
			{
				case L2Item.CRYSTAL_A:
					crystalId = 1461;
					switch (scroll.getItemId())
					{
						case 729:
						case 731:
						case 6569:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 730:
						case 732:
						case 6570:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_B:
					crystalId = 1460;
					switch (scroll.getItemId())
					{
						case 947:
						case 949:
						case 6571:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 948:
						case 950:
						case 6572:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_C:
					crystalId = 1459;
					switch (scroll.getItemId())
					{
						case 951:
						case 953:
						case 6573:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 952:
						case 954:
						case 6574:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_D:
					crystalId = 1458;
					switch (scroll.getItemId())
					{
						case 955:
						case 957:
						case 6575:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 956:
						case 958:
						case 6576:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_S:
					crystalId = 1462;
					switch (scroll.getItemId())
					{
						case 959:
						case 961:
						case 6577:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 960:
						case 962:
						case 6578:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_R:
					crystalId = 1462;
					switch (scroll.getItemId())
					{
						case 959:
						case 961:
						case 6577:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 960:
						case 962:
						case 6578:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_S80:
					crystalId = 1462;
					switch (scroll.getItemId())
					{
						case 959:
						case 961:
						case 6577:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 960:
						case 962:
						case 6578:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
				case L2Item.CRYSTAL_S84:
					crystalId = 1462;
					switch (scroll.getItemId())
					{
						case 959:
						case 961:
						case 6577:
						case 9210:
							if (itemType2 == L2Item.TYPE2_WEAPON)
							{
								enchantItem = true;
							}
							break;
						case 960:
						case 962:
						case 6578:
						case 9211:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
							{
								enchantItem = true;
							}
							break;
					}
					break;
			}
			
			if (!enchantItem)
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantItem(null);
				return;
			}
			
			if (scroll.getItemId() >= 6569 && scroll.getItemId() <= 6578)
			{
				enchantBreak = false;
			}
			else
			{
				for (int crystalscroll : CRYSTAL_SCROLLS)
					if (scroll.getItemId() == crystalscroll)
					{
						enchantBreak = false;
						break;
					}
				
				for (int donatorscroll : DONATOR_SCROLLS)
					if (scroll.getItemId() == donatorscroll)
					{
						enchantBreak = false;
						break;
					}
			}
			
			int chance = 0;
			int maxEnchantLevel = 0;
			
			if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
			{
				for (int normalweaponscroll : NORMAL_WEAPON_SCROLLS)
					if (scroll.getItemId() == normalweaponscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.NORMAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(Config.NORMAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_NORMAL <= 0 ? 65535 : Config.ENCHANT_MAX_WEAPON_NORMAL;
					}
				for (int blessedweaponscroll : BLESSED_WEAPON_SCROLLS)
					if (scroll.getItemId() == blessedweaponscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.BLESS_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(Config.BLESS_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_BLESSED <= 0 ? 65535 : Config.ENCHANT_MAX_WEAPON_BLESSED;
					}
				for (int crystalweaponscroll : CRYSTAL_WEAPON_SCROLLS)
					if (scroll.getItemId() == crystalweaponscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.CRYTAL_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYTAL_WEAPON_ENCHANT_LEVEL.get(Config.CRYTAL_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYTAL_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_CRYSTAL <= 0 ? 65535 : Config.ENCHANT_MAX_WEAPON_CRYSTAL;
					}
				for (int scrollId : DONATOR_WEAPON_SCROLL)
				{
					if (scroll.getItemId() == scrollId)
					{
						if (item.getEnchantLevel() + 1 > Config.DONATOR_WEAPON_ENCHANT_LEVEL.size())
						{
							chance = Config.DONATOR_WEAPON_ENCHANT_LEVEL.get(Config.DONATOR_WEAPON_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.DONATOR_WEAPON_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_WEAPON_DONATOR <= 0 ? 65535 : Config.ENCHANT_MAX_WEAPON_DONATOR;
					}
				}
			}
			else if (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
			{
				for (int normalarmorscroll : NORMAL_ARMOR_SCROLLS)
					if (scroll.getItemId() == normalarmorscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.NORMAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(Config.NORMAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_NORMAL <= 0 ? 65535 : Config.ENCHANT_MAX_ARMOR_NORMAL;
					}
				for (int blessedarmorscroll : BLESSED_ARMOR_SCROLLS)
					if (scroll.getItemId() == blessedarmorscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.BLESS_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(Config.BLESS_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_BLESSED <= 0 ? 65535 : Config.ENCHANT_MAX_ARMOR_BLESSED;
					}
				for (int crystalarmorscroll : CRYSTAL_ARMOR_SCROLLS)
					if (scroll.getItemId() == crystalarmorscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_CRYSTAL <= 0 ? 65535 : Config.ENCHANT_MAX_ARMOR_CRYSTAL;
					}
				for (int scrollId : DONATOR_ARMOR_SCROLL)
				{
					if (scroll.getItemId() == scrollId)
					{
						if (item.getEnchantLevel() + 1 > Config.DONATOR_ARMOR_ENCHANT_LEVEL.size())
						{
							chance = Config.DONATOR_ARMOR_ENCHANT_LEVEL.get(Config.DONATOR_ARMOR_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.DONATOR_ARMOR_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_ARMOR_DONATOR <= 0 ? 65535 : Config.ENCHANT_MAX_ARMOR_DONATOR;
					}
				}
			}
			else if (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
			{
				for (int normaljewelscroll : NORMAL_ARMOR_SCROLLS)
					if (scroll.getItemId() == normaljewelscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.NORMAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_NORMAL <= 0 ? 65535 : Config.ENCHANT_MAX_JEWELRY_NORMAL;
					}
				for (int blessedjewelscroll : BLESSED_ARMOR_SCROLLS)
					if (scroll.getItemId() == blessedjewelscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.BLESS_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(Config.BLESS_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.BLESS_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_BLESSED <= 0 ? 65535 : Config.ENCHANT_MAX_JEWELRY_BLESSED;
					}
				for (int crystaljewelscroll : CRYSTAL_ARMOR_SCROLLS)
					if (scroll.getItemId() == crystaljewelscroll)
					{
						if (item.getEnchantLevel() + 1 > Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_CRYSTAL <= 0 ? 65535 : Config.ENCHANT_MAX_JEWELRY_CRYSTAL;
					}
				for (int scrollId : DONATOR_ARMOR_SCROLL)
				{
					if (scroll.getItemId() == scrollId)
					{
						if (item.getEnchantLevel() + 1 > Config.DONATOR_JEWELRY_ENCHANT_LEVEL.size())
						{
							chance = Config.DONATOR_JEWELRY_ENCHANT_LEVEL.get(Config.DONATOR_JEWELRY_ENCHANT_LEVEL.size());
						}
						else
						{
							chance = Config.DONATOR_JEWELRY_ENCHANT_LEVEL.get(item.getEnchantLevel() + 1);
						}
						maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY_DONATOR <= 0 ? 65535 : Config.ENCHANT_MAX_JEWELRY_DONATOR;
					}
				}
			}
			
			if ((maxEnchantLevel != 0 && item.getEnchantLevel() >= maxEnchantLevel))
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantItem(null);
				return;
			}
			
			scroll = activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item);
			if (scroll == null)
			{
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to sharpen the thing without a point in the hands", Config.DEFAULT_PUNISH);
				activeChar.setActiveEnchantItem(null);
				return;
			}
			activeChar.getInventory().updateInventory(scroll);
			
			if (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)
			{
				chance = 100;
			}
			else if (activeChar.getRace() == Race.Dwarf && Config.ENCHANT_DWARF_SYSTEM)
			{
				int _charlevel = activeChar.getLevel();
				int _itemlevel = item.getEnchantLevel();
				if (_charlevel >= 20 && _itemlevel <= Config.ENCHANT_DWARF_1_ENCHANTLEVEL)
				{
					chance = chance + Config.ENCHANT_DWARF_1_CHANCE;
				}
				else if (_charlevel >= 40 && _itemlevel <= Config.ENCHANT_DWARF_2_ENCHANTLEVEL)
				{
					chance = chance + Config.ENCHANT_DWARF_2_CHANCE;
				}
				else if (_charlevel >= 76 && _itemlevel <= Config.ENCHANT_DWARF_3_ENCHANTLEVEL)
				{
					chance = chance + Config.ENCHANT_DWARF_3_CHANCE;
				}
			}
			
			switch (item.getLocation())
			{
				case INVENTORY:
				case PAPERDOLL:
					switch (item.getLocation())
					{
						case VOID:
						case PET:
						case WAREHOUSE:
						case CLANWH:
						case LEASE:
						case FREIGHT:
						case NPC:
							chance = 0;
							activeChar.setActiveEnchantItem(null);
							Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
							return;
					}
					if (item.getOwnerId() != activeChar.getObjectId())
					{
						activeChar.setActiveEnchantItem(null);
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
						return;
					}
					break;
				default:
					chance = 0;
					activeChar.setActiveEnchantItem(null);
					Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
					return;
			}
			
			boolean failed = false;
			if (Rnd.get(100) < chance)
			{
				synchronized (item)
				{
					if (item.getOwnerId() != activeChar.getObjectId())
					{
						activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
						activeChar.setActiveEnchantItem(null);
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
						return;
					}
					if (item.getEnchantLevel() == 0)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId()));
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
					}
					item.setEnchantLevel(item.getEnchantLevel() + 1);
					item.setLastChange(L2ItemInstance.MODIFIED);
				}
			}
			else
			{
				
				failed = true;
				if (enchantBreak)
				{
					if (!Config.ENCHANTROLLBACK)
					{
						if (item.isEquipped())
						{
							if (item.getEnchantLevel() > 0)
							{
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
							}
							else
							{
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));
							}
							
							L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
							InventoryUpdate iu = new InventoryUpdate();
							for (L2ItemInstance element : unequiped)
							{
								iu.addItem(element);
							}
							activeChar.sendPacket(iu);
						}
						
						int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
						if (count < 1)
						{
							count = 1;
						}
						
						L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
						if (destroyItem == null)
						{
							if (item.getLocation() != null)
							{
								activeChar.getWarehouse().destroyItem("Enchant", item, activeChar, null);
							}
							
							activeChar.setActiveEnchantItem(null);
							activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
							return;
						}
						if (item.getEnchantLevel() > 0)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(item.getItemId()));
						}
						L2World.getInstance().removeObject(destroyItem);
						
						L2ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals).addNumber(count));
						activeChar.getInventory().updateInventory(crystals);
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(1, crystalId, count));
					}
					else
					{
						item.setEnchantLevel(item.getEnchantLevel() - Config.ENCHANTROLLBACK_VALUE);
						item.setLastChange(L2ItemInstance.MODIFIED);
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
						activeChar.sendMessage("Enchantment Failed: Your " + item.getItemName() + " back to " + item.getEnchantLevel() + ".");
					}
				}
				else
				{
					if (!Config.ENCHANTROLLBACK)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED));
						if (Config.ALT_FAILED_ENC_LEVEL)
						{
							item.setEnchantLevel(Config.ENCHANT_SAFE_MAX);
						}
						else
						{
							item.setEnchantLevel(0);
						}
						item.setLastChange(L2ItemInstance.MODIFIED);
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
					}
					else
					{
						item.setEnchantLevel(item.getEnchantLevel() - Config.ENCHANTROLLBACK_VALUE);
						item.setLastChange(L2ItemInstance.MODIFIED);
						activeChar.sendMessage("Enchantment Failed: Your " + item.getItemName() + " back to " + item.getEnchantLevel() + ".");
						activeChar.sendPacket(new ExPutEnchantTargetItemResult(2, 0, 0));
					}
				}
			}
			if (!failed)
			{
				activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			}
			activeChar.getInventory().updateInventory(item);
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.broadcastUserInfo();
			activeChar.setActiveEnchantItem(null);
		}
		finally
		{
			activeChar.getInventory().updateDatabase();
		}
	}
	
}