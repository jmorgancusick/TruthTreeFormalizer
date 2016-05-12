/* 
 * Copyright (c) 2016, Aaron Perl
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package perl.aaron.TruthTrees;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import perl.aaron.TruthTrees.logic.AtomicStatement;
import perl.aaron.TruthTrees.logic.Biconditional;
import perl.aaron.TruthTrees.logic.Conditional;
import perl.aaron.TruthTrees.logic.Conjunction;
import perl.aaron.TruthTrees.logic.Disjunction;
import perl.aaron.TruthTrees.logic.Negation;
import perl.aaron.TruthTrees.logic.Statement;


public class ExpressionParser {
	
	private static final ArrayList<Set<Character>> operators;
	private static final String negationPattern;
//	private static final Pattern operatorPattern;
	
	static
	{
		operators = new ArrayList<Set<Character>>();
		
		Set<Character> conditionals = new LinkedHashSet<Character>();
		conditionals.add('\u2192');
		conditionals.add('$');
		operators.add(conditionals);
		
		Set<Character> biconditionals = new LinkedHashSet<Character>();
		biconditionals.add('\u2194');
		biconditionals.add('%');
		operators.add(biconditionals);
		
		Set<Character> conjunctions = new LinkedHashSet<Character>();
		conjunctions.add('\u2227');		// conjunction unicode
		conjunctions.add('&');			// conjunction (junction, what's your function?)
		operators.add(conjunctions);
		
		Set<Character> disjunctions = new LinkedHashSet<Character>();
		disjunctions.add('\u2228');		// disjunction unicode
		disjunctions.add('|');			// disjunction
		operators.add(disjunctions);
		
		negationPattern = "[\u00AC~!]";
		
//		operatorString = "";
//		for (char curOperator : operators)
//			operatorString += curOperator;
//		
//		operatorPattern = Pattern.compile("[\\s]*[" + operatorString + "][\\s]*");
	}
	
	private ExpressionParser() {}
	
	private static int findZeroDepth(String searchString, Set<Character> searchChars)
	{
		int depth = 0;
		for (int i = 0; i < searchString.length(); i++)
		{
			char curChar = searchString.charAt(i);
			if (curChar == '(')
				depth++;
			else if (curChar == ')')
			{
				if (depth == 0)
					return -1;
				depth--;
			}
			else if (searchChars.contains(curChar) && depth == 0)
				return i;
		}
		return -1;
	}
	
	private static int findZeroDepth(String searchString, char searchChar)
	{
		Set<Character> tmpSet = new LinkedHashSet<Character>();
		tmpSet.add(searchChar);
		return findZeroDepth(searchString, tmpSet);
	}
	
	private static Vector<String> splitZeroDepth(String searchString, Set<Character> splitChars)
	{
		Vector<String> retVal = new Vector<String>();
		String curString = searchString;
		int findIndex;
		
		while ((findIndex = findZeroDepth(curString, splitChars)) > -1)
		{
			retVal.add(curString.substring(0, findIndex));
			curString = curString.substring(findIndex + 1);
		}
		
		retVal.add(curString);
		
		return retVal;
	}
	
	private static Statement recurseStatement(String subExpression)
	{	
		int operatorSet = -1;
		
		for (int i = 0; i < operators.size(); i++)
		{
			Set<Character> curOperatorSet = operators.get(i);
			for (char curOperator : curOperatorSet)
			{
				int curOperatorIndex = findZeroDepth(subExpression, curOperator);
				if (curOperatorIndex > -1)
				{
					if (operatorSet > -1)
						return null;
					operatorSet = i;
					break;
				}
			}
		}
		
//		System.out.println("Operator set = " + operatorSet);
		
		if (operatorSet == -1)
		{
			if (subExpression.matches("\\w+") || subExpression.matches("(\\w+)"))
			{
//				System.out.println("Atomic Statement : " + subExpression);
				return new AtomicStatement(subExpression);
			}
			else if (subExpression.startsWith("(") && subExpression.endsWith(")"))
			{
//				System.out.println("Parenthesis around " + subExpression.substring(1,subExpression.length() -1));
				return recurseStatement(
						subExpression.substring(1, subExpression.length() - 1));
			}
			else if (subExpression.matches(negationPattern + ".+"))
			{
//				System.out.println("Negation of " + subExpression.substring(1));
				return new Negation(recurseStatement(subExpression.substring(1)));
			}
			return null;
		}
		
		Vector<String> operandStrings = splitZeroDepth(subExpression, operators.get(operatorSet));
		
		Vector<Statement> operands = new Vector<Statement>();
		
		for (String curOperandString : operandStrings)
		{
			Statement curOperand = recurseStatement(curOperandString);
			if (curOperand == null)
				return null;
			operands.add(curOperand);
		}
		
		switch (operatorSet)
		{
		case 0: // conditional
			if (operands.size() != 2)
				return null; // conditional has exactly 2 operands
			return new Conditional(operands.get(0),operands.get(1));
		case 1: // biconditional
			if (operands.size() != 2)
				return null; // biconditional ---
			return new Biconditional(operands.get(0),operands.get(1));
		case 2: // conjunction
			if (operands.size() < 2)
				return null; // conjunction has at least two operands (how did you even manage this?)
			return new Conjunction(operands);
		case 3: // disjunction
			if (operands.size() < 2)
				return null; // disjunction ---
			return new Disjunction(operands);
		}
		
		return null;
	}
	
	public static Statement parseExpression(String expression)
	{
//		if (checkForMismatchedParenthesis(expression))
//			throw new ...
		return recurseStatement(expression.replaceAll("\\s", ""));
	}

}
