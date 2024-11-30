package com.sjc.async.callback;

import java.util.List;

/**
 * @author abstractMoonAstronaut
 * {@code @date} 2024/11/30
 * {@code @msg} reserved
 */
public interface IGroupCallback {

    void success (List<?> result);

    void failure (Exception e);
}
