package by.luba.scoreboard.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Match {
    private String homeTeamName;
    private String awayTeamName;
    private int homeTeamScore;
    private int awayTeamScore;
    private LocalDateTime startDateTime;

    public Match(String homeTeamName, String awayTeamName) {
        this.homeTeamName = homeTeamName;
        this.awayTeamName = awayTeamName;
        this.startDateTime = LocalDateTime.now();
    }

    public boolean isMatch(String homeTeamName, String awayTeamName) {
        return homeTeamName.equals(this.homeTeamName)
            && awayTeamName.equals(this.awayTeamName);
    }

    public void updateScore(int homeTeamScore, int awayTeamScore) {
        this.homeTeamScore = homeTeamScore;
        this.awayTeamScore = awayTeamScore;
    }

    public int calculateTotalScore() {
        return awayTeamScore + homeTeamScore;
    }
}
