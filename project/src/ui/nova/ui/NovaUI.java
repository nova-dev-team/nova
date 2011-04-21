package nova.ui;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

/**
 * User interface for Nova.
 * 
 * @author santa
 * 
 */
public class NovaUI implements Application {

	Window window = null;

	@Override
	public void startup(Display display, Map<String, String> properties)
			throws Exception {
		BXMLSerializer bxmlSerializer = new BXMLSerializer();
		this.window = (Window) bxmlSerializer.readObject(getClass()
				.getResource("bxml/mainframe.bxml"));
		this.window.open(display);
	}

	@Override
	public boolean shutdown(boolean optional) throws Exception {
		if (window != null) {
			window.close();
		}
		return false;
	}

	@Override
	public void resume() throws Exception {
		// do nothing
	}

	@Override
	public void suspend() throws Exception {
		// do nothing
	}

	public static void main(String[] args) {
		DesktopApplicationContext.main(NovaUI.class, args);
	}
}
