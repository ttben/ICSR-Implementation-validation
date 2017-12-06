package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.util.*;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.isAGetter;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.usesLocalVariable;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.*;

public class IGSInlinerSimple extends AbstractProcessor<CtClass> {
    private Accumulator accumulator;
    List<CtInvocation> igsInvocation = new ArrayList<>();
    List<CtClass> targetClasses = new ArrayList<>();

    public IGSInlinerSimple(Accumulator accumulator, List<CtInvocation> igsInvocation)  {
        this.accumulator = accumulator;
        this.igsInvocation = igsInvocation;

    }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        for (CtInvocation invocation : igsInvocation) {
            targetClasses.add(invocation.getParent(CtClass.class));
        }
        for (CtClass c : targetClasses) {
            if (c == candidate) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(CtClass clazz) {
        for (CtInvocation entry : igsInvocation) {
            if (isAGetter(entry.getExecutable().getExecutableDeclaration())) {
                // processGetter(entry, entry.getExecutable().getExecutableDeclaration());
            } else {
                processSetter(entry, entry.getExecutable().getExecutableDeclaration());
            }
        }
    }

    private void processSetter(CtInvocation entry, CtExecutable getterOrSetterMethod) {
        getterOrSetterMethod = getterOrSetterMethod.clone();

        CtParameter parameter = (CtParameter) getterOrSetterMethod.getParameters().get(0);

        //  Build a new variable that will store the value passed as parameter during the
        //  original call to the setter method
        CtVariable newVariable = createVariable(parameter);
        modifyParameterNames(getterOrSetterMethod, parameter, newVariable);
        replaceInvocationByUpdatedSetterBody(entry, getterOrSetterMethod, newVariable);
    }

    private void replaceInvocationByUpdatedSetterBody(CtInvocation ctInvocation, CtExecutable getterOrSetterMethod, CtVariable newVariable) {
        //  For all call to the setter method, replace it by the updated body

        // initialize with the parameter passed during the initial set call
        newVariable.setDefaultExpression((CtExpression) ctInvocation.getArguments().get(0));

        // Add the variable to the body that will replace the call
        CtStatementList newBlock = getFactory().createStatementList();
        newBlock.setParent(ctInvocation.getParent());

        newBlock.addStatement((CtStatement) newVariable);
        for (CtStatement s : getterOrSetterMethod.getBody().getStatements()) {
            newBlock.addStatement(s);
        }

        if (accumulator == null) {
            //normal calls:
            ctInvocation.insertBefore((CtStatement) newVariable);

            //  Effectively replace the call by the updated body
            ctInvocation.replace(getterOrSetterMethod.getBody().getStatements());
        } else {
            accumulator.add(this, ctInvocation, newBlock);
        }

    }

    private void modifyParameterNames(CtExecutable getterOrSetterMethod, CtParameter parameter, CtVariable newVariable) {
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
    }

    private CtVariable createVariable(CtParameter parameter) {
        UUID uuid = UUID.randomUUID();
        CtVariable newVariable = getFactory().createLocalVariable();
        newVariable.setType(parameter.getType());
        newVariable.setSimpleName("_$_" + uuid.toString().replaceAll("-", "_"));
        return newVariable;
    }

    private boolean processGetter(Map.Entry<CtExecutable, List<CtInvocation>> entry, CtExecutable getterOrSetterMethod) {
        //  Check if a local variable, in the Getter/Setter, is declared; because if its happened
        //  renaming must be triggered
        if (usesLocalVariable(getterOrSetterMethod)) {
            // renaming needed
            return true;
        }

        if (getterOrSetterMethod instanceof CtMethod && ((CtMethodImpl) getterOrSetterMethod).isAbstract()) {
            return false;
        }
        if (getterOrSetterMethod == null || getterOrSetterMethod.getBody() == null || getterOrSetterMethod.getBody().getStatements() == null) {
            System.out.println();
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

