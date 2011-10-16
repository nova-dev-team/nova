package nova.ui;

import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

/**
 * User interface for Nova.
 * 
 * @author santa
 * 
 */
public class NovaUI implements Application, Bindable {

    Window window = null;

    PushButton pushButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location,
            Resources resources) {
        System.out.println("called");
        this.pushButton = (PushButton) namespace.get("pushButton");
        this.pushButton.getButtonPressListeners().add(
                new ButtonPressListener() {

                    @Override
                    public void buttonPressed(Button arg0) {
                        System.out.println("pushed");
                        pushButton.setButtonData("pushed");
                    }
                });
    }

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
