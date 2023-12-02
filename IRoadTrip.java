import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class IRoadTrip {

    private Map<String, Set<String>> countryBorders;
    private Map<String, Map<String, Double>> countryDistances;

    private Map<String, Map<String, Integer>> countryGraph;
    private Set<String> visited;

    private Map<String, String> countryEndDates;

    public IRoadTrip(String[] args) throws ParseException {
        if (args.length != 3) {
            System.err.println("Invalid number of arguments. Usage: IRoadTrip borders.txt capdist.csv state_name.tsv");
            System.exit(1);
        }

        try {
            // Read borders.txt
            countryBorders = readBorders(args[0]);

            // Read capdist.csv
            countryDistances = readDistances(args[1]);

            // Read state_name.tsv
            countryEndDates = readStateName(args[2]);

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            System.exit(1);
        }
        countryGraph = createGraph();
    }

    private Map<String, Map<String, Integer>> createGraph() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
    
        // Filter out countries that are not in countryEndDates
        Set<String> validCountries = new HashSet<>(countryEndDates.keySet());
        validCountries.retainAll(countryBorders.keySet());
    
        for (String country : validCountries) {
            graph.put(country, new HashMap<>());
        }
    
        for (Map.Entry<String, Set<String>> entry : countryBorders.entrySet()) {
            String country = entry.getKey();
            Map<String, Integer> edges = graph.get(country);
    
            for (String neighbor : entry.getValue()) {
                // Use countryEndDates to get the corresponding full names
                String countryFullName = getFullName(country);
                String neighborFullName = getFullName(neighbor);
             //   System.out.println("TEST");

                String countryIDA = getIDA(country);
                String neighborIDA = getIDA(neighbor);
                System.out.println(country + " " + countryIDA); //keeps returning null why??
                if (countryIDA != null && neighborIDA != null &&
                    countryDistances.containsKey(countryIDA) && 
                    countryDistances.get(countryIDA).containsKey(neighborIDA)) {
                    double distance = countryDistances.get(countryIDA).get(neighborIDA);
                    edges.put(neighbor, (int) Math.round(distance));     
                    System.out.println("TEST2");
                }

            }
        }
    
        // Debugging output
        // System.out.println("Constructed graph:");
        // for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        //     String country = entry.getKey();
        //     Map<String, Integer> neighbors = entry.getValue();
    
        //     System.out.print(country + " -> ");
        //     for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
        //         System.out.print(neighbor.getKey() + "(" + neighbor.getValue() + " km) ");
        //     }
        //     System.out.println();
        // }
    
        return graph;
    }
    
    private String getIDA(String countryFullName) {
        for (Map.Entry<String, String> entry : countryEndDates.entrySet()) {
         //   System.out.println("Checking: " + entry.getKey());  // Use getKey() to get the full name
         //   System.out.println("Input: " + countryFullName);

            if (entry.getKey().equalsIgnoreCase(countryFullName)) {
                System.out.println("Test");
                return entry.getValue();
            }
        }
        // System.out.println("No match found for: " + countryFullName);
        return null; // Handle not found
    }
    
    private String getFullName(String countryCode) {
        for (Map.Entry<String, String> entry : countryEndDates.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(countryCode)) {
                return entry.getKey();
            }
        }
        return null; // Handle not found
    }
    
    
    public static Map<String, Set<String>> readBorders(String filename) throws IOException {
        Map<String, Set<String>> borders = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String country = parts[0].trim();
                    String[] neighbors = parts[1].split(";");
                    Set<String> borderingCountries = new TreeSet<>();  // Using TreeSet for automatic sorting
                    for (String neighbor : neighbors) {
                        String neighborCountry = neighbor.trim().split("\\s+")[0]; // Extracting only the country name
                        borderingCountries.add(neighborCountry);
                    }
                    borders.put(country, borderingCountries);
                }
            }
        }

        // Debugging output
        System.out.println("Read borders:");
        for (Map.Entry<String, Set<String>> entry : borders.entrySet()) {
            String country = entry.getKey();
            Set<String> borderingCountries = entry.getValue();
            System.out.print(country + ": ");
            StringJoiner joiner = new StringJoiner(",");
            for (String neighbor : borderingCountries) {
                joiner.add(neighbor);
            }
            System.out.println(joiner.toString());
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
    
                    // Print for debugging
                  //  System.out.println("Added distance between " + country1 + " and " + country2 + ": " + kmdist);
                }
            }
        }
        // Print for debugging
        // System.out.println("Read distances:");
        // for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
        //     String country = entry.getKey();
        //     Map<String, Double> neighborDistances = entry.getValue();
        //     System.out.print(country + " -> ");
        //     for (Map.Entry<String, Double> neighbor : neighborDistances.entrySet()) {
        //         System.out.print(neighbor.getKey() + "(" + neighbor.getValue() + " km) ");
        //     }
        //     System.out.println();
        // }
        return distances;
    }
    

    public static Map<String, String> readStateName(String filename) throws IOException, ParseException {
        Map<String, String> countries = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Print for debugging
            System.out.println("Read state names:");
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 5) {
                   // System.out.println("test");

                    String countryFullName = parts[2].trim();
                    String countryAbbreviation = parts[1].trim();
                    Date endDate;
                   // System.out.println("test2");

                    try {
                        endDate = dateFormat.parse(parts[4].trim());
                    } catch (ParseException e) {
                        e.printStackTrace(); // Print the stack trace for debugging
                        throw new RuntimeException("Error parsing date", e);
                    }
                    
    
                    // Print for debugging
                    //System.out.println("Read entry: " + countryFullName + " -> " + countryAbbreviation);
    
                    // Only store countries with the last date of 2020-12-31
                    if (endDate.equals(dateFormat.parse("2020-12-31"))) {
                        countries.put(countryFullName, countryAbbreviation);
                    }
                }
            }
        }
        return countries;
    }
    

    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error parsing date: " + dateString, e);
        }
    }

    public int getDistance(String country1, String country2) {
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
    
        dfs(startCountry, endCountry, path, distances);
    
        System.out.println("Visited nodes: " + visited);  // Debugging statement
    
        return new PathInfo(path, distances);
    }
    

    private void dfs(String currentCountry, String destination, List<String> path, List<Integer> distances) {
        System.out.println("Visiting: " + currentCountry);  // Debugging statement
        visited.add(currentCountry);
        path.add(currentCountry);
    
        if (currentCountry.equals(destination)) {
            System.out.println("Reached destination: " + currentCountry);  // Debugging statement
            return;
        }
    
        for (Map.Entry<String, Integer> neighbor : countryGraph.get(currentCountry).entrySet()) {
            if (!visited.contains(neighbor.getKey())) {
                distances.add(neighbor.getValue());
                dfs(neighbor.getKey(), destination, path, distances);
            }
        }
    
        // Check if the path and distances lists are not empty before removing elements
        if (!path.isEmpty() && !distances.isEmpty() && !path.get(path.size() - 1).equals(destination)) {
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

    public boolean checkCountry(String country) {
        return countryGraph.containsKey(country);
    }
    

    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);
        String country1;
        String country2;
    
        while (true) {
            System.out.print("Enter the name of the first country (type EXIT to quit): ");
            country1 = scanner.nextLine();
            if (checkCountry(country1)) {
                break;
            } else if (country1.equalsIgnoreCase("EXIT")) {
                System.exit(0);
            } else {
                System.out.println("Invalid country name. Please enter a valid country name.");
            }
        }
    
        while (true) {
            System.out.print("Enter the name of the second country (type EXIT to quit): ");
            country2 = scanner.nextLine();
            if (checkCountry(country2)) {
                break;
            } else if (country2.equalsIgnoreCase("EXIT")) {
                System.exit(0);
            } else {
                System.out.println("Invalid country name. Please enter a valid country name.");
            }
        }
    
        PathInfo pathInfo = findPath(country1, country2);
    
        List<String> path = pathInfo.getPath();
    
        if (path.isEmpty()) {
            System.out.println("No valid path found between " + country1 + " and " + country2 + ".");
        } else {
            System.out.println("Route from " + country1 + " to " + country2 + ":");
    
            List<Integer> distances = pathInfo.getDistances();
    
            for (int i = 0; i < path.size() - 1; i++) {
                System.out.println("* " + path.get(i) + " --> " + path.get(i + 1) + " (" + distances.get(i) + " km.)");
            }
        }
    }
    
    public void printGraph() {
        for (Map.Entry<String, Map<String, Integer>> entry : countryGraph.entrySet()) {
            String country = entry.getKey();
            Map<String, Integer> neighbors = entry.getValue();
    
            System.out.print(country + " -> ");
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                System.out.print(neighbor.getKey() + "(" + neighbor.getValue() + " km) ");
            }
            System.out.println();
        }
    }
    

    public static void main(String[] args) throws ParseException {
        IRoadTrip a3 = new IRoadTrip(args);
    
        // debugging test

        a3.printGraph();
    
        a3.acceptUserInput();
    }
    
}
