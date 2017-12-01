package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.Launcher;
import spoon.processing.Processor;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LambdaLauncher {


    public static void main(String[] args) throws Exception {
        applyOn("/Users/benjaminbenni/Work/interference/src/main/resources/DataTypeField.java");

//        applyOn("/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/export/format/GoogleFitData.java");
    }

    private static void applyOn(String inputPath) {
        IGSInliner igsInliner = new IGSInliner();
        IGSInlinerPostCondition igsInlinerPostCondition = new IGSInlinerPostCondition(igsInliner.mapsSetterToTheirInlines);

        //applyProcs(inputPath, Arrays.asList(igsInliner, new AddNPGuard(),igsInlinerPostCondition), "target/spooned");
        applyProcs(inputPath, Arrays.asList(igsInliner, new AddNPGuard(), igsInlinerPostCondition), "target/spooned-Guard_of_IGS_of_c");
        applyProcs(inputPath, Arrays.asList(new AddNPGuard(), igsInliner, igsInlinerPostCondition), "target/spooned-IGS_of_Guard_of_c");

        /*
        applyProcs(inputPath, Arrays.asList(new AddNPGuard(), new IGSInliner()), "target/spooned-GI");
        applyProcs(inputPath, Arrays.asList(new IGSInliner(), new AddNPGuard()), "target/spooned-IG");
        */

    }


    private static void applyProcs(String inputPath, List<Processor> processors, String outputPath) {
        System.out.print("Order of application:");
        String desc = "code";
        for (Processor p : processors) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
        }
        System.out.println(desc);

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

