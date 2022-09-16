package com.example.springboot.repository;

import com.example.springboot.entities.Board;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface BoardRepository extends MongoRepository<Board, UUID> {
}
