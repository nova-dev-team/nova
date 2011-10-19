package nova.test.functional.master;

import junit.framework.TestCase;
import nova.master.models.Vdisk;

import org.junit.Test;

public class TestVdiskDB extends TestCase {

    @Test
    public void testSave() {
        Vdisk vdisk = new Vdisk();
        vdisk.setFileName("blah");
        vdisk.setDisplayName("haha");
        vdisk.setDiskFormat(".qcow2");
        vdisk.setOsFamily("linux");
        vdisk.setDescription("lalalala");
        vdisk.setOsName("Ubuntu 9.10");
        vdisk.setSoftList("blahblah");

        vdisk.save();

        Vdisk vdiskRead = Vdisk.findById(vdisk.getId());
        System.out.print("\n\nFileName: " + vdiskRead.getFileName()
                + "\nDisplayName: " + vdiskRead.getDisplayName()
                + "\nDiskFormat: " + vdiskRead.getDiskFormat() + "\nOsFamily: "
                + vdiskRead.getOsFamily() + "\nDescription: "
                + vdiskRead.getDescription() + "\nOsName: "
                + vdiskRead.getOsName() + "\nSoftList: "
                + vdiskRead.getSoftList() + "\n\n");

        Vdisk.delete(vdiskRead);

    }

}