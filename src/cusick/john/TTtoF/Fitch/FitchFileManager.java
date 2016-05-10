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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import perl.aaron.TruthTrees.graphics.TreePanel;

public class FitchFileManager {
	private static final String EXTENSION = "prf";
	
	public void saveFile(JPanel parent, FitchProof proof)
	{
		final JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter tftFilter = new FileNameExtensionFilter(
		  EXTENSION + " files(*." + EXTENSION + ")",
		  EXTENSION);
		
		fileChooser.addChoosableFileFilter(tftFilter);
		fileChooser.setFileFilter(tftFilter);
		
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			if (fileChooser.getFileFilter() == tftFilter && !file.getName().endsWith("." + EXTENSION))
				file = new File(file.getAbsolutePath() + "." + EXTENSION);
			System.out.println(file.getName());
			outputFitchFile(file, proof);
		}
	}
	
	
	private String assembleFitchOSLine(){
		String osType = "Unknown:";
	    if (OS_NAME.toLowerCase().startsWith("mac os x")) {
	      osType = FITCH_OS_CONSTANT_MAC;
	    } else if (OS_NAME.toLowerCase().startsWith("windows")) {
	      osType = FITCH_OS_CONSTANT_WINDOWS;
	    } else if (OS_NAME.toLowerCase().startsWith("linux")) {
	      osType = FITCH_OS_CONSTANT_LINUX;
	    }
	    return osType+OS_NAME+OS_VERSION;
	}
		
	private long sumCharAsciiValues(String str){
		//calculate the check sum for each line
		long sum = 0;
		for(int i = 0; i < str.length(); i++){
			sum += (long)str.charAt(i);
		}
		return sum;
	}
	
	public int outputFitchFile(File file, FitchProof proof){
		
		//open filename
		PrintWriter writer;
		try {
			writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Could no open output file!");
			return 1;
		}
		
		
		
		//Vector<String> line;
		//line.add(""); //blank, just to align indexes and lines
 		
		
		//Fitch version number
		//TODO: is there a way to find Fitch's current version number?
		String line1 = FITCH_VERSION;
		//Operating System
		String line2 = assembleFitchOSLine();
		//Fitch constant
		String line3 = FITCH_LINE3_CONSTANT;
		//Time stamps (can't be empty on windows, just pull some other file's timestamp)
		String line4 = "C1462893784184D1462893817578";
		//Fitch file format
		String line5 = FITCH_FILE_FORMAT;
		
		writer.println(line1);
		writer.println(line2);
		writer.println(line3);
		writer.println(line4);
		writer.println(line5);
		
		//this is the proof, too long to save in String so output incrementally
		long line6CheckSum = printProof(writer,proof);
		
		//check sum is the sum of ascii values from the first 6 lines
		long checkSum = 
				sumCharAsciiValues(line1) +
				sumCharAsciiValues(line2) +
				sumCharAsciiValues(line3) +
				sumCharAsciiValues(line4) +
				sumCharAsciiValues(line5) +
				line6CheckSum;
		
		//append the check sum to the end of line 6
		String checkSumStr = "c="+checkSum+";";
		
		//this is the s = ...; line, not sure what it does, doesn't seem to matter
		String line7 = "s=123;"; //random number 123
		
		writer.println(checkSumStr);
		writer.println(line7);
		
		
		writer.close();
		return 0;
	}
	
	//TODO use string builder
	private Long printProof(PrintWriter writer, FitchProof proof) {
		
		proof.setFitchIndices();
		
		Long runningCheckSum = (long) 0;
		Stack<String> closingString = new Stack<String>();
		
		runningCheckSum += OutputAndAdd(writer, PROOF_OPENING_LINE, true, closingString);
		runningCheckSum += OutputAndAdd(writer, PROOF_DRIVER, true, closingString);
		
		//RECURSIVE PRINT CALL

		System.out.println("checksum BEFORE RECURSE: "+runningCheckSum);
		runningCheckSum += recursiveProofPrinter(writer, proof, closingString, 0);
		System.out.println("checksum AFTER RECURSE: "+runningCheckSum);
		
		//TODO NEED TO CHANGE TO ACCEPT GOALS
		//TODO take closingString as arg and just update in function
		runningCheckSum += OutputAndAdd(writer, PROOF_GOAL, false, closingString);
		runningCheckSum += OutputAndAdd(writer, PROOF_A, false, closingString);

		System.out.println("checksum AFTER LAST: "+runningCheckSum);
		
		while(closingString.empty() == false){
			System.out.println("pops at end (should be 2)");
			runningCheckSum += CloseBranches(1, writer, closingString);
		}
		
		return runningCheckSum;
	}
	
	private Long recursiveProofPrinter(PrintWriter writer, FitchProof proof, Stack<String> closingString, int lineIndex) {
		Long runningCheckSum = (long) 0;
		if(lineIndex >= proof.getLength()){
			return (long) 0;
		}
		
		if(lineIndex != 0){ //all but first call
			//comma separating each line/subproof
			runningCheckSum += OutputAndAdd(writer, ",", false, closingString); 
		}
		
		if(lineIndex == 0 || proof.get(lineIndex).isStartofSubproof()){ //first call or start of subproof
			if(lineIndex == 0){
				runningCheckSum += OutputAndAdd(writer, PROOF_STARTER, true, closingString);
			} else if(proof.get(lineIndex).isStartofSubproof()){
				runningCheckSum += OutputAndAdd(writer, PROOF_SUB_STARTER, true, closingString);
			}
			
			//S
			runningCheckSum += OutputAndAdd(writer, PROOF_STEP_INFO, true, closingString);
			runningCheckSum += OutputAndAdd(writer, PROOF_STEP_INFO_R_DEFAULT, false, closingString);
			runningCheckSum += CloseBranches(1, writer, closingString); //closes PROOF_STEP_INFO
			
			//R
//			OutputAndAdd(writer, PROOF_RULE, runningCheckSum, true, closingString);
			runningCheckSum += OutputAndAdd(writer, PROOF_RULE_PROOF_START, false, closingString);
//			CloseBranches(1, writer, runningCheckSum, closingString);
			
			//O
			if(lineIndex == 0){
				runningCheckSum += OutputAndAdd(writer, PROOF_OBJECT_PROOF_START, false, closingString);
			} else if(proof.get(lineIndex).isStartofSubproof()){
				runningCheckSum += OutputAndAdd(writer, PROOF_OBJECT_SUBPROOF_START, false, closingString);	
			}
			
			//U -- Support, no supports for opening a new proof
			//get rid of delim, just call OutputAndAdd with true and close branch
			runningCheckSum += OutputAndAdd(writer, PROOF_SUPPORT, true, closingString);
			runningCheckSum += CloseBranches(1, writer, closingString);
			
			//B -- idk what this is, always just b()
			runningCheckSum += OutputAndAdd(writer, PROOF_B, false, closingString);
			
			//F -- recursive call
			runningCheckSum += OutputAndAdd(writer, PROOF_F, true, closingString);
			
			//TODO dont forget about commas!
		}
		
		// proofLine	

//		System.out.println("checksum before: "+runningCheckSum);
		runningCheckSum += OutputAndAdd(writer, PROOF_SIMPLE_STEP, true, closingString);
//		System.out.println("checksum after: "+runningCheckSum);
		//S
		runningCheckSum += OutputAndAdd(writer, PROOF_STEP_INFO, true, closingString);
		runningCheckSum += OutputAndAdd(writer, PROOF_STEP_INFO_R_NORMAL, true, closingString);
		runningCheckSum += OutputAndAdd(writer, proof.get(lineIndex).getStatementFitchString(), false, closingString); // print the statement text
		runningCheckSum += CloseBranches(2, writer, closingString); //closes PROOF_STEP_INFO & PROOF_STEP_INFO_R_NORMAL

		//R
		if(proof.get(lineIndex).isLemma()){
			//must import Lemma
			runningCheckSum += OutputLemma(writer, proof.get(lineIndex).getRule(), closingString);
		} else{
			//normal F rule
			String rule = getFitchRuleString(proof, lineIndex);
			runningCheckSum += OutputAndAdd(writer, rule, false, closingString);
		}

		//O
		runningCheckSum += OutputAndAdd(writer, PROOF_OBJECT_NORMAL_LINE, false, closingString);
		
		//U
		runningCheckSum += OutputAndAdd(writer, PROOF_SUPPORT, true, closingString);
		runningCheckSum += OutputSupportSteps(writer, proof, lineIndex, closingString);
		runningCheckSum += CloseBranches(1, writer, closingString); //close the support branch
		
		//B -- idk what this is, always just b()
		runningCheckSum += OutputAndAdd(writer, PROOF_B, false, closingString);
		
		//NO F for line, just recursive call (comma will be put at start of next line)
		
		//close Simple Step
		runningCheckSum += CloseBranches(1, writer, closingString);
		
		//if this is the last step in a subproof (or end of proof)
		if(proof.get(lineIndex).isEndofSubproof() || lineIndex == proof.getLength()-1){
			//close F branch and subproof branch
			runningCheckSum += CloseBranches(2, writer, closingString); 
		}
		
		runningCheckSum += recursiveProofPrinter(writer, proof, closingString, lineIndex+1); //recursive call
		return runningCheckSum;
		
//		if(lineIndex == 0 || proof.get(lineIndex).isStartofSubproof()){
//			CloseBranches(1, writer, runningCheckSum, closingString); //close subproof/proof start F branch
//		}
		//TODO call this in driver
//		if(lineIndex == 0){ //only close when back at top level recursion
//			System.out.println("closing proof starter");
//			CloseBranches(1, writer,runningCheckSum, closingString); //closes PROOF_STARTER
//		}
		
	}
	


	private Long OutputLemma(PrintWriter writer, String rule, Stack<String> closingString) {
		System.out.println("starting lemma output");
		//rule should correspond to the correct lemma file (without the .prf)
		//open the proof, read in starting at the correct line/location (should all be the same)
		//output to the new proof, close
		Long runningCheckSum = (long) 0; //try to just use a constand runningCheckSum

		InputStream fis;

		//output lemma rule start
		runningCheckSum += OutputAndAdd(writer, PROOF_STEP_INFO_R_LEMMA, true, closingString);
		
		//output lemma name (first part of lemma rule) 
		runningCheckSum += OutputAndAdd(writer, rule, false, closingString);
		runningCheckSum += CloseBranches(1, writer, closingString);
		
		System.out.println("test1");
		
		//output lemma proof (second part of lemma rule)
		
		File file = new File("src/cusick/john/TTtoF/FundamentalTruthTreeLemmas/"+rule+".prf");
		try {
			System.out.println("test2");
			Scanner scan = new Scanner(file);

			System.out.println("test3");
			//skip first 5 lines
			scan.nextLine();
			scan.nextLine();
			scan.nextLine();
			scan.nextLine();
			scan.nextLine();
			
			//skip first open lineProof
			scan.findInLine("=openproof.zen.Openproof\\{");
			
			//copy the proof in, '}c=' should always signify the end and should never occur
			//in the actual proof's data
			String lemmaProof = scan.nextLine();
			lemmaProof = lemmaProof.substring(0, lemmaProof.indexOf("}c="));
			
			System.out.println(lemmaProof);
			
			System.out.println("test4");
			runningCheckSum += OutputAndAdd(writer, lemmaProof, false, closingString);
			
			scan.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("test4");
		//close final branch of lemma rule
		runningCheckSum += CloseBranches(1, writer, closingString);
		
		
//		switch(rule){
//			case FitchProof.LEMMA_DEMORGAN1:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_DEMORGAN1;
//				break;
//			case FitchProof.LEMMA_DEMORGAN2:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_DEMORGAN2;
//				break;
//			case FitchProof.LEMMA_COND:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_COND;
//				break;
//			case FitchProof.LEMMA_NEG_COND:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_NEG_COND;
//				break;
//			case FitchProof.LEMMA_BICOND:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_BICOND;
//				break;
//			case FitchProof.LEMMA_NEG_BICOND:
//				runningCheckSum += PROOF_LEMMA_CHECKSUM_NEG_BICOND;
//				break;
//		}
		
		return runningCheckSum;
	}


	private Long OutputSupportSteps(PrintWriter writer, FitchProof proof, int lineIndex, Stack<String> closingString) {
		Long runningCheckSum = (long) 0;
		Vector<Integer> currLineSS = proof.get(lineIndex).getFitchSS();
		Vector<String> referencedSubProofs = new Vector<String>();
		
		String outputSI = "";
		String outputSS = "";
		
		int numSupportsPrinted = 0;  //used for comma printing
		
		for(int i = 0; i< proof.get(lineIndex).getReferencedLines().size(); i++){
			ProofLine lineRef = proof.get(lineIndex).getReferencedLines().get(i);
			//if the reference line is a line in a deeper subproof (SS size is greater)
			//then this line MUST reference the whole subproof
//			Vector<Integer> referencedSS = new Vector<Integer>();
			System.out.println("Line: "+proof.get(lineIndex).getLineNumber()+". "+proof.get(lineIndex).getStatementFitchString());
			System.out.println("ref line: "+lineRef.getLineNumber()+". "+lineRef.getStatementFitchString());
			System.out.println("currLineSS: "+currLineSS);
			System.out.println("lineRef: "+lineRef);
			System.out.println("lineRef.getFitchSS(): "+lineRef.getFitchSS());
			if (lineRef.getFitchSS().size() > currLineSS.size()){
				//reference the whole proof (if you haven't already)
				outputSI = String.valueOf(lineRef.getFitchSI());
				outputSS = lineRef.getFitchSSParentSubproofString(currLineSS.size());
				if(referencedSubProofs.contains(outputSS)){  //if already contains, skip
					outputSI = "";
					outputSS = "";
					continue;
				} else{ //if not, add it
					//add to referenced sub proofs (so it's not referenced a second time		
					referencedSubProofs.add(outputSS);
				}
			}
			else{ //just a normal line ref
				outputSI = String.valueOf(lineRef.getFitchSI());
				outputSS = lineRef.getFitchSS_String();
			}
			
			//add comma if not the first support header
			if(numSupportsPrinted != 0){
				runningCheckSum += OutputAndAdd(writer, ",", false, closingString);
			}
			
			//put support header
			runningCheckSum += OutputAndAdd(writer, PROOF_SUPPORT_STEP, true, closingString);
			
			//add SI string
			runningCheckSum += OutputAndAdd(writer, outputSI, false, closingString);
			runningCheckSum += CloseBranches(1, writer, closingString); // close part of support header, to get the SS
			
			//add SS string
			runningCheckSum += OutputAndAdd(writer, outputSS, false, closingString);
			
			//close support header
			runningCheckSum += CloseBranches(1, writer, closingString); // close part of support header, to get the SS
			
			
			
			//DEBUG OUTPUT
			System.out.println("Added SI for "+proof.get(lineIndex).getLineNumber()+". "+proof.get(lineIndex).getStatementFitchString()+ " --> "+lineRef.getLineNumber()+". "+lineRef.getStatementFitchString()+": "+outputSI);
			System.out.println("Added SS for "+proof.get(lineIndex).getLineNumber()+". "+proof.get(lineIndex).getStatementFitchString()+ " --> "+lineRef.getLineNumber()+". "+lineRef.getStatementFitchString()+": "+outputSS);
			
			outputSI = "";
			outputSS = "";
			
			numSupportsPrinted++;
		}
		
		return runningCheckSum;
	}


	private String getFitchRuleString(FitchProof proof, int lineIndex) {
		//String rtn = "";
		if(proof.get(lineIndex).isPremise()){
			return PROOF_RULE_PREMISE;
		} else if(proof.get(lineIndex).isStartofSubproof()){
			return PROOF_RULE_PREMISE;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_NEG_INTRO)){
			return PROOF_RULE_NEG_INTRO;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_NEG_ELIM)){
			return PROOF_RULE_NEG_ELIM;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_CONJ_ELIM)){
			return PROOF_RULE_CONJ_ELIM;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_DISJ_ELIM)){
			return PROOF_RULE_DISJ_ELIM;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_BICOND_ELIM)){
			return PROOF_RULE_BICOND_ELIM;
		} else if(proof.get(lineIndex).getRule().equals(FitchProof.RULE_CONTR_INTRO)){
			return PROOF_RULE_CONTR_INTRO;
		} else{
			//don't know how to handle this case
			System.err.println("Unrecognized rule!");
			return "";
		}
	}


	private Long CloseBranches(int numCloses, PrintWriter writer, Stack<String> closingString){
		Long runningCheckSum = (long) 0;
		for(int i = 0; i < numCloses; i++){
			String partialClose = closingString.pop();
//			System.out.println("partialClose: "+partialClose);
			runningCheckSum += sumCharAsciiValues(partialClose);
			writer.print(partialClose);
		}
		return runningCheckSum;
	}

	private Long OutputAndAdd(PrintWriter writer, String str, boolean split, Stack<String> closingString){
		String outputString = str;
		if(split == true){
			String[] a = str.split(Pattern.quote(DELIM));
//			if (a.length == 2){
//				outputString = a[0];
//				closingString.push(a[1]);
//			}
//			else{
//				System.err.println("Havent handled split case yet");
//				return (long) 0;
//			}
			outputString = a[0];
			//add to stack in reverse order so they are popped in the correct order
			for(int i = a.length - 1; i > 0 ; i--){
				closingString.push(a[i]);
			}
		}
//		System.out.println("return: "+closingString);
		writer.print(outputString);
		return sumCharAsciiValues(outputString);
	}

	//private variables
	private static final String FITCH_VERSION = "3.5.3.24204";
	private static final String FITCH_LINE3_CONSTANT = "FchF";
	private static final String FITCH_FILE_FORMAT = "newFormat";
	private static final String FITCH_OS_CONSTANT_LINUX = "linux:";
	private static final String FITCH_OS_CONSTANT_WINDOWS = "wnds:";
	private static final String FITCH_OS_CONSTANT_MAC = "macs:";
	private static final String OS_NAME = System.getProperty("os.name");
	private static final String OS_VERSION = System.getProperty("os.version");
	
	//DELIMETER, Note: shouldn't matter because all DELIMS should be gone before entering any proof
	//info and before a fitch program ever tries to open
	private static final String DELIM = "$$$";
	
	//fitch output strings
	/* Just used as the first part of line 6, init a proof */
	private static final String PROOF_OPENING_LINE = "=openproof.zen.Openproof{"+DELIM+"}";
	/* Just used as the second part of line 6, init a proof */
	private static final String PROOF_DRIVER = "p=openproof.fitch.FitchProofDriver{"+DELIM+"}";
	/* opens up a new proof, use at very beginning*/
	private static final String PROOF_STARTER = "p=openproof.proofdriver.DRProof{"+DELIM+"}";
	/* opens up a new subproof, plug multiple (comma separated) into PROOF_F*/
	private static final String PROOF_SUB_STARTER = "openproof.proofdriver.DRProof=openproof.proofdriver.DRProof{"+DELIM+"}";
	/* first term in either a new (sub)proof a step */
	private static final String PROOF_STEP_INFO = "s(openproof.proofdriver.DRStepInfo=openproof.proofdriver.DRStepInfo{"+DELIM+"})";
	/* r term for new proof (plug into PROOF_STEP_INFO */
	private static final String PROOF_STEP_INFO_R_DEFAULT = "r&1;";
	/* r term for new step, plug into PROOF_STEP_INFO */
	private static final String PROOF_STEP_INFO_R_NORMAL = "r=openproof.foldriver.FOLDriver{t=\""+DELIM+"\";}";
	/* r term for new lemma, plug into PROOF_STEP_INFO, lemma name goes into DELIM1, lemma text into DELIM2 */
	private static final String PROOF_STEP_INFO_R_LEMMA = "r=openproof.fold.FOLLemmaRule{u="+DELIM+";s=fol;"+DELIM+"}";
	
	/* second term for new (sub)proof or step */
//	private static final String PROOF_RULE = "r=openproof.proofdriver.DRProofRule{"+DELIM+"s=step;}";
	
	/* proof rule for proof starts */
	private static final String PROOF_RULE_PROOF_START = "r=openproof.proofdriver.DRProofRule{u=uProof;s=step;}";
	/* proof rule for premises */
	private static final String PROOF_RULE_PREMISE = "r=openproof.stepdriver.SRPremiseRule{u=uPremise;s=step;}";
	/* proof rule for subproof start */
//	private static final String PROOF_RULE_SUBPROOF_START = "r&24;";
	/* proof rule for negation introduction */
	private static final String PROOF_RULE_NEG_INTRO = "r=openproof.fold.OPNegationIntroRule{u=\"u\\254 Intro\";s=fol;}";
	/* proof rule for negation elimination */
	private static final String PROOF_RULE_NEG_ELIM = "r=openproof.fold.OPNegationElimRule{u=\"u\\254 Elim\";s=fol;}";
	/* proof rule for subproof start */
	private static final String PROOF_RULE_CONJ_ELIM = "r=openproof.fold.OPConjunctionElimRule{u=\"u\\u2227 Elim\";s=fol;}";
	/* proof rule for subproof start */
	private static final String PROOF_RULE_DISJ_ELIM = "r=openproof.fold.OPDisjunctionElimRule{u=\"u\\u2228 Elim\";s=fol;}";
	/* proof rule for subproof start */
	private static final String PROOF_RULE_BICOND_ELIM = "r=openproof.fold.OPBiconditionalElimRule{u=\"u\\u2194 Elim\";s=fol;}";
	/* proof rule for contradiction introduction */
	private static final String PROOF_RULE_CONTR_INTRO = "r=openproof.fold.OPBottomIntroRule{u=\"u\\u22A5 Intro\";s=fol;}";

	/* third term for new (sub)proof or step */
	private static final String PROOF_OBJECT_PROOF_START = "o=openproof.zen.proofdriver.OPDStatusObject{c=1;s=\"\";l=\"\";d@k=\"\";t=false;}";
	/* used in place of PROOF_OBJECT if line is empty or start of subproof */
	private static final String PROOF_OBJECT_SUBPROOF_START = "o&6;";
	/* used in place of PROOF_OBJECT if line is empty (shouldn't happen in truth tree conversions) 
	 * Note: 'f=1;' portion seems like it's only placed on lines that can be verified (not premise/start of subproof, BUT also not on lemmas..),
	 * But it also seems putting it on down doesnt hurt, so it will be left for now */
	private static final String PROOF_OBJECT_NORMAL_LINE = "o=openproof.fold.FOLRuleStatus{c=1;s=\"\";l=\"\";d@k=\"\";t=false;f=1;}";
	/* fourth term for new (sub)proof or step */
	private static final String PROOF_SUPPORT = "u=openproof.proofdriver.DRSupport{t("+DELIM+")}";
	/* references a supporting line, plug into PROOF_SUPOPRT */
	//I believe sb always false, but si and ss change
	private static final String PROOF_SUPPORT_STEP = "openproof.proofdriver.DRSupportPack=openproof.proofdriver.DRSupportPack{si&"+DELIM+";ss="+DELIM+";sb=false;}";
	/* fifth term for new (sub)proof or step */ 
	private static final String PROOF_B = "b()";
	/* sixth term for new (sub)proof or step, contains reminaing stpes of proof */
	private static final String PROOF_F = "f("+DELIM+")";
	/* start of new step, plug multiple (comma separated) into PROOF_F */
	private static final String PROOF_SIMPLE_STEP = "openproof.proofdriver.DRSimpleStep=openproof.proofdriver.DRSimpleStep{"+DELIM+"}";
//	private static final String PROOF_ = "";
//	private static final String PROOF_ = "";
	
	private static final String PROOF_GOAL = "g=openproof.proofdriver.DRGoalList{g()}";
	private static final String PROOF_A = "a=true;";

	//the checksums of all the lemmas, these can be stored because the lemmas are directly copied in
	private static final Long PROOF_LEMMA_CHECKSUM_DEMORGAN1 = (long) 0;
	private static final Long PROOF_LEMMA_CHECKSUM_DEMORGAN2 = (long) 0;
	private static final Long PROOF_LEMMA_CHECKSUM_COND = (long) 1469568;
	private static final Long PROOF_LEMMA_CHECKSUM_NEG_COND = (long) 0;
	private static final Long PROOF_LEMMA_CHECKSUM_BICOND = (long) 2199660;
	private static final Long PROOF_LEMMA_CHECKSUM_NEG_BICOND = (long) 0;
	
}
