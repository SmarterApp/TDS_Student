/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.utils;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import tds.student.performance.domain.OpportunitySegmentInsert;
import tds.student.performance.domain.OpportunitySegmentProperties;

import java.util.List;
import java.util.UUID;

public class InitializeSegmentsHelper {


    public static List<OpportunitySegmentInsert> createOpportunitySegmentInsertList(List<OpportunitySegmentProperties> propertiesList) {

        Function<OpportunitySegmentProperties, OpportunitySegmentInsert> transform = new Function<OpportunitySegmentProperties, OpportunitySegmentInsert>() {
            @Override
            public OpportunitySegmentInsert apply(OpportunitySegmentProperties input) {
                return new OpportunitySegmentInsert(input);
            }
        };

        return FluentIterable
                .from(propertiesList)
                .transform(transform)
                .toList();

    }

    // Get the minimum segment position
    public static Integer minimumSegmentPosition(List<OpportunitySegmentInsert> segmentList) {

        OpportunitySegmentInsert minimumPosition = Iterables.getFirst(
                Ordering.natural()
                        .nullsLast()
                        .onResultOf(new Function<OpportunitySegmentInsert, Integer>() {
                            public Integer apply(OpportunitySegmentInsert segment) {
                                return segment.getSegmentPosition();
                            }
                        }).sortedCopy(segmentList)
                , null);

        return minimumPosition == null ? null : minimumPosition.getSegmentPosition();
    }

    // Get the maximum segment position
    public static Integer maximumSegmentPosition(List<OpportunitySegmentInsert> segmentList) {

        OpportunitySegmentInsert maximumPosition = Iterables.getFirst(
                Ordering.natural()
                        .nullsLast()
                        .reverse()
                        .onResultOf(new Function<OpportunitySegmentInsert, Integer>() {
                            public Integer apply(OpportunitySegmentInsert segment) {
                                return segment.getSegmentPosition();
                            }
                        }).sortedCopy(segmentList)
                , null);

        return maximumPosition == null ? null : maximumPosition.getSegmentPosition();
    }

    // Filter the insert list by segmentPosition
    public static List<OpportunitySegmentInsert> segmentPositionFiltered(List<OpportunitySegmentInsert> insertList,
                                                                         final Integer segmentPosition) {
        return FluentIterable
                .from(insertList)
                .filter(new Predicate<OpportunitySegmentInsert>() {
                    @Override
                    public boolean apply(OpportunitySegmentInsert segment) {
                        return segment != null &&
                                segment.getSegmentPosition() != null &&
                                segment.getSegmentPosition().equals(segmentPosition);
                    }
                })
                .toList();
    }

    // Filter the insert list by segmentPosition and oppKey
    public static List<OpportunitySegmentInsert> segmentPositionAndOppKeyFiltered(List<OpportunitySegmentInsert> insertList,
                                                                                  final Integer segmentPosition,
                                                                                  final UUID oppKey) {
        return FluentIterable
                .from(insertList)
                .filter(new Predicate<OpportunitySegmentInsert>() {
                    @Override
                    public boolean apply(OpportunitySegmentInsert segment) {
                        return segment != null &&
                                segment.getSegmentPosition() != null &&
                                segment.getSegmentPosition().equals(segmentPosition) &&
                                segment.get_fk_TestOpportunity() != null &&
                                segment.get_fk_TestOpportunity().equals(oppKey);
                    }
                })
                .toList();
    }

    // Filter the insert list by segmentPosition and oppKey
    public static List<OpportunitySegmentInsert> insertListFiltered(List<OpportunitySegmentInsert> insertList,
                                                                    final Integer segmentPosition,
                                                                    final UUID oppKey,
                                                                    final String _efk_Segment) {
        return FluentIterable
                .from(insertList)
                .filter(new Predicate<OpportunitySegmentInsert>() {
                    @Override
                    public boolean apply(OpportunitySegmentInsert segment) {
                        return segment != null &&
                                segment.getSegmentPosition() != null &&
                                segment.getSegmentPosition().equals(segmentPosition) &&
                                segment.get_fk_TestOpportunity() != null &&
                                segment.get_fk_TestOpportunity().equals(oppKey) &&
                                segment.get_efk_Segment() != null &&
                                segment.get_efk_Segment().equals(_efk_Segment);
                    }
                })
                .toList();
    }

    /*
select
    _fk_TestOpportunity
from
    Segments34eae539z6c39z4427zb454z1f0372611575
where
    _fk_TestOpportunity = 0x13bc24bf08ce49e9ae8d2f495becceed
    and opItemCnt + ftItemCnt > 0 limit 1
             */
    // Filter the insert list by segmentPosition and oppKey
    public static List<OpportunitySegmentInsert> insertListFilteredItemsInPool(List<OpportunitySegmentInsert> insertList,
                                                                               final UUID oppKey) {
        return FluentIterable
                .from(insertList)
                .filter(new Predicate<OpportunitySegmentInsert>() {
                    @Override
                    public boolean apply(OpportunitySegmentInsert segment) {
                        return segment != null &&
                                segment.get_fk_TestOpportunity() != null &&
                                segment.get_fk_TestOpportunity().equals(oppKey) &&
                                ( (segment.getOpItemCnt() != null ? segment.getOpItemCnt() : 0) +
                                        (segment.getFtItemCnt() != null ? segment.getFtItemCnt() : 0) > 0 )
                                ;
                    }
                })
                .toList();
    }

}
