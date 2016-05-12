/*
 * Copyright (c) 2016, John Cusick. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package cusick.john.TTtoF;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import cusick.john.TTtoF.Fitch.FitchFileManager;
import cusick.john.TTtoF.Fitch.FitchProof;
import cusick.john.TTtoF.Fitch.ProofLine;
import perl.aaron.TruthTrees.Branch;
import perl.aaron.TruthTrees.BranchLine;
import perl.aaron.TruthTrees.BranchTerminator;
import perl.aaron.TruthTrees.TruthTreeFileManager;
import perl.aaron.TruthTrees.logic.AtomicStatement;
import perl.aaron.TruthTrees.logic.Biconditional;
import perl.aaron.TruthTrees.logic.Conditional;
import perl.aaron.TruthTrees.logic.Conjunction;
import perl.aaron.TruthTrees.logic.Disjunction;
import perl.aaron.TruthTrees.logic.Negation;
import perl.aaron.TruthTrees.logic.Statement;

/**
 * This class takes care of converting a truth tree in memory to a formal F proof in memory.
 */
public class Converter {
	//Notes on how rootBranch works: rootBranch is essentially the start of the proof AFTER the premises

	/**
	 * Set of branches that require lemmas, but have already been instantiated, used so that lemmas
	 * aren't copied into a F proof more than once
	 */
	private Set<BranchLine> instantiatedLemmas = new HashSet<BranchLine>();
	
	/**
	 * Maps a Truth Tree Branch to it's parent BranchLine.
	 */
	private HashMap<Branch, BranchLine> branchDecompositionMap = new HashMap<Branch, BranchLine>();
	
	/**
	 * Maps a Truth Tree's BranchLine to it's equivalent proof line. This is updated when lemmas are used,
	 * and is utilized to construct support mappings in the F proof.
	 */
	private HashMap<BranchLine, ProofLine> referenceMap = new HashMap<BranchLine, ProofLine>();

	/**
	 * Special map for biconditionals, as they require x2 subproofs, and x4 and eliminations.
	 * P <-> Q will point to {P & Q, ~P & ~Q}
	 */
	private HashMap<BranchLine, Vector<ProofLine>> bicondMap = new HashMap<BranchLine, Vector<ProofLine>>();
	
	/**
	 * Called at the start to quickly map all truth tree branches to their parent branchlines.
	 * @param branch
	 */
	public void setBranchDecompositions(Branch branch){
		for(int i = 0; i < branch.numLines(); i++){
			//BranchLine decomposes into a branch
			if(branch.getLine(i).getSelectedBranches().isEmpty() == false){
				for(Branch derivedBranch : branch.getLine(i).getSelectedBranches()){
					//map any branches caused by this branchLine to this branchLine
					branchDecompositionMap.put(derivedBranch, branch.getLine(i));
				}
			}
		}
		if(branchDecompositionMap.containsKey(branch)){
			branch.setDecomposedFrom(branchDecompositionMap.get(branch));
		}
		
		//recurse
		for(Branch branchChild : branch.getBranches()){
			setBranchDecompositions(branchChild);
		}
	}
	
	/** 
	 * Recursive helper print for debugging, prints trees.
	 * @param branch root branch to start print
	 */
	public void recursivePrint(Branch branch){
		System.out.println("recursivePrintStart (branch counter)");
		//print BranchLine things
		for(int i = 0; i < branch.numLines(); i++){
			if(branch.getLine(i).getStatement()!=null){
				System.out.println(branch.getLine(i).getStatement().toString());	
			}
		}
		
		//print Branch things
		if(branch.getDecomposedFrom() != null){ //non root
			System.out.println("branch decomposed from: "+branch.getDecomposedFrom().getStatement().toString());
		}
		//recurse
		for(Branch branchChild : branch.getBranches()){
			recursivePrint(branchChild);
		}
	}
	
	/**
	 * Driver call to convert. Opens up the files and converts them in memory, and outputs the Fitch file
	 * @param ttFile Input Truth Tree file
	 * @param fitchFile Output Fitch file
	 * @return formal F proof
	 */
	public FitchProof truthTreeToFitch(File ttFile, File fitchFile){
		FitchProof formalProof = new FitchProof();
        
		//(1) get the truth tree into memory
		//Notes on how rootBranch works: rootBranch is essentially the start of the proof AFTER the premises
        Branch rootBranch = TruthTreeFileManager.loadFromFile(ttFile);
        
        
        
        
        setBranchDecompositions(rootBranch.getRoot());
        System.out.println("======= recursive print ======");
        recursivePrint(rootBranch.getRoot());
        System.out.println("branchDecompositionMap: "+branchDecompositionMap.toString());
        
        
        transformTreeToProof(rootBranch, formalProof);
        
        //output Fitch proof
        FitchFileManager fFileManager = new FitchFileManager();
        fFileManager.outputFitchFile(fitchFile, formalProof);
		
		return formalProof;
	}
	
	/** 
	 * Transforms a Truth tree in memory to a formal F proof in memory
	 * @param rootBranch input truth tree root branch
	 * @param formalProof modifiable F proof (output)
	 */
	private void transformTreeToProof(Branch rootBranch, FitchProof formalProof){
		Branch premises = rootBranch.getRoot(); 		
		
		/* Note: premises always starts with an empty statement (guessing this is a bug)
		 * Also Note: add all but the last premise, because this is the negated conclusion 
		 */
		
		//TODO monitor the Truth Tree software, if this bug is ever patched, this lines need to be deleted
		//handle the truth tree bug where first premise is always empty
		premises.removeLine(0);

		int numPremises = premises.numLines();
		
		//First: add the premises and the negated conclusion
        for(int i = 0; i < numPremises-1; i++){
			ProofLine premiseLine = formalProof.addPremise(premises.getLine(i));  //Note: handle decomposition linking in FitchProof
			referenceMap.put(premises.getLine(i), premiseLine);
        }
		
        //negated conclusion
        ProofLine negConcl = formalProof.initializeProofByContradiction(premises.getLine(numPremises-1));
        referenceMap.put(premises.getLine(numPremises-1), negConcl);
        formalProof.printProof();
        
		
        //Next: recursively construct the rest of the proof
        //start recursion level and 1 because really it's the subproof level
        //recursive level 0: premises
        //recursive level 1: negated contradiction (same level as first set of statements)
        System.out.println("============ recursive call ==========");
        recursiveTreeTraversal(rootBranch, formalProof);
        
        //finally, set the last statement to an end of subproof
        formalProof.getProof().get(formalProof.getLength()-1).setEndofSubproof(true);;
        //do a neg intro -- reference everything after premises 
        ProofLine negIntro = new ProofLine(new Negation(negConcl.getStatement()), formalProof, 2, FitchProof.RULE_NEG_INTRO);
        for(int i = numPremises - 1; i < formalProof.getLength(); i++){
        	negIntro.addReferencedLine(formalProof.getProof().get(i));
        }
        formalProof.addLine(negIntro);
        
        //do a neg elim if necessary to match goal
        if(negConcl.getStatement() instanceof Negation){ //need to do neg elim
        	ProofLine negElim = new ProofLine(
        			((Negation) negConcl.getStatement()).getNegand(), formalProof, 
        			0, FitchProof.RULE_NEG_ELIM);
        	negElim.addReferencedLine(negIntro);
        	formalProof.addLine(negElim);
        }
        
        formalProof.printProof();
        
	}
	
	/**
	 * When a branch is encountered, insert the necessary lemma that lead to it.
	 * This makes use of the branchDecompositionMap
	 * @param branch 
	 * @param formalProof
	 * @return
	 */
	private int insertBranchLemma(Branch branch, FitchProof formalProof){
		ProofLine pl = null;
		ProofLine lemmaLine = null;
		Statement equivalentStatement = null;
		BranchLine parentBranchLine = branch.getDecomposedFrom();
		if(instantiatedLemmas.contains(parentBranchLine)){
			return 0; // this lemma has already been instantiated
		}
		
		Statement parentStatement = parentBranchLine.getStatement();
		String lemmaRule = "";
		
		if(parentStatement instanceof Disjunction){
			//This is the only branch where no lemma is needed
			return 0;
		}
		else if(parentStatement instanceof Conditional){
			equivalentStatement = getEquivalentConditional((Conditional)parentStatement);
			lemmaRule = FitchProof.LEMMA_COND;
		}
		else if(parentStatement instanceof Biconditional){
			equivalentStatement = getEquivalentBiconditional((Biconditional)parentStatement);
			lemmaRule = FitchProof.LEMMA_BICOND;
		}
		else if(branch.getDecomposedFrom().getStatement() instanceof Negation){
			Statement statementWithoutNegation = ((Negation) parentStatement).getNegand();
			if(statementWithoutNegation instanceof Conjunction){
				//DeMorgans 1 lemma needed
				equivalentStatement = getEquivalentDemorgan1((Conjunction) statementWithoutNegation);
				lemmaRule = FitchProof.LEMMA_DEMORGAN1;
			} else if(statementWithoutNegation instanceof Biconditional){
				//NegBicond lemma needed
				equivalentStatement = getEquivalentNegatedBiconditional((Biconditional) statementWithoutNegation);
				lemmaRule = FitchProof.LEMMA_NEG_BICOND;
			}
		}
		
		Statement lemmaStatement = new Biconditional(parentStatement, equivalentStatement);
		lemmaLine = new ProofLine(lemmaStatement, formalProof, 0, lemmaRule);
		//no references to set
		formalProof.addLine(lemmaLine);

		pl = new ProofLine(equivalentStatement, formalProof, 0, FitchProof.RULE_BICOND_ELIM);
		//add reference lines
		pl.addReferencedLine(lemmaLine); //the lemma
		pl.addReferencedLine(referenceMap.get(branch.getDecomposedFrom())); //the orginal proofLine, retrieved form referenceMap
		formalProof.addLine(pl);
		
		//update referenceMap
		referenceMap.put(branch.getDecomposedFrom(), pl);
		
		//add to lemma instantiated
		instantiatedLemmas.add(branch.getDecomposedFrom());
		
		return 0;
	}
	

	/** 
	 * Called on root branch after premises and negated conclusion have been established. 
	 * This function handles the majority of the proof conversion.
	 * @param branch
	 * @param formalProof
	 * @return status
	 */
	private int recursiveTreeTraversal(Branch branch, FitchProof formalProof){
		//handle branch
		System.out.println("*start recursiveTreeTraversal");

		
		for(int i = 0; i < branch.numLines(); i++){
			System.out.println(branch.getLine(i).toString());
			
			if(branch.getLine(i) instanceof BranchTerminator){ //terminator branchLine
				//handle terminator
				BranchTerminator contradictionLine = (BranchTerminator) branch.getLine(i);
				ProofLine pl = new ProofLine(new AtomicStatement("⊥"), formalProof, 0, FitchProof.RULE_CONTR_INTRO);
				pl.setEndofSubproof(true);
				for(BranchLine referenceLine : contradictionLine.getSelectedLines()){
					pl.addReferencedLine(referenceMap.get(referenceLine));
				}
				formalProof.addLine(pl);
				referenceMap.put(branch.getLine(i), pl);
			}
			else{
				//handle typical line
				branchLineToProofLine(branch.getLine(i), formalProof);
			}
        }
		
		if(branch.getDecomposedFrom() == null){
			//okay if you cant find branch line so long as referenceMap changed
		}
		else{
			//needs to occur before recursive call, but after this branch's lines have been handled
			System.out.println("call insert branch lemma");
			insertBranchLemma(branch, formalProof);
		}
		
		//recursive call on each branch (this is also the base case, if no branches, no loop execution)
		for(Branch branchChild : branch.getBranches()){
			recursiveTreeTraversal(branchChild, formalProof);
		}
		
		//after all branches, use or elim
		if(branch.getBranches().size() > 1){
			//after recursive calls, contradiction. return line numbers to reference from above call?
			ProofLine pl = new ProofLine(new AtomicStatement("⊥"), formalProof, 2, FitchProof.RULE_DISJ_ELIM);
			for(Branch branchChild : branch.getBranches()){
				for(int i = 0; i < branchChild.numLines(); i++){
					//TODO reference the OR statement
					pl.addReferencedLine(referenceMap.get(branchChild.getLine(i)));
				}
			}
			
			//add the reference to the disjunctive statement too (take the first branchChild, add reference to what that was decomposed from, using refMap)
			if(branch.getDecomposedFrom() != null){
//				System.out.println("decomposed from: "+branch.getDecomposedFrom().getStatement().toString());
//				System.out.println("decomposed from fitch: "+referenceMap.get(branch.getDecomposedFrom()).getStatement().toString());
				pl.addReferencedLine(referenceMap.get(branch.getDecomposedFrom()));
			}
			
			
			//TODO, should you always do this? I think so...
			pl.setEndofSubproof(true);
			formalProof.addLine(pl);
		}
		return 0;
	}

	
	//TODO make bicond, negbicond, demorgans 2, and negcond lemmas all taken care of
	//	   in an "insertNonBranchingLemmaLine()" function instead of in this function 

	/**
	 * Converts one truth tree BranchLine to it's equivalent F proof lines
	 * @param bLine
	 * @param formalProof
	 * @return status
	 */
	public int branchLineToProofLine(BranchLine bLine, FitchProof formalProof) {
		ProofLine pl = null;
		
		boolean bicondCase = false;
		int bicondReferenceIndex = 0; //= 0 for LHS decomp (positive P, Q for bicond, positive P neg Q for neg bicond)
									  //= 1 for RHS decomp (negative P, Q for bicond, neg P pos Q for neg bicond)
		
		//here is where we will check if lemmas need to be added
		//this is the statement from which the current line was inferred
		BranchLine parentBranchLine = bLine.getDecomposedFrom();
		
		System.out.println("BRANCHTOLINE-bLine:"+bLine.getStatement().toString());
		System.out.println("BRANCHTOLINE-parent: "+parentBranchLine.getStatement().toString());
		
		//MOST OF THESE LEMMAS HAVE ALREADY BEEN APPLIED (all those that causing a branch)
		//ANY LEMMAS THAT HAVE NOT BEEN APPLIED ARE CONJUNCTION LEMMAS AND HANDLED INSIDE THESE IFS
		if(parentBranchLine.getStatement() instanceof Conjunction){
			//no branch
			pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_CONJ_ELIM);
		}
		else if(parentBranchLine.getStatement() instanceof Disjunction){
			//branch
			pl = new ProofLine(bLine.getStatement(), formalProof, 1, "");
			pl.setStartofSubproof(true);
		}
		else if(parentBranchLine.getStatement() instanceof Conditional){
			//Cond, branch
			pl = new ProofLine(bLine.getStatement(), formalProof, 1, "");
			pl.setStartofSubproof(true);
		}
		else if(parentBranchLine.getStatement() instanceof Biconditional){
			//Bicond, branch

			
			bicondCase = true;
			
			Statement bicondLHS = ((Biconditional) parentBranchLine.getStatement()).getOperands().get(0); 
			Statement bicondRHS = ((Biconditional) parentBranchLine.getStatement()).getOperands().get(1);
			
			//identify if this branch is LHS(e.g., P & Q) or RHS (e.g. ~P & ~Q)
			//LHS
			if(bicondLHS.equals(bLine.getStatement()) || bicondRHS.equals(bLine.getStatement())){
				if(bicondMap.containsKey(parentBranchLine) && bicondMap.get(parentBranchLine).get(0) != null){ // conjunction has already been made
					//references handled below
				} else{ // make the conjunction
					ProofLine bicondDecomp = new ProofLine(new Conjunction(bicondLHS, bicondRHS), formalProof, 1, "");
					bicondDecomp.setStartofSubproof(true);
					
					Vector<ProofLine> value = new Vector<ProofLine>();
					value.add(bicondDecomp);
					value.add(null);
					bicondMap.put(parentBranchLine, value);

					//no references for start of subproof
					formalProof.addLine(bicondDecomp);
				}
				bicondReferenceIndex = 0;
			}
			//RHS
			else if(bLine.getStatement().equals(new Negation(bicondLHS)) || bLine.getStatement().equals(new Negation(bicondRHS))){
				if(bicondMap.containsKey(parentBranchLine) && bicondMap.get(parentBranchLine).get(1) != null){ // conjunction has already been made
					//references handled below
				} else{ // make the conjunction
					ProofLine bicondDecomp = new ProofLine(new Conjunction(new Negation(bicondLHS), new Negation(bicondRHS)), formalProof, 1, "");
					bicondDecomp.setStartofSubproof(true);
					
					Vector<ProofLine> value = new Vector<ProofLine>();
					value.add(null);
					value.add(bicondDecomp);
					bicondMap.put(parentBranchLine, value);

					//no references for start of subproof
					formalProof.addLine(bicondDecomp);
				}
				bicondReferenceIndex = 1;
			}
			
			//must handle the reference differently
			pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_CONJ_ELIM);
			pl.addReferencedLine(bicondMap.get(parentBranchLine).get(bicondReferenceIndex));
			
		}
		else if(parentBranchLine.getStatement() instanceof Negation){
			Statement statementWithoutNegation = ((Negation) parentBranchLine.getStatement()).getNegand();
			if(statementWithoutNegation instanceof Negation){
				//no branch
				pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_NEG_ELIM);
			} else if(statementWithoutNegation instanceof Conjunction){
				//DeMorgans 1, branch
				pl = new ProofLine(bLine.getStatement(), formalProof, 1, "");
				pl.setStartofSubproof(true);
				
			} else if(statementWithoutNegation instanceof Disjunction){
				//DeMorgans 2, no branch
				
				if(instantiatedLemmas.contains(parentBranchLine)){
					// this lemma has already been instantiated
				}
				else{
					//insert DeMorgans2 lemma
					Statement lemmaRHS =  getEquivalentDemorgan2((Disjunction) statementWithoutNegation);
					ProofLine lemma = new ProofLine(
							new Biconditional(parentBranchLine.getStatement(), lemmaRHS), 
							formalProof, 0, FitchProof.LEMMA_DEMORGAN2);
					
					formalProof.addLine(lemma);
					
					//insert the equivalent conjunction using bicond elim
					ProofLine eqLemma = new ProofLine(lemmaRHS, formalProof, 0, FitchProof.RULE_BICOND_ELIM);
					eqLemma.addReferencedLine(lemma);
					eqLemma.addReferencedLine(referenceMap.get(parentBranchLine));
					formalProof.addLine(eqLemma);
					
					//update the reference map
					referenceMap.put(parentBranchLine, eqLemma);
					
					//add to instantiated lemmas
					instantiatedLemmas.add(parentBranchLine);
				}
				
				pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_CONJ_ELIM);
				
			} else if(statementWithoutNegation instanceof Conditional){
				//NegCond, no branch
				if(instantiatedLemmas.contains(parentBranchLine)){
					// this lemma has already been instantiated
				}
				else{
					//insert NegCond lemma
					Statement lemmaRHS = getEquivalentNegatedConditional((Conditional) statementWithoutNegation);
					ProofLine lemma = new ProofLine(
							new Biconditional(parentBranchLine.getStatement(), lemmaRHS), 
							formalProof, 0, FitchProof.LEMMA_NEG_COND);
					
					formalProof.addLine(lemma);
					//add to instantiated lemmas
					instantiatedLemmas.add(parentBranchLine);
					
					//insert the equivalent conjunction using bicond elim
					ProofLine eqLemma = new ProofLine(lemmaRHS, formalProof, 0, FitchProof.RULE_BICOND_ELIM);
					eqLemma.addReferencedLine(lemma);
					eqLemma.addReferencedLine(referenceMap.get(parentBranchLine));
					formalProof.addLine(eqLemma);
					
					//update the reference map
					referenceMap.put(parentBranchLine, eqLemma);
				}
				pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_CONJ_ELIM);
				
			} else if(statementWithoutNegation instanceof Biconditional){
				//NegBicond, branch
					
				bicondCase = true;
				
				Statement bicondLHS = ((Biconditional) ((Negation) parentBranchLine.getStatement()).getNegand()).getOperands().get(0); 
				Statement bicondRHS = ((Biconditional) ((Negation) parentBranchLine.getStatement()).getNegand()).getOperands().get(1);
				
				//identify if this branch is LHS(e.g., ~P & Q) or RHS (e.g. P & ~Q)
				//LHS
				if(bLine.getStatement().equals(bicondLHS) || bLine.getStatement().equals(new Negation(bicondRHS))){
					if(bicondMap.containsKey(parentBranchLine) && bicondMap.get(parentBranchLine).get(0) != null){ // conjunction has already been made
						//references handled below
					} else{ // make the conjunction
						ProofLine bicondDecomp = new ProofLine(new Conjunction(bicondLHS, new Negation(bicondRHS)), formalProof, 1, "");
						bicondDecomp.setStartofSubproof(true);
						
						Vector<ProofLine> value = new Vector<ProofLine>();
						value.add(bicondDecomp);
						value.add(null);
						bicondMap.put(parentBranchLine, value);

						//no references for start of subproof
						formalProof.addLine(bicondDecomp);
					}
					bicondReferenceIndex = 0;
				}
				//RHS
				else if(bLine.getStatement().equals(new Negation(bicondLHS)) || bLine.getStatement().equals(bicondRHS)){
					if(bicondMap.containsKey(parentBranchLine) && bicondMap.get(parentBranchLine).get(1) != null){ // conjunction has already been made
						//references handled below
					} else{ // make the conjunction
						ProofLine bicondDecomp = new ProofLine(new Conjunction(new Negation(bicondLHS), bicondRHS), formalProof, 1, "");
						bicondDecomp.setStartofSubproof(true);
						
						Vector<ProofLine> value = new Vector<ProofLine>();
						value.add(null);
						value.add(bicondDecomp);
						bicondMap.put(parentBranchLine, value);

						//no references for start of subproof
						formalProof.addLine(bicondDecomp);
					}
					bicondReferenceIndex = 1;
				}
				
				//must handle the reference differently
				pl = new ProofLine(bLine.getStatement(), formalProof, 0, FitchProof.RULE_CONJ_ELIM);
				pl.addReferencedLine(bicondMap.get(parentBranchLine).get(bicondReferenceIndex));
				
			}
		}
		
		//bicond case handles references differently
		if(pl.isStartofSubproof() == false && bicondCase == false){
			pl.addReferencedLine(referenceMap.get(parentBranchLine));
		}
		
		formalProof.addLine(pl);
		referenceMap.put(bLine, pl);
		
		return 0;
	}

	/**
	 * Retrieve the equivalent Demorgan1 statement ㄱ(P ∧ Q) ↔ (ㄱP ∨ ㄱQ)
	 * @param statement Conjunction (i.e., LHS without the negation: P ∧ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: ㄱP ∨ ㄱQ)
	 */
	private Statement getEquivalentDemorgan1(Conjunction statement) {
		//ㄱ(P ∧ Q) ↔ (ㄱP ∨ ㄱQ)
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Disjunction equivalentStatement = new Disjunction(
				new Negation(operand1),
				new Negation(operand2));
		return equivalentStatement;
	}
	
	/**
	 * Retrieve the equivalent Demorgan2 statement ㄱ(P ∨ Q) ↔ (ㄱP ∧ ㄱQ)
	 * @param statement Disjunction (i.e., LHS without the negation: P ∨ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: ㄱP ∧ ㄱQ)
	 */
	private Statement getEquivalentDemorgan2(Disjunction statement) {
		//ㄱ(P ∨ Q) ↔ (ㄱP ∧ ㄱQ)
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Conjunction equivalentStatement = new Conjunction(
				new Negation(operand1),
				new Negation(operand2));
		return equivalentStatement;
	}

	/**
	 * Retrieve the equivalent Conditional statement (P ➝ Q) ↔ (ㄱP ∨ Q)
	 * @param statement Conditional (i.e., LHS of the biconditional equality: P ➝ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: ㄱP ∨ Q)
	 */
	private Statement getEquivalentConditional(Conditional statement) {
		//(P ➝ Q) ↔ (ㄱP ∨ Q)
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Disjunction equivalentStatement = new Disjunction(
				new Negation(operand1),
				operand2);
		return equivalentStatement;
	}
	
	/**
	 * Retrieve the equivalent Neg Cond statement ㄱ(P ➝ Q) ↔ (P ∧ ㄱQ)
	 * @param statement Conditional (i.e., LHS without the negation: P ➝ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: P ∧ ㄱQ)
	 */
	private Statement getEquivalentNegatedConditional(Conditional statement) {
		//ㄱ(P ➝ Q) ↔ (P ∧ ㄱQ)
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Conjunction equivalentStatement = new Conjunction(
				operand1,
				new Negation(operand2));
		return equivalentStatement;
	}
	
	/**
	 * Retrieve the equivalent Bicond statement (P ↔ Q) ↔ ((P ∧ Q) ∨ (ㄱP ∧ ㄱQ))
	 * @param statement Biconditional (i.e., LHS of the biconditional equality: P ↔ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: (P ∧ Q) ∨ (ㄱP ∧ ㄱQ))
	 */
	private Statement getEquivalentBiconditional(Biconditional statement) {
		//(P ↔ Q) ↔ ((P ∧ Q) ∨ (ㄱP ∧ ㄱQ))
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Disjunction equivalentStatement = new Disjunction(
				new Conjunction(operand1, operand2),
				new Conjunction(new Negation(operand1), new Negation(operand2)));
		return equivalentStatement;
	}
	
	/**
	 * Retrieve the equivalent Neg Bicond statement ㄱ(P ↔ Q) ↔ ((P ∧ ㄱQ) ∨ (ㄱP ∧ Q))
	 * @param statement Biconditional (i.e., LHS without the negation: P ↔ Q)
	 * @return equivalent statement (i.e., RHS of the biconditional equality: (P ∧ ㄱQ) ∨ (ㄱP ∧ Q))
	 */
	private Statement getEquivalentNegatedBiconditional(Biconditional statement) {
		//ㄱ(P ↔ Q) ↔ ((P ∧ ㄱQ) ∨ (ㄱP ∧ Q))
		Statement operand1 = statement.getOperands().get(0);
		Statement operand2 = statement.getOperands().get(1);
		Disjunction equivalentStatement = new Disjunction(
				new Conjunction(operand1, new Negation(operand2)),
				new Conjunction(new Negation(operand1), operand2));
		return equivalentStatement;
	}
}
