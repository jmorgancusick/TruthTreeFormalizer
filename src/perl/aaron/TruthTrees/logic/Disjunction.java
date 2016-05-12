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

public class Disjunction extends LogicalOperator {
	/**
	 * Creates a Disjunction of the provided statements
	 * @param disjuncts The Statements being disjuncted
	 */
	public Disjunction(Statement... disjuncts) {
		statements = new ArrayList<Statement>();
		Collections.addAll(statements, disjuncts);
	}
	
	public Disjunction(List<Statement> disjuncts) {
		statements = new ArrayList<Statement>();
		statements.addAll(disjuncts);
	}
	
	public String toString()
	{
		ArrayList<Statement> statementsAL = (ArrayList<Statement>) statements;
		String statementString = "";
		for (int i = 0; i < statementsAL.size()-1; i++)
			statementString += statementsAL.get(i).toStringParen() + " \u2228 ";
		return statementString + statementsAL.get(statementsAL.size()-1).toStringParen();
	}

	public boolean verifyDecomposition(List<List<Statement>> branches)
	{
		if (branches.size() != statements.size()) // there must be one branch per disjunct
			return false;
		// boolean arrays in Java default to false
		boolean disjuncts[] = new boolean[statements.size()]; 	// Every disjunct must match up to a branch...
		for (List<Statement> curBranch : branches) 				// ... and every branch must match up to a disjunct
		{
			boolean satisfied = false;
			if (curBranch.size() != 1) 					// Every branch must have one and only one statement in it
				return false;
			for (int i = 0; i < statements.size(); i++) // try to map a disjunct to the current branch
			{
				if (disjuncts[i]) continue; 					// skip already used disjuncts 
				if (statements.get(i).equals(curBranch.get(0))) // the current branch is equal to this disjunct
				{
					satisfied = true;		// branch satisfied
					disjuncts[i] = true; 	// flag this disjunct to avoid duplicate use
					break;
				}
			}
			if (!satisfied) // no disjunct matched this branch
				return false;
		}
		return true;
	}

	public boolean equals(Statement other)
	{
		if (!(other instanceof Disjunction))
			return false;
		List<Statement> otherStatements = ((Disjunction) other).getOperands();
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
