package entity;

import io.github.zaharchenko.hibernate.softdelete.core.api.SoftDelete;
import io.github.zaharchenko.hibernate.softdelete.core.api.SoftDeleteColumn;

import javax.persistence.*;
import java.util.List;

@Entity
@SoftDelete
public class Owner {

    @Id
    private Long id;

    private String name;

    @SoftDeleteColumn
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Pet> petList;

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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public List<Pet> getPetList() {
        return petList;
    }

    public void setPetList(List<Pet> petList) {
        this.petList = petList;
    }
}
