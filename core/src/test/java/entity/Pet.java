package entity;

import io.github.zaharchenko.hibernate.softdelete.core.api.SoftDelete;
import io.github.zaharchenko.hibernate.softdelete.core.api.SoftDeleteColumn;

import javax.persistence.*;
import java.util.Date;

@Entity
@SoftDelete
public class Pet {

    @Id
    private Long id;

    private String name;

    @SoftDeleteColumn
    private Date deletedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Owner owner;

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

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
