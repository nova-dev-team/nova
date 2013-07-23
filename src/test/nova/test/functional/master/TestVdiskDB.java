package nova.test.functional.master;

import junit.framework.TestCase;
import nova.master.models.Vdisk;

import org.junit.Test;

public class TestVdiskDB extends TestCase {

    @Test
    public void testSave() {
        Vdisk vdisk = new Vdisk();
        vdisk.setFileName("linux");
        vdisk.setDisplayName("Ubuntu 11.04");
        vdisk.setDiskFormat("img");
        vdisk.setOsFamily("linux");
        vdisk.setDescription("Ubuntu 11.04 Image");
        vdisk.setOsName("Ubuntu 11.04");
        vdisk.setSoftList("Vim,Eclipse,JDC");

        vdisk.save();

        vdisk = new Vdisk();
        vdisk.setFileName("xp");
        vdisk.setDisplayName("xp");
        vdisk.setDiskFormat("img");
        vdisk.setOsFamily("windows");
        vdisk.setDescription("XP Image");
        vdisk.setOsName("XP");
        vdisk.setSoftList("Vim,Eclipse,JDC");

        vdisk.save();

        Vdisk vdiskRead = Vdisk.findById(vdisk.getId());
        System.out.print("\n\nFileName: " + vdiskRead.getFileName()
                + "\nDisplayName: " + vdiskRead.getDisplayName()
                + "\nDiskFormat: " + vdiskRead.getDiskFormat() + "\nOsFamily: "
                + vdiskRead.getOsFamily() + "\nDescription: "
                + vdiskRead.getDescription() + "\nOsName: "
                + vdiskRead.getOsName() + "\nSoftList: "
                + vdiskRead.getSoftList() + "\n\n");

        // Vdisk.delete(vdiskRead);

    }

    @Test
    public void testGetAllVdisk() {
        for (Vdisk vdisk : Vdisk.all()) {
            System.out.println(vdisk);
        }
    }

}
