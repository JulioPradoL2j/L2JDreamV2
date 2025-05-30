package com.dream.game.network.clientpackets;

import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.GMHennaInfo;
import com.dream.game.network.serverpackets.GMViewCharacterInfo;
import com.dream.game.network.serverpackets.GMViewItemList;
import com.dream.game.network.serverpackets.GMViewPledgeInfo;
import com.dream.game.network.serverpackets.GMViewQuestInfo;
import com.dream.game.network.serverpackets.GMViewSkillInfo;
import com.dream.game.network.serverpackets.GMViewWarehouseWithdrawList;

public class RequestGMCommand extends L2GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		L2PcInstance activeChar = getClient().getActiveChar();

		if (player == null || activeChar == null || !activeChar.allowAltG())
			return;

		switch (_command)
		{
			case 1:
			{
				sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2:
			{
				if (player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				else
				{
					activeChar.sendMessage(player.getName() + " not in slaughter.");
				}
				break;
			}
			case 3:
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4:
			{
				sendPacket(new GMViewQuestInfo(player));
				break;
			}
			case 5:
			{
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMHennaInfo(player));
				break;
			}
			case 6:
			{
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}
	}

}