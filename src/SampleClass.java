import java.io.IOException;
import java.util.ArrayDeque;

public class SampleClass {
    public static void main(String[] args) throws IOException {
        byte buf = 0;
        ArrayDeque<Byte> stack = new ArrayDeque<>();

        buf = (byte) System.in.read();

        stack.push(buf);

        buf = stack.pop();

        System.out.println((char) buf);
    }
}
