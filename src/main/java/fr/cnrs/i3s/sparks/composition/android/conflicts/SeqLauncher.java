package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.Launcher;
import spoon.processing.Processor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SeqLauncher {

    private final String inputPath;
    private final String outputPath;
    private final List<Processor> processors;

    public static void main(String[] args) {
        String inputPath = "/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/";
        String outputPath = "target/spooned-seq-igs-guard-checkIGS";
        IGSInlinerAlternative igsInliner = new IGSInlinerAlternative();
        List<Processor> processors = Arrays.asList(igsInliner, new AddNPGuard(), new IGSInlinerAlternativePostCondition(igsInliner.mapsSetterToTheirInlines));

        SeqLauncher seqLauncher = new SeqLauncher(inputPath, processors, outputPath);
        seqLauncher.apply();

        // ---

        outputPath = "target/spooned-seq-guard-igs-checkIGS";
        processors = Arrays.asList(new AddNPGuard(), new IGSInlinerAlternative());

        seqLauncher = new SeqLauncher(inputPath, processors, outputPath);
        seqLauncher.apply();
    }

    SeqLauncher(String inputPath, List<Processor> processors, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.processors = processors;
    }

    private void apply() {

        System.out.print("Applying processors:");

        String desc = "code";
        for (Processor p : processors) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
            System.out.print(" " + p.getClass().getSimpleName());
        }

        System.out.println();

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
}
