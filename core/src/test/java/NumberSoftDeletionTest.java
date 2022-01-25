import entity.Visit;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberSoftDeletionTest extends BaseHibernateTest {

    @Test
    public void numberPropertySoftDeletedEntity() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilderImpl((SessionFactoryImpl) sessionFactory);
        CriteriaQuery<Visit> criteriaQuery = criteriaBuilder.createQuery(Visit.class);
        CriteriaQuery<Visit> appCriteriaQuery = criteriaQuery.select(criteriaQuery.from(Visit.class));

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            //Create sample data
            Visit visit1 = new Visit();
            visit1.setDate(new Date());
            visit1.setId(1L);
            session.save(visit1);

            Visit visit2 = new Visit();
            visit2.setDate(new Date());
            visit2.setId(2L);
            session.save(visit2);

            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            //load all entities
            List<Visit> visits = session.createQuery(appCriteriaQuery).list();
            assertEquals(2, visits.size());

            //soft delete entity with id 2
            Visit pet2 = session.get(Visit.class, 2L);
            session.delete(pet2);

            tx.commit();
        }
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            List<Visit> visits = session.createQuery(appCriteriaQuery).list();
            assertEquals(1, visits.size());

            tx.commit();
        }

        //restore entity
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("update Visit set deleted = null where id = 2").executeUpdate();
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            List<Visit> visits = session.createQuery(appCriteriaQuery).list();
            assertEquals(2, visits.size());

            tx.commit();
        }
    }
}
