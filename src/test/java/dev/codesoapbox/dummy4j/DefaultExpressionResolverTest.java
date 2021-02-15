package dev.codesoapbox.dummy4j;

import dev.codesoapbox.dummy4j.definitions.LocalizedDummyDefinitions;
import dev.codesoapbox.dummy4j.definitions.LocalizedDummyDefinitionsMap;
import dev.codesoapbox.dummy4j.definitions.providers.DefinitionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultExpressionResolverTest {

    @Mock
    private DefinitionProvider definitionProvider;

    @Mock
    private RandomService randomService;

    private DefaultExpressionResolver expressionResolver;

    @BeforeEach
    void setUp() {
        String localeEn = "en";
        String localeFr = "fr";

        LocalizedDummyDefinitions dummyDefinitionsEn =
                new LocalizedDummyDefinitionsMap(localeEn, buildDefinitionMapEn());
        LocalizedDummyDefinitions dummyDefinitionsFr =
                new LocalizedDummyDefinitionsMap(localeFr, buildDefinitionMapFr());
        when(definitionProvider.get())
                .thenReturn(asList(dummyDefinitionsEn, dummyDefinitionsFr));

        expressionResolver = new DefaultExpressionResolver(asList(localeEn, localeFr), randomService,
                definitionProvider);
    }

    private Map<String, Object> buildDefinitionMapEn() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("deep", "value");
        nestedMap.put("advanced", "#{something.deep}123");
        nestedMap.put("empty", emptyList());
        nestedMap.put("special", "$ $$ #{something.special_nested}");
        nestedMap.put("special_nested", "$ $$ \\abc");

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("something", nestedMap);
        rootMap.put("somethingKey", "deep");
        rootMap.put("list", Arrays.asList("1", "#{something.special}"));
        rootMap.put("shared", singletonList("1"));

        return rootMap;
    }

    private Map<String, Object> buildDefinitionMapFr() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("frenchAddition", "value");

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("something", nestedMap);
        rootMap.put("shared", singletonList("2"));

        return rootMap;
    }

    @Test
    void shouldResolveSingleValuePlaceholder() {
        String result = expressionResolver.resolve("#{something.deep}");
        assertEquals("value", result);
    }

    @Test
    void shouldReturnEmptyStringWhenUnableToResolvePlaceholder() {
        LocalizedDummyDefinitions dummyDefinitions = mock(LocalizedDummyDefinitions.class);
        when(definitionProvider.get())
                .thenReturn(singletonList(dummyDefinitions));
        when(dummyDefinitions.resolve(any()))
                .thenReturn(null);
        when(dummyDefinitions.getLocale())
                .thenReturn("en");
        expressionResolver = new DefaultExpressionResolver(singletonList("en"), randomService, definitionProvider);
        String result = expressionResolver.resolve("#{something.notexisting}");
        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyStringWhenResolvedPathIsEmpty() {
        String result = expressionResolver.resolve("#{something.empty}");
        assertEquals("", result);
    }

    @Test
    void shouldResolveExpression() {
        String result = expressionResolver.resolve("#{something.deep}-#{something.deep}");
        assertEquals("value-value", result);
    }

    @Test
    void shouldResolveSingleValuePlaceholderWithSecondaryLocaleIfNotFoundInPrimary() {
        String result = expressionResolver.resolve("#{something.frenchAddition}");
        assertEquals("value", result);
    }

    @Test
    void shouldResolveExpressionWithDigits() {
        when(randomService.nextInt(9))
                .thenReturn(9, 1, 2, 3, 4, 5);
        String result = expressionResolver.resolve("#-##-###");
        assertEquals("9-12-345", result);
    }

    @CsvSource({
            "\\#, #",
            "\\#-\\##-\\#\\##, #-#9-##9"
    })
    @ParameterizedTest
    void shouldNotResolveEscapedHashSymbol(String input, String output) {
        lenient().when(randomService.nextInt(9))
                .thenReturn(9);
        String result = expressionResolver.resolve(input);
        assertEquals(output, result);
    }

    @Test
    void shouldResolvePlaceholderWhichResolvesToExpression() {
        String result = expressionResolver.resolve("#{something.advanced}");
        assertEquals("value123", result);
    }

    @Test
    void shouldResolveSingleLocalePlaceholderWithinSingleLocalePlaceholder() {
        String result = expressionResolver.resolve("#{something.#{somethingKey}}");
        assertEquals("value", result);
    }

    @Test
    void shouldResolveMultiLocalePlaceholderWithinSingleLocalePlaceholder() {
        String result = expressionResolver.resolve("#{something.#{{somethingKey}}}");
        assertEquals("value", result);
    }

    @Test
    void shouldResolveSingleLocalePlaceholderWithinMultiLocalePlaceholder() {
        String result = expressionResolver.resolve("#{{something.#{somethingKey}}}");
        assertEquals("value", result);
    }

    @Test
    void shouldResolveMultiLocalePlaceholderWithinMultiLocalePlaceholder() {
        String result = expressionResolver.resolve("#{{something.#{{somethingKey}}}}");
        assertEquals("value", result);
    }

    @Test
    void shouldNotResolveEscapedHashSymbolWhenResolvingNestedExpression() {
        lenient().when(randomService.nextInt(9))
                .thenReturn(9);
        String result = expressionResolver.resolve("\\##{something.#{somethingKey}}");
        assertEquals("#value", result);
    }

    @Test
    void shouldResolveSpecialChars() {
        String result = expressionResolver.resolve("#{something.special}");
        assertEquals("$ $$ $ $$ \\abc", result);
    }

    @Test
    void shouldResolveMultiLocalePlaceholder() {
        Set<String> expected = new HashSet<>();
        expected.add("1");
        expected.add("2");

        Set<String> result = new HashSet<>();

        mockRandomServiceForFullRange();

        for (int i = 0; i < 10; i++) {
            result.add(expressionResolver.resolve("#{{shared}}"));
        }
        assertEquals(expected, result);
    }

    private void mockRandomServiceForFullRange() {
        AtomicInteger counter = new AtomicInteger();
        doAnswer(inv -> {
            int value = counter.getAndIncrement();
            if(value < (int) inv.getArgument(0)) {
                return value;
            }
            return inv.getArgument(0);
        }).when(randomService).nextInt(anyInt());
    }

    @Test
    void shouldResolveSingleLocalePlaceholder() {
        Set<String> expected = new HashSet<>();
        expected.add("1");

        Set<String> result = new HashSet<>();

        mockRandomServiceForFullRange();

        for (int i = 0; i < 10; i++) {
            result.add(expressionResolver.resolve("#{shared}"));
        }
        assertEquals(expected, result);
    }
}