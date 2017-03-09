package AIR.SpellCheck.Engines;

import AIR.SpellCheck.SpellEngineFactory;
import dk.dren.hunspell.Hunspell;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HunspellEngineTest {
    private final static String LANGUAGE_CODE = "languageCode";
    private final static String WORD = "word";

    @Mock
    private SpellEngineFactory spellEngineFactory;

    @Mock
    private Hunspell.Dictionary dictionary;

    private HunspellEngine engine;

    @Before
    public void setup() {
        engine = spy(new HunspellEngine(spellEngineFactory));
        doReturn(LANGUAGE_CODE).when(engine).getLanguage();

        when(spellEngineFactory.getDictionary(LANGUAGE_CODE)).thenReturn(dictionary);
    }

    @Test
    public void itShouldUseDictionaryToDetectCorrectSpelling() {
        when(dictionary.misspelled(WORD)).thenReturn(false);
        assertThat(engine.CheckWord(WORD)).isTrue();

        when(dictionary.misspelled(WORD)).thenReturn(true);
        assertThat(engine.CheckWord(WORD)).isFalse();
    }

    @Test
    public void itShouldReturnCorrectIfDictionaryFails() {
        when(dictionary.misspelled(WORD)).thenThrow(new IllegalStateException("Bad Stuff"));
        assertThat(engine.CheckWord(WORD)).isTrue();
    }

    @Test
    public void itShouldUseDictionaryToGetSuggestions() {
        final List<String> suggestions = Lists.newArrayList("word1", "word2");
        when(dictionary.misspelled(WORD)).thenReturn(true);
        when(dictionary.suggest(WORD)).thenReturn(suggestions);
        assertThat(engine.GetSuggestions(WORD)).containsExactlyElementsOf(suggestions);
    }

    @Test
    public void itShouldNotReturnSuggestionsForACorrectlySpelledWord() {
        final List<String> suggestions = Lists.newArrayList("word1", "word2");
        when(dictionary.misspelled(WORD)).thenReturn(false);
        when(dictionary.suggest(WORD)).thenReturn(suggestions);
        assertThat(engine.GetSuggestions(WORD)).isEmpty();
    }

    @Test
    public void itShouldNotReturnSuggestionsIfDictionaryFails() {
        when(dictionary.misspelled(WORD)).thenThrow(new IllegalArgumentException("Bad Stuff"));
        assertThat(engine.GetSuggestions(WORD)).isEmpty();
    }
}