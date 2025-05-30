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
import com.dream.game.model.CursedWeapon;
import com.dream.game.model.L2Boss;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2FeedableBeastInstance;
import com.dream.game.model.actor.instance.L2FestivalMonsterInstance;
import com.dream.game.model.actor.instance.L2FortCommanderInstance;
import com.dream.game.model.actor.instance.L2FortSiegeGuardInstance;
import com.dream.game.model.actor.instance.L2GuardInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2RiftInvaderInstance;
import com.dream.game.model.actor.instance.L2SiegeGuardInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CursedWeaponsManager
{
	private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());

	private static CursedWeaponsManager _instance;

	public static void announce(SystemMessage sm)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
			{
				continue;
			}

			player.sendPacket(sm);
		}
	}

	public static final CursedWeaponsManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CursedWeaponsManager();
		}
		return _instance;
	}

	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{

			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();

			statement.close();
			con.close();
		}
		catch (SQLException e)
		{
			_log.fatal("CursedWeaponsManager: Failed to remove data: " + e);
		}

	}

	private Map<Integer, CursedWeapon> _cursedWeapons;

	public CursedWeaponsManager()
	{
		_cursedWeapons = new HashMap<>();
		load();
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		if (Config.ALLOW_CURSED_WEAPONS)
		{
			CursedWeapon cw = _cursedWeapons.get(item.getItemId());

			if (player.isCursedWeaponEquipped())
			{
				CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());

				cw2.setNbKills(cw2.getStageKills() - 1);
				cw2.increaseKills();

				cw.setPlayer(player);
				cw.endOfLife();
			}
			else
			{
				cw.activate(player, item);
			}
		}
	}

	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (Config.ALLOW_CURSED_WEAPONS)
		{
			if (attackable instanceof L2SiegeGuardInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2FestivalMonsterInstance || attackable instanceof L2GuardInstance || attackable instanceof L2Boss || attackable instanceof L2FeedableBeastInstance || attackable instanceof L2FortSiegeGuardInstance || attackable instanceof L2FortCommanderInstance)
				return;

			for (CursedWeapon cw : _cursedWeapons.values())
			{
				if (cw.isActive())
				{
					continue;
				}

				if (cw.checkDrop(attackable, player))
				{
					break;
				}
			}
		}
	}

	public void drop(int itemId, L2Character killer)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		cw.dropIt(killer);
	}

	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}

	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}

	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}

	public int getLevel(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		return cw.getLevel();
	}

	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception e)
		{
		}
	}

	public void increaseKills(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		cw.increaseKills();
	}

	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}

	
	private final void load()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File(Config.DATAPACK_ROOT, "data/xml/world/cursedWeapons.xml");
			if (!file.exists())
				throw new IOException();

			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();

							CursedWeapon cw = new CursedWeapon(id, skillId, name);

							int val;
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDropRate(val);
								}
								else if ("duration".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDuration(val);
								}
								else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDurationLost(val);
								}
								else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDisapearChance(val);
								}
								else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setStageKills(val);
								}

							_cursedWeapons.put(id, cw);
						}
				}

			Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			ResultSet rset;

			if (Config.ALLOW_CURSED_WEAPONS)
			{
				statement = con.prepareStatement("SELECT itemId, charId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
				rset = statement.executeQuery();

				while (rset.next())
				{
					int itemId = rset.getInt("itemId");
					int playerId = rset.getInt("charId");
					int playerKarma = rset.getInt("playerKarma");
					int playerPkKills = rset.getInt("playerPkKills");
					int nbKills = rset.getInt("nbKills");
					long endTime = rset.getLong("endTime");

					CursedWeapon cw = _cursedWeapons.get(itemId);
					cw.setPlayerId(playerId);
					cw.setPlayerKarma(playerKarma);
					cw.setPlayerPkKills(playerPkKills);
					cw.setNbKills(nbKills);
					cw.setEndTime(endTime);
					cw.reActivate();
				}

				rset.close();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("TRUNCATE TABLE cursed_weapons");
				rset = statement.executeQuery();
				rset.close();
				statement.close();
			}
			con.close();

			for (CursedWeapon cw : _cursedWeapons.values())
			{
				if (cw.isActivated())
				{
					continue;
				}

				int itemId = cw.getItemId();
				try
				{
					statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					rset = statement.executeQuery();

					if (rset.next())
					{
						int playerId = rset.getInt("owner_id");
						_log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

						statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						if (statement.executeUpdate() != 1)
						{
							_log.warn("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}

						statement.close();

						statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId = ?");
						statement.setInt(1, cw.getPlayerKarma());
						statement.setInt(2, cw.getPlayerPkKills());
						statement.setInt(3, playerId);
						if (statement.executeUpdate() != 1)
						{
							_log.warn("Error while updating karma & pkkills for charId " + cw.getPlayerId());
						}

						removeFromDb(itemId);
					}
				}
				catch (SQLException sqlE)
				{

				}
				try
				{
					con.close();
				}
				catch (Exception e)
				{
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not load CursedWeapons data: " + e);
		}

		_log.info("Cursed Weapon's: Loaded " + _cursedWeapons.size() + " Cursed Weapon(s).");
	}

	public void onEnter(L2PcInstance player)
	{
		if (player == null)
			return;

		for (CursedWeapon cw : _cursedWeapons.values())
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquippedId(cw.getItemId());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1).addString(cw.getName()).addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000)));
			}
	}

	public void onExit(L2PcInstance player)
	{
		if (player == null)
			return;

		for (CursedWeapon cw : _cursedWeapons.values())
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(null);
				cw.setItem(null);
			}
	}

	public final void reload()
	{
		_cursedWeapons = new HashMap<>();
		load();
	}

	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			cw.saveData();
		}
	}
}