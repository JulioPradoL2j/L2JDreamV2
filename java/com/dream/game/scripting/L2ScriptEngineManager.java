package com.dream.game.scripting;

import com.dream.Config;
import com.dream.game.util.Util;
import com.dream.util.JarUtils;
import com.l2jserver.script.jython.JythonScriptEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.log4j.Logger;

public final class L2ScriptEngineManager
{
	private static final Logger _log = Logger.getLogger(L2ScriptEngineManager.class.getName());

	private static L2ScriptEngineManager INSTANCE;

	public static File SCRIPT_FOLDER;

	public static CompiledScript compile(ScriptEngine engine, String script) throws ScriptException
	{
		if (!(engine instanceof Compilable))
			return null;
		return ((Compilable) engine).compile(script);
	}

	public static Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return L2ScriptEngineManager.eval(engine, script, null);
	}

	public static Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if (engine instanceof Compilable && Config.COMPILE_SCRIPTS)
		{
			Compilable eng = (Compilable) engine;
			CompiledScript cs = eng.compile(script);
			return context != null ? cs.eval(context) : cs.eval();
		}

		return context != null ? engine.eval(script, context) : engine.eval(script);
	}

	public static String getClassForFile(File script)
	{
		String path = script.getAbsolutePath();
		String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		if (path.startsWith(scpPath))
		{
			int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx);
		}
		return null;
	}

	public static L2ScriptEngineManager getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new L2ScriptEngineManager();
		}
		return INSTANCE;
	}

	public static ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}

	private static Object loadBinaryClassCache(ObjectInputStream is) throws IOException, ClassNotFoundException
	{
		return is.readObject();
	}

	public static void reportScriptFileError(File script, ScriptException e)
	{
		String dir = script.getParent();
		String name = script.getName() + ".error.log";
		if (dir != null)
		{
			File file = new File(dir + "/" + name);

			FileOutputStream fos = null;
			try
			{
				if (!file.exists())
				{
					file.createNewFile();
				}

				fos = new FileOutputStream(file);
				String errorHeader = "Error on: " + file.getCanonicalPath() + "\r\nLine: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + "\r\n\r\n";
				fos.write(errorHeader.getBytes());
				fos.write(e.getMessage().getBytes());
				_log.warn("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
			}
			catch (IOException ioe)
			{
				_log.warn("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage(), ioe);
			}
			finally
			{
				try
				{
					if (fos != null)
					{
						fos.close();
					}
				}
				catch (Exception e1)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			_log.warn("Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() + "Additionally failed when trying to write an error report on script directory.");
		}
	}

	private final Map<String, ScriptEngine> _nameEngines = new HashMap<>();

	private final Map<String, ScriptEngine> _extEngines = new HashMap<>();

	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<>();

	private String _currentLoadingScript = null;

	private final boolean VERBOSE_LOADING = false;

	private final boolean PURGE_ERROR_LOG = true;

	private L2ScriptEngineManager()
	{
		SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		_log.info("Initializing Script Engine Manager.");

		for (ScriptEngineFactory factory : factories)
		{
			try
			{
				ScriptEngine engine = factory.getScriptEngine();
				boolean reg = false;
				for (String name : factory.getNames())
				{
					ScriptEngine existentEngine = _nameEngines.get(name);
					if (existentEngine != null)
					{
						double engineVer = Double.parseDouble(factory.getEngineVersion());
						double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());

						if (engineVer <= existentEngVer)
						{
							continue;
						}
					}
					reg = true;
					_nameEngines.put(name, engine);
				}

				if (reg)
				{
					_log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
				}

				for (String ext : factory.getExtensions())
				{
					_extEngines.put(ext, engine);
				}
			}
			catch (Exception e)
			{
				_log.warn("Failed initializing factory. ", e);
			}
		}

		preConfigure();
	}

	public Object eval(String engineName, String script) throws ScriptException
	{
		return this.eval(engineName, script, null);
	}

	public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
			throw new ScriptException("No engine registered with name (" + engineName + ")");

		return L2ScriptEngineManager.eval(engine, script, context);
	}

	public void executeAllScriptsInDirectory(File dir) throws IOException, ClassNotFoundException
	{
		this.executeAllScriptsInDirectory(dir, false, 0);
	}

	public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth) throws IOException, ClassNotFoundException
	{
		this.executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0);
	}

	private void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, int currentDepth) throws IOException, ClassNotFoundException
	{
		if (dir.isDirectory())
		{
			for (File file : dir.listFiles())
				if (file.isDirectory() && recurseDown && maxDepth > currentDepth)
				{
					if (VERBOSE_LOADING)
					{
						_log.info("Entering folder: " + file.getName());
					}
					this.executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1);
				}
				else if (file.isFile())
				{
					try
					{
						String name = file.getName();
						int lastIndex = name.lastIndexOf('.');
						String extension;
						if (lastIndex != -1)
						{
							extension = name.substring(lastIndex + 1);
							ScriptEngine engine = getEngineByExtension(extension);
							if (engine != null)
							{
								this.executeScript(engine, file);
							}
						}
					}
					catch (FileNotFoundException e)
					{
						_log.error(e.getMessage(), e);
					}
					catch (ScriptException e)
					{
						reportScriptFileError(file, e);
					}
				}
		}
		else
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
	}

	public void executeScript(File file) throws ScriptException, IOException, ClassNotFoundException
	{
		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		String extension;
		if (lastIndex != -1)
		{
			extension = name.substring(lastIndex + 1);
		}
		else
			throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");

		ScriptEngine engine = getEngineByExtension(extension);
		if (engine == null)
		{
			if (Config.DEBUG)
				throw new ScriptException("No engine registered for extension (" + extension + ")");
			return;
		}

		this.executeScript(engine, file);
	}

	public void executeScript(ScriptEngine engine, File file) throws ScriptException, IOException, ClassNotFoundException
	{

		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		String extension = name.substring(lastIndex);
		File compiledScript = new File(file.getAbsolutePath().replace(extension, ".cs"));
		if (compiledScript.exists() && compiledScript.lastModified() > file.lastModified())
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(compiledScript));
			Object o = ois.readObject();
			ois.close();
			if (o instanceof CompiledScript)
			{
				ScriptContext context = new SimpleScriptContext();
				context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, getEngineByExtension("py"), ScriptContext.ENGINE_SCOPE);
				setCurrentLoadingScript(file.getAbsolutePath());
				try
				{
					((CompiledScript) o).eval(context);
				}
				finally
				{
					setCurrentLoadingScript(null);
				}
				return;
			}
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		if (VERBOSE_LOADING)
		{
			_log.info("Loading Script: " + file.getAbsolutePath());
		}

		if (PURGE_ERROR_LOG)
		{
			name = file.getAbsolutePath() + ".error.log";
			File errorLog = new File(name);
			if (errorLog.isFile())
			{
				errorLog.delete();
			}
		}

		File f = new File("./game.jar");
		String path = SCRIPT_FOLDER.getAbsolutePath() + File.pathSeparator + f.getAbsolutePath();
		f = new File("./cachedir/cachedir");
		if (f.exists())
		{
			path = f.getAbsolutePath() + File.pathSeparator + path;
		}

		if (engine instanceof Compilable && Config.COMPILE_SCRIPTS)
		{
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
			context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("classpath", path, ScriptContext.ENGINE_SCOPE);
			context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, engine, ScriptContext.ENGINE_SCOPE);

			setCurrentLoadingScript(file.getAbsolutePath());
			ScriptContext ctx = engine.getContext();
			try
			{
				engine.setContext(context);
				Compilable eng = (Compilable) engine;
				CompiledScript cs = eng.compile(reader);

				cs.eval(context);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(compiledScript));
				oos.writeObject(cs);
				oos.close();
			}
			finally
			{
				engine.setContext(ctx);
				setCurrentLoadingScript(null);
				context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
			}
		}
		else
		{
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
			context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("classpath", path, ScriptContext.ENGINE_SCOPE);
			context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			setCurrentLoadingScript(file.getAbsolutePath());
			try
			{
				engine.eval(reader, context);
			}
			catch (Exception e)
			{
				_log.error("Error loading script " + Util.getRelativePath(SCRIPT_FOLDER, f), e);
			}
			finally
			{
				setCurrentLoadingScript(null);
				engine.getContext().removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				engine.getContext().removeAttribute("sourcepath", ScriptContext.ENGINE_SCOPE);
				engine.getContext().removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
			}
		}
	}

	public void executeScript(String engineName, File file) throws ScriptException, IOException, ClassNotFoundException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
			throw new ScriptException("No engine registered with name (" + engineName + ")");

		this.executeScript(engine, file);
	}

	protected String getCurrentLoadingScript()
	{
		return _currentLoadingScript;
	}

	public ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}

	private ScriptEngine getEngineByName(String name)
	{
		return _nameEngines.get(name);
	}

	public ScriptContext getScriptContext(String engineName)
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
			throw new IllegalStateException("No engine registered with name (" + engineName + ")");

		return L2ScriptEngineManager.getScriptContext(engine);
	}

	public List<ScriptManager<?>> getScriptManagers()
	{
		return _scriptManagers;
	}

	@SuppressWarnings(
	{
		"deprecation"
	})
	private void loadJar(File f) throws IOException
	{
		int loaded = 0;
		JarFile jar = new JarFile(f);
		Enumeration<JarEntry> jarfiles = jar.entries();
		JarUtils.addURL(f.toURL());
		while (jarfiles.hasMoreElements())
		{
			JarEntry je = jarfiles.nextElement();
			if (!je.isDirectory())
				if (je.getName().endsWith(".class"))
				{
					try
					{
						Class<?> clazz = Class.forName(je.getName().replace(".class", "").replace("/", "."));
						if (ManagedScript.class.isAssignableFrom(clazz))
						{
							Constructor<?> ctor = clazz.getConstructor();
							if (ctor != null)
							{
								setCurrentLoadingScript(null);
								ctor.newInstance();
								loaded++;
							}
						}
					}
					catch (Exception e)
					{
						_log.warn("Script Engine Manager: Error loading " + je.getName(), e);
					}
				}
				else if (je.getName().endsWith(".cs"))
				{
					try
					{

						ObjectInputStream ois = new ObjectInputStream(jar.getInputStream(je));
						Object o = loadBinaryClassCache(ois);
						ois.close();

						if (o instanceof CompiledScript)
						{
							ScriptContext context = new SimpleScriptContext();
							context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, getEngineByExtension("py"), ScriptContext.ENGINE_SCOPE);
							setCurrentLoadingScript(jar.getName() + "::");
							try
							{
								((CompiledScript) o).eval(context);
								loaded++;
							}
							finally
							{
								setCurrentLoadingScript(null);
							}
						}
					}
					catch (Exception e)
					{
						_log.warn("Script Engine Manager: Error loading " + je.getName(), e);
					}
				}
		}
		_log.info("Script Engine Manager: Loaded " + loaded + " script(s) from " + f.getName());
	}

	public void loadScripts() throws IOException
	{
		File f = new File(Config.DATAPACK_ROOT, "data/scripts");
		loadScripts(f, true);
		loadScripts(f, false);

	}

	private void loadScripts(File f, boolean jaronly) throws IOException
	{
		if (f.exists())
			if (f.isFile())
			{
				if ((f.getName().endsWith(".java") || f.getName().endsWith(".py")) && !jaronly)
				{
					try
					{
						executeScript(f);
					}
					catch (ScriptException sce)
					{
						reportScriptFileError(f, sce);
					}
					catch (Exception e)
					{
						_log.warn("Failed loading: " + f.getCanonicalPath() + ": " + e);
						e.printStackTrace();
					}
				}
				else if (f.getName().endsWith(".jar") && jaronly)
				{
					loadJar(f);
				}
			}
			else if (f.isDirectory())
			{
				for (File file : f.listFiles())
				{
					try
					{
						loadScripts(file, jaronly);
					}
					catch (IOException e)
					{

					}
				}
			}
	}

	private void preConfigure()
	{

		String dataPackDirForwardSlashes = SCRIPT_FOLDER.getPath().replaceAll("\\\\", "/");
		String configScript = "import sys;sys.path.insert(0,'" + dataPackDirForwardSlashes + "');";
		try
		{
			this.eval("jython", configScript);
		}
		catch (ScriptException e)
		{
			_log.error("Failed preconfiguring jython: " + e.getMessage());
		}
	}

	public void registerScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.add(manager);
	}

	public void removeScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.remove(manager);
	}

	protected void setCurrentLoadingScript(String currentLoadingScript)
	{
		_currentLoadingScript = currentLoadingScript;
	}
}