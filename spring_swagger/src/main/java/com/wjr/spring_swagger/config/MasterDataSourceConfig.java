package com.wjr.spring_swagger.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 主数据源配置
 */
@Configuration
@MapperScan(basePackages = MasterDataSourceConfig.PACKAGE, sqlSessionFactoryRef = "masterSqlSessionFactory")
public class MasterDataSourceConfig extends BaseDataSourceConfig{

    /**
     * 配置多数据源 关键就在这里 这里配置了不同的mapper对应不同的数据源
     */
    static final String PACKAGE = "com.wjr.spring_swagger.mapper.master";
    static final String MAPPER_LOCATION = "classpath:mapper/master/*.xml";

    /**
     * 连接数据库信息 这个其实更好的是用配置中心完成
     */
    @Value("${master.datasource.url}")
    private String url;

    @Value("${master.datasource.username}")
    private String username;

    @Value("${master.datasource.password}")
    private String password;

    @Value("${master.datasource.driverClassName}")
    private String driverClassName;


    @Bean(name = "masterDataSource")
    @Primary //标志这个 Bean 如果在多个同类 Bean 候选时，该 Bean 优先被考虑。
    public DataSource masterDataSource() {
        DruidDataSource dataSource = currencyDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean(name = "masterTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(masterDataSource());
    }

    @Bean(name = "masterSqlSessionFactory")
    @Primary
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("masterDataSource") DataSource masterDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(masterDataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MasterDataSourceConfig.MAPPER_LOCATION));
        //开启驼峰命令转换
        sessionFactory.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
       //分页插件
       // Interceptor interceptor = new PageInterceptor();
       // Properties properties = new Properties();
       // //数据库
       // properties.setProperty("helperDialect", "clickhouse");
       // //是否将参数offset作为PageNum使用
       // properties.setProperty("offsetAsPageNum", "true");
       // //是否进行count查询
       // properties.setProperty("rowBoundsWithCount", "true");
       // //是否分页合理化
       // properties.setProperty("reasonable", "false");
       // interceptor.setProperties(properties);
       // sessionFactory.setPlugins(new Interceptor[] {interceptor});

        return sessionFactory.getObject();
    }
}
