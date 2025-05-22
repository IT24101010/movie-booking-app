package com.PGN15.movie_booking_app.controller;

import com.PGN15.movie_booking_app.model.Movie;
import com.PGN15.movie_booking_app.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //, indicating it handles incoming requests and returns data directly in the response body (typically as JSON).
@RequestMapping("/api/movies")  //  maps all methods in this class to URLs starting with

public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> addMovie(@RequestBody Movie movieDTO) {
        if (movieDTO.getTitle() == null || movieDTO.getTitle().trim().isEmpty() ||
                movieDTO.getGenre() == null || movieDTO.getGenre().trim().isEmpty() ||
                movieDTO.getDuration() <= 0 ||
                movieDTO.getPrice() < 0 ||
                movieDTO.getReleaseDate() == null) {
            return ResponseEntity.badRequest().body("Title, genre, release date, a valid duration, and a non-negative price are required.");
        }

        Movie newMovie = new Movie(
                movieDTO.getTitle(),
                movieDTO.getGenre(),
                movieDTO.getDirector(),
                movieDTO.getCast(),
                movieDTO.getDuration(),
                movieDTO.getRating(),
                movieDTO.getSynopsis(),
                movieDTO.getReleaseDate(),
                movieDTO.getPrice(),
                movieDTO.getPosterUrl(),
                movieDTO.getBannerUrl()
        );

        Movie addedMovie = movieService.addMovie(newMovie);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedMovie);
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean sortByReleaseDate) {

        if (Boolean.TRUE.equals(sortByReleaseDate)) {
            return ResponseEntity.ok(movieService.getAllMoviesSortedByReleaseDate());
        }
        // Filter logic for title
        if (title != null && !title.trim().isEmpty()) { // Added a check for empty title string
            return ResponseEntity.ok(movieService.searchMoviesByTitle(title));
        }

        return ResponseEntity.ok(movieService.getAllMovies()); // Default: all movies unsorted
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<Movie> getMovieById(@PathVariable String movieId) {
        return movieService.getMovieById(movieId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<?> updateMovie(@PathVariable String movieId, @RequestBody Movie movieDetails) {
        movieDetails.setMovieId(movieId);
        if (movieDetails.getPrice() < 0) {
            return ResponseEntity.badRequest().body("Price cannot be negative.");
        }
        if (movieDetails.getReleaseDate() == null) {
        }

        Movie updatedMovie = movieService.updateMovie(movieDetails);
        if (updatedMovie != null) {
            return ResponseEntity.ok(updatedMovie);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie not found or update failed.");
        }
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String movieId) {
        if (movieService.deleteMovie(movieId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}