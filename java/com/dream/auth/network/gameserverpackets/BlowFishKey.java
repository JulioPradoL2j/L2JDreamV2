/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.dream.auth.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.dream.auth.network.clientpackets.ClientBasePacket;

public class BlowFishKey extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(BlowFishKey.class.getName());
	byte[] _key;

	public BlowFishKey(byte[] decrypt, RSAPrivateKey privateKey)
	{
		super(decrypt);
		int size = readD();
		byte[] tempKey = readB(size);
		try
		{
			byte[] tempDecryptKey;
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			tempDecryptKey = rsaCipher.doFinal(tempKey);
			int i = 0;
			int len = tempDecryptKey.length;
			for (; i < len; i++)
				if (tempDecryptKey[i] != 0)
				{
					break;
				}
			_key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, _key, 0, len - i);
		}
		catch (GeneralSecurityException e)
		{
			_log.fatal("Error While decrypting blowfish key (RSA)", e);
		}

	}

	public byte[] getKey()
	{
		return _key;
	}

}