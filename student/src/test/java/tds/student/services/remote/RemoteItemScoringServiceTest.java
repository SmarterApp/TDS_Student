/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.services.remote;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ItemScoringRepository;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RemoteItemScoringServiceTest {

    @Mock
    private IItemScoringService mockLegacyItemScoringService;

    @Mock
    private ItemScoringRepository mockRepository;

    private RemoteItemScoringService service;

    @Before
    public void setup() {
        service = new RemoteItemScoringService(mockLegacyItemScoringService, true, true, mockRepository);
    }

    @Test
    public void itShouldUseRepositoryAndLegacyToUpdateResponsesWhenEnabled() throws Exception {
        final OpportunityInstance oppInstance = oppInstance();
        final List<ItemResponseUpdate> responsesUpdated = newArrayList(mock(ItemResponseUpdate.class));
        final Float pageDuration = 1.23f;

        service.updateResponses(oppInstance, responsesUpdated, pageDuration);

        verify(mockLegacyItemScoringService).updateResponses(eq(oppInstance), eq(responsesUpdated), eq(pageDuration));
        verify(mockRepository).updateResponses(
            oppInstance.getExamId(),
            oppInstance.getSessionKey(),
            oppInstance.getExamBrowserKey(),
            oppInstance.getExamClientName(),
            pageDuration,
            responsesUpdated);
    }

    @Test
    public void itShouldOnlyUseLegacyToUpdateResponsesWhenOnlyLegacyEnabled() throws Exception {
        final OpportunityInstance oppInstance = oppInstance();
        final List<ItemResponseUpdate> responsesUpdated = newArrayList(mock(ItemResponseUpdate.class));
        final Float pageDuration = 1.23f;

        service = new RemoteItemScoringService(mockLegacyItemScoringService, false, true, mockRepository);
        service.updateResponses(oppInstance, responsesUpdated, pageDuration);

        verify(mockLegacyItemScoringService).updateResponses(eq(oppInstance), eq(responsesUpdated), eq(pageDuration));
        verifyZeroInteractions(mockRepository);
    }

    @Test
    public void itShouldOnlyUseRemoteToUpdateResponsesWhenOnlyRemoteEnabled() throws Exception {
        final OpportunityInstance oppInstance = oppInstance();
        final List<ItemResponseUpdate> responsesUpdated = newArrayList(mock(ItemResponseUpdate.class));
        final Float pageDuration = 1.23f;

        service = new RemoteItemScoringService(mockLegacyItemScoringService, true, false, mockRepository);
        service.updateResponses(oppInstance, responsesUpdated, pageDuration);

        verifyZeroInteractions(mockLegacyItemScoringService);
        verify(mockRepository).updateResponses(
            oppInstance.getExamId(),
            oppInstance.getSessionKey(),
            oppInstance.getExamBrowserKey(),
            oppInstance.getExamClientName(),
            pageDuration,
            responsesUpdated);
    }

    @Test(expected = IllegalStateException.class)
    public void itShouldThrowIfNeitherRemoteNorLegacyAreEnabled() throws Exception {
        new RemoteItemScoringService(mockLegacyItemScoringService, false, false, mockRepository);
    }

    @Test
    public void itShouldOnlyUseLegacyToCheckScoreability() throws Exception {
        final IItemResponseScorable responseScorable = mock(ItemResponseScorable.class);
        final IITSDocument itsDoc = mock(IITSDocument.class);

        service.checkScoreability(responseScorable, itsDoc);

        verify(mockLegacyItemScoringService).checkScoreability(responseScorable, itsDoc);
        verifyZeroInteractions(mockRepository);
    }

    @Test
    public void itShouldOnlyUseLegacyToUpdateItemScore() throws Exception {
        final UUID oppKey = UUID.randomUUID();
        final IItemResponseScorable responseScorable = mock(ItemResponseScorable.class);
        final ItemScore score = mock(ItemScore.class);

        service.updateItemScore(oppKey, responseScorable, score);

        verify(mockLegacyItemScoringService).updateItemScore(oppKey, responseScorable, score);
        verifyZeroInteractions(mockRepository);
    }

    @Test
    public void itShouldOnlyUseLegacyToScoreItem() throws Exception {
        final UUID oppKey = UUID.randomUUID();
        final IItemResponseScorable responseScorable = mock(ItemResponseScorable.class);
        final IITSDocument itsDoc = mock(IITSDocument.class);

        service.scoreItem(oppKey, responseScorable, itsDoc);

        verify(mockLegacyItemScoringService).scoreItem(oppKey, responseScorable, itsDoc);
        verifyZeroInteractions(mockRepository);
    }

    private OpportunityInstance oppInstance() {
        return new OpportunityInstance(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "clientName",
            "user agent");
    }
}