package com.dream.game.model.actor.knownlist;

import java.util.Map;

import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.L2Decoy;
import com.dream.game.model.actor.L2Npc;
import com.dream.game.model.actor.L2Summon;
import com.dream.game.model.actor.instance.L2BoatInstance;
import com.dream.game.model.actor.instance.L2DoorInstance;
import com.dream.game.model.actor.instance.L2ItemInstance;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.actor.instance.L2PetInstance;
import com.dream.game.model.actor.instance.L2StaticObjectInstance;
import com.dream.game.network.serverpackets.CharInfo;
import com.dream.game.network.serverpackets.DeleteObject;
import com.dream.game.network.serverpackets.DoorInfo;
import com.dream.game.network.serverpackets.DoorStatusUpdate;
import com.dream.game.network.serverpackets.DropItem;
import com.dream.game.network.serverpackets.GetOnVehicle;
import com.dream.game.network.serverpackets.L2GameServerPacket;
import com.dream.game.network.serverpackets.NpcInfo;
import com.dream.game.network.serverpackets.PetInfo;
import com.dream.game.network.serverpackets.PetItemList;
import com.dream.game.network.serverpackets.PrivateStoreMsgBuy;
import com.dream.game.network.serverpackets.PrivateStoreMsgSell;
import com.dream.game.network.serverpackets.RecipeShopMsg;
import com.dream.game.network.serverpackets.RelationChanged;
import com.dream.game.network.serverpackets.SpawnItem;
import com.dream.game.network.serverpackets.StaticObject;
import com.dream.game.network.serverpackets.VehicleInfo;
import com.dream.util.SingletonMap;

public class PcKnownList extends PlayableKnownList
{
	private Map<Integer, Integer> _knownRelations;

	public PcKnownList(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{

		if (!super.addKnownObject(object, dropper))
			return false;

		final L2PcInstance active_char = getActiveChar();
		if (active_char == null)
		{
			return false;
		}

		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
		{
			active_char.sendPacket(new SpawnItem(object));
		}
		else
		{
			L2GameServerPacket infoPacket = object.getInfoPacket();
			if (infoPacket != null)
			{
				active_char.sendPacket(infoPacket);
			}
			else if (object instanceof L2ItemInstance)
			{
				if (dropper != null)
				{
					active_char.sendPacket(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
				}
				else
				{
					active_char.sendPacket(new SpawnItem(object));
				}
			}
			else if (object instanceof L2DoorInstance)
			{
				active_char.sendPacket(new DoorInfo((L2DoorInstance) object, false));
				active_char.sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
			}
			else if (object instanceof L2BoatInstance)
			{
				if (!active_char.isInBoat())
					if (object != active_char.getBoat())
					{
						active_char.sendPacket(new VehicleInfo((L2BoatInstance) object));
						((L2BoatInstance) object).sendVehicleDeparture(active_char);
					}
			}
			else if (object instanceof L2StaticObjectInstance)
			{
				active_char.sendPacket(new StaticObject((L2StaticObjectInstance) object));
			}
			else if (object instanceof L2Decoy)
			{
				active_char.sendPacket(new CharInfo((L2Decoy) object));
			}
			else if (object instanceof L2Npc)
			{
				active_char.sendPacket(new NpcInfo((L2Npc) object));
			}
			else if (object instanceof L2Summon)
			{
				L2Summon summon = (L2Summon) object;

				if (active_char == summon.getOwner())
				{
					active_char.sendPacket(new PetInfo(summon, 0));

					summon.updateEffectIcons();

					if (summon instanceof L2PetInstance)
					{
						active_char.sendPacket(new PetItemList((L2PetInstance) summon));
					}
				}
				else
				{
					active_char.sendPacket(new NpcInfo(summon, 0));
				}
			}
			else if (object instanceof L2PcInstance)
			{
				L2PcInstance otherPlayer = (L2PcInstance) object;

				if (!active_char.showTraders())
					if (otherPlayer.inPrivateMode())
						return true;

				if (otherPlayer.isInBoat())
				{
					otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition());
					active_char.sendPacket(new CharInfo(otherPlayer));

					active_char.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
				}
				else
				{
					active_char.sendPacket(new CharInfo(otherPlayer));
				}

				getKnownRelations().put(object.getObjectId(), -1);
				RelationChanged.sendRelationChanged(otherPlayer, getActiveChar());

				if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
				{
					active_char.sendPacket(new PrivateStoreMsgSell(otherPlayer));
				}
				else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY)
				{
					active_char.sendPacket(new PrivateStoreMsgBuy(otherPlayer));
				}
				else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE)
				{
					active_char.sendPacket(new RecipeShopMsg(otherPlayer));
				}
			}
			if (object instanceof L2Character)
			{
				L2Character obj = (L2Character) object;
				if (obj.getAI() != null)
				{
					obj.getAI().describeStateToPlayer(getActiveChar());
				}

			}

		}
		return true;
	}

	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) _activeChar;
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		final int knownlistSize = getKnownObjects().size();

		if (knownlistSize <= 25)
			return 4200;

		if (knownlistSize <= 35)
			return 3600;

		if (knownlistSize <= 70)
			return 2910;

		return 2310;
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		final int knownlistSize = getKnownObjects().size();

		if (knownlistSize <= 25)
			return 3500;

		if (knownlistSize <= 35)
			return 2900;

		if (knownlistSize <= 70)
			return 2300;

		return 1700;
	}

	public final Map<Integer, Integer> getKnownRelations()
	{
		if (_knownRelations == null)
		{
			_knownRelations = new SingletonMap<Integer, Integer>().setShared();
		}
		return _knownRelations;
	}

	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownRelations().clear();
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		final L2PcInstance active_char = getActiveChar();

		L2PcInstance object_char = null;
		if (object instanceof L2PcInstance)
		{
			object_char = (L2PcInstance) object;
		}

		if (object_char != null && !active_char.isGM())
		{
			if (!object_char.getAppearance().isInvisible() && !object_char.inObserverMode())
			{
				active_char.sendPacket(new DeleteObject(object));
			}
			else if (object_char.isGM() && object_char.getAppearance().isInvisible() && !object_char.isTeleporting())
			{
				active_char.sendPacket(new DeleteObject(object));
			}
		}
		else
		{
			active_char.sendPacket(new DeleteObject(object));
		}
		return true;
	}
}