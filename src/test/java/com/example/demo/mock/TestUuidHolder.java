package com.example.demo.mock;

import com.example.demo.common.domain.UuidHolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestUuidHolder implements UuidHolder {

    private final String holder;

    @Override
    public String random() {
        return holder;
    }
}
