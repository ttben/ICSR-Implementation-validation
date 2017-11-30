package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.*;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.*;

public class IGSInliner extends AbstractProcessor<CtClass> {
    private Map<CtExecutable, List<CtInvocation>> igsToInvocationsMap= new HashMap<>();

    public Map<CtExecutable, List<CtInvocation>> getIgsToInvocationsMap() {
        return igsToInvocationsMap;
    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
         igsToInvocationsMap= new HashMap<>();

        List<CtInvocation> elements = candidate.getElements(new AbstractFilter<CtInvocation>() {
            @Override
            public boolean matches(CtInvocation element) {
                return super.matches(element);
            }
        });

        for (CtInvocation invocation : elements) {
            CtExecutableReference executableReference = invocation.getExecutable();

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


    @Override
    public void process(CtClass clazz) {
        for (Map.Entry<CtExecutable, List<CtInvocation>> entry : igsToInvocationsMap.entrySet()) {
            CtExecutable getterOrSetterMethod = entry.getKey();

            if (isAGetter(entry.getKey())) {
                if (processGetter(entry, getterOrSetterMethod)) return;

            } else {
                processSetter(entry, getterOrSetterMethod);
            }
        }
    }

    private void processSetter(Map.Entry<CtExecutable, List<CtInvocation>> entry, CtExecutable getterOrSetterMethod) {
        getterOrSetterMethod = getterOrSetterMethod.clone();

        CtParameter parameter = (CtParameter) getterOrSetterMethod.getParameters().get(0);

        //  Build a new variable that will store the value passed as parameter during the
        //  original call to the setter method
        UUID uuid = UUID.randomUUID();
        CtVariable newVariable = getFactory().createLocalVariable();
        newVariable.setType(parameter.getType());
        newVariable.setSimpleName("_$_" + uuid.toString().replaceAll("-", "_"));

        parameter.setSimpleName(newVariable.getSimpleName());

        // Find where the parameter of the setter is used
        List<CtParameterReference> parameterUsage = getterOrSetterMethod.getElements(new AbstractFilter<CtParameterReference>() {
            @Override
            public boolean matches(CtParameterReference element) {
                return super.matches(element);
            }
        });

        //  Replace parameter usage by new variable introduced
        for (CtParameterReference parameterReference : parameterUsage) {
            parameterReference.setSimpleName(newVariable.getSimpleName());
        }

        //  For all call to the setter method, replace it by the updated body
        for (CtInvocation ctInvocation : entry.getValue()) {
            // initialize with the parameter passed during the initial set call
            newVariable.setDefaultExpression((CtExpression) ctInvocation.getArguments().get(0));

            // Add the variable to the body that will replace the call
            ctInvocation.insertBefore((CtStatement) newVariable);

            //  Effectively replace the call by the updated body
            ctInvocation.replace(getterOrSetterMethod.getBody().getStatements());
        }
    }

    private boolean processGetter(Map.Entry<CtExecutable, List<CtInvocation>> entry, CtExecutable getterOrSetterMethod) {
        //  Check if a local variable, in the Getter/Setter, is declared; because if its happened
        //  renaming must be triggered
        if (usesLocalVariable(getterOrSetterMethod)) {
            // renaming needed
            return true;
        }

        List<CtStatement> statements = getterOrSetterMethod.getBody().clone().getStatements();

        // Common case
        if (statements.size() == 1) {

            CtExpression returnedExpression = ((CtReturn) statements.get(0)).getReturnedExpression();
            for (CtInvocation ctInvocation : entry.getValue()) {
                ctInvocation.replace(returnedExpression);
            }
        }
        return false;
    }
}

