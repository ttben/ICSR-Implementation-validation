package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

import java.util.Set;

import static fr.cnrs.i3s.sparks.composition.android.conflicts.GetterSetterCriterion.isASetter;


public class AddNPGuard extends AbstractProcessor<CtClass> {

    IGSInliner igsInliner;

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        igsInliner = new IGSInliner();
        return igsInliner.isToBeProcessed(candidate);
    }

    @Override
    public void process(CtClass ctClass) {
        //  Retrieve all getter/setters
        Set<CtExecutable> ctExecutables = igsInliner.getIgsToInvocationsMap().keySet();
        for (CtExecutable executable : ctExecutables) {

            if (isASetter(executable)) {
                CtParameter parameter = (CtParameter) executable.getParameters().get(0);

                //  Create a if that wrap the actual body of the setter
                CtIf ctIf = getFactory().createIf();
                ctIf.setThenStatement(executable.getBody().clone());

                //  Add NON NULL guard with parameter name
                ctIf.setCondition(getFactory().createCodeSnippetExpression(parameter.getSimpleName() + " != null"));

                //  Replace body of the initial method by the if (that wraps everything)
                executable.setBody(ctIf);
            }
        }
    }
}
