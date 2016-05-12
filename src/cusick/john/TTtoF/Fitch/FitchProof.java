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

package cusick.john.TTtoF.Fitch;
import java.util.Vector;

import perl.aaron.TruthTrees.BranchLine;
import perl.aaron.TruthTrees.logic.Negation;
import perl.aaron.TruthTrees.logic.Statement;

public class FitchProof {
	
	//member variables
	private Vector<ProofLine> proof;
	Statement goal; //vector?
	private Vector<ProofLine> premises;

	//6 rules and 6 lemmas needed (these strings use unicode characters)
	public static final String RULE_NEG_INTRO = "¬ Intro";
	public static final String RULE_NEG_ELIM = "¬ Elim";
	public static final String RULE_CONJ_ELIM = "∧ Elim";
	public static final String RULE_DISJ_ELIM = "∨ Elim";
	public static final String RULE_BICOND_ELIM = "↔ Elim";
	public static final String RULE_CONTR_INTRO = "⊥ Intro";

	public static final String LEMMA_DEMORGAN1 = "DeMorgan1";
	public static final String LEMMA_DEMORGAN2 = "DeMorgan2";
	public static final String LEMMA_COND = "Cond";
	public static final String LEMMA_NEG_COND = "NegCond";
	public static final String LEMMA_BICOND = "BiCond";
	public static final String LEMMA_NEG_BICOND = "NegBiCond";
	
	
	//constructor
	public FitchProof(){
		proof = new Vector<ProofLine>();
		premises = new Vector<ProofLine>();
	}
	
	public Vector<ProofLine> getProof() {
		return proof;
	}

	public void setProof(Vector<ProofLine> proof) {
		this.proof = proof;
	}

	public ProofLine addPremise(BranchLine bLine) {
		ProofLine pl = new ProofLine(bLine.getStatement(), this, 0,"");
		pl.setPremise(true);
		proof.add(pl);
		premises.add(pl);
		return pl;
	}
	
	public ProofLine initializeProofByContradiction(BranchLine bLine) {
		ProofLine pl = new ProofLine(bLine.getStatement(), this, 1, "");
		pl.setStartofSubproof(true);
		//set goal (test for neg)
		if(bLine.getStatement() instanceof Negation){
			goal = ((Negation) bLine.getStatement()).getNegand();
		}
		else{
			goal = new Negation(bLine.getStatement());
		}
		proof.add(pl);
		return pl;
	}

	
	public void addLine(ProofLine pl){
		proof.add(pl);
	}
	
	public ProofLine get(int i){
		return getProof().get(i);
	}


//	public ProofLine addContradiction(BranchLine bLine, int recursionLevel, HashMap<BranchLine, ProofLine> conversionMap) {
//		ProofLine pl = new ProofLine(bLine, recursionLevel, proof.size()+1);
//		
//		//set rule
//		pl.setRule(RULE_CONTR_INTRO);
//		
//		//add references
//		for(BranchLine contradictionLine : bLine.getSelectedLines()){
//			pl.addReferencedLine(conversionMap.get(contradictionLine));
//		}
//		
//		proof.add(pl);
//		return pl;
//	}

	/** 
	 * Get the Fitch proof's current subproof level
	 * @return the last ProofLine's subproof level
	 */
	public int getCurrentSubproofLevel() {
		// TODO Auto-generated method stub
		if(proof.size() > 0){
			return proof.get(proof.size()-1).getSubproofLevel();
		}
		return 0;
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return proof.size();
	}
	
	//Organized fitch print
	public void printProof(){
		System.out.println("=========================");
		System.out.println("=======FITCH PRINT=======");
		System.out.println("=========================");
		String appendStr = "|";
		boolean printBreakLine = false;
		for(int i = 0; i < proof.size(); i++){
			
			if(i>0){
				//end of premises
				if(!proof.get(i).isPremise() && proof.get(i-1).isPremise()){
					printBreakLine = true;
				}
				
				//end of subproof assume
				if(proof.get(i-1).isStartofSubproof()){
					printBreakLine = true;
				}
				
				if(printBreakLine == true){
					System.out.println(appendStr.substring(0,appendStr.length()-1) + "+" + "--");
				}
				
				//start of subproof
				if(proof.get(i).isStartofSubproof()){
					appendStr+=" |";
				}
				//lase was end of subproof
				if(proof.get(i-1).isEndofSubproof()){
					appendStr = appendStr.substring(0, appendStr.length()-2);
				}
				
				//add a little space if ending and starting a subproof
				if(proof.get(i-1).isEndofSubproof() && proof.get(i).isStartofSubproof()){
					System.out.println(appendStr);
				}
			}
			
			System.out.print(appendStr + proof.get(i).getLineNumber() + ". "+proof.get(i).getStatement().toString());
			System.out.print("\t"+proof.get(i).getRule());
			for(int j = 0; j < proof.get(i).getReferencedLines().size(); j++){
				try{
					System.out.print(" "+proof.get(i).getReferencedLines().get(j).getLineNumber());
				} catch(NullPointerException e){
//					System.err.println("NULL IN PRINT");
//					e.printStackTrace();
				}
			}
			System.out.println();
			
			
			
			printBreakLine = false;
		}

		System.out.println("=========================");
		System.out.println("=====END FITCH PRINT=====");
		System.out.println("=========================");
	}

	/**
	 * used to set Fitch SI and Fitch SS before printing procedure
	 */
	public void setFitchIndices() {
		//TODO insert blurb on how SI and SS work

		
		//set initial values 
		Integer currSI = 13;
		Vector<Integer> currSS = new Vector<Integer>();
		currSS.add(0);
		if(proof.size() > 0){ //this should always be true
			proof.get(0).setFitchSI(currSI); /* 13 is the starting Fitch SI number */
			proof.get(0).setFitchSS(currSS);
		}
		
		int currentSubProofLevel = 0;
//		int currSubProofLineNumber = 0;
		
		System.out.println("FITCH INDICES: ");
		System.out.println(proof.get(0).getLineNumber()+". "+proof.get(0).getStatement().toString() +" SI: "+proof.get(0).getFitchSI() + "\tSS: "+proof.get(0).getFitchSS_String());
		
		for(int i = 1; i < proof.size(); i++){
//			currSI += 8; //constant increment of 8
//			currSI += currSubProofLineNumber; //also add the current line number
			
			if(proof.get(i-1).isEndofSubproof()){
				currSS.remove(currentSubProofLevel);
				currentSubProofLevel--;
			}

			currSS.set(currentSubProofLevel, currSS.get(currentSubProofLevel)+1);
			
			if(proof.get(i).isStartofSubproof()){
				currentSubProofLevel++;
				currSS.add(0); 
			}
			
			

			proof.get(i).setFitchSS(currSS);

			
			System.out.println(proof.get(i).getLineNumber()+". "+proof.get(i).getStatement().toString() +" SI: "+proof.get(i).getFitchSI() + "\tSS: "+proof.get(i).getFitchSS_String());
		}
		
		
	}

	
}
