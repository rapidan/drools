package org.drools.verifier.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.drools.StatelessSession;
import org.drools.StatelessSessionResult;
import org.drools.base.RuleNameMatchesAgendaFilter;
import org.drools.verifier.TestBase;
import org.drools.verifier.components.AnalyticsRule;
import org.drools.verifier.dao.AnalyticsResult;
import org.drools.verifier.dao.AnalyticsResultFactory;
import org.drools.verifier.redundancy.RedundancyTestBase;
import org.drools.verifier.report.components.Redundancy;

public class EquivalentRulesTest extends RedundancyTestBase {

	public void testRuleRedundancy() throws Exception {
		StatelessSession session = getStatelessSession(this.getClass()
				.getResourceAsStream("Rules.drl"));

		session.setAgendaFilter(new RuleNameMatchesAgendaFilter(
				"Find equivalent Rules"));

		Collection<Object> data = new ArrayList<Object>();

		String ruleName1 = "Rule 1";
		String ruleName2 = "Rule 2";

		AnalyticsRule rule1 = new AnalyticsRule();
		rule1.setRuleName(ruleName1);
		AnalyticsRule rule2 = new AnalyticsRule();
		rule2.setRuleName(ruleName2);

		Redundancy r1 = new Redundancy(rule1, rule2);

		data.add(rule1);
		data.add(rule2);
		data.add(r1);

		AnalyticsResult result = AnalyticsResultFactory.createAnalyticsResult();
		session.setGlobal("result", result);

		StatelessSessionResult sessionResult = session.executeWithResults(data);

		Map<String, Set<String>> map = createRedundancyMap(sessionResult
				.iterateObjects());

		assertTrue(TestBase.mapContains(map, ruleName1, ruleName2));
		assertTrue(TestBase.mapContains(map, ruleName2, ruleName1));

		if (!map.isEmpty()) {
			fail("More equivalences than was expected.");
		}
	}
}