package nova.test.functional.master;

import junit.framework.Assert;
import nova.master.models.Pnode;
import nova.master.models.Pnode.Status;

import org.junit.Test;

public class TestPnodeDB {

    @Test
    public void testSave() {
        Pnode pnode = new Pnode();
        pnode.setHostname("eagle's PC");
        pnode.setIp("166.111.131.155");
        pnode.setMacAddress("a2:d5:45:5f:53:ad");
        pnode.setPort(4000);
        pnode.setStatus(Status.RUNNING);

        pnode.save();
        Pnode pnodeLoad = Pnode.findById(pnode.getId());
        System.out.println(pnodeLoad);
        Assert.assertEquals(pnode, pnodeLoad);

        // Pnode.delete(pnodeLoad);
    }

    @Test
    public void testGetAllPnode() {
        for (Pnode pnode : Pnode.all()) {
            System.out.println(pnode);
        }
    }

    @Test
    public void testFindPnodeByIp() {
        Pnode pnode = Pnode.findByIp("127.0.0.1");
        System.out.println(pnode);

        Pnode pnode2 = Pnode.findByIp("127.0.0.3");
        System.out.println(pnode2);
    }
}
