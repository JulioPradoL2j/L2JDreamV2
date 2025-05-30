package com.dream.game.model;

import java.util.Set;

import com.dream.Message;
import com.dream.game.manager.BlockListManager;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.SystemMessageId;
import com.dream.game.network.serverpackets.SystemMessage;

public final class BlockList
{
	public static boolean isBlocked(L2PcInstance listOwner, L2PcInstance player)
	{
		return listOwner.getBlockList().contains(player);
	}

	private final L2PcInstance _owner;
	private final Set<String> _set;

	private boolean _blockingAll = false;

	public BlockList(L2PcInstance owner)
	{
		_owner = owner;
		_set = BlockListManager.getInstance().getBlockList(_owner.getObjectId());
	}

	public void add(String name)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(name);
		if (player == null)
		{
			_owner.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if (player.isGM())
		{
			_owner.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
			return;
		}

		if (_set.add(player.getName()))
		{
			_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addPcName(player));

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(_owner));

			BlockListManager.insert(_owner, player);
		}
		else
		{
			_owner.sendMessage(String.format(Message.getMessage(_owner, Message.MessageId.MSG_ALREADY_IGNORED), player.getName()));
		}
	}

	private boolean contains(L2PcInstance player)
	{
		if (player == null || player.isGM())
			return false;

		return _blockingAll || _set.contains(player.getName());
	}

	public void remove(String name)
	{
		if (_set.remove(name))
		{
			_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(name));

			BlockListManager.remove(_owner, name);
		}
		else
		{
			_owner.sendMessage(String.format(Message.getMessage(_owner, Message.MessageId.MSG_NOT_IN_IGNOR_LIST), name));
		}
	}

	public void sendListToOwner()
	{
		_owner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);
		for (String name : _set)
		{
			_owner.sendMessage(name);
		}
	}

	public void setBlockingAll(boolean blockingAll)
	{
		_blockingAll = blockingAll;

		if (_blockingAll)
		{
			_owner.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
		}
		else
		{
			_owner.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
		}
	}
}