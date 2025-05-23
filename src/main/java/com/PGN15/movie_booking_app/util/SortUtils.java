package com.PGN15.movie_booking_app.util;

import com.PGN15.movie_booking_app.model.Movie;
import java.util.List;
import java.time.LocalDate;


public class SortUtils {


    public static void insertionSortMoviesByReleaseDate(List<Movie> movies) {
        if (movies == null || movies.size() < 2) {
            return;
        }

        int n = movies.size();
        for (int i = 1; i < n; ++i) {
            Movie keyMovie = movies.get(i);
            LocalDate keyReleaseDate = keyMovie.getReleaseDate();
            int j = i - 1;


            while (j >= 0) {
                Movie currentMovieInSortedPart = movies.get(j);
                LocalDate currentReleaseDateInSortedPart = currentMovieInSortedPart.getReleaseDate();

                boolean shouldMove;
                if (keyReleaseDate == null && currentReleaseDateInSortedPart != null) {
                    shouldMove = false; // Nulls go to the end, so don't move a non-null for a null key
                } else if (keyReleaseDate != null && currentReleaseDateInSortedPart == null) {
                    shouldMove = true; // Non-null key should come before a null in the sorted part
                } else if (keyReleaseDate == null && currentReleaseDateInSortedPart == null) {
                    shouldMove = false; // Keep relative order of nulls (or could add secondary sort criteria)
                } else { // Both dates are non-null
                    shouldMove = currentReleaseDateInSortedPart.isAfter(keyReleaseDate);
                }

                if (shouldMove) {
                    movies.set(j + 1, currentMovieInSortedPart);
                    j = j - 1;
                } else {
                    break;
                }
            }
            movies.set(j + 1, keyMovie);
        }
    }
}