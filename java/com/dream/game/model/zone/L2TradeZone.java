package com.dream.game.model.zone;

import org.w3c.dom.Node;

public class L2TradeZone extends L2DefaultZone
{
	private boolean _canCraft;
	private boolean _canSell;
	private boolean _canBuy;

	public boolean canBuy()
	{
		return _canBuy;
	}

	public boolean canCrfat()
	{
		return _canCraft;
	}

	public boolean canSell()
	{
		return _canSell;
	}

	@Override
	protected void parseZoneDetails(Node zn)
	{
		for (Node n = zn.getFirstChild(); n != null; n = n.getNextSibling())
			if (n.getNodeName().equals("craft"))
			{
				_canCraft = Boolean.parseBoolean(n.getAttributes().getNamedItem("enabled").getNodeValue());
			}
			else if (n.getNodeName().equals("sell"))
			{
				_canSell = Boolean.parseBoolean(n.getAttributes().getNamedItem("enabled").getNodeValue());
			}
			else if (n.getNodeName().equals("buy"))
			{
				_canBuy = Boolean.parseBoolean(n.getAttributes().getNamedItem("enabled").getNodeValue());
			}
	}
}
