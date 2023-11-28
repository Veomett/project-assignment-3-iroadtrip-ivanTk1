import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IRoadTrip {

    private Map<String, Map<String, Integer>> countryBorders;
    private Map<String, Map<String, Double>> countryDistances;

    private Map<String, Map<String, Integer>> countryGraph;
    private Set<String> visited;


    public IRoadTrip(String[] args) {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments. Usage: IRoadTrip borders.txt capdist.csv state_name.tsv");
            System.exit(1);
        }

        try {
            // Read borders.txt
            countryBorders = readBorders(args[0]);

            // Read capdist.csv
            countryDistances = readDistances(args[1]);

            // Read state_name.tsv later

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            System.exit(1);
        }
        countryGraph = createGraph();

    }

    private Map<String, Map<String, Integer>> createGraph() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        // Add vertices to the graph
        for (String country : countryBorders.keySet()) {
            graph.put(country, new HashMap<>());
        }

        // Add edges with distances
        for (String country : countryBorders.keySet()) {
            Map<String, Integer> neighbors = countryBorders.get(country);
            Map<String, Integer> edges = graph.get(country);

            for (String neighbor : neighbors.keySet()) {
                int distance = neighbors.get(neighbor);
                edges.put(neighbor, distance);

                // If the graph is undirected, add the reverse edge
                graph.get(neighbor).put(country, distance);
            }
        }

        return graph;
    }



    private Map<String, Map<String, Integer>> readBorders(String filename) throws IOException {
        Map<String, Map<String, Integer>> borders = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String country = parts[0].trim();
                    String[] neighbors = parts[1].split(";");
                    Map<String, Integer> neighborDistances = new HashMap<>();
                    for (String neighbor : neighbors) {
                        String[] neighborParts = neighbor.split("\\s+");
                        if (neighborParts.length == 2) {
                            String neighborCountry = neighborParts[0].trim();
                            int distance = Integer.parseInt(neighborParts[1].trim());
                            neighborDistances.put(neighborCountry, distance);
                        }
                    }
                    borders.put(country, neighborDistances);
                }
            }
        }
        return borders;
    }

    private Map<String, Map<String, Double>> readDistances(String filename) throws IOException {
        Map<String, Map<String, Double>> distances = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Skip the header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String country1 = parts[1].trim();
                    String country2 = parts[3].trim();
                    double kmdist = Double.parseDouble(parts[4].trim());
                    double midist = Double.parseDouble(parts[5].trim());

                    // Add distances for both directions (country1 to country2 and vice versa)
                    distances.computeIfAbsent(country1, k -> new HashMap<>()).put(country2, kmdist);
                    distances.computeIfAbsent(country2, k -> new HashMap<>()).put(country1, kmdist);
                }
            }
        }
        return distances;
    }


    public int getDistance (String country1, String country2) {
        if (countryDistances.containsKey(country1) && countryDistances.get(country1).containsKey(country2)) {
            return (int) Math.round(countryDistances.get(country1).get(country2));
        } else {
            return -1;
        }
    }


    public PathInfo findPath(String startCountry, String endCountry) {
        visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();

        // Use DFS to find the path and distances
        dfs(startCountry, endCountry, path, distances);

        return new PathInfo(path, distances);
    }

    private void dfs(String currentCountry, String destination, List<String> path, List<Integer> distances) {
        visited.add(currentCountry);
        path.add(currentCountry);

        if (currentCountry.equals(destination)) {
            return;
        }

        for (Map.Entry<String, Integer> neighbor : countryGraph.get(currentCountry).entrySet()) {
            if (!visited.contains(neighbor.getKey())) {
                distances.add(neighbor.getValue());  // Add the distance to the list
                dfs(neighbor.getKey(), destination, path, distances);
            }
        }

        if (!path.get(path.size() - 1).equals(destination)) {
            path.remove(path.size() - 1);
            distances.remove(distances.size() - 1);
        }
    }

    public static class PathInfo {
        private List<String> path;
        private List<Integer> distances;

        public PathInfo(List<String> path, List<Integer> distances) {
            this.path = path;
            this.distances = distances;
        }

        public List<String> getPath() {
            return path;
        }

        public List<Integer> getDistances() {
            return distances;
        }
    }



    public void acceptUserInput() {
        // Replace with your code
        System.out.println("IRoadTrip - skeleton");
    }


    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);

        a3.acceptUserInput();
    }

}

