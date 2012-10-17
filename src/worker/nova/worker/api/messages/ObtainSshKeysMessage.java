package nova.worker.api.messages;

public class ObtainSshKeysMessage {
    public ObtainSshKeysMessage() {
    }

    public ObtainSshKeysMessage(String vClusterName, String vmName) {
        this.vClusterName = vClusterName;
        this.vmName = vmName;
    }

    public String vClusterName, vmName;

}
