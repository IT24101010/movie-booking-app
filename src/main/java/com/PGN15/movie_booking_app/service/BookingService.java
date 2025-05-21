package com.hiruna.movieticketbooking.service;

import com.hiruna.movieticketbooking.model.Booking;
import com.hiruna.movieticketbooking.model.Movie;
import com.hiruna.movieticketbooking.model.Showtime;
import com.hiruna.movieticketbooking.model.User;
import com.hiruna.movieticketbooking.repository.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Booking management.
 * Handles business logic related to creating, viewing, and managing bookings.
 */
@Service
public class BookingService {

    private final ShowtimeService showtimeService;
    private final UserService userService;
    private final MovieService movieService; // For price calculation or movie validation

    // Example: Define a base price per ticket. This could be more dynamic.
    private static final double BASE_PRICE_PER_TICKET = 10.00;

    /**
     * Constructor with dependency injection for required services.
     * @param showtimeService Service for showtime-related operations.
     * @param userService Service for user-related operations.
     * @param movieService Service for movie-related operations.
     */
    @Autowired
    public BookingService(ShowtimeService showtimeService, UserService userService, MovieService movieService) {
        this.showtimeService = showtimeService;
        this.userService = userService;
        this.movieService = movieService;
    }

    /**
     * Creates a new booking.
     * Validates user, showtime, and seat availability.
     * Updates showtime seat availability upon successful booking.
     * @param booking The booking object to create (ID and timestamp will be set).
     * @return The created booking with ID and timestamp, or null if booking failed.
     */
    public synchronized Booking createBooking(Booking booking) {
        // 1. Validate User
        Optional<User> userOpt = userService.getUserById(booking.getUserId());
        if (userOpt.isEmpty()) {
            System.err.println("Booking creation failed: User ID '" + booking.getUserId() + "' not found.");
            return null;
        }

        // 2. Validate Showtime
        Optional<Showtime> showtimeOpt = showtimeService.getShowtimeById(booking.getShowtimeId());
        if (showtimeOpt.isEmpty()) {
            System.err.println("Booking creation failed: Showtime ID '" + booking.getShowtimeId() + "' not found.");
            return null;
        }
        Showtime showtime = showtimeOpt.get();

        // 3. Validate Movie (associated with showtime)
        Optional<Movie> movieOpt = movieService.getMovieById(showtime.getMovieId());
        if(movieOpt.isEmpty()){
            System.err.println("Booking creation failed: Movie with ID '" + showtime.getMovieId() + "' (associated with showtime) not found.");
            return null;
        }

        // 4. Check seat availability (this is a critical synchronized step)
        if (!showtimeService.checkSeatAvailability(showtime.getShowtimeId(), booking.getNumberOfSeats())) {
            System.err.println("Booking creation failed: Not enough available seats for showtime '" + booking.getShowtimeId() + "'. Requested: " + booking.getNumberOfSeats() + ", Available: " + showtime.getAvailableSeats());
            return null;
        }

        // 5. Calculate total price (can be more complex based on movie, time, seat type)
        // For now, using the booking's pre-calculated price or recalculating it.
        // If the booking object doesn't have a price, calculate it.
        if (booking.getTotalPrice() <= 0) {
            booking.setTotalPrice(calculateTotalPrice(showtime.getMovieId(), booking.getNumberOfSeats()));
        }


        // 6. Update seat availability in Showtime (critical step)
        boolean seatsUpdated = showtimeService.updateSeatAvailability(showtime.getShowtimeId(), booking.getNumberOfSeats());
        if (!seatsUpdated) {
            System.err.println("Booking creation failed: Could not update showtime seat availability for showtime ID '" + showtime.getShowtimeId() + "'.");
            // This indicates a potential concurrency issue or logic error if checkSeatAvailability passed.
            return null;
        }

        // 7. Set final booking details (ID and timestamp are set by Booking constructor)
        // booking.setBookingTimestamp(LocalDateTime.now()); // Already handled by constructor

        // 8. Record the booking
        FileHandler.appendToFile(FileHandler.BOOKINGS_FILE, booking.toCsvString());
        return booking;
    }

    /**
     * Retrieves all bookings made by a specific user.
     * @param userId The ID of the user.
     * @return A list of Booking objects for the given user.
     */
    public List<Booking> getUserBookings(String userId) {
        return getAllBookingsInternal().stream()
                .filter(b -> b.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific booking by its ID.
     * @param bookingId The ID of the booking.
     * @return An Optional containing the Booking if found, otherwise an empty Optional.
     */
    public Optional<Booking> getBookingById(String bookingId) {
        return getAllBookingsInternal().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst();
    }

    /**
     * Retrieves all bookings in the system (typically for admin use).
     * @return A list of all Booking objects.
     */
    public List<Booking> getAllBookings() {
        return getAllBookingsInternal();
    }

    /**
     * Cancels an existing booking.
     * This involves marking the booking (or deleting it) and restoring seat availability.
     * @param bookingId The ID of the booking to cancel.
     * @return true if cancellation was successful, false otherwise (e.g., booking not found).
     */
    public synchronized boolean cancelBooking(String bookingId) {
        Optional<Booking> bookingOpt = getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            System.err.println("Cancellation failed: Booking ID '" + bookingId + "' not found.");
            return false;
        }
        Booking booking = bookingOpt.get();

        // 1. Restore seat availability in Showtime
        // The number of seats to release is the negative of seats booked.
        boolean seatsRestored = showtimeService.updateSeatAvailability(booking.getShowtimeId(), -booking.getNumberOfSeats());
        if (!seatsRestored) {
            System.err.println("Cancellation critical error: Could not restore seat availability for showtime ID '" + booking.getShowtimeId() + "' during cancellation of booking '" + bookingId + "'. Data might be inconsistent.");
            // Depending on policy, you might still proceed to remove the booking record or halt.
            // For this project, we'll proceed but log the error.
        }

        // 2. Delete the booking record
        // (Alternatively, you could add a 'status' field to Booking and mark it as "CANCELLED")
        boolean deleted = FileHandler.deleteLineById(FileHandler.BOOKINGS_FILE, bookingId);
        if (!deleted) {
            System.err.println("Cancellation failed: Could not delete booking record for ID '" + bookingId + "' from file, though seats might have been restored.");
            // This is also a problematic state.
            return false; // Indicate overall cancellation failure if record deletion fails.
        }

        System.out.println("Booking " + bookingId + " cancelled successfully. Seats restored: " + seatsRestored);
        return true;
    }

    /**
     * Deletes a booking record from the system (typically an admin function for cleanup).
     * This method DOES NOT handle seat restoration. For that, use cancelBooking.
     * @param bookingId The ID of the booking to delete.
     * @return true if the booking record was successfully deleted, false otherwise.
     */
    public boolean deleteBookingRecordForAdmin(String bookingId) {
        if (getBookingById(bookingId).isEmpty()) {
            System.err.println("Admin booking deletion failed: Booking ID '" + bookingId + "' not found.");
            return false;
        }
        return FileHandler.deleteLineById(FileHandler.BOOKINGS_FILE, bookingId);
    }


    /**
     * Internal helper method to read and parse all bookings from the file.
     * @return A list of Booking objects.
     */
    private List<Booking> getAllBookingsInternal() {
        return FileHandler.readFile(FileHandler.BOOKINGS_FILE).stream()
                .map(Booking::fromCsvString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total price for a booking (Abstraction example).
     * Price could depend on the movie, showtime (e.g., peak hours), or seat type.
     * @param movieId The ID of the movie (can be used for dynamic pricing).
     * @param numberOfSeats The number of seats being booked.
     * @return The calculated total price. Returns 0.0 if movie not found or other error.
     */
    public double calculateTotalPrice(String movieId, int numberOfSeats) {
        Optional<Movie> movieOpt = movieService.getMovieById(movieId);
        if (movieOpt.isEmpty()) {
            System.err.println("Price calculation failed: Movie with ID '" + movieId + "' not found.");
            return 0.0; // Or throw an exception
        }
        // Movie movie = movieOpt.get();
        // Add logic here if price depends on movie.getRating(), movie.getGenre(), etc.
        // Or if showtime has premium pricing.
        return BASE_PRICE_PER_TICKET * numberOfSeats;
    }
}
