import entity.Clinic;
import entity.Pet;
import entity.Owner;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManyToManyTest extends BaseHibernateTest {

    @Test
    public void manyToMany() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilderImpl((SessionFactoryImpl) sessionFactory);
        CriteriaQuery<Clinic> clinicCriteriaQuery = criteriaBuilder.createQuery(Clinic.class);
        CriteriaQuery<Clinic> appClinicCriteriaQuery = clinicCriteriaQuery.select(clinicCriteriaQuery.from(Clinic.class));

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Owner owner1 = new Owner();
            owner1.setId(11L);
            owner1.setName("Owner 11");
            session.save(owner1);

            Owner owner2 = new Owner();
            owner2.setId(22L);
            owner2.setName("Owner 22");
            session.save(owner2);

            //Create sample data
            Clinic clinic = new Clinic();
            clinic.setName("Clinic");
            clinic.setId(1L);
            clinic.setOwnersList(new ArrayList<>());
            clinic.getOwnersList().add(owner1);
            clinic.getOwnersList().add(owner2);
            session.save(clinic);
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Clinic clinic = session.get(Clinic.class,1L);
            assertEquals(2, clinic.getOwnersList().size());

            //soft delete entity with id 2
            Owner owner = session.get(Owner.class, 11L);
            session.delete(owner);

            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Clinic clinic = session.get(Clinic.class,1L);
            assertEquals(1, clinic.getOwnersList().size());
            assertEquals(22L,clinic.getOwnersList().get(0).getId());

            tx.commit();
        }

        //restore entity
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("update Owner set isDeleted = false where id = 11").executeUpdate();
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Clinic clinic = session.get(Clinic.class,1L);
            assertEquals(2, clinic.getOwnersList().size());

            tx.commit();
        }
    }
}
