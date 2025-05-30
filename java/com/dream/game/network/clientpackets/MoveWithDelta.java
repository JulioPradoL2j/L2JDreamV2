package com.dream.game.network.clientpackets;

public class MoveWithDelta extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _dx, _dy, _dz;

	@Override
	protected void readImpl()
	{
		_dx = readD();
		_dy = readD();
		_dz = readD();
	}

	@Override
	protected void runImpl()
	{

	}

}