package nova.master.api.messages;

public class RegisterVdiskMessage {

    /**
     * No-arg constructore for gson.
     */
    public RegisterVdiskMessage() {

    }

    public RegisterVdiskMessage(String displayName, String fileName,
            String imageType, String osFamily, String osName, String imgPath,
            String description) {
        this.displayName = displayName;
        this.fileName = fileName;
        this.imageType = imageType;
        this.osFamily = osFamily;
        this.osName = osName;
        this.imgPath = imgPath;
        this.description = description;
    }

    public String displayName, fileName, imageType;
    public String osFamily, osName, description;
    public String imgPath;

}
