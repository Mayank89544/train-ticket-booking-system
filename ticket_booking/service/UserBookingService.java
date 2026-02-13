package ticket_booking.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket_booking.entities.Ticket;
import ticket_booking.entities.Train;
import ticket_booking.entities.User;
import ticket_booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserBookingService {
    private User user;
    private List<User> userList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERS_PATH =  System.getProperty("user.dir")
            + "/app/src/main/java/ticket_booking/localDb/users.json";

    private TrainService trainService;


    public UserBookingService(User user) throws IOException {
        this.user = user;
        this.trainService = new TrainService();  // ⭐
        loadUsers();
    }

    public UserBookingService() throws IOException{
        this.trainService = new TrainService();
        loadUsers();
    }

    public void loadUsers() throws IOException {
        userList = objectMapper.readValue(new File(USERS_PATH), new TypeReference<List<User>>() {});
    }

    public User loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(u ->
                        u.getName().equals(user.getName()) &&
                                UserServiceUtil.checkPassword(
                                        user.getPassword(),
                                        u.getHashedPassword()
                                )
                )
                .findFirst();

        return foundUser.orElse(null);
    }


    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }

    }

    private void saveUserListToFile() throws IOException{
            File usersFile = new File(USERS_PATH);
            objectMapper.writeValue(usersFile , userList);
    }

    public void fetchBooking() {
        if (user == null || user.getTicketsBooked() == null || user.getTicketsBooked().isEmpty()) {
            System.out.println("No tickets booked");
            return;
        }

        int i = 1;
        for (Ticket t : user.getTicketsBooked()) {
            System.out.println(
                    i++ + ". " +
                            t.getSource() + " → " + t.getDestination() +
                            " | Train " + t.getTrainId() +
                            " | Seat (" + (t.getRow()+1) + "," + (t.getCol()+1) + ")"
            );
        }
    }


    public Boolean cancelBooking(String ticketId){
        Boolean cancelled = false;

        for(User user : userList){
            List<Ticket> tickets = user.getTicketsBooked();

            if(tickets != null){
                Boolean removed = tickets.removeIf(ticket -> ticket.getTicketId().equals(ticketId));
                if(removed){
                    cancelled = true;
                    break;
                }
            }
        }

        if(cancelled){
            try {
                saveUserListToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return cancelled;
    }

    public Boolean cancelBookingByIndex(int index) {
        if (user == null || user.getTicketsBooked() == null) return false;

        if (index < 1 || index > user.getTicketsBooked().size()) return false;

        Ticket ticket = user.getTicketsBooked().get(index - 1);
        return cancelBooking(ticket.getTicketId());
    }


    public List<Train> getTrains(String source, String destination){
        return trainService.searchTrains(source, destination);
    }


    public int[][] fetchSeats(Train train){
        return train == null ? null : train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            int[][] seats = train.getSeats();

            if (seats == null) return false;

            if (row < 0 || row >= seats.length) return false;
            if (seat < 0 || seat >= seats[row].length) return false;

            if (seats[row][seat] == 0) {
                seats[row][seat] = 1;
                train.setSeats(seats);
                trainService.addTrain(train); // ⭐ SAME service
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public void addTicketToUser(Ticket ticket) {
        for (User u : userList) {
            if (u.getUserId().equals(ticket.getUserId())) {

                if (u.getTicketsBooked() == null) {
                    u.setTicketsBooked(new ArrayList<>());
                }

                u.getTicketsBooked().add(ticket);

                // ⭐ SYNC logged-in user reference
                this.user = u;

                try {
                    saveUserListToFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }




}
