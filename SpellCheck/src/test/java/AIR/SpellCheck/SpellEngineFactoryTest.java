package AIR.SpellCheck;

import com.atlascopco.hunspell.Hunspell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SpellEngineFactoryTest {
    private static final String EN_US = "ENU";
    private static final String ES_MX = "ESN";

    private static SpellEngineFactory factory;

    @BeforeClass
    public static void setup() {
        final Map<String, String> languages = new HashMap<>();
        languages.put(EN_US, "en_US");
        languages.put(ES_MX, "es_MX");

        factory = new SpellEngineFactory(languages);
    }

    @AfterClass
    public static void tearDown() {
        factory.getDictionary(EN_US).close();
        factory.getDictionary(ES_MX).close();
    }

    @Test
    public void itShouldProvideAnEnUsDictionary() {
        final Hunspell enUs = factory.getDictionary(EN_US);
        assertThat(enUs.isCorrect("happy")).isTrue();
        assertThat(enUs.isCorrect("hapy")).isFalse();

        assertThat(enUs.suggest("hapy")).contains("happy");
    }

    @Test
    public void itShouldProvideAnEsMxDictionary() {
        final Hunspell esMx = factory.getDictionary(ES_MX);
        assertThat(esMx.isCorrect("bien")).isTrue();
        assertThat(esMx.isCorrect("bein")).isFalse();

        assertThat(esMx.suggest("bein")).contains("bien");
    }

    @Test
    public void itShouldDefaultToAnEnUsDictionary() {
        final Hunspell enUs = factory.getDictionary();
        assertThat(enUs.isCorrect("happy")).isTrue();
        assertThat(enUs.isCorrect("bien")).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldThrowWhenFetchingDictionaryForNullLanguage() {
        factory.getDictionary(null);
    }
}