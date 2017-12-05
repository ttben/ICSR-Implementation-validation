package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.reflect.declaration.CtElement;

import java.util.HashMap;
import java.util.Map;

public class Accumulator {
    private Map<CtElement, CtElement> modificationsMap = new HashMap<>();

    public Map<CtElement, CtElement> getModificationsMap() {
        return modificationsMap;
    }

    public void add(CtElement e, CtElement snippet) {
        for (CtElement element : modificationsMap.keySet()) {
            if (e == element) {
                throw new IllegalStateException("Oups. Dafuq?");
            }
        }

        this.modificationsMap.put(e, snippet);
    }
}
