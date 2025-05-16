package com.PGN15.movie_booking_app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private String movieId;
    private String title;
    private String genre;
    private String director;
    private List<String> cast;
    private int duration;
    private String rating;
    private String synopsis;
    private double price;
    private String posterUrl;
    private String bannerUrl;


    public Movie(String title, String genre, String director, List<String> cast, int duration, String rating, String synopsis, double price, String posterUrl, String bannerUrl) {
        this.movieId = UUID.randomUUID().toString();
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.cast = (cast == null) ? new ArrayList<>() : new ArrayList<>(cast); // Handle null cast
        this.duration = duration;
        this.rating = rating;
        this.synopsis = synopsis;
        this.price = price;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
    }


    private static String escapeCsvField(String field) {
        if (field == null) return "" ;
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }


    public String toCsvString() {
        String castString = (cast != null && !cast.isEmpty()) ? String.join(";", cast) : "";

        String  escapedSynopsis =  escapeCsvField(synopsis);
        String escapedPosterUrl  =escapeCsvField(posterUrl) ;
        String  escapedBannerUrl =escapeCsvField(bannerUrl);

        return String.join(",",
                escapeCsvField(movieId) ,
                escapeCsvField(title),
                escapeCsvField(genre),
                escapeCsvField(director),
                escapeCsvField(castString),
                escapeCsvField(String.valueOf(duration)),
                escapeCsvField(rating),
                 escapedSynopsis,
                escapeCsvField(String.valueOf(price)) ,
                escapedPosterUrl ,
                escapedBannerUrl
        );
    }



}