package by.luba.scoreboard.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import by.luba.scoreboard.domain.Match;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScoreboardServiceImpl implements ScoreboardService {
    private final List<Match> matchesInProgress = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean startMatch(String homeTeam, String awayTeam) {
        if (isNotValidTeamNames(homeTeam, awayTeam)) {
            log.warn("Cannot update match with empty home/away team names");
            return false;
        }

        Optional<Match> alreadyStartedMatch = findMatch(homeTeam, awayTeam);

        if (alreadyStartedMatch.isPresent()) {
            log.warn("Match already started: {} vs {}", homeTeam, awayTeam);
            return false;
        }

        // can be improved with tryLock
        lock.writeLock().lock();
        try {
            Match newMatch = new Match(homeTeam, awayTeam);
            matchesInProgress.add(newMatch);

            log.info("Match started: {} vs {}", homeTeam, awayTeam);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    @Override
    public boolean updateScore(String homeTeam,
                               String awayTeam,
                               int homeScore,
                               int awayScore) {
        if (isNotValidTeamNames(homeTeam, awayTeam)) {
            log.warn("Cannot update match with empty home/away team names");
            return false;
        }

        if (homeScore < 0 || awayScore < 0) {
            log.warn("Cannot update match with negative score");
            return false;
        }

        // can be improved with tryLock
        lock.writeLock().lock();
        try {
            Optional<Match> match = findMatch(homeTeam, awayTeam);
            if (match.isPresent()) {
                match.get().updateScore(homeScore, awayScore);
                return true;
            }
            {
                log.warn("Match not found: {} vs {}", homeTeam, awayTeam);
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void finishMatch(String homeTeam, String awayTeam) {
        matchesInProgress.removeIf(match -> match.isMatch(homeTeam, awayTeam));
    }

    @Override
    public void printSummary() {
        lock.readLock().lock();
        try {
            extractOrderedMatches()
                .forEach(this::printMatchInfo);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Match> extractOrderedMatches() {
        return matchesInProgress.stream()
            .sorted(
                Comparator.comparingInt(Match::calculateTotalScore).reversed()
                    .thenComparing(Match::getStartDateTime, Comparator.reverseOrder())
            )
            .collect(Collectors.toList());
    }

    private void printMatchInfo(Match match) {
        log.info("{} {} - {} {}",
            match.getHomeTeamName(),
            match.getHomeTeamScore(),
            match.getAwayTeamScore(),
            match.getAwayTeamName()
        );
    }

    private Optional<Match> findMatch(String homeTeam, String awayTeam) {
        return matchesInProgress
            .stream()
            .filter(match -> match.isMatch(homeTeam, awayTeam))
            .findFirst();
    }

    private boolean isNotValidTeamNames(String homeTeam, String awayTeam) {
        return homeTeam == null
            || homeTeam.isEmpty()
            || awayTeam == null
            || awayTeam.isEmpty();
    }
}
