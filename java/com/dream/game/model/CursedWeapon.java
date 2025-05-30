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
package com.dream.game.model;

import com.dream.Config;
import com.dream.L2DatabaseFactory;
import com.dream.Message;
import com.dream.game.datatables.xml.SkillTable;
import com.dream.game.manager.CursedWeaponsManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.Location;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.network.serverpackets.Earthquake;
import com.dream.game.network.serverpackets.ExRedSky;
import com.dream.game.network.serverpackets.InventoryUpdate;
import com.dream.game.network.serverpackets.ItemList;
import com.dream.game.network.serverpackets.SocialAction;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.templates.item.L2Item;
import com.dream.tools.random.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

public class CursedWeapon
{
	private class RemoveTask implements Runnable
	{
		protected RemoveTask()
		{
		}

		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= getEndTime())
			{
				endOfLife();
			}
		}
	}

	private static final Logger _log = Logger.getLogger(CursedWeapon.class.getName());
	private final String _name;
	private final int _itemId;
	private final int _skillId;
	private final int _skillMaxLevel;
	private int _dropRate;
	private int _duration;
	private int _durationLost;
	private int _disapearChance;

	private int _stageKills;
	private boolean _isDropped = false;
	private boolean _isActivated = false;

	private ScheduledFuture<?> _removeTask;
	private int _nbKills = 0;

	private long _endTime = 0;
	private int _playerId = 0;
	private L2PcInstance _player = null;
	private L2ItemInstance _item = null;
	private int _playerKarma = 0;

	private int _playerPkKills = 0;

	public CursedWeapon(int itemId, int skillId, String name)
	{
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted())
		{
			if (!player.dismount())
			{
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CANT_PICK_UP_WHILE_RIDING));
				player.dropItem("InvDrop", item, null, true);
				return;
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(_item.getItemId()));
			return;
		}

		if (player._event != null)
		{
			player._event.remove(player);
		}
		_isActivated = true;

		_player = player;
		_playerId = _player.getObjectId();
		_playerKarma = _player.getKarma();
		_playerPkKills = _player.getPkKills();
		saveData();

		_player.setKarma(9999999);
		_player.setPkKills(0);

		if (_player.isInParty())
		{
			_player.getParty().removePartyMember(_player);
		}

		if (_player.isWearingFormalWear())
		{
			_player.getInventory().unEquipItemInSlot(10);
		}

		giveSkill();

		_item = item;
		_player.getInventory().equipItemAndRecord(_item);
		_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(_item));
		_player.getStatus().setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
		_player.getStatus().setCurrentCp(_player.getMaxCp());
		_player.sendPacket(new ItemList(_player, false));
		_player.broadcastUserInfo();
		SocialAction atk = new SocialAction(_player, 17);
		_player.broadcastPacket(atk);
		_player.setCursedWeaponEquippedId(_itemId);
		CursedWeaponsManager.announce(SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION).addZoneName(_player.getX(), _player.getY(), _player.getZ()).addItemName(_item));
	}

	private void cancelTask()
	{
		if (_removeTask != null)
		{
			_removeTask.cancel(true);
			_removeTask = null;
		}
	}

	public boolean checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (Rnd.get(1000000) < _dropRate)
		{
			dropIt(attackable, player);

			_endTime = System.currentTimeMillis() + _duration * 60000L;
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000, _durationLost * 12000L);

			return true;
		}

		return false;
	}

	public void cursedOnLogin(L2PcInstance activeChar)
	{
		_player = activeChar;
		giveSkill();

		CursedWeaponsManager.announce(SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION).addZoneName(_player.getX(), _player.getY(), _player.getZ()).addItemName(_player.getCursedWeaponEquippedId()));
		CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_player.getCursedWeaponEquippedId());
		SystemMessage msg2 = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
		int timeLeftInHours = (int) (cw.getTimeLeft() / 60000 / 60);
		msg2.addItemName(_player.getCursedWeaponEquippedId());
		msg2.addNumber(timeLeftInHours * 60);
		_player.sendPacket(msg2);
	}

	private void dropIt(L2Attackable attackable, L2PcInstance player)
	{
		dropIt(attackable, player, null, true);
	}

	public void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster)
	{
		_isActivated = false;

		if (fromMonster)
		{
			_item = attackable.dropItem(player, _itemId, 1);
			_item.setDropTime(0);

			ExRedSky packet = new ExRedSky(10);
			Earthquake eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 14, 3);
			for (L2PcInstance aPlayer : L2World.getInstance().getAllPlayers())
			{
				aPlayer.sendPacket(packet);
				aPlayer.sendPacket(eq);
			}
		}
		else
		{
			_item = _player.getInventory().getItemByItemId(_itemId);
			_player.dropItem("DieDrop", _item, killer, true);
			_player.setKarma(_playerKarma);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkillAndAppearance();
			_player.abortAttack();

			_player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
			_player.store();
			_player.sendPacket(new ItemList(_player, false));
			_player.broadcastUserInfo();
		}

		_isDropped = true;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
		sm.addZoneName(player.getX(), player.getY(), player.getZ());
		if (fromMonster)
		{
			sm.addItemName(_itemId);
		}
		else
		{
			sm.addItemName(_item);
		}
		CursedWeaponsManager.announce(sm);
	}

	public void dropIt(L2Character killer)
	{
		if (Rnd.get(100) <= _disapearChance)
		{
			endOfLife();
		}
		else
		{
			dropIt(null, _player, killer, false);
			_player.setKarma(_playerKarma);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkillAndAppearance();

			_player.abortAttack();
			_player.broadcastUserInfo();
		}
	}

	
	public void endOfLife()
	{
		if (_isActivated)
		{
			if (_player != null && _player.isOnline() == 1)
			{
				_log.info(_name + " being removed online.");

				_player.abortAttack();

				_player.setKarma(_playerKarma);
				_player.setPkKills(_playerPkKills);
				_player.setCursedWeaponEquippedId(0);
				removeSkillAndAppearance();

				_player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
				_player.store();

				L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
				InventoryUpdate iu = new InventoryUpdate();
				if (removedItem.getCount() == 0)
				{
					iu.addRemovedItem(removedItem);
				}
				else
				{
					iu.addModifiedItem(removedItem);
				}

				_player.sendPacket(iu);

				_player.broadcastUserInfo();
			}
			else
			{
				_log.info(_name + " being removed offline.");

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(con);

					PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					statement.setInt(1, _playerId);
					statement.setInt(2, _itemId);
					if (statement.executeUpdate() != 1)
					{
						_log.warn("Error while deleting itemId " + _itemId + " from userId " + _playerId);
					}

					statement.close();
					statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?");
					statement.setInt(1, _playerKarma);
					statement.setInt(2, _playerPkKills);
					statement.setInt(3, _playerId);
					if (statement.executeUpdate() != 1)
					{
						_log.warn("Error while updating karma & pkkills for userId " + _playerId);
					}

					statement.close();
				}
				catch (Exception e)
				{
					_log.warn("Could not delete : " + e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}
		else if (_player != null && _player.getInventory().getItemByItemId(_itemId) != null)
		{
			L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("", _itemId, 1, _player, null);
			{
				InventoryUpdate iu = new InventoryUpdate();
				if (removedItem.getCount() == 0)
				{
					iu.addRemovedItem(removedItem);
				}
				else
				{
					iu.addModifiedItem(removedItem);
				}

				_player.sendPacket(iu);
			}

			_player.broadcastUserInfo();
		}
		else if (_item != null)
		{
			_item.decayMe();
			L2World.getInstance().removeObject(_item);
			_log.info(_name + " item has been removed from World.");
		}

		CursedWeaponsManager.removeFromDb(_itemId);
		CursedWeaponsManager.announce(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addString(_name).addItemName(_itemId));

		cancelTask();
		_isActivated = false;
		_isDropped = false;
		_endTime = 0;
		_player = null;
		_playerId = 0;
		_playerKarma = 0;
		_playerPkKills = 0;
		_item = null;
		_nbKills = 0;
	}

	public Location getCurrentLocation()
	{
		if (_isActivated && _player != null)
			return _player.getPosition().getCurrentLocation();

		if (_isDropped && _item != null)
			return _item.getPosition().getCurrentLocation();

		return null;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLevel()
	{
		return Math.min(1 + _nbKills / _stageKills, _skillMaxLevel);
	}

	public String getName()
	{
		return _name;
	}

	public int getNbKills()
	{
		return _nbKills;
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	public int getPlayerId()
	{
		return _playerId;
	}

	public int getPlayerKarma()
	{
		return _playerKarma;
	}

	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public int getStageKills()
	{
		return _stageKills;
	}

	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}

	public void giveSkill()
	{
		int level = 1 + _nbKills / _stageKills;
		if (level > _skillMaxLevel)
		{
			level = _skillMaxLevel;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		// Yesod:
		// To properly support subclasses this skill can not be stored.
		_player.addSkill(skill, false);

		// Void Burst, Void Flow
		skill = SkillTable.getInstance().getInfo(3630, 1);
		_player.addSkill(skill, false);
		skill = SkillTable.getInstance().getInfo(3631, 1);
		_player.addSkill(skill, false);

		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.debug("Player " + _player.getName() + " has been awarded with skill " + skill);
		}
		_player.sendSkillList();
	}

	public void goTo(L2PcInstance player)
	{
		if (player == null)
			return;

		if (_isActivated)
		{
			player.teleToLocation(_player.getX(), _player.getY(), _player.getZ() + 20);
		}
		else if (_isDropped)
		{
			player.teleToLocation(_item.getX(), _item.getY(), _item.getZ() + 20);
		}
		else
		{
			player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_CHAR_NOT_FOUND));
		}
	}

	public void increaseKills()
	{
		_nbKills++;

		if (_player != null && _player.isOnline() > 0)
		{
			_player.setPkKills(_nbKills);
			_player.broadcastUserInfo();

			if (_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
			{
				giveSkill();
			}
		}

		_endTime -= _durationLost * 60000;
		saveData();
	}

	public void increaseKills(int kills)
	{
		_nbKills += kills;

		if (_player != null && _player.isOnline() > 0)
		{
			_player.setPkKills(_nbKills);
			_player.broadcastUserInfo();

			if (_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
			{
				giveSkill();
			}
		}

		_endTime -= _durationLost * 60000;
		saveData();
	}

	public boolean isActivated()
	{
		return _isActivated;
	}

	public boolean isActive()
	{
		return _isActivated || _isDropped;
	}

	public boolean isDropped()
	{
		return _isDropped;
	}

	public void reActivate()
	{
		_isActivated = true;
		if (_endTime - System.currentTimeMillis() <= 0)
		{
			endOfLife();
		}
		else
		{
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);
		}
	}

	public void removeSkillAndAppearance()
	{
		_player.removeSkill(SkillTable.getInstance().getInfo(_skillId, _player.getSkillLevel(_skillId)), false);
		_player.removeSkill(SkillTable.getInstance().getInfo(3630, 1), false);
		_player.removeSkill(SkillTable.getInstance().getInfo(3631, 1), false);
		_player.sendSkillList();
	}

	
	public void saveData()
	{
		if (_log.isDebugEnabled() || Config.DEBUG)
		{
			_log.debug("CursedWeapon: Saving data to disk.");
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, _itemId);
			statement.executeUpdate();

			if (_isActivated)
			{
				statement = con.prepareStatement("INSERT INTO cursed_weapons (itemId, charId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, _itemId);
				statement.setInt(2, _playerId);
				statement.setInt(3, _playerKarma);
				statement.setInt(4, _playerPkKills);
				statement.setInt(5, _nbKills);
				statement.setLong(6, _endTime);
				statement.executeUpdate();
			}

			statement.close();
			con.close();
		}
		catch (SQLException e)
		{
			_log.error("CursedWeapon: Failed to save data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void setActivated(boolean isActivated)
	{
		_isActivated = isActivated;
	}

	public void setDisapearChance(int disapearChance)
	{
		_disapearChance = disapearChance;
	}

	public void setDropped(boolean isDropped)
	{
		_isDropped = isDropped;
	}

	public void setDropRate(int dropRate)
	{
		_dropRate = dropRate;
	}

	public void setDuration(int duration)
	{
		_duration = duration;
	}

	public void setDurationLost(int durationLost)
	{
		_durationLost = durationLost;
	}

	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}

	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}

	public void setNbKills(int nbKills)
	{
		_nbKills = nbKills;
	}

	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}

	public void setPlayerId(int playerId)
	{
		_playerId = playerId;
	}

	public void setPlayerKarma(int playerKarma)
	{
		_playerKarma = playerKarma;
	}

	public void setPlayerPkKills(int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}

	public void setStageKills(int stageKills)
	{
		_stageKills = stageKills;
	}
}