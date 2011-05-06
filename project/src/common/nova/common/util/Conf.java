package nova.common.util;

import java.util.Properties;

/**
 * Config file abstraction.
 * 
 * @author santa
 * 
 */
public class Conf extends Properties {

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
	public void setDefaultValue(String key, Object value) {
		if (this.containsKey(key) == false) {
			this.put(key, value);
		}
	}

	/**
	 * Get a string config.
	 * 
	 * @param key
	 *            The config name.
	 * @return The config info, as string. If not found, null will be returned.
	 */
	public String getString(String key) {
		if (this.containsKey(key)) {
			return this.get(key).toString();
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
	public Integer getInteger(String key) throws NumberFormatException {
		if (this.containsKey(key)) {
			return Integer.parseInt(this.get(key).toString());
		} else {
			return null;
		}
	}

}
