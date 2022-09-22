package com.example.springboot;

import com.example.springboot.entities.Board;
import com.example.springboot.entities.Square;
import com.example.springboot.logic.Errors;
import com.example.springboot.repository.BoardRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// endpoints
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000/")
public class CheckersController {

    //accessor type name;  = new variable
    private final BoardRepository boardRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/board")
    public ResponseEntity<JsonNode> board() throws JsonProcessingException {
        JsonNode boardJson = objectMapper.valueToTree(boardRepository.findAll().get(0));
        AtomicBoolean isBlack = new AtomicBoolean(true);
        List<Square> squares = boardRepository.findAll().get(0).getSquares()
                .stream()
                .filter((Square::isContainsPiece))
                .toList();
        boolean hasWinner = squares.stream()
                .map(square -> {
                    isBlack.set(square.isBlack());
                    return square.isBlack();
                })
                .allMatch(isNotRed -> isNotRed == squares.get(0).isBlack());
        if (hasWinner){
            JsonNode responseJson = objectMapper.readTree(String.format("{\"board\":%s,\"winner\":\"%s\"}",
                    boardJson.at("/squares").toString(),
                    isBlack.get() ? "black" : "red"));
            return new ResponseEntity<>(responseJson, HttpStatus.OK);
        }
                // stream is a flow of date which can be filtered. map changes each element of a stream, one element at a time. reduce breaks it down to one element.
        JsonNode responseJson = objectMapper.readTree(String.format("{\"board\":%s}", boardJson.at("/squares").toString()));
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }
    private void makeKings() {
        Board board = boardRepository.findAll().get(0);
        List<Square> updatedSquares = board.getSquares().stream().map(square -> {
            if (square.getYCoord() == 0 && !square.isBlack()) {
                return square.toBuilder().isKing(true).build();
            }
            if (square.getYCoord() == 7 && square.isBlack()) {
                return square.toBuilder().isKing(true).build();
            }
            return square;
        }).toList();
        boardRepository.save(board.toBuilder().squares(updatedSquares).build());
    }
    private ResponseEntity<JsonNode> normalMove(Square newStartSquare, Square newToSquare) {
        Board board = boardRepository.findAll().get(0);
        List<Square> newSquares = new ArrayList<>(board.getSquares());
        newSquares.set(newStartSquare.getYCoord() * 8 + newStartSquare.getXCoord(), newStartSquare);
        newSquares.set(newToSquare.getYCoord() * 8 + newToSquare.getXCoord(), newToSquare);
        boardRepository.save(board.toBuilder().squares(newSquares).build());
        makeKings();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private  List<TakeMove> addTakeMoves(int takePosition, int movePosition, Square startSquare, Square endSquare, List<TakeMove> takeMoves, List<Square> squares) {
        List<Square> piecesRemoved = new ArrayList<>();
        piecesRemoved.add(squares.get(takePosition));
        TakeMove takeMove = new TakeMove(startSquare, squares.get(movePosition), piecesRemoved);
        takeMoves.add(takeMove);
        List<Square> updatedSquares = new ArrayList<>(squares);
        updatedSquares.set(takePosition, squares.get(takePosition).toBuilder().containsPiece(false).build());
        takeMoves.addAll(findValidTakeMoves(squares.get(movePosition), updatedSquares, startSquare, endSquare, piecesRemoved));
        return takeMoves;
    }

    private  List<TakeMove> addTakeMoves(int takePosition, int movePosition, Square startSquare, Square endSquare, List<TakeMove> takeMoves, List<Square> squares, List<Square> piecesRemoved) {
        List<Square> newPiecesRemoved = new ArrayList<>(piecesRemoved);
        newPiecesRemoved.add(squares.get(takePosition));
        TakeMove takeMove = new TakeMove(startSquare, squares.get(movePosition), newPiecesRemoved);
        takeMoves.add(takeMove);
        squares.set(takePosition, squares.get(takePosition).toBuilder().containsPiece(false).build());
        takeMoves.addAll(findValidTakeMoves(squares.get(movePosition), squares, startSquare, endSquare, newPiecesRemoved));
        return takeMoves;
    }

    private List<TakeMove> findValidTakeMoves(Square startSquare, Square toSquare, List<Square> squares) {
        List<TakeMove> takeMoves = new LinkedList<>();
        int startPosition = startSquare.getYCoord() * 8 + startSquare.getXCoord();
        if (
                startPosition <= 45 &&
                        !squares.get(startPosition + 18).isContainsPiece() &&
                        squares.get(startPosition + 9).isContainsPiece() &&
                        squares.get(startPosition + 9).getXCoord() != 0 && squares.get(startPosition + 9).getXCoord() != 7 &&
                        squares.get(startPosition + 9).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(startPosition + 9, startPosition + 18, startSquare, toSquare, takeMoves, squares));
        }
        if (
                startPosition >= 18 &&
                        !squares.get(startPosition - 18).isContainsPiece() &&
                        squares.get(startPosition - 9).isContainsPiece() &&
                        squares.get(startPosition - 9).getXCoord() != 0 && squares.get(startPosition - 9).getXCoord() != 7 &&
                        squares.get(startPosition - 9).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(startPosition - 9, startPosition - 18, startSquare, toSquare, takeMoves, squares));
        }
        if (
                startPosition <= 49 &&
                        !squares.get(startPosition + 14).isContainsPiece() &&
                        squares.get(startPosition + 7).isContainsPiece() &&
                        squares.get(startPosition + 7).getXCoord() != 0 && squares.get(startPosition + 7).getXCoord() != 7 &&
                        squares.get(startPosition + 7).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(startPosition + 7, startPosition + 14, startSquare, toSquare, takeMoves, squares));
        }
        if (
                startPosition >= 14 &&
                        !squares.get(startPosition - 14).isContainsPiece() &&
                        squares.get(startPosition - 7).isContainsPiece() &&
                        squares.get(startPosition - 7).getXCoord() != 0 && squares.get(startPosition - 7).getXCoord() != 7 &&
                        squares.get(startPosition - 7).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(startPosition - 7, startPosition - 14, startSquare, toSquare, takeMoves, squares));
        }
        return takeMoves;
    }

    private List<TakeMove> findValidTakeMoves(Square currentSquare, List<Square> squares, Square startSquare, Square endSquare, List<Square> piecesRemoved)  {
        List<TakeMove> takeMoves = new LinkedList<>();
        int currentPosition = currentSquare.getYCoord() * 8 + currentSquare.getXCoord();
        // = assign data to a variable
        // == asks if one is equal to the other
        // signature = accessor, type, name and
        if(currentSquare.getXCoord() == endSquare.getXCoord() && currentSquare.getYCoord() == endSquare.getYCoord()) {
            return takeMoves;
        }

        if (
                currentPosition <= 45 &&
                        !squares.get(currentPosition + 18).isContainsPiece() &&
                        squares.get(currentPosition + 9).isContainsPiece() &&
                        squares.get(currentPosition + 9).getXCoord() != 0 && squares.get(currentPosition + 9).getXCoord() != 7 &&
                        squares.get(currentPosition + 9).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(currentPosition + 9, currentPosition + 18, startSquare, endSquare, takeMoves, squares, piecesRemoved));

        }
        if (
                currentPosition >= 18 &&
                        !squares.get(currentPosition - 18).isContainsPiece() &&
                        squares.get(currentPosition - 9).isContainsPiece() &&
                        squares.get(currentPosition - 9).getXCoord() != 0 && squares.get(currentPosition - 9).getXCoord() != 7 &&
                        squares.get(currentPosition - 9).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(currentPosition - 9, currentPosition - 18, startSquare, endSquare, takeMoves, squares, piecesRemoved));

        }
        if (
                currentPosition <= 49 &&
                        !squares.get(currentPosition + 14).isContainsPiece() &&
                        squares.get(currentPosition + 7).isContainsPiece() &&
                        squares.get(currentPosition + 7).getXCoord() != 0 && squares.get(currentPosition + 7).getXCoord() != 7 &&
                        squares.get(currentPosition + 7).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(currentPosition + 7, currentPosition + 14, startSquare, endSquare, takeMoves, squares, piecesRemoved));

        }
        if (
                currentPosition >= 14 &&
                        !squares.get(currentPosition - 14).isContainsPiece() &&
                        squares.get(currentPosition - 7).isContainsPiece() &&
                        squares.get(currentPosition - 7).getXCoord() != 0 && squares.get(currentPosition - 7).getXCoord() != 7 &&
                        squares.get(currentPosition - 7).isBlack() ^ startSquare.isBlack()
        ) {
            takeMoves.addAll(addTakeMoves(currentPosition - 7, currentPosition - 14, startSquare, endSquare, takeMoves, squares, piecesRemoved));

        }
        return takeMoves;
    }

    @PostMapping("/move")
    public ResponseEntity<JsonNode> move(@RequestBody JsonNode body) throws JsonProcessingException {
        //extracting data from body
        int xFrom = body.findValue("posFrom").findValue("x").asInt();
        int yFrom = body.findValue("posFrom").findValue("y").asInt();
        int xTo = body.findValue("posTo").findValue("x").asInt();
        int yTo = body.findValue("posTo").findValue("y").asInt();
        String colour = body.findValue("playerColour").asText();
        // finding the squares
        Square startSquare = boardRepository.findAll().get(0).getSquares().get(yFrom * 8 + xFrom);
        Square toSquare = boardRepository.findAll().get(0).getSquares().get(yTo * 8 + xTo);
        // if expressions
        if (!startSquare.isContainsPiece()) {
            JsonNode error = objectMapper.readTree(String.format("{\"code\":\"%s\",\"message\":\"%s\"}", Errors.E02.name(), Errors.E02.getMessage()));
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if (toSquare.isContainsPiece()) {
            JsonNode error = objectMapper.readTree(String.format("{\"code\":\"%s\",\"message\":\"%s\"}", Errors.E03.name(), Errors.E03.getMessage()));
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if ((startSquare.isBlack() && colour.equals("red")) || (!startSquare.isBlack() && colour.equals("black"))) {
            JsonNode error = objectMapper.readTree(String.format("{\"code\":\"%s\",\"message\":\"%s\"}", Errors.E04.name(), Errors.E04.getMessage()));
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        int xMove = xTo - xFrom;
        int yMove = yTo - yFrom;
        boolean validMove = Math.pow(xMove * yMove, 2) == 1;
        Square newStartSquare = startSquare.toBuilder().containsPiece(false).build();

        if (validMove && startSquare.isKing()) {
            Square newToSquare = toSquare.toBuilder().containsPiece(true).isBlack(startSquare.isBlack()).isKing(true).build();
            newStartSquare = newStartSquare.toBuilder().isKing(false).build();
            return normalMove(newStartSquare, newToSquare);
        }

        if (validMove && startSquare.isBlack() && yMove > 0) {
            Square newToSquare = toSquare.toBuilder().containsPiece(true).isBlack(true).build();
            return normalMove(newStartSquare, newToSquare);
        }

        if (validMove && !startSquare.isBlack() && yMove < 0) {
            Square newToSquare = toSquare.toBuilder().containsPiece(true).isBlack(false).build();
            return normalMove(newStartSquare, newToSquare);
        }


        List<TakeMove> validTakeMoves = findValidTakeMoves(startSquare, toSquare,  boardRepository.findAll().get(0).getSquares());
        List<TakeMove> matchingMoves = validTakeMoves.stream().filter(takeMove -> {
            int startPosition = yFrom * 8 + xFrom;
            int endPosition = yTo * 8 + xTo;
            int moveStartPosition = takeMove.startSquare().getYCoord() * 8 + takeMove.startSquare().getXCoord();
            int moveToPosition = takeMove.toSquare().getYCoord() * 8 + takeMove.toSquare().getXCoord();

            return (startPosition == moveStartPosition && endPosition == moveToPosition);
        }).toList();

        if (matchingMoves.size() == 0) {
            JsonNode error = objectMapper.readTree(String.format("{\"code\":\"%s\",\"message\":\"%s\"}", Errors.E01.name(), Errors.E01.getMessage()));
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        else {
            matchingMoves.stream().reduce((matchingMove, nextMatchingMove) -> matchingMove.piecesRemoved().size()>nextMatchingMove.piecesRemoved().size() ?
                    matchingMove : nextMatchingMove)
                    .ifPresent(matchingMove -> matchingMove.execute(boardRepository));
            makeKings();
            return new ResponseEntity<>(HttpStatus.OK);
        }

    }

    @GetMapping("/start")
    public ResponseEntity<JsonNode> start() throws JsonProcessingException {
        Board board = boardRepository.findAll().get(0);
        if (board.getNumberOfPlayers() == 1) {
            Board newBoard = board.toBuilder().numberOfPlayers(2).build();
            boardRepository.save(newBoard);
        }
       else {
            List<Square> squares = new LinkedList<>();
            for (int y = 0; y < 8; y++) {
                for  (int x = 0; x < 8; x++) {
                    boolean hasPiece = (x - y)%2 != 0 && y != 3 && y != 4;
                    boolean teamBlack = hasPiece && y < 3;
                    Square square = Square.builder()
                            .containsPiece(hasPiece)
                            .isBlack(teamBlack)
                            .isKing(false)
                            .xCoord(x)
                            .yCoord(y)
                            .build();
                    squares.add(square);
                }
            }
            boardRepository.deleteAll();
            boardRepository.save(Board.builder().uuid(UUID.randomUUID()).squares(squares).numberOfPlayers(1).build());
        }

        JsonNode body = objectMapper.readTree(String.format("{\"player\" : %1d }", boardRepository.findAll().get(0).getNumberOfPlayers()-1));
        return new ResponseEntity<>(body, HttpStatus.OK);
    }


    private record TakeMove(Square startSquare, Square toSquare, List<Square> piecesRemoved) {

        public void execute(BoardRepository boardRepository) {
                Square newToSquare = toSquare.toBuilder().containsPiece(true).isBlack(startSquare.isBlack()).isKing(startSquare.isKing()).build();
                Square newStartSquare = startSquare.toBuilder().containsPiece(false).isKing(false).build();
                List<Square> newPiecesList = boardRepository.findAll().get(0).getSquares();
                newPiecesList = newPiecesList.stream().map(square -> {
                    boolean needsPieceRemoved = piecesRemoved.stream()
                            .map(piece ->
                                    piece.getXCoord() == square.getXCoord() && piece.getYCoord() == square.getYCoord()
                            ).reduce(false, (aBoolean, aBoolean2) -> aBoolean || aBoolean2);
                    if (needsPieceRemoved) return square.toBuilder().containsPiece(false).isKing(false).build();
                    return square;
                }).map(square -> {
                    if (square.getYCoord() == toSquare.getYCoord() && square.getXCoord() == toSquare.getXCoord())
                        return newToSquare;
                    if (square.getYCoord() == startSquare.getYCoord() && square.getXCoord() == startSquare.getXCoord())
                        return newStartSquare;
                    return square;
                }).toList();
                boardRepository.save(boardRepository.findAll().get(0).toBuilder().squares(newPiecesList).build());

            }

        }
}
// new var is: type name =

