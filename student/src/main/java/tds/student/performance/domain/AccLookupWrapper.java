/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.domain;

import org.apache.commons.lang.builder.HashCodeBuilder;
import tds.itemrenderer.data.AccLookup;

/**
 * In order to keep the footprint of changes low for this performance round of improvements, we are wrapping AccLookup instead of altering it in the itemrenderer project
 * AccLookup does not implement hashCode() which is a dependency in order to have @Cacheable work.  So when AccLookup is a parameter we will wrap it in this object and call another helper method which we can then cache.
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
        if (this.accLookup == null)
            return 0;

        return new HashCodeBuilder(13, 41)
                .appendSuper(this.accLookup.getTypes().hashCode())
                .append(this.accLookup.getPosition())
                .append(this.accLookup.getId())
                .toHashCode();
    }
}
