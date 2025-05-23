package com.hiruna.movieticketbooking.controller;

import com.hiruna.movieticketbooking.model.Showtime;
import com.hiruna.movieticketbooking.service.MovieService;
import com.hiruna.movieticketbooking.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import com.hiruna.movieticketbooking.service.BookingService;
import com.hiruna.movieticketbooking.model.Booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for handling Showtime-related HTTP requests.
 * Provides RESTful endpoints for managing movie showtimes.
 */
@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService, MovieService movieService) {
        this.showtimeService = showtimeService;
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> addShowtime(@RequestBody Showtime showtime) {
        if (showtime.getMovieId() == null || showtime.getMovieId().trim().isEmpty() ||
                showtime.getDateTime() == null ||
                showtime.getScreenOrAuditorium() == null || showtime.getScreenOrAuditorium().trim().isEmpty() ||
                showtime.getTotalSeats() <= 0) {
            return ResponseEntity.badRequest().body("Movie ID, dateTime, screen, and a valid totalSeats are required.");
        }
        if (movieService.getMovieById(showtime.getMovieId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Movie with ID " + showtime.getMovieId() + " not found. Cannot add showtime.");
        }

        Showtime newShowtime = new Showtime(
                showtime.getMovieId(),
                showtime.getDateTime(),
                showtime.getScreenOrAuditorium(),
                showtime.getTotalSeats()
        );

        Showtime addedShowtime = showtimeService.addShowtime(newShowtime);
        if (addedShowtime != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(addedShowtime);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add showtime.");
        }
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Showtime>> getShowtimesForMovie(@PathVariable String movieId) {
        return ResponseEntity.ok(showtimeService.getShowtimesForMovie(movieId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getShowtimesByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<Showtime> showtimes = showtimeService.getShowtimesByDate(localDate);
            return ResponseEntity.ok(showtimes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    @GetMapping("/{showtimeId}")
    public ResponseEntity<Showtime> getShowtimeById(@PathVariable String showtimeId) {
        return showtimeService.getShowtimeById(showtimeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{showtimeId}")
    public ResponseEntity<?> updateShowtime(@PathVariable String showtimeId, @RequestBody Showtime showtimeDetails) {
        showtimeDetails.setShowtimeId(showtimeId);

        if (showtimeDetails.getMovieId() != null && movieService.getMovieById(showtimeDetails.getMovieId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Movie with ID " + showtimeDetails.getMovieId() + " not found. Cannot update showtime.");
        }

        Showtime updatedShowtime = showtimeService.updateShowtime(showtimeDetails);
        if (updatedShowtime != null) {
            return ResponseEntity.ok(updatedShowtime);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Showtime not found or update failed.");
        }
    }

    @DeleteMapping("/{showtimeId}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable String showtimeId) {
        if (showtimeService.deleteShowtime(showtimeId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Showtime>> getAllShowtimes() {
        return ResponseEntity.ok(showtimeService.getAllShowtimes());
    }
}