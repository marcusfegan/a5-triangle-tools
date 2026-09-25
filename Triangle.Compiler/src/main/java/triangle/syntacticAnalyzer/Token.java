/*
 * @(#)Token.java                       
 * 
 * Revisions and updates (c) 2022-2025 Sandy Brownlee. alexander.brownlee@stir.ac.uk
 * 
 * Original release:
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle.syntacticAnalyzer;

final class Token {

	protected Kind kind;
	protected String spelling;
	protected SourcePosition position;

	public Token(Kind kind, String spelling, SourcePosition position) {

		// If this token is an identifier, is it also a reserved word?
		if (kind == Kind.IDENTIFIER) {
			this.kind = Kind.fromSpelling(spelling);
		} else {
			this.kind = kind;
		}

		this.spelling = spelling;
		this.position = position;

	}

	public static String spell(Kind kind) {
		return kind.spelling;
	}

	@Override
	public String toString() {
		return "Kind=" + kind + ", spelling=" + spelling + ", position=" + position;
	}

	// Token classes...

	public enum Kind {
		// literals, identifiers, operators...
		INTLITERAL("<int>"), CHARLITERAL("<char>"), IDENTIFIER("<identifier>"), OPERATOR("<operator>"),

		// reserved words - keep in alphabetical order for ease of maintenance...
		ARRAY("array"), BEGIN("begin"), CONST("const"), DO("do"), ELSE("else"), END("end"), FUNC("func"), IF("if"), IN("in"), LET("let"), OF("of"),
		PROC("proc"), RECORD("record"), REPEAT ("repeat"),  THEN("then"), TYPE("type"), UNTIL ("until"),  VAR("var"), WHILE("while"),

		// punctuation...
		DOT("."), COLON(":"), SEMICOLON(";"), COMMA(","), BECOMES(":="), IS("~"),

		// brackets...
		LPAREN("("), RPAREN(")"), LBRACKET("["), RBRACKET("]"), LCURLY("{"), RCURLY("}"),

		// special tokens...
		EOT(""), ERROR("<error>");
		
	    public final String spelling;
		
	    private Kind(String spelling) {
	        this.spelling = spelling;
	    }
	    
	    /**
	     * iterate over the reserved words above to find the one with a given spelling
	     * need to specify firstReservedWord and lastReservedWord (inclusive) for this
	     * to work!
	     * 
	     * @return Kind.IDENTIFIER if no matching token class found
	     */
	    public static Kind fromSpelling(String spelling) {
	    	boolean isRW = false;
	    	for (Kind kind: Kind.values()) {
	    		if (kind == firstReservedWord) {
	    			isRW = true;
	    		}
	    		
	    		if (isRW && kind.spelling.equals(spelling)) {
	    			return kind;
	    		}
	    		
	    		if (kind == lastReservedWord) {
	    			// if we get here, we've not found a match, so break and return failure
	    			break;
	    		}
	    	}
	    	return Kind.IDENTIFIER;
	    }
	    
	    private final static Kind firstReservedWord = ARRAY, lastReservedWord = WHILE;
	}

}
