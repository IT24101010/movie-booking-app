package com.PGN15.movie_booking_app.service;

import com.PGN15.movie_booking_app.model.Movie;
import com.PGN15.movie_booking_app.model.Showtime;
import com.PGN15.movie_booking_app.repository.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Showtime management.
 * Handles business logic related to movie showtimes.
 */
@Service
public class ShowtimeService {

    private final MovieService movieService; // Dependency for validating movie existence

    /**
     * Constructor with dependency injection for MovieService.
     * @param movieService Service to manage movie data.
     */
    @Autowired // Spring will inject the MovieService instance
    public ShowtimeService(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Adds a new showtime to the system.
     * Validates that the associated movie exists.
     * @param showtime The showtime object to add (ID will be generated if not present).
     * @return The added showtime, or null if the associated movie doesn't exist or add fails.
     */
    public Showtime addShowtime(Showtime showtime) {
        // Validate if movieId exists using the injected MovieService
        Optional<Movie> movieOptional = movieService.getMovieById(showtime.getMovieId());
        if (movieOptional.isEmpty()) {
            System.err.println("Cannot add showtime: Movie with ID '" + showtime.getMovieId() + "' not found.");
            return null;
        }
        // Showtime constructor should generate an ID if not provided.
        FileHandler.appendToFile(FileHandler.SHOWTIMES_FILE, showtime.toCsvString());
        return showtime;
    }

    /**
     * Retrieves a showtime by its unique ID.
     * @param showtimeId The ID of the showtime to find.
     * @return An Optional containing the Showtime if found, otherwise an empty Optional.
     */
    public Optional<Showtime> getShowtimeById(String showtimeId) {
        return getAllShowtimesInternal().stream()
                .filter(st -> st.getShowtimeId().equals(showtimeId))
                .findFirst();
    }

    /**
     * Retrieves all showtimes available in the system.
     * @return A list of all Showtime objects.
     */
    public List<Showtime> getAllShowtimes() {
        return getAllShowtimesInternal();
    }


    /**
     * Retrieves all showtimes for a specific movie.
     * @param movieId The ID of the movie.
     * @return A list of Showtime objects for the given movie.
     */
    public List<Showtime> getShowtimesForMovie(String movieId) {
        return getAllShowtimesInternal().stream()
                .filter(st -> st.getMovieId().equals(movieId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all showtimes scheduled for a specific date.
     * @param date The date to search for showtimes.
     * @return A list of Showtime objects scheduled for the given date.
     */
    public List<Showtime> getShowtimesByDate(LocalDate date) {
        return getAllShowtimesInternal().stream()
                .filter(st -> st.getDateTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing showtime's details.
     * The showtime is identified by its showtimeId.
     * @param updatedShowtime The showtime object with new details.
     * @return The updated showtime object if successful, or null if not found or update failed.
     */
    public Showtime updateShowtime(Showtime updatedShowtime) {
        Optional<Showtime> existingShowtimeOpt = getShowtimeById(updatedShowtime.getShowtimeId());
        if (existingShowtimeOpt.isEmpty()) {
            System.err.println("Showtime update failed: Showtime with ID '" + updatedShowtime.getShowtimeId() + "' not found.");
            return null;
        }
        // Validate movie if movieId is part of the update
        if (movieService.getMovieById(updatedShowtime.getMovieId()).isEmpty()){
            System.err.println("Showtime update failed: Movie with ID '" + updatedShowtime.getMovieId() + "' not found.");
            return null;
        }

        boolean success = FileHandler.updateLineById(FileHandler.SHOWTIMES_FILE, updatedShowtime.getShowtimeId(), updatedShowtime.toCsvString());
        return success ? updatedShowtime : null;
    }

    /**
     * Synchronized method to update the available seat count for a showtime.
     * This is crucial for booking to prevent overbooking.
     * @param showtimeId The ID of the showtime.
     * @param seatsToBookOrRelease The number of seats being booked (negative if releasing/cancelling).
     * @return true if the seat availability was successfully updated, false otherwise (e.g., not enough seats).
     */
    public synchronized boolean updateSeatAvailability(String showtimeId, int seatsToBookOrRelease) {
        Optional<Showtime> showtimeOpt = getShowtimeById(showtimeId);
        if (showtimeOpt.isPresent()) {
            Showtime showtime = showtimeOpt.get();
            if (seatsToBookOrRelease > 0) { // Booking seats
                if (showtime.getAvailableSeats() >= seatsToBookOrRelease) {
                    showtime.setAvailableSeats(showtime.getAvailableSeats() - seatsToBookOrRelease);
                } else {
                    System.err.println("Failed to update seats: Not enough available seats for showtime " + showtimeId);
                    return false; // Not enough seats
                }
            } else { // Releasing seats (e.g. cancellation), seatsToBookOrRelease is negative
                int seatsToRelease = -seatsToBookOrRelease;
                // Ensure available seats do not exceed total seats
                showtime.setAvailableSeats(Math.min(showtime.getTotalSeats(), showtime.getAvailableSeats() + seatsToRelease));
            }
            return FileHandler.updateLineById(FileHandler.SHOWTIMES_FILE, showtimeId, showtime.toCsvString());
        }
        System.err.println("Failed to update seats: Showtime with ID " + showtimeId + " not found.");
        return false; // Showtime not found
    }


    /**
     * Deletes a showtime from the system.
     * @param showtimeId The ID of the showtime to delete.
     * @return true if the showtime was successfully deleted, false otherwise.
     */
    public boolean deleteShowtime(String showtimeId) {
        if (getShowtimeById(showtimeId).isEmpty()) {
            System.err.println("Showtime deletion failed: Showtime with ID '" + showtimeId + "' not found.");
            return false;
        }
        // Consider if there are active bookings for this showtime.
        // A robust system might prevent deletion or handle cascading deletes of bookings.
        return FileHandler.deleteLineById(FileHandler.SHOWTIMES_FILE, showtimeId);
    }

    /**
     * Internal helper method to read and parse all showtimes from the file.
     * @return A list of Showtime objects.
     */
    private List<Showtime> getAllShowtimesInternal() {
        return FileHandler.readFile(FileHandler.SHOWTIMES_FILE).stream()
                .map(Showtime::fromCsvString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a requested number of seats are available for a specific showtime (Abstraction example).
     * @param showtimeId The ID of the showtime.
     * @param seatsRequested The number of seats requested.
     * @return true if enough seats are available, false otherwise or if showtime not found.
     */
    public boolean checkSeatAvailability(String showtimeId, int seatsRequested) {
        return getShowtimeById(showtimeId)
                .map(st -> st.getAvailableSeats() >= seatsRequested)
                .orElse(false); // If showtime not found, seats are not available
    }
}
