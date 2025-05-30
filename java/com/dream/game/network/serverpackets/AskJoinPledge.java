package com.dream.game.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
	private final int _requestorObjId;
	private final String _subPledgeName;
	private final int _pledgeType;
	private final String _pledgeName;

	public AskJoinPledge(int requestorObjId, String subPledgeName, int pledgeType, String pledgeName)
	{
		_requestorObjId = requestorObjId;
		_subPledgeName = subPledgeName;
		_pledgeType = pledgeType;
		_pledgeName = pledgeName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x32);
		writeD(_requestorObjId);
		if (_subPledgeName != null)
		{
			writeS(_pledgeType > 0 ? _subPledgeName : _pledgeName);
		}
		if (_pledgeType != 0)
		{
			writeD(_pledgeType);
		}
		writeS(_pledgeName);
	}

}