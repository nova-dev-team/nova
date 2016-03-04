package nova.master.models;

//author: @Herb
//The switch for load balance.

public class Bswitch {

    String name = null;

    private Bswitch() {
    }

    private static volatile Bswitch instance = null;

    public static Bswitch getInstance() {
        if (instance == null) {
            synchronized (Bswitch.class) {
                if (instance == null) {
                    instance = new Bswitch();
                }
            }
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void printInfo() {
        System.out.println("the name is " + name);
    }

}