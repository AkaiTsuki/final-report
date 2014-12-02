package aprior;

import java.io.*;
import java.util.*;

/**
 * Created by jiachiliu on 11/19/14.
 * <p/>
 * Aprior Algorithm to mining frequent items
 */
public class Aprior {

    List<List<Integer>> frequentItemSets = new ArrayList<List<Integer>>();
    Map<Integer, List<Integer>> transMap = new HashMap<Integer, List<Integer>>();

    public void fit(List<List<Integer>> transactions, int threshold) {
        List<List<Integer>> freqItems = generateOneFrequentItems(transactions, threshold);
        System.out.println("Finished generate 1-freq itemsets");
        int lastSize = frequentItemSets.size();
        System.out.println("1-freqItems: " + freqItems.size());
        frequentItemSets.addAll(freqItems);
        int maxLoop = Integer.MAX_VALUE;
        int loop = 0;

        while (true) {
            loop++;
            System.out.format("============== Loop %d==============\n", loop);
            List<List<Integer>> candidates = selfJoin(freqItems);
            System.out.println("candidates: " + candidates.size());
            prune(candidates);
            freqItems = scan(candidates, transactions, threshold);
            frequentItemSets.addAll(freqItems);
            if (lastSize == frequentItemSets.size() || loop > maxLoop) break;
            lastSize = frequentItemSets.size();
        }
    }

    private List<List<Integer>> scan(List<List<Integer>> candidates, List<List<Integer>> transactions, int threshold) {
        List<List<Integer>> freqItems = new ArrayList<List<Integer>>();

        int i = 0;
        for (List<Integer> candidate : candidates) {
            i++;
            System.out.format("Scanning %d/%d\r", i, candidates.size());
            List<Integer> transactionIds = getMinSupportTransactions(candidate);
            int count = 0;
            for (int t = 0; t < transactionIds.size(); t++) {
                List<Integer> transaction = transactions.get(transactionIds.get(t));
                if (!containsAll(transaction, candidate)) {
                    continue;
                }
                count++;
            }
            if (count >= threshold) {
                freqItems.add(candidate);
            }
        }
        System.out.format("Scanning finished: %d left!\n", freqItems.size());
        return freqItems;
    }

    private List<Integer> getMinSupportTransactions(List<Integer> candidate) {
        int min = Integer.MAX_VALUE;
        List<Integer> minTransaction = null;
        for (int i = 0; i < candidate.size(); i++) {
            List<Integer> transactions = transMap.get(candidate.get(i));
            if (transactions.size() < min) {
                min = transactions.size();
                minTransaction = transactions;
            }
        }
        return minTransaction;
    }

    private boolean containsAll(List<Integer> transaction, List<Integer> candidate) {

//        if (transaction.size() < candidate.size()) return false;
//
//        int c = 0;
//        int t = 0;
//
//        while (c < candidate.size() && t < transaction.size()) {
//            if (transaction.get(t).equals(candidate.get(c))) {
//                t++;
//                c++;
//            } else {
//                t++;
//            }
//        }
//        return c == candidate.size();

//        Set<Integer> trans = new HashSet<Integer>(transaction);
//        return trans.containsAll(candidate);

        return transaction.containsAll(candidate);
    }

    private List<List<Integer>> prune(List<List<Integer>> candidates) {
        List<List<Integer>> pruned = new ArrayList<List<Integer>>();
        for (int i = 0; i < candidates.size(); i++) {
            System.out.format("Pruning %d/%d\r", i, candidates.size());
            List<Integer> candidate = candidates.get(i);
            boolean canAdd = true;
            List<List<Integer>> subsets = getSubsets(candidate);
            for (List<Integer> subset : subsets) {
                if (!this.frequentItemSets.contains(subset)) {
                    canAdd = false;
                    break;
                }
            }
            if (canAdd) {
                pruned.add(candidate);
            }
        }
        System.out.format("Pruning finished: %d left\n!", pruned.size());
        return pruned;
    }

    private List<List<Integer>> getSubsets(List<Integer> candidate) {
        List<List<Integer>> subsets = new ArrayList<List<Integer>>();

        int last = candidate.size() - 1;
        for (int i = last - 1; i >= 0; i--) {
            List<Integer> subset = new ArrayList<Integer>();
            for (int j = 0; j < candidate.size(); j++) {
                if (j != i) {
                    subset.add(candidate.get(j));
                }
            }
            subsets.add(subset);
        }
//        System.out.println("subsets: " + subsets);
        return subsets;
    }

    private List<List<Integer>> selfJoin(List<List<Integer>> freqItems) {
        List<List<Integer>> results = new ArrayList<List<Integer>>();

        if (freqItems.size() == 0)
            return results;

        int len = freqItems.get(0).size();
        if (len == 1) {
            for (int i = 0; i < freqItems.size(); i++) {
                List<Integer> items1 = freqItems.get(i);
                for (int j = i + 1; j < freqItems.size(); j++) {
                    List<Integer> items2 = freqItems.get(j);
                    List<Integer> joined = new ArrayList<Integer>(items1.size() + 1);
                    joined.add(items1.get(0));
                    joined.add(items2.get(0));
                    results.add(joined);
                }
            }
        } else {
            for (int i = 0; i < freqItems.size(); i++) {
                List<Integer> items1 = freqItems.get(i);
                for (int j = i + 1; j < freqItems.size(); j++) {
                    List<Integer> items2 = freqItems.get(j);
                    boolean canJoin = true;
                    List<Integer> joined = new ArrayList<Integer>(items1.size() + 1);
                    for (int k = 0; k < items1.size() - 1; k++) {
                        if (!items1.get(k).equals(items2.get(k))) {
                            canJoin = false;
                            break;
                        }
                        joined.add(items1.get(k));
                    }
                    if (canJoin) {
                        joined.add(Math.min(items1.get(items1.size() - 1), items2.get(items2.size() - 1)));
                        joined.add(Math.max(items1.get(items1.size() - 1), items2.get(items2.size() - 1)));
                        results.add(joined);
                    }
                }
            }
        }


        return results;
    }

    private List<List<Integer>> generateOneFrequentItems(List<List<Integer>> transactions, int threshold) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        for (int t = 0; t < transactions.size(); t++) {
            List<Integer> transaction = transactions.get(t);
            for (Integer i : transaction) {
                if (!map.containsKey(i)) {
                    map.put(i, 1);
                } else {
                    map.put(i, map.get(i) + 1);
                }

                if (!transMap.containsKey(i)) {
                    transMap.put(i, new ArrayList<Integer>());
                }
                transMap.get(i).add(t);
            }
        }


        List<List<Integer>> oneFreq = new ArrayList<List<Integer>>();
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() >= threshold) {
                List<Integer> set = new ArrayList<Integer>();
                set.add(entry.getKey());
                oneFreq.add(set);
            }
        }
        return oneFreq;
    }

    public static List<List<Integer>> loadData(String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));
        List<List<Integer>> data = new ArrayList<List<Integer>>();
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().length() > 0) {
                String transaction = line.split("\t")[1];
                String[] items = transaction.split(",");
                List<Integer> itemSet = new ArrayList<Integer>(items.length);
                for (String i : items) {
                    itemSet.add(Integer.parseInt(i));
                }
                data.add(itemSet);
            }
        }
        return data;
    }

    public List<List<Integer>> getFrequentItemSets() {
        return frequentItemSets;
    }

    public static void main(String[] args) throws Exception {
//        String dataset = "/Users/jiachiliu/IdeaProjects/Hadoop/data/aprior/freqItems.txt";
//        String dataset = "/Users/jiachiliu/IdeaProjects/Hadoop/data/aprior/dataset.txt";
        String dataset = "/Users/jiachiliu/IdeaProjects/Hadoop/data/aprior/transactions.txt";
        List<List<Integer>> transactions = loadData(dataset);
        System.out.println("Transactions: " + transactions.size());
        Aprior clf = new Aprior();
        int threshold = 10;
        clf.fit(transactions, threshold);
        List<List<Integer>> frequentItems = clf.getFrequentItemSets();
//        System.out.println("Final Frequent Item sets: " + frequentItems);
        writeToFile(frequentItems, "frequentItems.txt");

    }

    public static void writeToFile(List<List<Integer>> frequentItems, String path) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        for (List<Integer> f : frequentItems) {
            StringBuilder sb = new StringBuilder();

            for (Integer i : f) {
                if (sb.length() > 0) sb.append(",");
                sb.append(i);
            }

            writer.write(sb.toString() + "\n");
        }

        writer.close();
    }
}
