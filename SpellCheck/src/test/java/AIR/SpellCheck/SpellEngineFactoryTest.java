package AIR.SpellCheck;

import dk.dren.hunspell.Hunspell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SpellEngineFactoryTest {
    private static final String EN_US = "ENU";
    private static final String ES_MX = "ESN";

    private static Map<String, String> languages;
    private static SpellEngineFactory factory;

    @BeforeClass
    public static void setup() {
        languages = new HashMap<>();
        languages.put(EN_US, "en_US");
        languages.put(ES_MX, "es_MX");

        factory = new SpellEngineFactory(languages);
    }

    @AfterClass
    public static void tearDown() {
        factory.getDictionary(EN_US).destroy();
        factory.getDictionary(ES_MX).destroy();
    }

    @Test
    public void itShouldProvideAnEnUsDictionary() {
        final Hunspell.Dictionary enUs = factory.getDictionary(EN_US);
        assertThat(enUs.misspelled("happy")).isFalse();
        assertThat(enUs.misspelled("hapy")).isTrue();
    }

    @Test
    public void itShouldProvideAnEsMxDictionary() {
        final Hunspell.Dictionary enUs = factory.getDictionary(ES_MX);
        assertThat(enUs.misspelled("bien")).isFalse();
        assertThat(enUs.misspelled("bein")).isTrue();
    }

    @Test
    public void itShouldDefaultToAnEnUsDictionary() {
        final Hunspell.Dictionary enUs = factory.getDictionary();
        assertThat(enUs.misspelled("happy")).isFalse();
        assertThat(enUs.misspelled("bien")).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldThrowWhenFetchingDictionaryForNullLanguage() {
        factory.getDictionary(null);
    }
}