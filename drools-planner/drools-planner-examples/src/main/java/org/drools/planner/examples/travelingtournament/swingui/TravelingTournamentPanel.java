package org.drools.planner.examples.travelingtournament.swingui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.drools.planner.core.move.Move;
import org.drools.planner.examples.common.swingui.SolutionPanel;
import org.drools.planner.examples.travelingtournament.domain.Day;
import org.drools.planner.examples.travelingtournament.domain.Match;
import org.drools.planner.examples.travelingtournament.domain.TravelingTournament;
import org.drools.planner.examples.travelingtournament.solver.simple.move.DayChangeMove;

/**
 * TODO this code is highly unoptimized
 * @author Geoffrey De Smet
 */
public class TravelingTournamentPanel extends SolutionPanel {

    public TravelingTournamentPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private TravelingTournament getTravelingTournament() {
        return (TravelingTournament) solutionBusiness.getSolution();
    }

    public void resetPanel() {
        removeAll();
        TravelingTournament travelingTournament = getTravelingTournament();
        Map<Day, DayPanel> dayPanelMap = new HashMap<Day, DayPanel>();
        for (Day day : travelingTournament.getDayList()) {
            TravelingTournamentPanel.DayPanel dayPanel = new DayPanel();
            add(dayPanel);
            dayPanelMap.put(day, dayPanel);
        }
        for (Match match : travelingTournament.getMatchList()) {
            TravelingTournamentPanel.DayPanel dayPanel = dayPanelMap.get(match.getDay());
            dayPanel.addMatch(match);
        }
    }

    private class DayPanel extends JPanel {

        public DayPanel() {
            super(new GridLayout(1, 0));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }

        public void addMatch(Match match) {
            JButton button = new JButton(new MatchAction(match));
            add(button);
        }

    }

    private class MatchAction extends AbstractAction {

        private Match match;

        public MatchAction(Match match) {
            super(match.toString());
            this.match = match;
        }

        public void actionPerformed(ActionEvent e) {
            List<Day> dayList = getTravelingTournament().getDayList();
            JComboBox dayListField = new JComboBox(dayList.toArray());
            dayListField.setSelectedItem(match.getDay());
            int result = JOptionPane.showConfirmDialog(TravelingTournamentPanel.this.getRootPane(), dayListField,
                    "Select day", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Day toDay = (Day) dayListField.getSelectedItem();
                Move move = new DayChangeMove(match, toDay);
                solutionBusiness.doMove(move);
                workflowFrame.updateScreen();
            }
        }

    }

}
