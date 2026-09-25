/*
 * @(#)Checker.java                       
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

package triangle.contextualAnalyzer;

import triangle.ErrorReporter;
import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.actuals.ConstActualParameter;
import triangle.abstractSyntaxTrees.actuals.EmptyActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.FuncActualParameter;
import triangle.abstractSyntaxTrees.actuals.MultipleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.ProcActualParameter;
import triangle.abstractSyntaxTrees.actuals.SingleActualParameterSequence;
import triangle.abstractSyntaxTrees.actuals.VarActualParameter;
import triangle.abstractSyntaxTrees.aggregates.MultipleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.MultipleRecordAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleArrayAggregate;
import triangle.abstractSyntaxTrees.aggregates.SingleRecordAggregate;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.BinaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.ConstantDeclaration;
import triangle.abstractSyntaxTrees.declarations.Declaration;
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.FunctionDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcedureDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.declarations.VariableDeclaration;
import triangle.abstractSyntaxTrees.expressions.ArrayExpression;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.CallExpression;
import triangle.abstractSyntaxTrees.expressions.CharacterExpression;
import triangle.abstractSyntaxTrees.expressions.EmptyExpression;
import triangle.abstractSyntaxTrees.expressions.IfExpression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.RecordExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.formals.ConstFormalParameter;
import triangle.abstractSyntaxTrees.formals.EmptyFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FormalParameter;
import triangle.abstractSyntaxTrees.formals.FormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.terminals.Terminal;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.FieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
import triangle.abstractSyntaxTrees.types.TypeDenoter;
import triangle.abstractSyntaxTrees.visitors.ActualParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.ActualParameterVisitor;
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.FormalParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.IdentifierVisitor;
import triangle.abstractSyntaxTrees.visitors.LiteralVisitor;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.abstractSyntaxTrees.visitors.VnameVisitor;
import triangle.abstractSyntaxTrees.vnames.DotVname;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;
import triangle.abstractSyntaxTrees.vnames.SubscriptVname;
import triangle.syntacticAnalyzer.SourcePosition;

public final class Checker implements ActualParameterVisitor<FormalParameter, Void>,
		ActualParameterSequenceVisitor<FormalParameterSequence, Void>, ArrayAggregateVisitor<Void, TypeDenoter>,
		CommandVisitor<Void, Void>, DeclarationVisitor<Void, Void>, ExpressionVisitor<Void, TypeDenoter>,
		FormalParameterSequenceVisitor<Void, Void>, IdentifierVisitor<Void, Declaration>,
		LiteralVisitor<Void, TypeDenoter>, OperatorVisitor<Void, Declaration>, ProgramVisitor<Void, Void>,
		RecordAggregateVisitor<Void, FieldTypeDenoter>, TypeDenoterVisitor<Void, TypeDenoter>,
		VnameVisitor<Void, TypeDenoter> {

	// Commands

	// Always returns null. Does not use the given object.

	@Override
	public Void visitAssignCommand(AssignCommand ast, Void arg) {
		var vType = ast.V.visit(this);
		var eType = ast.E.visit(this);

		checkAndReportError(ast.V.variable, "LHS of assignment is not a variable", ast.V);
		checkAndReportError(eType.equals(vType), "assignment incompatibilty", ast);

		return null;
	}

	@Override
	public Void visitCallCommand(CallCommand ast, Void arg) {
		var binding = ast.I.visit(this);

		if (binding instanceof ProcedureDeclaration procedure) {
			ast.APS.visit(this, procedure.getFormals());
		} else {
			reportUndeclaredOrError(binding, ast.I, "\"%\" is not a procedure identifier");
		}

		return null;
	}

	@Override
	public Void visitEmptyCommand(EmptyCommand ast, Void arg) {
		return null;
	}

	@Override
	public Void visitIfCommand(IfCommand ast, Void arg) {
		var eType = ast.E.visit(this);

		checkAndReportError(eType.equals(StdEnvironment.booleanType), "Boolean expression expected here", ast.E);

		ast.C1.visit(this);
		ast.C2.visit(this);

		return null;
	}

	@Override
	public Void visitLetCommand(LetCommand ast, Void arg) {
		idTable.openScope();
		ast.D.visit(this);
		ast.C.visit(this);
		idTable.closeScope();
		return null;
	}

	@Override
	public Void visitSequentialCommand(SequentialCommand ast, Void arg) {
		ast.C1.visit(this);
		ast.C2.visit(this);
		return null;
	}

	@Override
	public Void visitWhileCommand(WhileCommand ast, Void arg) {
		var eType = ast.E.visit(this);

		checkAndReportError(eType.equals(StdEnvironment.booleanType), "Boolean expression expected here", ast.E);
		ast.C.visit(this);

		return null;
	}

	@Override
	public Void visitRepeatCommand(RepeatCommand ast, Void unused) {
		return null;
	}

	// Expressions

	// Returns the TypeDenoter denoting the type of the expression. Does
	// not use the given object.

	@Override
	public TypeDenoter visitArrayExpression(ArrayExpression ast, Void arg) {
		var elemType = ast.AA.visit(this);
		var il = new IntegerLiteral(Integer.toString(ast.AA.elemCount), ast.getPosition());
		ast.type = new ArrayTypeDenoter(il, elemType, ast.getPosition());
		return ast.type;
	}

	@Override
	public TypeDenoter visitBinaryExpression(BinaryExpression ast, Void arg) {
		var e1Type = ast.E1.visit(this);
		var e2Type = ast.E2.visit(this);
		var binding = ast.O.visit(this);

		if (binding instanceof BinaryOperatorDeclaration bbinding) {
			if (bbinding.ARG1 == StdEnvironment.anyType) {
				// this operator must be "=" or "\="
				checkAndReportError(e1Type.equals(e2Type), "incompatible argument types for \"%\"", ast.O, ast);
			} else {
				checkAndReportError(e1Type.equals(bbinding.ARG1), "wrong argument type for \"%\"", ast.O, ast.E1);
				checkAndReportError(e2Type.equals(bbinding.ARG2), "wrong argument type for \"%\"", ast.O, ast.E2);
			}
			return ast.type = bbinding.RES;
		}

		reportUndeclaredOrError(binding, ast.O, "\"%\" is not a binary operator");
		return ast.type = StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitCallExpression(CallExpression ast, Void arg) {
		var binding = ast.I.visit(this);

		if (binding instanceof FunctionDeclaration function) {
			ast.APS.visit(this, function.getFormals());
			return ast.type = function.getType();
		}

		reportUndeclaredOrError(binding, ast.I, "\"%\" is not a function identifier");
		return ast.type = StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitCharacterExpression(CharacterExpression ast, Void arg) {
		return ast.type = StdEnvironment.charType;
	}

	@Override
	public TypeDenoter visitEmptyExpression(EmptyExpression ast, Void arg) {
		return ast.type = null;
	}

	@Override
	public TypeDenoter visitIfExpression(IfExpression ast, Void arg) {
		var e1Type = ast.E1.visit(this);
		checkAndReportError(e1Type.equals(StdEnvironment.booleanType), "Boolean expression expected here", ast.E1);

		var e2Type = ast.E2.visit(this);
		var e3Type = ast.E3.visit(this);
		checkAndReportError(e2Type.equals(e3Type), "incompatible limbs in if-expression", ast);
		return ast.type = e2Type;
	}

	@Override
	public TypeDenoter visitIntegerExpression(IntegerExpression ast, Void arg) {
		return ast.type = StdEnvironment.integerType;
	}

	@Override
	public TypeDenoter visitLetExpression(LetExpression ast, Void arg) {
		idTable.openScope();
		ast.D.visit(this);
		ast.type = ast.E.visit(this);
		idTable.closeScope();
		return ast.type;
	}

	@Override
	public TypeDenoter visitRecordExpression(RecordExpression ast, Void arg) {
		var rType = ast.RA.visit(this);
		return ast.type = new RecordTypeDenoter(rType, ast.getPosition());
	}

	@Override
	public TypeDenoter visitUnaryExpression(UnaryExpression ast, Void arg) {
		var eType = ast.E.visit(this);
		var binding = ast.O.visit(this);

		if (binding instanceof UnaryOperatorDeclaration ubinding) {
			checkAndReportError(eType.equals(ubinding.ARG), "wrong argument type for \"%\"", ast.O);
			return ast.type = ubinding.RES;
		}

		reportUndeclaredOrError(binding, ast.O, "\"%\" is not a unary operator");
		return ast.type = StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitVnameExpression(VnameExpression ast, Void arg) {
		return ast.type = ast.V.visit(this);
	}

	// Declarations

	// Always returns null. Does not use the given object.
	@Override
	public Void visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
		return null;
	}

	@Override
	public Void visitConstDeclaration(ConstDeclaration ast, Void arg) {
		ast.E.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "identifier \"%\" already declared", ast.I, ast);
		return null;
	}

	@Override
	public Void visitFuncDeclaration(FuncDeclaration ast, Void arg) {
		ast.T = ast.T.visit(this);
		// permits recursion
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "identifier \"%\" already declared", ast.I, ast);

		idTable.openScope();
		ast.FPS.visit(this);
		var eType = ast.E.visit(this);
		idTable.closeScope();

		checkAndReportError(ast.T.equals(eType), "body of function \"%\" has wrong type", ast.I, ast.E);
		return null;
	}

	@Override
	public Void visitProcDeclaration(ProcDeclaration ast, Void arg) {
		// permits recursion
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "identifier \"%\" already declared", ast.I, ast);

		idTable.openScope();
		ast.FPS.visit(this);
		ast.C.visit(this);
		idTable.closeScope();

		return null;
	}

	@Override
	public Void visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
		ast.D1.visit(this);
		ast.D2.visit(this);
		return null;
	}

	@Override
	public Void visitTypeDeclaration(TypeDeclaration ast, Void arg) {
		ast.T = ast.T.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "identifier \"%\" already declared", ast.I, ast);
		return null;
	}

	@Override
	public Void visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
		return null;
	}

	@Override
	public Void visitVarDeclaration(VarDeclaration ast, Void arg) {
		ast.T = ast.T.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "identifier \"%\" already declared", ast.I, ast);
		return null;
	}

	// Array Aggregates

	// Returns the TypeDenoter for the Array Aggregate. Does not use the
	// given object.

	@Override
	public TypeDenoter visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
		var eType = ast.E.visit(this);
		var elemType = ast.AA.visit(this);
		ast.elemCount = ast.AA.elemCount + 1;
		checkAndReportError(eType.equals(elemType), "incompatible array-aggregate element", ast.E);
		return elemType;
	}

	@Override
	public TypeDenoter visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
		var elemType = ast.E.visit(this);
		ast.elemCount = 1;
		return elemType;
	}

	// Record Aggregates

	// Returns the TypeDenoter for the Record Aggregate. Does not use the
	// given object.

	@Override
	public FieldTypeDenoter visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
		var eType = ast.E.visit(this);
		var rType = ast.RA.visit(this);
		var fType = checkFieldIdentifier(rType, ast.I);
		checkAndReportError(fType.equals(StdEnvironment.errorType), "duplicate field \"%\" in record", ast.I);
		return ast.type = new MultipleFieldTypeDenoter(ast.I, eType, rType, ast.getPosition());
	}

	@Override
	public FieldTypeDenoter visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
		var eType = ast.E.visit(this);
		return ast.type = new SingleFieldTypeDenoter(ast.I, eType, ast.getPosition());
	}

	// Formal Parameters

	// Always returns null. Does not use the given object.

	@Override
	public Void visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
		ast.T = ast.T.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "duplicated formal parameter \"%\"", ast.I, ast);
		return null;
	}

	@Override
	public Void visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
		idTable.openScope();
		ast.FPS.visit(this);
		idTable.closeScope();
		ast.T = ast.T.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "duplicated formal parameter \"%\"", ast.I, ast);
		return null;
	}

	@Override
	public Void visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
		idTable.openScope();
		ast.FPS.visit(this);
		idTable.closeScope();
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "duplicated formal parameter \"%\"", ast.I, ast);
		return null;
	}

	@Override
	public Void visitVarFormalParameter(VarFormalParameter ast, Void arg) {
		ast.T = ast.T.visit(this);
		idTable.enter(ast.I.spelling, ast);
		checkAndReportError(!ast.duplicated, "duplicated formal parameter \"%\"", ast.I, ast);
		return null;
	}

	@Override
	public Void visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
		return null;
	}

	@Override
	public Void visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
		ast.FP.visit(this);
		ast.FPS.visit(this);
		return null;
	}

	@Override
	public Void visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
		ast.FP.visit(this);
		return null;
	}

	// Actual Parameters

	// Always returns null. Uses the given FormalParameter.

	@Override
	public Void visitConstActualParameter(ConstActualParameter ast, FormalParameter arg) {
		var eType = ast.E.visit(this);
		if (arg instanceof ConstFormalParameter param) {
			checkAndReportError(eType.equals(param.T), "wrong type for const actual parameter", ast.E);
		} else {
			reportError("const actual parameter not expected here", ast);
		}
		return null;
	}

	@Override
	public Void visitFuncActualParameter(FuncActualParameter ast, FormalParameter arg) {
		var binding = ast.I.visit(this);
		if (binding instanceof FunctionDeclaration function) {
			var formals = function.getFormals();
			var functionType = function.getType();
			if (arg instanceof FuncFormalParameter param) {
				if (!formals.equals(param.getFormals())) {
					reportError("wrong signature for function \"%\"", ast.I);
				} else if (!functionType.equals(param.T)) {
					reportError("wrong type for function \"%\"", ast.I);
				}
			} else {
				reportError("func actual parameter not expected here", ast);
			}
		} else {
			reportUndeclaredOrError(binding, ast.I, "\"%\" is not a function identifier");
		}
		return null;
	}

	@Override
	public Void visitProcActualParameter(ProcActualParameter ast, FormalParameter arg) {
		var binding = ast.I.visit(this);
		if (binding instanceof ProcedureDeclaration procedure) {
			var formals = procedure.getFormals();
			if (arg instanceof ProcFormalParameter param) {
				checkAndReportError(formals.equals(param.getFormals()), "wrong signature for procedure \"%\"", ast.I);
			} else {
				reportError("proc actual parameter not expected here", ast);
			}
		} else {
			reportUndeclaredOrError(binding, ast.I, "\"%\" is not a procedure identifier");
		}
		return null;
	}

	@Override
	public Void visitVarActualParameter(VarActualParameter ast, FormalParameter arg) {
		var vType = ast.V.visit(this);
		if (!ast.V.variable) {
			reportError("actual parameter is not a variable", ast.V);
		} else if (arg instanceof VarFormalParameter parameter) {
			checkAndReportError(vType.equals(parameter.T), "wrong type for var actual parameter", ast.V);
		} else {
			reportError("var actual parameter not expected here", ast.V);
		}
		return null;
	}

	@Override
	public Void visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, FormalParameterSequence arg) {
		checkAndReportError(arg instanceof EmptyFormalParameterSequence, "too few actual parameters", ast);
		return null;
	}

	@Override
	public Void visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, FormalParameterSequence arg) {
		if (arg instanceof MultipleFormalParameterSequence formals) {
			ast.AP.visit(this, formals.FP);
			ast.APS.visit(this, formals.FPS);
		} else {
			reportError("too many actual parameters", ast);
		}
		return null;
	}

	@Override
	public Void visitSingleActualParameterSequence(SingleActualParameterSequence ast, FormalParameterSequence arg) {
		if (arg instanceof SingleFormalParameterSequence formal) {
			ast.AP.visit(this, formal.FP);
		} else {
			reportError("incorrect number of actual parameters", ast);
		}
		return null;
	}

	// Type Denoters

	// Returns the expanded version of the TypeDenoter. Does not
	// use the given object.

	@Override
	public TypeDenoter visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) {
		return StdEnvironment.anyType;
	}

	@Override
	public TypeDenoter visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
		ast.T = ast.T.visit(this);
		checkAndReportError(ast.IL.getValue() != 0, "arrays must not be empty", ast.IL);
		return ast;
	}

	@Override
	public TypeDenoter visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) {
		return StdEnvironment.booleanType;
	}

	@Override
	public TypeDenoter visitCharTypeDenoter(CharTypeDenoter ast, Void arg) {
		return StdEnvironment.charType;
	}

	@Override
	public TypeDenoter visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) {
		return StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
		var binding = ast.I.visit(this);
		if (binding instanceof TypeDeclaration decl) {
			return decl.T;
		}

		reportUndeclaredOrError(binding, ast.I, "\"%\" is not a type identifier");
		return StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitIntTypeDenoter(IntTypeDenoter ast, Void arg) {
		return StdEnvironment.integerType;
	}

	@Override
	public TypeDenoter visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
		ast.FT = (FieldTypeDenoter) ast.FT.visit(this);
		return ast;
	}

	@Override
	public TypeDenoter visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
		ast.T = ast.T.visit(this);
		ast.FT.visit(this);
		return ast;
	}

	@Override
	public TypeDenoter visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
		ast.T = ast.T.visit(this);
		return ast;
	}

	// Literals, Identifiers and Operators
	@Override
	public TypeDenoter visitCharacterLiteral(CharacterLiteral CL, Void arg) {
		return StdEnvironment.charType;
	}

	@Override
	public Declaration visitIdentifier(Identifier I, Void arg) {
		var binding = idTable.retrieve(I.spelling);
		if (binding != null) {
			I.decl = binding;
		}
		return binding;
	}

	@Override
	public TypeDenoter visitIntegerLiteral(IntegerLiteral IL, Void arg) {
		return StdEnvironment.integerType;
	}

	@Override
	public Declaration visitOperator(Operator O, Void arg) {
		var binding = idTable.retrieve(O.spelling);
		if (binding != null) {
			O.decl = binding;
		}
		return binding;
	}

	// Value-or-variable names

	// Determines the address of a named object (constant or variable).
	// This consists of a base object, to which 0 or more field-selection
	// or array-indexing operations may be applied (if it is a record or
	// array). As much as possible of the address computation is done at
	// compile-time. Code is generated only when necessary to evaluate
	// index expressions at run-time.
	// currentLevel is the routine level where the v-name occurs.
	// frameSize is the anticipated size of the local stack frame when
	// the object is addressed at run-time.
	// It returns the description of the base object.
	// offset is set to the total of any field offsets (plus any offsets
	// due to index expressions that happen to be literals).
	// indexed is set to true iff there are any index expressions (other
	// than literals). In that case code is generated to compute the
	// offset due to these indexing operations at run-time.

	// Returns the TypeDenoter of the Vname. Does not use the
	// given object.

	@Override
	public TypeDenoter visitDotVname(DotVname ast, Void arg) {
		ast.type = null;
		var vType = ast.V.visit(this);
		ast.variable = ast.V.variable;
		if (vType instanceof RecordTypeDenoter record) {
			ast.type = checkFieldIdentifier(record.FT, ast.I);
			checkAndReportError(ast.type != StdEnvironment.errorType, "no field \"%\" in this record type",
					ast.I);
		} else {
			reportError("record expected here", ast.V);
		}
		return ast.type;
	}

	@Override
	public TypeDenoter visitSimpleVname(SimpleVname ast, Void arg) {
		ast.variable = false;
		ast.type = StdEnvironment.errorType;

		var binding = ast.I.visit(this);
		if (binding instanceof ConstantDeclaration constant) {
			ast.variable = false;
			return ast.type = constant.getType();
		} else if (binding instanceof VariableDeclaration variable) {
			ast.variable = true;
			return ast.type = variable.getType();
		}

		reportUndeclaredOrError(binding, ast.I, "\"%\" is not a const or var identifier");
		return ast.type = StdEnvironment.errorType;
	}

	@Override
	public TypeDenoter visitSubscriptVname(SubscriptVname ast, Void arg) {
		var vType = ast.V.visit(this);
		ast.variable = ast.V.variable;

		var eType = ast.E.visit(this);
		if (vType != StdEnvironment.errorType) {
			if (vType instanceof ArrayTypeDenoter arrayType) {
				checkAndReportError(eType.equals(StdEnvironment.integerType), "Integer expression expected here",
						ast.E);
				ast.type = arrayType.T;
			} else {
				reportError("array expected here", ast.V);
			}
		}

		return ast.type;
	}

	// Programs

	@Override
	public Void visitProgram(Program ast, Void arg) {
		ast.C.visit(this);
		return null;
	}

	// Checks whether the source program, represented by its AST, satisfies the
	// language's scope rules and type rules.
	// Also decorates the AST as follows:
	// (a) Each applied occurrence of an identifier or operator is linked to
	// the corresponding declaration of that identifier or operator.
	// (b) Each expression and value-or-variable-name is decorated by its type.
	// (c) Each type identifier is replaced by the type it denotes.
	// Types are represented by small ASTs.

	public void check(Program ast) {
		ast.visit(this);
	}

	/////////////////////////////////////////////////////////////////////////////

	public Checker(ErrorReporter reporter) {
		this.reporter = reporter;
		this.idTable = new IdentificationTable();
		establishStdEnvironment();
	}

	private IdentificationTable idTable;
	private static SourcePosition dummyPos = new SourcePosition();
	private ErrorReporter reporter;

	private void reportUndeclaredOrError(Declaration binding, Terminal leaf, String message) {
		if (binding == null) {
			reportError("\"%\" is not declared", leaf);
		} else {
			reportError(message, leaf);
		}
	}

	private void reportError(String message, Terminal ast) {
		reportError(message, ast, ast);
	}

	private void reportError(String message, Terminal spellingNode, AbstractSyntaxTree positionNode) {
		reporter.reportError(message, spellingNode.spelling, positionNode.getPosition());
	}

	private void reportError(String message, AbstractSyntaxTree positionNode) {
		reporter.reportError(message, "", positionNode.getPosition());
	}

	private void checkAndReportError(boolean condition, String message, String token, SourcePosition position) {
		if (!condition) {
			reporter.reportError(message, token, position);
		}
	}

	private void checkAndReportError(boolean condition, String message, Terminal ast) {
		checkAndReportError(condition, message, ast, ast);
	}

	private void checkAndReportError(boolean condition, String message, Terminal spellingNode,
			AbstractSyntaxTree positionNode) {
		checkAndReportError(condition, message, spellingNode.spelling, positionNode.getPosition());
	}

	private void checkAndReportError(boolean condition, String message, AbstractSyntaxTree positionNode) {
		checkAndReportError(condition, message, "", positionNode.getPosition());
	}

	private static TypeDenoter checkFieldIdentifier(FieldTypeDenoter ast, Identifier I) {
		if (ast instanceof MultipleFieldTypeDenoter ft) {
			if (ft.I.spelling.compareTo(I.spelling) == 0) {
				I.decl = ast;
				return ft.T;
			} else {
				return checkFieldIdentifier(ft.FT, I);
			}
		} else if (ast instanceof SingleFieldTypeDenoter ft) {
			if (ft.I.spelling.compareTo(I.spelling) == 0) {
				I.decl = ast;
				return ft.T;
			}
		}
		return StdEnvironment.errorType;
	}

	// Creates a small AST to represent the "declaration" of a standard
	// type, and enters it in the identification table.

	private TypeDeclaration declareStdType(String id, TypeDenoter typedenoter) {

		var binding = new TypeDeclaration(new Identifier(id, dummyPos), typedenoter, dummyPos);
		idTable.enter(id, binding);
		return binding;
	}

	// Creates a small AST to represent the "declaration" of a standard
	// type, and enters it in the identification table.

	private ConstDeclaration declareStdConst(String id, TypeDenoter constType) {

		// constExpr used only as a placeholder for constType
		var constExpr = new IntegerExpression(null, dummyPos);
		constExpr.type = constType;
		var binding = new ConstDeclaration(new Identifier(id, dummyPos), constExpr, dummyPos);
		idTable.enter(id, binding);
		return binding;
	}

	// Creates a small AST to represent the "declaration" of a standard
	// type, and enters it in the identification table.

	private ProcDeclaration declareStdProc(String id, FormalParameterSequence fps) {

		var binding = new ProcDeclaration(new Identifier(id, dummyPos), fps, new EmptyCommand(dummyPos), dummyPos);
		idTable.enter(id, binding);
		return binding;
	}

	// Creates a small AST to represent the "declaration" of a standard
	// type, and enters it in the identification table.

	private FuncDeclaration declareStdFunc(String id, FormalParameterSequence fps, TypeDenoter resultType) {

		var binding = new FuncDeclaration(new Identifier(id, dummyPos), fps, resultType, new EmptyExpression(dummyPos),
				dummyPos);
		idTable.enter(id, binding);
		return binding;
	}

	// Creates a small AST to represent the "declaration" of a
	// unary operator, and enters it in the identification table.
	// This "declaration" summarises the operator's type info.

	private UnaryOperatorDeclaration declareStdUnaryOp(String op, TypeDenoter argType, TypeDenoter resultType) {

		var binding = new UnaryOperatorDeclaration(new Operator(op, dummyPos), argType, resultType, dummyPos);
		idTable.enter(op, binding);
		return binding;
	}

	// Creates a small AST to represent the "declaration" of a
	// binary operator, and enters it in the identification table.
	// This "declaration" summarises the operator's type info.

	private BinaryOperatorDeclaration declareStdBinaryOp(String op, TypeDenoter arg1Type, TypeDenoter arg2type,
			TypeDenoter resultType) {

		var binding = new BinaryOperatorDeclaration(new Operator(op, dummyPos), arg1Type, arg2type, resultType,
				dummyPos);
		idTable.enter(op, binding);
		return binding;
	}

	// Creates small ASTs to represent the standard types.
	// Creates small ASTs to represent "declarations" of standard types,
	// constants, procedures, functions, and operators.
	// Enters these "declarations" in the identification table.

	private final static Identifier dummyI = new Identifier("", dummyPos);

	private void establishStdEnvironment() {

		// idTable.startIdentification();
		StdEnvironment.booleanType = new BoolTypeDenoter(dummyPos);
		StdEnvironment.integerType = new IntTypeDenoter(dummyPos);
		StdEnvironment.charType = new CharTypeDenoter(dummyPos);
		StdEnvironment.anyType = new AnyTypeDenoter(dummyPos);
		StdEnvironment.errorType = new ErrorTypeDenoter(dummyPos);

		StdEnvironment.booleanDecl = declareStdType("Boolean", StdEnvironment.booleanType);
		StdEnvironment.falseDecl = declareStdConst("false", StdEnvironment.booleanType);
		StdEnvironment.trueDecl = declareStdConst("true", StdEnvironment.booleanType);
		StdEnvironment.notDecl = declareStdUnaryOp("\\", StdEnvironment.booleanType, StdEnvironment.booleanType);
		StdEnvironment.andDecl = declareStdBinaryOp("/\\", StdEnvironment.booleanType, StdEnvironment.booleanType,
				StdEnvironment.booleanType);
		StdEnvironment.orDecl = declareStdBinaryOp("\\/", StdEnvironment.booleanType, StdEnvironment.booleanType,
				StdEnvironment.booleanType);

		StdEnvironment.integerDecl = declareStdType("Integer", StdEnvironment.integerType);
		StdEnvironment.maxintDecl = declareStdConst("maxint", StdEnvironment.integerType);
		StdEnvironment.addDecl = declareStdBinaryOp("+", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.integerType);
		StdEnvironment.subtractDecl = declareStdBinaryOp("-", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.integerType);
		StdEnvironment.multiplyDecl = declareStdBinaryOp("*", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.integerType);
		StdEnvironment.divideDecl = declareStdBinaryOp("/", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.integerType);
		StdEnvironment.moduloDecl = declareStdBinaryOp("//", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.integerType);
		StdEnvironment.lessDecl = declareStdBinaryOp("<", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.booleanType);
		StdEnvironment.notgreaterDecl = declareStdBinaryOp("<=", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.booleanType);
		StdEnvironment.greaterDecl = declareStdBinaryOp(">", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.booleanType);
		StdEnvironment.notlessDecl = declareStdBinaryOp(">=", StdEnvironment.integerType, StdEnvironment.integerType,
				StdEnvironment.booleanType);

		StdEnvironment.charDecl = declareStdType("Char", StdEnvironment.charType);
		StdEnvironment.chrDecl = declareStdFunc("chr",
				new SingleFormalParameterSequence(
						new ConstFormalParameter(dummyI, StdEnvironment.integerType, dummyPos), dummyPos),
				StdEnvironment.charType);
		StdEnvironment.ordDecl = declareStdFunc("ord",
				new SingleFormalParameterSequence(new ConstFormalParameter(dummyI, StdEnvironment.charType, dummyPos),
						dummyPos),
				StdEnvironment.integerType);
		StdEnvironment.eofDecl = declareStdFunc("eof", new EmptyFormalParameterSequence(dummyPos),
				StdEnvironment.booleanType);
		StdEnvironment.eolDecl = declareStdFunc("eol", new EmptyFormalParameterSequence(dummyPos),
				StdEnvironment.booleanType);
		StdEnvironment.getDecl = declareStdProc("get", new SingleFormalParameterSequence(
				new VarFormalParameter(dummyI, StdEnvironment.charType, dummyPos), dummyPos));
		StdEnvironment.putDecl = declareStdProc("put", new SingleFormalParameterSequence(
				new ConstFormalParameter(dummyI, StdEnvironment.charType, dummyPos), dummyPos));
		StdEnvironment.getintDecl = declareStdProc("getint", new SingleFormalParameterSequence(
				new VarFormalParameter(dummyI, StdEnvironment.integerType, dummyPos), dummyPos));
		StdEnvironment.putintDecl = declareStdProc("putint", new SingleFormalParameterSequence(
				new ConstFormalParameter(dummyI, StdEnvironment.integerType, dummyPos), dummyPos));
		StdEnvironment.geteolDecl = declareStdProc("geteol", new EmptyFormalParameterSequence(dummyPos));
		StdEnvironment.puteolDecl = declareStdProc("puteol", new EmptyFormalParameterSequence(dummyPos));
		StdEnvironment.equalDecl = declareStdBinaryOp("=", StdEnvironment.anyType, StdEnvironment.anyType,
				StdEnvironment.booleanType);
		StdEnvironment.unequalDecl = declareStdBinaryOp("\\=", StdEnvironment.anyType, StdEnvironment.anyType,
				StdEnvironment.booleanType);

	}
}
