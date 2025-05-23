package com.PGN15.movie_booking_app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ComingSoonMovie extends Movie {

    private String expectedReleasePeriod;

    public ComingSoonMovie(String title, String genre, String director, List<String> cast,
                           String synopsis, LocalDate expectedReleaseDate, String expectedReleasePeriod,
                           String posterUrl, String bannerUrl) {
        super(title, genre, director, cast, 0, "TBA", synopsis, expectedReleaseDate, 0.0, posterUrl, bannerUrl);
        this.expectedReleasePeriod = expectedReleasePeriod;
        this.movieType = "COMING_SOON";
    }

    // Overridden toCsvString() method (Polymorphism).

    @Override
    public String toCsvString() {

        return super.toCsvString();

    }

    @Override
    public String getDisplayStatus() {
        return "Coming Soon! (" + (expectedReleasePeriod != null ? expectedReleasePeriod : "Date TBC") + ")";
    }

    public String getPromotionalMessage() {
        String releaseInfo = (expectedReleasePeriod != null && !expectedReleasePeriod.isEmpty())
                ? expectedReleasePeriod
                : "soon";
        return String.format("Don't miss '%s'! Hitting cinemas %s.", getTitle(), releaseInfo);
    }
}