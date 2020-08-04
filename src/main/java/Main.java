import java.sql.*;
import java.util.*;

public class Main {

    // Setup
    private static String URL = "jdbc:sqlite::resource:Chinook_Sqlite.sqlite";
    private static Connection conn = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("");
        int customerId = scanner.nextInt();

        Customer customer = getCustomer(customerId);

        if (customer == null) {
            customer = getRandomCustomer();
        }
        printCustomerName(customer);

        ArrayList<String> allGenres = getCustomerGenres(customerId);
        ArrayList<String> genres = getMostPopularGenre(allGenres);

        printMostPopularGenre(genres);
    }

    public static void printCustomerName(Customer customer) {
        System.out.println();
        System.out.println("Customer's full name is \"" + customer.getName() + "\"");
    }

    public static void printMostPopularGenre(ArrayList<String> genres) {
        if (genres.size() > 1) {
            System.out.print("This customers favorite genres are ");
            for (int i = 0; i < genres.size(); i++) {
                while (i < genres.size()) {
                    System.out.print(genres.get(i) + " and ");
                }
            }
            System.out.print(genres.get(genres.size() - 1));
        } else {
            System.out.print("This customers favorite genre is ");
            for (String genre : genres) {
                System.out.print(genre);
            }
        }
    }

    public static Customer getCustomer(int customerId) {
        try {
            // Open Connection
            conn = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");

            // Prepare Statement
            PreparedStatement ps =
                    conn.prepareStatement("SELECT FirstName,LastName FROM customer WHERE CustomerId=?");
            ps.setInt(1, customerId);
            // Execute Statement
            ResultSet resultSet = ps.executeQuery();

            // Process Results
            while (resultSet.next()) {
                Customer customer = new Customer(
                        resultSet.getString(customerId),
                        resultSet.getString("FirstName"),
                        resultSet.getString("LastName")
                );
                return customer;
            }
        } catch (Exception ex) {
            System.out.println("Something went wrong...");
            System.out.println(ex.toString());
        } finally {
            try {
                // Close Connection
                conn.close();
            } catch (Exception ex) {
                System.out.println("Something went wrong while closing connection.");
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public static Customer getRandomCustomer() {
        try {
            // Open Connection
            conn = DriverManager.getConnection(URL);
            System.out.println("Connection to SQLite has been established.");

            // Prepare Statement
            PreparedStatement ps =
                    conn.prepareStatement("SELECT * FROM customer");
            // Execute Statement
            ResultSet resultSet = ps.executeQuery();

            // Process Results
            ArrayList<Customer> customers = new ArrayList<>();
            while (resultSet.next()) {
                customers.add(new Customer(
                        resultSet.getString("CustomerId"),
                        resultSet.getString("FirstName"),
                        resultSet.getString("LastName")
                ));
            }

            int ran = new Random().nextInt(customers.size());

            return customers.get(ran);

        } catch (Exception ex) {
            System.out.println("Something went wrong...");
            System.out.println(ex.toString());
        } finally {
            try {
                // Close Connection
                conn.close();
            } catch (Exception ex) {
                System.out.println("Something went wrong while closing connection.");
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public static ArrayList<String> getCustomerGenres(int customerId) {
        try {
            // Open Connection
            conn = DriverManager.getConnection(URL);

            // Prepare Statement
            PreparedStatement getInvoiceId =
                    conn.prepareStatement("SELECT Genre.Name FROM Genre " +
                            "JOIN Track ON Track.GenreId = Genre.GenreId " +
                            "JOIN InvoiceLine ON InvoiceLine.TrackId = Track.TrackId " +
                            "JOIN Invoice ON InvoiceLine.InvoiceId = Invoice.InvoiceId " +
                            "WHERE CustomerId=?");
            getInvoiceId.setInt(1, customerId);

            // Execute Statement
            ResultSet resultSet = getInvoiceId.executeQuery();

            ArrayList<String> allGenres = new ArrayList<>();
            // Process Results
            while (resultSet.next()) {
                allGenres.add(resultSet.getString("Name"));
            }

            return allGenres;
        } catch (Exception ex) {
            System.out.println("Something went wrong...");
            System.out.println(ex.toString());
        } finally {
            try {
                // Close Connection
                conn.close();
            } catch (Exception ex) {
                System.out.println("Something went wrong while closing connection.");
                System.out.println(ex.toString());
            }
        }
        return null;
    }

    public static ArrayList<String> getMostPopularGenre(ArrayList<String> allGenres) {

        // Insert all unique strings and update count if a string is not unique.
        Map<String, Integer> occurrences = new HashMap<String, Integer>();
        for (String genre : allGenres) {
            if (occurrences.keySet().contains(genre)) // if already exists then update count.
                occurrences.put(genre, occurrences.get(genre) + 1);
            else
                occurrences.put(genre, 1); // else insert it in the map.
        }
        // Traverse the map for the maximum value.
        ArrayList<String> genres = new ArrayList<>();
        String maxGenre = "";
        int maxVal = 0;
        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            String key = entry.getKey();
            Integer count = entry.getValue();
            if (count > maxVal) {
                maxVal = count;
                maxGenre = key;
            }
            // Condition for the tie.
            else if (count == maxVal) {
                genres.add(key);
                genres.add(maxGenre);
            }
        }
        genres.add(maxGenre);
        return genres;
    }
}
