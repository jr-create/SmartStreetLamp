package com.wjr.spring_swagger.bean.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.spring_swagger.bean.user
 * @ClassName: Status
 * @create 2022-03-10 23:00
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebResult {
    private Integer code;
    private Object data;
}
