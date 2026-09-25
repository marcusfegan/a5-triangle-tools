/*
 * @(#)LayoutVisitor.java                       
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

package triangle.treeDrawer;

import java.awt.FontMetrics;

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
import triangle.abstractSyntaxTrees.declarations.FuncDeclaration;
import triangle.abstractSyntaxTrees.declarations.ProcDeclaration;
import triangle.abstractSyntaxTrees.declarations.SequentialDeclaration;
import triangle.abstractSyntaxTrees.declarations.UnaryOperatorDeclaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
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
import triangle.abstractSyntaxTrees.formals.FuncFormalParameter;
import triangle.abstractSyntaxTrees.formals.MultipleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.ProcFormalParameter;
import triangle.abstractSyntaxTrees.formals.SingleFormalParameterSequence;
import triangle.abstractSyntaxTrees.formals.VarFormalParameter;
import triangle.abstractSyntaxTrees.terminals.CharacterLiteral;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.terminals.IntegerLiteral;
import triangle.abstractSyntaxTrees.terminals.Operator;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
import triangle.abstractSyntaxTrees.types.ArrayTypeDenoter;
import triangle.abstractSyntaxTrees.types.BoolTypeDenoter;
import triangle.abstractSyntaxTrees.types.CharTypeDenoter;
import triangle.abstractSyntaxTrees.types.ErrorTypeDenoter;
import triangle.abstractSyntaxTrees.types.IntTypeDenoter;
import triangle.abstractSyntaxTrees.types.MultipleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.RecordTypeDenoter;
import triangle.abstractSyntaxTrees.types.SimpleTypeDenoter;
import triangle.abstractSyntaxTrees.types.SingleFieldTypeDenoter;
import triangle.abstractSyntaxTrees.types.TypeDeclaration;
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

public class LayoutVisitor implements ActualParameterVisitor<Void, DrawingTree>,
		ActualParameterSequenceVisitor<Void, DrawingTree>, ArrayAggregateVisitor<Void, DrawingTree>,
		CommandVisitor<Void, DrawingTree>, DeclarationVisitor<Void, DrawingTree>, ExpressionVisitor<Void, DrawingTree>,
		FormalParameterSequenceVisitor<Void, DrawingTree>, IdentifierVisitor<Void, DrawingTree>,
		LiteralVisitor<Void, DrawingTree>, OperatorVisitor<Void, DrawingTree>, ProgramVisitor<Void, DrawingTree>,
		RecordAggregateVisitor<Void, DrawingTree>, TypeDenoterVisitor<Void, DrawingTree>,
		VnameVisitor<Void, DrawingTree> {

	private final int BORDER = 5;
	private final int PARENT_SEP = 30;

	private FontMetrics fontMetrics;

	public LayoutVisitor(FontMetrics fontMetrics) {
		this.fontMetrics = fontMetrics;
	}

	// Commands
	@Override
	public DrawingTree visitAssignCommand(AssignCommand ast, Void obj) {
		var d1 = ast.V.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("AssignCom.", d1, d2);
	}

	@Override
	public DrawingTree visitCallCommand(CallCommand ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.APS.visit(this);
		return layoutBinary("CallCom.", d1, d2);
	}

	@Override
	public DrawingTree visitEmptyCommand(EmptyCommand ast, Void obj) {
		return layoutNullary("EmptyCom.");
	}

	@Override
	public DrawingTree visitIfCommand(IfCommand ast, Void obj) {
		var d1 = ast.E.visit(this);
		var d2 = ast.C1.visit(this);
		var d3 = ast.C2.visit(this);
		return layoutTernary("IfCom.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitLetCommand(LetCommand ast, Void obj) {
		var d1 = ast.D.visit(this);
		var d2 = ast.C.visit(this);
		return layoutBinary("LetCom.", d1, d2);
	}

	@Override
	public DrawingTree visitSequentialCommand(SequentialCommand ast, Void obj) {
		var d1 = ast.C1.visit(this);
		var d2 = ast.C2.visit(this);
		return layoutBinary("Seq.Com.", d1, d2);
	}

	@Override
	public DrawingTree visitWhileCommand(WhileCommand ast, Void obj) {
		var d1 = ast.E.visit(this);
		var d2 = ast.C.visit(this);
		return layoutBinary("WhileCom.", d1, d2);
	}

	@Override
	public DrawingTree visitRepeatCommand(RepeatCommand ast, Void unused) {
		return null;
	}

	// Expressions
	@Override
	public DrawingTree visitArrayExpression(ArrayExpression ast, Void obj) {
		var d1 = ast.AA.visit(this);
		return layoutUnary("ArrayExpr.", d1);
	}

	@Override
	public DrawingTree visitBinaryExpression(BinaryExpression ast, Void obj) {
		var d1 = ast.E1.visit(this);
		var d2 = ast.O.visit(this);
		var d3 = ast.E2.visit(this);
		return layoutTernary("Bin.Expr.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitCallExpression(CallExpression ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.APS.visit(this);
		return layoutBinary("CallExpr.", d1, d2);
	}

	@Override
	public DrawingTree visitCharacterExpression(CharacterExpression ast, Void obj) {
		var d1 = ast.CL.visit(this);
		return layoutUnary("Char.Expr.", d1);
	}

	@Override
	public DrawingTree visitEmptyExpression(EmptyExpression ast, Void obj) {
		return layoutNullary("EmptyExpr.");
	}

	@Override
	public DrawingTree visitIfExpression(IfExpression ast, Void obj) {
		var d1 = ast.E1.visit(this);
		var d2 = ast.E2.visit(this);
		var d3 = ast.E3.visit(this);
		return layoutTernary("IfExpr.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitIntegerExpression(IntegerExpression ast, Void obj) {
		var d1 = ast.IL.visit(this);
		return layoutUnary("Int.Expr.", d1);
	}

	@Override
	public DrawingTree visitLetExpression(LetExpression ast, Void obj) {
		var d1 = ast.D.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("LetExpr.", d1, d2);
	}

	@Override
	public DrawingTree visitRecordExpression(RecordExpression ast, Void obj) {
		var d1 = ast.RA.visit(this);
		return layoutUnary("Rec.Expr.", d1);
	}

	@Override
	public DrawingTree visitUnaryExpression(UnaryExpression ast, Void obj) {
		var d1 = ast.O.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("UnaryExpr.", d1, d2);
	}

	@Override
	public DrawingTree visitVnameExpression(VnameExpression ast, Void obj) {
		var d1 = ast.V.visit(this);
		return layoutUnary("VnameExpr.", d1);
	}

	// Declarations
	@Override
	public DrawingTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void obj) {
		var d1 = ast.O.visit(this);
		var d2 = ast.ARG1.visit(this);
		var d3 = ast.ARG2.visit(this);
		var d4 = ast.RES.visit(this);
		return layoutQuaternary("Bin.Op.Decl.", d1, d2, d3, d4);
	}

	@Override
	public DrawingTree visitConstDeclaration(ConstDeclaration ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("ConstDecl.", d1, d2);
	}

	@Override
	public DrawingTree visitFuncDeclaration(FuncDeclaration ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.FPS.visit(this);
		var d3 = ast.T.visit(this);
		var d4 = ast.E.visit(this);
		return layoutQuaternary("FuncDecl.", d1, d2, d3, d4);
	}

	@Override
	public DrawingTree visitProcDeclaration(ProcDeclaration ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.FPS.visit(this);
		var d3 = ast.C.visit(this);
		return layoutTernary("ProcDecl.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitSequentialDeclaration(SequentialDeclaration ast, Void obj) {
		var d1 = ast.D1.visit(this);
		var d2 = ast.D2.visit(this);
		return layoutBinary("Seq.Decl.", d1, d2);
	}

	@Override
	public DrawingTree visitTypeDeclaration(TypeDeclaration ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("TypeDecl.", d1, d2);
	}

	@Override
	public DrawingTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void obj) {
		var d1 = ast.O.visit(this);
		var d2 = ast.ARG.visit(this);
		var d3 = ast.RES.visit(this);
		return layoutTernary("UnaryOp.Decl.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitVarDeclaration(VarDeclaration ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("VarDecl.", d1, d2);
	}

	// Array Aggregates
	@Override
	public DrawingTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void obj) {
		var d1 = ast.E.visit(this);
		var d2 = ast.AA.visit(this);
		return layoutBinary("Mult.ArrayAgg.", d1, d2);
	}

	@Override
	public DrawingTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void obj) {
		var d1 = ast.E.visit(this);
		return layoutUnary("Sing.ArrayAgg.", d1);
	}

	// Record Aggregates
	@Override
	public DrawingTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.E.visit(this);
		var d3 = ast.RA.visit(this);
		return layoutTernary("Mult.Rec.Agg.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("Sing.Rec.Agg.", d1, d2);
	}

	// Formal Parameters
	@Override
	public DrawingTree visitConstFormalParameter(ConstFormalParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("ConstF.P.", d1, d2);
	}

	@Override
	public DrawingTree visitFuncFormalParameter(FuncFormalParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.FPS.visit(this);
		var d3 = ast.T.visit(this);
		return layoutTernary("FuncF.P.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitProcFormalParameter(ProcFormalParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.FPS.visit(this);
		return layoutBinary("ProcF.P.", d1, d2);
	}

	@Override
	public DrawingTree visitVarFormalParameter(VarFormalParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("VarF.P.", d1, d2);
	}

	@Override
	public DrawingTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void obj) {
		return layoutNullary("EmptyF.P.S.");
	}

	@Override
	public DrawingTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void obj) {
		var d1 = ast.FP.visit(this);
		var d2 = ast.FPS.visit(this);
		return layoutBinary("Mult.F.P.S.", d1, d2);
	}

	@Override
	public DrawingTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void obj) {
		var d1 = ast.FP.visit(this);
		return layoutUnary("Sing.F.P.S.", d1);
	}

	// Actual Parameters
	@Override
	public DrawingTree visitConstActualParameter(ConstActualParameter ast, Void obj) {
		var d1 = ast.E.visit(this);
		return layoutUnary("ConstA.P.", d1);
	}

	@Override
	public DrawingTree visitFuncActualParameter(FuncActualParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		return layoutUnary("FuncA.P.", d1);
	}

	@Override
	public DrawingTree visitProcActualParameter(ProcActualParameter ast, Void obj) {
		var d1 = ast.I.visit(this);
		return layoutUnary("ProcA.P.", d1);
	}

	@Override
	public DrawingTree visitVarActualParameter(VarActualParameter ast, Void obj) {
		var d1 = ast.V.visit(this);
		return layoutUnary("VarA.P.", d1);
	}

	@Override
	public DrawingTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void obj) {
		return layoutNullary("EmptyA.P.S.");
	}

	@Override
	public DrawingTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void obj) {
		var d1 = ast.AP.visit(this);
		var d2 = ast.APS.visit(this);
		return layoutBinary("Mult.A.P.S.", d1, d2);
	}

	@Override
	public DrawingTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void obj) {
		var d1 = ast.AP.visit(this);
		return layoutUnary("Sing.A.P.S.", d1);
	}

	// Type Denoters
	@Override
	public DrawingTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void obj) {
		return layoutNullary("any");
	}

	@Override
	public DrawingTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void obj) {
		var d1 = ast.IL.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("ArrayTypeD.", d1, d2);
	}

	@Override
	public DrawingTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void obj) {
		return layoutNullary("bool");
	}

	@Override
	public DrawingTree visitCharTypeDenoter(CharTypeDenoter ast, Void obj) {
		return layoutNullary("char");
	}

	@Override
	public DrawingTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void obj) {
		return layoutNullary("error");
	}

	@Override
	public DrawingTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void obj) {
		var d1 = ast.I.visit(this);
		return layoutUnary("Sim.TypeD.", d1);
	}

	@Override
	public DrawingTree visitIntTypeDenoter(IntTypeDenoter ast, Void obj) {
		return layoutNullary("int");
	}

	@Override
	public DrawingTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void obj) {
		var d1 = ast.FT.visit(this);
		return layoutUnary("Rec.TypeD.", d1);
	}

	@Override
	public DrawingTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		var d3 = ast.FT.visit(this);
		return layoutTernary("Mult.F.TypeD.", d1, d2, d3);
	}

	@Override
	public DrawingTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.T.visit(this);
		return layoutBinary("Sing.F.TypeD.", d1, d2);
	}

	// Literals, Identifiers and Operators
	@Override
	public DrawingTree visitCharacterLiteral(CharacterLiteral ast, Void obj) {
		return layoutNullary(ast.spelling);
	}

	@Override
	public DrawingTree visitIdentifier(Identifier ast, Void obj) {
		return layoutNullary(ast.spelling);
	}

	@Override
	public DrawingTree visitIntegerLiteral(IntegerLiteral ast, Void obj) {
		return layoutNullary(ast.spelling);
	}

	@Override
	public DrawingTree visitOperator(Operator ast, Void obj) {
		return layoutNullary(ast.spelling);
	}

	// Value-or-variable names
	@Override
	public DrawingTree visitDotVname(DotVname ast, Void obj) {
		var d1 = ast.I.visit(this);
		var d2 = ast.V.visit(this);
		return layoutBinary("DotVname", d1, d2);
	}

	@Override
	public DrawingTree visitSimpleVname(SimpleVname ast, Void obj) {
		var d1 = ast.I.visit(this);
		return layoutUnary("Sim.Vname", d1);
	}

	@Override
	public DrawingTree visitSubscriptVname(SubscriptVname ast, Void obj) {
		var d1 = ast.V.visit(this);
		var d2 = ast.E.visit(this);
		return layoutBinary("Sub.Vname", d1, d2);
	}

	// Programs
	@Override
	public DrawingTree visitProgram(Program ast, Void obj) {
		var d1 = ast.C.visit(this);
		return layoutUnary("Program", d1);
	}

	private DrawingTree layoutCaption(String name) {
		var w = fontMetrics.stringWidth(name) + 4;
		var h = fontMetrics.getHeight() + 4;
		return new DrawingTree(name, w, h);
	}

	private DrawingTree layoutNullary(String name) {
		var dt = layoutCaption(name);
		dt.contour.upper_tail = new Polyline(0, dt.height + 2 * BORDER, null);
		dt.contour.upper_head = dt.contour.upper_tail;
		dt.contour.lower_tail = new Polyline(-dt.width - 2 * BORDER, 0, null);
		dt.contour.lower_head = new Polyline(0, dt.height + 2 * BORDER, dt.contour.lower_tail);
		return dt;
	}

	private DrawingTree layoutUnary(String name, DrawingTree d1) {
		var dt = layoutCaption(name);
		dt.setChildren(new DrawingTree[] { d1 });
		attachParent(dt, join(dt));
		return dt;
	}

	private DrawingTree layoutBinary(String name, DrawingTree d1, DrawingTree d2) {
		var dt = layoutCaption(name);
		dt.setChildren(new DrawingTree[] { d1, d2 });
		attachParent(dt, join(dt));
		return dt;
	}

	private DrawingTree layoutTernary(String name, DrawingTree d1, DrawingTree d2, DrawingTree d3) {
		var dt = layoutCaption(name);
		dt.setChildren(new DrawingTree[] { d1, d2, d3 });
		attachParent(dt, join(dt));
		return dt;
	}

	private DrawingTree layoutQuaternary(String name, DrawingTree d1, DrawingTree d2, DrawingTree d3, DrawingTree d4) {
		var dt = layoutCaption(name);
		dt.setChildren(new DrawingTree[] { d1, d2, d3, d4 });
		attachParent(dt, join(dt));
		return dt;
	}

	private void attachParent(DrawingTree dt, int w) {
		var y = PARENT_SEP;
		var x2 = (w - dt.width) / 2 - BORDER;
		var x1 = x2 + dt.width + 2 * BORDER - w;

		dt.children[0].offset.y = y + dt.height;
		dt.children[0].offset.x = x1;
		dt.contour.upper_head = new Polyline(0, dt.height, new Polyline(x1, y, dt.contour.upper_head));
		dt.contour.lower_head = new Polyline(0, dt.height, new Polyline(x2, y, dt.contour.lower_head));
	}

	private int join(DrawingTree dt) {

		dt.contour = dt.children[0].contour;
		var sum = dt.children[0].width + 2 * BORDER;
		var w = sum;

		for (var i = 1; i < dt.children.length; i++) {
			var d = merge(dt.contour, dt.children[i].contour);
			dt.children[i].offset.x = d + w;
			dt.children[i].offset.y = 0;
			w = dt.children[i].width + 2 * BORDER;
			sum += d + w;
		}
		return sum;
	}

	private int merge(Polygon c1, Polygon c2) {
		int x = 0, y = 0, total = 0;
		var upper = c1.lower_head;
		var lower = c2.upper_head;

		while (lower != null && upper != null) {
			var d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
			x += d;
			total += d;

			if (y + lower.dy <= upper.dy) {
				x += lower.dx;
				y += lower.dy;
				lower = lower.link;
			} else {
				x -= upper.dx;
				y -= upper.dy;
				upper = upper.link;
			}
		}

		if (lower != null) {
			var b = bridge(c1.upper_tail, 0, 0, lower, x, y);
			c1.upper_tail = (b.link != null) ? c2.upper_tail : b;
			c1.lower_tail = c2.lower_tail;
		} else {
			var b = bridge(c2.lower_tail, x, y, upper, 0, 0);
			if (b.link == null) {
				c1.lower_tail = b;
			}
		}

		c1.lower_head = c2.lower_head;

		return total;
	}

	private int offset(int p1, int p2, int a1, int a2, int b1, int b2) {

		if (b2 <= p2 || p2 + a2 <= 0) {
			return 0;
		}

		var t = b2 * a1 - a2 * b1;
		if (t > 0) {
			if (p2 < 0) {
				var s = p2 * a1;
				return Math.max(0, s / a2 - p1);
			} else if (p2 > 0) {
				var s = p2 * b1;
				return Math.max(0, s / b2 - p1);
			} else {
				return Math.max(0, -p1);
			}
		} else if (b2 < p2 + a2) {
			var s = (b2 - p2) * a1;
			return Math.max(0, b1 - (p1 + s / a2));
		} else if (b2 > p2 + a2) {
			var s = (a2 + p2) * b1;
			return Math.max(0, s / b2 - (p1 + a1));
		} else {
			return Math.max(0, b1 - (p1 + a1));
		}
	}

	private Polyline bridge(Polyline line1, int x1, int y1, Polyline line2, int x2, int y2) {

		int dx;
		var dy = y2 + line2.dy - y1;
		if (line2.dy == 0) {
			dx = line2.dx;
		} else {
			var s = dy * line2.dx;
			dx = s / line2.dy;
		}

		var r = new Polyline(dx, dy, line2.link);
		line1.link = new Polyline(x2 + line2.dx - dx - x1, 0, r);

		return r;
	}

}