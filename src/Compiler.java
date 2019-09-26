import compiler.BytecodeCompiler;
import parsermanual.ParseTree;
import parsermanual.ParserManual;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        List<String> lineList = Files.readAllLines(new File("./HelloWorld.chef").toPath());
        String[] lines = lineList.toArray(new String[0]);

        ParserManual parser = new ParserManual();
        ParseTree tree = parser.parse(lines);

        exit();

        BytecodeCompiler bc = new BytecodeCompiler();
        byte[] bytecode = bc.compile(tree);

        FileOutputStream writer = new FileOutputStream("Out.class");
        writer.write(bytecode);
        writer.close();
    }

    private static void exit() {
        if (Math.random() < 1) {
            System.exit(0);
        }
    }
}
