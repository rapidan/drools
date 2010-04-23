package org.drools.bpmn2.xpath;

import java.util.List;
import java.util.Map;

import org.drools.base.TypeResolver;
import org.drools.compiler.Dialect;
import org.drools.io.Resource;
import org.drools.lang.descr.BaseDescr;
import org.drools.lang.descr.FunctionDescr;
import org.drools.lang.descr.ProcessDescr;
import org.drools.lang.descr.RuleDescr;
import org.drools.rule.builder.AccumulateBuilder;
import org.drools.rule.builder.ActionBuilder;
import org.drools.rule.builder.ConsequenceBuilder;
import org.drools.rule.builder.EnabledBuilder;
import org.drools.rule.builder.EngineElementBuilder;
import org.drools.rule.builder.EntryPointBuilder;
import org.drools.rule.builder.FromBuilder;
import org.drools.rule.builder.PackageBuildContext;
import org.drools.rule.builder.PatternBuilder;
import org.drools.rule.builder.PredicateBuilder;
import org.drools.rule.builder.ProcessBuildContext;
import org.drools.rule.builder.ProcessClassBuilder;
import org.drools.rule.builder.QueryBuilder;
import org.drools.rule.builder.ReturnValueBuilder;
import org.drools.rule.builder.ReturnValueEvaluatorBuilder;
import org.drools.rule.builder.RuleBuildContext;
import org.drools.rule.builder.RuleClassBuilder;
import org.drools.rule.builder.RuleConditionBuilder;
import org.drools.rule.builder.SalienceBuilder;

public class XPathDialect implements Dialect {

    public static final String ID = "XPath";
    
	private static final XPathReturnValueEvaluatorBuilder RETURN_VALUE_EVALUATOR_BUILDER = new XPathReturnValueEvaluatorBuilder();
	
	public String getId() {
		return ID;
	}

	public String getExpressionDialectName() {
		return "XPath";
	}

	public ReturnValueEvaluatorBuilder getReturnValueEvaluatorBuilder() {
		return RETURN_VALUE_EVALUATOR_BUILDER;
	}

	public ActionBuilder getActionBuilder() {
		return null;
	}

	public void addImport(String importEntry) {
	}

	public void addProcess(ProcessBuildContext context) {
	}

	public void addRule(RuleBuildContext context) {
	}

	public void addStaticImport(String importEntry) {
	}

	public void compileAll() {
	}

	public AccumulateBuilder getAccumulateBuilder() {
		return null;
	}

	public RuleConditionBuilder getBuilder(Class<?> clazz) {
		return null;
	}

	public Map<Class<?>,EngineElementBuilder> getBuilders() {
		return null;
	}

	public ConsequenceBuilder getConsequenceBuilder() {
		return null;
	}

	public EntryPointBuilder getEntryPointBuilder() {
		return null;
	}

	public RuleConditionBuilder getEvalBuilder() {
		return null;
	}

	public FromBuilder getFromBuilder() {
		return null;
	}

	public PatternBuilder getPatternBuilder() {
		return null;
	}

	public PredicateBuilder getPredicateBuilder() {
		return null;
	}

	public ProcessClassBuilder getProcessClassBuilder() {
		return null;
	}

	public QueryBuilder getQueryBuilder() {
		return null;
	}

	public List<?> getResults() {
		return null;
	}

	public ReturnValueBuilder getReturnValueBuilder() {
		return null;
	}

	public RuleClassBuilder getRuleClassBuilder() {
		return null;
	}

	public SalienceBuilder getSalienceBuilder() {
		return null;
	}

	public TypeResolver getTypeResolver() {
		return null;
	}

	public void init(RuleDescr ruleDescr) {
	}

	public void init(ProcessDescr processDescr) {
	}

	public void postCompileAddFunction(FunctionDescr functionDescr,
			TypeResolver typeResolver) {
	}

	public void preCompileAddFunction(FunctionDescr functionDescr,
			TypeResolver typeResolver) {
	}

	public EnabledBuilder getEnabledBuilder() {
		return null;
	}

	public void addFunction(FunctionDescr functionDescr, TypeResolver typeResolver, Resource resource) {
	}

	public AnalysisResult analyzeBlock(PackageBuildContext context,
			BaseDescr descr, String text,
			Map<String, Class<?>>[] availableIdentifiers) {
		return null;
	}

	public AnalysisResult analyzeExpression(PackageBuildContext context,
			BaseDescr descr, Object content,
			Map<String, Class<?>>[] availableIdentifiers) {
		return null;
	}

}
