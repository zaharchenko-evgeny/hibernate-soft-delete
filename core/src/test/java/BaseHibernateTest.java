import entity.Clinic;
import entity.Owner;
import entity.Pet;
import entity.Visit;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseHibernateTest {
    protected SessionFactory sessionFactory;

    @BeforeEach
    public void before() {
        // setup the session factory
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Pet.class)
                .addAnnotatedClass(Owner.class)
                .addAnnotatedClass(Clinic.class)
                .addAnnotatedClass(Visit.class);
        configuration.setProperty("hibernate.dialect",
                "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class",
                "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test_mem");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.show_sql", "true");

        sessionFactory = configuration.buildSessionFactory();

    }

    @AfterEach
    public void after() {
        sessionFactory.close();
    }
}
