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

import perl.aaron.TruthTrees.logic.AtomicStatement;
import perl.aaron.TruthTrees.logic.Negation;
import perl.aaron.TruthTrees.logic.Statement;

public class BranchTerminator extends BranchLine {

	public BranchTerminator(Branch branch) {
		super(branch);
	}
	
	public String toString()
	{
		return "\u2715";
	}
	
	public void setIsPremise(boolean isPremise) {}
	public boolean isPremise() { return false; }
	public void setStatement(Statement s) {}
	public void setDecomposedFrom(BranchLine decomposedFrom) {}
	public BranchLine getDecomposedFrom() { return null; }
	
	public String verifyDecomposition()
	{
		if (selectedLines.size() != 2)
		{
			return "Invalid number of supporting statements for branch termination";
		}
		ArrayList<BranchLine> selectedList = new ArrayList<BranchLine>();
		selectedList.addAll(selectedLines);
		AtomicStatement atomic;
		Statement negatedStatement;
		if (selectedList.get(0).getStatement() instanceof AtomicStatement)
		{
			atomic = (AtomicStatement)selectedList.get(0).getStatement();
			negatedStatement = selectedList.get(1).getStatement();
		}
		else if (selectedList.get(1).getStatement() instanceof AtomicStatement)
		{
			atomic = (AtomicStatement)selectedList.get(1).getStatement();
			negatedStatement = selectedList.get(0).getStatement();
		}
		else
			return "No atomic statement found in branch termination justification";
		
		if (!(negatedStatement instanceof Negation))
			return "No negation found in branch termination justification";
		
		Negation negated = (Negation) negatedStatement;
		if (!negated.getNegand().equals(atomic))
			return "Incorrect atomic statement/negation pair in branch termination justification";
		return null;
	}

}
