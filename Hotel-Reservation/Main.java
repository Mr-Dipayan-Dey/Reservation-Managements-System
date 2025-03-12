import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_management";
    private static final String username = "root";
    private static final String password = "2006";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
//            System.out.println("Connection Estublished");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            while (true) {
                System.out.println("\nHOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner, statement);
                        break;
                    case 2:
                        viewReservations(connection, statement);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner, statement);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        catch (InterruptedException e){
            throw  new RuntimeException(e);
        }


    }

    private static void reserveRoom(Connection connection, Scanner scanner, Statement statement) {
        try {
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.print("Enter room number: ");
            int roomNumber = scanner.nextInt();
            System.out.print("Enter contact number: ");
            String contactNumber = scanner.next();
            String insertQuery = "INSERT INTO reservations (guest_name, room_number, contact_number) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, guestName);
            preparedStatement.setInt(2, roomNumber);
            preparedStatement.setString(3, contactNumber);
            int insert = preparedStatement.executeUpdate();

            if (insert > 0) {
                // Step 2: Retrieve the Last Inserted ID
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int reservationId = generatedKeys.getInt(1);
                    String customId = "RES-" + reservationId;

                    // Step 3: Update the custom ID
                    String updateQuery = "UPDATE reservations SET custom_id = ? WHERE reservation_id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, customId);
                    updateStatement.setInt(2, reservationId);
                    updateStatement.executeUpdate();

//                    System.out.println("Reservation Inserted Successfully with ID: " + customId);
                    System.out.println("Reservation Successfull...");
                }
            } else {
                System.out.println("Reservation Failed!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private static void viewReservations(Connection connection, Statement statement) {
        String retrive_query = "SELECT reservation_id,custom_id ,guest_name,contact_number,room_number,reservation_data FROM reservations";
        try {
            ResultSet resultSet = statement.executeQuery(retrive_query);
            System.out.println("Current Reservations:");
//            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+-----------------+");
//            System.out.println("| Reservation ID | Guest           | Room Number   |Contact Number        |Reservation Date         |  Customer Id    |");
//            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+-----------------+");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+-------------------------+");
            System.out.println("| Reservation ID | Customer Id     |Guest          |Contact Number        |Room Number              |Reservation Date         |");
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+-------------------------+");
            while (resultSet.next()) {
                int res_id = resultSet.getInt("reservation_id");
                String cus_name = resultSet.getString("guest_name");
                int room_no = resultSet.getInt("room_number");
                String contact_no = resultSet.getString("contact_number");
                String cus_id=resultSet.getString("custom_id");
                String res_date = resultSet.getTimestamp("reservation_data").toString();

                // Format and display the reservation data in a table-like format

                System.out.printf("| %-14d | %-10s      |%-15s| %-15s      | %-13d           | %-19s   |\n",
                        res_id, cus_id, cus_name, contact_no, room_no, res_date);

            }
            System.out.println("+----------------+-----------------+---------------+----------------------+-------------------------+-------------------------+");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner, Statement statement) {
        try {
            System.out.print("Enter customer ID: ");
            String customerId = scanner.next();
            System.out.print("Enter guest name: ");
            String guestName = scanner.next();
            String Query = "SELECT room_number FROM reservations WHERE custom_id = ? AND guest_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(Query);
            preparedStatement.setString(1, customerId);
            preparedStatement.setString(2, guestName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int room_no = resultSet.getInt("room_number");
                System.out.println("Room number for Reservation ID " + customerId +
                        " and Guest " + guestName + " is: " + room_no);
            } else {
                System.out.println("Data Not Found....");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private static void updateReservation(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            if (!GuestExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }
            System.out.print("Enter new guest name: ");
            String newGuestName = scanner.nextLine();
            System.out.print("Enter new room number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.print("Enter new contact number: ");
            String newContactNumber = scanner.next();
            String sql = "UPDATE reservations SET guest_name = '" + newGuestName + "', " +
                    "room_number = " + newRoomNumber + ", " +
                    "contact_number = '" + newContactNumber + "' " +
                    "WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                System.out.println(affectedRows>0?"Reservation Update Successfully":"Failed! ");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void deleteReservation(Connection connection, Scanner scanner) {
        try {

            // Check if the table is empty
            String checkTableEmpty = "SELECT COUNT(*) FROM reservations";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(checkTableEmpty)) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    System.out.println("No reservations available to delete.");
                    return;
                }
            }

            System.out.print("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();
            if (!GuestExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }
            String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static boolean GuestExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next(); // If there's a result, the reservation exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Handle database errors as needed
        }


    }
   public static void exit() throws InterruptedException{
       System.out.print("Exiting System");
       int i = 5;
       while(i!=0){
           System.out.print(".");
           Thread.sleep(1000);
           i--;
       }
       System.out.println("\nThankYou For Using Hotel Reservation System!!!");
   }

}
