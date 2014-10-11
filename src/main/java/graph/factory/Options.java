package graph.factory;

/**
 * Created by conor on 11/10/2014.
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Allows the client to configure how the graph is set up.
 * For example, a max depth or list of ignore strings.
 */
public class Options {
    private final int maxDepth;
    private final List<String> typeFilters;
    private final List<String> ignoreList;

    public static class Builder {
        private int maxDepth = 1000;
        private List<String> typeFilters = Collections.EMPTY_LIST;
        private List<String> ignoreList = Collections.EMPTY_LIST;

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder typeFilters(List<String> filters) {
            if (filters != null) {
                this.typeFilters = filters;
            }
            return this;
        }

        public Builder typeFilters(String... filters) {
            if (filters != null) {
                this.typeFilters = Arrays.asList(filters);
            }
            return this;
        }

        public Builder ignoreList(List<String> ignores) {
            if (ignores != null) {
                this.ignoreList = ignores;
            }
            return this;
        }

        public Builder ignoreList(String... ignores) {
            if (ignores != null) {
                this.typeFilters = Arrays.asList(ignores);
            }
            return this;
        }

        public Options build() {
            return new Options(this);
        }
    }

    public int getMaxDepth() {
        return this.maxDepth;
    }

    public List<String> getTypeFilters() {
        return this.typeFilters;
    }

    public List<String> getIgnoreList() {
        return this.ignoreList;
    }

    public Options(Builder b) {
        this.ignoreList = b.ignoreList;
        this.maxDepth = b.maxDepth;
        this.typeFilters = b.typeFilters;
    }
}
