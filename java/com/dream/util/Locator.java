package com.dream.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Locator
{
	
	private Locator()
	{
	}
	
	
	public static File getClassSource(Class<?> c)
	{
		final String classResource = c.getName().replace('.', '/') + ".class";
		return getResourceSource(c.getClassLoader(), classResource);
	}
	
	
	public static File getResourceSource(ClassLoader classLoader, String resource)
	{
		ClassLoader c = classLoader;
		if (c == null)
		{
			c = Locator.class.getClassLoader();
		}
		URL url = null;
		if (c == null)
		{
			url = ClassLoader.getSystemResource(resource);
		}
		else
		{
			url = c.getResource(resource);
		}
		if (url != null)
		{
			final String u = url.toString();
			if (u.startsWith("jar:file:"))
			{
				final int pling = u.indexOf('!');
				final String jarName = u.substring(4, pling);
				return new File(fromURI(jarName));
			}
			else if (u.startsWith("file:"))
			{
				final int tail = u.indexOf(resource);
				final String dirName = u.substring(0, tail);
				return new File(fromURI(dirName));
			}
		}
		return null;
	}
	

	public static String fromURI(String uriValue)
	{
		String uri = uriValue;
		URL url = null;
		try
		{
		
			url = URI.create(uri).toURL();
		}
		catch (MalformedURLException emYouEarlEx)
		{
			
		}
		
		if ((url == null) || !("file".equals(url.getProtocol())))
		{
			throw new IllegalArgumentException("Can only handle valid file: URIs");
		}
		
		final StringBuilder buf = new StringBuilder(url.getHost());
		if (buf.length() > 0)
		{
			buf.insert(0, File.separatorChar).insert(0, File.separatorChar);
		}
		final String file = url.getFile();
		final int queryPos = file.indexOf('?');
		buf.append((queryPos < 0) ? file : file.substring(0, queryPos));
		uri = buf.toString().replace('/', File.separatorChar);
		if ((File.pathSeparatorChar == ';') && uri.startsWith("\\") && (uri.length() > 2) && Character.isLetter(uri.charAt(1)) && (uri.lastIndexOf(':') > -1))
		{
			uri = uri.substring(1);
		}
		
		return decodeUri(uri);
	}
	

	private static String decodeUri(String uri)
	{
		if (uri.indexOf('%') == -1)
		{
			return uri;
		}
		final StringBuilder sb = new StringBuilder();
		final CharacterIterator iter = new StringCharacterIterator(uri);
		for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
		{
			if (c == '%')
			{
				final char c1 = iter.next();
				if (c1 != CharacterIterator.DONE)
				{
					final int i1 = Character.digit(c1, 16);
					final char c2 = iter.next();
					if (c2 != CharacterIterator.DONE)
					{
						final int i2 = Character.digit(c2, 16);
						sb.append((char) ((i1 << 4) + i2));
					}
				}
			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
	}
	

	
}