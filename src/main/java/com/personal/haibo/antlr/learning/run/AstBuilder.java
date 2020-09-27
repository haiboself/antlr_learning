package com.personal.haibo.antlr.learning.run;


import com.personal.haibo.antlr.learning.spark.SqlBaseBaseVisitor;
import com.personal.haibo.antlr.learning.spark.SqlBaseParser;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.ArrayList;
import java.util.List;

abstract class AstNode {

    public String toString(){
        return this.getClass().getName();
    }
}

class CreateTable extends AstNode {
    private String tb;
    private Query query;
    CreateTable(String tb, Query query){ this.tb = tb; this.query = query;}

    public String toString(){
        return String.format("CreateTable(tb=%s,%s)", tb, query.toString());
    }
}

class Query extends AstNode {
    private String projects;
    private List<String> tbs;
    private String sql;

    Query(String projects, List<String> tbs, String sql) {
        this.projects = projects;
        this.tbs = tbs;
        this.sql = sql;
    }

    public String toString(){
        return String.format("Query(projects=%s,tbs=%s,sql=%s)", projects, tbs, sql);
    }
}


public class AstBuilder extends SqlBaseBaseVisitor {
    private String originSql;
    public AstBuilder(String sql){
        this.originSql = sql;
    }

    @Override
    public Object visitChildren(RuleNode node) {
        int n = node.getChildCount();
        if(n == 1){
            return node.getChild(0).accept(this);
        } else {
            return null;
        }
    }

    @Override public AstNode visitCreateHiveTable(SqlBaseParser.CreateHiveTableContext ctx) {
        String tb = visitCreateTableHeader(ctx.createTableHeader());
        Query query = visitQuery(ctx.query());

        return new CreateTable(tb, query);
    }

    @Override public String visitCreateTableHeader(SqlBaseParser.CreateTableHeaderContext ctx) {
         return ctx.tableIdentifier().getText();
    }

    @Override public Query visitQuery(SqlBaseParser.QueryContext ctx) {
        return (Query) visit(ctx.queryNoWith());
    }

    @Override public Query visitSingleInsertQuery(SqlBaseParser.SingleInsertQueryContext ctx) {
        return (Query) visit(ctx.queryTerm());
    }

    @Override public Query visitQuerySpecification(SqlBaseParser.QuerySpecificationContext ctx) {
        String projects = ctx.namedExpressionSeq().getText();
        String sql = originSql.substring(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        List<String> tbs = visitFromClause(ctx.fromClause());

        return new Query(projects ,tbs, sql);
    }

    @Override public List<String> visitFromClause(SqlBaseParser.FromClauseContext ctx) {

        List<String> tbs = new ArrayList<String>();
        for(SqlBaseParser.RelationContext r : ctx.relation()){
            if(r.relationPrimary() instanceof SqlBaseParser.TableNameContext) {
                tbs.add((String) visit(r.relationPrimary()));
            }
        }

        return tbs;
    }

    @Override public String visitTableName(SqlBaseParser.TableNameContext ctx) {
        return ctx.tableIdentifier().getText();
    }
}


