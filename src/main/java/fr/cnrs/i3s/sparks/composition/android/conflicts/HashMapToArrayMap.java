package fr.cnrs.i3s.sparks.composition.android.conflicts;

import android.util.ArrayMap;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapToArrayMap extends AbstractProcessor<CtLocalVariable> {
    private List<CtLocalVariable> alreadyHandled = new ArrayList<>();

    @Override
    public boolean isToBeProcessed(CtLocalVariable localVariable) {
        System.out.println("HMU");
        if (alreadyHandled.contains(localVariable)) {
            return false;
        }

        alreadyHandled.add(localVariable);

        try {
            Class type = localVariable.getType().getActualClass();
            if (!Map.class.isAssignableFrom(type)) {
                return false;
            }

            List<CtInvocation> elements = getCtInvocations(localVariable);
            for (CtInvocation ctInvocation : elements) {
                if (!checkExecutableReference(ctInvocation.getExecutable())) {
                    return false;
                }
            }
            System.out.println(elements);


        } catch (SpoonClassNotFoundException e) {
            // nothing
            return false;
        }
        return true;
    }

    private List<CtInvocation> getCtInvocations(CtLocalVariable localVariable) {
        return localVariable.getParent(CtMethod.class).getElements(new AbstractFilter<CtInvocation>() {
            @Override
            public boolean matches(CtInvocation element) {
                if (!(element.getTarget() instanceof CtVariableRead)) {
                    return false;
                }
                CtExpression target = element.getTarget();

                if ((target instanceof CtInvocation) || (target instanceof CtVariableRead)) {
                    CtVariableReference variable = ((CtVariableRead) element.getTarget()).getVariable();
                    if (variable.getSimpleName().equals(localVariable.getSimpleName())) {
                        return true;
                    }
                }

                return false;
            }
        });
    }

    private boolean checkExecutableReference(CtExecutableReference executable) {
        String simpleName = executable.getSimpleName();
        return !simpleName.equals("forEach") && !simpleName.equals("compute");
    }

    @Override
    public void process(CtLocalVariable ctLocalVariable) {
        CtTypeReference<Object> hashmapTtype = getFactory().Code().createCtTypeReference(HashMap.class);
        CtTypeReference<Object> ctTypeReference = getFactory().Code().createCtTypeReference(ArrayMap.class);
        ctLocalVariable.getType().replace(ctTypeReference);
        ctLocalVariable.setType(ctTypeReference);

        if (ctLocalVariable.getDefaultExpression() != null) {
            if (ctLocalVariable.getDefaultExpression() instanceof CtConstructorCall) {
                if (((CtConstructorCall)ctLocalVariable.getDefaultExpression()).getExecutable().getDeclaringType().equals(hashmapTtype)) {
                    CtExecutableReference<Object> executableReference = getFactory().createExecutableReference();
                    executableReference.setType(ctTypeReference);
                    executableReference.setDeclaringType(ctTypeReference);
                    CtConstructorCall<Object> constructorCall = getFactory().createConstructorCall();
                    constructorCall.setExecutable(executableReference);
                    ctLocalVariable.setDefaultExpression(constructorCall);
                }
            }
        }

        List<CtInvocation> ctInvocations = getCtInvocations(ctLocalVariable);
        for (CtInvocation invocation : ctInvocations) {
            invocation.getTarget().setType(ctTypeReference);
            System.out.println(invocation);
        }
    }
}
