/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.editor.indention;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.pascal.interperter.declaration.lang.types.OperatorTypes;
import com.duy.pascal.interperter.source_include.ScriptSource;
import com.duy.pascal.interperter.tokenizer.Lexer;
import com.duy.pascal.interperter.tokens.EOFToken;
import com.duy.pascal.interperter.tokens.OperatorToken;
import com.duy.pascal.interperter.tokens.Token;
import com.duy.pascal.interperter.tokens.WordToken;
import com.duy.pascal.interperter.tokens.basic.ColonToken;
import com.duy.pascal.interperter.tokens.basic.CommaToken;
import com.duy.pascal.interperter.tokens.basic.ConstToken;
import com.duy.pascal.interperter.tokens.basic.DoToken;
import com.duy.pascal.interperter.tokens.basic.DotDotToken;
import com.duy.pascal.interperter.tokens.basic.ElseToken;
import com.duy.pascal.interperter.tokens.basic.FinalizationToken;
import com.duy.pascal.interperter.tokens.basic.ForToken;
import com.duy.pascal.interperter.tokens.basic.FunctionToken;
import com.duy.pascal.interperter.tokens.basic.IfToken;
import com.duy.pascal.interperter.tokens.basic.ImplementationToken;
import com.duy.pascal.interperter.tokens.basic.InitializationToken;
import com.duy.pascal.interperter.tokens.basic.InterfaceToken;
import com.duy.pascal.interperter.tokens.basic.OfToken;
import com.duy.pascal.interperter.tokens.basic.PeriodToken;
import com.duy.pascal.interperter.tokens.basic.ProcedureToken;
import com.duy.pascal.interperter.tokens.basic.ProgramToken;
import com.duy.pascal.interperter.tokens.basic.RepeatToken;
import com.duy.pascal.interperter.tokens.basic.SemicolonToken;
import com.duy.pascal.interperter.tokens.basic.ThenToken;
import com.duy.pascal.interperter.tokens.basic.ToToken;
import com.duy.pascal.interperter.tokens.basic.TypeToken;
import com.duy.pascal.interperter.tokens.basic.UntilToken;
import com.duy.pascal.interperter.tokens.basic.UsesToken;
import com.duy.pascal.interperter.tokens.basic.VarToken;
import com.duy.pascal.interperter.tokens.basic.WhileToken;
import com.duy.pascal.interperter.tokens.closing.ClosingToken;
import com.duy.pascal.interperter.tokens.closing.EndBracketToken;
import com.duy.pascal.interperter.tokens.closing.EndParenToken;
import com.duy.pascal.interperter.tokens.closing.EndToken;
import com.duy.pascal.interperter.tokens.grouping.BeginEndToken;
import com.duy.pascal.interperter.tokens.grouping.BracketedToken;
import com.duy.pascal.interperter.tokens.grouping.CaseToken;
import com.duy.pascal.interperter.tokens.grouping.GrouperToken;
import com.duy.pascal.interperter.tokens.grouping.ParenthesizedToken;
import com.duy.pascal.interperter.tokens.grouping.RecordToken;
import com.duy.pascal.interperter.tokens.ignore.CommentToken;
import com.duy.pascal.interperter.tokens.ignore.CompileDirectiveToken;
import com.duy.pascal.interperter.tokens.ignore.GroupingExceptionToken;
import com.duy.pascal.interperter.tokens.value.ValueToken;
import com.duy.pascal.interperter.utils.ArrayUtil;
import com.duy.frontend.DLog;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class will be reformat code pascal
 * <p>
 * <p>
 * Created by Duy on 07-May-17.
 */
public class IndentCode {
    public static final Class[] NON_NEED_SPACE = new Class[]{
            DotDotToken.class, PeriodToken.class, /*AssignmentToken.class,*/
            ColonToken.class, CommaToken.class, SemicolonToken.class, BracketedToken.class,
            ParenthesizedToken.class, /*OperatorToken.class,*/ EndBracketToken.class,
            EndParenToken.class};

    public static final Class[] STATEMENTS = new Class[]{
            BeginEndToken.class, IfToken.class, ThenToken.class, DoToken.class, ForToken.class,
            ToToken.class, CaseToken.class, RepeatToken.class, UntilToken.class, WhileToken.class
    };

    public static final Class[] OPERATORS = new Class[]{
            OperatorToken.class
    };

    public static final Class[] DECLARE_CLASSES = new Class[]{
            VarToken.class, ConstToken.class, UsesToken.class, FunctionToken.class, TypeToken.class,
            ProcedureToken.class, ImplementationToken.class, InterfaceToken.class,

            SemicolonToken.class, PeriodToken.class
    };


    public static final String TAG = "IndentCode";
    public static final String TAB = "  "; //2 space
    private int mode;
    private Reader source;

    private LinkedList<Token> stack = new LinkedList<>();
    private StringBuilder mResult;

    public IndentCode(Reader source) throws IOException {
        this.source = source;
        loadInput();
        parse();
    }

    public IndentCode() {
    }

    public static void main(String[] args) throws IOException {
        File dir = new File("C:\\Users\\Duy\\IdeaProjects\\JSPIIJ\\tests\\basic");
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".pas")) {
                IndentCode indentCode = new IndentCode(new FileReader(file));
////                System.out.println(indentCode.getResult());
////                System.out.println("------------------------");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setSource(String code) throws IOException {
        this.source = new StringReader(code);
        loadInput();
        parse();
    }

    public StringBuilder replaceNewLine(StringBuilder text) {
        StringBuilder result = new StringBuilder(text);
        int i = result.indexOf("\n\n");
        while (i > 0) {
            result.replace(i, i + 2, "\n");
            i = result.indexOf("\n\n");
        }
        return result;
    }

    public StringBuilder getResult() {
        String string = mResult.toString();
        while (string.indexOf("\n\n\n") > 0)
            string = string.replace("\n\n\n", "\n\n");
        return new StringBuilder(string);
    }

    private void loadInput() throws IOException {
        Lexer lexer = new Lexer((source), "indent", new ArrayList<ScriptSource>());
        Token token = lexer.yylex();
        while (!(token instanceof EOFToken)) {
            stack.add(token);
            token = lexer.yylex();
        }
    }

    private void parse() {
        mResult = new StringBuilder();
        while (peek() != null) {
            mResult.append(processNext(0));
        }
        DLog.d(TAG, "parse: eof");
    }

    private StringBuilder processNext(int depth, @Nullable Token token) {
        if (token instanceof EOFToken || token instanceof GroupingExceptionToken || token == null) {
            return new StringBuilder(); //end of file
        }

        if (token instanceof BeginEndToken) {
            return completeCompoundStatement(depth, token);

        } else if (token instanceof ValueToken) {
            return completeValue((ValueToken) token);

        } else if (token instanceof IfToken) {
            return completeIfStatement(depth, token);

        } else if (token instanceof DotDotToken) {
            return new StringBuilder(token.toString());
        } else if (token instanceof VarToken
                || token instanceof TypeToken
                || token instanceof ConstToken
                || token instanceof UsesToken
                || token instanceof InterfaceToken
                || token instanceof ImplementationToken
                || token instanceof FinalizationToken
                || token instanceof InitializationToken) {

            return completeDeclare(depth, token);

        } else if (token instanceof ProgramToken) {

            StringBuilder result = new StringBuilder();
            result.append(getTab(depth));
            result.append(token.toString()).append(" ");
            result.append(getLineCommand(depth, false));
            return result;

        } else if (token instanceof RecordToken) {
            return completeRecordToken(depth, token);
        } else if (token instanceof FunctionToken ||
                token instanceof ProcedureToken) {
            return completeFunctionToken(depth, token);
        } else if (token instanceof CaseToken) {
            return completeCaseToken(depth, token);
        } else if (token instanceof RepeatToken) {
            return completeRepeatUntil(depth, token);

        } else if (token instanceof GrouperToken) {
            return new StringBuilder(token.toString());

        } else if (token instanceof SemicolonToken) {
            return new StringBuilder(token.toString()).append(" \n");

        } else if (token instanceof ForToken) {
            return completeFor(depth, token);

        } else if (token instanceof WhileToken) {
            return completeWhile(depth, token);
        } else if (token instanceof ElseToken) {
            return processElse(depth, token);
        } else if (token instanceof WordToken) {
            return completeWord((WordToken) token);
        } else if (isCloseToken(token)) {

            return completeCloseToken(token);

        } else if (token instanceof CommentToken) {
            return completeCommentToken(null, depth, (CommentToken) token);
        } else if (token instanceof PeriodToken) {
            return new StringBuilder(token.toString());

        } else if (token instanceof OperatorToken) {
            if (peek() instanceof ValueToken && ((OperatorToken) token).type == OperatorTypes.DEREF) {
                return new StringBuilder(token.toString());
            } else {
                return new StringBuilder(token.toString()).append(" ");
            }
        } else if (token instanceof CompileDirectiveToken) {
            return new StringBuilder(token.toString()).append("\n");
        }
        return new StringBuilder(token.toString()).append(" ");
    }

    private StringBuilder completeDeclare(int depth, Token token) {
        StringBuilder result = new StringBuilder();
        result.append("\n").append(getTab(depth)).append(token).append("\n");

        while (peek() instanceof WordToken || peek() instanceof OperatorToken
                || peek() instanceof CommentToken) {
            result.append(getLineCommand(depth + 1, true, ArrayUtil.join(STATEMENTS, DECLARE_CLASSES)));
            if (peek() instanceof SemicolonToken) {
                appendSemicolon(result);
            }
        }
        result.append("\n");
        return result;
    }


    private StringBuilder completeCommentToken(StringBuilder last, int depth, CommentToken token) {
        StringBuilder result = new StringBuilder();
        result.append(token).append("\n");
        return result;
    }

    private StringBuilder completeCloseToken(Token token) {
        if (peek() instanceof EndToken) return new StringBuilder(token.toString()).append(" ");

        if (peek() instanceof PeriodToken || peek() instanceof SemicolonToken
                || peek() instanceof ClosingToken || peek() instanceof CommaToken) {
            return new StringBuilder(token.toString());
        } else {
            return new StringBuilder(token.toString()).append(" ");
        }
    }

    private Token peek() {
        if (stack.size() == 0) {
            return null;
        }
        return stack.peek();
    }

    private Token take() {
        if (stack.size() == 0) {
            return null;
        }
        return stack.pop();
    }

    private boolean isCloseToken(Token token) {
        return token instanceof EndToken ||
                token instanceof EndParenToken ||
                token instanceof EndBracketToken;
    }

    private StringBuilder completeValue(ValueToken token) {
        if (!(peek() instanceof WordToken || isStatement(peek()) || peek() instanceof OperatorToken)) {
            return new StringBuilder(token.toString());
        } else {
            return new StringBuilder(token.toString()).append(" ");
        }
    }

    private boolean needSpace(@Nullable Token token) {
        if (token == null) {
            return false;
        }
        for (Class aClass : NON_NEED_SPACE) {
            if (token.getClass() == aClass) return false;
        }
        return true;
    }

    private StringBuilder completeWord(WordToken token) {
        if (needSpace(peek())) {
            if (peek() instanceof OperatorToken) {
                if (((OperatorToken) peek()).type == OperatorTypes.DEREF) {
                    return new StringBuilder(token.getOriginalName());
                }
            }
            return new StringBuilder(token.getOriginalName()).append(" ");
        } else {
            return new StringBuilder(token.getOriginalName());
        }
    }

    private StringBuilder completeCaseToken(int depth, Token token) {
        StringBuilder caseStatement = new StringBuilder();
        caseStatement.append(getTab(depth)).append(token.toString()).append(" ");    //append "case .. of .."
        caseStatement.append(getLineCommand(depth, false, OfToken.class));

        if (peek() instanceof OfToken) caseStatement.append(take()).append(" \n");

        StringBuilder body = new StringBuilder();

        while (peek() != null && !(peek() instanceof EndToken)) {
            body.append(getLineCommand(depth + 1, true, SemicolonToken.class, EndToken.class));
            if (peek() instanceof SemicolonToken) appendSemicolon(body);
        }

        caseStatement.append(body);

        if (peek() instanceof ElseToken) {
            caseStatement.append(getTab(depth)).append(take()).append(" ");
            caseStatement.append(getLineCommand(depth, false));
            caseStatement.append("\n");
        }
        if (peek() instanceof EndToken) {
            caseStatement.append("\n").append(getTab(depth))
                    .append(completeEnd((EndToken) take()));
        }
        return caseStatement;
    }

    private StringBuilder completeRecordToken(int depth, Token token) {
        StringBuilder record = new StringBuilder();
        record.append(token.toString());
        record.append("\n");

        while (peek() != null && !(peek() instanceof EndToken)) {
            record.append(getLineCommand(depth + 1, true, SemicolonToken.class, EndToken.class));
            if (peek() instanceof SemicolonToken) appendSemicolon(record);
        }

        if (peek() instanceof EndToken) {
            token = take();
            record.append(getTab(depth)).append(completeEnd((EndToken) token));
        }
        return record;
    }

    private StringBuilder completeEnd(EndToken endToken) {
        StringBuilder end = new StringBuilder();
        if (!(peek() instanceof SemicolonToken
                || peek() instanceof PeriodToken)) {
            end.append("\n");
        }
        end.append(endToken.toString());
        return end;
    }

    private StringBuilder completeTypeToken() {
        return null;
    }

    private StringBuilder completeRepeatUntil(int depth, Token token) {
        StringBuilder result = new StringBuilder();
        result.append(getTab(depth)).append(token).append(" ").append("\n"); //repeat

        while (peek() != null && !(peek() instanceof UntilToken)) {
            result.append(getLineCommand(depth + 1, true, SemicolonToken.class, UntilToken.class));
            if (peek() instanceof SemicolonToken) {
                appendSemicolon(result);
            }
        }

        if (peek() instanceof UntilToken) {
            result.append("\n").append(getTab(depth)).append(take()).append(" "); //until
            result.append(getLineCommand(depth, false, SemicolonToken.class));
        }
        return result;
    }

    private StringBuilder completeWhile(int depth, @NonNull Token token) {
        StringBuilder whileStatement = new StringBuilder();
        whileStatement.append(token.toString()).append(" ");

        StringBuilder next = new StringBuilder();
        while (peek() != null
                && !(peek() instanceof DoToken)
                && !(peek() instanceof SemicolonToken)) {
            next.append(processNext(depth, take()));
        }
        whileStatement.append(next)/*.append(" ")*/;

        //if contain else token
        if (peek() instanceof DoToken) {
            //append else
            whileStatement.append(completeDo(depth, take()));
        }

        return whileStatement;
    }

    private StringBuilder completeDo(int currentDepth, Token token) {
        StringBuilder result = new StringBuilder();
        result.append(token).append(" ").append("\n"); //append "do"

        //add command
        StringBuilder next = new StringBuilder();
        next.append(getLineCommand(currentDepth + 1, true));
        result.append(next);

        DLog.d(TAG, "completeDo() returned: \n" + next);
        return result;
    }

    private StringBuilder processElse(int depth, Token token) {
        return new StringBuilder(token.toString()).append(" ");
    }

    private StringBuilder completeFor(int depth, Token token) {
        StringBuilder forStatement = new StringBuilder();
        forStatement.append(token.toString()).append(" "); //append "for"
        forStatement.append(getLineCommand(depth, false, SemicolonToken.class, DoToken.class));

        if (peek() instanceof DoToken) {
            //append else
            forStatement.append(completeDo(depth, take()));
        }
        return forStatement;
    }

    private StringBuilder completeCompoundStatement(int depth, @NonNull Token token) {
        //append "begin"
        StringBuilder beginEnd = new StringBuilder();
        beginEnd.append("\n");
        beginEnd.append(getTab(depth)).append(token.toString()).append("\n");

        StringBuilder body = new StringBuilder();
        while (peek() != null && !(peek() instanceof EndToken)) {
            body.append(getLineCommand(depth + 1, true, SemicolonToken.class, EndToken.class));
            if (peek() instanceof SemicolonToken) {
                body.append(take()).append("\n");
            } else if (peek() instanceof PeriodToken) {
                body.append(take()).append("\n");
            }
        }

        beginEnd.append(body);
        beginEnd.append("\n");

        token = peek();
        if (token instanceof EndToken) {
            take();
            beginEnd.append(getTab(depth)).append(token.toString());
            if ((peek() instanceof SemicolonToken || peek() instanceof PeriodToken
                    || peek() instanceof ElseToken)) {
            } else {
                beginEnd.append("\n");
            }
        }

        return replaceNewLine(beginEnd);
    }

    private StringBuilder processNext(int depth) {
        if (stack.size() == 0) return new StringBuilder();
        Token token = take();
        return processNext(depth, token);
    }

    private StringBuilder completeIfStatement(int depth, Token token) {
        StringBuilder result = new StringBuilder();
        result.append(token.toString()).append(" ");

        result.append(getLineCommand(depth + 1, false,
                ThenToken.class, ElseToken.class, SemicolonToken.class))/*.append(" ")*/;

        //then expression
        if (peek() instanceof ThenToken) {
            //append else
            result.append(take()).append("\n");
            result.append(getLineCommand(depth + 1, true, ElseToken.class, SemicolonToken.class))
                    .append(" ");
        }

        if (peek() instanceof ElseToken) {
            result.append("\n").append(getTab(depth)).append(take()).append(" ");  //append else

            if (peek() instanceof IfToken) {
                result.append(getLineCommand(depth, false, SemicolonToken.class));
            } else {
                result.append("\n");
                result.append(getLineCommand(depth + 1, true,
                        IfToken.class, SemicolonToken.class));
            }
        }
        return result;
    }

    private void appendSemicolon(StringBuilder result) {
        result.append(take()).append("\n");
    }

    private StringBuilder completeFunctionToken(int depth, Token token) {
        StringBuilder result = new StringBuilder();
        result.append(getTab(depth)).append(token).append(" ");
        result.append(getLineCommand(depth, false, SemicolonToken.class));
        if (peek() instanceof SemicolonToken) {
            appendSemicolon(result);
        }
        return result;
    }

    private boolean isGroupToken(Token token) {
        return (token instanceof GrouperToken || token instanceof RepeatToken)
                && !(token instanceof BracketedToken || token instanceof ParenthesizedToken);
    }

    private boolean isStatement(Token token) {
        return token instanceof IfToken ||
                token instanceof ElseToken ||
                token instanceof ThenToken ||
                token instanceof DoToken ||
                token instanceof WhileToken ||
                token instanceof ForToken ||
                token instanceof ToToken ||
                token instanceof BeginEndToken
                || token instanceof EndToken ||
                token instanceof RepeatToken
                || token instanceof CaseToken;
    }

    private boolean isIn(Class source, Class... data) {
        if (source == CommentToken.class) return true;
        for (Class aClass : data) {
            if (source == aClass) return true;
        }
        return false;
    }

    private StringBuilder getUntil(int depth, Class... stopToken) {
        StringBuilder result = new StringBuilder();
        while (peek() != null && !isIn(peek().getClass(), stopToken)) {
            StringBuilder next = processNext(depth, take());
            result.append(next);
        }
        return result;
    }

    //end of lineInfo by ;
    private StringBuilder getLineCommand(int depth, boolean tab, Class... stopToken) {
        StringBuilder result = new StringBuilder();

        boolean cmt = false;
        while (peek() instanceof CommentToken) {
            if (tab) result.append(getTab(depth));
            result.append(completeCommentToken(result, depth, (CommentToken) take()));
            //  return result;
            cmt = true;
        }
        if (tab && !isGroupToken(peek())) {
            result.append(getTab(depth));
        }
        if (isGroupToken(peek()) && !isIn(peek().getClass(), stopToken)) {
            result.append(processNext(depth, take()));
//            System.out.println("result = \n" + result);
            return result;
        }
        while (peek() != null && !isIn(peek().getClass(), stopToken)) {
            StringBuilder next = processNext(depth, take());
            result.append(next);
        }
//        System.out.println("result = \n" + result);
        return result;
    }

    //end of lineInfo by ;
    private StringBuilder getLineCommand(int depth, boolean tab) {
        return getLineCommand(depth, tab, SemicolonToken.class);
    }

    private String getWord(Token token) {
        return token.toString() + " ";
    }

    private StringBuilder getTab(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            stringBuilder.append(TAB);
        }
        return stringBuilder;
    }

    public enum FormatMode {}
}
