package nova.test.functional.master;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;

public class TestMakeDir extends TestCase {

    @Test
    public void testMakeDir() {

        createFolder("/home/zhaox09");

    }

    // create a new file folder
    public void createFolder(String folderPath) {
        try {
            String filePath = folderPath;
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.mkdir();
            }
        } catch (Exception e) {
            System.out.println("Create new file folder @ error");
            e.printStackTrace();
        }
    }

    // delete a file folder
    public void deleteFolder(String folderPath) {
        try {
            String filePath = folderPath;
            File delPath = new File(filePath);
            delPath.delete();
        } catch (Exception e) {
            System.out.println("Delete file folder @ error");
            e.printStackTrace();
        }
    }
}
