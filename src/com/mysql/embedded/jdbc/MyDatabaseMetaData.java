/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package com.mysql.embedded.jdbc;

import com.mysql.embedded.api.api;

import java.sql.*;

/**
 * @author Peter Sch�fer
 */

public class MyDatabaseMetaData
        implements java.sql.DatabaseMetaData
{
    protected MyConnection connection;

    protected MyDatabaseMetaData(MyConnection connection)
    {
        this.connection = connection;
    }

    public int getDatabaseMajorVersion() throws SQLException
    {
	   return connection.getDatabaseMajorVersion();
    }

    public int getDatabaseMinorVersion() throws SQLException
    {
        return 0;
    }

    public int getDefaultTransactionIsolation() throws SQLException
    {
        return 0;
    }

    public int getDriverMajorVersion()
    {
        return MySqlEmbeddedDriver.MAJOR_VERSION;
    }

    public int getDriverMinorVersion()
    {
        return MySqlEmbeddedDriver.MINOR_VERSION;
    }

    public int getJDBCMajorVersion() throws SQLException
    {
        /** JDBC 2.0 is supported, right ? */
        return 2;
    }

    public int getJDBCMinorVersion() throws SQLException
    {
        return 0;
    }

    public int getMaxBinaryLiteralLength() throws SQLException
    {
        return 16777208;
    }

    public int getMaxCatalogNameLength() throws SQLException
    {
        return 32;
    }

    public int getMaxCharLiteralLength() throws SQLException
    {
        return 16777208;
    }

    public int getMaxColumnNameLength() throws SQLException
    {
        return 64;
    }

    public int getMaxColumnsInGroupBy() throws SQLException
    {
        return 64;
    }

    public int getMaxColumnsInIndex() throws SQLException
    {
        return 16;
    }

    public int getMaxColumnsInOrderBy() throws SQLException
    {
        return 64;
    }

    public int getMaxColumnsInSelect() throws SQLException
    {
        return 256;
    }

    public int getMaxColumnsInTable() throws SQLException
    {
        return 512;
    }

    public int getMaxConnections() throws SQLException
    {
        return 0;
    }

    public int getMaxCursorNameLength() throws SQLException
    {
        return 64;
    }

    public int getMaxIndexLength() throws SQLException
    {
        return 256;
    }

    public int getMaxProcedureNameLength() throws SQLException
    {
        return 0;
    }

    public int getMaxRowSize() throws SQLException
    {
        return Integer.MAX_VALUE - 8;
    }

    public int getMaxSchemaNameLength() throws SQLException
    {
        return 0;
    }

    public int getMaxStatementLength() throws SQLException
    {
        return 0;
    }

    public int getMaxStatements() throws SQLException
    {
        return 0;
    }

    public int getMaxTableNameLength() throws SQLException
    {
        return 64;
    }

    public int getMaxTablesInSelect() throws SQLException
    {
        return 256;
    }

    public int getMaxUserNameLength() throws SQLException
    {
        return 16;
    }

    public int getResultSetHoldability() throws SQLException
    {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    public int getSQLStateType() throws SQLException
    {
        return 0;
    }

    public boolean allProceduresAreCallable() throws SQLException
    {
        return false;
    }

    public boolean allTablesAreSelectable() throws SQLException
    {
        return false;
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        return false;
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        return false;
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        return false;
    }

    public boolean isCatalogAtStart() throws SQLException
    {
        return false;
    }

    public boolean isReadOnly() throws SQLException
    {
        return false;
    }

    public boolean locatorsUpdateCopy() throws SQLException
    {
        return false;
    }

    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        return false;
    }

    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        return false;
    }

    public boolean nullsAreSortedAtStart() throws SQLException
    {
        return false;
    }

    public boolean nullsAreSortedHigh() throws SQLException
    {
        return false;
    }

    public boolean nullsAreSortedLow() throws SQLException
    {
        return false;
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        return false;
    }

    public boolean supportsANSI92FullSQL() throws SQLException
    {
        return false;
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        return false;
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        return false;
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        return false;
    }

    public boolean supportsBatchUpdates() throws SQLException
    {
        return false;
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        return false;
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        return false;
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsColumnAliasing() throws SQLException
    {
        return false;
    }

    public boolean supportsConvert() throws SQLException
    {
        return false;
    }

    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        return false;
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        return false;
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        return false;
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        return false;
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        return false;
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        return false;
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        return false;
    }

    public boolean supportsFullOuterJoins() throws SQLException
    {
        return true;
    }

    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        return false;
    }

    public boolean supportsGroupBy() throws SQLException
    {
        return true;
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        return true;
    }

    public boolean supportsGroupByUnrelated() throws SQLException
    {
        return false;
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        return false;
    }

    public boolean supportsLikeEscapeClause() throws SQLException
    {
        return false;
    }

    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        return false;
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        return false;
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        return true;
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        return true;
    }

    public boolean supportsMultipleOpenResults() throws SQLException
    {
        return true;
    }

    public boolean supportsMultipleResultSets() throws SQLException
    {
        return true;
    }

    public boolean supportsMultipleTransactions() throws SQLException
    {
        return false;
    }

    public boolean supportsNamedParameters() throws SQLException
    {
        return false;
    }

    public boolean supportsNonNullableColumns() throws SQLException
    {
        return false;
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        return false;
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        return false;
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        return false;
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        return false;
    }

    public boolean supportsOrderByUnrelated() throws SQLException
    {
        return false;
    }

    public boolean supportsOuterJoins() throws SQLException
    {
        return true;
    }

    public boolean supportsPositionedDelete() throws SQLException
    {
        return false;
    }

    public boolean supportsPositionedUpdate() throws SQLException
    {
        return false;
    }

    public boolean supportsSavepoints() throws SQLException
    {
        return false;
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        return false;
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        return false;
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        return false;
    }

    public boolean supportsSelectForUpdate() throws SQLException
    {
        return false;
    }

    public boolean supportsStatementPooling() throws SQLException
    {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }

    public boolean supportsStoredProcedures() throws SQLException
    {
        return false;
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        return false;
    }

    public boolean supportsSubqueriesInExists() throws SQLException
    {
        return false;
    }

    public boolean supportsSubqueriesInIns() throws SQLException
    {
        return false;
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        return false;
    }

    public boolean supportsTableCorrelationNames() throws SQLException
    {
        return false;
    }

    public boolean supportsTransactions() throws SQLException
    {
        return false;
    }

    public boolean supportsUnion() throws SQLException
    {
        return false;
    }

    public boolean supportsUnionAll() throws SQLException
    {
        return false;
    }

    public boolean usesLocalFilePerTable() throws SQLException
    {
        return false;
    }

    public boolean usesLocalFiles() throws SQLException
    {
        return false;
    }

    public boolean deletesAreDetected(int type) throws SQLException
    {
        return false;
    }

    public boolean insertsAreDetected(int type) throws SQLException
    {
        return false;
    }

    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        return holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    public boolean supportsResultSetType(int type) throws SQLException
    {
        return (type==ResultSet.TYPE_FORWARD_ONLY) ||
                (type==ResultSet.TYPE_SCROLL_INSENSITIVE);
    }

    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        return false;
    }

    public boolean updatesAreDetected(int type) throws SQLException
    {
        return false;
    }

    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        return false;
    }

    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        return concurrency == ResultSet.CONCUR_READ_ONLY;
    }

    public String getCatalogSeparator() throws SQLException
    {
        return null;
    }

    public String getCatalogTerm() throws SQLException
    {
        return "database";
    }

    public String getDatabaseProductName() throws SQLException
    {
        return api.mysql_get_server_info(connection.connectionHandle);
    }

    public String getDatabaseProductVersion() throws SQLException
    {
        int result = api.mysql_get_server_version(connection.connectionHandle);
        return String.valueOf(result)+" for "+MySqlEmbeddedDriver.OS_NAME;
    }

    public String getDriverName() throws SQLException
    {
        return MySqlEmbeddedDriver.VERSION_NAME;
    }

    public String getDriverVersion() throws SQLException
    {
        return MySqlEmbeddedDriver.MAJOR_VERSION+"."+MySqlEmbeddedDriver.MINOR_VERSION;
    }

    public String getExtraNameCharacters() throws SQLException
    {
        return null;
    }

    public String getIdentifierQuoteString() throws SQLException
    {
        return null;
    }

    public String getNumericFunctions() throws SQLException
    {
        return "ABS,ACOS,ASIN,ATAN,ATAN2,BIT_COUNT,CEILING,COS," +
        "COT,DEGREES,EXP,FLOOR,LOG,LOG10,MAX,MIN,MOD,PI,POW," +
        "POWER,RADIANS,RAND,ROUND,SIN,SQRT,TAN,TRUNCATE";
    }

    public String getProcedureTerm() throws SQLException
    {
        return "PROCEDURE";
    }

    public String getSQLKeywords() throws SQLException
    {
        return null;
    }

    public String getSchemaTerm() throws SQLException
    {
        return null;
    }

    public String getSearchStringEscape() throws SQLException
    {
        return null;
    }

    public String getStringFunctions() throws SQLException
    {
        return "ASCII,BIN,BIT_LENGTH,CHAR,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT," +
        "CONCAT_WS,CONV,ELT,EXPORT_SET,FIELD,FIND_IN_SET,HEX,INSERT," +
        "INSTR,LCASE,LEFT,LENGTH,LOAD_FILE,LOCATE,LOCATE,LOWER,LPAD," +
        "LTRIM,MAKE_SET,MATCH,MID,OCT,OCTET_LENGTH,ORD,POSITION," +
        "QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX," +
        "SPACE,STRCMP,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING," +
        "SUBSTRING_INDEX,TRIM,UCASE,UPPER";
    }

    public String getSystemFunctions() throws SQLException
    {
        return "DATABASE,USER,SYSTEM_USER,SESSION_USER,PASSWORD,ENCRYPT,LAST_INSERT_ID,VERSION";
    }

    public String getTimeDateFunctions() throws SQLException
    {
        return "DAYOFWEEK,WEEKDAY,DAYOFMONTH,DAYOFYEAR,MONTH,DAYNAME," +
        "MONTHNAME,QUARTER,WEEK,YEAR,HOUR,MINUTE,SECOND,PERIOD_ADD," +
        "PERIOD_DIFF,TO_DAYS,FROM_DAYS,DATE_FORMAT,TIME_FORMAT," +
        "CURDATE,CURRENT_DATE,CURTIME,CURRENT_TIME,NOW,SYSDATE," +
        "CURRENT_TIMESTAMP,UNIX_TIMESTAMP,FROM_UNIXTIME," +
        "SEC_TO_TIME,TIME_TO_SEC";
    }

    public String getURL() throws SQLException
    {
        String cat = connection.getCatalog();
        if (cat==null)
            return "jdbc:mysql-embedded";
        else
            return "jdbc:mysql-embedded/"+cat;
    }

    public String getUserName() throws SQLException
    {
        return null;
    }

    public Connection getConnection() throws SQLException
    {
        return connection;
    }

    public ResultSet getCatalogs() throws SQLException
    {
        String sql ="SELECT DISTINCT Catalog_Name AS TABLE_CAT " +
                    " FROM Information_Schema.Schemata" +
                    " WHERE Catalog_Name IS NOT NULL";
        return connection.createStatement().executeQuery(sql);
    }

    public ResultSet getSchemas() throws SQLException
    {
        String sql ="SELECT DISTINCT Schema_Name AS TABLE_SCHEM," +
                    " Catalog_Name AS TABLE_CATALOG" +
                    " FROM Information_Schema.Schemata";
        return connection.createStatement().executeQuery(sql);
    }

    public ResultSet getTableTypes() throws SQLException
    {
        String sql ="SELECT DISTINCT TABLE_TYPE " +
                    " FROM Information_Schema.Tables";
        return connection.createStatement().executeQuery(sql);
    }

    public ResultSet getTypeInfo() throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        if (schema==null) schema="%";
        if (table==null) table="%";

        String sql ="SELECT NULL AS TABLE_CAT," +
                    " Table_Schema AS TABLE_SCHEM," +
                    " TABLE_NAME," +
                    " COLUMN_NAME," +
                    " Ordinal_Position AS KEY_SEQ," +
                    " Constraint_Name AS PK_NAME" +
                    " FROM Information_Schema.Key_Column_Usage" +
                    " WHERE Constraint_Name = 'PRIMARY'" +
                    "   AND Table_Schema LIKE '"+schema+"'" +
                    "   AND Table_Name LIKE '"+table+"'" +
                    " ORDER BY Ordinal_Position";
        throw new UnsupportedOperationException();
    }

    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        //  TOOD
        throw new UnsupportedOperationException();
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        //  TODO
        throw new UnsupportedOperationException();
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
    {
        String sql;
        if (catalog==null)
            sql = "SHOW INDEX FROM "+table;
        else
            sql = "SHOW INDEX FROM "+catalog+"."+table;

        Statement stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        return stm.executeQuery(sql);
    }

    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        if (schemaPattern==null) schemaPattern = "%";
        String sql = "SELECT Table_Catalog AS TABLE_CAT," +
                " Table_Schema AS TABLE_SCHEM," +
                " TABLE_NAME," +
                " COLUMN_NAME," +
                " 0 AS DATA_TYPE," +
                " Data_Type AS TYPE_NAME," +
                " Character_Maximum_Length AS COLUMN_SIZE," +
                " NULL AS BUFFER_LENGTH," +
                " Numeric_Precision AS DECIMAL_DIGITS," +
                " 10 AS NUM_PREC_RADIX," +
                " Is_Nullable AS NULLABLE," +
                " Column_Comment AS REMARKS," +
                " NULL AS COLUMN_DEF," +
                " NULL AS SQL_DATA_TYPE, " +
                " NULL AS SQL_DATETIME_SUB," +
                " Character_Octet_Length AS CHAR_OCTET_LENGTH," +
                " ORDINAL_POSITION," +
                " IS_NULLABLE," +
                " NULL AS SCOPE_CATLOG," +
                " NULL AS SCOPE_SCHEM," +
                " NULL AS SCOPE_TABLE," +
                " NULL AS SOURCE_DATA_TYPE" +
                " FROM Information_Schema.Columns " +
                " WHERE Table_Schema LIKE '"+schemaPattern+"'" +
                "  AND Table_Name LIKE '"+tableNamePattern+"'" +
                "  AND Column_Name LIKE '"+columnNamePattern+"'" +
                " ORDER BY Ordinal_Position";
        return connection.createStatement().executeQuery(sql);
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public ResultSet getTables(String catalog,
                               String schemaPattern, String tableNamePattern, String types[]) throws SQLException
    {
        if (schemaPattern==null) schemaPattern="%";

        String sql = "SELECT Table_Catalog AS TABLE_CAT," +
                " Table_Schema AS TABLE_SCHEM," +
                " TABLE_NAME," +
                " TABLE_TYPE," +
                " Table_Comment AS REMAKRS," +
                " NULL AS TYPE_CAT," +
                " NULL AS TYPE_SCHEM," +
                " NULL AS TYPE_NAME," +
                " NULL AS SELF_REFERENCING_COL_NAME," +
                " NULL AS REF_GENERATION" +
                " FROM Information_Schema.Tables " +
                " WHERE Table_Schema LIKE '"+schemaPattern+"'" +
                "   AND Table_Name LIKE '"+tableNamePattern+"'";
        if (types!=null && types.length > 0) {
            sql += "  AND Table_Type IN (";
            for (int i=0; i < types.length; i++) {
                if (i>0) sql += ",";
                sql += "'"+types[i]+"'";
            }
            sql += ")";
        }
        return connection.createStatement().executeQuery(sql);
    }

    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
