package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;

import java.util.ArrayList;
import java.util.List;

/*
    Check if every setter call has been deleted
 */
public class IGSInlinerSimplePostCondition extends AbstractProcessor<CtInvocation> {
    private List<CtInvocation> oldCalls = new ArrayList<>();

    int nbErrors = 0;
    int nbSetters = 0;

    public IGSInlinerSimplePostCondition(List<CtInvocation> callsToInternalSetters) {
        oldCalls = callsToInternalSetters;
    }

    @Override
    public boolean isToBeProcessed(CtInvocation candidate) {
        nbErrors = 0;
        nbSetters = 0;

        for (CtInvocation invocation : oldCalls) {
            if (invocation == candidate) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(CtInvocation ctInvocation) {
        System.err.printf("> Error, invocation %s in class %s has NOT been inlined <\n",
                ctInvocation.getShortRepresentation(),
                ctInvocation.getParent(CtClass.class).getQualifiedName());
    }
}
