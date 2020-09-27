package com.personal.haibo.antlr.learning.run;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.internal.SQLConf;

public class Catalyst {
    public static void main(String[] args) {
        String sql = "create table x.y as select * from a.b";
        CatalystSqlParser parser = new CatalystSqlParser(new SQLConf());
        SqlBaseParser.CreateHiveTableContext tree = (SqlBaseParser.CreateHiveTableContext) parser.parse(sql, SqlBaseParser::statement);
        SqlBaseParser.QueryContext query = tree.query();
        int start = query.getStart().getStartIndex();
        int stop = query.getStop().getStopIndex()+1;

        String tmpTb = "create table h.h as " + sql.substring(start, stop);
        LogicalPlan logicalPlan = (LogicalPlan) parser.astBuilder().visit(tree);
        System.out.println(logicalPlan);
    }
}
