package com.personal.haibo.antlr.learning.run;


import com.personal.haibo.antlr.learning.spark.SqlBaseBaseVisitor;
import com.personal.haibo.antlr.learning.spark.SqlBaseParser;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.spark.sql.catalyst.expressions.Attribute;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;

abstract class AstNode {}

class CreateTable extends AstNode {
    private String tb;
    private Query query;
    CreateTable(String tb, Query query){ this.tb = tb; this.query = query;}

    public String toString(){
        return String.format("CreateTable(tb=%s)\n+-\t%s", tb, query.toString());
    }
}

class Query extends AstNode {
    private Project project;
    private List<String> tbs;
    private String sql;

    Query(Project project, List<String> tbs, String sql) {
        this.project = project;
        this.tbs = tbs;
        this.sql = sql;
    }

    public String toString(){
        return String.format("Query(tbs=%s,sql=%s)\n\t+-\t%s", tbs, sql, project.toString());
    }
}

class Project extends AstNode {
    private String name;
    Project(String name){
        this.name = name;
    }

    public String toString(){
        return String.format("Project(name=%s)", name);
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
         return ctx.tableIdentifier().table.getText();
    }

    @Override public Query visitQuery(SqlBaseParser.QueryContext ctx) {
        return (Query) visit(ctx.queryNoWith());
    }

    @Override public Query visitSingleInsertQuery(SqlBaseParser.SingleInsertQueryContext ctx) {
        return (Query) visit(ctx.queryTerm());
    }

    @Override public Query visitQuerySpecification(SqlBaseParser.QuerySpecificationContext ctx) {
        Project project = new Project(ctx.namedExpressionSeq().getText());
        String sql = originSql.substring(ctx.start.getStartIndex(), ctx.stop.getStopIndex()+1);
        List<String> tbs = visitFromClause(ctx.fromClause());

        return new Query(project ,tbs, sql);
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


