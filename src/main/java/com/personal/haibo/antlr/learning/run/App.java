package com.personal.haibo.antlr.learning.run;

import com.personal.haibo.antlr.learning.spark.SqlBaseBaseVisitor;
import com.personal.haibo.antlr.learning.spark.SqlBaseLexer;
import com.personal.haibo.antlr.learning.spark.SqlBaseParser;
import com.personal.haibo.antlr.learning.spark.SqlBaseVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import javax.management.Query;
import java.util.ArrayList;
import java.util.List;


public class App {
    public static void main(String[] args) {
        String sql = "CREATE TABLE X.Y AS SELECT * FROM A.B JOIN C.D";
        ParserRuleContext tree = parse(sql);

        // 1. 提取表信息
        System.out.println(extractTbs(tree));

        // 2. 精简树
        AstNode myTree = (AstNode) new AstBuilder(sql).visit(tree);
        System.out.println(myTree.toString());

        // 3. 诊断能力
        sql = "SELECT * FROM X.Y WHERE X IN ('A' 'B')";
        diagnose(sql);
    }

    private static List<String> extractTbs(ParserRuleContext tree){
        final List<String> tbs = new ArrayList<String>();

        new SqlBaseBaseVisitor<Object>(){
            @Override public Object visitTableIdentifier(SqlBaseParser.TableIdentifierContext ctx) {
                tbs.add(ctx.getText());
                return visitChildren(ctx);
            }

        }.visit(tree);

        return tbs;
    }

    private static void diagnose(String sql){
        ParserRuleContext tree = parse(sql);

        new SqlBaseBaseVisitor<Object>(){
            @Override public Object visitStringLiteral(SqlBaseParser.StringLiteralContext ctx) {
                int count = ctx.getChildCount();
                if(count > 1){
                    String msg = "发现字符串常量合并情况,常量值:" + ctx.getText();
                    throw new RuntimeException(msg);
                }

                return visitChildren(ctx);
            }
        }.visit(tree);
    }

    private static SqlBaseParser getParser(String sql) {
        CharStream cs = CharStreams.fromString(sql);

        // 词法分析, 生成 tokens
        SqlBaseLexer lexer = new SqlBaseLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SqlBaseParser parser = new SqlBaseParser(tokens);
        return parser;
    }

    private static ParserRuleContext parse(String sql) {
        SqlBaseParser parser = getParser(sql);
        ParserRuleContext tree = parser.statement();

        return tree;
    }
}