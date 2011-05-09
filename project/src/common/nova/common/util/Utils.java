package nova.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.stringtree.Fetcher;
import org.stringtree.finder.StringFinder;
import org.stringtree.template.InlineTemplater;

/**
 * Provides handy utility functions.
 * 
 * @author santa
 * 
 */
public class Utils {

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(Utils.class);

	/**
	 * The home for nova.
	 */
	public static final String NOVA_HOME;

	/*
	 * Static constructor to determine NOVA_HOME.
	 */
	static {
		final String confName = Utils.pathJoin("conf", "nova.properties");
		File cwd = new File(".");
		String folderPath = null;
		try {
			folderPath = cwd.getCanonicalPath();
		} catch (IOException e) {
			logger.error("Failed to determine current working directory", e);
		}

		// find conf file in folders
		while (true) {
			File folder = new File(folderPath);

			String confPath = Utils.pathJoin(folderPath, confName);
			File confFile = new File(confPath);
			if (confFile.exists()) {
				break;
			}

			String parentPath = folder.getParent();
			if (parentPath == null || parentPath.equals(folderPath)) {
				folderPath = null;
				break;
			} else {
				folderPath = parentPath;
			}
		}

		NOVA_HOME = folderPath;
		if (NOVA_HOME == null) {
			System.err
					.println("Fatal error: failed to locate conf/nova.properties!");
			System.exit(1);
		}
	}

	public static void mkdirs(String... paths) {
		String path = Utils.pathJoin(paths);
		File pathFile = new File(path);
		pathFile.mkdirs();
	}

	/**
	 * Join several paths.
	 * 
	 * @param paths
	 *            Arbitrary length arguments, each is a path segment.
	 * @return The joined full path.
	 */
	public static String pathJoin(String... paths) {
		StringBuffer sb = new StringBuffer();

		// normalized path segments
		ArrayList<String> normPaths = new ArrayList<String>();

		for (String path : paths) {
			if (path.equals("")) {
				continue;
			} else {
				normPaths.add(path);
			}
		}

		if (normPaths.size() > 0) {
			sb.append(normPaths.get(0));
			for (int i = 1; i < normPaths.size(); i++) {
				String pathSegment = normPaths.get(i);
				if (pathSegment.startsWith(File.separator)) {
					sb = new StringBuffer();
				} else {
					sb.append(File.separator);
				}
				sb.append(pathSegment);
			}
		}
		return sb.toString();
	}

	/**
	 * Expand from a template. The values are extracted from an object.
	 * 
	 * @param template
	 *            The template string.
	 * @param obj
	 *            The object from which the values should be retrieved.
	 * @return The expanded template.
	 */
	public static String expandTemplate(String template, final Object obj) {
		StringFinder finder = new StringFinder() {

			@SuppressWarnings("rawtypes")
			Class objClass = obj.getClass();

			@Override
			public Object getObject(String key) {
				try {
					Field field = objClass.getDeclaredField(key);
					field.setAccessible(true);
					return field.get(obj);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public Fetcher getUnderlyingFetcher() {
				return null;
			}

			@Override
			public boolean contains(String key) {
				try {
					objClass.getField(key);
					return true;
				} catch (NoSuchFieldException e) {
					return false;
				}
			}

			@Override
			public String get(String key) {
				return getObject(key).toString();
			}
		};

		InlineTemplater templater = new InlineTemplater(finder);
		return templater.expand(template);
	}

	/**
	 * Generate string from a template.
	 * 
	 * @param template
	 *            The template content, place holders are like "${key}".
	 * @param values
	 *            The template values in a map.
	 * @return Generated content.
	 */
	public static String expandTemplate(String template,
			final Map<String, Object> values) {

		StringFinder finder = new StringFinder() {

			@Override
			public Object getObject(String key) {
				return values.get(key);
			}

			@Override
			public Fetcher getUnderlyingFetcher() {
				return null;
			}

			@Override
			public boolean contains(String key) {
				return values.containsKey(key);
			}

			@Override
			public String get(String key) {
				return values.get(key).toString();
			}
		};

		InlineTemplater templater = new InlineTemplater(finder);
		return templater.expand(template);
	}

	/**
	 * Expand strings from a template file.
	 * 
	 * @param fpath
	 *            Path to the template file.
	 * @param values
	 *            The template values in a map.
	 * @return Generated content.
	 */
	public static String expandTemplateFile(String fpath,
			Map<String, Object> values) {
		String template = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					fpath)));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			template = sb.toString();
			br.close();
		} catch (IOException e) {
			logger.error("Failed to read '" + fpath + "'", e);
		}
		return Utils.expandTemplate(template, values);
	}
}
