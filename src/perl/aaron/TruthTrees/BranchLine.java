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

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import perl.aaron.TruthTrees.logic.AtomicStatement;
import perl.aaron.TruthTrees.logic.Decomposable;
import perl.aaron.TruthTrees.logic.Statement;
import perl.aaron.TruthTrees.logic.Negation;

/**
 * A class that represents a single line in a branch, used for storing and verifying decompositions
 * @author Aaron
 *
 */
public class BranchLine {
	protected Branch parent;
	protected Statement statement;
//	protected Set<Set<BranchLine>> decomposition;
	protected Set<Branch> selectedBranches; // holds the parent of the split that decomposes this line
	protected Set<BranchLine> selectedLines;
	protected BranchLine decomposedFrom;
	protected boolean isPremise;
	public static final Color SELECTED_COLOR = new Color(0.3f,0.9f,0.9f);
	public static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	public static final Color EDIT_COLOR = Color.GREEN;

	public BranchLine(Branch branch)
	{
		parent = branch;
		statement = null;
//		decomposition = new LinkedHashSet<Set<BranchLine>>();
		selectedBranches = new LinkedHashSet<Branch>();
		selectedLines = new LinkedHashSet<BranchLine>();
		isPremise = false;
	}
	
	public String toString()
	{
		if (statement != null)
			return statement.toString();
		return "";
	}
	
	public void setIsPremise(boolean isPremise)
	{
		this.isPremise = isPremise;
	}
	
	public boolean isPremise()
	{
		return isPremise;
	}
	
	public void setStatement(Statement statement)
	{
		this.statement = statement;
	}
	
	public Statement getStatement()
	{
		return statement;
	}
	
	public int getWidth(FontMetrics f)
	{
		return f.stringWidth(toString());
	}
	
	public Set<BranchLine> getSelectedLines()
	{
		return selectedLines;
	}
	
	public Set<Branch> getSelectedBranches()
	{
		return selectedBranches;
	}
	
	public void setDecomposedFrom(BranchLine decomposedFrom)
	{
		this.decomposedFrom = decomposedFrom;
	}
	
	public BranchLine getDecomposedFrom()
	{
		return decomposedFrom;
	}
	
	public Branch getParent()
	{
		return parent;
	}
	
	public String verifyDecomposition()
	{
		// Check if the statement is decomposable and it is not the negation of an atomic statement
		if (statement == null)
			return null;
		if (decomposedFrom == null && !isPremise)
			return "Unexpected statement \"" + statement.toString() + "\" in tree";
		if (statement instanceof Decomposable &&
				!(statement instanceof Negation && (((Negation)statement).getNegand() instanceof AtomicStatement)))
		{
			if (selectedBranches.size() > 0) // branching decomposition (disjunction)
			{
				Set<BranchLine> usedLines = new LinkedHashSet<BranchLine>();
				for (Branch curRootBranch : selectedBranches)
				{
					List<List<Statement>> curTotalSet = new ArrayList<List<Statement>>();
					for (Branch curBranch : curRootBranch.getBranches())
					{
						List<Statement> curBranchSet = new ArrayList<Statement>();
						for (BranchLine curLine : selectedLines)
						{
							if (curLine.getParent() == curBranch)
							{
								curBranchSet.add(curLine.getStatement());
								usedLines.add(curLine);
							}
						}
						curTotalSet.add(curBranchSet);
					}
					if (!((Decomposable)statement).verifyDecomposition(curTotalSet))
						return "Invalid decomposition of statement \"" + statement.toString() + "\"";
				}
				if (!usedLines.equals(selectedLines)) // extra lines that were unused
					return "Too many statements decomposed from \"" + statement.toString() + "\"";
				if (!BranchLine.satisfiesAllBranches(parent, selectedBranches))
					return "Statement \"" + statement.toString() + "\" not decomposed in every child branch";
			}
			else // non-branching decomposition (conjunction)
			{
				Map<Branch, List<Statement>> branchMap = new LinkedHashMap<Branch, List<Statement>>();
				for (BranchLine curLine : selectedLines)
				{
					List<Statement> curList; 
					if (branchMap.containsKey(curLine.getParent()))
						curList = branchMap.get(curLine.getParent());
					else
					{
						curList = new ArrayList<Statement>();
						branchMap.put(curLine.getParent(), curList);
					}
					curList.add(curLine.getStatement());
				}
				for (Branch curBranch : branchMap.keySet())
				{
					List<List<Statement>> currentDecomp = new ArrayList<List<Statement>>();
					currentDecomp.add(branchMap.get(curBranch));
					if (!((Decomposable) statement).verifyDecomposition(currentDecomp))
					{
						return "Invalid decomposition of statement \"" + statement.toString() + "\"";
					}
				}
				if (branchMap.size() == 0)
				{
					return "Statement \"" + statement.toString() + "\" has not been decomposed!";
				}
				if(!BranchLine.satisfiesAllBranches(parent, branchMap.keySet()))
				{
					return "Statement \"" + statement.toString() + "\" not decomposed in every child branch";
				}
			}
		}
		return null;
	}
	
	public static boolean satisfiesAllBranches(Branch root, Set<Branch> descendents)
	{
		if (descendents.contains(root) || root.isTerminated())
			return true;
		else
		{
			if (root.getBranches().size() > 0)
			{
				for (Branch curBranch : root.getBranches())
				{
					if (!satisfiesAllBranches(curBranch, descendents))
						return false;
				}
				return true;
			}
			else
				return false;
		}
	}
	
}
