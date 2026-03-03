package com.shieldgate.service;

import com.shieldgate.domain.RuleConfig;
import com.shieldgate.dto.RulesUpdateRequest;
import com.shieldgate.repository.RuleConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {

    private final RuleConfigRepository ruleConfigRepository;

    public RuleService(RuleConfigRepository ruleConfigRepository) {
        this.ruleConfigRepository = ruleConfigRepository;
    }

    @Transactional
    public RuleConfig getCurrentRule() {
        return ruleConfigRepository.findById(1L)
            .orElseGet(() -> ruleConfigRepository.save(RuleConfig.defaults()));
    }

    @Transactional
    public RuleConfig update(RulesUpdateRequest request) {
        RuleConfig rule = getCurrentRule();
        rule.setBaseLimitPerMinute(request.baseLimitPerMinute());
        rule.setThrottledLimitPerMinute(request.throttledLimitPerMinute());
        rule.setWarnThreshold(request.warnThreshold());
        rule.setThrottleThreshold(request.throttleThreshold());
        rule.setBanThreshold(request.banThreshold());
        rule.setBanMinutes(request.banMinutes());
        return ruleConfigRepository.save(rule);
    }
}
