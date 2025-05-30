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
package com.dream.game.model.multisell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.MultiSellList;
import com.dream.game.templates.item.L2Armor;
import com.dream.game.templates.item.L2Item;
import com.dream.game.templates.item.L2Weapon;
import com.dream.game.util.Util;

public class L2Multisell
{
	private final static Logger _log = Logger.getLogger(L2Multisell.class.getName());

	private static L2Multisell _instance;

	private static ListContainer generateMultiSell(int listId, boolean inventoryOnly, L2PcInstance player, double taxRate)
	{
		ListContainer listTemplate = L2Multisell.getInstance().getList(listId);
		ListContainer list = new ListContainer();
		if (listTemplate == null)
			return list;

		list = new ListContainer();
		list.setListId(listId);

		if (inventoryOnly)
		{
			if (player == null)
				return list;

			L2ItemInstance[] items;

			if (listTemplate.getMaintainEnchantment())
			{
				items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
			}
			else
			{
				items = player.getInventory().getUniqueItems(false, false, false);
			}

			int enchantLevel;
			for (final L2ItemInstance item : items)
				// only do the matchup on equipable items that are not currently equipped
				// so for each appropriate item, produce a set of entries for the multisell list.
				if (!item.isWear() && (item.getItem() instanceof L2Armor || item.getItem() instanceof L2Weapon))
				{
					enchantLevel = listTemplate.getMaintainEnchantment() ? item.getEnchantLevel() : 0;
					// loop through the entries to see which ones we wish to include
					for (final Entry ent : listTemplate.getEntries())
					{
						boolean doInclude = false;

						// check ingredients of this entry to see if it's an entry we'd like to include.
						for (final Ingredient ing : ent.getIngredients())
							if (item.getItemId() == ing.getItemId())
							{
								doInclude = true;
								break;
							}

						// manipulate the ingredients of the template entry for this particular instance shown
						// i.e: Assign enchant levels and/or apply taxes as needed.
						if (doInclude)
						{
							list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.getMaintainEnchantment(), enchantLevel, taxRate));
						}
					}
				}

			items = null;
		} // end if "inventory-only"
		else
		{
			for (Entry ent : listTemplate.getEntries())
			{
				list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0, taxRate));
			}
		}

		return list;
	}

	public static L2Multisell getInstance()
	{
		if (_instance == null)
		{
			_instance = new L2Multisell();
		}
		return _instance;
	}

	private static Entry prepareEntry(Entry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel, double taxRate)
	{
		final Entry newEntry = new Entry();
		newEntry.setEntryId(templateEntry.getEntryId() * 100000 + enchantLevel);
		int adenaAmount = 0;

		for (Ingredient ing : templateEntry.getIngredients())
		{
			Ingredient newIngredient = new Ingredient(ing);

			if (ing.getItemId() == 57 && ing.isTaxIngredient())
			{
				if (applyTaxes)
				{
					adenaAmount += (int) Math.round(ing.getItemCount() * taxRate);
				}
				continue;
			}
			else if (ing.getItemId() == 57)
			{
				adenaAmount += ing.getItemCount();
				continue;
			}
			else if (newIngredient.getItemId() > 0)
			{
				if (ItemTable.getInstance().getTemplate(ing.getItemId()) == null)
				{
					_log.warn("Item " + ing.getItemId() + " not exists");
					continue;

				}
				L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
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

			newEntry.addIngredient(newIngredient);
		}

		if (adenaAmount > 0)
		{
			newEntry.addIngredient(new Ingredient(57, adenaAmount, 0, false, false));
		}
		for (Ingredient ing : templateEntry.getProducts())
		{
			if (ing == null)
			{
				continue;
			}
			Ingredient newIngredient = new Ingredient(ing);

			if (ItemTable.getInstance().getTemplate(ing.getItemId()) == null)
			{
				_log.warn("Item " + ing.getItemId() + " not exists");
				continue;

			}
			L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
			if (tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				if (maintainEnchantment)
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
				else
				{
					newIngredient.setEnchantmentLevel(ing.getEnchantmentLevel());
				}
			newEntry.addProduct(newIngredient);
		}
		return newEntry;
	}

	private final List<ListContainer> _entries = new ArrayList<>();

	public L2Multisell()
	{
		parseData();
	}

	public ListContainer getList(int id)
	{
		synchronized (_entries)
		{
			for (ListContainer list : _entries)
				if (list.getListId() == id)
					return list;
		}

		_log.warn("[L2Multisell] can't find list with id: " + id);
		return null;
	}

	private void parse()
	{
		Document doc = null;
		int id = 0;

		for (File f : Util.getDatapackFiles("xml/multisell", ".xml"))
		{
			id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
			try
			{

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("Error loading file " + f.getAbsolutePath(), e);
			}
			try
			{
				ListContainer list = parseDocument(doc);
				list.setListId(id);
				_entries.add(list);
			}
			catch (Exception e)
			{
				_log.fatal("Error in file " + f.getAbsolutePath(), e);
			}
		}
	}

	private void parseData()
	{
		_entries.clear();
		parse();
	}

	protected ListContainer parseDocument(Document doc)
	{
		ListContainer list = new ListContainer();

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;
				attribute = n.getAttributes().getNamedItem("applyTaxes");
				if (attribute == null)
				{
					list.setApplyTaxes(false);
				}
				else
				{
					list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				attribute = n.getAttributes().getNamedItem("maintainEnchantment");
				if (attribute == null)
				{
					list.setMaintainEnchantment(false);
				}
				else
				{
					list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
				}
				attribute = n.getAttributes().getNamedItem("maintainEnchantmentMinLvl");
				if (attribute != null)
				{
					list.setMaintainEnchantmentLvl(Integer.parseInt(attribute.getNodeValue()));
				}

				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						Entry e = parseEntry(d);
						list.addEntry(e);
					}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				Entry e = parseEntry(n);
				list.addEntry(e);
			}

		return list;
	}

	protected Entry parseEntry(Node n)
	{
		int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());

		Node first = n.getFirstChild();
		Entry entry = new Entry();

		for (n = first; n != null; n = n.getNextSibling())
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;

				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0;

				boolean isTaxIngredient = false, mantainIngredient = false;

				attribute = n.getAttributes().getNamedItem("isTaxIngredient");

				if (attribute != null)
				{
					isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}

				attribute = n.getAttributes().getNamedItem("mantainIngredient");

				if (attribute != null)
				{
					mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
				}

				attribute = n.getAttributes().getNamedItem("enchant");

				if (attribute != null)
				{
					enchant = Integer.parseInt(attribute.getNodeValue());
				}

				Ingredient e = new Ingredient(id, count, enchant, isTaxIngredient, mantainIngredient);
				entry.addIngredient(e);
			}
			else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				Node attribute;
				int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				int enchant = 0;
				attribute = n.getAttributes().getNamedItem("enchant");

				if (attribute != null)
				{
					enchant = Integer.parseInt(attribute.getNodeValue());
				}

				Ingredient e = new Ingredient(id, count, enchant, false, false);
				entry.addProduct(e);
			}

		entry.setEntryId(entryId);

		return entry;
	}

	public void reload()
	{
		parseData();
	}

	public void SeparateAndSend(final int listId, final L2PcInstance player, final boolean inventoryOnly, final double taxRate)
	{
		ListContainer list = generateMultiSell(listId, inventoryOnly, player, taxRate);
		ListContainer temp = new ListContainer();

		int page = 1;

		temp.setListId(list.getListId());

		for (final Entry e : list.getEntries())
		{
			if (temp.getEntries().size() == 40)
			{
				player.sendPacket(new MultiSellList(temp, page, 0));
				page++;
				temp = new ListContainer();
				temp.setListId(list.getListId());
			}

			temp.addEntry(e);
		}

		player.setMultiSellId(listId);

		player.sendPacket(new MultiSellList(temp, page, 1));

		list = null;
		temp = null;
	}
}