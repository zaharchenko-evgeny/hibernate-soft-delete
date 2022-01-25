package entity;

import ru.zaharchenko.hibernate.softdelete.core.api.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.List;

@Entity
@SoftDelete(column = "DELETED_AT", type = Date.class)
public class Clinic {

    @Id
    private Long id;

    private String name;

    @ManyToMany
    private List<Owner> ownersList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<entity.Owner> getOwnersList() {
        return ownersList;
    }

    public void setOwnersList(List<entity.Owner> ownersList) {
        this.ownersList = ownersList;
    }

}
