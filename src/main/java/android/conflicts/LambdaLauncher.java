package android.conflicts;

import spoon.Launcher;
import spoon.processing.Processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LambdaLauncher {

    static String[] inputs = {
            "/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/export/format/GoogleFitData.java"
    };
    public static void main(String[] args) throws Exception {
        for (String input : inputs) {
            applyOn(input);
        }
    }

    private static void applyOn(String inputPath) {
        applyProcs(inputPath, Arrays.asList(new IGSInliner()), "target/spooned"); // good example with export/format/GoogleFitData.java that has a myDb at null and uses IGS
        //applyProcs(inputPath, Arrays.asList(new MyHMUFixer()), "target/spooned");
       // applyProcs(inputPath, Arrays.asList(new MyHMUFixer(),new AddLambda()), "target/spooned");
        //applyProcs(inputPath, Arrays.asList(), "target/base");
    }


    private static void applyProcs(String inputPath, List<Processor> processors, String outputPath) {
        // create spoon
        Launcher spoon = new Launcher();

        //spoon.getEnvironment().setLevel("ALL");
        // Do not fail if class not found
        spoon.getEnvironment().setNoClasspath(true);
        spoon.getEnvironment().setCommentEnabled(false);

        String version = inputPath;
        version = version.replaceAll("[^0-9]+", " ");
        String descVersion = "";
        for (String s : version.trim().split(" ")) {
            descVersion += "-" + s;
        }

        // set the output folder
        spoon.getEnvironment().getDefaultFileGenerator().setOutputDirectory(new File(outputPath + descVersion));

        // set the project root
        spoon.addInputResource(inputPath);

        for (Processor p : processors) {
            spoon.addProcessor(p);
        }

        spoon.buildModel();
        spoon.process();
        spoon.prettyprint();
    }

    private static void applyProc(String inputPath, Processor p, String outputPath) {
        applyProcs(inputPath, Arrays.asList(p), outputPath);
    }

}

