package nova.master.models;

/**
 * Model of a software package.
 * 
 * @author santa
 * 
 */
public class SoftwarePackage {

	/**
	 * Name of the package, for users.
	 */
	String displayName;

	/**
	 * Name of the supported OS.
	 */
	String os;

	/**
	 * Name of the folder which contains all files of this software package.
	 */
	String folderName;
}
