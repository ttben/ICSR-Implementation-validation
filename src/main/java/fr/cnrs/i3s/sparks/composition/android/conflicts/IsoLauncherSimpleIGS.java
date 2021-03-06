package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.Launcher;
import spoon.processing.Processor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IsoLauncherSimpleIGS {
    private static Map<CtClass, Map<CtExecutable, CtBlock>> mapSetterToTheirLines;
    private static List<CtInvocation> callToSimpleSetterToDelete = new ArrayList<>();
    private final String inputPath;
    private final String outputPath;
    private final List<Processor> processors;
    private final Accumulator accumulator;

    static int nbErrors = 0;
    static int nbSetters = 0;

    public static void main(String[] args) throws Exception {
        String inputPath = "/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/";
        String outputPath = "target/spooned-iso-simple-igs";
        Accumulator accumulator = new Accumulator();

        IGSSimpleCapture igsSimpleCapture = new IGSSimpleCapture();
        callToSimpleSetterToDelete = igsSimpleCapture.igsInvocation;
        IGSInlinerSimple igsInliner = new IGSInlinerSimple(accumulator, igsSimpleCapture.igsInvocation);

        //List<Processor> processors = Arrays.asList(igsSimpleCapture, new AddNPGuard(accumulator),igsInliner, new IGSInlinerSimplePostCondition(callToSimpleSetterToDelete));
        List<Processor> processors = Arrays.asList(igsSimpleCapture,igsInliner,new AddNPGuard(accumulator), new IGSInlinerSimplePostCondition(callToSimpleSetterToDelete));

        IsoLauncherSimpleIGS seqLauncher = new IsoLauncherSimpleIGS(inputPath, processors, outputPath, accumulator);
        seqLauncher.apply();
    }

    IsoLauncherSimpleIGS(String inputPath, List<Processor> processors, String outputPath, Accumulator accumulator) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.processors = processors;
        this.accumulator = accumulator;
    }


    public void apply() throws Exception {
        String desc = "code";
        for (Processor p : processors) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
        }

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


        Map<Processor, Map<CtElement, CtElement>> modificationsMap = accumulator.getModificationsMap();
        for (Processor currentProcessor : modificationsMap.keySet()) {
            Map<CtElement, CtElement> currentMap = modificationsMap.get(currentProcessor);
            for (CtElement e : currentMap.keySet()) {
                CtElement ctElement = currentMap.get(e);
                if (ctElement instanceof CtStatementList) {
                    CtElement original = e;
                    for (CtStatement c : ((CtStatementList) ctElement).getStatements()) {
                        ((CtStatement) e).insertAfter(c);
                        e = c;
                    }
                    original.delete();
                } else if (e instanceof CtMethod) { // setter

                    CtIf shouldBeACtIf = (CtIf) currentMap.get(e);
                    ((CtMethod) e).setBody(shouldBeACtIf);
                    //e.setParent(null);
                }
            }
        }

        //spoon.getModel().processWith(new IGSInlinerAlternativePostCondition(mapSetterToTheirLines));
        spoon.getModel().processWith(new IGSInlinerSimplePostCondition(callToSimpleSetterToDelete));
        spoon.prettyprint();
    }
}

