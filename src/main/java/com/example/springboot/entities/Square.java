package com.example.springboot.entities;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.mongodb.core.mapping.Document;

// class
@Value
@Jacksonized
@Document(collection = "Square")
@Builder(builderClassName = "Builder", toBuilder = true)
public class Square {
    // creating different aspects: state type name;
     boolean isBlack;
     boolean isKing;
     boolean containsPiece;
     int xCoord;
     int yCoord;
}



