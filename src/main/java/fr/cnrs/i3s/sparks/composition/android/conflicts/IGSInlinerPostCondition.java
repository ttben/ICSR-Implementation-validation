package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.List;
import java.util.Map;

public class IGSInlinerPostCondition extends AbstractProcessor<CtClass> {
    private Map<CtExecutable, List<CtInvocation>> igsToInvocationsMap;

    @Override
    public boolean isToBeProcessed(CtClass candidate) {
        

        candidate.getElements(new AbstractFilter<CtMethod>() {
            @Override
            public boolean matches(CtMethod element) {
                if (GetterSetterCriterion.isASetter(element.getReference().getExecutableDeclaration())) {
                    System.out.println();
                }
                return super.matches(element);
            }
        });
        return super.isToBeProcessed(candidate);
    }

    @Override
    public void process(CtClass ctClass) {

    }
}
