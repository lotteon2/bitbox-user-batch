package attendance.batch.domain;

import attendance.batch.common.BaseEntity;
import io.github.bitbox.bitbox.enums.AuthorityType;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "member")
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "member_id")
    private String memberId;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "member_nickname", nullable = false)
    private String memberNickname;

    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @ColumnDefault("'https://mblogthumb-phinf.pstatic.net/MjAyMDExMDFfMTgy/MDAxNjA0MjI4ODc1NDMw.Ex906Mv9nnPEZGCh4SREknadZvzMO8LyDzGOHMKPdwAg.ZAmE6pU5lhEdeOUsPdxg8-gOuZrq_ipJ5VhqaViubI4g.JPEG.gambasg/%EC%9C%A0%ED%8A%9C%EB%B8%8C_%EA%B8%B0%EB%B3%B8%ED%94%84%EB%A1%9C%ED%95%84_%ED%95%98%EB%8A%98%EC%83%89.jpg?type=w800'")
    @Column(name = "member_profile_img", nullable = false, columnDefinition = "LONGTEXT")
    private String memberProfileImg;

    @ColumnDefault("0")
    @Column(name = "member_credit", nullable = false)
    private long memberCredit;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'GENERAL'")
    @Column(name = "member_authority", nullable = false)
    private AuthorityType memberAuthority;

    @ColumnDefault("false")
    @Column(name = "deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    public Member(Long classId, String memberNickname, String memberEmail, String memberProfileImg, long memberCredit, AuthorityType memberAuthority, boolean isDeleted) {
        this.classId = classId;
        this.memberNickname = memberNickname;
        this.memberEmail = memberEmail;
        this.memberProfileImg = memberProfileImg;
        this.memberCredit = memberCredit;
        this.memberAuthority = memberAuthority;
        this.isDeleted = isDeleted;
    }
}