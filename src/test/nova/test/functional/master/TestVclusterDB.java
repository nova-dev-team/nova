package nova.test.functional.master;

import junit.framework.TestCase;
import nova.master.models.Vcluster;

import org.junit.Test;

public class TestVclusterDB extends TestCase {

    @Test
    public void testSave() {
        Vcluster vcluster = new Vcluster();
        vcluster.setClusterName("Professor A's Cluster");
        vcluster.setClusterSize(100);
        vcluster.setFristIp("10.0.0.1");
        vcluster.setOsUsername("Professor A");
        vcluster.setOsPassword("os");
        vcluster.setSshPrivateKey("sshprivatekey");
        vcluster.setSshPublicKey("sshpublickey");
        vcluster.setUserId(2013054856);

        vcluster.save();

        Vcluster vclusterRead = Vcluster.findById(vcluster.getId());

        System.out.println(vclusterRead);

        // Vcluster.delete(vclusterRead);

    }

    @Test
    public void testGetAllVnode() {
        for (Vcluster vcluster : Vcluster.all()) {
            System.out.println(vcluster);
        }
    }
}
