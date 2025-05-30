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
package com.dream.game.datatables.xml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.dream.Config;
import com.dream.game.manager.CastleManager;
import com.dream.game.manager.TownManager;
import com.dream.game.model.actor.instance.L2MerchantInstance;
import com.dream.game.model.entity.Town;
import com.dream.game.model.entity.siege.Castle;

public class MerchantPriceConfigTable
{
	public static final class MerchantPriceConfig
	{
		private final int _id;
		private final String _name;
		private final int _baseTax;
		private final Castle _castle;
		private final int _zoneId;

		public MerchantPriceConfig(int id, String name, int baseTax, Castle castle, int zoneId)
		{
			_id = id;
			_name = name;
			_baseTax = baseTax;
			_castle = castle;
			_zoneId = zoneId;
		}

		public int getBaseTax()
		{
			return _baseTax;
		}

		public double getBaseTaxRate()
		{
			return _baseTax / 100.0;
		}

		public Castle getCastle()
		{
			return _castle;
		}

		public double getCastleTaxRate()
		{
			return hasCastle() ? getCastle().getTaxRate() : 0.0;
		}

		public int getId()
		{
			return _id;
		}

		public String getName()
		{
			return _name;
		}

		public int getTotalTax()
		{
			return hasCastle() ? getCastle().getTaxPercent() + getBaseTax() : getBaseTax();
		}

		public double getTotalTaxRate()
		{
			return getTotalTax() / 100.0;
		}

		public int getZoneId()
		{
			return _zoneId;
		}

		public boolean hasCastle()
		{
			return getCastle() != null;
		}
	}

	private static final Logger _log = Logger.getLogger(MerchantPriceConfigTable.class.getName());

	private static MerchantPriceConfigTable _instance;

	private static final String MPCS_FILE = "MerchantPriceConfig.xml";

	public static MerchantPriceConfigTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MerchantPriceConfigTable();
		}

		return _instance;
	}

	private static MerchantPriceConfig parseMerchantPriceConfig(Node n)
	{
		if (n.getNodeName().equals("priceConfig"))
		{
			int id, baseTax, castleId, zoneId = -1;
			String name;
			Castle castle = null;

			Node node = n.getAttributes().getNamedItem("id");
			if (node == null)
				throw new IllegalStateException("Must define the priceConfig 'id'");

			id = Integer.parseInt(node.getNodeValue());

			node = n.getAttributes().getNamedItem("name");
			if (node == null)
				throw new IllegalStateException("Must define the priceConfig 'name'");

			name = node.getNodeValue();

			node = n.getAttributes().getNamedItem("baseTax");
			if (node == null)
				throw new IllegalStateException("Must define the priceConfig 'baseTax'");

			baseTax = Integer.parseInt(node.getNodeValue());

			node = n.getAttributes().getNamedItem("castleId");
			if (node != null)
			{
				castleId = Integer.parseInt(node.getNodeValue());
				castle = CastleManager.getInstance().getCastleById(castleId);
			}

			node = n.getAttributes().getNamedItem("zoneId");
			if (node != null)
			{
				zoneId = Integer.parseInt(node.getNodeValue());
			}

			return new MerchantPriceConfig(id, name, baseTax, castle, zoneId);
		}
		return null;
	}

	private final Map<Integer, MerchantPriceConfig> _mpcs = new HashMap<>();

	private MerchantPriceConfig _defaultMpc;

	private MerchantPriceConfigTable()
	{
		try
		{
			loadXML();
			_log.info("Merchant Price Data: Loaded " + _mpcs.size() + " merchant price configs.");
		}
		catch (Exception e)
		{
			_log.fatal("Failed loading MerchantPriceConfigTable. Reason: " + e.getMessage(), e);
		}
	}

	public MerchantPriceConfig getMerchantPriceConfig(int id)
	{
		return _mpcs.get(id);
	}

	public MerchantPriceConfig getMerchantPriceConfig(L2MerchantInstance npc)
	{
		Town _town = TownManager.getInstance().getTown(npc);
		if (_town != null)
		{
			for (MerchantPriceConfig mpc : _mpcs.values())
				if (_town.getCastle() == mpc.getCastle())
					return mpc;
		}
		return _defaultMpc;
	}

	public void loadXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/xml/world/" + MPCS_FILE);
		if (file.exists())
		{
			int defaultPriceConfigId;
			Document doc = factory.newDocumentBuilder().parse(file);

			Node n = doc.getDocumentElement();
			Node dpcNode = n.getAttributes().getNamedItem("defaultPriceConfig");
			if (dpcNode == null)
				throw new IllegalStateException("merchantPriceConfig must define an 'defaultPriceConfig'");

			defaultPriceConfigId = Integer.parseInt(dpcNode.getNodeValue());

			MerchantPriceConfig mpc;
			for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
			{
				mpc = parseMerchantPriceConfig(n);
				if (mpc != null)
				{
					_mpcs.put(mpc.getId(), mpc);
				}
			}

			MerchantPriceConfig defaultMpc = this.getMerchantPriceConfig(defaultPriceConfigId);
			if (defaultMpc == null)
				throw new IllegalStateException("'defaultPriceConfig' points to an non-loaded priceConfig");

			_defaultMpc = defaultMpc;
		}
	}
}