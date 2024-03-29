package ovh.equino.actracker.jpa.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;

@Entity
@Table(name = "tag_share")
@NoArgsConstructor
@Getter
@Setter
public class TagShareEntity extends JpaEntity {

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private TagEntity tag;

    @Column(name = "grantee_id")
    private String granteeId;

    @Column(name = "grantee_name")
    private String granteeName;

}
