package tds.student.performance.domain;

import org.apache.commons.lang.builder.HashCodeBuilder;
import tds.itemrenderer.data.AccLookup;

/**
 * In order to keep the footprint of changes low for this performance round of improvements, we are wrapping AccLookup insttead of altering it in the itemrenderer project
 * AccLookup does not implement hashCode() which is a dependency in order to have @Cacheable work.  So when AccLookup is a parameter we will wrap it in this object and call another helper mehod which we can then cache.
 */
public class AccLookupWrapper {
    private AccLookup accLookup;

    public AccLookupWrapper(AccLookup accLookup) {
        this.accLookup = accLookup;
    }

    public AccLookup getValue() {
        return this.accLookup;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 41)
                .appendSuper(this.accLookup.getTypes().hashCode())
                .append(this.accLookup.getPosition())
                .append(this.accLookup.getId())
                .toHashCode();
    }
}
