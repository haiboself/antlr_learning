package com.personal.haibo.antlr.learning.run;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser;
import org.apache.spark.sql.catalyst.parser.SqlBaseParser;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.internal.SQLConf;

public class Catalyst {
    public static void main(String[] args) {
        String sql = "SELECT * FROM TEST.Y WHERE X IN ('A' 'B')";
        CatalystSqlParser parser = new CatalystSqlParser(new SQLConf());
        ParserRuleContext tree = parser.parse(sql, SqlBaseParser::singleStatement);
        LogicalPlan logicalPlan = (LogicalPlan) parser.astBuilder().visit(tree);
        System.out.println(logicalPlan.toString());
    }
}
