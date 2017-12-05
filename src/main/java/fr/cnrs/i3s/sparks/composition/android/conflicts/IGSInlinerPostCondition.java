package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.List;
import java.util.Map;

/*
    Check if every setter (even not simple) has been correctly 'inlined'
 */
public class IGSInlinerPostCondition extends AbstractProcessor<CtClass> {
    private Map<CtClass, Map<CtExecutable, CtBlock>> mapSetterToTheirInlinments;
    private List<CtExecutable> newSetters;

    int nbErrors = 0;
    int nbSetters = 0;

    public IGSInlinerPostCondition(Map<CtClass, Map<CtExecutable, CtBlock>> mapSetterToTheirInlinments) {
        this.mapSetterToTheirInlinments = mapSetterToTheirInlinments;
    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        nbErrors = 0;
        nbSetters = 0;

        newSetters = candidate.getElements(new AbstractFilter<CtExecutable>() {
            @Override
            public boolean matches(CtExecutable element) {
                return mapSetterToTheirInlinments.containsKey(candidate) && mapSetterToTheirInlinments.get(candidate).keySet().contains(element);
            }
        });

        nbSetters += newSetters.size();
        return !newSetters.isEmpty();
    }

    @Override
    public void process(CtClass ctClass) {
        boolean error = false;
        if (!mapSetterToTheirInlinments.containsKey(ctClass)){
            return;
        }
        Map<CtExecutable, CtBlock> mapSetterToTheirInlinments = this.mapSetterToTheirInlinments.get(ctClass);

        for (CtExecutable executable : mapSetterToTheirInlinments.keySet()) {
            CtBlock newBlock = executable.getBody();
            CtBlock oldBlock = mapSetterToTheirInlinments.get(executable);

            if (!newBlock.equals(oldBlock)) {
                System.err.println("A Post condition has been violated: the inliner has not inlined the content of its setter");
                error = true;
                nbErrors++;
            }
        }
        if (error) {
            System.err.printf("> %d ERROR(s) found in class %s<\n", nbErrors, ctClass.getQualifiedName());
        } else {
            System.out.println("Over processing PostCondition IGSInliner.");
        }
    }
}
