package mah.common.search;


import mah.common.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zgq on 16-11-24.
 */
public class SearcherImplV2 implements Searcher {

    private SearchResultComparator comparator;

    public SearcherImplV2() {
        this.comparator = new SearchResultComparator();
    }

    private static class Result {

        private int prority;
        private List<Integer> matchedIndexs = new ArrayList<>(3);

        Result() {
            prority = 0;
        }

        public void addPrority(int pror) {
            this.prority =prority+ pror;
        }

        public void addMatchedIndex(int index) {
            matchedIndexs.add(index);
        }

        public List<Integer> getMatchedIndexs() {
            return matchedIndexs;
        }

        public int getPrority() {
            return prority;
        }
    }

    private List<SearchResult> fuzzyMatch(List<? extends Searchable> data, String keyword, boolean smart) {
        List<SearchResult> searchResults = new ArrayList<>();
        for (Searchable dataRow : data) {
            int prority = 0;
            List<String> dataCells = dataRow.fetchData();
            Map<Integer, String> matchedColumns = new HashMap<>();
            Map<Integer, List<Integer>> matchedIndexs = new HashMap<>();
            for (int k = 0; k < dataCells.size(); k++) {
                Result result = new Result();
                String columnData = dataCells.get(k);
                computePrority2(columnData, keyword, smart, 0, result);
                List<Integer> matchedIndexs1 = result.getMatchedIndexs();
                if (matchedIndexs1.size() > 0) {
                    matchedColumns.put(k, columnData);
                    matchedIndexs.put(k, matchedIndexs1);
                }
                prority += result.getPrority();
            }
            if (prority > 0) {
                SearchResult searchResult = new SearchResult(dataRow, prority);
                MatchedResult matchedResult = new MatchedResult(matchedColumns, matchedIndexs);
                searchResult.setMatchedResult(matchedResult);
                searchResults.add(searchResult);
            }
        }
        searchResults.sort(comparator);
        return searchResults;
    }

    @Override
    public List<SearchResult> smartFuzzyMatch(List<? extends Searchable> data, String keyword) {
        return fuzzyMatch(data, keyword, true);
    }


    static class Node {
        int prority;
        List<Integer> matchedIndexs;
        List<Integer> realIndexs;

        public Node(int prority, List<Integer> matchedIndexs) {
            this.prority = prority;
            this.matchedIndexs = matchedIndexs;
        }
    }

    protected boolean compare(char c, char c2, boolean smart) {
        if (smart) {
            if (Character.isUpperCase(c)) {
                return c == c2;
            }
            return c == Character.toLowerCase(c2);
        }
        return c == c2;
    }

    protected void computePrority2(String content, String keyword, boolean smart, int prevLen, Result result) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(keyword)) {
            return;
        }
        char fkc = keyword.charAt(0);
        List<Node> matchedNode = new ArrayList<>();
        List<Node> curNodes = new ArrayList<>();
        mat:
        for (int i1 = 0; i1 < content.length(); i1++) {
            char c = content.charAt(i1);
            List<Integer> ni;
            if (compare(fkc, c, smart)) {
                ni = new ArrayList<>();
                // add new node when encountering first char
                curNodes.add(new Node(0, ni));
            }

            for (Node node : curNodes) {
                List<Integer> oi = node.matchedIndexs;
                int os = oi.size();
                if (os < keyword.length()) {
                    // match next char of node
                    char c1 = keyword.charAt(os);
                    if (compare(c1, c, smart)) {
                        oi.add(i1);
                    }
                }
            }

            // select longest node
            int ms = -1;
            int mms = -1;
            for (int i2 = 0; i2 < curNodes.size(); i2++) {
                Node node = curNodes.get(i2);
                List<Integer> mi = node.matchedIndexs;
                int size = mi.size();
                if (size >= mms) {
                    mms = size;
                    ms = i2;
                }
            }

            if (ms != -1) {

                // remove those nodes that are prior to longest node
                Node longestNode = curNodes.get(ms);
                for (int k = ms - 1; k >= 0; k--) {
                    curNodes.remove(k);
                }

                // retrieving completes if there is only node
                List<Integer> nodeMatchedIndexs = longestNode.matchedIndexs;
                if (nodeMatchedIndexs.size() == keyword.length()) {
                    matchedNode.add(longestNode);
                    if (curNodes.size() == 1) {
                        break mat;
                    }
                    // remove matched node
                    curNodes.remove(longestNode);
                }
            }
        }

        int size = matchedNode.size();
        if (size > 0) {
            int end = 0;
            Node node = matchedNode.get(matchedNode.size() - 1);
            List<Integer> mi = node.matchedIndexs;
            if (mi.size() >= 0) {
                end = mi.get(mi.size() - 1);
            }
            Node propNode = selectProrityNode(matchedNode);
            computeProp(propNode, prevLen, result);
            if (end >= content.length() - 1) {
                return;
            }
            computePrority2(content.substring(end + 1, content.length()), keyword, smart, prevLen + end + 1, result);
        }
    }

    private void computeProp(Node propNode, int prevLen, Result result) {
        List<Integer> matchedIndexs = propNode.matchedIndexs;
        for (Integer matchedIndex : matchedIndexs) {
            result.addMatchedIndex(matchedIndex + prevLen);
        }
        result.addPrority(propNode.prority);
    }

    protected final Node selectProrityNode(List<Node> matchedNodes) {
        int prop = 0;
        int ind = 0;
        for (int j = 0; j < matchedNodes.size(); j++) {
            Node node = matchedNodes.get(j);
            List<Integer> matchedIndexs = node.matchedIndexs;
            int q = 0;
            for (int i = matchedIndexs.size() - 1; i >= 1; i--) {
                int ind1 = matchedIndexs.get(i - 1);
                int ind2 = matchedIndexs.get(i);
                if ((ind2 - ind1) == 1) {
                    q += 10;
                }
            }
            if (q > prop) {
                ind = j;
                prop = q;
            }
        }
        Node node = matchedNodes.get(ind);
        node.prority = prop;
        return node;
    }

}
