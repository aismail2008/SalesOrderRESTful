package com.code.dal;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.code.enums.FunctionProcedureParameterType;
import com.code.exceptions.DatabaseException;
import com.code.exceptions.NoDataException;

public class DataAccess {
	protected static SessionFactory sessionFactory;

	protected DataAccess() {
	}

	public static void init() {
		try {
			Configuration configuration = new Configuration();
			configuration.configure("com/code/dal/hibernate.cfg.xml");
			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
			sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (HibernateException exception) {
			System.out.println("Problem creating session Factory!");
			exception.printStackTrace();
		}
	}

	public static CustomSession getSession() {
		return new CustomSession(sessionFactory.openSession());
	}

	public static void addEntity(BaseEntity bean, CustomSession... useSession)
			throws DatabaseException {
		boolean isOpenedSession = false;
		if (useSession != null && useSession.length > 0)
			isOpenedSession = true;

		Session session = isOpenedSession ? useSession[0].getSession()
				: sessionFactory.openSession();

		try {
			if (!isOpenedSession)
				session.beginTransaction();

			session.save(bean);

			if (!isOpenedSession)
				session.getTransaction().commit();
		} catch (Exception e) {
			if (!isOpenedSession)
				session.getTransaction().rollback();
			throw new DatabaseException(e.getMessage());
		} finally {
			if (!isOpenedSession)
				session.close();
		}
	}

	public static void updateEntity(BaseEntity bean,
			CustomSession... useSession) throws DatabaseException {
		boolean isOpenedSession = false;
		if (useSession != null && useSession.length > 0)
			isOpenedSession = true;

		Session session = isOpenedSession ? useSession[0].getSession()
				: sessionFactory.openSession();

		try {
			if (!isOpenedSession)
				session.beginTransaction();

			session.saveOrUpdate(bean);

			if (!isOpenedSession)
				session.getTransaction().commit();
		} catch (Exception e) {
			if (!isOpenedSession)
				session.getTransaction().rollback();
			throw new DatabaseException(e.getMessage());
		} finally {
			if (!isOpenedSession)
				session.close();
		}
	}

	public static void deleteEntity(BaseEntity bean,
			CustomSession... useSession) throws DatabaseException {
		boolean isOpenedSession = false;
		if (useSession != null && useSession.length > 0)
			isOpenedSession = true;

		Session session = isOpenedSession ? useSession[0].getSession()
				: sessionFactory.openSession();

		try {
			if (!isOpenedSession)
				session.beginTransaction();

			session.delete(bean);

			if (!isOpenedSession)
				session.getTransaction().commit();
		} catch (Exception e) {
			if (!isOpenedSession)
				session.getTransaction().rollback();
			throw new DatabaseException(e.getMessage());
		} finally {
			if (!isOpenedSession)
				session.close();
		}
	}

	public static <T> List<T> executeNamedQuery(Class<T> dataClass,
			String queryName, Map<String, Object> parameters)
			throws DatabaseException, NoDataException {
		return executeUpdateNamedQuery(dataClass, queryName, parameters, false);
	}

	public static <T> List<T> updateNamedQuery(Class<T> dataClass,
			String queryName, Map<String, Object> parameters)
			throws DatabaseException, NoDataException {
		return executeUpdateNamedQuery(dataClass, queryName, parameters, true);
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> executeUpdateNamedQuery(Class<T> dataClass,
			String queryName, Map<String, Object> parameters, boolean update)
			throws DatabaseException, NoDataException {
		Session session = sessionFactory.openSession();

		try {
			Query q = session.getNamedQuery(queryName);

			if (parameters != null) {
				for (String paramName : parameters.keySet()) {
					Object value = parameters.get(paramName);

					if (value instanceof Integer)
						q.setInteger(paramName, (Integer) value);
					else if (value instanceof String)
						q.setString(paramName, (String) value);
					else if (value instanceof Long)
						q.setLong(paramName, (Long) value);
					else if (value instanceof Float)
						q.setFloat(paramName, (Float) value);
					else if (value instanceof Double)
						q.setDouble(paramName, (Double) value);
					else if (value instanceof Date)
						q.setDate(paramName, (Date) value);
					else if (value instanceof Object[])
						q.setParameterList(paramName, (Object[]) value);
				}
			}

			List<T> result = new ArrayList<T>();
			if (!update) {
				result = q.list();
			} else {
				List<Integer> resultInteger = new ArrayList<Integer>();
				resultInteger.add(q.executeUpdate());
				result = (List<T>) resultInteger;
			}

			if (result == null || result.size() == 0)
				throw new NoDataException("");

			return result;
		} catch (NoDataException e) {
			throw e;
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		} finally {
			session.close();
		}
	}

	public static <T> List<T> executeNativeQuery(String queryString,
			Map<String, Object> parameters) throws DatabaseException,
			NoDataException {
		Session session = sessionFactory.openSession();
		Query query = session.createSQLQuery(queryString);

		try {
			if (parameters != null) {
				for (String paramName : parameters.keySet()) {
					Object value = parameters.get(paramName);

					if (value instanceof Integer)
						query.setInteger(paramName, (Integer) value);
					else if (value instanceof String)
						query.setString(paramName, (String) value);
					else if (value instanceof Long)
						query.setLong(paramName, (Long) value);
					else if (value instanceof Float)
						query.setFloat(paramName, (Float) value);
					else if (value instanceof Double)
						query.setDouble(paramName, (Double) value);
					else if (value instanceof Date)
						query.setDate(paramName, (Date) value);
					else if (value instanceof Object[])
						query.setParameterList(paramName, (Object[]) value);
				}
			}

			@SuppressWarnings("unchecked")
			List<T> result = query.list();

			if (result == null || result.size() == 0)
				throw new NoDataException("");

			return result;
		} catch (NoDataException e) {
			throw e;
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		} finally {
			session.close();
		}
	}

	/**
	 * call a database procedure or function through a JDBC context. *
	 * 
	 * @param functionCall
	 *            the function call string as example :
	 *            "{? = call functionName(?, ?, ?)}"
	 * @param params
	 *            the function parameters
	 * @param pramsType
	 *            the parameters types, which can be <i>in</i> , <i>out</i> or
	 *            <i>inout</i>
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static HashMap<String, Object> callFunctionOrProcedure(
			final String functionCall, final HashMap<Integer, Object> params,
			final HashMap<Integer, Object> pramsType) {

		final HashMap<String, Object> result = new HashMap<String, Object>();
		Work work = new Work() {
			@Override
			public void execute(java.sql.Connection connection)
					throws SQLException {
				CallableStatement statement = null;
				statement = connection.prepareCall(functionCall);

				if (params != null)
					for (Integer param : params.keySet()) {
						Object o = params.get(param);
						if (o == null) {
							statement.setNull(param, Types.VARCHAR);
						} else if (o instanceof BigDecimal) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.DECIMAL);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setBigDecimal(param, (BigDecimal) o);
						}

						else if (o instanceof Integer) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.INTEGER);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setInt(param, (Integer) o);
						} else if (o instanceof Boolean) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement
										.registerOutParameter(param, Types.BIT);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setBoolean(param, (Boolean) o);
						} else if (o instanceof Long) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.BIGINT);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setLong(param, (Long) o);
						} else if (o instanceof Date) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.DATE);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setDate(param, new java.sql.Date(
										((Date) o).getTime()));
						}

						else if (o instanceof Double) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.DOUBLE);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setDouble(param, (Double) o);
						}

						else if (o instanceof Float) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.FLOAT);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setFloat(param, (Float) o);
						}

						else if (o instanceof String) {
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.OUT
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.registerOutParameter(param,
										Types.VARCHAR);
							if (pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN
											.getCode())
									|| pramsType
											.get(param)
											.toString()
											.equals(FunctionProcedureParameterType.IN_OUT
													.getCode()))
								statement.setString(param, (String) o);
						}
					}

				// Execute Statment
				statement.execute();
				connection.commit();

				for (Integer param : params.keySet()) {
					Object o = params.get(param);

					if (pramsType
							.get(param)
							.toString()
							.equals(FunctionProcedureParameterType.OUT
									.getCode())
							|| pramsType
									.get(param)
									.toString()
									.equals(FunctionProcedureParameterType.IN_OUT
											.getCode())) {
						if (o != null) {
							if (o instanceof BigDecimal) {
								result.put(param.toString(),
										statement.getBigDecimal(param));
							} else if (o instanceof Integer) {
								result.put(param.toString(),
										statement.getInt(param));

							} else if (o instanceof Boolean) {
								result.put(param.toString(),
										statement.getBoolean(param));

							} else if (o instanceof Long) {
								result.put(param.toString(),
										statement.getLong(param));

							} else if (o instanceof Date) {
								result.put(param.toString(),
										statement.getDate(param));

							} else if (o instanceof Double) {
								result.put(param.toString(),
										statement.getDouble(param));
							} else if (o instanceof Float) {
								result.put(param.toString(),
										statement.getFloat(param));

							} else if (o instanceof String) {
								result.put(param.toString(),
										statement.getString(param));
							}
						} else {
							result.put(param.toString(), null);
						}
					}
				}
			}
		};
		getSession().getSession().doWork(work);
		return result;
	}
}