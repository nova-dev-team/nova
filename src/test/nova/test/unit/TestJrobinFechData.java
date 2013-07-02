package nova.test.unit;

import nova.common.util.RRDTools;

public class TestJrobinFechData extends Thread {

    public void run() {
        while (true) {
            double[][] test = RRDTools.getMonitorInfo(1);
            System.out
                    .println(".....................................................");
            for (int i = 0; i < test.length; i++) {
                for (int j = 0; j < test[i].length; j++) {
                    System.out.print(test[i][j]);
                    System.out.print(" ");
                }
                System.out.println(" ");
            }
            System.out
                    .println("..............................................................");
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TestJrobinFechData test = new TestJrobinFechData();
        test.start();
    }
}
