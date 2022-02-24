package com.wjr.spring_swagger.mapper.base;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.mapper.base
 * @ClassName: BaseMapper
 * @create 2022-02-12 20:58
 * @Description: BaseMapper
 */

import java.util.List;

public interface BaseMapper<T> {

    /**
     * 单条新增插入数据
     */
    void insert(T entity) throws Exception;


    /**
     * 批量新增据插入数据
     */
    int insertBatch(List<T> entityList) throws Exception;

    /**
     * 更新数据
     */
    void update(T entity) throws Exception;

    /**
     * 根据ID删除数据
     */
    void deleteByPrimaryKey(int id) throws Exception;

    /**
     * 删除数据
     */
    void delete(T entity) throws Exception;


    /**
     * 根据id查询单个记录
     */
    T findByPrimaryKey(int id);

    /**
     * 根据对象查询单个记录
     */
    T findByEntity(T entity);

    /**
     * 根据对象查询多条记录
     */
    List<T> findByListEntity(T entity);

    /**
     * 查询所有记录
     */
    List<T> findAll();

    /**
     * 根据对象查询信息
     */
    Object findByObject(Object obj);

    List getAllDevice();

    void insertOrUpdateBatch(List devicesList);


}
