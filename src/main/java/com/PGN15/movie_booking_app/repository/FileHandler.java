package com.PGN15.movie_booking_app.repository;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class FileHandler {


    public static final String DATA_DIRECTORY = System.getProperty("user.dir") + File.separator + "data";

    // Define file names as constants
    public static final String USERS_FILE = DATA_DIRECTORY + File.separator + "users.txt";
    public static final String MOVIES_FILE = DATA_DIRECTORY + File.separator + "movies.txt";
    public static final String SHOWTIMES_FILE = DATA_DIRECTORY + File.separator + "showtimes.txt";
    public static final String BOOKINGS_FILE = DATA_DIRECTORY + File.separator + "bookings.txt";
    public static final String ADMINS_FILE = DATA_DIRECTORY + File.separator + "admins.txt";


     // Static initializer block to create the data directory and files if they don't exist.

    static {
        Path dataDirPath = Paths.get(DATA_DIRECTORY);
        if (Files.notExists(dataDirPath)) {
            try {
                Files.createDirectories(dataDirPath);
                System.out.println("Data directory created at: " + DATA_DIRECTORY);
            } catch (IOException e) {
                System.err.println("Error creating data directory: " + e.getMessage());
            }
        }
        // Ensure files are created
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(MOVIES_FILE);
        createFileIfNotExists(SHOWTIMES_FILE);
        createFileIfNotExists(BOOKINGS_FILE);
        createFileIfNotExists(ADMINS_FILE);
    }


     // Creates a file if it does not already exist.

    private static void createFileIfNotExists(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
                System.out.println("Created file: " + filePath);
            } catch (IOException e) {
                System.err.println("Error creating file " + filePath + ": " + e.getMessage());
            }
        }
    }



      // Reads all lines from a specified file.

    public static synchronized List<String> readFile(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            System.err.println("File not found: " + filePath + ". Returning empty list.");
            createFileIfNotExists(filePath);
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Writes a list of lines to a specified file, overwriting any existing content.

    public static synchronized void writeFile(String filePath, List<String> lines) {
        Path path = Paths.get(filePath);
        createFileIfNotExists(filePath); // Ensure file exists before writing
        try {
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }

    //Appends a single line to a specified file.

    public static synchronized void appendToFile(String filePath, String line) {
        Path path = Paths.get(filePath);
        createFileIfNotExists(filePath); // Ensure file exists before appending
        try {
            // Ensure the line ends with a newline character
            String lineToWrite = line.endsWith(System.lineSeparator()) ? line : line + System.lineSeparator();
            Files.writeString(path, lineToWrite, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error appending to file " + filePath + ": " + e.getMessage());
        }
    }



    //Deletes a line from a file that contains a specific identifier.


    public static synchronized boolean deleteLineById(String filePath, String identifier) {
        List<String> lines = readFile(filePath);
        List<String> updatedLines = lines.stream()
                .filter(line -> {
                    if (line.trim().isEmpty()) return false;
                    String[] parts = line.split(",", 2);
                    return parts.length > 0 && !parts[0].trim().equals(identifier);
                })
                .collect(Collectors.toList());

        if (lines.size() != updatedLines.size()) {
            writeFile(filePath, updatedLines);
            return true;
        }
        return false;
    }

    // Updates a line in a file that contains a specific identifier.

    public static synchronized boolean updateLineById(String filePath, String identifier, String newLineContent) {
        List<String> lines = readFile(filePath);
        boolean updated = false;
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) { // Preserve genuinely empty lines if any, or skip
                updatedLines.add(line); // Or `continue;` if they should be removed
                continue;
            }
            String[] parts = line.split(",", 2);
            if (parts.length > 0 && parts[0].trim().equals(identifier)) {
                updatedLines.add(newLineContent);
                updated = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (updated) {
            writeFile(filePath, updatedLines);
        }
        return updated;
    }
}
