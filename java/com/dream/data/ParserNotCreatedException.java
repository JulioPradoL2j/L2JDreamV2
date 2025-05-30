package com.dream.data;

public class ParserNotCreatedException extends Exception
{
	private static final long serialVersionUID = 6517876994935741910L;

	public ParserNotCreatedException()
	{
		super("Parser could not be created!");
	}
}