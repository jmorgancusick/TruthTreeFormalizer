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

package perl.aaron.TruthTrees.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conjunction extends LogicalOperator {
	/**
	 * Creates a Conjunction of the provided statements
	 * @param disjuncts The Statements being conjuncted
	 */
	public Conjunction(Statement... conjuncts) {
		statements = new ArrayList<Statement>();
		Collections.addAll(statements, conjuncts);
	}
	
	public Conjunction(List<Statement> conjuncts) {
		statements = new ArrayList<Statement>();
		statements.addAll(conjuncts);
	}
	
	public String toString()
	{
		ArrayList<Statement> statementsAL = (ArrayList<Statement>) statements;
		String statementString = "";
		for (int i = 0; i < statementsAL.size()-1; i++)
			statementString += statementsAL.get(i).toStringParen() + " \u2227 ";
		return statementString + statementsAL.get(statementsAL.size()-1).toStringParen();
	}

	public boolean verifyDecomposition(List< List<Statement> > branches) {
		if (branches.size() != 1) // There should be only 1 branch
			return false;
		List<Statement> decomposedList = branches.get(0);
		if (decomposedList.size() != statements.size()) // One decomposed statement per conjunct
			return false;
		boolean[] conjuncts = new boolean[statements.size()]; 	// Every conjunct must match up to a statement...
		for (Statement curStatement : decomposedList)			// ... and every statement must match up to a conjunct
		{
			boolean satisfied = false;
			for (int i = 0; i < statements.size(); i++) // try to map a conjunct to the current statement
			{
				if (conjuncts[i]) continue; 				// skip already used conjuncts 
				if (statements.get(i).equals(curStatement)) // the current statement is equal to this conjunct
				{
					satisfied = true;		// branch satisfied
					conjuncts[i] = true; 	// flag this conjunct to avoid duplicate use
					break;
				}
			}
			if (!satisfied) // no conjunct matched this statement
				return false;
		}
		return true;
	}

	public boolean equals(Statement other) {
		if (!(other instanceof Conjunction))
			return false;
		List<Statement> otherStatements = ((Conjunction) other).getOperands();
		if (statements.size() != otherStatements.size())
			return false;
		for (int i = 0; i < statements.size(); i++)
		{
			//TODO accept statements in different order?
			if ( !(statements.get(i).equals(otherStatements.get(i))) )
				return false;
		}
		return true;
	}
}
