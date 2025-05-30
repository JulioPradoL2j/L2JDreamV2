package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.GameTimeController;
import com.dream.game.ai.CtrlEvent;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.NextAction;
import com.dream.game.ai.NextAction.NextActionCallback;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.handler.IItemHandler;
import com.dream.game.handler.ItemHandler;
import com.dream.game.manager.FortSiegeManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.entity.events.arenaduel.ArenaDuel;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.ShowCalculator;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2ArmorType;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.templates.item.L2WeaponType;
import com.dream.game.util.FloodProtector;
import com.dream.game.util.FloodProtector.Protected;
import com.dream.game.util.IllegalPlayerAction;
import com.dream.game.util.Util;

public final class UseItem extends L2GameClientPacket
{
	public class WeaponEquipTask implements Runnable
	{
		L2ItemInstance item;
		L2PcInstance activeChar;

		public WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}

		@Override
		public void run()
		{
			if (activeChar.isAttackingNow())
				return;
			activeChar.useEquippableItem(item, false);
		}
	}

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{

		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.isOutOfControl())
			return;

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(SystemMessageId.NOT_USE_ITEMS_IN_PRIVATE_STORE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getSecondRefusal())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			activeChar.cancelActiveTrade();
		}

		activeChar._inWorld = true;
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		if (activeChar._lastUseItem == item.getItemId())
			if (!FloodProtector.tryPerformAction(activeChar, Protected.USEITEM))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

		if ((activeChar._event != null) && activeChar._event.isRunning() && !activeChar._event.canUseItem(activeChar, item))
			return;

		activeChar._lastUseItem = item.getItemId();

		if (item.isWear())
			return;

		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}

		int itemId = item.getItemId();
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0 && (itemId == 736 || itemId == 1538 || itemId == 1829 || itemId == 1830 || itemId == 3958 || itemId == 5858 || itemId == 5859 || itemId == 6663 || itemId == 6664 || itemId >= 7117 && itemId <= 7135 || itemId >= 7554 && itemId <= 7559 || itemId == 7618 || itemId == 7619))
			return;

		if (itemId == 57)
			return;
		
		if (activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if (activeChar.isDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
			return;
		}

		// Tournament
		if (!Config.ARENA_DUEL_ALLOW_S && item.getItem().getItemGrade() == L2Item.CRYSTAL_S && (activeChar.isInArenaEvent() || ArenaDuel.getInstance().isRegistered(activeChar)))
		{
			activeChar.sendMessage("Tournament Manager: Items Grade S cannot be used in Tournament");
			return;
		}
		if (activeChar.isInArenaEvent() || activeChar.isArenaProtection())
		{
			if (Config.ARENA_DUEL_LIST_ITEMS_RESTRICT.contains(Integer.valueOf(item.getItemId())))
			{
				activeChar.sendMessage("You can not use this item during Tournament.");
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (Config.CHECK_ENCHANT_LEVEL_EQUIP)
			if (!activeChar.isGM())
			{
				if (Config.ENCHANT_OVER_CHANT_CHECK > 0 && item.getItem().getType2() == L2Item.TYPE2_WEAPON && item.getEnchantLevel() > Config.ENCHANT_OVER_CHANT_CHECK)
				{
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to put overchanted item!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}
				if (Config.ENCHANT_OVER_CHANT_CHECK > 0 && item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR && item.getEnchantLevel() > Config.ENCHANT_OVER_CHANT_CHECK)
				{
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to put overchanted item!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}
				if (Config.ENCHANT_OVER_CHANT_CHECK > 0 && item.getItem().getType2() == L2Item.TYPE2_ACCESSORY && item.getEnchantLevel() > Config.ENCHANT_OVER_CHANT_CHECK)
				{
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to put overchanted item!", IllegalPlayerAction.PUNISH_KICK);
					return;
				}
			}

		if (item.getItem() instanceof L2Armor && item.getItem().getItemType() == L2ArmorType.PET || item.getItem() instanceof L2Weapon && item.getItem().getItemType() == L2WeaponType.PET)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
			return;
		}

		if (!item.isEquipped())
			if (!item.getItem().checkCondition(activeChar, activeChar, true))
				return;

		if (!Config.ALLOW_HEAVY_USE_LIGHT)
			if (Config.NOTALLOWEDUSELIGHT.contains(activeChar.getClassId().getId()))
			{
				if (item.getItemType() == L2ArmorType.LIGHT)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					return;
				}
			}

		if (!Config.ALLOW_LIGHT_USE_HEAVY)
			if (Config.NOTALLOWEDUSEHEAVY.contains(activeChar.getClassId().getId()))
			{
				if (item.getItemType() == L2ArmorType.HEAVY)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					return;
				}
			}

		if (Config.ALT_DISABLE_BOW_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.BOW)
			{
				if (Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_DAGGER_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.DAGGER)
			{
				if (Config.DISABLE_DAGGER_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_SWORD_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.SWORD)
			{
				if (Config.DISABLE_SWORD_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_BLUNT_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.BLUNT)
			{
				if (Config.DISABLE_BLUNT_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_DUAL_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.DUAL)
			{
				if (Config.DISABLE_DUAL_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_POLE_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.POLE)
			{
				if (Config.DISABLE_POLE_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (Config.ALT_DISABLE_BIGSWORD_CLASSES)
		{
			if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.BIGSWORD)
			{
				if (Config.DISABLE_BIGSWORD_CLASSES.contains(activeChar.getClassId().getId()))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}

		if (activeChar.isInOlympiadMode())
			if (item.isHeroItem() || item.isOlyRestrictedItem())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

		if (item.isEquipable())
		{
			if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(itemId));
				return;
			}

			if (!activeChar.isGM() && !activeChar.isHero() && item.isHeroItem())
				return;

			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				{
					if (activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemId() == Config.FORTSIEGE_COMBAT_FLAG_ID)
					{
						activeChar.sendPacket(SystemMessageId.NO_CONDITION_TO_EQUIP);
						return;
					}

					if (activeChar.isMounted() || activeChar.isDisarmed())
					{
						activeChar.sendPacket(SystemMessageId.NO_CONDITION_TO_EQUIP);
						return;
					}

					if (activeChar.isCursedWeaponEquipped())
						return;

					break;
				}
				case L2Item.SLOT_CHEST:
				case L2Item.SLOT_BACK:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_HEAD:
				case L2Item.SLOT_FULL_ARMOR:
				case L2Item.SLOT_LEGS:
				{
					break;
				}
			}

			if (activeChar.isCursedWeaponEquipped() && itemId == 6408)
				return;

			if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			{
				final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.CAST, new NextActionCallback()
				{
					@Override
					public void doWork()
					{
						activeChar.useEquippableItem(item, true);
					}
				});

				activeChar.getAI().setNextAction(nextAction);
			}
			else if (FortSiegeManager.getInstance().isCombat(item.getItemId()))
				return;
			else if (activeChar.isAttackingNow() && Config.BLOCK_CHANGE_WEAPON_ON_ATTACK)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar), (activeChar.getAttackEndTime() - GameTimeController.getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
			}
			else
			{
				activeChar.useEquippableItem(item, true);
			}

		}
		else
		{
			L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			int itemid = item.getItemId();
			if (itemid == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD && (itemid >= 6519 && itemid <= 6527 || itemid >= 7610 && itemid <= 7613 || itemid >= 7807 && itemid <= 7809 || itemid >= 8484 && itemid <= 8486 || itemid >= 8505 && itemid <= 8513 || itemid == 8548))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			else
			{

				IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
				if (handler == null)
				{
					if (item.getEtcItem() != null)
					{
						for (String skillInfo : item.getEtcItem().getSkills())
						{
							String sk[] = skillInfo.split("-");
							L2Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(sk[0]), Integer.parseInt(sk[1]));
							if (skill != null)
								if (skill.checkCondition(activeChar, activeChar))
								{
									activeChar.useMagic(skill, true, true);
								}
								else if (skill.checkCondition(activeChar, activeChar.getTarget()))
								{
									activeChar.useMagic(skill, true, true);
								}
						}
					}
				}
				else
				{
					handler.useItem(activeChar, item);
				}
			}
		}
	}

}