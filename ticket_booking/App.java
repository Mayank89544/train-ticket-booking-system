package ticket_booking;

import ticket_booking.entities.Ticket;
import ticket_booking.entities.Train;
import ticket_booking.entities.User;
import ticket_booking.service.UserBookingService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static ticket_booking.util.UserServiceUtil.hashPassword;

public class App  {
    public static void main(String []args){
        System.out.println("Running the Train Ticket backend program !");
        Scanner s = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;
        try{
            userBookingService = new UserBookingService();
        }catch (IOException ex){
            ex.printStackTrace();
            return;
        }

        Train trainSelectedForBooking = null;
        String selectedSource = "";
        String selectedDestination = "";
        User loggedInUser = null;



        while(option != 7){
            System.out.println("Choose Option");
            System.out.println("1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Booking");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit The App");

            option = s.nextInt();

            switch (option){
                case 1:
                    System.out.println("Enter the UserName to signUp");
                    String nameToSignUp = s.next();
                    System.out.println("Enter the password to SignUp");
                    String passwordToSignUp = s.next();
                    User userToSignUp = new User(nameToSignUp , passwordToSignUp , hashPassword(passwordToSignUp) , new ArrayList<>() , UUID.randomUUID().toString());
                    userBookingService.signUp(userToSignUp);
                    break;
                case 2:
                    System.out.println("Enter the UserName to Login");
                    String nameToLogin = s.next();

                    System.out.println("Enter the password to Login");
                    String passwordToLogin = s.next();

                    User tempUser = new User();
                    tempUser.setName(nameToLogin);
                    tempUser.setPassword(passwordToLogin);

                    try {
                        userBookingService = new UserBookingService(tempUser);
                        User loggedIn = userBookingService.loginUser();

                        if (loggedIn == null) {
                            System.out.println("Invalid username or password");
                            break;
                        }

                        loggedInUser = loggedIn;
                        System.out.println("Login successful");

                    } catch (IOException ex) {
                        System.out.println("Login failed due to system error");
                        return;
                    }
                    break;

                case 3:
                    System.out.println("Fetching the Booking details");
                    userBookingService.fetchBooking();
                    break;
                case 4:
                    System.out.println("Type your source station");
                    String source = s.next();
                    System.out.println("Type your destination station");
                    String dest = s.next();

                    selectedSource = source;
                    selectedDestination = dest;

                    List<Train> trains = userBookingService.getTrains(source, dest);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found for this route");
                        break;
                    }
                    int index = 1;
                    for (Train t: trains){
                        System.out.println(index+" Train id : "+t.getTrainId());
                        for (Map.Entry<String, String> entry: t.getStationTimes().entrySet()){
                            System.out.println("station "+entry.getKey()+" time: "+entry.getValue());
                        }
                        index++;
                    }
                    System.out.println("Select a train by typing 1,2,3...");
                    int choice = s.nextInt();

                    if (choice < 1 || choice > trains.size()) {
                        System.out.println("Invalid train selection");
                        break;
                    }
                    trainSelectedForBooking = trains.get(choice - 1);
                    System.out.println("Train selected successfully!");
                    System.out.println(
                            "DEBUG seats = " + Arrays.deepToString(
                                    trainSelectedForBooking.getSeats()
                            )
                    );

                    break;
                case 5:
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please select a train first (option 4).");
                        break;
                    }

                    int[][] seats = trainSelectedForBooking.getSeats();

                    if (seats == null) {
                        System.out.println("Seat information not available for this train.");
                        break;
                    }

                    System.out.println("Select a seat out of these seats");
                    for (int i = 0; i < seats.length; i++) {
                        for (int j = 0; j < seats[i].length; j++) {
                            System.out.print(seats[i][j] + " ");
                        }
                        System.out.println();
                    }

                    System.out.println("Enter the row (1-based index)");
                    int row = s.nextInt() - 1;

                    System.out.println("Enter the column (1-based index)");
                    int col = s.nextInt() - 1;

                    System.out.println("Booking your seat....");
                    Boolean booked = userBookingService.bookTrainSeat(trainSelectedForBooking, row, col);

                    if (!Boolean.TRUE.equals(booked)) {
                        System.out.println("Can't book this seat");
                        break;
                    }

                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(),
                            loggedInUser.getUserId(),
                            trainSelectedForBooking.getTrainId(),
                            selectedSource,
                            selectedDestination,
                            LocalDate.now().toString() ,row ,col
                    );

                    userBookingService.addTicketToUser(ticket);
                    System.out.println("Booked! Enjoy your journey");
                    break;

                case 6:
                    userBookingService.fetchBooking();
                    System.out.println("Enter booking number to cancel:");
                    int bookingNumber = s.nextInt();

                    Boolean cancelled = userBookingService.cancelBookingByIndex(bookingNumber);
                    if (Boolean.TRUE.equals(cancelled)) {
                        System.out.println("Ticket cancelled successfully");
                    } else {
                        System.out.println("Invalid selection");
                    }
                    break;

                default:
                    break;

            }
        }


    }

    public Object getGreeting() {
        return null;
    }
}
