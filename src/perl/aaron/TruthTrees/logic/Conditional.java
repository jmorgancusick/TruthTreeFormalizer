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

import java.util.List;

public class Conditional extends BinaryOperator {

	public Conditional(Statement a, Statement b)
	{
		super(a,b);
	}
	public String toString() {
		return  statements.get(0).toStringParen()+ " \u2192 " +statements.get(1).toStringParen();
	}
	public boolean verifyDecomposition(List<List<Statement>> branches) {
		if (branches.size() != 2) // conditionals decompose into 2 branches (implication to a disjunction)
			return false;
		if (branches.get(0).size() != 1 || branches.get(1).size() != 1) // each branch should have 1 statement
			return false;
		Statement antecedentNeg = new Negation(statements.get(0)); // a -> b <=> ~a v b
		Statement consequent = statements.get(1);
		Statement a = branches.get(0).get(0);
		Statement b = branches.get(1).get(0);
		return ((antecedentNeg.equals(a) && consequent.equals(b)) ||
				(antecedentNeg.equals(b) && consequent.equals(a)));
	}
	public boolean equals(Statement other) {
		if (!(other instanceof Conditional))
			return false;
		List<Statement> otherStatements = ((Conditional) other).getOperands();
		return (statements.get(0).equals(otherStatements.get(0))) && (statements.get(1).equals(otherStatements.get(1)));
	}

}
