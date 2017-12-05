package fr.cnrs.i3s.sparks.composition.android.conflicts;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import spoon.Launcher;
import spoon.experimental.modelobs.ActionBasedChangeListenerImpl;
import spoon.experimental.modelobs.FineModelChangeListener;
import spoon.experimental.modelobs.action.Action;
import spoon.processing.AbstractProcessor;
import spoon.processing.Processor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.*;

public class LambdaLauncher {
    static int nbErrors = 0;
    static int nbSetters = 0;

    public static void main(String[] args) throws Exception {
        Collection<File> files = FileUtils.listFiles(new File("/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/"), new SuffixFileFilter(".java"), FileFilterUtils.trueFileFilter());
        Accumulator accumulator = new Accumulator();

        /*
        for (File f : files) {
            applyOn(f.getAbsolutePath(), accumulator);
        }


        Map<CtElement, String> modificationsMap = accumulator.getModificationsMap();
        for (CtElement e : modificationsMap.keySet()) {
            e.replace();
        }

        System.out.println(nbErrors + " errors (IGS post conditions violated) has been found.");
        System.out.println(nbSetters + " setters.");
        */


        //System.out.print("Order of application:");
        String desc = "code";
        AbstractProcessor[] processors = {new IGSInliner(accumulator), new AddNPGuard(accumulator)};
        //AbstractProcessor[] processors = {new IGSInliner(accumulator)};
        for (Processor p : Arrays.asList(processors)) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
        }
        //System.out.println(desc);

        // create spoon
        Launcher spoon = new Launcher();

        //spoon.getEnvironment().setLevel("ALL");
        // Do not fail if class not found
        spoon.getEnvironment().setNoClasspath(true);
        spoon.getEnvironment().setCommentEnabled(false);

        String version = "/Users/benjaminbenni/Work/interference/src/main/resources/DataTypeField.java";
        version = version.replaceAll("[^0-9]+", " ");
        String descVersion = "";
        for (String s : version.trim().split(" ")) {
            descVersion += "-" + s;
        }

        FineModelChangeListener modelChangeListener = spoon.getEnvironment().getModelChangeListener();
        MyActionListener actionBasedChangeListener = new MyActionListener();
        spoon.getEnvironment().setModelChangeListener(actionBasedChangeListener);


        //System.out.println(spoon.getEnvironment().getModelChangeListener().getClass().getSimpleName());
        // set the output folder
        spoon.getEnvironment().getDefaultFileGenerator().setOutputDirectory(new File("target/spooned-iso" + descVersion));

        // set the project root
        spoon.addInputResource("/Users/benjaminbenni/Work/interference/src/main/resources/DataTypeField.java");

        for (Processor p : processors) {
            spoon.addProcessor(p);
        }

        spoon.buildModel();


        spoon.process();



        Map<CtElement, CtElement> modificationsMap = accumulator.getModificationsMap();
        for (CtElement e : modificationsMap.keySet()) {
            CtElement ctElement = modificationsMap.get(e);
            if (ctElement instanceof CtStatementList) {
                CtElement original = e;
                for (CtStatement c : ((CtStatementList) ctElement).getStatements()) {
                    ((CtStatement) e).insertAfter(c);
                    e = c;
                }
                original.delete();
            } else if (e instanceof CtBlock){ // setter

                CtIf shouldBeACtIf = (CtIf) modificationsMap.get(e);
                ((CtBlock)e).setStatements(Arrays.asList(shouldBeACtIf));
                //e.setParent(null);
            }
        }
        spoon.prettyprint();

        System.out.println(accumulator);
        //applyOn("/Users/benjaminbenni/Work/interference/src/main/resources/DataTypeField.java");
        //applyOn("/Users/benjaminbenni/Downloads/runnerup-1844222ffb76494cd9673623956b2a1f92b92f45/app/src/org/runnerup/export/format/GoogleFitData.java");
    }

    private static void applyOn(String inputPath, Accumulator accumulator) {
        IGSInliner igsInliner = new IGSInliner(accumulator);
        IGSInlinerPostCondition igsInlinerPostCondition = new IGSInlinerPostCondition(igsInliner.mapsSetterToTheirInlines);

        iso(inputPath, Arrays.asList(igsInliner, new AddNPGuard(accumulator)), "target/spooned-Guard_of_IGS_of_c");
        //applyProcs(inputPath, Arrays.asList(new HashMapToArrayMap(), new AddLambda(), igsInliner, new AddNPGuard(), igsInlinerPostCondition), "target/spooned-Guard_of_IGS_of_c");
        //applyProcs(inputPath, Arrays.asList(new AddNPGuard(), new HashMapToArrayMap(), igsInliner, igsInlinerPostCondition), "target/spooned-IGS_of_Guard_of_c");
    }


    private static void applyProcs(String inputPath, List<Processor> processors, String outputPath) {
        //System.out.print("Order of application:");
        String desc = "code";
        for (Processor p : processors) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
        }
        //System.out.println(desc);

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
        //System.out.println("---- OVER -----");
    }

    static void iso(String inputPath, List<Processor> processors, String outputPath) {
        //System.out.print("Order of application:");
        String desc = "code";
        for (Processor p : processors) {
            desc = p.getClass().getSimpleName() + "(" + desc + ")";
        }
        //System.out.println(desc);

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

        FineModelChangeListener modelChangeListener = spoon.getEnvironment().getModelChangeListener();
        MyActionListener actionBasedChangeListener = new MyActionListener();
        spoon.getEnvironment().setModelChangeListener(actionBasedChangeListener);


        //System.out.println(spoon.getEnvironment().getModelChangeListener().getClass().getSimpleName());
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

        // System.out.println(actionBasedChangeListener.getActions());

        for (Action a : actionBasedChangeListener.getActions()) {
        }
        //System.out.println("---- OVER -----");
    }

    static class MyActionListener extends ActionBasedChangeListenerImpl {
        private List<Action> actions = new ArrayList<>();

        @Override
        public void onAction(Action action) {
            actions.add(action);
            super.onAction(action);
        }

        List<Action> getActions() {
            return actions;
        }
    }

    private static void applyProc(String inputPath, Processor p, String outputPath) {
        applyProcs(inputPath, Arrays.asList(p), outputPath);
    }

}

