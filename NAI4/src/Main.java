import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    static int k;
    public static void main(String[] args) {
        int vecSize;
        k = Integer.parseInt(args[0]);
        File testSet = new File(args[1]);
        int failures = 0;


        List<Observation> observations = new ArrayList<>();
        ConcurrentHashMap<String, List<List<Double>>> testMap = new ConcurrentHashMap<>();
        try (
                BufferedReader testSetReader = new BufferedReader(new FileReader(testSet.getAbsolutePath()));
        ){
            while (testSetReader.ready())
                addToMap(testMap, observations, testSetReader.readLine().split(","));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConcurrentHashMap<Integer, CopyOnWriteArrayList<Observation>> groupMap = new ConcurrentHashMap<>();
        for (Observation observation : observations) {
            if (!groupMap.containsKey(observation.group)) {
                CopyOnWriteArrayList<Observation> list = new CopyOnWriteArrayList<>();
                list.add(observation);
                groupMap.put(observation.group, list);
            } else {
                groupMap.get(observation.group).add(observation);
            }
        }
        System.out.println(groupMap);
        for (int i = 0; i < groupMap.size(); i++) {
            System.out.print("Group " + i + ": ");
            System.out.println(groupMap.get(i).size());
        }

        Observation[] centroids = new Observation[k];
        updateCentroids(groupMap, centroids);
        System.out.println(Arrays.toString(centroids));

        double distSum1 = 0;
        for (int i = 0; i < k; i++) {
            int finalI = i;
            distSum1 += groupMap.get(i).stream().mapToDouble(e -> e.getDistance(centroids[finalI].vec)).sum();
        }
        System.out.println("Distance sum: " + distSum1);

        ConcurrentHashMap<Integer, CopyOnWriteArrayList<Observation>> tmpGroupMap = new ConcurrentHashMap<>();
        do {
            System.out.println();
            tmpGroupMap = deepCopyWorkAround(groupMap);
            System.out.println(tmpGroupMap);

//          (a) Dla każdego przykładu znajdujemy najbliższy centroid i przypisujemy go do
//          jego grupy.
            for (int i = 0; i < k; i++) {
                List<Observation> list = groupMap.get(i);

                for (int j = 0; j < k; j++) {
                    int finalJ = j;
                    groupMap.get(j).forEach(e -> {
                        e.group = getClosestCentroidIndex(e, centroids);
                        if (e.group != finalJ){
                            groupMap.get(finalJ).remove(e);
                            groupMap.get(e.group).add(e);
                        }
                    });
                }

//                for (Iterator<Observation> iterator = groupMap.get(i).iterator(); iterator.hasNext();){
//                    Observation e = iterator.next();
//                    e.group = getClosestCentroidIndex(e, centroids);
//                    if (e.group != i){
//                        iterator.remove();
//                        groupMap.get(e.group).add(e);
//                    }
//                }

            }


//          (b) Dla każdej grupy wyliczamy nowy centroid, będący uśrednieniem wszystkich
//          punktów z grupy.
            updateCentroids(groupMap, centroids);

//          Po każdej iteracji: wypisywać sumę odległości przykładów od ich centroidów. Ta wartość powinna zmniejszać się z każdą iteracją.
//          Uwaga: wypisujemy sumę dla wszystkich przykładów, a nie każdej grupy osobno.
            double distSum = 0;
            for (int i = 0; i < k; i++) {
                int finalI = i;
                distSum += groupMap.get(i).stream().mapToDouble(e -> e.getDistance(centroids[finalI].vec)).sum();
            }
            System.out.println("Distance sum: " + distSum);


            for (int i = 0; i < groupMap.size(); i++) {
                System.out.print("Group " + i + ": ");
                System.out.println(groupMap.get(i).size());
            }

        } while (!areEqual(groupMap, tmpGroupMap));

    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

    public static <T> boolean areEqual(ConcurrentHashMap<Integer, CopyOnWriteArrayList<T>> first, ConcurrentHashMap<Integer, CopyOnWriteArrayList<T>> second) {
        return first.entrySet().stream()
                .allMatch(e -> listEqualsIgnoreOrder(e.getValue(), second.get(e.getKey())));
    }

    public static <T> ConcurrentHashMap<Integer, CopyOnWriteArrayList<T>> deepCopyWorkAround(ConcurrentHashMap<Integer, CopyOnWriteArrayList<T>> original)
    {
        ConcurrentHashMap<Integer, CopyOnWriteArrayList<T>> copy = new ConcurrentHashMap<>();
        for (Map.Entry<Integer, CopyOnWriteArrayList<T>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new CopyOnWriteArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public static void updateCentroids(ConcurrentHashMap<Integer, CopyOnWriteArrayList<Observation>> groupMap, Observation[] centroids) {
        for (int i = 0; i < k; i++) {
            Observation centroid = new Observation(i, .0, .0, .0, .0);
            if (groupMap.get(i).size() > 0) {
                groupMap.get(i).forEach(e -> centroid.addVector(e.vec));
                centroid.divideVector(groupMap.get(i).size());
            }
            centroids[i] = centroid;
        }
    }

    public static int getClosestCentroidIndex(Observation observation, Observation[] centroids){
        double minDist = observation.getDistance(centroids[0].vec);
        int minDistCentroidIndex = 0;
        for (int i = 0; i < centroids.length; i++) {
            double currDist = observation.getDistance(centroids[i].vec);
            if (currDist < minDist) {
                minDist = currDist;
                minDistCentroidIndex = i;
            }
        }
        return minDistCentroidIndex;
    }

    public static void addToMap(ConcurrentHashMap<String, List<List<Double>>> map, List<Observation> observations, String[] currVec){
        if (map.get(currVec[currVec.length - 1]) == null) {
            observations.add(new Observation((int) (Math.random() * k), convertToList(currVec)));
            map.put(currVec[currVec.length - 1], new ArrayList<>(Collections.singleton(convertToList(currVec))));
        } else {
            observations.add(new Observation((int) (Math.random() * k), convertToList(currVec)));
            map.get(currVec[currVec.length - 1]).add(convertToList(currVec));
        }

    }

    public static List<Double> convertToList(String[] vec){
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < vec.length-1; i++) {
            list.add(Double.parseDouble(vec[i]));
        }
        return list;
    }
}
