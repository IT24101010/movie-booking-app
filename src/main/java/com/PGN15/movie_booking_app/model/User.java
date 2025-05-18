package com.hiruna.movieticketbooking.model;

import lombok.Data;
import lombok.EqualsAndHashCode;//if Two objects with the same data are considered equal
import lombok.NoArgsConstructor;//to generate no arguments Constructor
import lombok.ToString;//to print object in class


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

//User is Child class of BaseUser Class
//Represents a registered user in the system.
public class User extends BaseUser {

    //Overloading Create User objects flexibly, with or without an ID.
    public User(String userId, String username, String password, String email, String contactNumber) {
        super(userId, username, password, email, contactNumber);
    }

    public User(String username, String password, String email, String contactNumber) {
        super(username, password, email, contactNumber);
    }

    //“fromUserCsvString reads a CSV line into a User object, enabling logins and updates from users.csv.”
    public static com.hiruna.movieticketbooking.model.User fromUserCsvString(String csvString) {
        BaseUser baseUser = BaseUser.fromCsvString(csvString); // Utilize base class parsing
        if (baseUser == null) {
            return null;
        }
        // If User had additional fields in CSV, parse them here.
        return new com.hiruna.movieticketbooking.model.User(baseUser.getUserId(), baseUser.getUsername(), baseUser.getPassword(), baseUser.getEmail(), baseUser.getContactNumber());
    }

}

