package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.*;
import java.util.function.BiConsumer;

public class AddLambda extends AbstractProcessor<CtForEach> {

    private Accumulator accumulator;

    AddLambda(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public boolean isToBeProcessed(CtForEach candidate) {
        //System.out.println("LAMBDA");
        try {
            return candidate.getVariable().getType().getActualClass().equals(Map.Entry.class)
                    && Map.class.isAssignableFrom(((CtInvocation)candidate.getExpression()).getTarget().getType().getActualClass());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void process(CtForEach forEach) {
        CtLocalVariable<?> variable = forEach.getVariable();
        if (forEach.getExpression() instanceof CtVariableRead) {
            CtVariableRead expression = (CtVariableRead) forEach.getExpression();
            String simpleName = expression.getVariable().getSimpleName();

            CtMethod parent = forEach.getParent(CtMethod.class);

            List<CtElement> elements = parent.getElements(new AbstractFilter<CtElement>() {
                @Override
                public boolean matches(CtElement element) {
                    if (element instanceof CtLocalVariable) {
                        CtExpression assignment = ((CtLocalVariable) element).getAssignment();
                        String simpleName1 = ((CtLocalVariable) element).getSimpleName();
                        return simpleName1.equals(simpleName);
                    }
                    return false;

                }
            });

            CtElement lastOne = elements.get(elements.size() - 1);
            // todo3
        } else if (forEach.getExpression() instanceof CtInvocationImpl) {

            CtExpression<?> expression = ((CtInvocationImpl) forEach.getExpression()).getTarget();

            CtInvocation ctInvocation = buildLambdaWrapper(expression);

            CtLambda ctLambda = getFactory().createLambda();
            ArrayList<CtLambda> listOfLambda = new ArrayList<>();
            listOfLambda.add(ctLambda);

            ctInvocation.setArguments(listOfLambda);

            CtStatement body = forEach.getBody();

            CtParameter<Object> parameter = getFactory().Core().createParameter();
            parameter.setSimpleName(variable.getSimpleName().trim());
            parameter.setType((CtTypeReference<Object>) variable.getType());

            ctLambda.setParameters(Arrays.asList(parameter));
            ctLambda.setBody(body);
            ctLambda.setParent(ctInvocation);

            forEach.replace(ctInvocation);
            //System.out.println(ctInvocation);
        }
    }

    private CtInvocation buildLambdaWrapper(CtExpression expression) {
        CtExecutableReference executableReference = getFactory().createExecutableReference();
        executableReference = executableReference.setParameters(getFactory().createReferences(Arrays.asList(BiConsumer.class)));
        executableReference.setSimpleName("forEach");

        CtInvocation invocation = getFactory().Code().createInvocation(expression, executableReference, Arrays.asList());

        return invocation;
    }
}
