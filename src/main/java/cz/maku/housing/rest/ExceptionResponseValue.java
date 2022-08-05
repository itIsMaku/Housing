package cz.maku.housing.rest;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExceptionResponseValue<V> extends ResponseValue<V> {

    @Getter
    private final Exception exception;

    public ExceptionResponseValue(@Nullable Code code, @Nullable String content, @NotNull Exception exception, @Nullable V value) {
        super(code, content, value);
        this.exception = exception;
    }
}
