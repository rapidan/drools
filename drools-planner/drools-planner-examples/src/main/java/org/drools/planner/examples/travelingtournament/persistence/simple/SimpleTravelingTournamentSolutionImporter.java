package org.drools.planner.examples.travelingtournament.persistence.simple;

import java.util.List;

import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;
import org.drools.planner.examples.travelingtournament.persistence.TravelingTournamentSolutionImporter;

/**
 * @author Geoffrey De Smet
 */
public class SimpleTravelingTournamentSolutionImporter extends TravelingTournamentSolutionImporter {

    public static void main(String[] args) {
        new SimpleTravelingTournamentSolutionImporter().convertAll();
    }

    public SimpleTravelingTournamentSolutionImporter() {
        super(new SimpleTravelingTournamentDaoImpl());
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new SimpleTravelingTournament();
    }

    public class SimpleTravelingTournament extends TravelingTournamentInputBuilder {

        protected void initializeMatchDays(TravelingTournament travelingTournament) {
            List<Match> matchList = travelingTournament.getMatchList();
            List<Day> dayList = travelingTournament.getDayList();
            for (Match match : matchList) {
                match.setDay(dayList.get(0));
            }
        }

    }

}
