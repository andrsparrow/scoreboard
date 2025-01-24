package by.luba.scoreboard.service


import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScoreboardServiceImplTest extends Specification {
    @Subject
    def scoreboardService = new ScoreboardServiceImpl()

    def "start match with valid teams"() {
        given:
        def homeTeam = "Liverpool"
        def awayTeam = "Manchester United"

        when:
        def result = scoreboardService.startMatch(homeTeam, awayTeam)

        then:
        result
        noExceptionThrown()

        then:
        def matches = scoreboardService.extractOrderedMatches()
        matches.size() == 1
        matches[0].getHomeTeamName() == homeTeam
        matches[0].getAwayTeamName() == awayTeam
    }

    @Unroll
    def "start match with invalid teams: #homeTeam vs #awayTeam -> ignore start match"() {
        when:
        def result = scoreboardService.startMatch(homeTeam, awayTeam)

        then:
        !result
        scoreboardService.extractOrderedMatches().size() == 0

        where:
        homeTeam    | awayTeam
        null        | "Manchester United"
        "Liverpool" | null
        ""          | "Manchester United"
        "Liverpool" | ""
    }

    def "start match with duplicate teams -> ignore start match"() {
        given:
        def homeTeam = "Liverpool"
        def awayTeam = "Manchester United"
        scoreboardService.startMatch(homeTeam, awayTeam)

        when:
        def result = scoreboardService.startMatch(homeTeam, awayTeam)

        then:
        !result
        scoreboardService.extractOrderedMatches().size() == 1
    }

    def "update score with valid match"() {
        given:
        def homeTeam1 = "Liverpool"
        def awayTeam1 = "Manchester United"
        scoreboardService.startMatch(homeTeam1, awayTeam1)

        def homeTeam2 = "Barcelona"
        def awayTeam2 = "Real Madrid"
        scoreboardService.startMatch(homeTeam2, awayTeam2)

        when:
        def result = scoreboardService.updateScore(homeTeam1, awayTeam1, 10, 5)

        then:
        result
        def match = scoreboardService.extractOrderedMatches()[0]
        match.homeTeamScore == 10
        match.awayTeamScore == 5
    }

    def "update score with invalid teams -> ignore update match score"() {
        when:
        def result = scoreboardService.updateScore(null, "Manchester United", 0, 0)

        then:
        !result
        scoreboardService.extractOrderedMatches().size() == 0
    }

    def "update score with invalid match-> no match update"() {
        given:
        def homeTeam = "Liverpool"
        def awayTeam = "Manchester United"
        scoreboardService.startMatch(homeTeam, awayTeam)

        def invalidHomeTeam = "Invalid Home Team"

        when:
        def result = scoreboardService.updateScore(invalidHomeTeam, awayTeam, 10, 5)

        then:
        !result
        def matches = scoreboardService.extractOrderedMatches()
        matches.size() == 1
        matches[0].homeTeamScore == 0
        matches[0].awayTeamScore == 0
    }

    def "update score with invalid score -> no match update"() {
        given:
        def homeTeam = "Liverpool"
        def awayTeam = "Manchester United"
        scoreboardService.startMatch(homeTeam, awayTeam)

        when:
        def result = scoreboardService.updateScore(homeTeam, awayTeam, -1, 0)

        then:
        !result
        def matches = scoreboardService.extractOrderedMatches()
        matches.size() == 1
        matches[0].homeTeamScore == 0
        matches[0].awayTeamScore == 0
    }

    def "finish match"() {
        given:
        def homeTeam = "Liverpool"
        def awayTeam = "Manchester United"
        scoreboardService.startMatch(homeTeam, awayTeam)

        when:
        scoreboardService.finishMatch(homeTeam, awayTeam)

        then:
        scoreboardService.extractOrderedMatches().size() == 0
    }

    def "extractOrderedMatches returns matches in correct order"() {
        given:
        scoreboardService.startMatch("Mexico", "Canada")
        scoreboardService.updateScore("Mexico", "Canada", 0, 5)

        scoreboardService.startMatch("Spain", "Brazil")
        scoreboardService.updateScore("Spain", "Brazil", 10, 2)

        scoreboardService.startMatch("Germany", "France")
        scoreboardService.updateScore("Germany", "France", 2, 2)

        scoreboardService.startMatch("Uruguay", "Italy")
        scoreboardService.updateScore("Uruguay", "Italy", 6, 6)

        scoreboardService.startMatch("Argentina", "Australia")
        scoreboardService.updateScore("Argentina", "Australia", 3, 1)

        when:
        def orderedMatches = scoreboardService.extractOrderedMatches()
        scoreboardService.printSummary()

        then:
        orderedMatches.size() == 5

        orderedMatches[0].homeTeamName == "Uruguay"
        orderedMatches[0].awayTeamName == "Italy"

        orderedMatches[1].homeTeamName == "Spain"
        orderedMatches[1].awayTeamName == "Brazil"

        orderedMatches[2].homeTeamName == "Mexico"
        orderedMatches[2].awayTeamName == "Canada"

        orderedMatches[3].homeTeamName == "Argentina"
        orderedMatches[3].awayTeamName == "Australia"

        orderedMatches[4].homeTeamName == "Germany"
        orderedMatches[4].awayTeamName == "France"
    }

    def "test multithreaded access"() {
        given:
        def executor = Executors.newFixedThreadPool(10)

        when:
        (1..20).each {
            executor.submit(() -> {
                def random = new Random()
                String homeTeam = "Team " + random.nextInt(100)
                String awayTeam = "Team " + (random.nextInt(100) + 1)
                scoreboardService.startMatch(homeTeam, awayTeam)

                def homeScore = random.nextInt(6)
                def awayScore = random.nextInt(6)

                scoreboardService.updateScore(homeTeam, awayTeam, homeScore, awayScore)
            })
        }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        then:
        noExceptionThrown()
        scoreboardService.printSummary()
    }
}
