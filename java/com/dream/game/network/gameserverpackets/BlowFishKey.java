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
package com.dream.game.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

public class BlowFishKey extends GameServerBasePacket
{
	private final static Logger _log = Logger.getLogger(BlowFishKey.class.getName());

	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		writeC(0x00);
		byte[] encrypted = null;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encrypted = rsaCipher.doFinal(blowfishKey);

			writeD(encrypted.length);
			writeB(encrypted);
		}
		catch (GeneralSecurityException e)
		{
			_log.fatal("Error While encrypting blowfish key for transmision (Crypt error)", e);
		}
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}