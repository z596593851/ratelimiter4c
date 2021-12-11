package com.github.ratelimiter4c.limiter.rule.source;

import java.util.List;

public interface AppLimitSource {
    List<AppLimitModel> load();
}
