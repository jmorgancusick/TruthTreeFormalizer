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
import java.util.List;

public class Biconditional extends BinaryOperator {

	public Biconditional(Statement a, Statement b) {
		super(a, b);
	}

	public boolean verifyDecomposition(List<List<Statement>> branches) {
		if (branches.size() != 2)
			return false;
		Conjunction AandB = new Conjunction(statements.get(0),statements.get(1));
		Conjunction NotAandNotB = new Conjunction(new Negation(statements.get(0)), new Negation(statements.get(1)));
		List<List<Statement>> branch1 = new ArrayList<List<Statement>>();
		branch1.add(branches.get(0));
		List<List<Statement>> branch2 = new ArrayList<List<Statement>>();
		branch2.add(branches.get(1));
		return 	(AandB.verifyDecomposition(branch1) && NotAandNotB.verifyDecomposition(branch2)) ||
				(AandB.verifyDecomposition(branch2) && NotAandNotB.verifyDecomposition(branch1));
	}

	public String toString() {
		return statements.get(0).toStringParen() + " \u2194 " + statements.get(1).toStringParen();
	}

	public boolean equals(Statement other) {
		if (!(other instanceof Biconditional))
			return false;
		Biconditional otherBiconditional = (Biconditional) other;
		for (int i = 0; i < 2; i++)
		{
			if (!statements.get(i).equals(otherBiconditional.getOperands().get(i)))
				return false;
		}
		return true;
	}

}
