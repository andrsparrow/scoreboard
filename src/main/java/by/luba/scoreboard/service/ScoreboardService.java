package by.luba.scoreboard.service;

import java.util.List;

import by.luba.scoreboard.domain.Match;

public interface ScoreboardService {
    boolean startMatch(String homeTeam, String awayTeam);

    boolean updateScore(String homeTeam,
                     String awayTeam,
                     int homeScore,
                     int awayScore);

    void finishMatch(String homeTeam, String awayTeam);

    void printSummary();

    List<Match> extractOrderedMatches();
}
