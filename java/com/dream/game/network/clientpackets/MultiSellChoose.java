package com.dream.game.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.dream.Config;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.L2Augmentation;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.itemcontainer.PcInventory;
import com.dream.game.model.multisell.Entry;
import com.dream.game.model.multisell.Ingredient;
import com.dream.game.model.multisell.L2Multisell;
import com.dream.game.model.multisell.ListContainer;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ExPCCafePointInfo;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.PledgeShowInfoUpdate;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;

public class MultiSellChoose extends L2GameClientPacket
{
	private int _listId, _entryId, _amount, _enchantment, _transactionTax;
	private L2Npc merchant;
	private L2ItemInstance product;
	private ListContainer list;

	private void doExchange(L2PcInstance player, Entry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int maintainEnchantmentLvl, int enchantment, boolean GMShop)
	{
		PcInventory inv = player.getInventory();
		merchant = player.getTarget() instanceof L2Npc ? (L2Npc) player.getTarget() : null;
		if (merchant == null && !GMShop)
			return;

		if (!GMShop && !merchant.isInsideRadius(player, 250, false, false))
			return;

		Entry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);
		int cnt = 0;
		for (Ingredient e : entry.getProducts())
			if (!ItemTable.getInstance().getTemplate(e.getItemId()).isStackable())
			{
				cnt += e.getItemCount() * _amount;
			}
		if (player.getInventory().getSize() + cnt >= player.getInventoryLimit())
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL.getSystemMessage());
			return;
		}

		List<Ingredient> _ingredientsList = new ArrayList<>();
		boolean newIng = true;
		for (Ingredient e : entry.getIngredients())
		{
			newIng = true;

			for (Ingredient ex : _ingredientsList)
				if (ex.getItemId() == e.getItemId() && ex.getEnchantmentLevel() == e.getEnchantmentLevel())
				{
					if ((long) ex.getItemCount() + e.getItemCount() >= Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						_ingredientsList.clear();
						_ingredientsList = null;
						return;
					}
					ex.setItemCount(ex.getItemCount() + e.getItemCount());
					newIng = false;
				}
			if (newIng)
			{
				_ingredientsList.add(new Ingredient(e));
			}
		}
		for (Ingredient e : _ingredientsList)
		{

			if ((double) e.getItemCount() * _amount >= Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				_ingredientsList.clear();
				_ingredientsList = null;
				return;
			}
			switch (e.getItemId())
			{
				case -200:
				{
					if (player.getClan() == null)
					{
						player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
						return;
					}
					if (!player.isClanLeader())
					{
						player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
						return;
					}
					if (player.getClan().getReputationScore() < e.getItemCount() * _amount)
					{
						player.sendPacket(SystemMessageId.CLAN_REPUTATION_SCORE_IS_TOO_LOW);
						return;
					}
					break;
				}
				case -100:
					if (player.getPcCaffePoints() < e.getItemCount() * _amount)
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						return;
					}
					break;
				default:
				{
					int enchantLvl = -1;
					if (maintainEnchantment)
					{
						enchantLvl = e.getEnchantmentLevel();
					}
					else if (e.getEnchantmentLevel() > 0)
					{
						enchantLvl = e.getEnchantmentLevel();
					}

					if (inv.getInventoryItemCount(e.getItemId(), enchantLvl) < (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient() ? e.getItemCount() * _amount : e.getItemCount()))
					{
						player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
						_ingredientsList.clear();
						_ingredientsList = null;
						return;
					}
					if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
					{
						_enchantment = 0;
					}
					break;
				}
			}
		}

		_ingredientsList.clear();
		_ingredientsList = null;
		List<L2Augmentation> augmentation = new ArrayList<>();

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUCCESSFULLY_TRADED_WITH_NPC));

		for (Ingredient e : entry.getIngredients())
		{
			switch (e.getItemId())
			{
				case -200:
				{
					int repCost = player.getClan().getReputationScore() - e.getItemCount() * _amount;
					player.getClan().setReputationScore(repCost, true);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(e.getItemCount() * _amount));
					player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
					break;
				}
				case -100:
					player.setPcCaffePoints(player.getPcCaffePoints() - e.getItemCount() * _amount);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT).addNumber(e.getItemCount() * _amount));
					player.sendPacket(new ExPCCafePointInfo(player, -(e.getItemCount() * _amount), true, 24, true));
					break;
				default:
				{
					L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());
					if (itemToTake == null)
						return;
					if (itemToTake.isWear())
						return;

					if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient())
						if (itemToTake.isStackable())
						{
							if (!player.destroyItem("Multisell", itemToTake.getObjectId(), e.getItemCount() * _amount, player.getTarget(), true))
								return;
						}
						else if (maintainEnchantment || e.getEnchantmentLevel() > 0)
						{
							L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
							for (int i = 0; i < e.getItemCount() * _amount; i++)
							{
								if (inventoryContents[i].isAugmented())
								{
									augmentation.add(inventoryContents[i].getAugmentation());
								}
								if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
									return;
							}
						}
						else
						{
							for (int i = 1; i <= e.getItemCount() * _amount; i++)
							{
								L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());

								itemToTake = inventoryContents[0];
								if (itemToTake.getEnchantLevel() > 0)
								{
									for (L2ItemInstance item : inventoryContents)
										if (item.getEnchantLevel() < itemToTake.getEnchantLevel())
										{
											itemToTake = item;
											if (itemToTake.getEnchantLevel() == 0)
											{
												break;
											}
										}
								}
								if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
									return;
							}
						}
					break;
				}
			}
		}

		for (Ingredient e : entry.getProducts())
		{
			if (ItemTable.getInstance().getTemplate(e.getItemId()).isStackable())
			{
				inv.addItem("Multisell", e.getItemId(), e.getItemCount() * _amount, player, player.getTarget());
			}
			else
			{
				product = null;
				for (int i = 0; i < e.getItemCount() * _amount; i++)
				{
					product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
					if (maintainEnchantment)
						if (i < augmentation.size())
						{
							product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill()));
						}
					product.setEnchantLevel(e.getEnchantmentLevel());
				}
			}

			if (e.getItemCount() * _amount > 1)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.getItemId()).addNumber(e.getItemCount() * _amount));
			}
			else if (maintainEnchantment && e.getEnchantmentLevel() > 0)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(e.getEnchantmentLevel()).addItemName(e.getItemId()));
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(e.getItemId()));
			}
		}
		player.sendPacket(new ItemList(player, false));
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		su = null;

		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		{
			merchant.getCastle().addToTreasury(_transactionTax * _amount);
		}
	}

	private Entry prepareEntry(L2Npc merchant, Entry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
	{
		final Entry newEntry = new Entry();
		if (templateEntry == null)
			return newEntry;
		newEntry.setEntryId(templateEntry.getEntryId());
		int totalAdenaCount = 0;
		boolean hasIngredient = false;

		for (Ingredient ing : templateEntry.getIngredients())
		{
			if (ing == null)
			{
				continue;
			}

			final Ingredient newIngredient = new Ingredient(ing);

			if (newIngredient.getItemId() == 57 && newIngredient.isTaxIngredient())
			{
				double taxRate = 0.;
				if (applyTaxes)
					if (merchant != null && merchant.getIsInTown())
					{
						taxRate = merchant.getCastle().getTaxRate();
					}

				_transactionTax = (int) Math.round(newIngredient.getItemCount() * taxRate);
				totalAdenaCount += _transactionTax;
				continue;
			}
			else if (ing.getItemId() == 57)
			{
				totalAdenaCount += newIngredient.getItemCount();
				continue;
			}
			else if (newIngredient.getItemId() > 0)
			{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if (tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				{
					if (maintainEnchantment)
					{
						newIngredient.setEnchantmentLevel(enchantLevel);
					}
					else
					{
						newIngredient.setEnchantmentLevel(ing.getEnchantmentLevel());
					}
					hasIngredient = true;
				}
			}
			newEntry.addIngredient(newIngredient);
		}
		if (totalAdenaCount > 0)
		{
			newEntry.addIngredient(new Ingredient(57, totalAdenaCount, false, false));
		}

		for (Ingredient ing : templateEntry.getProducts())
		{
			if (ing == null)
			{
				continue;
			}

			final Ingredient newIngredient = new Ingredient(ing);

			if (hasIngredient)
			{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if (tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
					if (maintainEnchantment)
					{
						newIngredient.setEnchantmentLevel(enchantLevel);
					}
					else
					{
						newIngredient.setEnchantmentLevel(ing.getEnchantmentLevel());
					}
			}
			newEntry.addProduct(newIngredient);
		}
		return newEntry;
	}

	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readD();
		_enchantment = _entryId % 100000;
		_entryId = _entryId / 100000;
		_transactionTax = 0;
	}

	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_amount < 1 || _amount > 5000)
		{
			player.setMultiSellId(-1);
			return;
		}

		list = L2Multisell.getInstance().getList(_listId);
		if (list == null)
			return;

		final L2Npc merchant = player.getTarget() instanceof L2Npc ? (L2Npc) player.getTarget() : null;

		// Possible fix to Multisell Radius
		if (merchant == null || !player.isInsideRadius(merchant, L2Npc.INTERACTION_DISTANCE, false, false))
		{
			player.setMultiSellId(-1);
			return;
		}

		final int selectedList = player.getMultiSellId();
		if (list == null || list.getListId() != _listId || selectedList != _listId)
		{
			player.setMultiSellId(-1);
			return;
		}

		if (player.isCastingNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setMultiSellId(-1);
			return;
		}

		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}

		for (Entry entry : list.getEntries())
			if (entry.getEntryId() == _entryId)
			{
				doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), list.getMaintainEnchantmentLvl(), _enchantment, _listId == player._bbsMultisell);
				return;
			}
	}

}