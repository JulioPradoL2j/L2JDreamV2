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
package com.dream.tools.math;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import org.apache.log4j.Logger;

public class ScrambledKeyPair
{
	private final static Logger _log = Logger.getLogger(ScrambledKeyPair.class);

	public static String getKeyPairs(byte[] key, int len)
	{
		int x = 1;
		char ch;
		String res = "" + (char) key[0];
		while (x < len)
		{
			ch = (char) key[x];
			res = res + (char) (ch ^ 64 ^ key[x - 1]);
			x++;
		}
		return res;
	}

	private static byte[] scrambleModulus(BigInteger modulus)
	{
		byte[] retScrambledModulus = modulus.toByteArray();

		if (retScrambledModulus.length == 0x81 && retScrambledModulus[0] == 0x00)
		{
			byte[] temp = new byte[0x80];
			System.arraycopy(retScrambledModulus, 1, temp, 0, 0x80);
			retScrambledModulus = temp;
		}

		for (int i = 0; i < 4; i++)
		{
			byte temp = retScrambledModulus[0x00 + i];
			retScrambledModulus[0x00 + i] = retScrambledModulus[0x4d + i];
			retScrambledModulus[0x4d + i] = temp;
		}
		for (int i = 0; i < 0x40; i++)
		{
			retScrambledModulus[i] = (byte) (retScrambledModulus[i] ^ retScrambledModulus[0x40 + i]);
		}

		for (int i = 0; i < 4; i++)
		{
			retScrambledModulus[0x0d + i] = (byte) (retScrambledModulus[0x0d + i] ^ retScrambledModulus[0x34 + i]);
		}

		for (int i = 0; i < 0x40; i++)
		{
			retScrambledModulus[0x40 + i] = (byte) (retScrambledModulus[0x40 + i] ^ retScrambledModulus[i]);
		}

		if (_log.isDebugEnabled())
		{
			_log.debug("Modulus was scrambled");
		}

		return retScrambledModulus;
	}

	private KeyPair pair;

	private byte[] scrambledModulus;

	public ScrambledKeyPair(KeyPair Pair)
	{
		pair = Pair;
		scrambledModulus = scrambleModulus(((RSAPublicKey) pair.getPublic()).getModulus());
	}

	public KeyPair getPair()
	{
		return pair;
	}

	public byte[] getScrambledModulus()
	{
		return scrambledModulus;
	}

	public void setPair(KeyPair pair)
	{
		this.pair = pair;
	}

	public void setScrambledModulus(byte[] scrambledModulus)
	{
		this.scrambledModulus = scrambledModulus;
	}
}