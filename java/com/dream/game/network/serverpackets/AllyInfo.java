package com.dream.game.network.serverpackets;

import java.util.Collection;

import com.dream.game.datatables.sql.ClanTable;
import com.dream.game.model.ClanInfo;
import com.dream.game.model.L2Clan;

public class AllyInfo extends L2GameServerPacket
{
	private final String _name;
	private final int _total;
	private final int _online;
	private final String _leaderC;
	private final String _leaderP;
	private final ClanInfo[] _allies;

	public AllyInfo(int allianceId)
	{
		final L2Clan leader = ClanTable.getInstance().getClan(allianceId);
		_name = leader.getAllyName();
		_leaderC = leader.getName();
		_leaderP = leader.getLeaderName();

		final Collection<L2Clan> allies = ClanTable.getInstance().getClanAllies(allianceId);
		_allies = new ClanInfo[allies.size()];
		int idx = 0, total = 0, online = 0;
		for (final L2Clan clan : allies)
		{
			final ClanInfo ci = new ClanInfo(clan);
			_allies[idx++] = ci;
			total += ci.getTotal();
			online += ci.getOnline();
		}

		_total = total;
		_online = online;
	}

	public ClanInfo[] getAllies()
	{
		return _allies;
	}

	public String getLeaderC()
	{
		return _leaderC;
	}

	public String getLeaderP()
	{
		return _leaderP;
	}

	public String getName()
	{
		return _name;
	}

	public int getOnline()
	{
		return _online;
	}

	public int getTotal()
	{
		return _total;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xb4);

		writeS(_name);
		writeD(_total);
		writeD(_online);
		writeS(_leaderC);
		writeS(_leaderP);

		writeD(_allies.length);
		for (final ClanInfo aci : _allies)
		{
			writeS(aci.getClan().getName());
			writeD(0x00);
			writeD(aci.getClan().getLevel());
			writeS(aci.getClan().getLeaderName());
			writeD(aci.getTotal());
			writeD(aci.getOnline());
		}
	}

}