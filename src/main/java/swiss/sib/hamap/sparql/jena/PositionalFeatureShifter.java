package swiss.sib.hamap.sparql.jena;

import java.util.List;

import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.util.FmtUtils;

/**
 * This functions a position via template source and template target alignment
 * as it would have been on the "source" entry to what it should be on a
 * "target" sequence.
 * 
 * The target sequence being the one we are looking at.
 * 
 * @see https://jena.apache.org/documentation/query/writing_functions.html *
 */
public class PositionalFeatureShifter extends FunctionBase {

	private static final int NUMBER_EXPECTED_ARGUMENTS = 5;

	public PositionalFeatureShifter() {
		super();
	}

	@Override
	public NodeValue exec(List<NodeValue> args) {
		if (args.size() != 5)
			throw new ExprEvalException("Incorrect number of arguments");
		return exec(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4));
	}

	public NodeValue exec(NodeValue alignStringSignatureTemplate, NodeValue signatureTemplateStart,
			NodeValue positionToMap, NodeValue alignStringSignatureTarget, NodeValue signatureTargetStart) {
		if (!alignStringSignatureTemplate.isString())
			throw new ExprEvalException(
					"Not a String: " + FmtUtils.stringForNode(alignStringSignatureTemplate.asNode()));
		else if (!alignStringSignatureTarget.isString())
			throw new ExprEvalException("Not a String: " + FmtUtils.stringForNode(alignStringSignatureTarget.asNode()));
		else if (!positionToMap.isNumber())
			throw new ExprEvalException("Not a number: " + FmtUtils.stringForNode(positionToMap.asNode()));
		else if (!signatureTemplateStart.isNumber())
			throw new ExprEvalException("Not a number: " + FmtUtils.stringForNode(signatureTemplateStart.asNode()));
		final String templateAlign = alignStringSignatureTemplate.asUnquotedString();
		final String targetAlign = alignStringSignatureTarget.asUnquotedString();

		// Step 1: Count the non-insertion chars in the 'template to signature' mapping.
		int templatePositionInt = positionToMap.getInteger().intValue()
				- (signatureTemplateStart.getInteger().intValue()) + 1;
		final int step1 = fromTemplateToSignature(templatePositionInt, templateAlign);

		// Step 2: Count the non-deletion chars in the 'signature to target' mapping,
		// using the signature position calculated in step1.
		final int step2 = fromSignatureToTarget(step1, targetAlign);
		final int mappedPosition = step2 + signatureTargetStart.getInteger().intValue() - 1;

		return NodeValue.makeInteger(mappedPosition);
	}

	static final int fromTemplateToSignature(final int pos, final String align) {
		int nonDeletionCounter = 0;
		int nonInsertionCounter = 0;
		for (int i = 0; i < align.length(); i++) {
			if (align.charAt(i) == '-' || Character.isUpperCase(align.charAt(i))) {
				nonInsertionCounter++;
			}
			if (align.charAt(i) != '-')
				nonDeletionCounter++;
			if (nonDeletionCounter == pos)
				return nonInsertionCounter;
		}
		throw new ExprEvalException("");
	}

	static final int fromSignatureToTarget(final int pos, final String align) {
		int nonDeletionCounter = 0;
		int nonInsertionCounter = 0;
		for (int i = 0; i < align.length(); i++) {
			if (align.charAt(i) == '-' || Character.isUpperCase(align.charAt(i))) {
				nonInsertionCounter++;
			}
			if (align.charAt(i) != '-')
				nonDeletionCounter++;
			if (nonInsertionCounter == pos)
				return nonDeletionCounter;
		}
		throw new ExprEvalException("");
	}

	@Override
	public void checkBuild(String uri, ExprList args) {
		if (args.size() != NUMBER_EXPECTED_ARGUMENTS)
			throw new QueryBuildException(
					"Function '" + this.getClass() + "' takes " + NUMBER_EXPECTED_ARGUMENTS + " arguments");
	}
}
