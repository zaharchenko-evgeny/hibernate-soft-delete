package entity;

import org.hibernate.annotations.Where;
import ru.zaharchenko.hibernate.softdelete.core.api.SoftDelete;
import ru.zaharchenko.hibernate.softdelete.core.api.SoftDeleteColumn;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@SoftDelete
@Where(clause = "deleted is null")
public class ExtendedEntity extends BaseEntity{

    @SoftDeleteColumn
    private Date deleted;

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }
}
