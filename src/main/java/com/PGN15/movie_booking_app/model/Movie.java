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

@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
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

    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


     // Constructor for creating a new movie with a generated ID.(brand new Movie)

    public Movie(String title, String genre, String director, List<String> cast, int duration, String rating, String synopsis, LocalDate releaseDate, double price, String posterUrl, String bannerUrl) {
        this.movieId = UUID.randomUUID().toString(); // Automatically generate a unique ID
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


     // All-arguments constructor, including movieId. reconstructing movie objects from a CSV  file where the ID  already exists

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



      // Converts this Movie object into a CSV-formatted string.return a csv string

    public String toCsvString() {

        String castString = (this.cast != null && !this.cast.isEmpty()) ? String.join(";", this.cast) : "";
        String releaseDateString = (this.releaseDate != null) ? this.releaseDate.format(CSV_DATE_FORMATTER) : "";

        return String.join(",",
                escapeCsvField(this.movieId),
                escapeCsvField(this.title),
                escapeCsvField(this.genre),
                escapeCsvField(this.director),
                escapeCsvField(castString),
                escapeCsvField(String.valueOf(this.duration)),//  converts its argument into a String.
                escapeCsvField(this.rating),
                escapeCsvField(this.synopsis),
                escapeCsvField(releaseDateString),
                escapeCsvField(String.valueOf(this.price)),
                escapeCsvField(this.posterUrl),
                escapeCsvField(this.bannerUrl)
        );
    }


     // Creates a Movie object from a CSV string or return or null if the CSV string is malformed or essential data is missing/invalid

    public static Movie fromCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }

        List<String> parts = parseCsvLine(csvString);

        // Expect 12 parts for a valid movie CSV line
        if (parts.size() < 12) {
            System.err.println("Malformed CSV string for Movie: \"" + csvString + "\". Expected 12 parts, got " + parts.size());
            System.err.println("Parsed parts: " + parts);
            return null;
        }

        // Unescape each part after parsing
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, unescapeCsvField(parts.get(i)));
        }

        try {
            String movieId = parts.get(0);
            String title = parts.get(1);
            String genre = parts.get(2);
            String director = parts.get(3);

            List<String> castList = new ArrayList<>();
            String rawCastString = parts.get(4);
            if (rawCastString != null && !rawCastString.trim().isEmpty()) {
                castList = Arrays.asList(rawCastString.split(";"));
            }

            int durationMovie = Integer.parseInt(parts.get(5));
            String rating = parts.get(6);
            String synopsis = parts.get(7);

            LocalDate releaseDateMovie = null;
            String releaseDateString = parts.get(8);
            if (releaseDateString != null && !releaseDateString.trim().isEmpty()) {
                releaseDateMovie = LocalDate.parse(releaseDateString, CSV_DATE_FORMATTER);
            }

            double priceMovie = Double.parseDouble(parts.get(9));
            String posterUrl = parts.get(10);
            String bannerUrl = parts.get(11);

            return new Movie(movieId, title, genre, director,
                    castList, durationMovie, rating, synopsis, releaseDateMovie, priceMovie, posterUrl, bannerUrl);

        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric value (duration/price) for movie from CSV: '" + csvString + "' - " + e.getMessage());
            return null;
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing release date for movie from CSV: '" + csvString + "' - " + e.getMessage());
            return null;
        }
    }


     // Helper method to parse a single line of CSV data.

    private static List<String> parseCsvLine(String csvLine) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csvLine.length(); i++) {
            char c = csvLine.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < csvLine.length() && csvLine.charAt(i + 1) == '"') {
                    currentPart.append('"');
                    i++; // Skip the second quote of the pair
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
        return parts;
    }



      // Helper method  to escape special characters in a csv field.

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }


    // Helper to unescape a CSV field that was previously escaped.

    private static String unescapeCsvField(String field) {
        if (field == null) return "";
        if (field.startsWith("\"") && field.endsWith("\"")) {
            String content = field.substring(1, field.length() - 1);
            return content.replace("\"\"", "\"");
        }
        return field;
    }
}