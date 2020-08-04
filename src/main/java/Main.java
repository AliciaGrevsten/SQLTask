import java.sql.*;
import java.util.*;

public class Main {

    // Setup
    private static String URL = "jdbc:sqlite::resource:Chinook_Sqlite.sqlite";
    private static Connection conn = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter customer id: ");

        Customer customer;
        int customerId;

        // If input is integer, proceed as usual
        if (scanner.hasNextInt()) {
            customerId = scanner.nextInt();
            customer = getCustomer(customerId);
        }
        // Otherwise fetch random customer
        else {
            System.out.println("Because you didn't provide a valid id " +
                    "I took the liberty to select one for you.");
            customer = getRandomCustomer();
            customerId = Integer.parseInt(customer.getCustomerID());
        }
        // If customer was not found from user input get random customer
        if (customer == null) {
            System.out.println("The id you provided was not found. " +
                    "Therefore I took the liberty to select one for you.");
            customer = getRandomCustomer();
            customerId = Integer.parseInt(customer.getCustomerID());
        }

        // Prints customer details
        printCustomerName(customer);

        // Fetches ALL the genres
        ArrayList<String> allGenres = getCustomerGenres(customerId);
        // If genres are found, proceed with fetching the most popular ones and then print the result
        if (allGenres != null) {
            ArrayList<String> genres = getMostPopularGenre(allGenres);
            printMostPopularGenre(genres);
        } else {
            System.out.println("Something went wrong when fetching the genres..");
        }
    }

    public static void printCustomerName(Customer customer) {
        System.out.println();
        System.out.println("Customer's full name is \"" + customer.getName() + "\" (id: " + customer.getCustomerID() + ")");
    }

    public static void printMostPopularGenre(ArrayList<String> genres) {
        switch (genres.size()) {
            case 1: // If the user has only one favorite genre
                System.out.print("This customers favorite music genre is ");
                for (String genre : genres) {
                    System.out.print(genre);
                }
                break;
            case 0: // If the customer has no favorite genres at all (unlikely)
                System.out.println("This customers favorite music genre was not found..");
                break;
            default: // If the customer has more than one favorite genre
                System.out.print("This customers favorite music genres are ");
                for (int i = 0; i < genres.size(); i++) {
                    while (i < genres.size()) {
                        System.out.print(genres.get(i) + " and ");
                    }
                }
                System.out.print(genres.get(genres.size() - 1));
                break;
        }
    }

    public static Customer getCustomer(int customerId) {
        try {
            // Open Connection
            conn = DriverManager.getConnection(URL);

            // Prepare Statement
            PreparedStatement ps =
                    conn.prepareStatement("SELECT CustomerId, FirstName,LastName FROM customer WHERE CustomerId=?");
            ps.setInt(1, customerId);
            // Execute Statement
            ResultSet resultSet = ps.executeQuery();

            // Process Results
            return new Customer(
                    resultSet.getString("CustomerId"),
                    resultSet.getString("FirstName"),
                    resultSet.getString("LastName")
            );

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

            // Prepare Statement
            PreparedStatement ps =
                    conn.prepareStatement("SELECT * FROM customer");
            // Execute Statement
            ResultSet resultSet = ps.executeQuery();

            // Process Results by creating list of all the customers in the database
            ArrayList<Customer> customers = new ArrayList<>();
            while (resultSet.next()) {
                customers.add(new Customer(
                        resultSet.getString("CustomerId"),
                        resultSet.getString("FirstName"),
                        resultSet.getString("LastName")
                ));
            }

            //  Selects a random number from 0 to size of customer array
            int ran = new Random().nextInt(customers.size());

            //  Returns customer with random id
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

            // Prepare Statement, Select the names of all the genres connected with this customer
            PreparedStatement getInvoiceId =
                    conn.prepareStatement("SELECT Genre.Name FROM Genre " +
                            "JOIN Track ON Track.GenreId = Genre.GenreId " +
                            "JOIN InvoiceLine ON InvoiceLine.TrackId = Track.TrackId " +
                            "JOIN Invoice ON InvoiceLine.InvoiceId = Invoice.InvoiceId " +
                            "WHERE CustomerId=?");
            getInvoiceId.setInt(1, customerId);

            // Execute Statement
            ResultSet resultSet = getInvoiceId.executeQuery();

            // Process Results, stores all the genres in an array
            ArrayList<String> allGenres = new ArrayList<>();
            while (resultSet.next()) {
                allGenres.add(resultSet.getString("Name"));
            }

            // Returns the array
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
        Map<String, Integer> occurrences = new HashMap<>();
        for (String genre : allGenres) {
            if (occurrences.containsKey(genre)) // if already exists then update count.
                occurrences.put(genre, occurrences.get(genre) + 1);
            else
                occurrences.put(genre, 1); // else insert it in the map.
        }
        // Traverse the map for the maximum value. Stores the most popular genre in array.
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

        // Returns array of most popular genre/-s
        return genres;
    }
}
