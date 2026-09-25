package triangle.abstractSyntaxTrees.visitors;

import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.CallCommand;
import triangle.abstractSyntaxTrees.commands.EmptyCommand;
import triangle.abstractSyntaxTrees.commands.IfCommand;
import triangle.abstractSyntaxTrees.commands.LetCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.commands.WhileCommand;
import triangle.abstractSyntaxTrees.commands.RepeatCommand;

public interface CommandVisitor<TArg, TResult> {

	TResult visitAssignCommand(AssignCommand ast, TArg arg);

	TResult visitCallCommand(CallCommand ast, TArg arg);

	TResult visitEmptyCommand(EmptyCommand ast, TArg arg);

	TResult visitIfCommand(IfCommand ast, TArg arg);

	TResult visitLetCommand(LetCommand ast, TArg arg);

	TResult visitSequentialCommand(SequentialCommand ast, TArg arg);

	TResult visitWhileCommand(WhileCommand ast, TArg arg);

	TResult visitRepeatCommand(RepeatCommand ast, TArg arg);

}
