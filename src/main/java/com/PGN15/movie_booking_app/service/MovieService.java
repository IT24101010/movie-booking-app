package com.PGN15.movie_booking_app.service.;

import com.PGN15.movie_booking_app.model.Movie;
import com.PGN15.movie_booking_app.repository.FileHandler;
import com.PGN15.movie_booking_app.util.SortUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    public Movie addMovie(Movie movie) {
        FileHandler.appendToFile(FileHandler.MOVIES_FILE, movie.toCsvString());
        return movie;
    }

    public Optional<Movie> getMovieById(String movieId) {
        return getAllMoviesInternal().stream()
                .filter(movie -> movie.getMovieId().equals(movieId))
                .findFirst();
    }

    public List<Movie> getAllMovies() {
        return getAllMoviesInternal();
    }


     // Retrieves all movies sorted by their release date in ascending order.

    public List<Movie> getAllMoviesSortedByReleaseDate() {
        List<Movie> movies = getAllMoviesInternal();
        // Create a mutable copy for sorting, as lists from Collectors.toList() might be unmodifiable
        List<Movie> mutableMovies = new ArrayList<>(movies);
        SortUtils.insertionSortMoviesByReleaseDate(mutableMovies);
        return mutableMovies;
    }

    public List<Movie> searchMoviesByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.trim().isEmpty()) {
            return getAllMoviesInternal();
        }
        String lowerCaseQuery = titleQuery.toLowerCase();
        return getAllMoviesInternal().stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }



    public Movie updateMovie(Movie updatedMovie) {
        Optional<Movie> existingMovieOpt = getMovieById(updatedMovie.getMovieId());
        if (existingMovieOpt.isEmpty()) {
            System.err.println("Movie update failed: Movie with ID '" + updatedMovie.getMovieId() + "' not found.");
            return null;
        }
        boolean success = FileHandler.updateLineById(FileHandler.MOVIES_FILE, updatedMovie.getMovieId(), updatedMovie.toCsvString());
        return success ? updatedMovie : null;
    }

    public boolean deleteMovie(String movieId) {
        if (getMovieById(movieId).isEmpty()) {
            System.err.println("Movie deletion failed: Movie with ID '" + movieId + "' not found.");
            return false;
        }
        return FileHandler.deleteLineById(FileHandler.MOVIES_FILE, movieId);
    }

    private List<Movie> getAllMoviesInternal() {
        return FileHandler.readFile(FileHandler.MOVIES_FILE).stream()
                .map(Movie::fromCsvString) // Uses the static method from Movie class
                .filter(Objects::nonNull)     // Filter out any nulls from parsing errors
                .collect(Collectors.toList());
    }


}