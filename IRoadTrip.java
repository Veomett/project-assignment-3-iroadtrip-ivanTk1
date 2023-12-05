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
    
        Set<String> validCountries = new HashSet<>(countryEndDates.keySet());
        validCountries.retainAll(countryBorders.keySet());
    
        for (String country : validCountries) {
            graph.put(country, new HashMap<>());
        }
    
        for (Map.Entry<String, Set<String>> entry : countryBorders.entrySet()) {
            String country = entry.getKey();
            Map<String, Integer> edges = graph.get(country);
    
            if (edges == null) {
                //System.out.println("DEBUG: No entry for country " + country + " in graph.");
                graph.put(country, new HashMap<>());  // Add a single-vertex entry
                continue;
                    }
    
            for (String neighbor : entry.getValue()) {
                String countryIDA = getIDA(country);
                String neighborIDA = getIDA(neighbor);
    
                if (countryIDA != null && neighborIDA != null &&
                        countryDistances.containsKey(countryIDA) &&
                        countryDistances.get(countryIDA).containsKey(neighborIDA)) {
                    double distance = countryDistances.get(countryIDA).get(neighborIDA);
                    edges.put(neighbor, (int) Math.round(distance));
                } else {
                   // System.out.println("DEBUG: Missing distance information for " + country + " to " + neighbor +
                  //          ". Skipping this edge.");
                }
            }
        }
    
        return graph;
    }
    
    
    private String getIDA(String countryFullName) {
        for (Map.Entry<String, String> entry : countryEndDates.entrySet()) {
            // System.out.println("Checking: " + entry.getKey());  // Use getKey() to get the full name
            // System.out.println("Input: " + countryFullName);
    
            if (entry.getKey().equalsIgnoreCase(countryFullName)) {
               // System.out.println("Test");
                return entry.getValue();
            }
        }
    
        // Print the country for which no match is found
      //  System.out.println("No match found for: " + countryFullName);
    
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
        // Create a map for replacements
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("US", "United States");
        replacementMap.put("Macedonia", "North Macedonia");
        replacementMap.put("Czech Republic", "Czechia");
        replacementMap.put("The Gambia", "Gambia, The");
        replacementMap.put("UK", "United Kingdom");
        replacementMap.put("UAE", "United Arab Emirates");
        replacementMap.put("Turkey (Turkiye)", "Turkey");
        replacementMap.put("Burkina", "Burkina Faso");
        replacementMap.put("Korea, North", "North Korea");
        replacementMap.put("Korea, South", "South Korea");

        // Add more replacements as needed
    
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
                        // Check if replacement exists in the map, otherwise use the original neighbor
                        String result = neighbor.replaceAll("[0-9,]|\\bkm\\b", "");
        
                        // Remove spaces from the beginning and end of the result
                        result = result.trim();
                      //  System.out.println("a" + result + "a");
                    
                        // Remove the last 2 letters, numbers, and commas, and trim spaces
                    
                        borderingCountries.add(result);
                    }
                    
                    borders.put(country, borderingCountries);
                }
            }
        }
    
        // Debugging output
        // System.out.println("Read borders:");
        // for (Map.Entry<String, Set<String>> entry : borders.entrySet()) {
        //     String country = entry.getKey();
        //     Set<String> borderingCountries = entry.getValue();
        //     System.out.print(country + ": ");
        //     StringJoiner joiner = new StringJoiner(",");
        //     for (String neighbor : borderingCountries) {
        //         joiner.add(neighbor);
        //     }
        //     System.out.println(joiner.toString());
        // }
    
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
        //  System.out.println("Read distances:");
        //  for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
        //      String country = entry.getKey();
        //      Map<String, Double> neighborDistances = entry.getValue();
        //      System.out.print(country + " -> ");
        //      for (Map.Entry<String, Double> neighbor : neighborDistances.entrySet()) {
        //          System.out.print(neighbor.getKey() + "(" + neighbor.getValue() + " km) ");
        //      }
        //      System.out.println();
        //  }
        return distances;
    }
    

    public static Map<String, String> readStateName(String filename) throws IOException, ParseException {
        Map<String, String> countries = new HashMap<>();
        Map<String, String> nameMapping = new HashMap<>();  // Add this line
    
        // Manually fill the nameMapping map
        nameMapping.put("Zimbabwe (Rhodesia)", "Zimbabwe");
        nameMapping.put("Tanzania/Tanganyika", "Tanzania");
        nameMapping.put("Congo, Democratic Republic of (Zaire)", "Congo, Democratic Republic of the");
        nameMapping.put("Yemen (Arab Republic of Yemen)", "Yemen");
        nameMapping.put("Vietnam, Democratic Republic of", "Vietnam");
        nameMapping.put("United States of America", "United States");
        nameMapping.put("Kyrgyz Republic", "Kyrgyzstan");
        nameMapping.put("Cambodia (Kampuchea)", "Cambodia");
        nameMapping.put("Myanmar (Burma)", "Burma");
        nameMapping.put("German Federal Republic", "Germany");
        nameMapping.put("Italy/Sardinia", "Italy");
        nameMapping.put("Russia (Soviet Union)", "Russia");
        nameMapping.put("Belarus (Byelorussia)", "Belarus");
        nameMapping.put("Tanzania/Tanganyika", "Tanzania");
        nameMapping.put("Iran (Persia)", "Iran");
        nameMapping.put("Turkey (Ottoman Empire)", "Turkey");
        nameMapping.put("Burkina Faso (Upper Volta)", "Burkina");
        nameMapping.put("East Timor", "Timor-Leste");
        nameMapping.put("Surinam", "Suriname");
        nameMapping.put("Sri Lanka (Ceylon)", "Sri");
        nameMapping.put("Macedonia (Former Yugoslav Republic of)", "North Macedonia");
        nameMapping.put("Czech Republic", "Czechia");
        nameMapping.put("Bahamas, The", "Bahamas");
        nameMapping.put("Bosnia and Herzegovina", "Bosnia-Herzegovina");
        nameMapping.put("Congo, Democratic Republic of (Zaire)", "Congo, Democratic Republic of");
        nameMapping.put("Congo", "Congo, Republic of the");
        nameMapping.put("Swaziland", "Eswatini");
        nameMapping.put("Denmark", "Denmark (Greenland)");
        nameMapping.put("Korea, People's Republic of", "North Korea");
        nameMapping.put("Korea, Republic of", "South Korea");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");
        nameMapping.put("NO", "CAN");

        // Add more mappings as needed
    
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Print for debugging
          //  System.out.println("Read state names:");
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
    
                    // Check if the country name needs to be updated
                    if (nameMapping.containsKey(countryFullName)) {
                        countryFullName = nameMapping.get(countryFullName);
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
    
    public int getDistance(String country1, String country2) {
        if (countryDistances.containsKey(country1) && countryDistances.get(country1).containsKey(country2)) {
            return (int) Math.round(countryDistances.get(country1).get(country2));
        } else {
            return -1;
        }
    }
    public PathInfo findPath(String startCountry, String endCountry) {
        if (!countryGraph.containsKey(startCountry) || !countryGraph.containsKey(endCountry)) {
            System.out.println("Invalid input countries.");
            return new PathInfo(new ArrayList<>(), new ArrayList<>());
        }
    
        // Dijkstra's algorithm
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));
    
        distance.put(startCountry, 0);
        queue.add(startCountry);
    
        while (!queue.isEmpty()) {
            String currentCountry = queue.poll();
            System.out.println("Processing: " + currentCountry + ", Distance: " + distance.get(currentCountry));
    
            if (currentCountry.equals(endCountry)) {
                System.out.println("Reached the destination: " + endCountry);
                break; // Reached the destination
            }
    
            // Null check added here
            Map<String, Integer> neighbors = countryGraph.get(currentCountry);
            if (neighbors == null) {
                System.out.println("No neighbors for: " + currentCountry);
                continue; // Skip if neighbors are null
            }
    
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextCountry = neighbor.getKey();
                int newDist = distance.get(currentCountry) + neighbor.getValue();
                System.out.println("Checking neighbor: " + nextCountry + ", Current Distance: " + distance.get(nextCountry));
    
                if (!distance.containsKey(nextCountry) || newDist < distance.get(nextCountry)) {
                    // Update the distance to the neighboring country if the new distance is shorter
                    distance.put(nextCountry, newDist);
                    previous.put(nextCountry, currentCountry);
                    queue.add(nextCountry);
                    System.out.println("Updated distance for: " + nextCountry + ", New Distance: " + newDist);
                }
            }
        }
    
        // Reconstruct the path
        List<String> path = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        String current = endCountry;
    
        while (previous.containsKey(current)) {
            path.add(0, current);
            distances.add(0, countryGraph.get(previous.get(current)).get(current));
            current = previous.get(current);
        }
    
        path.add(0, startCountry);
        return new PathInfo(path, distances);
    }
    
    

    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);
        String country1;
        String country2;
    
        while (true) {
            System.out.print("Enter the name of the first country (type EXIT to quit): ");
            country1 = scanner.nextLine();
            if (isPresent(country1)) {
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
            if (isPresent(country1)) {
                break;
            } else if (country2.equalsIgnoreCase("EXIT")) {
                System.exit(0);
            } else {
                System.out.println("Invalid country name. Please enter a valid country name.");
            }
        }
    
        PathInfo pathInfo = findPath(country1, country2);
    
        List<String> path = pathInfo.getPath();
        
       // System.out.println(path);

        if (path.size() < 2) {
            System.out.println("No valid path found between " + country1 + " and " + country2 + ".");
        } else {
            System.out.println("Route from " + country1 + " to " + country2 + ":");
    
            List<Integer> distances = pathInfo.getDistances();
    
            for (int i = 0; i < path.size() - 1; i++) {
                System.out.println("* " + path.get(i) + " --> " + path.get(i + 1) + " (" + distances.get(i) + " km.)");
            }
        }
    }

    private boolean isPresent(String country) {
        return countryGraph.containsKey(country);
    }
    

    public class PathInfo {
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
