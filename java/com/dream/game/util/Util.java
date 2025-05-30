package com.dream.game.util;

import java.io.File;
import java.io.FileFilter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.dream.Config;
import com.dream.game.model.L2Object;
import com.dream.game.model.actor.L2Character;
import com.dream.game.model.actor.instance.L2PcInstance;
import com.dream.game.model.world.Location;
import com.dream.game.network.ThreadPoolManager;
import com.dream.game.taskmanager.MemoryWatchDog;
import com.dream.lang.L2Thread;
import com.dream.tools.util.CustomFileNameFilter;
import com.dream.util.ValueSortMap;

public final class Util
{
	public interface Filter<T>
	{
		public boolean match(T o);
	}

	private final static Logger _log = Logger.getLogger(Util.class);

	public final static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}

		return angleTarget;
	}

	public final static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	public final static double calculateDistance(int x1, int y1, int z1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}

	public final static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;

		if (includeZAxis)
		{
			double dz = z1 - z2;

			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}

		return Math.sqrt(dx * dx + dy * dy);
	}

	public final static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return 1000000;
		return calculateDistance(obj1.getPosition().getX(), obj1.getPosition().getY(), obj1.getPosition().getZ(), obj2.getPosition().getX(), obj2.getPosition().getY(), obj2.getPosition().getZ(), includeZAxis);
	}

	public final static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}

		return (int) (angleTarget * 182.044444444);
	}

	public final static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
		{
			angleTarget += 360;
		}

		return (int) (angleTarget * 182.044444444);
	}

	public final static int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	public static double calculatePercentage(double number, double percentage)
	{
		double values = number * percentage;
		double tmp = values / 100;

		return tmp;
	}

	public static String capitalizeFirst(String str)
	{
		str = str.trim();

		if (str.length() > 0 && Character.isLetter(str.charAt(0)))
			return str.substring(0, 1).toUpperCase() + str.substring(1);

		return str;
	}

	public static String capitalizeWords(String str)
	{
		char[] charArray = str.toCharArray();
		String result = "";

		charArray[0] = Character.toUpperCase(charArray[0]);

		for (int i = 0; i < charArray.length; i++)
		{
			if (Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}

			result += Character.toString(charArray[i]);
		}

		return result;
	}

	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return false;

		if (range == -1)
			return true;

		int rad = 0;
		if (obj1 instanceof L2Character)
		{
			try
			{
				rad += ((L2Character) obj1).getTemplate().getCollisionRadius();
			}
			catch (NullPointerException npe)
			{
				rad += 20;
			}
		}
		if (obj2 instanceof L2Character)
		{
			try
			{
				rad += ((L2Character) obj2).getTemplate().getCollisionRadius();
			}
			catch (NullPointerException npe)
			{
				rad += 20;
			}
		}

		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();

		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = dx * dx + dy * dy + dz * dz;

			return d <= range * range + 2 * range * rad + rad * rad;
		}

		double d = dx * dx + dy * dy;

		return d <= range * range + 2 * range * rad + rad * rad;
	}

	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return false;
		if (radius == -1)
			return true; // not limited

		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();

		if (includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}

		return dx * dx + dy * dy <= radius * radius;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
			if (element == obj)
				return true;

		return false;
	}

	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
			if (element == obj)
				return true;

		return false;
	}

	public final static int convertDegreeToClientHeading(double degree)
	{
		if (degree < 0)
		{
			degree += 360;
		}

		return (int) (degree * 182.044444444);
	}

	public final static double convertHeadingToDegree(int clientHeading)
	{
		double degree = clientHeading / 182.044444444;

		return degree;
	}

	public static int convertMinutesToMiliseconds(int minutesToConvert)
	{
		return minutesToConvert * 60000;
	}

	public static int convertMinutesToSeconds(int minutesToConvert)
	{
		return minutesToConvert * 60;
	}

	public static double convertPercentageByMultipler(double multiplerX100)
	{
		return 100 - multiplerX100;
	}

	public static int convertSecondsToMiliseconds(int secondsToConvert)
	{
		return secondsToConvert * 1000;
	}

	public static <T> Collection<T> filter(Collection<T> l, Filter<T> filter)
	{
		List<T> result = new ArrayList<>();
		Iterator<T> it = l.iterator();
		while (it.hasNext())
		{
			T item = it.next();
			if (filter.match(item))
			{
				result.add(item);
			}
		}
		return result;
	}

	public static Location findPointForDistance(Location src, Location dst, double distance)
	{
		double fullDistance = calculateDistance(src.getX(), src.getY(), src.getZ(), dst.getX(), dst.getY());
		if (Math.abs(distance) >= fullDistance)
			return distance > 0 ? dst : src;
		if (distance < 0)
		{
			distance = fullDistance + distance;
		}
		if (distance >= fullDistance)
			return dst;
		double t = distance / fullDistance;
		double l = dst.getX() - src.getX();
		double m = dst.getY() - src.getY();
		double n = dst.getZ() - src.getZ();
		int x = (int) (src.getX() + l * t);
		int y = (int) (src.getY() + m * t);
		int z = (int) (src.getZ() + n * t);
		return new Location(x, y, z, calculateHeadingFrom(src.getX(), src.getY(), dst.getX(), dst.getY()));
	}

	public static String formatNumber(double value)
	{
		return NumberFormat.getInstance(Locale.ENGLISH).format(value);
	}

	public static long gc(int i, int delay)
	{
		long freeMemBefore = MemoryWatchDog.getMemFree();
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		while (--i > 0)
		{
			try
			{
				Thread.sleep(delay);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			rt.gc();
		}
		rt.runFinalization();
		return MemoryWatchDog.getMemFree() - freeMemBefore;
	}

	public static File[] getDatapackFiles(String dirname, String extention)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);

		if (!dir.exists())
		{
			_log.error("Unable to Find File " + dir + extention + ", Please Update Your Datapack!");
			return null;
		}

		CustomFileNameFilter filter = new CustomFileNameFilter(extention);

		return dir.listFiles(filter);
	}

	public static String[] getMemUsage()
	{
		return L2Thread.getMemoryUsageStatistics();
	}

	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}

	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
	}

	public static String implodeString(Collection<String> strCollection, String strDelim)
	{
		return implodeString(strCollection.toArray(new String[strCollection.size()]), strDelim);
	}

	public static String implodeString(String[] strArray, String strDelim)
	{
		String result = "";

		for (String strValue : strArray)
		{
			result += strValue + strDelim;
		}

		return result;
	}

	public static boolean isAlphaNumeric(String text)
	{
		if (text == null)
			return false;

		boolean result = true;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++)
			if (!Character.isLetterOrDigit(chars[i]))
			{
				result = false;
				break;
			}

		return result;
	}

	public static boolean isDigit(String text)
	{
		if (text == null || text.isEmpty())
			return false;
		for (char c : text.toCharArray())
			if (!Character.isDigit(c))
				return false;
		return true;
	}

	public static boolean isValidName(String text, String regex)
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(regex);
		}
		catch (PatternSyntaxException e)
		{
			pattern = Pattern.compile(".*");
		}

		Matcher regexp = pattern.matcher(text);

		return regexp.matches();
	}

	public static boolean isValidPlayerName(String text)
	{
		return isValidName(text, "^[A-Za-z0-9]{1,16}$");
	}

	public static File[] listDirFiles(String path, final String extension)
	{
		File F = new File(path);
		File[] files = F.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().toLowerCase().endsWith(extension);
			}
		});

		return files;
	}

	public static File[] listFolders(String path)
	{
		File F = new File(path);
		File[] files = F.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().toLowerCase().endsWith("");
			}
		});

		return files;
	}

	private static void printCpuInfo()
	{
		_log.info("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		_log.info("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
	}

	public static void printGeneralSystemInfo()
	{
		printSystemTime();
		printOSInfo();
		printCpuInfo();
		printRuntimeInfo();
		printJreInfo();
		printJvmInfo();
	}

	private static void printJreInfo()
	{
		_log.info("Java Platform Information");
		_log.info("Java Runtime  Name: " + System.getProperty("java.runtime.name"));
		_log.info("Java Version: " + System.getProperty("java.version"));
		_log.info("Java Class Version: " + System.getProperty("java.class.version"));
	}

	private static void printJvmInfo()
	{
		_log.info("Virtual Machine Information (JVM)");
		_log.info("JVM Name: " + System.getProperty("java.vm.name"));
		_log.info("JVM installation directory: " + System.getProperty("java.home"));
		_log.info("JVM version: " + System.getProperty("java.vm.version"));
		_log.info("JVM Vendor: " + System.getProperty("java.vm.vendor"));
		_log.info("JVM Info: " + System.getProperty("java.vm.info"));
	}

	private static void printOSInfo()
	{
		_log.info("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		_log.info("OS Arch: " + System.getProperty("os.arch"));
	}

	private static void printRuntimeInfo()
	{
		_log.info("Runtime Information");
		_log.info("Current Free Heap Size: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb");
		_log.info("Current Heap Size: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb");
		_log.info("Maximum Heap Size: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb");
	}

	private static void printSystemTime()
	{
		Date dateInfo = new Date();

		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");

		String dayInfo = df.format(dateInfo);

		_log.info("System Time: " + dayInfo);
	}

	public static String reverseColor(String color)
	{
		char[] ch1 = color.toCharArray();
		char[] ch2 = new char[6];
		ch2[0] = ch1[4];
		ch2[1] = ch1[5];
		ch2[2] = ch1[2];
		ch2[3] = ch1[3];
		ch2[4] = ch1[0];
		ch2[5] = ch1[1];

		return new String(ch2);
	}

	public static float roundTo(float val, int numPlaces)
	{
		if (numPlaces <= 1)
			return Math.round(val);

		float exponent = (float) Math.pow(10, numPlaces);

		return Math.round(val * exponent) / exponent;
	}

	public static Map<Integer, Integer> sortMap(Map<Integer, Integer> map, boolean asc)
	{
		ValueSortMap vsm = new ValueSortMap();

		return vsm.sortThis(map, asc);
	}
}