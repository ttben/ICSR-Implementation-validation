package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.ArrayList;
import java.util.List;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.isASetter;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.getAllMethods;
import static fr.cnrs.i3s.sparks.composition.android.conflicts.MethodFilter.keepSetters;


public class AddNPGuard extends AbstractProcessor<CtClass> {
    private List<CtMethod> settersToModify = new ArrayList<>();
    private Accumulator accumulator;

    AddNPGuard(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    public AddNPGuard() { }

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        if (candidate.getQualifiedName().equalsIgnoreCase("org.runnerup.export.format.GoogleFitData")) {
            System.out.println();
        }
        //System.out.println("Starting analyse AddGuard...");

        List<CtMethod> allMethods = getAllMethods(candidate);
        settersToModify = keepSetters(allMethods);
        //System.out.println("Over analyse AddGuard.");

        System.out.println(candidate.getQualifiedName());

        return !settersToModify.isEmpty();
    }

    @Override
    public void process(CtClass ctClass) {
        //System.out.println("Starting to process AddGuard ...");

        List<CtMethod> setters = settersToModify;
        for (CtExecutable currentSetterMethod : setters) {

            if (isASetter(currentSetterMethod)) {
                CtParameter parameter = (CtParameter) currentSetterMethod.getParameters().get(0);

                //  Create a if that wrap the actual body of the setter
                CtIf ctIf = getFactory().createIf();
                ctIf.setThenStatement(currentSetterMethod.getBody().clone());

                //  Add NON NULL guard with parameter name
                ctIf.setCondition(getFactory().createCodeSnippetExpression(parameter.getSimpleName() + " != null"));

                if (accumulator != null) {
                    accumulator.add(this, currentSetterMethod, ctIf);
                } else {
                    //  Replace body of the initial method by the if (that wraps everything)
                    currentSetterMethod.setBody(ctIf);
                }
            }
        }
        //System.out.println("Over processing AddGuard.");
    }
}
