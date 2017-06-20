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
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.student.services.remote.impl.RemoteExamineeNoteServiceImpl;
import tds.student.sql.abstractions.IOpportunityRepository;
import tds.student.sql.abstractions.IResponseRepository;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamineeNoteRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamineeNoteServiceImplTest {
    @Mock
    private IOpportunityRepository mockLegacyOpportunityRepository;

    @Mock
    private IResponseRepository mockLegacyResponseRepository;

    @Mock
    private ExamineeNoteRepository mockRemoteExamineeNoteRepository;

    private RemoteExamineeNoteService remoteExamineeNoteService;
    private final OpportunityInstance mockOpportunityInstance = new OpportunityInstance(UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID());

    @Before
    public void setUp() {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            true,
            true);
    }

    @Test
    public void shouldFindAnExamLevelNoteFromLegacyDatabaseWhenLegacyCallsAndRemoteCallsAreEnabled() throws ReturnStatusException {
        ExamineeNote mockNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(mockOpportunityInstance.getExamId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();
        when(mockRemoteExamineeNoteRepository.findNoteInExamContext(any(OpportunityInstance.class)))
            .thenReturn(Optional.of(mockNote));
        when(mockLegacyOpportunityRepository.getComment(any(UUID.class)))
            .thenReturn("Exam-level note");

        final String result = remoteExamineeNoteService.findExamNote(mockOpportunityInstance);
        verify(mockLegacyOpportunityRepository).getComment(any(UUID.class));
        verifyZeroInteractions(mockRemoteExamineeNoteRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);

        assertThat(result).isEqualTo(mockNote.getNote());
    }

    @Test
    public void shouldFindExamLevelNoteWhenLegacyCallsAreEnabledButRemoteCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            true,
            false);

        ExamineeNote mockNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(mockOpportunityInstance.getExamId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();
        when(mockRemoteExamineeNoteRepository.findNoteInExamContext(any(OpportunityInstance.class)))
            .thenReturn(Optional.of(mockNote));
        when(mockLegacyOpportunityRepository.getComment(any(UUID.class)))
            .thenReturn("Exam-level note");

        final String result = remoteExamineeNoteService.findExamNote(mockOpportunityInstance);
        verify(mockLegacyOpportunityRepository).getComment(any(UUID.class));
        verifyZeroInteractions(mockRemoteExamineeNoteRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);

        assertThat(result).isEqualTo(mockNote.getNote());
    }

    @Test
    public void shouldFindExamLevelNoteWhenRemoteCallsAreEnabledButLegacyCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            false,
            true);

        ExamineeNote mockNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(mockOpportunityInstance.getExamId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();
        when(mockRemoteExamineeNoteRepository.findNoteInExamContext(any(OpportunityInstance.class)))
            .thenReturn(Optional.of(mockNote));
        when(mockLegacyOpportunityRepository.getComment(any(UUID.class)))
            .thenReturn("Exam-level note");

        final String result = remoteExamineeNoteService.findExamNote(mockOpportunityInstance);
        verify(mockRemoteExamineeNoteRepository).findNoteInExamContext(any(OpportunityInstance.class));
        verifyZeroInteractions(mockLegacyOpportunityRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);

        assertThat(result).isEqualTo(mockNote.getNote());
    }

    @Test
    public void shouldReturnNullForAnExamWithoutANoteWhenLegacyCallsAreEnabled() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            true,
            false);

        when(mockRemoteExamineeNoteRepository.findNoteInExamContext(any(OpportunityInstance.class)))
            .thenReturn(Optional.<ExamineeNote>absent());
        when(mockLegacyOpportunityRepository.getComment(any(UUID.class)))
            .thenReturn(null);

        final String result = remoteExamineeNoteService.findExamNote(mockOpportunityInstance);

        assertThat(result).isNull();
    }

    @Test
    public void shouldReturnNullForAnExamWithoutANoteWhenRemoteCallsAreEnabled() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            false,
            true);

        when(mockRemoteExamineeNoteRepository.findNoteInExamContext(any(OpportunityInstance.class)))
            .thenReturn(Optional.<ExamineeNote>absent());
        when(mockLegacyOpportunityRepository.getComment(any(UUID.class)))
            .thenReturn(null);

        final String result = remoteExamineeNoteService.findExamNote(mockOpportunityInstance);

        assertThat(result).isNull();
    }

    @Test
    public void shouldInsertAnExamLevelNoteIntoBothDatabases() throws ReturnStatusException {
        doNothing().when(mockLegacyOpportunityRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyString());
        doNothing().when(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));

        remoteExamineeNoteService.saveExamNote(mockOpportunityInstance, 42L, "Exam-level note");
        verify(mockLegacyOpportunityRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyString());
        verify(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));
        verifyZeroInteractions(mockLegacyResponseRepository);
    }

    @Test
    public void shouldInsertAnExamLevelNoteIntoLegacyDatabaseWhenLegacyCallsAreEnabledButRemoteCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            true,
            false);

        doNothing().when(mockLegacyOpportunityRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyString());

        remoteExamineeNoteService.saveExamNote(mockOpportunityInstance, 42L, "Exam-level note");
        verify(mockLegacyOpportunityRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyString());
        verifyZeroInteractions(mockRemoteExamineeNoteRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);
    }

    @Test
    public void shouldInsertAnExamLevelNoteIntoExamDatabaseWhenRemoteCallsAreEnabledButLegacyCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            false,
            true);

        doNothing().when(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));

        remoteExamineeNoteService.saveExamNote(mockOpportunityInstance, 42L, "Exam-level note");
        verify(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));
        verifyZeroInteractions(mockLegacyOpportunityRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);
    }

    @Test
    public void shouldInsertItemLevelNoteIntoBothDatabases() throws ReturnStatusException {
        doNothing().when(mockLegacyResponseRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyInt(),
            anyString());
        doNothing().when(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));

        remoteExamineeNoteService.saveItemNote(mockOpportunityInstance, 42L,
            5,
            "Item-level note");
        verify(mockLegacyResponseRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyInt(),
            anyString());
        verify(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));
        verifyZeroInteractions(mockLegacyOpportunityRepository);
    }

    @Test
    public void shouldInsertItemLevelNoteIntoLegacyDatabaseWhenLegacyCallsAreEnabledButRemoteCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            true,
            false);

        doNothing().when(mockLegacyResponseRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyInt(),
            anyString());

        remoteExamineeNoteService.saveItemNote(mockOpportunityInstance, 42L,
            5,
            "Item-level note");
        verify(mockLegacyResponseRepository).recordComment(any(UUID.class),
            anyLong(),
            any(UUID.class),
            anyInt(),
            anyString());
        verifyZeroInteractions(mockRemoteExamineeNoteRepository);
        verifyZeroInteractions(mockLegacyOpportunityRepository);
    }

    @Test
    public void shouldInsertItemLevelNoteIntoExamDatabaseWhenRemoteCallsAreEnabledButLegacyCallsAreNot() throws ReturnStatusException {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            false,
            true);

        doNothing().when(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));

        remoteExamineeNoteService.saveItemNote(mockOpportunityInstance, 42L,
            5,
            "Item-level note");

        verify(mockRemoteExamineeNoteRepository).save(any(OpportunityInstance.class),
            any(ExamineeNote.class));
        verifyZeroInteractions(mockLegacyOpportunityRepository);
        verifyZeroInteractions(mockLegacyResponseRepository);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenLegacyCallsAndRemoteCallsAreDisabled() {
        remoteExamineeNoteService = new RemoteExamineeNoteServiceImpl(mockLegacyOpportunityRepository,
            mockLegacyResponseRepository,
            mockRemoteExamineeNoteRepository,
            false,
            false);
    }
}