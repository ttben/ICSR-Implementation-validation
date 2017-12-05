package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.List;
import java.util.Map;

/*
stocker linlgnment que j'ai fait pour chacun des CALL de setter
et vérifier, a la fin, que ces inlinement sont egaux au body setter
 */
public class IGSInlinerPostCondition extends AbstractProcessor<CtClass> {
    private Map<CtExecutable, CtBlock> mapSetterToTheirInlinments;
    private List<CtExecutable> newSetters;

    public IGSInlinerPostCondition(Map<CtExecutable, CtBlock> mapSetterToTheirInlinments) {
        this.mapSetterToTheirInlinments = mapSetterToTheirInlinments;
    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        //System.out.println("Starting analyse PostCondition IGSInliner ...");

        newSetters = candidate.getElements(new AbstractFilter<CtExecutable>() {
            @Override
            public boolean matches(CtExecutable element) {
                return mapSetterToTheirInlinments.keySet().contains(element);
            }
        });

       //System.out.println("Over analyse PostCondition IGSInliner.");
        LambdaLauncher.nbSetters += newSetters.size();
        return !newSetters.isEmpty();
    }

    @Override
    public void process(CtClass ctClass) {
        boolean error = false;
        //System.out.println("Starting to process PostCondition IGSInliner ...");
        for (CtExecutable executable : mapSetterToTheirInlinments.keySet()) {
            CtBlock newBlock = executable.getBody();
            CtBlock oldBlock = mapSetterToTheirInlinments.get(executable);

            if (!newBlock.equals(oldBlock)) {
                //System.err.println("A Post condition has been violated: the inliner has not inlined the content of its setter");
                error = true;
                LambdaLauncher.nbErrors++;
            }
        }
        if (error) {
            //System.err.println("> ERROR <");
        } else {
            //System.out.println("Over processing PostCondition IGSInliner.");
        }
    }
}
