/*
 * @(#)Encoder.java                       
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

package triangle.codeGenerator;

import triangle.ErrorReporter;
import triangle.StdEnvironment;
import triangle.abstractMachine.Machine;
import triangle.abstractMachine.OpCode;
import triangle.abstractMachine.Primitive;
import triangle.abstractMachine.Register;
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
import triangle.abstractSyntaxTrees.declarations.Declaration;
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
import triangle.abstractSyntaxTrees.vnames.Vname;
import triangle.codeGenerator.entities.AddressableEntity;
import triangle.codeGenerator.entities.EqualityRoutine;
import triangle.codeGenerator.entities.FetchableEntity;
import triangle.codeGenerator.entities.Field;
import triangle.codeGenerator.entities.KnownAddress;
import triangle.codeGenerator.entities.KnownRoutine;
import triangle.codeGenerator.entities.KnownValue;
import triangle.codeGenerator.entities.PrimitiveRoutine;
import triangle.codeGenerator.entities.RoutineEntity;
import triangle.codeGenerator.entities.RuntimeEntity;
import triangle.codeGenerator.entities.TypeRepresentation;
import triangle.codeGenerator.entities.UnknownAddress;
import triangle.codeGenerator.entities.UnknownRoutine;
import triangle.codeGenerator.entities.UnknownValue;

public final class Encoder implements ActualParameterVisitor<Frame, Integer>,
		ActualParameterSequenceVisitor<Frame, Integer>, ArrayAggregateVisitor<Frame, Integer>,
		CommandVisitor<Frame, Void>, DeclarationVisitor<Frame, Integer>, ExpressionVisitor<Frame, Integer>,
		FormalParameterSequenceVisitor<Frame, Integer>, IdentifierVisitor<Frame, Void>, LiteralVisitor<Void, Void>,
		OperatorVisitor<Frame, Void>, ProgramVisitor<Frame, Void>, RecordAggregateVisitor<Frame, Integer>,
		TypeDenoterVisitor<Frame, Integer>, VnameVisitor<Frame, RuntimeEntity> {

	// Commands
	@Override
	public Void visitAssignCommand(AssignCommand ast, Frame frame) {
		var valSize = ast.E.visit(this, frame);
		encodeStore(ast.V, frame.expand(valSize), valSize);
		return null;
	}

	@Override
	public Void visitCallCommand(CallCommand ast, Frame frame) {
		var argsSize = ast.APS.visit(this, frame);
		ast.I.visit(this, frame.replace(argsSize));
		return null;
	}

	@Override
	public Void visitEmptyCommand(EmptyCommand ast, Frame frame) {
		return null;
	}

	@Override
	public Void visitIfCommand(IfCommand ast, Frame frame) {
		ast.E.visit(this, frame);
		var jumpifAddr = emitter.emit(OpCode.JUMPIF, Machine.falseRep, Register.CB, 0);
		ast.C1.visit(this, frame);
		var jumpAddr = emitter.emit(OpCode.JUMP, 0, Register.CB, 0);
		emitter.patch(jumpifAddr);
		ast.C2.visit(this, frame);
		emitter.patch(jumpAddr);
		return null;
	}

	@Override
	public Void visitLetCommand(LetCommand ast, Frame frame) {
		var extraSize = ast.D.visit(this, frame);
		ast.C.visit(this, frame.expand(extraSize));
		if (extraSize > 0) {
			emitter.emit(OpCode.POP, extraSize);
		}
		return null;
	}

	@Override
	public Void visitSequentialCommand(SequentialCommand ast, Frame frame) {
		ast.C1.visit(this, frame);
		ast.C2.visit(this, frame);
		return null;
	}

	@Override
	public Void visitWhileCommand(WhileCommand ast, Frame frame) {
		var jumpAddr = emitter.emit(OpCode.JUMP, 0, Register.CB, 0);
		var loopAddr = emitter.getNextInstrAddr();
		ast.C.visit(this, frame);
		emitter.patch(jumpAddr);
		ast.E.visit(this, frame);
		emitter.emit(OpCode.JUMPIF, Machine.trueRep, Register.CB, loopAddr);
		return null;
	}

	@Override
	public Void visitRepeatCommand(RepeatCommand ast, Frame frame) {
		return null;
	}

	// Expressions
	@Override
	public Integer visitArrayExpression(ArrayExpression ast, Frame frame) {
		ast.type.visit(this, frame);
		return ast.AA.visit(this, frame);
	}

	@Override
	public Integer visitBinaryExpression(BinaryExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		var valSize1 = ast.E1.visit(this, frame);
		var frame1 = frame.expand(valSize1);
		var valSize2 = ast.E2.visit(this, frame1);
		var frame2 = frame.replace(valSize1 + valSize2);
		ast.O.visit(this, frame2);
		return valSize;
	}

	@Override
	public Integer visitCallExpression(CallExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		var argsSize = ast.APS.visit(this, frame);
		ast.I.visit(this, frame.replace(argsSize));
		return valSize;
	}

	@Override
	public Integer visitCharacterExpression(CharacterExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		emitter.emit(OpCode.LOADL, ast.CL.getValue());
		return valSize;
	}

	@Override
	public Integer visitEmptyExpression(EmptyExpression ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitIfExpression(IfExpression ast, Frame frame) {
		ast.type.visit(this);
		ast.E1.visit(this, frame);
		var jumpifAddr = emitter.emit(OpCode.JUMPIF, Machine.falseRep, Register.CB, 0);
		var valSize = ast.E2.visit(this, frame);
		var jumpAddr = emitter.emit(OpCode.JUMP, 0, Register.CB, 0);
		emitter.patch(jumpifAddr);
		valSize = ast.E3.visit(this, frame);
		emitter.patch(jumpAddr);
		return valSize;
	}

	@Override
	public Integer visitIntegerExpression(IntegerExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		emitter.emit(OpCode.LOADL, ast.IL.getValue());
		return valSize;
	}

	@Override
	public Integer visitLetExpression(LetExpression ast, Frame frame) {
		ast.type.visit(this);
		var extraSize = ast.D.visit(this, frame);
		var frame1 = frame.expand(extraSize);
		var valSize = ast.E.visit(this, frame1);
		if (extraSize > 0) {
			emitter.emit(OpCode.POP, valSize, extraSize);
		}
		return valSize;
	}

	@Override
	public Integer visitRecordExpression(RecordExpression ast, Frame frame) {
		ast.type.visit(this);
		return ast.RA.visit(this, frame);
	}

	@Override
	public Integer visitUnaryExpression(UnaryExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		ast.E.visit(this, frame);
		ast.O.visit(this, frame.replace(valSize));
		return valSize;
	}

	@Override
	public Integer visitVnameExpression(VnameExpression ast, Frame frame) {
		var valSize = ast.type.visit(this);
		encodeFetch(ast.V, frame, valSize);
		return valSize;
	}

	// Declarations
	@Override
	public Integer visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitConstDeclaration(ConstDeclaration ast, Frame frame) {
		var extraSize = 0;
		if (ast.E.isLiteral()) {
			ast.entity = new KnownValue(ast.E.type.getSize(), ast.E.getValue());
		} else {
			var valSize = ast.E.visit(this, frame);
			ast.entity = new UnknownValue(valSize, frame);
			extraSize = valSize;
		}
		writeTableDetails(ast);
		return extraSize;
	}

	@Override
	public Integer visitFuncDeclaration(FuncDeclaration ast, Frame frame) {
		var argsSize = 0;
		var valSize = 0;

		var jumpAddr = emitter.emit(OpCode.JUMP, 0, Register.CB, 0);
		ast.entity = new KnownRoutine(Machine.closureSize, frame.getLevel(), emitter.getNextInstrAddr());
		writeTableDetails(ast);
		if (frame.getLevel() == Machine.maxRoutineLevel) {
			reporter.reportRestriction("can't nest routines more than 7 deep");
		} else {
			var frame1 = frame.push(0);
			argsSize = ast.FPS.visit(this, frame1);
			var frame2 = frame.push(Machine.linkDataSize);
			valSize = ast.E.visit(this, frame2);
		}
		emitter.emit(OpCode.RETURN, valSize, argsSize);
		emitter.patch(jumpAddr);
		return 0;
	}

	@Override
	public Integer visitProcDeclaration(ProcDeclaration ast, Frame frame) {
		var argsSize = 0;
		var jumpAddr = emitter.emit(OpCode.JUMP, 0, Register.CB, 0);
		ast.entity = new KnownRoutine(Machine.closureSize, frame.getLevel(), emitter.getNextInstrAddr());
		writeTableDetails(ast);
		if (frame.getLevel() == Machine.maxRoutineLevel) {
			reporter.reportRestriction("can't nest routines so deeply");
		} else {
			var frame1 = frame.push(0);
			argsSize = ast.FPS.visit(this, frame1);
			var frame2 = frame.push(Machine.linkDataSize);
			ast.C.visit(this, frame2);
		}
		emitter.emit(OpCode.RETURN, argsSize);
		emitter.patch(jumpAddr);
		return 0;
	}

	@Override
	public Integer visitSequentialDeclaration(SequentialDeclaration ast, Frame frame) {
		var extraSize1 = ast.D1.visit(this, frame);
		var frame1 = frame.expand(extraSize1);
		var extraSize2 = ast.D2.visit(this, frame1);
		return extraSize1 + extraSize2;
	}

	@Override
	public Integer visitTypeDeclaration(TypeDeclaration ast, Frame frame) {
		// just to ensure the type's representation is decided
		ast.T.visit(this);
		return 0;
	}

	@Override
	public Integer visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitVarDeclaration(VarDeclaration ast, Frame frame) {
		var extraSize = ast.T.visit(this);
		emitter.emit(OpCode.PUSH, extraSize);
		ast.entity = new KnownAddress(Machine.addressSize, frame);
		writeTableDetails(ast);
		return extraSize;
	}

	// Array Aggregates
	@Override
	public Integer visitMultipleArrayAggregate(MultipleArrayAggregate ast, Frame frame) {
		var elemSize = ast.E.visit(this, frame);
		var frame1 = frame.expand(elemSize);
		var arraySize = ast.AA.visit(this, frame1);
		return elemSize + arraySize;
	}

	@Override
	public Integer visitSingleArrayAggregate(SingleArrayAggregate ast, Frame frame) {
		return ast.E.visit(this, frame);
	}

	// Record Aggregates
	@Override
	public Integer visitMultipleRecordAggregate(MultipleRecordAggregate ast, Frame frame) {
		var fieldSize = ast.E.visit(this, frame);
		var frame1 = frame.expand(fieldSize);
		var recordSize = ast.RA.visit(this, frame1);
		return fieldSize + recordSize;
	}

	@Override
	public Integer visitSingleRecordAggregate(SingleRecordAggregate ast, Frame frame) {
		return ast.E.visit(this, frame);
	}

	// Formal Parameters
	@Override
	public Integer visitConstFormalParameter(ConstFormalParameter ast, Frame frame) {
		var valSize = ast.T.visit(this);
		ast.entity = new UnknownValue(valSize, frame.getLevel(), -frame.getSize() - valSize);
		writeTableDetails(ast);
		return valSize;
	}

	@Override
	public Integer visitFuncFormalParameter(FuncFormalParameter ast, Frame frame) {
		var argsSize = Machine.closureSize;
		ast.entity = new UnknownRoutine(Machine.closureSize, frame.getLevel(), -frame.getSize() - argsSize);
		writeTableDetails(ast);
		return argsSize;
	}

	@Override
	public Integer visitProcFormalParameter(ProcFormalParameter ast, Frame frame) {
		var argsSize = Machine.closureSize;
		ast.entity = new UnknownRoutine(Machine.closureSize, frame.getLevel(), -frame.getSize() - argsSize);
		writeTableDetails(ast);
		return argsSize;
	}

	@Override
	public Integer visitVarFormalParameter(VarFormalParameter ast, Frame frame) {
		ast.T.visit(this);
		ast.entity = new UnknownAddress(Machine.addressSize, frame.getLevel(), -frame.getSize() - Machine.addressSize);
		writeTableDetails(ast);
		return Machine.addressSize;
	}

	@Override
	public Integer visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Frame frame) {
		var argsSize1 = ast.FPS.visit(this, frame);
		var frame1 = frame.expand(argsSize1);
		var argsSize2 = ast.FP.visit(this, frame1);
		return argsSize1 + argsSize2;
	}

	@Override
	public Integer visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Frame frame) {
		return ast.FP.visit(this, frame);
	}

	// Actual Parameters
	@Override
	public Integer visitConstActualParameter(ConstActualParameter ast, Frame frame) {
		return ast.E.visit(this, frame);
	}

	@Override
	public Integer visitFuncActualParameter(FuncActualParameter ast, Frame frame) {
		var routineEntity = (RoutineEntity) ast.I.decl.entity;
		routineEntity.encodeFetch(emitter, frame);
		return Machine.closureSize;
	}

	@Override
	public Integer visitProcActualParameter(ProcActualParameter ast, Frame frame) {
		var routineEntity = (RoutineEntity) ast.I.decl.entity;
		routineEntity.encodeFetch(emitter, frame);
		return Machine.closureSize;
	}

	@Override
	public Integer visitVarActualParameter(VarActualParameter ast, Frame frame) {
		encodeFetchAddress(ast.V, frame);
		return Machine.addressSize;
	}

	@Override
	public Integer visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Frame frame) {
		var argsSize1 = ast.AP.visit(this, frame);
		var frame1 = frame.expand(argsSize1);
		var argsSize2 = ast.APS.visit(this, frame1);
		return argsSize1 + argsSize2;
	}

	@Override
	public Integer visitSingleActualParameterSequence(SingleActualParameterSequence ast, Frame frame) {
		return ast.AP.visit(this, frame);
	}

	// Type Denoters
	@Override
	public Integer visitAnyTypeDenoter(AnyTypeDenoter ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitArrayTypeDenoter(ArrayTypeDenoter ast, Frame frame) {
		int typeSize;
		if (ast.entity == null) {
			var elemSize = ast.T.visit(this);
			typeSize = ast.IL.getValue() * elemSize;
			ast.entity = new TypeRepresentation(typeSize);
			writeTableDetails(ast);
		} else {
			typeSize = ast.entity.getSize();
		}
		return typeSize;
	}

	@Override
	public Integer visitBoolTypeDenoter(BoolTypeDenoter ast, Frame frame) {
		if (ast.entity == null) {
			ast.entity = new TypeRepresentation(Machine.booleanSize);
			writeTableDetails(ast);
		}
		return Machine.booleanSize;
	}

	@Override
	public Integer visitCharTypeDenoter(CharTypeDenoter ast, Frame frame) {
		if (ast.entity == null) {
			ast.entity = new TypeRepresentation(Machine.characterSize);
			writeTableDetails(ast);
		}
		return Machine.characterSize;
	}

	@Override
	public Integer visitErrorTypeDenoter(ErrorTypeDenoter ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitSimpleTypeDenoter(SimpleTypeDenoter ast, Frame frame) {
		return 0;
	}

	@Override
	public Integer visitIntTypeDenoter(IntTypeDenoter ast, Frame frame) {
		if (ast.entity == null) {
			ast.entity = new TypeRepresentation(Machine.integerSize);
			writeTableDetails(ast);
		}
		return Machine.integerSize;
	}

	@Override
	public Integer visitRecordTypeDenoter(RecordTypeDenoter ast, Frame frame) {
		int typeSize;
		if (ast.entity == null) {
			typeSize = ast.FT.visit(this, frame);
			ast.entity = new TypeRepresentation(typeSize);
			writeTableDetails(ast);
		} else {
			typeSize = ast.entity.getSize();
		}
		return typeSize;
	}

	@Override
	public Integer visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Frame frame) {
		if (frame == null) { // in this case, we're just using the frame to wrap up the size
			frame = Frame.Initial;
		}
		
		var offset = frame.getSize();
		int fieldSize;
		if (ast.entity == null) {
			fieldSize = ast.T.visit(this);
			ast.entity = new Field(fieldSize, offset);
			writeTableDetails(ast);
		} else {
			fieldSize = ast.entity.getSize();
		}

		var offset1 = frame.replace(offset + fieldSize);
		var recSize = ast.FT.visit(this, offset1);
		return fieldSize + recSize;
	}

	@Override
	public Integer visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Frame frame) {
		var offset = frame.getSize();
		int fieldSize;
		if (ast.entity == null) {
			fieldSize = ast.T.visit(this);
			ast.entity = new Field(fieldSize, offset);
			writeTableDetails(ast);
		} else {
			fieldSize = ast.entity.getSize();
		}

		return fieldSize;
	}

	// Literals, Identifiers and Operators
	@Override
	public Void visitCharacterLiteral(CharacterLiteral ast, Void arg) {
		return null;
	}

	@Override
	public Void visitIdentifier(Identifier ast, Frame frame) {
		var routineEntity = (RoutineEntity) ast.decl.entity;
		routineEntity.encodeCall(emitter, frame);
		return null;
	}

	@Override
	public Void visitIntegerLiteral(IntegerLiteral ast, Void arg) {
		return null;
	}

	@Override
	public Void visitOperator(Operator ast, Frame frame) {
		var routineEntity = (RoutineEntity) ast.decl.entity;
		routineEntity.encodeCall(emitter, frame);
		return null;
	}

	// Value-or-variable names
	@Override
	public RuntimeEntity visitDotVname(DotVname ast, Frame frame) {
		var baseObject = ast.V.visit(this, frame);
		ast.offset = ast.V.offset + ((Field) ast.I.decl.entity).getFieldOffset();
		// I.decl points to the appropriate record field
		ast.indexed = ast.V.indexed;
		return baseObject;
	}

	@Override
	public RuntimeEntity visitSimpleVname(SimpleVname ast, Frame frame) {
		ast.offset = 0;
		ast.indexed = false;
		return ast.I.decl.entity;
	}

	@Override
	public RuntimeEntity visitSubscriptVname(SubscriptVname ast, Frame frame) {
		var baseObject = ast.V.visit(this, frame);
		ast.offset = ast.V.offset;
		ast.indexed = ast.V.indexed;
		var elemSize = ast.type.visit(this);
		if (ast.E.isLiteral()) {
			ast.offset = ast.offset + ast.E.getValue() * elemSize;
		} else {
			// v-name is indexed by a proper expression, not a literal
			if (ast.indexed) {
				frame = frame.expand(Machine.integerSize);
			}
			ast.E.visit(this, frame);
			if (elemSize != 1) {
				emitter.emit(OpCode.LOADL, 0, elemSize);
				emitter.emit(OpCode.CALL, Register.PB, Primitive.MULT);
			}
			if (ast.indexed)
				emitter.emit(OpCode.CALL, Register.PB, Primitive.ADD);
			else {
				ast.indexed = true;
			}
		}
		return baseObject;
	}

	// Programs
	@Override
	public Void visitProgram(Program ast, Frame frame) {
		return ast.C.visit(this, frame);
	}

	public Encoder(Emitter emitter, ErrorReporter reporter) {
		this.emitter = emitter;
		this.reporter = reporter;

		elaborateStdEnvironment();
	}

	private Emitter emitter;

	private ErrorReporter reporter;

	// Generates code to run a program.
	// showingTable is true iff entity description details
	// are to be displayed.
	public final void encodeRun(Program program, boolean showingTable) {
		tableDetailsReqd = showingTable;
		// startCodeGeneration();
		program.visit(this, Frame.Initial);
		emitter.emit(OpCode.HALT);
	}

	// Decides run-time representation of a standard constant.
	private final void elaborateStdConst(ConstDeclaration constDeclaration, int value) {

		var typeSize = constDeclaration.E.type.visit(this);
		constDeclaration.entity = new KnownValue(typeSize, value);
		writeTableDetails(constDeclaration);
	}

	// Decides run-time representation of a standard routine.
	private final void elaborateStdPrimRoutine(Declaration routineDeclaration, Primitive primitive) {
		routineDeclaration.entity = new PrimitiveRoutine(Machine.closureSize, primitive);
		writeTableDetails(routineDeclaration);
	}

	private final void elaborateStdEqRoutine(Declaration routineDeclaration, Primitive primitive) {
		routineDeclaration.entity = new EqualityRoutine(Machine.closureSize, primitive);
		writeTableDetails(routineDeclaration);
	}

	/**
	 * in the following we associate the primitive routines (Primitive.*)
	 * and the primitive types (Machine.*) in the abstract machine with 
	 * the relevant std env declarations. 
	 * The primitive routines are listed in Table C3 (P411) of the PLPJ book, 
	 * and Table 3 of the Triangle guide
	 */
	private final void elaborateStdEnvironment() {
		tableDetailsReqd = false;
		elaborateStdConst(StdEnvironment.falseDecl, Machine.falseRep);
		elaborateStdConst(StdEnvironment.trueDecl, Machine.trueRep);
		elaborateStdPrimRoutine(StdEnvironment.notDecl, Primitive.NOT);
		elaborateStdPrimRoutine(StdEnvironment.andDecl, Primitive.AND);
		elaborateStdPrimRoutine(StdEnvironment.orDecl, Primitive.OR);
		elaborateStdConst(StdEnvironment.maxintDecl, Machine.maxintRep);
		elaborateStdPrimRoutine(StdEnvironment.addDecl, Primitive.ADD);
		elaborateStdPrimRoutine(StdEnvironment.subtractDecl, Primitive.SUB);
		elaborateStdPrimRoutine(StdEnvironment.multiplyDecl, Primitive.MULT);
		elaborateStdPrimRoutine(StdEnvironment.divideDecl, Primitive.DIV);
		elaborateStdPrimRoutine(StdEnvironment.moduloDecl, Primitive.MOD);
		elaborateStdPrimRoutine(StdEnvironment.lessDecl, Primitive.LT);
		elaborateStdPrimRoutine(StdEnvironment.notgreaterDecl, Primitive.LE);
		elaborateStdPrimRoutine(StdEnvironment.greaterDecl, Primitive.GT);
		elaborateStdPrimRoutine(StdEnvironment.notlessDecl, Primitive.GE);
		elaborateStdPrimRoutine(StdEnvironment.chrDecl, Primitive.ID);
		elaborateStdPrimRoutine(StdEnvironment.ordDecl, Primitive.ID);
		elaborateStdPrimRoutine(StdEnvironment.eolDecl, Primitive.EOL);
		elaborateStdPrimRoutine(StdEnvironment.eofDecl, Primitive.EOF);
		elaborateStdPrimRoutine(StdEnvironment.getDecl, Primitive.GET);
		elaborateStdPrimRoutine(StdEnvironment.putDecl, Primitive.PUT);
		elaborateStdPrimRoutine(StdEnvironment.getintDecl, Primitive.GETINT);
		elaborateStdPrimRoutine(StdEnvironment.putintDecl, Primitive.PUTINT);
		elaborateStdPrimRoutine(StdEnvironment.geteolDecl, Primitive.GETEOL);
		elaborateStdPrimRoutine(StdEnvironment.puteolDecl, Primitive.PUTEOL);
		elaborateStdEqRoutine(StdEnvironment.equalDecl, Primitive.EQ);
		elaborateStdEqRoutine(StdEnvironment.unequalDecl, Primitive.NE);
	}

	boolean tableDetailsReqd;

	/**
	 * called by all visitor methods here; could be used to insert debugging information
	 */
	public static void writeTableDetails(AbstractSyntaxTree ast) {
	}

	// Generates code to pop the top off the stack
	// and store the value in a named constant or variable
	// frame the local stack frame when
	// the constant or variable is fetched at run-time.
	// valSize is the size of the constant or variable's value.

	private void encodeStore(Vname V, Frame frame, int valSize) {

		var baseObject = (AddressableEntity) V.visit(this, frame);
		// If indexed = true, code will have been generated to load an index value.
		if (valSize > 255) {
			reporter.reportRestriction("can't store values larger than 255 words");
			valSize = 255; // to allow code generation to continue
		}

		baseObject.encodeStore(emitter, frame, valSize, V);
	}

	// Generates code to fetch the value of a named constant or variable
	// and push it on to the stack.
	// currentLevel is the routine level where the vname occurs.
	// frameSize is the anticipated size of the local stack frame when
	// the constant or variable is fetched at run-time.
	// valSize is the size of the constant or variable's value.

	private void encodeFetch(Vname V, Frame frame, int valSize) {

		var baseObject = (FetchableEntity) V.visit(this, frame);
		// If indexed = true, code will have been generated to load an index value.
		if (valSize > 255) {
			reporter.reportRestriction("can't load values larger than 255 words");
			valSize = 255; // to allow code generation to continue
		}

		baseObject.encodeFetch(emitter, frame, valSize, V);
	}

	// Generates code to compute and push the address of a named variable.
	// vname is the program phrase that names this variable.
	// currentLevel is the routine level where the vname occurs.
	// frameSize is the anticipated size of the local stack frame when
	// the variable is addressed at run-time.

	private void encodeFetchAddress(Vname V, Frame frame) {

		var baseObject = (AddressableEntity) V.visit(this, frame);
		baseObject.encodeFetchAddress(emitter, frame, V);
	}
}
