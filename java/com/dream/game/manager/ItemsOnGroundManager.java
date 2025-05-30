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
package com.dream.game.manager;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.templates.item.L2EtcItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ItemsOnGroundManager
{
	private static class SingletonHolder
	{
		protected static final ItemsOnGroundManager _instance = new ItemsOnGroundManager();
	}

	protected class StoreInDb extends Thread
	{
		
		@Override
		public void run()
		{
			if (!Config.SAVE_DROPPED_ITEM)
				return;

			emptyTable();

			if (_items.isEmpty() && _log.isDebugEnabled() || Config.DEBUG)
			{
				_log.warn("ItemsOnGroundManager: nothing to save...");
				return;
			}

			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("INSERT INTO items_on_ground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)");
				for (L2ItemInstance item : _items)
				{
					if (item == null)
					{
						continue;
					}

					if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
					{
						continue;
					}

					try
					{
						statement.setInt(1, item.getObjectId());
						statement.setInt(2, item.getItemId());
						statement.setInt(3, item.getCount());
						statement.setInt(4, item.getEnchantLevel());
						statement.setInt(5, item.getX());
						statement.setInt(6, item.getY());
						statement.setInt(7, item.getZ());

						if (item.isProtected())
						{
							statement.setLong(8, -1);
						}
						else
						{
							statement.setLong(8, item.getDropTime());
						}

						if (item.isEquipable())
						{
							statement.setLong(9, 1);
						}
						else
						{
							statement.setLong(9, 0);
						}

						statement.execute();
						statement.clearParameters();
					}
					catch (Exception e)
					{
						_log.fatal("error while inserting into table ItemsOnGround " + e, e);
					}

				}
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("SQL error while storing items on ground: " + e.getMessage(), e);
			}

			if (_log.isDebugEnabled() || Config.DEBUG)
			{
				_log.warn("ItemsOnGroundManager: " + _items.size() + " items on ground saved");
			}
		}
	}

	protected static Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());

	public static final ItemsOnGroundManager getInstance()
	{
		return SingletonHolder._instance;
	}

	protected List<L2ItemInstance> _items = null;

	protected ItemsOnGroundManager()
	{
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		_items = new ArrayList<>();

		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new StoreInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
		}

		load();
	}

	public void cleanUp()
	{
		_items.clear();
	}

	
	public void emptyTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement del = con.prepareStatement("DELETE FROM items_on_ground");
			del.execute();
			del.close();
		}
		catch (Exception e1)
		{
			_log.fatal("error while cleaning table items_on_ground " + e1, e1);
		}
	}

	
	private void load()
	{
		if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
		{
			emptyTable();
		}

		if (!Config.SAVE_DROPPED_ITEM)
			return;

		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				String str = null;
				if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "UPDATE items_on_ground SET drop_time=? WHERE drop_time=-1 AND equipable=0";
				}
				else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
				{
					str = "UPDATE items_on_ground SET drop_time=? WHERE drop_time=-1";
				}

				PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.fatal("error while updating table ItemsOnGround " + e, e);
			}
		}

		// Add items to world
		L2ItemInstance item;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement s = con.createStatement();
			ResultSet result;

			int count = 0;
			result = s.executeQuery("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM items_on_ground");

			while (result.next())
			{
				item = new L2ItemInstance(result.getInt(1), result.getInt(2));
				L2World.getInstance().storeObject(item);

				if (item.isStackable() && result.getInt(3) > 1)
				{
					item.setCount(result.getInt(3));
				}

				if (result.getInt(4) > 0)
				{
					item.setEnchantLevel(result.getInt(4));
				}

				item.getPosition().setXYZ(result.getInt(5), result.getInt(6), result.getInt(7));
				item.getPosition().getWorldRegion().addVisibleObject(item);
				item.setDropTime(result.getLong(8));
				if (result.getLong(8) == -1)
				{
					item.setProtected(true);
				}
				else
				{
					item.setProtected(false);
				}

				L2World.getInstance().addVisibleObject(item, null);
				_items.add(item);
				count++;

				// add to ItemsAutoDestroy only items not protected
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
					if (result.getLong(8) > -1)
						if (Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB)
						{
							ItemsAutoDestroy.getInstance().addItem(item);
						}
			}
			result.close();
			s.close();

			if (count > 0)
			{
				System.out.println("ItemsOnGroundManager: restored " + count + " items.");
			}
			else
			{
				System.out.println("Initializing ItemsOnGroundManager.");
			}
		}
		catch (Exception e)
		{
			_log.fatal("error while loading ItemsOnGround " + e, e);
		}

		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
		{
			emptyTable();
		}
	}

	public void removeObject(L2ItemInstance item)
	{
		if (Config.SAVE_DROPPED_ITEM && _items != null)
		{
			_items.remove(item);
		}
	}

	public void save(L2ItemInstance item)
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			_items.add(item);
		}
	}

	public void saveData()
	{
		new StoreInDb().run();
	}
}