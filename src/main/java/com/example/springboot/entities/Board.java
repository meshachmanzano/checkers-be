package com.example.springboot.entities;


import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Value
@Jacksonized
@Document(collection = "Board")
@Builder(builderClassName = "Builder", toBuilder = true)
public class Board {

    @MongoId(targetType = FieldType.STRING)
    @NonNull UUID uuid;
    // <> contains a type
    @NonNull List<Square> squares;
    @NonNull int numberOfPlayers;
}
