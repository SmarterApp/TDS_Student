package tds.student.sql.repository.remote;

import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.student.sql.data.OpportunityInstance;
import tds.student.sql.repository.remote.ExamineeNoteRepository;
import tds.student.sql.repository.remote.impl.RemoteExamineeNoteRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteExamineeNoteRepositoryTest {

    @Mock
    private RestTemplate mockRestTemplate;

    private ExamineeNoteRepository remoteExamineeNoteRepository;
    private final OpportunityInstance mockOpportunityInstance = new OpportunityInstance(UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID());

    @Before
    public void setUp() {
        remoteExamineeNoteRepository = new RemoteExamineeNoteRepository(mockRestTemplate,
            "http://localhost:8080/exam");
    }

    @Test
    public void shouldReturnAnExamLevelNote() throws ReturnStatusException {
        ExamineeNote mockNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();

        when(mockRestTemplate.getForEntity(any(URI.class), eq(ExamineeNote.class)))
            .thenReturn(new ResponseEntity<>(mockNote, HttpStatus.OK));

        Optional<ExamineeNote> result =
            remoteExamineeNoteRepository.findNoteInExamContext(new OpportunityInstance(UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()));
        verify(mockRestTemplate).getForEntity(any(URI.class), eq(ExamineeNote.class));

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    public void shouldReturnAnOptionalAbsentWhenAnExamLevelNoteIsNotFound() throws ReturnStatusException {
        when(mockRestTemplate.getForEntity(any(URI.class), eq(ExamineeNote.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Optional<ExamineeNote> result =
            remoteExamineeNoteRepository.findNoteInExamContext(mockOpportunityInstance);
        verify(mockRestTemplate).getForEntity(any(URI.class), eq(ExamineeNote.class));

        assertThat(result.isPresent()).isFalse();
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenAnErrorOtherThan404IsReturned() throws ReturnStatusException {
        when(mockRestTemplate.getForEntity(any(URI.class), eq(ExamineeNote.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        remoteExamineeNoteRepository.findNoteInExamContext(new OpportunityInstance(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()));
        verify(mockRestTemplate).getForEntity(any(URI.class), eq(ExamineeNote.class));
    }

    @Test
    public void shouldInsertAnExamineeNote() throws ReturnStatusException {
        ExamineeNote note = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();

        when(mockRestTemplate.postForEntity(any(URI.class),
            any(ExamineeNote.class),
            eq(ResponseEntity.class)))
            .thenReturn(new ResponseEntity<ResponseEntity>(HttpStatus.NO_CONTENT));

        remoteExamineeNoteRepository.save(mockOpportunityInstance, note);
        verify(mockRestTemplate).postForEntity(any(URI.class),
            any(ExamineeNote.class),
            eq(ResponseEntity.class));
    }

    @Test(expected = ReturnStatusException.class)
    public void shouldThrowReturnStatusExceptionWhenAnErrorOccursDuringInsert() throws ReturnStatusException {
        ExamineeNote note = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam-level note")
            .build();

        when(mockRestTemplate.postForEntity(any(URI.class),
            any(ExamineeNote.class),
            eq(ResponseEntity.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        remoteExamineeNoteRepository.save(mockOpportunityInstance, note);
        verify(mockRestTemplate).postForEntity(any(URI.class),
            any(ExamineeNote.class),
            eq(ResponseEntity.class));
    }
}
