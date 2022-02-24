package com.wjr.spring_swagger.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 次数据源 另一个数据源配置
 */
@Configuration
@MapperScan(basePackages = ClusterDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "clusterSqlSessionFactory")
public class ClusterDataSourceConfig  extends BaseDataSourceConfig {

    /**
     * 配置多数据源 关键就在这里 这里配置了不同的mapper对应不同的数据源
     */
    static final String PACKAGE = "com.wjr.spring_swagger.mapper.cluster";
    static final String MAPPER_LOCATION = "classpath:mapper/cluster/*.xml";

    @Value("${cluster.datasource.url}")
    private String url;

    @Value("${cluster.datasource.username}")
    private String username;

    @Value("${cluster.datasource.password}")
    private String password;

    @Value("${cluster.datasource.driverClassName}")
    private String driverClass;

    @Bean(name = "clusterDataSource")
    public DataSource clusterDataSource() {
        DruidDataSource dataSource = BaseDataSourceConfig.currencyDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClass);

        return dataSource;
    }

    @Bean(name = "clusterTransactionManager")
    public DataSourceTransactionManager clusterTransactionManager() {
        return new DataSourceTransactionManager(clusterDataSource());
    }

    @Bean(name = "clusterSqlSessionFactory")
    public SqlSessionFactory clusterSqlSessionFactory(@Qualifier("clusterDataSource") DataSource clusterDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(clusterDataSource);

//     //分页插件
//     Interceptor interceptor = new PageInterceptor();
//     Properties properties = new Properties();
//     //数据库
//     properties.setProperty("helperDialect", "mysql");
//     //是否将参数offset作为PageNum使用
//     properties.setProperty("offsetAsPageNum", "true");
//     //是否进行count查询
//     properties.setProperty("rowBoundsWithCount", "true");
//     //是否分页合理化
//     properties.setProperty("reasonable", "false");
//     interceptor.setProperties(properties);
//     sessionFactory.setPlugins(new Interceptor[] {interceptor});


        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(ClusterDataSourceConfig.MAPPER_LOCATION));
        return sessionFactory.getObject();
    }
}
