/*
 * @(#)Scanner.java                       
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

public final class Scanner {

	private SourceFile sourceFile;
	private boolean debug;

	private char currentChar;
	private StringBuffer currentSpelling;
	private boolean currentlyScanningToken;

	public static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	public static boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	// isOperator returns true iff the given character is an operator character.

	public static boolean isOperator(char c) {
		return (c == '+' || c == '-' || c == '*' || c == '/' || c == '=' || c == '<' || c == '>' || c == '\\'
				|| c == '&' || c == '@' || c == '%' || c == '^' || c == '?' || c == '!');
	}

	///////////////////////////////////////////////////////////////////////////////

	public Scanner(SourceFile source) {
		sourceFile = source;
		currentChar = sourceFile.getSource();
		debug = false;
	}

	public void enableDebugging() {
		debug = true;
	}

	// takeIt appends the current character to the current token, and gets
	// the next character from the source program.

	private void takeIt() {
		if (currentlyScanningToken)
			currentSpelling.append(currentChar);
		currentChar = sourceFile.getSource();
	}

	// scanSeparator skips a single separator.

	private void scanSeparator() {
		switch (currentChar) {
		
		// comment
		case '!':
		case '#':
			takeIt();
			
			// the comment ends when we reach an end-of-line (EOL) or end of file (EOT - for end-of-transmission)
			while ((currentChar != SourceFile.EOL) && (currentChar != SourceFile.EOT))
				takeIt();
			if (currentChar == SourceFile.EOL)
				takeIt();
			break;

			// block comments
			case '$':
				takeIt();
				// the comment ends when we reach a closing $
				while (currentChar != '$' && currentChar != SourceFile.EOT)
					takeIt();
				if (currentChar == '$')
					takeIt();
				break;

		// whitespace
		case ' ':
		case '\n':
		case '\r':
		case '\t':
			takeIt();
			break;
		}
	}

	private Token.Kind scanToken() {

		switch (currentChar) {

		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
			takeIt();
			while (isLetter(currentChar) || isDigit(currentChar))
				takeIt();
			return Token.Kind.IDENTIFIER;

		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			takeIt();
			while (isDigit(currentChar))
				takeIt();
			return Token.Kind.INTLITERAL;

		case '+':
		case '-':
		case '*':
		case '/':
		case '=':
		case '<':
		case '>':
		case '\\':
		case '&':
		case '@':
		case '%':
		case '^':
		case '?':
		case '|':
			takeIt();
			while (isOperator(currentChar))
				takeIt();
			return Token.Kind.OPERATOR;

		case '\'':
			takeIt();
			takeIt(); // the quoted character
			if (currentChar == '\'') {
				takeIt();
				return Token.Kind.CHARLITERAL;
			} else
				return Token.Kind.ERROR;

		case '.':
			takeIt();
			return Token.Kind.DOT;

		case ':':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return Token.Kind.BECOMES;
			} else
				return Token.Kind.COLON;

		case ';':
			takeIt();
			return Token.Kind.SEMICOLON;

		case ',':
			takeIt();
			return Token.Kind.COMMA;

		case '~':
			takeIt();
			return Token.Kind.IS;

		case '(':
			takeIt();
			return Token.Kind.LPAREN;

		case ')':
			takeIt();
			return Token.Kind.RPAREN;

		case '[':
			takeIt();
			return Token.Kind.LBRACKET;

		case ']':
			takeIt();
			return Token.Kind.RBRACKET;

		case '{':
			takeIt();
			return Token.Kind.LCURLY;

		case '}':
			takeIt();
			return Token.Kind.RCURLY;

		case SourceFile.EOT:
			return Token.Kind.EOT;

		default:
			takeIt();
			return Token.Kind.ERROR;
		}
	}

	public Token scan() {
		Token tok;
		SourcePosition pos;
		Token.Kind kind;

		currentlyScanningToken = false;
		// skip any whitespace or comments
		while (currentChar == '!' || currentChar == ' ' || currentChar == '\n' || currentChar == '\r'
				|| currentChar == '\t' || currentChar == '#' || currentChar == '$')
			scanSeparator();

		currentlyScanningToken = true;
		currentSpelling = new StringBuffer("");
		pos = new SourcePosition();
		pos.start = sourceFile.getCurrentLine();

		kind = scanToken();

		pos.finish = sourceFile.getCurrentLine();
		tok = new Token(kind, currentSpelling.toString(), pos);
		if (debug)
			System.out.println(tok);
		return tok;
	}

}
