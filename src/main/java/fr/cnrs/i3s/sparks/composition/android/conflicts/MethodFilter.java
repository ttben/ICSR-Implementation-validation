package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.isAGetterSetter;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.isASetter;

public class MethodFilter {
    static Map<CtExecutable, List<CtInvocation>> buildInvocationsToModify(List<CtInvocation> callsToGetterSetter) {
        Map<CtExecutable, List<CtInvocation>> igsToInvocationsMap = new HashMap<>();

        for (CtInvocation invocation : callsToGetterSetter) {
            CtExecutableReference executableReference = invocation.getExecutable();

            List<CtInvocation> invocationList = new ArrayList<>();
            if (igsToInvocationsMap.containsKey(executableReference.getExecutableDeclaration())) {
                invocationList = igsToInvocationsMap.get(executableReference.getExecutableDeclaration());
            }

            invocationList.add(invocation);
            igsToInvocationsMap.put(executableReference.getExecutableDeclaration(), invocationList);
        }

        return igsToInvocationsMap;
    }

    static List<CtInvocation> keepCallsToGetterSetter(List<CtInvocation> invocations) {
        List<CtInvocation> invocationsToGetterSetter = new ArrayList<>();
        for (CtInvocation invocation : invocations) {
            CtExecutableReference executableReference = invocation.getExecutable();

            if (isAGetterSetter(executableReference.getExecutableDeclaration())) {
                invocationsToGetterSetter.add(invocation);
            }
        }
        return invocationsToGetterSetter;
    }

    static List<CtInvocation> keepCallsToSetter(List<CtInvocation> invocations) {
        List<CtInvocation> invocationsToGetterSetter = new ArrayList<>();
        for (CtInvocation invocation : invocations) {
            CtExecutableReference executableReference = invocation.getExecutable();

            if (isASetter(executableReference.getExecutableDeclaration())) {
                invocationsToGetterSetter.add(invocation);
            }
        }
        return invocationsToGetterSetter;
    }

    static List<CtMethod> keepSetters(List<CtMethod> methods) {
        List<CtMethod> setters = new ArrayList<>();
        for (CtMethod currentMethod : methods) {
            CtExecutableReference executableReference = currentMethod.getReference();
            if (isASetter(executableReference.getExecutableDeclaration())) {
                setters.add(currentMethod);
            }
        }
        return setters;
    }

    static List<CtInvocation> keepCallsToInternalMethods(List<CtInvocation> allMethodInvocations, CtClass ctClass) {
        List<CtInvocation> internalCalls = new ArrayList<>();
        for (CtInvocation invocation : allMethodInvocations) {
            CtExecutableReference executableReference = invocation.getExecutable();

            // if its an internal call
            if (executableReference.getDeclaringType() != null && executableReference.getDeclaringType().equals(ctClass.getReference())) {
                //  if its a getter/setter
                internalCalls.add(invocation);
            }
        }

        return internalCalls;
    }

    static List<CtMethod> getAllMethods(CtClass ctClass) {
        List<CtMethod> elements = ctClass.getElements(new AbstractFilter<CtMethod>() {
            @Override
            public boolean matches(CtMethod element) {
                return element.getParent().equals(ctClass);
            }
        });

        return elements;
    }

    static  List<CtInvocation> getAllMethodInvocations(CtClass ctClass) {
        List<CtInvocation> elements = ctClass.getElements(new AbstractFilter<CtInvocation>() {
            @Override
            public boolean matches(CtInvocation element) {
                return super.matches(element);
            }
        });
        return elements;
    }


}
