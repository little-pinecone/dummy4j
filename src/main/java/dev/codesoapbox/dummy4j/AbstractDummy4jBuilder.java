package dev.codesoapbox.dummy4j;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * An abstract dummy builder which can optionally be extended to build classes extending Dummy4j.
 *
 * @see Dummy4jBuilder
 * @since 0.3.1
 */
public abstract class AbstractDummy4jBuilder<T extends AbstractDummy4jBuilder<T, E>, E> {

    protected Long seed;
    protected List<String> locale;
    protected List<String> paths;

    protected AbstractDummy4jBuilder() {
    }

    public T seed(Long seed) {
        self().seed = seed;
        return self();
    }

    protected abstract T self();

    public T locale(List<String> locale) {
        self().locale = locale;
        return self();
    }

    /**
     * @since 0.6.0
     */
    public T locale(String... locale) {
        self().locale = varArgsToList(locale);
        return self();
    }

    private List<String> varArgsToList(String... elements) {
        if (elements.length == 1 && elements[0] == null) {
            return emptyList();
        }

        return asList(elements);
    }

    public T paths(List<String> paths) {
        self().paths = paths;
        return self();
    }

    /**
     * @since 0.6.0
     */
    public T paths(String... paths) {
        self().paths = varArgsToList(paths);
        return self();
    }

    public abstract E build();
}
