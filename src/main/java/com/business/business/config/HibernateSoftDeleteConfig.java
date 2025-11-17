package com.business.business.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HibernateSoftDeleteConfig {

//    public HibernateSoftDeleteConfig(EntityManagerFactory entityManagerFactory) {
//        var sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
//        var registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
//
//        registry.appendListeners(EventType.LOAD, (LoadEventListener) (LoadEvent event, LoadEventListener.LoadType loadType) -> {
//            Session session = event.getSession();
//            // Automatically enable soft-delete filter if not active
//            if (!session.getEnabledFilterNames().contains("deletedFilter")) {
//                session.enableFilter("deletedFilter").setParameter("isDeleted", false);
//            }
//        });
//    }

    private final EntityManagerFactory entityManagerFactory;

    @PostConstruct //ensures that the method runs after dependency injection
    public void registerSoftDeleteListener() {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        ServiceRegistryImplementor serviceRegistry = sessionFactory.getServiceRegistry();
        EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);

        if (registry == null) {
            throw new IllegalStateException("EventListenerRegistry not found in Hibernate ServiceRegistry.");
        }

        registry.appendListeners(EventType.LOAD, (LoadEventListener) (LoadEvent event, LoadEventListener.LoadType loadType) -> {
            SessionImplementor session = (SessionImplementor) event.getSession();

            // Hibernate 6.x: access filters through LoadQueryInfluencers
            LoadQueryInfluencers influencers = session.getLoadQueryInfluencers();

            influencers.getEnabledFilters();
            if (!influencers.getEnabledFilters().containsKey("deletedFilter")) {
                session.enableFilter("deletedFilter").setParameter("isDeleted", false);
            }
        });
    }
}