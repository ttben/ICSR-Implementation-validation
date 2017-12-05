package fr.cnrs.i3s.sparks.composition.android.conflicts;

import spoon.processing.Processor;
import spoon.reflect.declaration.CtElement;

import java.util.HashMap;
import java.util.Map;

public class Accumulator {
    private Map<Processor,Map<CtElement, CtElement>> modificationsMap = new HashMap<>();

    public Map<Processor,Map<CtElement, CtElement>> getModificationsMap() {
        return modificationsMap;
    }

    public void add(Processor p, CtElement e, CtElement snippet) {

        Map<CtElement, CtElement> modificationsMap = new HashMap<>();
        if (this.modificationsMap.containsKey(p)) {
            modificationsMap = this.modificationsMap.get(p);
        }

        for (CtElement element : modificationsMap.keySet()) {
            if (e == element) {
                // can happened: the Guard and Inliner will put
                //fixme guard seems to push the same method twice, why?
                //throw new IllegalStateException("Oups. Dafuq?");
            }
        }

        modificationsMap.put(e, snippet);
        this.modificationsMap.put(p, modificationsMap);
    }
}
