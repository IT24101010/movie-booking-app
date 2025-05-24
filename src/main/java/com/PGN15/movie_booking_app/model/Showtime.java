package com.PGN15.movie_booking_app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Represents a Showtime for a Movie.
 * Encapsulation: Showtime details are private with public accessors (Lombok @Data).
 * Association: Contains a movieId, linking it to a Movie object.
 * Abstraction: Methods like toCsvString, fromCsvString, and potentially checkSeatAvailability (in service)
 * abstract underlying complexities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    private String showtimeId;
    private String movieId;           // Foreign key to Movie
    private LocalDateTime dateTime;   // Use ISO-8601 format for string storage (e.g., YYYY-MM-DDTHH:MM:SS)
    private String screenOrAuditorium;
    private int totalSeats;
    private int availableSeats;

    // Formatter for robust date-time parsing and formatting
    private static final DateTimeFormatter CSV_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * Constructor for creating a new showtime with a generated ID.
     * Initially, all seats are available.
     * @param movieId The ID of the movie for this showtime.
     * @param dateTime The date and time of the showtime.
     * @param screenOrAuditorium The screen or auditorium where the movie will be shown.
     * @param totalSeats The total number of seats available for this showtime.
     */
    public Showtime(String movieId, LocalDateTime dateTime, String screenOrAuditorium, int totalSeats) {
        this.showtimeId = UUID.randomUUID().toString();
        this.movieId = movieId;
        this.dateTime = dateTime;
        this.screenOrAuditorium = screenOrAuditorium;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats; // All seats available initially
    }

    /**
     * Converts Showtime object to a CSV string for file storage.
     * Format: showtimeId,movieId,dateTime(ISO),screenOrAuditorium,totalSeats,availableSeats
     * @return CSV string representation of the showtime.
     */
    public String toCsvString() {
        return String.join(",",
                showtimeId,
                movieId,
                dateTime.format(CSV_DATE_TIME_FORMATTER), // Format LocalDateTime to ISO string
                screenOrAuditorium,
                String.valueOf(totalSeats),
                String.valueOf(availableSeats)
        );
    }

    /**
     * Creates a Showtime object from a CSV string.
     * @param csvString The CSV string.
     * @return A Showtime object, or null if the CSV is malformed or data is invalid.
     */
    public static Showtime fromCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvString.split(",", 6); // Expect 6 parts
        if (parts.length < 6) {
            System.err.println("Malformed CSV string for Showtime: " + csvString + ". Expected 6 parts, got " + parts.length);
            return null;
        }

        try {
            String showtimeId = parts[0];
            String movieId = parts[1];
            LocalDateTime dateTimeParsed = LocalDateTime.parse(parts[2], CSV_DATE_TIME_FORMATTER); // Parse ISO string to LocalDateTime
            String screen = parts[3];
            int totalSeatsParsed = Integer.parseInt(parts[4]);
            int availableSeatsParsed = Integer.parseInt(parts[5]);

            return new Showtime(showtimeId, movieId, dateTimeParsed, screen, totalSeatsParsed, availableSeatsParsed);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date-time for Showtime from CSV: " + csvString + " - " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing seat numbers for Showtime from CSV: " + csvString + " - " + e.getMessage());
            return null;
        } catch (Exception e) { // Catch any other unexpected errors during parsing
            System.err.println("Unexpected error parsing Showtime from CSV: " + csvString + " - " + e.getMessage());
            return null;
        }
    }
}
