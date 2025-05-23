package com.PGN15.movie_booking_app.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    // We use 'protected' so subclasses can easily access or override it if needed.
    protected String movieType = "REGULAR";

    // Formatter for parsing/formatting LocalDate for CSV (YYYY-MM-DD)
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;



    // Constructor for creating a new movie with a generated ID.(brand new Movie)

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
                escapeCsvField(bannerUrl),
                escapeCsvField(this.movieType)
        );
    }




    // Creates a Movie object from a CSV string or return or null if the CSV string is malformed or essential data is missing/invalid

    public static Movie fromCsvString(String csvString) {
        if (csvString == null || csvString.trim().isEmpty()) {
            return null;
        }

        List<String> parts = parseCsvLine(csvString);

        if (parts.size() < 13) {
            System.err.println("Malformed CSV string for Movie: \"" + csvString + "\". Expected 13 parts, got " + parts.size());
            System.err.println("Parsed parts: " + parts);
            return null;
        }

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
            String type = parts.get(12);

            if ("COMING_SOON".equals(type)) {

                ComingSoonMovie csm = new ComingSoonMovie(title, genre, director, castList, synopsis, releaseDateMovie, "", posterUrl, bannerUrl);
                csm.setMovieId(movieId);
                csm.setDuration(durationMovie);
                csm.setRating(rating);
                csm.setPrice(priceMovie);
                return csm;
            } else {
                // Default to REGULAR Movie
                return new Movie(movieId, title, genre, director,
                        castList, durationMovie, rating, synopsis, releaseDateMovie, priceMovie, posterUrl, bannerUrl);
            }

        } catch (Exception e) {
            System.err.println("Error parsing movie from CSV: '" + csvString + "' - " + e.getMessage());
            return null;
        }
    }

    // Helper method to parse a single line of CSV data.
    private static List<String> parseCsvLine(String csvLine) { /* ... same as before ... */
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csvLine.length(); i++) {
            char c = csvLine.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < csvLine.length() && csvLine.charAt(i + 1) == '"') {
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

    // Example instance method to show polymorphism later
    public String getDisplayStatus() {
        return "Now Playing / Available";
    }
}