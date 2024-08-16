package com.example.demo.mock;

import com.example.demo.common.domain.ClockHolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestClockHolder implements ClockHolder {

    private final long holder;

    @Override
    public long millis() {
        return holder;
    }
}
