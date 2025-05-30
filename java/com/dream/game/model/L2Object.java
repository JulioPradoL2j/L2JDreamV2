package com.dream.game.model;

import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Playable;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.knownlist.ObjectKnownList;
import com.dream.game.model.actor.poly.ObjectPoly;
import com.dream.game.model.actor.position.ObjectPosition;
import com.dream.game.model.world.L2World;
import com.dream.game.model.world.L2WorldRegion;
import com.dream.game.model.world.Location;
import com.dream.game.network.serverpackets.ActionFailed;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.lang.L2Entity;

import javolution.text.TextBuilder;

public abstract class L2Object extends L2Entity
{
	public static final L2Object[] EMPTY_ARRAY = new L2Object[0];

	public final static L2PcInstance getActingPlayer(L2Object obj)
	{
		return obj == null ? null : obj.getActingPlayer();
	}

	public final static L2Summon getActingSummon(L2Object obj)
	{
		return obj == null ? null : obj.getActingSummon();
	}

	private String _name;
	private ObjectPoly _poly;
	private ObjectPosition _position;

	private boolean _inWorld;

	protected L2Object(int objectId)
	{
		super(objectId);
		_name = "";
	}

	public void decayMe()
	{
		L2WorldRegion reg = getPosition().getWorldRegion();

		synchronized (this)
		{
			getPosition().clearWorldRegion();
		}
		_inWorld = false;
		L2World.getInstance().removeVisibleObject(this, reg);
		L2World.getInstance().removeObject(this);
	}

	public void firstSpawn()
	{
		onSpawn();
	}

	public L2PcInstance getActingPlayer()
	{
		return null;
	}

	public L2PcInstance getPlayer()
	{
		return null;
	}

	public boolean isPlayer()
	{
		return false;
	}

	public boolean isItem()
	{
		return false;
	}

	public L2Playable getPlayable()
	{
		return null;
	}

	public boolean isPlayable()
	{
		return false;
	}

	public L2Character getCharacter()
	{
		return null;
	}

	public boolean isCharacter()
	{
		return false;
	}

	public L2Npc getNpc()
	{
		return null;
	}

	public boolean isNpc()
	{
		return false;
	}

	public L2Summon getActingSummon()
	{
		return null;
	}

	public int getColHeight()
	{
		return 50;
	}

	public int getColRadius()
	{
		return 50;
	}

	public int getHeading()
	{
		return 0;
	}

	public L2GameServerPacket getInfoPacket()
	{
		return null;
	}

	public ObjectKnownList getKnownList()
	{
		return ObjectKnownList.getInstance();
	}

	public Location getLoc()
	{
		return new Location(getX(), getY(), getZ(), 0);
	}

	public final String getName()
	{
		return _name;
	}

	public final ObjectPoly getPoly()
	{
		if (_poly == null)
		{
			_poly = new ObjectPoly();
		}
		return _poly;
	}

	public final ObjectPosition getPosition()
	{
		if (_position == null)
		{
			_position = new ObjectPosition(this);
		}
		return _position;
	}

	public Integer getPrimaryKey()
	{
		return getObjectId();
	}

	public L2WorldRegion getWorldRegion()
	{
		return getPosition().getWorldRegion();
	}

	public final int getX()
	{
		return getPosition().getX();
	}

	public final int getY()
	{
		return getPosition().getY();
	}

	public final int getZ()
	{
		return getPosition().getZ();
	}

	public boolean isAttackable()
	{
		return false;
	}

	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public boolean isInFunEvent()
	{
		L2PcInstance player = getActingPlayer();

		return player != null && player.isInFunEvent();
	}

	public boolean isInWorld()
	{
		return _inWorld;
	}

	public final boolean isVisible()
	{
		return getPosition().getWorldRegion() != null;
	}

	public void onAction(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onActionShift(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void onSpawn()
	{
	}

	public void reset()
	{
	}

	public void setName(String name)
	{
		_name = name == null ? "" : name.intern();
	}

	public final void setXYZ(int x, int y, int z)
	{
		getPosition().setXYZ(x, y, z);
	}

	public final void spawnMe()
	{
		spawnMe(false);
	}

	private void spawnMe(boolean firstspawn)
	{
		synchronized (this)
		{
			getPosition().updateWorldRegion();
		}

		L2World.getInstance().storeObject(this);

		getPosition().getWorldRegion().addVisibleObject(this);

		L2World.getInstance().addVisibleObject(this, null);
		_inWorld = true;
		if (firstspawn)
		{
			firstSpawn();
		}
		else
		{
			onSpawn();
		}
	}

	public final void spawnMe(int x, int y, int z)
	{
		spawnMe(x, y, z, false);
	}

	public final void spawnMe(int x, int y, int z, boolean firstspawn)
	{
		synchronized (this)
		{
			getPosition().setWorldPosition(x, y, z);
		}

		spawnMe(firstspawn);
	}

	public void toggleVisible()
	{
		if (isVisible())
		{
			decayMe();
		}
		else
		{
			spawnMe();
		}
	}

	@Override
	public String toString()
	{
		TextBuilder tb = TextBuilder.newInstance();
		tb.append("(");
		tb.append(getClass().getSimpleName());
		tb.append(") ");
		tb.append(getObjectId());
		tb.append(" - ");
		tb.append(getName());

		try
		{
			return tb.toString();
		}
		finally
		{
			TextBuilder.recycle(tb);
		}
	}

}