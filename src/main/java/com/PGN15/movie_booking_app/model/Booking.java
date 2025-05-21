package com.hiruna.movieticketbooking.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a Booking made by a User for a Showtime.
 * Encapsulation: Booking details are private with public accessors (Lombok @Data).
 * Association: Links User (via userId) and Showtime (via showtimeId).
 * Abstraction: Methods like toCsvString, fromCsvString, and potentially calculateTotalPrice (in service)
 * abstract underlying complexities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private String bookingId;
    private String userId;            // Foreign key to User
    private String showtimeId;        // Foreign key to Showtime
    private List<String> seatsBooked; // e.g., ["A1", "A2"] - Stored as semicolon-separated string
    private int numberOfSeats;
    private double totalPrice;
    private LocalDateTime bookingTimestamp; // Use ISO-8601 format

    private static final DateTimeFormatter CSV_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * Constructor for creating a new booking with a generated ID and current timestamp.
     * @param userId The ID of the user making the booking.
     * @param showtimeId The ID of the showtime being booked.
     * @param seatsBooked A list of specific seat identifiers booked (can be empty if not applicable).
     * @param numberOfSeats The total number of seats booked.
     * @param totalPrice The total price for the booking.
     */
    public Booking(String userId, String showtimeId, List<String> seatsBooked, int numberOfSeats, double totalPrice) {
        this.bookingId = UUID.randomUUID().toString();
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatsBooked = (seatsBooked == null) ? new ArrayList<>() : new ArrayList<>(seatsBooked); // Ensure mutable list
        this.numberOfSeats = numberOfSeats;
        this.totalPrice = totalPrice;
        this.bookingTimestamp = LocalDateTime.now(); // Set booking time to current time
    }


    /**
     * Converts Booking object to a CSV string for file storage.
     * SeatsBooked list is joined by semicolons.
     * Format: bookingId,userId,showtimeId,seat1;seat2;...,numberOfSeats,totalPrice,bookingTimestamp(ISO)
     * @return CSV string representation of the booking.
     */
    public String toCsvString() {
        String seatsString = (seatsBooked != null && !seatsBooked.isEmpty()) ? String.join(";", seatsBooked) : "";
        return String.join(",",
                bookingId,
                userId,
                showtimeId,
                seatsString,
                String.valueOf(numberOfSeats),
                String.valueOf(totalPrice),
                bookingTimestamp.format(CSV_DATE_TIME_FORMATTER)
        );
    }

    /**
     * Creates a Booking object from a CSV string.
     * @param csvString The CSV string.
     * @return A Booking object, or null if the CSV is malformed or data is invalid.
     */
    public static Booking fromCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvString.split(",", 7); // Expect 7 parts
        if (parts.length < 7) {
            System.err.println("Malformed CSV string for Booking: " + csvString + ". Expected 7 parts, got " + parts.length);
            return null;
        }

        try {
            String bookingId = parts[0];
            String userId = parts[1];
            String showtimeId = parts[2];

            List<String> seatsList = new ArrayList<>();
            if (parts[3] != null && !parts[3].trim().isEmpty()) {
                seatsList = Arrays.asList(parts[3].split(";"));
            }

            int numSeats = Integer.parseInt(parts[4]);
            double price = Double.parseDouble(parts[5]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[6], CSV_DATE_TIME_FORMATTER);

            // Reconstruct object using the all-args constructor to ensure all fields are set
            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setUserId(userId);
            booking.setShowtimeId(showtimeId);
            booking.setSeatsBooked(seatsList);
            booking.setNumberOfSeats(numSeats);
            booking.setTotalPrice(price);
            booking.setBookingTimestamp(timestamp);
            return booking;

        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date-time for Booking from CSV: " + csvString + " - " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric fields for Booking from CSV: " + csvString + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error parsing Booking from CSV: " + csvString + " - " + e.getMessage());
            return null;
        }
    }
}
