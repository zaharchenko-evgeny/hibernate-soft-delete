package entity;

import ru.zaharchenko.hibernate.softdelete.core.api.SoftDelete;
import ru.zaharchenko.hibernate.softdelete.core.api.SoftDeleteColumn;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@SoftDelete
public class Visit {

    @Id
    private Long id;

    @Column(name = "DATE")
    private Date date;

    @SoftDeleteColumn
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
