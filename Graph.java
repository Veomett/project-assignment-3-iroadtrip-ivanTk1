import java.util.HashMap;
import java.util.Map;

class Graph {
    private Map<String, Map<String, Integer>> borders;

    public Graph(Map<String, Map<String, Integer>> borders) {
        this.borders = borders;
    }

    public void printGraph() {
        for (String country : borders.keySet()) {
            System.out.print(country + " borders: ");
            Map<String, Integer> neighbors = borders.get(country);
            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                System.out.print(entry.getKey() + "(" + entry.getValue() + "km) ");
            }
            System.out.println();
        }
    }




}
