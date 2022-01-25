import entity.Owner;
import entity.Pet;
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
import static org.junit.jupiter.api.Assertions.assertNull;

public class SimpleSoftDeletionTest extends BaseHibernateTest {


    @Test
    public void simpleSoftDeletedEntity() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilderImpl((SessionFactoryImpl) sessionFactory);
        CriteriaQuery<Pet> petCriteriaQuery = criteriaBuilder.createQuery(Pet.class);
        CriteriaQuery<Pet> appPetCriteriaQuery = petCriteriaQuery.select(petCriteriaQuery.from(Pet.class));

        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            Owner owner = new Owner();
            owner.setId(1L);
            owner.setName("Owner");
            owner.setPetList(new ArrayList<>());
            session.save(owner);

            //Create sample data
            Pet pet1 = new Pet();
            pet1.setName("Test1");
            pet1.setId(1L);
            pet1.setOwner(owner);
            session.save(pet1);

            Pet pet2 = new Pet();
            pet2.setName("Test2");
            pet2.setId(2L);
            pet2.setOwner(owner);
            session.save(pet2);

            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            //load all entities
            List<Pet> pets = session.createQuery(appPetCriteriaQuery).list();
            assertEquals(2, pets.size());

            Owner owner = session.get(Owner.class,1L);
            assertEquals(2, owner.getPetList().size());

            //soft delete entity with id 2
            Pet pet2 = session.get(Pet.class, 2L);
            session.delete(pet2);

            tx.commit();
        }
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();

            List<Pet> pets = session.createQuery(appPetCriteriaQuery).list();
            assertEquals(1, pets.size());

            Owner owner = session.get(Owner.class,1L);
            assertEquals(1, owner.getPetList().size());

            tx.commit();
        }

        //restore entity
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.createNativeQuery("update Pet set deletedDate = null where id = 2").executeUpdate();
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            List<Pet> pets = session.createQuery(appPetCriteriaQuery).list();
            assertEquals(2, pets.size());

            Owner owner = session.get(Owner.class,1L);
            assertEquals(2, owner.getPetList().size());
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Owner owner = session.get(Owner.class,1L);
            session.delete(owner);
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Pet pet = session.get(Pet.class,1L);
            assertNull(pet.getOwner());
            tx.commit();
        }
    }
}
