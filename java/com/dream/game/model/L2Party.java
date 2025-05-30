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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.dream.Config;
import com.dream.Message;
import com.dream.game.datatables.sql.ItemTable;
import com.dream.game.datatables.xml.MapRegionTable.TeleportWhereType;
import com.dream.game.manager.DuelManager;
import com.dream.game.manager.PartyRoomManager;
import com.dream.game.model.actor.L2Attackable;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2SummonInstance;
import com.dream.game.model.entity.DimensionalRift;
import com.dream.game.model.entity.sevensigns.SevenSignsFestival;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.CreatureSay;
import com.dream.game.network.serverpackets.ExCloseMPCC;
import com.dream.game.network.serverpackets.ExClosePartyRoom;
import com.dream.game.network.serverpackets.ExManagePartyRoomMember;
import com.dream.game.network.serverpackets.ExMultiPartyCommandChannelInfo;
import com.dream.game.network.serverpackets.ExOpenMPCC;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.PartySmallWindowAdd;
import com.dream.game.network.serverpackets.PartySmallWindowAll;
import com.dream.game.network.serverpackets.PartySmallWindowDelete;
import com.dream.game.network.serverpackets.PartySmallWindowDeleteAll;
import com.dream.game.network.serverpackets.SystemMessage;
import com.dream.game.skills.Stats;
import com.dream.game.util.Util;
import com.dream.tools.random.Rnd;
import com.dream.util.LinkedBunch;

public class L2Party
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};

	public static final int ITEM_LOOTER = 0;

	public static final int ITEM_RANDOM = 1;

	public static final int ITEM_RANDOM_SPOIL = 2;

	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;

	private static double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
			return 1;
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}

		return BONUS_EXP_SP[i];
	}

	private static double getExpBonus(int membersCount)
	{
		if (membersCount < 2)
			return getBaseExpSpBonus(membersCount);

		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}

	private static double getSpBonus(int membersCount)
	{
		if (membersCount < 2)
			return getBaseExpSpBonus(membersCount);

		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}

	private static List<L2Playable> getValidMembers(List<L2Playable> members, int topLvl)
	{
		List<L2Playable> validMembers = new ArrayList<>();

		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (L2Playable member : members)
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
		}
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
				{
					validMembers.add(member);
				}
			}
		}
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			int i = members.size() - 1;
			if (i < 1)
				return members;
			if (i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}

			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
				{
					validMembers.add(member);
				}
			}
		}
		return validMembers;
	}

	private List<L2PcInstance> _members = null;
	private int _pendingInvitation = 0;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;

	private int _itemLastLoot = 0;

	private L2CommandChannel _commandChannel = null;

	private L2PartyRoom _partyRoom = null;

	private DimensionalRift _dr;

	public L2Party(L2PcInstance leader, int itemDistribution)
	{
		_itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		_partyLvl = leader.getLevel();
		_partyRoom = leader.getPartyRoom();
	}

	public boolean addPartyMember(L2PcInstance player)
	{
		if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
		{
			int _min = _partyLvl;
			int _max = _partyLvl;

			boolean invalidMember = false;

			for (int i = 0; i < getMemberCount(); i++)
			{
				int _lvl = getPartyMembers().get(i).getLevel();
				if (_lvl < _min)
				{
					_min = _lvl;
				}
				if (_lvl > _max)
				{
					_max = _lvl;
				}
			}

			if (player.getLevel() > _max)
			{
				invalidMember = player.getLevel() - _min > Config.MAX_PARTY_LEVEL_DIFFERENCE;
			}
			if (player.getLevel() < _min)
			{
				invalidMember = _max - player.getLevel() > Config.MAX_PARTY_LEVEL_DIFFERENCE;
			}

			if (invalidMember)
			{
				getLeader().sendMessage(Message.getMessage(getLeader(), Message.MessageId.MSG_LVL_DIFF_TO_HIGH_TO_INVITE));
				player.sendMessage(Message.getMessage(player, Message.MessageId.MSG_LVL_DIFF_TO_HIGH_TO_JOIN));
				return false;
			}
		}
		if (player.isLookingForParty())
		{
			PartyRoomManager.getInstance().removeFromWaitingList(player);
			player.sendPacket(ExClosePartyRoom.STATIC_PACKET);
		}
		L2PartyRoom room = getPartyRoom();
		L2PartyRoom newMembRoom = player.getPartyRoom();
		if (newMembRoom != null && newMembRoom != room)
			if (newMembRoom.getLeader() == player)
			{
				PartyRoomManager.getInstance().removeRoom(newMembRoom.getId());
			}
			else
			{
				newMembRoom.removeMember(player, false);
			}
		player.sendPacket(new PartySmallWindowAll(this));

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addString(getLeader().getName()));
		broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addString(player.getName()));
		broadcastToPartyMembers(new PartySmallWindowAdd(player));

		getPartyMembers().add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}

		for (L2PcInstance member : getPartyMembers())
		{
			member.broadcastUserInfo();
		}

		updateEffectIcons();

		if (isInDimensionalRift())
		{
			_dr.partyMemberInvited();
		}

		if (isInCommandChannel())
		{
			player.sendPacket(ExOpenMPCC.STATIC_PACKET);
			getCommandChannel().broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(getCommandChannel()));
		}
		if (room != null)
		{
			if (getMemberCount() == 2)
			{
				room.setParty(this);
			}
			room.addMember(player);
			room.broadcastPacket(new ExManagePartyRoomMember(ExManagePartyRoomMember.MODIFIED, player));
		}
		return true;
	}

	public void broadcastCSToPartyMembers(CreatureSay msg, L2PcInstance broadcaster)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}

			if (!(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(member, broadcaster)))
			{
				member.sendPacket(msg);
			}
		}
	}

	public void broadcastSnoopToPartyMembers(int type, String name, String text)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			member.broadcastSnoop(type, name, text);
		}
	}

	public void broadcastToPartyMembers(L2GameServerPacket msg)
	{
		for (L2PcInstance member : getPartyMembers())
			if (member != null)
			{
				member.sendPacket(msg);
			}
	}

	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
	{
		for (L2PcInstance member : getPartyMembers())
			if (member != null && !member.equals(player))
			{
				member.sendPacket(msg);
			}
	}

	public void broadcastToPartyMembersNewLeader()
	{
		broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addString(getLeader().getName()));
		L2PartyRoom room = getPartyRoom();
		if (room != null)
		{
			room.broadcastPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED.getSystemMessage());
		}
		refreshPartyView();
	}

	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		L2PcInstance leader = getLeader();

		if (player != null && !player.isInDuel())
			if (getPartyMembers().contains(player))
			{
				if (leader == player)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
				}
				else
				{
					int p1 = getPartyMembers().indexOf(player);
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, leader);

					L2PartyRoom room = getPartyRoom();
					if (room != null)
					{
						leader.setLookingForParty(false);
						leader.broadcastUserInfo();
						player.setLookingForParty(true);
						player.broadcastUserInfo();
						room.updateRoomStatus(true);
					}

					broadcastToPartyMembersNewLeader();
					if (isInCommandChannel() && getCommandChannel().getChannelLeader() == leader)
					{
						_commandChannel.setChannelLeader(getLeader());
						_commandChannel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1).addString(getLeader().getName()));
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
			}
	}

	public void decreasePendingInvitationNumber()
	{
		_pendingInvitation--;
	}

	public void distributeAdena(L2PcInstance player, int adena, L2Character target)
	{
		List<L2PcInstance> membersList = getPartyMembers();

		List<L2PcInstance> ToReward = new ArrayList<>();
		for (L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}
			ToReward.add(member);
		}

		if (ToReward.isEmpty())
			return;

		int count = adena / ToReward.size();
		for (L2PcInstance member : ToReward)
		{
			member.addAdena("Party", count, player, true);
		}
	}

	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null || player == null)
			return;

		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}

		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);

		if (looter.getInventory().validateCapacityByItemId(item.getItemId()))
		{
			looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);

			if (item.getCount() > 1)
			{
				SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2_S3) : SystemMessage.getSystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
				msg.addString(looter.getName());
				msg.addItemName(item.getItemId());
				msg.addNumber(item.getCount());
				broadcastToPartyMembers(looter, msg);
			}
			else
			{
				SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_PICKED_UP_S2);
				msg.addString(looter.getName());
				msg.addItemName(item.getItemId());
				broadcastToPartyMembers(looter, msg);
			}
		}
	}

	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.destroyItem("Party", item, player, null);
			return;
		}

		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);

		if (item.getCount() > 1)
		{
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3).addString(target.getName()).addItemName(item).addNumber(item.getCount()));
		}
		else
		{
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_PICKED_UP_S2).addString(target.getName()).addItemName(item.getItemId()));
		}
	}

	public void distributeXpAndSp(long xpReward, int spReward, List<L2Playable> rewardedMembers, int topLvl, L2Npc target, int partyDmg, boolean isChampion)
	{
		L2SummonInstance summon = null;
		List<L2Playable> validMembers = getValidMembers(rewardedMembers, topLvl);

		long CalcXPReward;
		int CalcSPReward;
		float penalty;
		double sqLevel;
		double preCalculationExp;
		double preCalculationSp;

		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());

		double sqLevelSum = 0;
		for (L2Playable character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}

		synchronized (rewardedMembers)
		{
			for (L2Character member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}
				CalcXPReward = xpReward;
				CalcSPReward = spReward;

				penalty = 0;

				if (member.getPet() instanceof L2SummonInstance)
				{
					summon = (L2SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}

				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetData().getOwnerExpTaken() > 0)
					{
						continue;
					}

					penalty = (float) 0.85;
				}

				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculationExp = sqLevel / sqLevelSum * (1 - penalty);
					preCalculationSp = sqLevel / sqLevelSum;

					if (!member.isDead())
					{
						long addexp = Math.round(member.calcStat(Stats.EXPSP_RATE, CalcXPReward * preCalculationExp, null, null));
						int addsp = (int) member.calcStat(Stats.EXPSP_RATE, CalcSPReward * preCalculationSp, null, null);

						member.addExpAndSp(addexp, addsp);
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
	}

	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;

		switch (_itemDistribution)
		{
			case ITEM_RANDOM:
				if (!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if (!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}

		if (looter == null)
		{
			looter = player;
		}
		return looter;
	}

	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++_itemLastLoot >= getMemberCount())
			{
				_itemLastLoot = 0;
			}
			L2PcInstance member;
			try
			{
				member = getPartyMembers().get(_itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
					return member;
			}
			catch (Exception e)
			{

			}
		}

		return null;
	}

	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new ArrayList<>();
		for (L2PcInstance member : getPartyMembers())
			if (member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				availableMembers.add(member);
			}
		if (!availableMembers.isEmpty())
			return availableMembers.get(Rnd.get(availableMembers.size()));

		return null;
	}

	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public DimensionalRift getDimensionalRift()
	{
		return _dr;
	}

	public synchronized L2PcInstance getLeader()
	{
		try
		{
			return _members.get(0);
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public L2PcInstance getMemberById(int objId)
	{
		for (L2PcInstance m : _members)
			if (m.getObjectId() == objId)
				return m;
		return null;
	}

	public int getMemberCount()
	{
		return getPartyMembers().size();
	}

	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}

	public synchronized List<L2PcInstance> getPartyMembers()
	{
		if (_members == null)
		{
			_members = new ArrayList<>();
		}
		return _members;
	}

	public L2PcInstance[] getPartyMembersWithoutLeader()
	{
		LinkedBunch<L2PcInstance> list = new LinkedBunch<>();

		for (L2PcInstance player : getPartyMembers())
		{
			if (player == null)
			{
				continue;
			}
			if (!isLeader(player))
			{
				list.add(player);
			}
		}

		return list.moveToArray(new L2PcInstance[list.size()]);
	}

	public L2PartyRoom getPartyRoom()
	{
		return _partyRoom;
	}

	public int getPendingInvitationNumber()
	{
		return _pendingInvitation;
	}

	private L2PcInstance getPlayerByName(String name)
	{
		for (L2PcInstance member : getPartyMembers())
			if (member.getName().equals(name))
				return member;
		return null;
	}

	public void increasePendingInvitationNumber()
	{
		_pendingInvitation++;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public boolean isInDimensionalRift()
	{
		return _dr != null;
	}

	public boolean isLeader(L2PcInstance player)
	{
		return getLeader().equals(player);
	}

	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}
			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		_partyLvl = newLevel;
	}

	public void refreshPartyView()
	{
		broadcastToPartyMembers(PartySmallWindowDeleteAll.STATIC_PACKET);

		final L2PcInstance leader = getLeader();

		broadcastToPartyMembers(leader, new PartySmallWindowAll(this));

		for (L2PcInstance member : getPartyMembersWithoutLeader())
		{
			leader.sendPacket(new PartySmallWindowAdd(member));
		}

		updateEffectIcons();
	}

	public void removePartyMember(L2PcInstance player)
	{
		removePartyMember(player, false);
	}

	public void removePartyMember(L2PcInstance player, boolean oust)
	{
		if (getPartyMembers().contains(player))
		{
			boolean isLeader = isLeader(player);
			getPartyMembers().remove(player);
			recalculatePartyLevel();

			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			}

			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}

			try
			{
				if (player.getFusionSkill() != null)
				{
					player.abortCast();
				}

				for (L2Character character : player.getKnownList().getKnownCharacters())
					if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == player)
					{
						character.abortCast();
					}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (oust)
			{
				player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY.getSystemMessage());
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_LEFT_PARTY.getSystemMessage());
			}
			player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
			player.setParty(null);
			if (player._inSepulture)
			{
				player.teleToLocation(TeleportWhereType.Town);
			}
			if (oust)
			{
				broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_EXPELLED_FROM_PARTY).addString(player.getName()));
			}
			else
			{
				broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY).addString(player.getName()));
			}
			broadcastToPartyMembers(new PartySmallWindowDelete(player));

			if (isInDimensionalRift())
			{
				_dr.partyMemberExited(player);
			}

			if (isLeader && getPartyMembers().size() > 1)
			{
				broadcastToPartyMembersNewLeader();
			}
			L2PartyRoom room = getPartyRoom();
			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
					if (getCommandChannel().getChannelLeader().equals(getLeader()))
					{
						getCommandChannel().disbandChannel();
					}
					else
					{
						L2CommandChannel cmd = getCommandChannel();
						getCommandChannel().removeParty(this);
						cmd.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addString(getLeader().getName()));
					}
				if (room != null)
				{
					setPartyRoom(null);
					if (isLeader)
					{
						PartyRoomManager.getInstance().removeRoom(room.getId());
						player.setLookingForParty(false);
						player.broadcastUserInfo();
					}
					else
					{
						room.setParty(null);
					}
				}
				L2PcInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(leader);
					}
					if (leader.isFestivalParticipant())
					{
						SevenSignsFestival.getInstance().updateParticipants(leader, this);
					}
				}
				_members = null;
			}
			else if (isInCommandChannel())
			{
				player.sendPacket(ExCloseMPCC.STATIC_PACKET);
				getCommandChannel().broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(getCommandChannel()));
			}
			if (room != null)
			{
				room.removeMember(player, oust);
			}
		}
	}

	public void removePartyMember(String name, boolean oust)
	{
		L2PcInstance player = getPlayerByName(name);

		if (player != null)
		{
			removePartyMember(player, oust);
		}
	}

	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public void setDimensionalRift(DimensionalRift dr)
	{
		_dr = dr;
	}

	public void setLootDistribution(int dist)
	{
		_itemDistribution = dist;
	}

	public void setPartyRoom(L2PartyRoom room)
	{
		_partyRoom = room;
	}

	private void updateEffectIcons()
	{
		for (L2PcInstance member : getPartyMembers())
			if (member != null)
			{
				member.updateEffectIcons();

				L2Summon summon = member.getPet();
				if (summon != null)
				{
					summon.updateEffectIcons();
				}
			}
	}
}