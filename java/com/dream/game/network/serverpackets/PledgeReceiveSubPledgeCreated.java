package com.dream.game.network.serverpackets;

import com.dream.game.model.L2Clan;
import com.dream.game.model.L2Clan.SubPledge;
import com.dream.game.model.L2ClanMember;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private final SubPledge _subPledge;
	private final L2Clan _clan;

	public PledgeReceiveSubPledgeCreated(SubPledge subPledge, L2Clan clan)
	{
		_subPledge = subPledge;
		_clan = clan;
	}

	private String getLeaderName()
	{
		if (_subPledge.getLeaderId() != 0 && _subPledge.getId() != L2Clan.SUBUNIT_ACADEMY)
			if (_clan != null)
			{
				L2ClanMember player = _clan.getClanMember(_subPledge.getLeaderId());
				if (player != null)
					return player.getName();
			}
		return "";
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3F);
		writeD(0x01);
		writeD(_subPledge.getId());
		writeS(_subPledge.getName());
		writeS(getLeaderName());
	}

}