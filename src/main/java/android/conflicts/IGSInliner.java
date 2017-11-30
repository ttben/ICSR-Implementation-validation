package android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.SpoonClassNotFoundException;
import spoon.support.reflect.code.CtAssignmentImpl;

import java.util.*;

public class IGSInliner extends AbstractProcessor<CtClass> {
    private Map<CtExecutable, List<CtInvocation>> igsToInvocationsMap;

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        igsToInvocationsMap = new HashMap<>();

        List<CtInvocation> elements = candidate.getElements(new AbstractFilter<CtInvocation>() {
            @Override
            public boolean matches(CtInvocation element) {
                return super.matches(element);
            }
        });

        for (CtInvocation invocation : elements) {
            CtExecutableReference executableReference = invocation.getExecutable();

            if (executableReference.getSimpleName().equalsIgnoreCase("setnamevalue")) {
                System.out.println();
            }

            // if its an internal call
            if (executableReference.getDeclaringType() != null && executableReference.getDeclaringType().equals(candidate.getReference())) {


                //  if its a getter/setter
                if (isAGetterSetter(executableReference.getExecutableDeclaration())) {
                    List<CtInvocation> invocationList = new ArrayList<>();
                    if (igsToInvocationsMap.containsKey(executableReference.getExecutableDeclaration())) {
                        invocationList = igsToInvocationsMap.get(executableReference.getExecutableDeclaration());
                    }

                    invocationList.add(invocation);
                    igsToInvocationsMap.put(executableReference.getExecutableDeclaration(), invocationList);
                }
            }
        }
        return !igsToInvocationsMap.isEmpty();
    }

    private boolean isAGetterSetter(CtExecutable executable) {
        try {
            return isAGetter(executable) || isASetter(executable);
        } catch (SpoonClassNotFoundException e) {
        }
        return false;
    }

    private boolean isAGetter(CtExecutable executable) {
        if (executable.getSimpleName().startsWith("get")) {
            if (!executable.getType().getActualClass().equals(Void.class)) {
                if (executable.getParameters().size() == 0) {
                    return true;
                }
            }
        } else if (executable.getSimpleName().startsWith("is")) {
            if (executable.getType().getSimpleName().equalsIgnoreCase("boolean") && executable.getType().isPrimitive()) {
                if (executable.getParameters().size() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isASetter(CtExecutable executable) {
        if (executable.getSimpleName().startsWith("set")) {
            if (executable.getType().getActualClass().getSimpleName().equals("void") && executable.getType().isPrimitive()) {
                if (executable.getParameters().size() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean usesLocalVariable(CtExecutable getterOrSetterMethod) {
        List<CtLocalVariableReference> ctLocalVariableReferences = getterOrSetterMethod.getElements(new AbstractFilter<CtLocalVariableReference>() {
            @Override
            public boolean matches(CtLocalVariableReference element) {
                return super.matches(element);
            }
        });

        return !ctLocalVariableReferences.isEmpty();
    }

    @Override
    public void process(CtClass clazz) {
        for (Map.Entry<CtExecutable, List<CtInvocation>> entry : igsToInvocationsMap.entrySet()) {
            CtExecutable getterOrSetterMethod = entry.getKey();

            if (isAGetter(entry.getKey())) {
                //  Check if a local variable, in the Getter/Setter, is declared; because if its happened
                //  renaming must be triggered
                if (usesLocalVariable(getterOrSetterMethod)) {
                    // renaming needed
                    return;
                }

                List<CtStatement> statements = getterOrSetterMethod.getBody().clone().getStatements();

                // Common case
                if (statements.size() == 1) {

                    CtExpression returnedExpression = ((CtReturn) statements.get(0)).getReturnedExpression();
                    for (CtInvocation ctInvocation : entry.getValue()) {
                        ctInvocation.replace(returnedExpression);
                    }
                }
            }

            // if it is a setter
            else {
                getterOrSetterMethod = getterOrSetterMethod.clone();

                CtParameter parameter = (CtParameter) getterOrSetterMethod.getParameters().get(0);


                UUID uuid = UUID.randomUUID();
                CtVariable newVariable = getFactory().createLocalVariable();
                newVariable.setType(parameter.getType());
                newVariable.setSimpleName("_$_" + uuid.toString());

                parameter.setSimpleName(newVariable.getSimpleName());

                // Find where the parameter of the setter is used
                List<CtParameterReference> parameterUsage = getterOrSetterMethod.getElements(new AbstractFilter<CtParameterReference>() {
                    @Override
                    public boolean matches(CtParameterReference element) {
                        return super.matches(element);
                    }
                });

                for (CtParameterReference parameterReference : parameterUsage) {
                    parameterReference.setSimpleName(newVariable.getSimpleName());
                }
                   // shouldBeModified.replace(ctInvocation);


                List<CtFieldWrite> whereToInjectParameter = getterOrSetterMethod.getElements(new AbstractFilter<CtFieldWrite>() {
                    @Override
                    public boolean matches(CtFieldWrite element) {
                        return super.matches(element);
                    }
                });
                CtFieldWrite ctFieldWrite = whereToInjectParameter.get(whereToInjectParameter.size() - 1);
                CtElement parent = ctFieldWrite.getParent();

                // should not happen
                if (!(parent instanceof CtAssignment)) {
                    return;
                }

                for (CtInvocation ctInvocation : entry.getValue()) {
                    CtAssignment clone = ((CtAssignmentImpl) parent);
                    clone.setAssignment((CtExpression) ctInvocation.getArguments().get(0));
                    ctInvocation.replace(getterOrSetterMethod.getBody().getStatements());
                    // shouldBeModified.replace(ctInvocation);
                }



                /*
                    List<CtStatement> statements = getterOrSetterMethod.getBody().clone().getStatements();
                // Common case
                if (statements.size() == 1) {
                    CtAssignment ctAssignment = (CtAssignment) statements.get(0);
                    CtExpression returnedExpression = ctAssignment.getAssigned();
                    for (CtInvocation ctInvocation : entry.getValue()) {

                        // if the setter call had as parameter a method call
                        if (ctInvocation.getArguments().get(0) instanceof CtInvocation) {
                            CtInvocation invocation = (CtInvocation) ctInvocation.getArguments().get(0);
                            ctAssignment.getAssignment().replace(invocation);
                            ctInvocation.insertBefore(ctAssignment);
                            ctInvocation.delete();
                        } else if (ctInvocation.getArguments().get(0) instanceof CtVariableRead) {
                            CtVariableRead o = (CtVariableRead)ctInvocation.getArguments().get(0);
                            ctAssignment.setAssignment(o);
                            ctInvocation.replace(ctAssignment);
                        }
                    }
                } else {  // if a null-check is performed for instance

                }
                */
            }
        }
    }
}

