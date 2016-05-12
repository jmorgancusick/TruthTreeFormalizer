/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
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

package cusick.john.TTtoF.graphics;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import cusick.john.TTtoF.Converter;

/**
 * ConverterPanel.java uses these files:
 *   images/Open16.gif
 *   images/Save16.gif
 *   images/Convert.gif
 */
public class ConverterPanel extends JPanel
                             implements ActionListener {
	

	/**
	 * generated serial version id to satisfy warning
	 */
	private static final long serialVersionUID = 3470483766196649684L;
	private File ttFile = null;
	private File fitchFile = null;
	
	private static final String TRUTH_TREE_EXTENSION = "tft";
	private static final String FITCH_EXTENSION = "prf";
    
	private static final String OPEN_BUTTON_TEXT = "Select Input Truth Tree...";
	private static final String SAVE_BUTTON_TEXT = "Select Output Fitch File...";
	private static final String CONVERT_BUTTON_TEXT = "Convert";
	
	static private final String newline = "\n";
    
    
    private JButton openButton, saveButton, convertButton;
    private JTextArea log;
    private JFileChooser fc;

    public ConverterPanel() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser(){
        	/**
			 * generated serial version id to satisfy warning
			 */
			private static final long serialVersionUID = -4705685293050532853L;

			//override to make sure files not overwritten
        	@Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?","Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }        
        };

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openButton = new JButton(OPEN_BUTTON_TEXT,
                                 createImageIcon("./images/Open16.gif"));
        openButton.addActionListener(this);

        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton(SAVE_BUTTON_TEXT,
                                 createImageIcon("./images/Save16.gif"));
        saveButton.addActionListener(this);
        
        //Create the save button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        convertButton = new JButton(CONVERT_BUTTON_TEXT,
                                 createImageIcon("./images/Convert.gif"));
        convertButton.setEnabled(false); 
        convertButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);
        
        JPanel convertButtonPanel = new JPanel(); //use FlowLayout
        convertButtonPanel.add(convertButton);
        
        
        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        add(convertButtonPanel, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
        	
        	FileNameExtensionFilter treeFilter = new FileNameExtensionFilter(
        			  TRUTH_TREE_EXTENSION + " files(*." + TRUTH_TREE_EXTENSION + ")",
        			  TRUTH_TREE_EXTENSION);
        	
        	fc.addChoosableFileFilter(treeFilter);
    		fc.setFileFilter(treeFilter);
        	
            int returnVal = fc.showOpenDialog(ConverterPanel.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                ttFile = fc.getSelectedFile();
                //This is where a real application would open the file.
                
                //TODO can overwrite log to keep track of input and output file (just keep 2 active lines)
                
                log.append("Opening: " + ttFile.getName() + newline);
            } else {
                ttFile = null;
                
                log.append("Open command cancelled by user." + newline);
            }
            //enables convert if both an input and output file chosen
            updateConvertButton();
            log.setCaretPosition(log.getDocument().getLength());

        //Handle save button action.
        } else if (e.getSource() == saveButton) {
        	
        	FileNameExtensionFilter fitchFilter = new FileNameExtensionFilter(
      			  FITCH_EXTENSION + " files(*." + FITCH_EXTENSION + ")",
      			  FITCH_EXTENSION);
      	
        	fc.addChoosableFileFilter(fitchFilter);
        	fc.setFileFilter(fitchFilter);
        	
        	
            int returnVal = fc.showSaveDialog(ConverterPanel.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fitchFile = fc.getSelectedFile();
                //This is where a real application would save the file.
                
                log.append("Saving: " + fitchFile.getName() + "." + FITCH_EXTENSION + newline);
            } else {
                fitchFile = null;
                
                log.append("Save command cancelled by user." + newline);
            }
            //enables convert if both an input and output file chosen
            updateConvertButton();
            
            log.setCaretPosition(log.getDocument().getLength());
        } else if (e.getSource() == convertButton) {
            
            //This is where a real application would convert.
            Converter converter= new Converter();
        	converter.truthTreeToFitch(ttFile, fitchFile);
              
        	log.append("Convert command executed by user." + newline);
            log.setCaretPosition(log.getDocument().getLength());
        }
        
    }
    
    
    private void updateConvertButton(){
    	if(ttFile != null && fitchFile != null){
        	convertButton.setEnabled(true); 
        } else{
        	convertButton.setEnabled(false); 
        }
    }
    
    

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ConverterPanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Truth Tree Formalizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        //Add content to the window.
        frame.add(new ConverterPanel());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
           


	public File getTtFile() {
		return ttFile;
	}

	public void setTtFile(File ttFile) {
		this.ttFile = ttFile;
	}

	public File getFitchFile() {
		return fitchFile;
	}

	public void setFitchFile(File fitchFile) {
		this.fitchFile = fitchFile;
	}
}
