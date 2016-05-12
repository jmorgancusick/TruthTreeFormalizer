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

import perl.aaron.TruthTrees.logic.Statement;

/**
 * One line of a formal F proof in memory
 */
public class ProofLine {

	//member variables
	private Statement statement;
	private String rule;
	private int subproofLevel; //starts at 0
	private Vector<ProofLine> referencedLines;
	private int lineNumber;
	private boolean premise = false;
	
	/** SI stands for step info, see Fitch_File_Documentation.txt */
	private int fitchSI = 13; //Fitch memory index for supporting rules, starts at 13
	/** SS stands for step index, see Fitch_File_Documentation.txt */
	private Vector<Integer> fitchSS; //Fitch branch index for supporting rules
	
	private boolean isStartofSubproof = false;
	private boolean isEndofSubproof = false;
	
	private FitchProof encompassingProof;
	
	/**
	 * Constructor for a proof line
	 * @param statement
	 * @param encompassingProof
	 * @param subproofInstruction 0: maintain subproof level, 1: open new subproof, 2: close current subproof (after inserting current line)
	 * @param rule use the RULE and LEMMA constants in FitchProof.java
	 */
	public ProofLine(Statement statement, FitchProof encompassingProof, int subproofInstruction, String rule){
		setEncompassingProof(encompassingProof);
		
		setStatement(statement);
		
		if(subproofInstruction == 0){
			setSubproofLevel(encompassingProof.getCurrentSubproofLevel());
		}
		else if(subproofInstruction == 1){
			setSubproofLevel(encompassingProof.getCurrentSubproofLevel() + 1);
		}
		else if(subproofInstruction == 2){
			setSubproofLevel(encompassingProof.getCurrentSubproofLevel() - 1);
		}
		
		//if the previous line ends a subproof
		if(encompassingProof.getProof().size() > 0){
			if(encompassingProof.getProof().get(encompassingProof.getLength()-1).isEndofSubproof){
				setSubproofLevel(getSubproofLevel() - 1);
			}
		}
		
		referencedLines = new Vector<ProofLine>();
		//fitchSS is initialized in setFitchSS()
		
		setLineNumber(encompassingProof.getLength()+1);
		
		setRule(rule);
	}
	
	public boolean isLemma(){
		if(rule.equals(FitchProof.LEMMA_DEMORGAN1) || rule.equals(FitchProof.LEMMA_DEMORGAN2) ||
				rule.equals(FitchProof.LEMMA_COND) || rule.equals(FitchProof.LEMMA_NEG_COND) ||
				rule.equals(FitchProof.LEMMA_BICOND) || rule.equals(FitchProof.LEMMA_NEG_BICOND)){
			return true;
		}
		return false;
	}

	public Statement getStatement() {
		return statement;
	}
	
	/**
	 * @return the line's statement in a form ready to print to a Fitch file
	 */
	public String getStatementFitchString() {
		String rtn = statement.toString();
		rtn = rtn.replace('¬', '~');
		rtn = rtn.replace('∧', '&');
		rtn = rtn.replace('∨', '|');
		rtn = rtn.replace('→', '$');
		rtn = rtn.replace('↔', '%');
		rtn = rtn.replace('⊥', '^');
		return rtn;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}
	
	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public int getSubproofLevel() {
		return subproofLevel;
	}

	public void setSubproofLevel(int subproofLevel) {
		this.subproofLevel = subproofLevel;
	}

	public Vector<ProofLine> getReferencedLines() {
		return referencedLines;
	}

	public void setReferencedLines(Vector<ProofLine> referencedLines) {
		this.referencedLines = referencedLines;
	}
	
	public void addReferencedLine(ProofLine lineRef) {
		referencedLines.add(lineRef);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public boolean isPremise() {
		return premise;
	}

	public void setPremise(boolean premise) {
		this.premise = premise;
	}

	public FitchProof getEncompassingProof() {
		return encompassingProof;
	}

	public void setEncompassingProof(FitchProof encompassingProof) {
		this.encompassingProof = encompassingProof;
	}

	public boolean isStartofSubproof() {
		return isStartofSubproof;
	}

	public void setStartofSubproof(boolean isStartofSubproof) {
		this.isStartofSubproof = isStartofSubproof;
	}

	public boolean isEndofSubproof() {
		return isEndofSubproof;
	}

	public void setEndofSubproof(boolean isEndofSubproof) {
		this.isEndofSubproof = isEndofSubproof;
	}

	public Vector<Integer> getFitchSS() {
		return fitchSS;
	}
	
	/** 
	 * @return An SS reference to this line's encompassing subproof
	 */
	public Vector<Integer> getFitchSSParentSubproof(){
		Vector<Integer> fitchSSParent = new Vector<Integer>(fitchSS);
		fitchSSParent.remove(fitchSSParent.size()-1);
		return fitchSSParent;
	}

	/**
	 * This function is called when a line uses and entire subproof as support.
	 * Note that a line that does reference an entire subproof as support will always
	 * be exactly one subproof level above it (e.g. neg intro, disj elim).
	 * @param referencerSubProofSize The subproof size of the line that uses this line as support
	 * @return An SS reference to this line's encompassing subproof (String form)
	 */
	public String getFitchSSParentSubproofString(int referencerSubProofSize){
		String str = "";
		for(int i = 0; i < referencerSubProofSize; i++){
			str = str.concat(String.valueOf(fitchSS.get(i)));
			str = str.concat(".");
		}
		return str;
	}
	
	/**
	 * @return The SS in string form.
	 */
	public String getFitchSS_String(){
		String str = "";
		for(int i = 0; i < fitchSS.size(); i++){
			str = str.concat(String.valueOf(fitchSS.get(i)));
			if(i != fitchSS.size()-1){
				str = str.concat(".");
			}
		}
		return str;
	}
	
	public void setFitchSS(Vector<Integer> fitchSS) {
		//make new vector because the passed will be updated outside
		this.fitchSS = new Vector<Integer>(fitchSS);
	}

	public int getFitchSI() {
		return fitchSI;
	}

	public void setFitchSI(int fitchSI) {
		this.fitchSI = fitchSI;
	}

}
