/*
 * @(#)StdEnvironment.java                       
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

package triangle;

import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDenoter;

public final class StdEnvironment {

	// These are small ASTs representing standard types.

	public static TypeDenoter booleanType, charType, integerType, anyType, errorType;

	public static TypeDeclaration booleanDecl, charDecl, integerDecl;

	// These are small ASTs representing "declarations" of standard entities.

	public static ConstDeclaration falseDecl, trueDecl, maxintDecl;

	public static UnaryOperatorDeclaration notDecl, barDecl;

	public static BinaryOperatorDeclaration andDecl, orDecl, addDecl, subtractDecl, multiplyDecl, divideDecl,
			moduloDecl, equalDecl, unequalDecl, lessDecl, notlessDecl, greaterDecl, notgreaterDecl;

	public static ProcDeclaration getDecl, putDecl, getintDecl, putintDecl, geteolDecl, puteolDecl;

	public static FuncDeclaration chrDecl, ordDecl, eolDecl, eofDecl;

}
