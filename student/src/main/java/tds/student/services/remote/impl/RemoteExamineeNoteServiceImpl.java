package tds.student.services.remote.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.student.services.remote.RemoteExamineeNoteService;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamineeNoteRepository;

@Service
public class RemoteExamineeNoteServiceImpl implements RemoteExamineeNoteService {
    private final IOpportunityRepository legacyOpportunityRepository;
    private final IResponseRepository legacyResponseRepository;
    private final ExamineeNoteRepository examineeNoteRepository;
    private final boolean isRemoteExamCallsEnabled;
    private final boolean isLegacyCallsEnabled;

    @Autowired
    public RemoteExamineeNoteServiceImpl(final IOpportunityRepository legacyOpportunityRepository,
                                         final IResponseRepository legacyResponseRepository,
                                         final ExamineeNoteRepository examineeNoteRepository,
                                         @Value("${tds.exam.legacy.enabled}") final boolean isLegacyCallsEnabled,
                                         @Value("${tds.exam.remote.enabled}") final boolean isRemoteExamCallsEnabled) {
        if (!isRemoteExamCallsEnabled && !isLegacyCallsEnabled) {
            throw new IllegalStateException("Remote and legacy calls are both disabled.  Please check progman configuration for 'tds.exam.remote.enabled' and 'tds.exam.legacy.enabled' settings");
        }

        this.legacyOpportunityRepository = legacyOpportunityRepository;
        this.legacyResponseRepository = legacyResponseRepository;
        this.examineeNoteRepository = examineeNoteRepository;
        this.isLegacyCallsEnabled = isLegacyCallsEnabled;
        this.isRemoteExamCallsEnabled = isRemoteExamCallsEnabled;
    }

    @Override
    public String findExamNote(final OpportunityInstance opportunityInstance) throws ReturnStatusException {
        if (isLegacyCallsEnabled) {
            return legacyOpportunityRepository.getComment(opportunityInstance.getKey());
        }

        final Optional<ExamineeNote> maybeExamNote = examineeNoteRepository.findNoteInExamContext(opportunityInstance);

        return maybeExamNote.isPresent()
            ? maybeExamNote.get().getNote()
            : null;
    }

    @Override
    public void insertExamNote(final OpportunityInstance opportunityInstance,
                               final long testeeKey ,
                               final String note) throws ReturnStatusException {
        if (isLegacyCallsEnabled) {
            legacyOpportunityRepository.recordComment(opportunityInstance.getSessionKey(),
                testeeKey,
                opportunityInstance.getKey(),
                note);
        }

        if (!isRemoteExamCallsEnabled) {
            return;
        }

        final ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withExamId(opportunityInstance.getExamId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote(note)
            .build();

        examineeNoteRepository.insert(opportunityInstance, examineeNote);
    }

    @Override
    public void insertItemNote(final OpportunityInstance opportunityInstance,
                               final long testeeKey,
                               final int position,
                               final String note) throws ReturnStatusException {

        if (isLegacyCallsEnabled) {
            legacyResponseRepository.recordComment(opportunityInstance.getSessionKey(),
                testeeKey,
                opportunityInstance.getKey(),
                position,
                note);
        }

        if (!isRemoteExamCallsEnabled) {
            return;
        }

        final ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withExamId(opportunityInstance.getExamId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(position)
            .withNote(note)
            .build();

        examineeNoteRepository.insert(opportunityInstance, examineeNote);
    }
}
