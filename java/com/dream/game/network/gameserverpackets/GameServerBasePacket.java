package com.dream.game.network.gameserverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public abstract class GameServerBasePacket
{
	public final static Logger _log = Logger.getLogger(GameServerBasePacket.class);

	private final ByteArrayOutputStream _bao;

	protected GameServerBasePacket()
	{
		_bao = new ByteArrayOutputStream();
	}

	public byte[] getBytes()
	{
		writeD(0x00);

		int padding = _bao.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; i++)
			{
				writeC(0x00);
			}
		}

		return _bao.toByteArray();
	}

	public abstract byte[] getContent() throws IOException;

	public int getLength()
	{
		return _bao.size() + 2;
	}

	protected void writeB(byte[] array)
	{
		try
		{
			_bao.write(array);
		}
		catch (IOException e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	protected void writeC(int value)
	{
		_bao.write(value & 0xff);
	}

	protected void writeD(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
		_bao.write(value >> 16 & 0xff);
		_bao.write(value >> 24 & 0xff);
	}

	protected void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xff));
		_bao.write((int) (value >> 8 & 0xff));
		_bao.write((int) (value >> 16 & 0xff));
		_bao.write((int) (value >> 24 & 0xff));
		_bao.write((int) (value >> 32 & 0xff));
		_bao.write((int) (value >> 40 & 0xff));
		_bao.write((int) (value >> 48 & 0xff));
		_bao.write((int) (value >> 56 & 0xff));
	}

	protected void writeH(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
	}

	protected void writeS(String text)
	{
		try
		{
			if (text != null)
			{
				_bao.write(text.getBytes("UTF-16LE"));
			}
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}

		_bao.write(0);
		_bao.write(0);
	}
}
