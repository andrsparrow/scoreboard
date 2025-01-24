package by.luba.scoreboard.service;

import java.util.List;

import by.luba.scoreboard.domain.Match;

public interface ScoreboardService {
    /**
     * Start a new match.
     *
     * @param homeTeam  Home team name.
     * @param awayTeam  Away team name.
     * @return  {@code true} if the match was successfully started, {@code false} otherwise.
     *          The match is not started if the home/away team names are empty
     *          or if the match is already in progress.
     */
    boolean startMatch(String homeTeam, String awayTeam);

    /**
     * Update the score of the existing match.
     *
     * @param homeTeam  Home team name.
     * @param awayTeam  Away team name.
     * @param homeScore Home team score.
     * @param awayScore Away team score.
     * @return  {@code true} if the match score was successfully updated, {@code false} otherwise.
     */
    boolean updateScore(String homeTeam,
                     String awayTeam,
                     int homeScore,
                     int awayScore);

    void finishMatch(String homeTeam, String awayTeam);

    void printSummary();

    List<Match> extractOrderedMatches();
}
