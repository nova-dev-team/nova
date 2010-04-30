import mx.rpc.events.ResultEvent;
import com.adobe.serialization.json.JSON;
import nova.GlobalConst;
import mx.controls.Alert;

// logout from Nova
private function logout(): void {
	navigateToURL(new URLRequest(GlobalConst.logoutURL), "_self");
}

