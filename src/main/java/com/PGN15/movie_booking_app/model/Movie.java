package com.PGN15.movie_booking_app.model;

import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
// @AllArgsConstructor
public class Movie {
    private String movieId;
    private String title;
    private String genre;
    private String director;
    private List<String> cast;
    private int duration;
    private String rating;
    private String synopsis;
    private LocalDate releaseDate;
    private double price;
    private String posterUrl;
    private String bannerUrl;

    // Formatter for parsing/formatting LocalDate for CSV
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    // Constructor for creating a new movie with a generated ID.

    public Movie(String title, String genre, String director, List<String> cast, int duration, String rating, String synopsis, LocalDate releaseDate, double price, String posterUrl, String bannerUrl) {
        this.movieId = UUID.randomUUID().toString();
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.cast = (cast == null) ? new ArrayList<>() : new ArrayList<>(cast);
        this.duration = duration;
        this.rating = rating;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.price = price;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
    }

    //All-arguments constructor, including movieId and the new releaseDate field.

    public Movie(String movieId, String title, String genre, String director, List<String> cast, int duration, String rating, String synopsis, LocalDate releaseDate, double price, String posterUrl, String bannerUrl) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.cast = (cast == null) ? new ArrayList<>() : new ArrayList<>(cast);
        this.duration = duration;
        this.rating = rating;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
        this.price = price;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
    }



     // Converts Movie object to a CSV string.

    public String toCsvString() {
        String castString = (cast != null && !cast.isEmpty()) ? String.join(";", cast) : "";
        String releaseDateString = (releaseDate != null) ? releaseDate.format(CSV_DATE_FORMATTER) : "";

        return String.join(",",
                escapeCsvField(movieId),
                escapeCsvField(title),
                escapeCsvField(genre),
                escapeCsvField(director),
                escapeCsvField(castString),
                escapeCsvField(String.valueOf(duration)),
                escapeCsvField(rating),
                escapeCsvField(synopsis),
                escapeCsvField(releaseDateString),
                escapeCsvField(String.valueOf(price)),
                escapeCsvField(posterUrl),
                escapeCsvField(bannerUrl)
        );
    }


     // Creates a Movie object from a CSV string.

    public static Movie fromCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < csvString.length(); i++) {
            char c = csvString.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < csvString.length() && csvString.charAt(i + 1) == '"') {
                    currentPart.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(currentPart.toString());
                currentPart.setLength(0);
            } else {
                currentPart.append(c);
            }
        }
        parts.add(currentPart.toString());

        if (parts.size() < 12) { //
            System.err.println("Malformed CSV string for Movie: \"" + csvString + "\". Expected 12 parts, got " + parts.size());
            System.err.println("Parsed parts: " + parts);
            return null;
        }

        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, unescapeCsvField(parts.get(i)));
        }

        String movieId = parts.get(0);
        String title = parts.get(1);
        String genre = parts.get(2);
        String director = parts.get(3);

        List<String> castList = new ArrayList<>();
        String rawCastString = parts.get(4);
        if (rawCastString != null && !rawCastString.trim().isEmpty()) {
            castList = Arrays.asList(rawCastString.split(";"));
        }

        int durationMovie;
        try {
            durationMovie = Integer.parseInt(parts.get(5));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing duration for movie from CSV part: '" + parts.get(5) + "' in line: " + csvString + " - " + e.getMessage());
            return null;
        }

        String rating = parts.get(6);
        String synopsis = parts.get(7);

        LocalDate releaseDateMovie = null;
        String releaseDateString = parts.get(8);
        if (releaseDateString != null && !releaseDateString.trim().isEmpty()) {
            try {
                releaseDateMovie = LocalDate.parse(releaseDateString, CSV_DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing release date for movie from CSV part: '" + releaseDateString + "' in line: " + csvString + " - " + e.getMessage());
            }
        }

        double priceMovie;
        try {
            priceMovie = Double.parseDouble(parts.get(9));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing price for movie from CSV part: '" + parts.get(9) + "' in line: " + csvString + " - " + e.getMessage());
            return null;
        }

        String posterUrl = parts.get(10);
        String bannerUrl = parts.get(11);

        return new Movie(movieId, title, genre, director,
                castList, durationMovie, rating, synopsis, releaseDateMovie, priceMovie, posterUrl, bannerUrl);
    }

    // Helper to escape CSV fields if they contain comma, quote, or newline
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // Helper to unescape CSV fields
    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        if (field.startsWith("\"") && field.endsWith("\"")) {
            String content = field.substring(1, field.length() - 1);
            return content.replace("\"\"", "\"");
        }
        return field;
    }
}