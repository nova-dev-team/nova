package nova.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Config file abstraction.
 * 
 * @author santa
 * 
 */
public class Conf extends Properties {

	/**
	 * Log4j logger.
	 */
	static Logger logger = Logger.getLogger(Utils.class);

	private Conf() {
		File confDir = new File(Utils.pathJoin(Utils.NOVA_HOME, "conf"));
		for (File f : confDir.listFiles()) {
			if (f.isFile() && f.getName().startsWith("nova.")
					&& f.getName().endsWith(".properties")) {
				logger.info("Loading config from " + f.getAbsolutePath());
				Properties subConf = new Properties();

				try {
					FileInputStream fis = new FileInputStream(f);
					subConf.load(fis);
					fis.close();
				} catch (FileNotFoundException e) {
					logger.fatal("Error loading config files", e);
					System.exit(1);
				} catch (IOException e) {
					logger.fatal("Error loading config files", e);
					System.exit(1);
				}

				this.putAll(subConf);
			}
		}
	}

	private static Conf conf = new Conf();

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 1907859519429882573L;

	/**
	 * Set default value for a Conf object.
	 * 
	 * @param key
	 *            The config name.
	 * @param value
	 *            The default value.
	 */
	public static void setDefaultValue(String key, Object value) {
		if (conf.containsKey(key) == false) {
			conf.put(key, value);
		}
	}

	/**
	 * Get a string config.
	 * 
	 * @param key
	 *            The config name.
	 * @return The config info, as string. If not found, null will be returned.
	 */
	public static String getString(String key) {
		if (conf.containsKey(key)) {
			return conf.get(key).toString();
		} else {
			return null;
		}
	}

	/**
	 * Get a integer config.
	 * 
	 * @param key
	 *            The config name.
	 * @return The config value, as integer. If not found, null will be
	 *         returned.
	 * @throws NumberFormatException
	 *             If the number is not valid.
	 */
	public static Integer getInteger(String key) throws NumberFormatException {
		if (conf.containsKey(key)) {
			return Integer.parseInt(conf.get(key).toString());
		} else {
			return null;
		}
	}

}
