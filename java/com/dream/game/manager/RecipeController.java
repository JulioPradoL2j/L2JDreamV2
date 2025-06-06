package com.dream.game.manager;

import com.dream.Config;
import com.dream.game.GameTimeController;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.model.L2ManufactureItem;
import com.dream.game.model.L2RecipeList;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RecipeInstance;
import com.dream.game.model.actor.instance.L2RecipeStatInstance;
import com.dream.game.model.itemcontainer.Inventory;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.MagicSkillUse;
import com.dream.game.network.serverpackets.RecipeBookItemList;
import com.dream.game.network.serverpackets.RecipeItemMakeInfo;
import com.dream.game.network.serverpackets.RecipeShopItemInfo;
import com.dream.game.network.serverpackets.SetupGauge;
import com.dream.game.network.serverpackets.StatusUpdate;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.templates.item.L2Item;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;
import com.dream.util.StatsSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class RecipeController
{
	private class RecipeItemMaker implements Runnable
	{
		private class TempItem
		{
			private final int _itemId;
			private int _quantity;
			@SuppressWarnings("unused")
			private final int _ownerId;
			private final int _referencePrice;
			private final String _itemName;
			
			public TempItem(L2ItemInstance item, int quantity)
			{
				super();
				_itemId = item.getItemId();
				_quantity = quantity;
				_ownerId = item.getOwnerId();
				_itemName = item.getItem().getName();
				_referencePrice = item.getReferencePrice();
			}
			
			public int getItemId()
			{
				return _itemId;
			}
			
			public String getItemName()
			{
				return _itemName;
			}
			
			public int getQuantity()
			{
				return _quantity;
			}
			
			public int getReferencePrice()
			{
				return _referencePrice;
			}
			
			public void setQuantity(int quantity)
			{
				_quantity = quantity;
			}
		}
		
		protected boolean _isValid;
		protected List<TempItem> _items = null;
		protected final L2RecipeList _recipeList;
		protected final L2PcInstance _player;
		protected final L2PcInstance _target;
		protected final L2Skill _skill;
		protected final int _skillId;
		protected final int _skillLevel;
		protected int _creationPasses = 1;
		protected int _itemGrab;
		protected int _exp = -1;
		protected int _sp = -1;
		protected int _price;
		protected int _totalItems;
		@SuppressWarnings("unused")
		protected int _materialsRefPrice;
		
		protected int _delay;
		
		public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
		{
			_player = pPlayer;
			_target = pTarget;
			_recipeList = pRecipeList;
			
			_isValid = false;
			_skillId = _recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
			_skillLevel = _player.getSkillLevel(_skillId);
			_skill = _player.getKnownSkill(_skillId);
			
			_player.isInCraftMode(true);
			
			if (_player.isAlikeDead())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isAlikeDead())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isProcessingTransaction())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player.isProcessingTransaction())
			{
				if (_player != _target)
				{
					_target.sendMessage("Manufacturer " + _player.getName() + " is busy.");
				}
				
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_recipeList.getRecipes().length == 0)
			{
				_player.sendMessage("No such recipe");
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_recipeList.getLevel() > _skillLevel)
			{
				_player.sendMessage("Need skill level " + _recipeList.getLevel());
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player != _target)
			{
				for (L2ManufactureItem temp : _player.getCreateList().getList())
					if (temp.getRecipeId() == _recipeList.getId())
					{
						_price = temp.getCost();
						if (_target.getAdena() < _price)
						{
							_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
							abort();
							return;
						}
						break;
					}
			}
			
			if ((_items = listItems(false)) == null)
			{
				abort();
				return;
			}
			
			for (TempItem i : _items)
			{
				_materialsRefPrice += i.getReferencePrice() * i.getQuantity();
				_totalItems += i.getQuantity();
			}
			
			if (!calculateStatUse(false, false))
			{
				abort();
				return;
			}
			
			if (_player.isNormalCraftMode())
			{
				calculateAltStatChange();
			}
			
			updateMakeInfo(true);
			updateCurMp();
			updateCurLoad();
			
			_player.isInCraftMode(false);
			_isValid = true;
		}
		
		private void abort()
		{
			updateMakeInfo(false);
			_player.isInCraftMode(false);
			_activeMakers.remove(_player);
		}
		
		private void calculateAltStatChange()
		{
			_itemGrab = _skillLevel;
			
			for (L2RecipeStatInstance altStatChange : _recipeList.getAltStatChange())
				if (altStatChange.getType() == L2RecipeStatInstance.statType.XP)
				{
					_exp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.statType.SP)
				{
					_sp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.statType.GIM)
				{
					_itemGrab *= altStatChange.getValue();
				}
			_creationPasses = _totalItems / _itemGrab + (_totalItems % _itemGrab != 0 ? 1 : 0);
			if (_creationPasses < 1)
			{
				_creationPasses = 1;
			}
		}
		
		private boolean calculateStatUse(boolean isWait, boolean isReduce)
		{
			boolean ret = true;
			for (L2RecipeStatInstance statUse : _recipeList.getStatUse())
			{
				double modifiedValue = statUse.getValue() / _creationPasses;
				if (statUse.getType() == L2RecipeStatInstance.statType.HP)
				{
					if (_player.getStatus().getCurrentHp() <= modifiedValue)
					{
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						{
							_target.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentHp(modifiedValue, _player, true, false, null);
					}
				}
				else if (statUse.getType() == L2RecipeStatInstance.statType.MP)
				{
					if (_player.getStatus().getCurrentMp() < modifiedValue)
					{
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						{
							_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentMp(modifiedValue);
					}
				}
				else
				{
					_target.sendMessage("Prescription error, notify the administrator.");
					ret = false;
					abort();
				}
			}
			return ret;
		}
		
		public void finishCrafting()
		{
			if (!Config.ALT_GAME_CREATION)
			{
				calculateStatUse(false, true);
			}
			
			if (_target != _player && _price > 0)
			{
				L2ItemInstance adenatransfer = _target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);
				
				if (adenatransfer == null)
				{
					_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
					abort();
					return;
				}
			}
			
			boolean success = false;
			if ((_items = listItems(true)) == null)
			{
				
			}
			else if (_recipeList.getSuccessRate() == 100 || Rnd.get(100) < _recipeList.getSuccessRate())
			{
				rewardPlayer(_price);
				success = true;
			}
			else if (_target != _player)
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED).addString(_target.getName()).addItemName(_recipeList.getItemId()).addNumber(_price));
				_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA).addString(_player.getName()).addItemName(_recipeList.getItemId()).addNumber(_price));
			}
			else
			{
				_target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
			}
			
			updateCurMp();
			updateCurLoad();
			_activeMakers.remove(_player);
			_player.isInCraftMode(false);
			_target.sendPacket(new ItemList(_target, false));
			updateMakeInfo(success);
		}
		
		private void grabSomeItems()
		{
			int grabItems = _itemGrab;
			while (grabItems > 0 && !_items.isEmpty())
			{
				TempItem item = _items.get(0);
				
				int count = item.getQuantity();
				if (count >= grabItems)
				{
					count = grabItems;
				}
				
				item.setQuantity(item.getQuantity() - count);
				if (item.getQuantity() <= 0)
				{
					_items.remove(0);
				}
				else
				{
					_items.set(0, item);
				}
				
				grabItems -= count;
				
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(count).addItemName(item.getItemId()));
				if (_target != _player)
				{
					_target.sendMessage("Manufacturer " + _player.getName() + " used " + count + " " + item.getItemName());
				}
			}
		}
		
		private List<TempItem> listItems(boolean remove)
		{
			L2RecipeInstance[] recipes = _recipeList.getRecipes();
			Inventory inv = _target.getInventory();
			List<TempItem> materials = new ArrayList<>();
			
			for (L2RecipeInstance recipe : recipes)
			{
				int quantity = _recipeList.isConsumable() ? (int) (recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : recipe.getQuantity();
				
				if (quantity > 0)
				{
					L2ItemInstance item = inv.getItemByItemId(recipe.getItemId());
					int itemQuantityAmount = item == null ? 0 : item.getCount();
					
					if (itemQuantityAmount < quantity)
					{
						_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE).addItemName(recipe.getItemId()).addNumber(quantity - itemQuantityAmount));
						
						abort();
						return null;
					}
					
					TempItem temp = new TempItem(item, quantity);
					materials.add(temp);
				}
			}
			
			if (remove)
			{
				for (TempItem tmp : materials)
				{
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(tmp.getItemId()).addNumber(tmp.getQuantity()));
				}
			}
			return materials;
		}
		
		private void rewardPlayer(int price)
		{
			int itemId = _recipeList.getItemId();
			int itemCount = _recipeList.getCount();
			
			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			
			_target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);
			if (_target != _player)
				if (itemCount == 1)
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_S1_FOR_S3_ADENA).addString(_target.getName()).addItemName(itemId).addNumber(price));
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_FOR_S3_ADENA).addString(_player.getName()).addItemName(itemId).addNumber(price));
				}
				else
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA).addString(_target.getName()).addNumber(itemCount).addItemName(itemId).addNumber(price));
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_S3_S_FOR_S4_ADENA).addString(_player.getName()).addNumber(itemCount).addItemName(itemId).addNumber(price));
				}
			
			if (itemCount > 1)
			{
				_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount));
			}
			else
			{
				_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1).addItemName(itemId));
			}
			
			if (Config.ALT_GAME_CREATION)
			{
				int recipeLevel = _recipeList.getLevel();
				if (_exp < 0)
				{
					_exp = template.getReferencePrice() * itemCount;
					_exp /= recipeLevel;
				}
				if (_sp < 0)
				{
					_sp = _exp / 10;
				}
				
				if (_exp < 0)
				{
					_exp = 0;
				}
				if (_sp < 0)
				{
					_sp = 0;
				}
				
				for (int i = _skillLevel; i > recipeLevel; i--)
				{
					_exp /= 4;
					_sp /= 4;
				}
				
				_player.addExpAndSp((int) _player.calcStat(Stats.EXPSP_RATE, _exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null), (int) _player.calcStat(Stats.EXPSP_RATE, _sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
			}
			updateMakeInfo(true);
		}
		
		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				_target.sendMessage("Creating things.");
				abort();
				return;
			}
			
			if (_player == null || _target == null)
			{
				_log.warn("player or target == null (disconnected?), aborting" + _target + _player);
				abort();
				return;
			}
			
			if (_player.isOnline() == 0 || _target.isOnline() == 0)
			{
				_log.warn("player or target is not online, aborting " + _target + _player);
				abort();
				return;
			}
			
			if (_player.isNormalCraftMode() && _activeMakers.get(_player) == null)
			{
				if (_target != _player)
				{
					_target.sendMessage("Manufacture terminated.");
					_player.sendMessage("Manufacture terminated.");
				}
				else
				{
					_player.sendMessage("Creating things.");
				}
				
				abort();
				return;
			}
			
			if (_player.isNormalCraftMode() && !_items.isEmpty())
			{
				if (!calculateStatUse(true, true))
					return;
				updateCurMp();
				
				grabSomeItems();
				
				if (!_items.isEmpty())
				{
					_delay = (int) (Config.ALT_GAME_CREATION_SPEED * _player.getStat().getMReuseRate(_skill) * GameTimeController.TICKS_PER_SECOND / Config.RATE_CONSUMABLE_COST) * GameTimeController.MILLIS_IN_TICK;
					_player.broadcastPacket(new MagicSkillUse(_player, _player, _skillId, _skillLevel, _delay, 0, false));
					
					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
				}
				else
				{
					_player.sendPacket(new SetupGauge(0, _delay));
					
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							finishCrafting();
						}
					}, _delay);
					
				}
			}
			else
			{
				finishCrafting();
			}
		}
		
		private void updateCurLoad()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
			_target.sendPacket(su);
		}
		
		private void updateCurMp()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getStatus().getCurrentMp());
			_target.sendPacket(su);
		}
		
		private void updateMakeInfo(boolean success)
		{
			if (_target == _player)
			{
				_target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
			}
			else
			{
				_target.sendPacket(new RecipeShopItemInfo(_player, _recipeList.getId()));
			}
		}
	}
	
	public final static Logger _log = Logger.getLogger(RecipeController.class.getName());
	
	private static RecipeController _instance;
	protected static final Map<L2PcInstance, RecipeItemMaker> _activeMakers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RecipeItemMaker>());
	private static final String RECIPES_FILE = "recipes.xml";
	
	public static RecipeController getInstance()
	{
		return _instance == null ? _instance = new RecipeController() : _instance;
	}
	
	private final Map<Integer, L2RecipeList> _lists;
	
	public RecipeController()
	{
		_lists = new HashMap<>();
		
		try
		{
			loadFromXML();
			_log.info("RecipeController: Loaded " + _lists.size() + " recipes.");
		}
		catch (Exception e)
		{
			_log.fatal("Failed loading recipe list", e);
		}
	}
	
	public int[] getAllItemIds()
	{
		int[] idList = new int[_lists.size()];
		int i = 0;
		for (L2RecipeList rec : _lists.values())
		{
			idList[i++] = rec.getRecipeId();
		}
		
		return idList;
	}
	
	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (L2RecipeList find : _lists.values())
			if (find.getRecipeId() == itemId)
				return find;
		return null;
	}
	
	public L2RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}
	
	public int getRecipesCount()
	{
		return _lists.size();
	}
	
	private L2RecipeList getValidRecipeList(L2PcInstance player, int id)
	{
		L2RecipeList recipeList = getRecipeList(id);
		
		if (recipeList == null || recipeList.getRecipes().length == 0)
		{
			player.sendMessage("There is no recipe for ID: " + id);
			player.isInCraftMode(false);
			return null;
		}
		return recipeList;
	}
	
	private void loadFromXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/xml/world/" + RECIPES_FILE);
		if (file.exists())
		{
			Document doc = factory.newDocumentBuilder().parse(file);
			List<L2RecipeInstance> recipePartList = new ArrayList<>();
			List<L2RecipeStatInstance> recipeStatUseList = new ArrayList<>();
			List<L2RecipeStatInstance> recipeAltStatChangeList = new ArrayList<>();
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					recipesFile:
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							recipePartList.clear();
							recipeStatUseList.clear();
							recipeAltStatChangeList.clear();
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							int id = -1;
							StatsSet set = new StatsSet();
							
							att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.fatal("Missing id for recipe item, skipping");
								continue;
							}
							id = Integer.parseInt(att.getNodeValue());
							set.set("id", id);
							
							att = attrs.getNamedItem("recipeId");
							if (att == null)
							{
								_log.fatal("Missing recipeId for recipe item id: " + id + ", skipping");
								continue;
							}
							set.set("recipeId", Integer.parseInt(att.getNodeValue()));
							
							att = attrs.getNamedItem("name");
							if (att == null)
							{
								_log.fatal("Missing name for recipe item id: " + id + ", skipping");
								continue;
							}
							set.set("recipeName", att.getNodeValue());
							
							att = attrs.getNamedItem("craftLevel");
							if (att == null)
							{
								_log.fatal("Missing level for recipe item id: " + id + ", skipping");
								continue;
							}
							set.set("craftLevel", Integer.parseInt(att.getNodeValue()));
							
							att = attrs.getNamedItem("type");
							if (att == null)
							{
								_log.fatal("Missing type for recipe item id: " + id + ", skipping");
								continue;
							}
							set.set("isDwarvenRecipe", att.getNodeValue().equalsIgnoreCase("dwarven"));
							
							att = attrs.getNamedItem("successRate");
							if (att == null)
							{
								_log.fatal("Missing successRate for recipe item id: " + id + ", skipping");
								continue;
							}
							set.set("successRate", Integer.parseInt(att.getNodeValue()));
							
							for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
								if ("statUse".equalsIgnoreCase(c.getNodeName()))
								{
									String statName = c.getAttributes().getNamedItem("name").getNodeValue();
									int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());
									try
									{
										recipeStatUseList.add(new L2RecipeStatInstance(statName, value));
									}
									catch (Exception e)
									{
										_log.fatal("Error in StatUse parameter for recipe item id: " + id + ", skipping");
										continue recipesFile;
									}
								}
								else if ("altStatChange".equalsIgnoreCase(c.getNodeName()))
								{
									String statName = c.getAttributes().getNamedItem("name").getNodeValue();
									int value = Integer.parseInt(c.getAttributes().getNamedItem("value").getNodeValue());
									try
									{
										recipeAltStatChangeList.add(new L2RecipeStatInstance(statName, value));
									}
									catch (Exception e)
									{
										_log.fatal("Error in AltStatChange parameter for recipe item id: " + id + ", skipping");
										continue recipesFile;
									}
								}
								else if ("ingredient".equalsIgnoreCase(c.getNodeName()))
								{
									int ingId = Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue());
									int ingCount = Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue());
									recipePartList.add(new L2RecipeInstance(ingId, ingCount));
								}
								else if ("production".equalsIgnoreCase(c.getNodeName()))
								{
									set.set("itemId", Integer.parseInt(c.getAttributes().getNamedItem("id").getNodeValue()));
									set.set("count", Integer.parseInt(c.getAttributes().getNamedItem("count").getNodeValue()));
								}
							
							L2RecipeList recipeList = new L2RecipeList(set);
							for (L2RecipeInstance recipePart : recipePartList)
							{
								recipeList.addRecipe(recipePart);
							}
							for (L2RecipeStatInstance recipeStatUse : recipeStatUseList)
							{
								recipeList.addStatUse(recipeStatUse);
							}
							for (L2RecipeStatInstance recipeAltStatChange : recipeAltStatChangeList)
							{
								recipeList.addAltStatChange(recipeAltStatChange);
							}
							
							_lists.put(id, recipeList);
						}
				}
		}
		else
		{
			_log.fatal("Recipes file (" + file.getAbsolutePath() + ") doesnt exists.");
		}
	}
	
	public synchronized void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
	{
		RecipeItemMaker maker = null;
		if (player.isNormalCraftMode())
		{
			maker = _activeMakers.get(player);
		}
		
		if (maker == null)
		{
			RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
			response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
			player.sendPacket(response);
			return;
		}
		
		player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
	}
	
	public synchronized void requestMakeItem(L2PcInstance player, int recipeListId)
	{
		if (player.isInDuel())
		{
			player.sendPacket(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			return;
		}
		
		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);
		
		if (recipeList == null)
			return;
		
		List<L2RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());
		
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		
		if (player.isNormalCraftMode() && (maker = _activeMakers.get(player)) != null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1).addItemName(recipeList.getItemId()).addString("You are busy creating"));
			return;
		}
		
		maker = new RecipeItemMaker(player, recipeList, player);
		if (maker._isValid)
			if (player.isNormalCraftMode())
			{
				_activeMakers.put(player, maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
	}
	
	public synchronized void requestMakeItemAbort(L2PcInstance player)
	{
		_activeMakers.remove(player);
	}
	
	public synchronized void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
	{
		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);
		
		if (recipeList == null)
			return;
		
		List<L2RecipeList> dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());
		
		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		
		if (manufacturer.isNormalCraftMode() && (maker = _activeMakers.get(manufacturer)) != null)
		{
			player.sendMessage("Manufacturer is busy, please try later.");
			return;
		}
		
		maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker._isValid)
			if (manufacturer.isNormalCraftMode())
			{
				_activeMakers.put(manufacturer, maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
	}
}