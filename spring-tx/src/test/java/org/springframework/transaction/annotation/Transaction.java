package org.springframework.transaction.annotation;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.junit.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_NESTED;
import static org.springframework.transaction.annotation.Propagation.*;


/**
 * @author ☞ 🏀 huqingfeng
 * @date 2019-07-12
 */
public class Transaction {

	private static DataSource dataSource;

	static {
		MysqlDataSource dataSourceMysql = new MysqlDataSource();
		dataSourceMysql.setUrl("jdbc:mysql://182.254.131.140/pes_jd_sub_01?" +
				"serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true");
		dataSourceMysql.setUser("wangmeng");
		dataSourceMysql.setPassword("Ywc201405");
		dataSource = dataSourceMysql;
	}

	@Test
	public void test1() throws Exception {
		TestBean1 tb = new TestBean1();
		DataSourceTransactionManager ptm = new DataSourceTransactionManager(dataSource);
		AnnotationTransactionAttributeSource tas = new AnnotationTransactionAttributeSource();
		TransactionInterceptor ti = new TransactionInterceptor(ptm, tas);

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(ITestBean1.class);
		proxyFactory.addAdvice(ti);
		proxyFactory.setTarget(tb);
		ITestBean1 proxy = (ITestBean1) proxyFactory.getProxy();
		proxy.doSomething(proxy);
	}

	interface ITestBean1 {

		void doSomething(ITestBean1 iTestBean1) throws Exception;

		void doSomething1() throws Exception;
	}

	static class TestBean1 implements ITestBean1, Serializable {


		@Override
		@Transactional(rollbackFor = Exception.class)
		public void doSomething(ITestBean1 iTestBean1) throws Exception {

			// 必须使用 org.springframework.jdbc.datasource.DataSourceUtils.getConnection
			// 获取连接才能被spring的事务管理器管理
			Connection connection = DataSourceUtils.getConnection(dataSource);
			PreparedStatement preparedStatement = connection.prepareStatement(
					"insert into test(id,name) value (null , 'AA')");
			preparedStatement.execute();
			// 手动失败
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			iTestBean1.doSomething1();

			System.out.println(this);

		}

		@Override
		@Transactional(propagation = NESTED,rollbackFor = Exception.class)
		public void doSomething1() throws Exception {
			if (1==1)
			throw new RuntimeException();
			Connection connection = DataSourceUtils.getConnection(dataSource);
			PreparedStatement preparedStatement = connection.prepareStatement(
					"update test set name = 'BB' where id = 1");
			preparedStatement.execute();
		}
	}
}
