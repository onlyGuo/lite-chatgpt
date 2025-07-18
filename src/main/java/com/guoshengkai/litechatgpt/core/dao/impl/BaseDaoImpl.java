package com.guoshengkai.litechatgpt.core.dao.impl;

import com.alibaba.fastjson.JSON;
import com.guoshengkai.litechatgpt.core.annotation.po.JsonField;
import com.guoshengkai.litechatgpt.core.beans.*;
import com.guoshengkai.litechatgpt.core.dao.BaseDao;
import com.guoshengkai.litechatgpt.core.sql.where.C;
import com.guoshengkai.litechatgpt.core.sql.where.SqlUtil;
import com.guoshengkai.litechatgpt.core.util.DBUtil;
import com.guoshengkai.litechatgpt.core.util.GenericsUtils;
import com.guoshengkai.litechatgpt.core.util.hibernate.jdbc.util.FormatStyle;
import com.guoshengkai.litechatgpt.core.util.lambda.LambdaUtil;
import com.guoshengkai.litechatgpt.exception.ServiceInvokeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class BaseDaoImpl<T extends PO, PK extends Serializable> implements BaseDao<T, PK> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private SqlSessionTemplate sqlSessionTemplate;

	private Class<T> entityClass;
	/**
	 * 实体类主键
	 */
	private String pkName;
	/**
	 * 数据库主键
	 */
	private String idName;

	private String seq;

	private String tableName;

	private List<Pram> sqlParms;


	private List<Pram> selectSqlParms;

	private SqlUtil<T> sqlUtil;

	private String idType;

	@Value("${spring.datasource.driver-class-name}")
	private String driverClassName;

	private Pattern rightFmtPattern = Pattern.compile("'(\\[fmt.R.+)'");

	private Pattern leftFmtPattern = Pattern.compile("'(\\[fmt.L.+)'");

	private StringBuffer selectSql = null;

	private StringBuffer insertSql = null;

	private StringBuffer updateSql = null;

	@Resource
	private DBUtil dbUtil;

	@SuppressWarnings("unchecked")
	public BaseDaoImpl(){
		super();

		this.sqlUtil = new SqlUtil<T>();

		this.entityClass = (Class<T>) GenericsUtils.getSuperClassGenricType(this.getClass());

		this.sqlParms = this.sqlUtil.getPramList(this.entityClass);

		this.selectSqlParms = this.sqlUtil.getPramListOfSelect(this.entityClass);

		this.tableName = this.sqlUtil.getTableName(this.entityClass);

		this.pkName = this.sqlUtil.getPkName(this.entityClass);

		this.idName = this.sqlUtil.getIdName(this.entityClass);

		this.seq = this.sqlUtil.getPkName(this.entityClass);

		this.idType = this.sqlUtil.getIdType(this.entityClass);

		getSelectSql();
		insertSql();
		updateSql();

		logger.info("Init" + getClass().toString());

	}

	@PostConstruct
	private void updateTable() {
		// 检查表是否存在

		if (dbUtil.tableExists(tableName)){
			// 检查表字段是否存在
			for (Pram pram : sqlParms) {
				if (!dbUtil.columnExists(tableName, pram.getDbField())){
					dbUtil.execute("ALTER TABLE " + tableName + " ADD " + pram.getDbField() + " " + pram.getDbType() + " NULL");
				}
			}
		}else{
			StringBuffer columns = new StringBuffer(pkName).append(" int auto_increment primary key");
			for (Pram pram : sqlParms) {
				columns.append(",\n").append(pram.getDbField()).append(" ").append(pram.getDbType()).append(" NULL");
			}
			dbUtil.execute("CREATE TABLE " + tableName + " (" + columns + ")");
		}
	}

	@Override
	public int add(T po) {

		StringBuffer insertSql = getInsertSql(po);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", insertSql.toString());

		List<Pram> pramList = SqlUtil.getPramListofStatic(po);
		PK pkValue = null;
		if (null != idType && idType.contains("String")){
			Pram pa = new Pram();
			pa.setDbField(idName);
			pkValue = getId();
			pa.setValue(pkValue);
			pramList.add(pa);
		}

		for (int i = 0; i < pramList.size(); i++) {
			if (pramList.get(i).getField() != null){
				JsonField annotation = pramList.get(i).getField().getAnnotation(JsonField.class);
				if (null != annotation){
					String stringify = JSON.toJSONString(pramList.get(i).getValue());
					paramMap.put(String.format("param_%s", i), stringify);
				}else{
					paramMap.put(String.format("param_%s", i), pramList.get(i).getValue());
				}
			}else{
				paramMap.put(String.format("param_%s", i), pramList.get(i).getValue());
			}


		}

		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(insertSql.toString()));
		int result = sqlSessionTemplate.insert("excuteInsertSql", paramMap);

		if(null == pkValue) {
			pkValue = getId();
		}
		SqlUtil.setFileValue(po, this.idName, pkValue);
		return result;
	}

	@Override
	public T get(PK id) {
		StringBuffer sql = new StringBuffer(getSelectSql()).append(" WHERE ").append(this.pkName).append(" = #{id}");
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", sql.toString());
		paramMap.put("id", id);

		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		Map<String, Object> resultMap = sqlSessionTemplate.selectOne("excuteQuerySql", paramMap);
		return handleResult(resultMap, this.entityClass);
	}

	@Override
	public T get(WherePrams where) {
		StringBuffer sql = new StringBuffer(getSelectSql()).append(" ").append(where.getWherePrams());
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", sql.toString());
		paramMap.putAll(where.getWhereMap());


		Map<String, Object> resultMap = sqlSessionTemplate.selectOne(
				"excuteQuerySql", paramMap);
		return handleResult(resultMap, this.entityClass);
	}

	@Override
	public Serializable getField(WherePrams where, String fileName) {
		StringBuffer sql = new StringBuffer("SELECT ")
				.append(fileName).append(" AS f_1 ")
				.append(" FROM ").append(tableName()).append(" ")
				.append(where.getWherePrams());

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", sql.toString());
		paramMap.putAll(where.getWhereMap());

		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		Map<String, Object> resultMap = sqlSessionTemplate.selectOne(
				"excuteQuerySql", paramMap);
		return (Serializable) resultMap.get("f_1");
	}

	@Override
	public List<T> list(WherePrams where) {
		StringBuffer sql = new StringBuffer(getSelectSql());
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.putAll(where.getWhereMap());

		String limit = where.getLimit();
		if (null != limit){
			if (driverClassName.toUpperCase().contains("MYSQL")){
				sql.append(" ").append(where.getWherePrams());
			}else if(driverClassName.toUpperCase().contains("ORACLE")){
				String[] page = limit.toUpperCase().replace("LIMIT", "").trim().replace(" ", "").split(",");
				String pageId = "RN________";
				sql = new StringBuffer(sql.toString().replace(limit, "")).append(" ").append(where.getWherePrams());
				String pageSql = "";
				for (int i = 0; i < selectSqlParms.size(); i++) {
					String file = selectSqlParms.get(i).getField().getName().toUpperCase();
					pageSql += file;
					if(i < selectSqlParms.size() -1){
						pageSql += ",";
					}else{
						pageSql += " ";
					}
				}
				pageSql += ", ROWNUM AS " + pageId;
				sql = new StringBuffer("SELECT * FROM (SELECT ")
						.append(pageSql).append(" FROM ( ").append(sql)
						.append(" )t)t2 WHERE t2.").append(pageId).append(" > ").append(page[0])
						.append(" AND t2.").append("pageId").append(" <= (")
						.append(page[0]).append(" + ").append(page[1]).append(")");
			}
		}else{
			sql.append(" ").append(where.getWherePrams());
		}
		paramMap.put("_sql", sql.toString());
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		return queryList(paramMap);
	}

	@Override
	public ResultPage<T> list(WherePrams where, int page, int size) {
		where.clearLimit();
		long count = count(where);
		List<T> list = list(where.limit((page - 1) * size, size));
		return new ResultPage<>(count, page, size, list);
	}

	@Override
	public List<T> list(WherePrams where, LeftJoin joins) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.putAll(where.getWhereMap());
		StringBuffer select = getSelectSqlByOtherTable(joins, paramMap);
		select.append(" ").append(where.getWherePrams());
		paramMap.put("_sql", select.toString());
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(select.toString()));
		return queryList(paramMap);
	}

	@Override
	public ResultPage<T> list(WherePrams where, LeftJoin joins, int page, int size) {
		StringBuffer select = new StringBuffer("SELECT COUNT(*) AS COUNT FROM ");
		select.append(tableName()).append(" ");
		Map<String, Object> whereMap = where.getWhereMap();
		getSelectTableSqlByOtherTable(select, joins, whereMap);
		select.append(" ").append(where.getWherePrams());
		whereMap.put("_sql", select.toString());
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(select.toString()));
		List<Map<String, Object>> selectList = sqlSessionTemplate.selectList("excuteQuerySql", whereMap);
		long count = 0;
		if (!selectList.isEmpty()){
			count = (Long)selectList.get(0).get("COUNT");
		}
		where.limit((page - 1) * size, size);
		List<T> list = list(where, joins);
		return new ResultPage<>(count, page, size, list);
	}

	@Override
	public List<T> list(String sql, Object... params) {
		Map<String, Object> sqlParamsMap = getSqlParamsMap(sql, params);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql));
		return queryList(sqlParamsMap);
	}

	@Override
	public List<T> list(String sql, WherePrams wherePrams) {
		sql += wherePrams.getWherePrams();
		Map<String, Object> whereMap = wherePrams.getWhereMap();
		whereMap.put("_sql", sql);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql));
		return queryList(whereMap);
	}

	@Override
	public int update(T po) {
		Serializable id = sqlUtil.getFileValue(po, this.idName);
		// 锁定当前时间
//		SqlUtil.setFileValue(po, "_updateTime", new Date());

		if(null == id){
			return 0;
		}

		Map<String, Object> paramMap = new HashMap<>();
		StringBuffer sql = getUpdateSql(po);
		paramMap.put("_sql", sql.toString());
		List<Pram> prams = sqlUtil.getPramList(po);

		for (int i = 0; i < prams.size(); i++) {
			JsonField annotation = prams.get(i).getField().getAnnotation(JsonField.class);
			if (null != annotation){
				String stringify = JSON.toJSONString(prams.get(i).getValue());
				paramMap.put("param_" + i, stringify);
			}else{
				paramMap.put("param_" + i, prams.get(i).getValue());
			}
		}
		paramMap.put("id", id);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		return sqlSessionTemplate.update("excuteUpdateSql", paramMap);
	}

	@Override
	public int del(PK id) {
		String sql = String.format("DELETE FROM %s WHERE %s = #{id}", tableName, this.pkName);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", sql);
		paramMap.put("id", id);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		return sqlSessionTemplate.delete("excuteDeleteSql", paramMap);
	}

	@Override
	public int del(WherePrams where) {
		String sql = String.format("DELETE FROM %s ", tableName()) + where.getWherePrams();
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("_sql", sql);
		paramMap.putAll(where.getWhereMap());
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		return sqlSessionTemplate.delete("excuteDeleteSql", paramMap);
	}

	@Override
	public List<Map<String, Object>> listBySql(String sql, Object... p) {
		Map<String, Object> sqlParamsMap = getSqlParamsMap(sql, p);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		List<Map<String, Object>> selectList = sqlSessionTemplate.selectList("excuteQuerySql", sqlParamsMap);
		return selectList;
	}

	@Override
	public int execute(String sql, Object... params) {
		Map<String, Object> sqlParamsMap = getSqlParamsMap(sql, params);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		return sqlSessionTemplate.update("excuteUpdateSql", sqlParamsMap);
	}

	@Override
	public long count(WherePrams where) {
		String sql = String.format("SELECT COUNT(*) AS COUNT FROM %s %s", tableName, where.getWherePrams());
		Map<String, Object> sqlParamsMap = new HashMap<>();
		sqlParamsMap.putAll(where.getWhereMap());
		sqlParamsMap.put("_sql", sql);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		List<Map<String, Object>> selectList = sqlSessionTemplate.selectList("excuteQuerySql", sqlParamsMap);
		if (selectList.isEmpty()){
			return 0;
		}
		return (Long)selectList.get(0).get("COUNT");
	}

	@Override
	public long size() {
		String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
		Map<String, Object> sqlParamsMap = new HashMap<>();
		sqlParamsMap.put("_sql", sql);
		logger.debug("SQL => \n{}", FormatStyle.BASIC.getFormatter().format(sql.toString()));
		long count = sqlSessionTemplate.selectOne("excuteQuerySql", sqlParamsMap);
		return count;
	}

	@Override
	public boolean isExist(T po) {
		WherePrams wherePrams = Method.createDefault();

		List<Pram> list = SqlUtil.getPramListofStatic(po);

		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				wherePrams = Method.where(list.get(i).getDbField(), C.EQ, (Serializable)list.get(i).getValue());
			}else{
				wherePrams.and(list.get(i).getDbField(), C.EQ, (Serializable)list.get(i).getValue());
			}
		}
		return count(wherePrams) > 0;
	}

	@Override
	public boolean isExist(WherePrams where) {
		return count(where) > 0;
	}

	/**
	 * 获取SQL模板执行参数集合
	 * @param sql
	 * 		SQL语句
	 * @param params
	 * 		参数列表
	 * @return
	 */
	private Map<String, Object> getSqlParamsMap(String sql, Object... params){
		int count = 0;
		StringBuffer newSql = new StringBuffer();
		Map<String, Object> paramMap = new HashMap<>();


		char[] chars = sql.toCharArray();
		for (int i = 0; i < chars.length; i++){
			if (chars[i] == '?'){
				if (params.length - 1 < count){
					throw new IllegalArgumentException("Sql params index out of range!");
				}

				if (params[count] instanceof Collection){
					Collection collection = (Collection)params[count];
					Iterator iterator = collection.iterator();
					StringBuffer buffer = new StringBuffer();
					buffer.append('(');
					while (iterator.hasNext()){
						buffer.append("'").append(iterator.next()).append("'");
						if (iterator.hasNext()){
							buffer.append(", ");
						}
					}
					buffer.append(")");
					newSql.append(buffer);
				}else{
					newSql.append("#{_p_" + count + "}");
					paramMap.put("_p_" + count, params[count]);
				}

				count++;

			}else{
				newSql.append(chars[i]);
			}
		}
		paramMap.put("_sql", newSql.toString());
		return paramMap;
	}

	/**
	 * 获取更新SQL
	 * @param po
	 * @return
	 */
	private StringBuffer getUpdateSql(T po){
		if (null == updateSql || updateSql.toString().isEmpty()){
			updateSql = new StringBuffer("UPDATE ").append(tableName).append(" SET ");
			List<Pram> prams = sqlUtil.getPramList(po);

			for (int i = 0; i < prams.size(); i++) {
				updateSql.append(prams.get(i).getDbField()).append(" = ")
						.append(String.format("#{param_%s}", String.valueOf(i)));
				if (i < prams.size() - 1){
					updateSql.append(", ");
				}
			}
			updateSql.append(" WHERE ").append(pkName).append(" = #{id}");
		}
		return updateSql;
	}

	/**
	 * 获取更新SQL 语句
	 * @return
	 */
	public String updateSql(){
		if (null == updateSql || updateSql.toString().isEmpty()){
			try {
				return getUpdateSql(this.entityClass.newInstance()).toString();
			} catch (InstantiationException|IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return updateSql.toString();
	}

	/**
	 * 获取插入操作的SQL头
	 * @param po
	 * 		对应的实体类
	 * @return
	 */
	private StringBuffer getInsertSql(T po){
		if (null == insertSql || insertSql.toString().isEmpty()){
			insertSql = new StringBuffer(String.format("INSERT INTO %s (", tableName));
			StringBuffer buffer = new StringBuffer();
			StringBuffer valueBuffer = new StringBuffer("(");
			List<Pram> pramList = SqlUtil.getParamListCommon(po, true);
			if (null != idType && idType.contains("String")){
				Pram pa = new Pram();
				pa.setDbField(idName);
				pramList.add(pa);
			}

			for (int i = 0; i < pramList.size(); i++) {
				buffer.append(pramList.get(i).getDbField());
				valueBuffer.append(String.format("#{param_%s}", String.valueOf(i)));
				if (i < pramList.size() - 1){
					buffer.append(", ");
					valueBuffer.append(", ");
				}else{
					buffer.append(")");
					valueBuffer.append(")");
				}
			}

			insertSql.append(buffer).append(" VALUES ").append(valueBuffer);
		}
		return insertSql;
	}

	/**
	 * 获取插入语句
	 * @return
	 */
	public String insertSql(){
		if (null == insertSql || insertSql.toString().isEmpty()){
			try {
				return getInsertSql(this.entityClass.newInstance()).toString();
			} catch (InstantiationException|IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return insertSql.toString();
	}

	/**
	 * 获取要查询SQL段
	 * @return
	 */
	private StringBuffer getSelectSql(){
		if (null == selectSql || selectSql.toString().isEmpty()){
			selectSql = new StringBuffer("SELECT ");
			for (int i = 0; i < selectSqlParms.size(); i++) {
				selectSql.append(selectSqlParms.get(i).getDbField())
						.append(" AS `").append(selectSqlParms.get(i).getField().getName()).append("`");
				if(i < selectSqlParms.size() -1){
					selectSql.append(",");
				}else{
					selectSql.append(" ");
				}
			}
			selectSql.append("FROM ").append(tableName());
		}
		return selectSql;
	}

	/**
	 * 获得带有外表的SQL查询字段语句
	 * @param joins
	 * @return
	 */
	private StringBuffer getSelectSqlByOtherTable(LeftJoin joins, Map<String, Object> params){
		StringBuffer select_ = new StringBuffer("SELECT ");
		for (int i = 0; i < selectSqlParms.size(); i++) {
			select_.append(tableName() + "." + selectSqlParms.get(i).getDbField())
					.append(" AS `").append(selectSqlParms.get(i).getField().getName()).append("`");
			if(i < selectSqlParms.size() -1){
				select_.append(",");
			}else{
				select_.append(" ");
			}
		}
		List<LeftJoin.JoinItem> items = joins.getItems();
		for (int i = 0; i < items.size(); i++){
			LeftJoin.JoinItem joinItem = items.get(i);
			for (int j = 0; j < joinItem.getJoinFields().length; j++){
				select_.append(",");
				String beanName = LambdaUtil.getBeanName(joinItem.getJoinFields()[j]);
				if (beanName.contains(".")){
					beanName = beanName.substring(beanName.indexOf("."));
				}
				select_.append(LambdaUtil.getTableName(joinItem.getJoinFields()[j]))
						.append(" AS `").append(beanName).append("`");
			}
		}
		select_.append(" FROM ").append(tableName() + " AS " + tableName());
		getSelectTableSqlByOtherTable(select_, joins, params);
		return select_;
	}

	private StringBuffer getSelectTableSqlByOtherTable(StringBuffer select_, LeftJoin joins, Map<String, Object> params){
		List<LeftJoin.JoinItem> items = joins.getItems();
		for (int i = 0; i < items.size(); i++){
			LeftJoin.JoinItem joinItem = items.get(i);
			select_.append(" LEFT JOIN ").append(sqlUtil.getTableName(joinItem.getClazz())).append(" ");
			String wherePrams = joinItem.getOn().getWherePrams();
			Map<String, Object> whereMap = joinItem.getOn().getWhereMap();
			int size = params.keySet().size();
			for (String key: whereMap.keySet()){
				Object o = whereMap.get(key);
				String newKey = String.format("#{param_%s}", size);
				wherePrams = wherePrams.replace(String.format("#{%s}", key), newKey);
				params.put("param_" + size, o);
				size ++;
			}
			select_.append("ON ").append(wherePrams.substring(5));
		}
		return select_;
	}

	public String selectSql(){
		if (null == selectSql){
			return getSelectSql().toString();
		}
		return selectSql.toString();
	}

	/**
	 * 查询表名
	 * @return
	 */
	public String tableName(){
		return tableName;
	}

	/**
	 * 单个列表元素
	 * @param list
	 * @return
	 */
	public T item(List<T> list){
		if (list.isEmpty()){
			return null;
		}
		return list.get(0);
	}


	private List<T> queryList(Map<String, Object> params){
		List<Map<String, Object>> selectList = sqlSessionTemplate.selectList("excuteQuerySql", params);

		List<T> list = new ArrayList<>();
		for (Map<String, Object> map : selectList) {
			T t = handleResult(map, this.entityClass);
			list.add(t);
		}
		return list;
	}


	@Override
	public List<T> in(String fileName, Serializable[] values) {
		if (values.length < 1){
			return new LinkedList<>();
		}
		StringBuffer sql = new StringBuffer(getSelectSql()).append(" WHERE ").append(fileName).append(" IN (");
		for(int i = 0; i < values.length; i++){
			if(i < values.length -1){
				sql.append("'").append(values[i]).append("', ");
			}else{
				sql.append("'").append(values[i]).append("')");
			}
		}
		return list(sql.toString());
	}

	public T handleResult(Map<String, Object> resultMap, Class<T> tClazz) {
		if (null == resultMap) {
			return null;
		}
		T t = null;
		try {
			t = tClazz.newInstance();
		} catch (InstantiationException|IllegalAccessException e) {
			throw new ServiceInvokeException("实例化Bean失败:" + this.entityClass, e);
		}
		for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
			String key = entry.getKey();
			Serializable val = (Serializable) entry.getValue();
			try {
				SqlUtil.setFileValueByOracle(t, key, val);
			} catch (Exception e) {
				throw new ServiceInvokeException("字段赋值失败(" + this.entityClass + ")不能赋值(" + key + ")", e);
			}
		}
		return t;
	}

	/**
	 * 是否为SQL表达符号
	 * @param c
	 * @return
	 */
	private boolean isC(String c){
		switch (c) {
			case "=":
			case "<":
			case ">":
			case "<>":
			case "LIKE":
			case "IN":
			case ">=":
			case "<=":
				return true;
			default:
				return false;
		}
	}

	/**
	 * 从List<String>集合中检查是否有存在的元素
	 * @param list
	 * @param tabName
	 * @return
	 */
	private boolean isExcTab (List<String> list, String tabName){
		for (String string : list) {
			if (tabName .equals( string)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private PK getId() {

		if(this.idType.indexOf("String") != -1) {
			return (PK)UUID.randomUUID().toString().replace("-", "");
		}
		return sqlSessionTemplate.selectOne("lastInsertId");
	}


	@Override
	public String toString() {
		return "DaoImpl{" +
				"\n\tsqlSessionTemplate=" + sqlSessionTemplate +
				", \n\tentityClass=" + entityClass +
				", \n\tpkName='" + pkName + '\'' +
				", \n\tidName='" + idName + '\'' +
				", \n\tseq='" + seq + '\'' +
				", \n\ttableName='" + tableName + '\'' +
				", \n\tsqlParms=" + sqlParms +
				", \n\tselectSqlParms=" + selectSqlParms +
				", \n\tnsqlUtil=" + sqlUtil +
				", \n\tidType='" + idType + '\'' +
				'}';
	}



}
