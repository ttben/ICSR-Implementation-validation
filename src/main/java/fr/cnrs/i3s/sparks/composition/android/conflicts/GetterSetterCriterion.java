package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.SpoonClassNotFoundException;

import java.util.List;

public class GetterSetterCriterion {
    static boolean isAGetterSetter(CtExecutable executable) {
        try {
            return isAGetter(executable) || isASetter(executable);
        } catch (SpoonClassNotFoundException e) {
        }
        return false;
    }

    static boolean isASimpleSetter(CtExecutable executable) {
        if (executable == null || executable.getSimpleName() == null) {
            return false;
        }

        if (executable.getSimpleName().startsWith("set")) {
            if (executable.getType().getActualClass().getSimpleName().equals("void") && executable.getType().isPrimitive()) {
                if (executable.getParameters().size() == 1 && executable.getBody().getStatements().size() == 1 && executable.getBody().getStatements().get(0) instanceof CtThisAccess) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isAGetter(CtExecutable executable) {
        if (executable == null || executable.getSimpleName() == null) {
           return false;
        }
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

    static boolean isASetter(CtExecutable executable) {
        if (executable == null || executable.getSimpleName() == null) {
            return false;
        }
        if (executable.getSimpleName().startsWith("set")) {
            if (executable.getType().getActualClass().getSimpleName().equals("void") && executable.getType().isPrimitive()) {
                if (executable.getParameters().size() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean usesLocalVariable(CtExecutable getterOrSetterMethod) {
        List<CtLocalVariableReference> ctLocalVariableReferences = getterOrSetterMethod.getElements(new AbstractFilter<CtLocalVariableReference>() {
            @Override
            public boolean matches(CtLocalVariableReference element) {
                return super.matches(element);
            }
        });

        return !ctLocalVariableReferences.isEmpty();
    }
}