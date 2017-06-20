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

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import tds.itemrenderer.data.IITSDocument;
import tds.itemscoringengine.ItemScore;
import tds.student.services.abstractions.IItemScoringService;
import tds.student.sql.data.IItemResponseScorable;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ItemScoringRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This IItemScoringService implementation forwards scoring requests to the ItemScoringRepository.
 */
@Service("integrationItemScoringService")
@Scope("prototype")
public class RemoteItemScoringService implements IItemScoringService {

    private final IItemScoringService legacyItemScoringService;
    private final boolean remoteExamCallsEnabled;
    private final boolean legacyCallsEnabled;
    private final ItemScoringRepository repository;


    @Autowired
    public RemoteItemScoringService(
        @Qualifier("legacyItemScoringService") final IItemScoringService legacyItemScoringService,
        @Value("${tds.exam.remote.enabled}") final Boolean remoteExamCallsEnabled,
        @Value("${tds.exam.legacy.enabled}") final Boolean legacyCallsEnabled,
        final ItemScoringRepository repository) {

        if (!remoteExamCallsEnabled && !legacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration");
        }

        this.legacyItemScoringService = legacyItemScoringService;
        this.remoteExamCallsEnabled = remoteExamCallsEnabled;
        this.legacyCallsEnabled = legacyCallsEnabled;
        this.repository = repository;
    }

    @Override
    public List<ItemResponseUpdateStatus> updateResponses(final OpportunityInstance oppInstance,
                                                          final List<ItemResponseUpdate> responsesUpdated,
                                                          final Float pageDuration) throws ReturnStatusException {
        List<ItemResponseUpdateStatus> responseUpdateStatuses = new ArrayList<>();

        if (legacyCallsEnabled) {
            responseUpdateStatuses = legacyItemScoringService.updateResponses(oppInstance, responsesUpdated, pageDuration);
        }

        if (!remoteExamCallsEnabled) {
            return responseUpdateStatuses;
        }

        responseUpdateStatuses = repository.updateResponses(
            oppInstance.getExamId(),
            oppInstance.getSessionKey(),
            oppInstance.getExamBrowserKey(),
            oppInstance.getExamClientName(),
            pageDuration,
            responsesUpdated);

        return responseUpdateStatuses;
    }

    @Override
    public ItemScore checkScoreability(final IItemResponseScorable responseScorable, final IITSDocument itsDoc) throws ReturnStatusException {
        if (legacyCallsEnabled) {
            return legacyItemScoringService.checkScoreability(responseScorable, itsDoc);
        }

        //This method is only called via a legacy service self-reference and via the unused TimerScoringService
        throw new UnsupportedOperationException("No remote implementation of checkScoreability");
    }

    @Override
    public boolean updateItemScore(final UUID oppKey, final IItemResponseScorable response, final ItemScore score) throws ReturnStatusException {
        if (legacyCallsEnabled) {
            return legacyItemScoringService.updateItemScore(oppKey, response, score);
        }

        //This method is only called via a legacy service self-reference and
        // via the unused TimerScoringService and ItemScoringCallbackHandler
        throw new UnsupportedOperationException("No remote implementation of updateItemScore");
    }

    @Override
    public ItemScore scoreItem(final UUID oppKey, final IItemResponseScorable responseScorable, final IITSDocument itsDoc) throws ReturnStatusException {
        if (legacyCallsEnabled) {
            return legacyItemScoringService.scoreItem(oppKey, responseScorable, itsDoc);
        }

        //This method is only called via a legacy service self-reference and
        // via the unused TimerScoringService and ItemScoringCallbackHandler
        throw new UnsupportedOperationException("No remote implementation of scoreItem");
    }
}
