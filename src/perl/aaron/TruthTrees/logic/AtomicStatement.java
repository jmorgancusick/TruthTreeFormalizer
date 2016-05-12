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

public class AtomicStatement extends Statement {
	private String _symbol;
	/**
	 * Creates an atomic statement with a given symbol
	 * @param symbol The character representing the statement
	 */
	public AtomicStatement(String symbol)
	{
		_symbol = symbol;
	}
	/**
	 * Returns the statement's symbol
	 * @return The character representing the statement
	 */
	public String getSymbol()
	{
		return _symbol;
	}
	public String toString()
	{
		return _symbol;
	}
	public String toStringParen()
	{
		return _symbol;
	}
	public boolean equals(Object other)
	{
		if (!(other instanceof AtomicStatement))
			return false;
		AtomicStatement otherAS = (AtomicStatement) other;
		return (otherAS.getSymbol().equals(_symbol));
	}
	public boolean equals(Statement other) {
		if (!(other instanceof AtomicStatement))
			return false;
		return ((AtomicStatement)other).getSymbol().equals(_symbol);
	}
}
