package com.hazebyte.crate.cratereloaded.model.mapper;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper
public interface CommonMapperUtil {

    @Named("wrapOptional")
    default <T> Optional<T> wrapOptional(T object) {
        return Optional.ofNullable(object);
    }

    @Named("unwrap")
    default <T> T unwrap(Optional<T> optional) {
        return optional.orElse(null);
    }
}
