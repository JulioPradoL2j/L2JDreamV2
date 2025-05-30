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
package com.dream.game.model.actor.instance;

import com.dream.Config;
import com.dream.game.ai.CtrlIntention;
import com.dream.game.ai.L2CharacterAI;
import com.dream.game.cache.HtmCache;
import com.dream.game.manager.CastleManager;
import com.dream.game.model.L2Skill;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.knownlist.StaticObjectKnownList;
import com.dream.game.model.actor.position.L2CharPosition;
import com.dream.game.model.actor.stat.StaticObjStat;
import com.dream.game.model.world.L2World;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.ChairSit;
import com.dream.game.network.serverpackets.MyTargetSelected;
import com.dream.game.network.serverpackets.NpcHtmlMessage;
import com.dream.game.network.serverpackets.ShowTownMap;
import com.dream.game.network.serverpackets.StaticObject;
import com.dream.game.templates.chars.L2CharTemplate;
import com.dream.game.templates.item.L2Weapon;

public class L2StaticObjectInstance extends L2Character
{
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public void doAttack(L2Character target)
		{
		}

		@Override
		public void doCast(L2Skill skill)
		{
		}

		@Override
		public L2StaticObjectInstance getActor()
		{
			return L2StaticObjectInstance.this;
		}

		@Override
		public boolean moveTo(int x, int y, int z)
		{
			return false;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		public void stopMove(L2CharPosition pos)
		{
		}
	}

	public static final int INTERACTION_DISTANCE = 150;
	private final int _staticObjectId;
	private int _meshIndex = 0;
	private int _type = -1;
	private int _x;
	private int _y;

	private String _texture;

	private L2PcInstance actualPersonToSitOn = null;

	public L2StaticObjectInstance(int objectId, L2CharTemplate template, int staticId)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_staticObjectId = staticId;
	}

	@Override
	public void broadcastFullInfoImpl()
	{

	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}

	@Override
	public final StaticObjectKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new StaticObjectKnownList(this);
		}

		return (StaticObjectKnownList) _knownList;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	private int getMapX()
	{
		return _x;
	}

	private int getMapY()
	{
		return _y;
	}

	public int getMeshIndex()
	{
		return _meshIndex;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public final StaticObjStat getStat()
	{
		if (_stat == null)
		{
			_stat = new StaticObjStat(this);
		}

		return (StaticObjStat) _stat;
	}

	public int getStaticObjectId()
	{
		return _staticObjectId;
	}

	public int getType()
	{
		return _type;
	}

	public boolean isBusy()
	{
		return actualPersonToSitOn != null;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (_type < 0)
		{
			_log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + getStaticObjectId());
		}
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

			if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);

				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_type == 2)
			{
				String filename = "data/html/signboard.htm";
				String content = HtmCache.getInstance().getHtm(filename);
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

				if (content == null)
				{
					html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
				}
				else
				{
					html.setHtml(content);
				}

				player.sendPacket(html);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_type == 0)
			{
				player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;

		if (player.isGM())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

			player.sendPacket(new StaticObject(this));

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			StringBuilder html1 = new StringBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>X: " + getX() + "</td></tr>");
			html1.append("<tr><td>Y: " + getY() + "</td></tr>");
			html1.append("<tr><td>Z: " + getZ() + "</td></tr>");
			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Static Object ID: " + getStaticObjectId() + "</td></tr>");
			html1.append("<tr><td>Mesh Index: " + getMeshIndex() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table></body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{

		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void setBusyStatus(L2PcInstance actualPersonToSitOn)
	{
		this.actualPersonToSitOn = actualPersonToSitOn;
	}

	public void setMap(String texture, int x, int y)
	{
		_texture = "town_map." + texture;
		_x = x;
		_y = y;
	}

	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
		broadcastPacket(new StaticObject(this));
	}

	public void setType(int type)
	{
		_type = type;
	}

	@Override
	public void updateAbnormalEffect()
	{

	}

	public boolean useThrone(L2PcInstance player)
	{
		if (actualPersonToSitOn != null && L2World.getInstance().findPlayer(actualPersonToSitOn.getObjectId()) == null)
		{
			setBusyStatus(null);
		}

		if (player.getTarget() != this || getType() != 1 || isBusy())
			return false;

		if (player.getClan() == null || CastleManager.getInstance().getCastle(this) == null || CastleManager.getInstance().getCastleById(player.getClan().getHasCastle()) == null)
			return false;

		if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			return false;

		if (CastleManager.getInstance().getCastle(this) != CastleManager.getInstance().getCastleById(player.getClan().getHasCastle()))
			return false;

		if (Config.ONLY_CLANLEADER_CAN_SIT_ON_THRONE && player.getObjectId() != player.getClan().getLeaderId())
			return false;

		setBusyStatus(player);
		player.setObjectSittingOn(this);

		player.sitDown();
		player.broadcastPacket(new ChairSit(player, getStaticObjectId()));

		return true;
	}
}