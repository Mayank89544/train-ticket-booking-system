package ticket_booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket_booking.entities.Train;
import ticket_booking.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {
    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String Trains_Path = System.getProperty("user.dir")
            + "/app/src/main/java/ticket_booking/localDb/trains.json";

    public TrainService() throws IOException {
        trainList = objectMapper.readValue(new File(Trains_Path), new TypeReference<List<Train>>() {});
    }

    public List<Train> searchTrains(String source, String destination) {
        String src = source.toLowerCase();
        String dest = destination.toLowerCase();

        return trainList.stream()
                .filter(train -> validTrain(train, src, dest))
                .collect(Collectors.toList());
    }

    public void addTrain(Train newTrain) throws IOException{
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId().equals(newTrain.getTrainId()))
                .findFirst();

        if(existingTrain.isPresent()){
            updateTrain(newTrain);
        }
        else{
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain)throws IOException{
        OptionalInt index = IntStream.range(0 , trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();
        if (index.isPresent()) {
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() throws IOException {
        File trainFiles = new File(Trains_Path);
        objectMapper.writeValue(trainFiles , trainList);
    }

    private boolean validTrain(Train train, String source, String destination) {
        List<String> stationOrder = train.getStations();

        int sourceIndex = stationOrder.indexOf(source);
        int destinationIndex = stationOrder.indexOf(destination);

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }


}
