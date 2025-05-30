package com.dream.game.network.clientpackets;

import com.dream.game.network.serverpackets.PackageSendableList;

public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectID;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}

	@Override
	public void runImpl()
	{
		sendPacket(new PackageSendableList(getClient().getActiveChar(), _objectID));
	}

}