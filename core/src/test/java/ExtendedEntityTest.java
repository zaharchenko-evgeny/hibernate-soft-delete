import entity.ExtendedEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExtendedEntityTest extends BaseHibernateTest {

    @Test
    public void extendedEntity() {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            ExtendedEntity baseEntity1 = new ExtendedEntity();
            baseEntity1.setId(11L);
            baseEntity1.setName("ExtendedEntity 11");
            session.save(baseEntity1);

            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            //soft delete entity with id 11
            ExtendedEntity extendedEntity = session.get(ExtendedEntity.class, 11L);
            session.delete(extendedEntity);

            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            ExtendedEntity extendedEntity = session.get(ExtendedEntity.class,11L);
            //TODO @Where do not applied to inherited classes
            assertNotEquals(null,extendedEntity);

            tx.commit();
        }

        //restore entity
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("update BaseEntity set deleted = null where id = 11").executeUpdate();
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            ExtendedEntity extendedEntity = session.get(ExtendedEntity.class,11L);
            assertEquals("ExtendedEntity 11", extendedEntity.getName());

            tx.commit();
        }
    }
}
