import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class test1 {
    public static void main(String[] args) {
        try {
            PrintWriter outpw = new PrintWriter(new FileWriter("test1111.txt"));
            outpw.println("test111111111111111111111");
            outpw.close();
        } catch (IOException e1) {

        }
    }

}
