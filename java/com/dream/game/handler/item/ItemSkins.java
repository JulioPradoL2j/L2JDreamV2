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
package com.dream.game.handler.item;

import com.dream.game.handler.IItemHandler;
import com.dream.game.model.DressMeData;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.network.serverpackets.MagicSkillUse;

public class ItemSkins implements IItemHandler
{

	private static final int ITEM_IDS[] =
	{
		30000,
		30001,
		30002,
		30003,
		30004,
		30005,
		30006,
		30007,
		30008,
		30009,
		30010,
		30011,
		30012,
		30013,
		30014,
		30015,
		30016,
		30017,
		30018,
		30019,
		30020,
		30021,
		30022,
		30023,
		30024,
		30025,
		30026,
		30027,
		30028,
		30029,
		30030
	};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean par)
	{

	}

	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemId = item.getItemId();

		if (itemId == 30000) // Dark
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15000);
			activeChar.getDressMeData().setHairId(15001);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30001) // Light
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15002);
			activeChar.getDressMeData().setHairId(15003);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30002) // Pirate
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15004);
			activeChar.getDressMeData().setHairId(15005);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30003) // Muskeeter
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15006);
			activeChar.getDressMeData().setHairId(15007);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30004) // Wizard
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15008);
			activeChar.getDressMeData().setHairId(15009);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30005) // Archer
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15010);
			activeChar.getDressMeData().setHairId(15011);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30006) // Ninja
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15012);
			activeChar.getDressMeData().setHairId(15013);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30007) // Beleth
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15014);
			activeChar.getDressMeData().setHairId(15015);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30008) // Knight
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15016);
			activeChar.getDressMeData().setHairId(15017);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30009) // Healer
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15018);
			activeChar.getDressMeData().setHairId(15019);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30010) // Lilith
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15020);
			activeChar.getDressMeData().setHairId(15021);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30011) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15022);
			activeChar.getDressMeData().setHairId(15023);

			activeChar.broadcastUserInfo();
		}

		if (itemId == 30012) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15024);
			activeChar.getDressMeData().setHairId(15025);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30013) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15026);
			activeChar.getDressMeData().setHairId(15027);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30014) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15028);
			activeChar.getDressMeData().setHairId(15029);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30015) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15030);
			activeChar.getDressMeData().setHairId(15031);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30016) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15032);
			activeChar.getDressMeData().setHairId(15033);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30017) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15034);
			activeChar.getDressMeData().setHairId(15035);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30018) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15036);
			activeChar.getDressMeData().setHairId(15037);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30019) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15038);
			activeChar.getDressMeData().setHairId(15039);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30020) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15040);
			activeChar.getDressMeData().setHairId(15041);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30021) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15042);
			activeChar.getDressMeData().setHairId(15043);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30022) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15044);
			activeChar.getDressMeData().setHairId(15045);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30023) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15046);
			activeChar.getDressMeData().setHairId(15047);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30024) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15048);
			activeChar.getDressMeData().setHairId(15049);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30025) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15050);
			activeChar.getDressMeData().setHairId(15051);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30026) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15052);
			activeChar.getDressMeData().setHairId(15053);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30027) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15054);
			activeChar.getDressMeData().setHairId(15055);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30028) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15056);
			activeChar.getDressMeData().setHairId(15057);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30029) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(15058);
			activeChar.getDressMeData().setHairId(15059);

			activeChar.broadcastUserInfo();
		}
		if (itemId == 30030) //
		{
			if (activeChar.getDressMeData() == null)
			{
				DressMeData dmd = new DressMeData();
				activeChar.setDressMeData(dmd);
			}
			activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2046, 1, 4000, 0, true));

			activeChar.setDressMeEnabled(true);

			activeChar.getDressMeData().setChestId(6408);
			activeChar.getDressMeData().setHairId(0);

			activeChar.broadcastUserInfo();
		}
	}

	/* (non-Javadoc)
	 * @see com.dream.game.handler.IItemHandler#getItemIds() */
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

}