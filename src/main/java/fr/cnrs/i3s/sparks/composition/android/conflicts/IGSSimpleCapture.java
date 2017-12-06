package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.getAllMethodInvocations;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.keepCallsToInternalMethods;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.keepCallsToSimpleSetter;

public class IGSSimpleCapture  extends AbstractProcessor<CtClass> {

    List<CtInvocation> igsInvocation = new ArrayList<>();

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        List<CtInvocation> allMethodInvocations = getAllMethodInvocations(candidate);
        List<CtInvocation> callsToInternalMethods = keepCallsToInternalMethods(allMethodInvocations, candidate);
        List<CtInvocation> callsToGetterSetter = keepCallsToSimpleSetter(callsToInternalMethods);
        igsInvocation.addAll(callsToGetterSetter);
        return false;
    }

    @Override
    public void process(CtClass ctClass) {

    }
}
