package cz.maku.housing.rest;

import cz.maku.mommons.Response;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ResponseValue<V> extends Response {

    @Getter
    private final V value;

    public ResponseValue(@Nullable Code code, @Nullable String content, @Nullable V value) {
        super(code, content);
        this.value = value;
    }
}
