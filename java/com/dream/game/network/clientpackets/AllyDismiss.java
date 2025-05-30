package com.dream.game.network.clientpackets;

import com.dream.Config;
import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.L2Clan;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.SystemMessageId;

public class AllyDismiss extends L2GameClientPacket
{
	private String _clanName;

	@Override
	protected void readImpl()
	{
		_clanName = readS();
	}

	@Override
	protected void runImpl()
	{
		if (_clanName == null)
			return;
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (player.getClan() == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		L2Clan leaderClan = player.getClan();
		if (leaderClan.getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if (!player.isClanLeader() || leaderClan.getClanId() != leaderClan.getAllyId())
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		L2Clan clan = ClanTable.getInstance().getClanByName(_clanName);
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.CLAN_DOESNT_EXISTS);
			return;
		}
		if (clan.getClanId() == leaderClan.getClanId())
		{
			player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW);
			return;
		}
		if (clan.getAllyId() != leaderClan.getAllyId())
		{
			player.sendPacket(SystemMessageId.DIFFERENT_ALLIANCE);
			return;
		}

		long currentTime = System.currentTimeMillis();
		leaderClan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000L, L2Clan.PENALTY_TYPE_DISMISS_CLAN); // 24*60*60*1000 = 86400000
		leaderClan.updateClanInDB();

		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.setAllyCrestId(0);
		clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000L, L2Clan.PENALTY_TYPE_CLAN_DISMISSED); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();

		player.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE);

		player.getClan().setAllyCrestId(0);
		for (L2PcInstance member : player.getClan().getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}

}