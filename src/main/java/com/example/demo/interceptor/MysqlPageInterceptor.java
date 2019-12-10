package com.example.demo.interceptor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import com.example.demo.model.OrderType;
import com.example.demo.model.PageCriteria;

import lombok.extern.slf4j.Slf4j;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
@Slf4j
public class MysqlPageInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();

        StatementHandler delegate = Optional.ofNullable(this.getFieldValue(handler, "delegate"))
                .filter(StatementHandler.class::isInstance).map(StatementHandler.class::cast)
                .orElseThrow(() -> new RuntimeException("MysqlPageInterceptor intercept() delegate is null."));

        BoundSql boundSql = delegate.getBoundSql();
        Object parameters = boundSql.getParameterObject();

        MappedStatement mappedStatement = Optional.ofNullable(this.getFieldValue(delegate, "mappedStatement"))
                .filter(MappedStatement.class::isInstance).map(MappedStatement.class::cast)
                .orElseThrow(() -> new RuntimeException("MysqlPageInterceptor intercept() mappedStatement is null."));

        PageCriteria pageCriteria = checkPageCriteria(parameters);
        String idColumn = getResultMapIdColumn(mappedStatement);

        if (pageCriteria != null && StringUtils.isNotEmpty(idColumn)) {
            Connection connection = (Connection) invocation.getArgs()[0];
            String sql = boundSql.getSql();
            this.injectTotalRowsToPageCriteria(parameters, mappedStatement, connection, pageCriteria);
            String pageSql = this.getPageSql(pageCriteria, sql, idColumn, mappedStatement);
            this.setFieldValue(boundSql, "sql", pageSql);
        }
        return invocation.proceed();
    }

    private PageCriteria checkPageCriteria(Object parameter) {

        if (parameter == null) {
            return null;
        }

        PageCriteria pageCriteria = null;

        if (this.hasSuperClass(parameter.getClass(), PageCriteria.class)) {
            pageCriteria = PageCriteria.class.cast(parameter);
        } else if (parameter instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) parameter;
            for (Object arg : map.values()) {
                if (arg != null && this.hasSuperClass(arg.getClass(), PageCriteria.class)) {
                    pageCriteria = PageCriteria.class.cast(arg);
                    break;
                }
            }
        }

        return pageCriteria;
    }

    private boolean hasSuperClass(Class<?> target, Class<?> source) {
        boolean has = false;
        if (ClassUtils.getAllSuperclasses(target).contains(source) || target == source) {
            has = true;
        }
        return has;
    }

    private void injectTotalRowsToPageCriteria(Object parameters, MappedStatement mappedStatement, Connection connection,
            PageCriteria pageCriteria) {
        BoundSql boundSql = mappedStatement.getBoundSql(parameters);
        String sql = boundSql.getSql();

        String countSql = this.getCountSql(sql, this.getResultMapIdColumn(mappedStatement));

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql, parameterMappings,
                parameters);

        for(ParameterMapping parameterMapping : parameterMappings) {
            String property = parameterMapping.getProperty();
            countBoundSql.setAdditionalParameter(property, boundSql.getAdditionalParameter(property));
        }

        ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameters, countBoundSql);
        PreparedStatement pstmt = null;

        ResultSet rs = null;

        try {
            pstmt = connection.prepareStatement(countSql);
            parameterHandler.setParameters(pstmt);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalRecord = rs.getInt(1);
                pageCriteria.setTotalRows(totalRecord);
            }
        } catch (SQLException e) {
            log.error("SQLException : " + ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                log.error("SQLException : " + ExceptionUtils.getStackTrace(e));
            }
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // nothing to do
    }

    private String getCountSql(String sql, String idColumn) {
        if(StringUtils.isNotEmpty(idColumn)) {
            return "select count(*) from (select distinct innerTable." + idColumn + " from (" + sql + ") AS innerTable) AS outerTable";
        } else {
            return "select count(*) from (" + sql + ") AS innerTable";
        }
    }

    private String getResultMapIdColumn(@NotNull MappedStatement mappedStatement) {
        String idColumn = null;
        if(!mappedStatement.getResultMaps().isEmpty()) {
            int size = mappedStatement.getResultMaps().get(0).getIdResultMappings().size();
            if(size == 1) {
                ResultMapping resultMapping = mappedStatement.getResultMaps().get(0).getIdResultMappings().get(0);
                idColumn = resultMapping.getColumn();
            }
        }
        return idColumn;
    }

    private String getPageSql(PageCriteria pageCriteria, String sql, String idColumn, MappedStatement mappedStatement) {
        StringBuilder sqlBuilder = new StringBuilder();

        String orderType = OrderType.ASC.name();
        String orderColumn = idColumn;

        if(StringUtils.isNotBlank(pageCriteria.getOrderColId())) {
            orderColumn = pageCriteria.getOrderColId();
        }

        if(pageCriteria.getOrderType() != null) {
            orderType = pageCriteria.getOrderType().name();
        }

        String uniqueIdxColumnQuery = new StringBuilder()
                .append("dense_rank() over (order by sourceTable.")
                .append(orderColumn)
                .append(" ")
                .append(orderType)
                .append(", sourceTable.")
                .append(idColumn)
                .append(")")
                .toString();

        sqlBuilder.append("SELECT resultTable.* FROM ( SELECT sourceTable.*")
                 .append(", ")
                 .append(uniqueIdxColumnQuery)
                 .append(" AS row_num_by_id_column")
                 .append(", ( 1 + ")
                 .append(pageCriteria.getTotalRows() + ") - ")
                 .append(uniqueIdxColumnQuery)
                 .append(" AS desc_row_num")
                 .append(" FROM (")
                 .append(sql)
                 .append(" ) sourceTable")
                 .append(" ) resultTable")
                 .append(" WHERE resultTable.row_num_by_id_column >= ")
                 .append(" " + pageCriteria.getStartRow())
                 .append(" AND resultTable.row_num_by_id_column <= ")
                 .append(" " + pageCriteria.getEndRow());

        return sqlBuilder.toString();
    }

    private Object getFieldValue(Object clazzInstance, Object field) throws IllegalAccessException {

        Field[] fields = getFields(clazzInstance.getClass());

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(field)) {
                fields[i].setAccessible(true);
                return fields[i].get(clazzInstance);
            }
        }
        return null;
    }

    private void setFieldValue(Object obj, String fieldName, String fieldValue) throws IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(obj, fieldValue);
        }
    }

    public static <T> Field getField(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field == null) {
                field = clazz.getSuperclass().getDeclaredField(fieldName);
            }
            return field;
        } catch (Exception e) {
            log.error("Exception : " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private <T> Field[] getFields(Class<T> clazz) {

        Field[] superFields = clazz.getSuperclass().getDeclaredFields();
        Field[] extFields = clazz.getDeclaredFields();
        Field[] fields = new Field[superFields.length + extFields.length];
        System.arraycopy(superFields, 0, fields, 0, superFields.length);
        System.arraycopy(extFields, 0, fields, superFields.length, extFields.length);

        return fields;
    }
}