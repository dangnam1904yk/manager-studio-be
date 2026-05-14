package com.manager.studio.managerstudio.util;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.util.EnumSet;

public class UUIDv7Generator implements BeforeExecutionGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        // Sử dụng thư viện uuid-creator để tạo v7
        return UuidCreator.getTimeOrderedEpoch();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
//        @Override
//        public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
//            // Trả về .toString() thay vì để nguyên Object UUID
//            return UuidCreator.getTimeOrderedEpoch().toString();
//        }
//
//        @Override
//        public EnumSet<EventType> getEventTypes() {
//            return EventTypeSets.INSERT_ONLY;
//        }

}
